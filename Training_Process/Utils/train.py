"""
CVPRO Competition v1.0.0. 
Code Developed by Augustin Rajkumar, Suresh Balaji, E.V.V Thrilok kumar, and Meritus R & D Team - November 09, 2023.
Copyright © 2023 Meritus R & D Team. All rights reserved.
This program is the intellectual property of Meritus AI, and may not be distributed 
or reproduced without explicit authorization from the copyright holder.
------------------------------------------------------------------------------------------------------------------
This script has code for Training the Neural Network.
"""

# Import Packages
import os
import numpy as np
from PIL import Image
import tensorflow
import seaborn as sns
import matplotlib.pyplot as plt
from sklearn.model_selection import train_test_split
from tensorflow.keras.utils import Sequence
from tensorflow.keras.callbacks import ModelCheckpoint
from sklearn.metrics import accuracy_score, precision_score, recall_score, f1_score, confusion_matrix

class TrainingModel:
    """
    This class depicts functions for training the model
    """
    def __init__(self, root_folder, images, controls, num_classes):
        """
        Initialize the arguments
        """
        self.root_folder = root_folder
        self.images_path = images
        self.labels = controls
        self.num_classes = num_classes
        self.save_folder = self.create_folder()

    # Train-test-validation split
    def split_data(self):
        """
        The function returns the splitting of the dataset
        """
        x_train, x_test, y_train, y_test = train_test_split(self.images_path, self.labels, test_size=0.2, shuffle=True)
        xtrain, x_val, ytrain, y_val = train_test_split(x_train, y_train, test_size=0.2, shuffle=True)
        print('Total Training Images is {} and Total Testing Images is {} and Total Testing Images is {}'.format(len(x_train), len(x_test), len(x_val)))
        return x_train, x_val, x_test, y_train, y_val, y_test

    class CustomDataGenerator(Sequence):
        """
        This class depicts the function for Custom data generator
        """
        def __init__(self, images_path, labels, batch_size):
            """
            Initialize the argument
            """
            self.images_path = images_path
            self.labels = labels
            self.batch_size = batch_size
        def __len__(self):
            """
            Getting length of Batch_size
            """
            return int(np.ceil(len(self.images_path) / float(self.batch_size)))
        def __getitem__(self, idx):
            """
            Getting a item(batch) one-by-one 
            """
            batch_paths = self.images_path[idx * self.batch_size : (idx + 1) * self.batch_size]
            batch_labels = self.labels[idx * self.batch_size : (idx + 1) * self.batch_size]
            batch_images = []
            for path in batch_paths:
                img = Image.open(path)
                # Example coordinates for cropping a 100 x 100 region from the top left corner
                left = 200
                upper = 100
                right = 500
                lower = 480
                img = img.crop((left, upper, right, lower))
                img = img.resize((200, 66)) #(width, height)
                img = np.asarray(img)
                img = img / 255
                batch_images.append(img)
            return np.array(batch_images), np.array(batch_labels)

    def create_folder(self):
        """
        Create the folder for saving model
        """
        save_folder = os.path.join(os.path.dirname(self.root_folder), "Save_Model")
        try:
            os.mkdir(save_folder)
            print("Save Folder is created successfully!")
        except FileExistsError:
            print("Save Folder already exists.")
        except Exception as folder_error:
            print("Error occurred:", str(folder_error))
        return save_folder

    def create_model(self, learning_rate):
        """
        Create the neural network model for training
        """
        # pilotnet input shape (66, 200, 3) as (height, width, channels)
        model = tensorflow.keras.Sequential(
            [
                tensorflow.keras.layers.InputLayer(input_shape=(66, 200, 3)),
                tensorflow.keras.layers.Conv2D(32, (5, 5), strides=(2, 2), activation="relu"),
                tensorflow.keras.layers.Conv2D(36, (5, 5), strides=(2, 2), activation="relu"),
                tensorflow.keras.layers.Conv2D(48, (5, 5), strides=(2, 2), activation="relu"),
                tensorflow.keras.layers.Conv2D(64, (3, 3), activation="relu"),
                tensorflow.keras.layers.Conv2D(64, (3, 3), activation="relu"),
                tensorflow.keras.layers.Flatten(),
                tensorflow.keras.layers.Dense(1164, activation="relu"),
                tensorflow.keras.layers.Dense(100, activation="relu"),
                tensorflow.keras.layers.Dense(50, activation="relu"),
                tensorflow.keras.layers.Dense(10, activation="relu"),
                tensorflow.keras.layers.Dense(self.num_classes, activation="softmax"),
            ]
        )
        model.summary()
        model.compile(
            tensorflow.keras.optimizers.Adam(learning_rate=learning_rate),
            loss=tensorflow.keras.losses.SparseCategoricalCrossentropy(),
            metrics=["Accuracy"],
        )
        return model

    def train(self, model, epochs, train_dataset, test_dataset):
        """
        This function is for Training the model and 
        to select the best and last checkpoint for the running model
        """
        # create checkpoint callback to save model checkpoints
        checkpoint_dir = os.path.join(self.save_folder, "checkpoints")
        os.makedirs(checkpoint_dir, exist_ok=True)
        early_stopping = tensorflow.keras.callbacks.EarlyStopping(monitor='val_loss', patience=3)
        checkpoint_callback = ModelCheckpoint(filepath=os.path.join(checkpoint_dir, "checkpoint_{epoch}"), save_weights_only=False)
        # train the model and save checkpoint after each epoch
        history = model.fit(train_dataset, epochs=epochs, validation_data=test_dataset,
                            callbacks=[checkpoint_callback, early_stopping], verbose=1)
        # find the best checkpoint based on validation loss
        best_checkpoint = None
        best_loss = float('inf')
        last_checkpoint = None
        for checkpoint in os.listdir(checkpoint_dir):
            if checkpoint.startswith("checkpoint"):
                loss = float(checkpoint.split("_")[1])
                if loss < best_loss:
                    best_loss = loss
                    best_checkpoint = os.path.join(checkpoint_dir, checkpoint)
                last_checkpoint = os.path.join(checkpoint_dir, checkpoint)
        return history, best_checkpoint, last_checkpoint

    def figure_val_loss(self, history):
        """
        Plot the validation loss and accuracy of model
        """
        # plt.figure(3)
        fig3, axs = plt.subplots(2, 1, figsize=(6, 6))
        # fig3.canvas.manager.set_window_title('Accuracy-Loss')
        axs[0].plot(history.history['loss'])
        axs[0].plot(history.history['val_loss'])
        axs[0].title.set_text('Training Loss vs Validation Loss')
        axs[0].legend(['Train', 'Val'])
        axs[1].plot(history.history['Accuracy'])
        axs[1].plot(history.history['val_Accuracy'])
        axs[1].title.set_text('Training Accuracy vs Validation Accuracy')
        axs[1].legend(['Train', 'Val'])
        plt.savefig(os.path.join(os.path.dirname(self.root_folder), 'Accuracy-Loss.png'))
        plt.show()

    def evaluate(self, model, test_generator):
        """
        To Evaluate the model
        """
        results = model.evaluate(test_generator, verbose=2)
        print("Test Loss : {:2f} and  Test Accuracy : {:.2f}".format(results[0], results[1]))

    def predict(self, model, test_generator):
        """
        To predict the model
        """
        predictions = model.predict(test_generator)
        print("Predictions: ", predictions)
        predict_classes = np.argmax(predictions, axis = 1)
        print(f"shape of the pred classes: {predict_classes.shape}")
        true_labels = test_generator.labels
        return true_labels, predict_classes

    def conf_matrix(self, true_labels, predict_classes):
        """
        Plotting the confusion matrix
        """
        conf_matrix = confusion_matrix(true_labels, predict_classes)
        sns.heatmap(conf_matrix, annot=True, fmt=".1f")
        # plt.figure(4)
        plt.ylabel("Prediction", fontsize=13)
        plt.xlabel("Actual", fontsize=13)
        plt.title("Confusion Matrix", fontsize=12)
        plt.savefig(os.path.join(os.path.dirname(self.root_folder), 'Confusion-Matrix.png'))
        plt.show()

    def train_metrics(self, true_labels, predict_classes):
        """
        Getting the mertics of model such as
        Accuracy, Precision, Recall, F1-score
        """
        accuracy = accuracy_score(true_labels, predict_classes)
        accuarcy_value = str(accuracy * 100)
        print("Accuracy  :", accuracy)
        precision = precision_score(true_labels, predict_classes, average=None)
        print("Precision :", precision)
        recall = recall_score(true_labels, predict_classes, average=None)
        print("Recall    :", recall)
        f_score = f1_score(true_labels, predict_classes, average=None)
        print("F1-score  :", f_score)
        return accuarcy_value

    def convert_to_tflite(self, accuarcy_value, best_checkpoint, last_checkpoint):
        """
        Convert checkpoint to TFlite 
        """
        # convert best_checkpoint to TFLite
        converter = tensorflow.lite.TFLiteConverter.from_saved_model(best_checkpoint)
        best_tflite_model = converter.convert()
        with open(os.path.join(self.save_folder, "best_model_labels.tflite"), 'wb') as f:
            f.write(best_tflite_model) 
        print("TFLite Model Files saved")    
        return best_tflite_model
        # # convert last_checkpoint to TFLite
        # converter = tensorflow.lite.TFLiteConverter.from_saved_model(last_checkpoint)
        # last_tflite_model = converter.convert()
        # with open(os.path.join(self.save_folder, "last_model.tflite"), 'wb') as f:
        #     f.write(last_tflite_model)
        # print("TFLite Model Files saved")
        # return best_tflite_model, last_tflite_model

    def save_labels(self, labels):
        """
        save the labels to text file for uploading to Mobile Application
        """
        with open(os.path.join(self.save_folder, "best_model_labels.txt"), 'w', encoding='utf-8') as f:
            for i, label in enumerate(labels):
                if i == len(labels) - 1:
                    f.write(label)
                else:
                    f.write(label + '\n')
        print("Labels Files saved")
        print("Training Progress is Completed Successfully")
