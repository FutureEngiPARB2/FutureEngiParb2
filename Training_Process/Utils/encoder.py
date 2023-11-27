"""
CVPRO Competition v1.0.0. 
Code Developed by Augustin Rajkumar, Suresh Balaji, E.V.V Thrilok kumar, and Meritus R & D Team - November 09, 2023.
Copyright © 2023 Meritus R & D Team. All rights reserved.
This program is the intellectual property of Meritus AI, and may not be distributed 
or reproduced without explicit authorization from the copyright holder.
-------------------------------------------------------------------------------------------------------------------
This script has the code for the Label-Encoding.
"""

# Import Packages
import os
import numpy as np
import pandas as pd
from tqdm import tqdm
import matplotlib.pyplot as plt
from sklearn.preprocessing import LabelEncoder

class Labeling:
    """
    This class depicts the function for labeling and encoding the label
    """
    def __init__(self, root_folder, csv_path):
        """
        Initialize
        """
        self.root_folder = root_folder
        self.csv_path = csv_path
        self.data, self.encoding_label, self.unique_values, self.decoding_label = self.encoder_label()
        self.value_counts = np.bincount(self.encoding_label)

    def encoder_label(self):
        """
        Encoding and Decoding the Target Values
        """
        data = pd.read_csv(self.csv_path)
        label_encoder = LabelEncoder()
        encoding_label = label_encoder.fit_transform(data["Label"])
        new_controls = encoding_label  # Assign transformed values directly
        data["Label"] = new_controls
        unique_values = label_encoder.classes_
        unique_control = np.unique(encoding_label)
        decoding_label = label_encoder.inverse_transform(unique_control)
        return data, encoding_label, unique_values, decoding_label

    def show_encode_plot(self):
        """
        Show the plot for encoded control values
        """
        # fig2 = plt.figure(2)
        # fig2.canvas.manager.set_window_title('Encoding Label')
        plt.bar(self.unique_values, self.value_counts, color='green')
        plt.xlabel('Unique Labels')
        plt.ylabel('Count')
        plt.title('Encode Labels')
        plt.savefig(os.path.join(os.path.dirname(self.root_folder), 'Encoding_Label.png'))
        plt.show()

    def num_label(self):
        """
        Show the number of classes
        """
        num_classes = len(self.unique_values)
        print("Number of classes: ", num_classes)
        print("unique_values", self.unique_values)
        print("value_counts", self.value_counts)
        return num_classes

    def load_data(self):
        """
        Loading the data from the csv to get features and target
        """
        images_path = []
        controls = []
        for i in tqdm(range(len(self.data))):
            images_path.append(os.path.join(self.root_folder, self.data["Image_Path"].iloc[i]))
            controls.append(self.data["Label"].iloc[i])
        images_path = np.asarray(images_path)
        controls = np.asarray(controls)
        return images_path, controls
