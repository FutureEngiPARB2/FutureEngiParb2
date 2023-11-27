"""
CVPRO Competition v1.0.0. 
Code Developed by Augustin Rajkumar, Suresh Balaji, E.V.V Thrilok kumar, and Meritus R & D Team - November 09, 2023.
Copyright © 2023 Meritus R & D Team. All rights reserved.
This program is the intellectual property of Meritus AI, and may not be distributed 
or reproduced without explicit authorization from the copyright holder.
-------------------------------------------------------------------------------------------------------------------
This script has the code for processing the Dataset.
"""

# Import Packages
import os
import time
import csv
import cv2
import pandas as pd
from tqdm import tqdm
from PIL import Image
import matplotlib.pyplot as plt
from sklearn.utils import shuffle

class CreateDataset:
    """
    This class depicts the function for converting raw data to processed dataset 
    """
    def __init__(self):
        """
        Initialize
        """
        self.missing_file = "MissingFile.log"
        self.root_folder, self.log_file_path = self.main_folder()
        print("Training Progress is Started....")

    def main_folder(self):
        """
        Change the directory to the root folder and create the log file for missing elements
        """
        root_path = 'Training_Data/Dataset_CVPRO'# root-path of Datasets
        root_folder = os.path.join(os.getcwd(), root_path) # root_folder
        log_file_path = os.path.join(root_path.split("/", maxsplit=1)[0], self.missing_file)
        # print("Log File Path:", log_file_path)
        if not os.path.exists(log_file_path):
            with open(log_file_path,'w', encoding='utf-8') as file:
                file.write("**** This File contains the error message of Missing File ****\n")
        return root_folder, log_file_path

    def path_folders(self):
        """
        Rooting the current working directory to "Images" and "Files"
        """
        folder_list = []
        for dataset_folder in os.listdir(self.root_folder):
            dataset_folder_path = os.path.join(self.root_folder, dataset_folder)
            # print("Dataset_Folder :", dataset_folder_path)
            if os.path.isdir(dataset_folder_path):
                print("Dataset_Folder :", dataset_folder_path)
                for list_folder in os.listdir(dataset_folder_path):
                    list_folder_path = os.path.join(dataset_folder_path, list_folder)
                    print("List_Folder :", list_folder_path)
                    if os.path.isdir(list_folder_path):
                        for folder in os.listdir(list_folder_path):
                            inside_folder_path = os.path.join(list_folder_path, folder)
                            print("Inside_Folder_Path :", inside_folder_path)
                            if os.path.isdir(inside_folder_path):
                                folder_list.append(inside_folder_path)
                            else:
                                print("Inside Folder is not Folder")
                    else:
                        print("The Folder is not Present in List_Folder")
            else:
                print("No! Folder is not present.")
        return folder_list

    def appending_csv(self, csv_path, path):
        """
        Appending the CSV from folders to a new csv files
        """
        try:
            data = pd.read_csv(csv_path)
            if data.shape[1] == 2:
                csv_data = pd.read_csv(csv_path, header=None, names=["Image_Path", "Label"], na_values=[""])
                csv_data.dropna(subset=["Image_Path", "Label"], inplace=True)
                csv_data = csv_data[csv_data.Label != "s"]
            else:
                print("Invalid CSV format. Expected either 2 or 3 columns.")
            csv_data["Image_Path"] = csv_data["Image_Path"].apply(lambda x: os.path.join(path, x))
            return csv_data
        except pd.errors.EmptyDataError:
            print(f"File {csv_path} is empty.")
            return pd.DataFrame()

    def self_driving(self, folder_path, dataset_folder, save_df):
        """
        Self-Driving
        """
        full_path = os.path.join(folder_path, dataset_folder)
        # print(f"Full path: {full_path}")
        for actual_dataset in os.listdir(full_path):
            actual_dataset_path = os.path.join(full_path, actual_dataset)
            # print(f"Actual-Dataset Path:{actual_dataset_path}")
            for dataset_folder in os.listdir(actual_dataset_path):
                dataset_path = os.path.join(actual_dataset_path, dataset_folder)
                # print(f"Dataset_Folder:{dataset_path}")
                folder_path = os.path.join(dataset_path, 'Files/', "Frame.csv")
                print(f"Folder_Path:{folder_path}")
                try:
                    csv_data = self.appending_csv(folder_path, actual_dataset_path)
                    save_df.append(csv_data)
                except Exception as csv_error:
                    print(f"Error processing CSV: {folder_path}")
                    print(f"Error message: {str(csv_error)}")

    # main function for list-of-folders
    def data_frame(self):
        """
        Save Dataframe in list
        """
        save_df = []
        for folder in os.listdir(self.root_folder):
            print(folder)
            if folder == "Self_Driving":
                self.self_driving(self.root_folder, folder, save_df)
        return save_df

    def list_to_dataframe(self, save_df):
        """
        To save the list to dataframe of csv
        """
        save_dataframe = pd.concat(save_df)
        shuffled = shuffle(save_dataframe, random_state=42)
        dataframe_path = os.path.join(os.path.dirname(self.root_folder), "cvpro.csv")
        try:
            if os.path.exists(dataframe_path) and os.path.isfile(dataframe_path):
                os.remove(dataframe_path)
                print("Already, CSV File delete and create a new CSV file")
                print(f"dataframe path: {dataframe_path}")
                # shuffled['Image_Path'] = shuffled['Image_Path'].replace('Images', 'Process_Image')
                # shuffled['Image_Path'] = shuffled['Image_Path'].apply(lambda x: x.replace('Images', 'Process_Image'))
                shuffled.to_csv(dataframe_path, index=False)
            else:
                print("The CSV File not found. Please, Continue.... ")
                print(f"dataframe path: {dataframe_path}")
                # shuffled['Image_Path'] = shuffled['Image_Path'].replace('Images', 'Process_Image')
                # shuffled['Image_Path'] = shuffled['Image_Path'].apply(lambda x: x.replace('Images', 'Process_Image'))
                shuffled.to_csv(dataframe_path, index=False)
            return dataframe_path
        except Exception as dataframe_error:
            print(f"Error saving dataframe: {str(dataframe_error)}")
            return None

    def random_images(self, csv_path):
        """
        Show the random_images and its respective control_values
        """
        data = pd.read_csv(csv_path)
        selected_images = data.sample(n=9) #num-images is 9 (default, we fixed)
        # plt.figure(1)
        fig, axes = plt.subplots(nrows=3, ncols=3, figsize=(8, 8))
        axes = axes.flatten()
        for i, (_, row) in enumerate(selected_images.iterrows()):
            image_path = row["Image_Path"]
            label = row["Label"]
            image = Image.open(image_path)
            axes[i].imshow(image)
            axes[i].set_title(label)
            axes[i].axis("off")
        plt.savefig(os.path.join(os.path.dirname(self.root_folder), 'Random_Images_Labels.png'))
        plt.show()
