"""
CVPRO Competition v1.0.0. 
Code Developed by Augustin Rajkumar, Suresh Balaji, E.V.V Thrilok kumar, and Meritus R & D Team - November 09, 2023.
Copyright © 2023 Meritus R & D Team. All rights reserved.
This program is the intellectual property of Meritus AI, and may not be distributed 
or reproduced without explicit authorization from the copyright holder.
-------------------------------------------------------------------------------------------------------------------
This script helps in training your model based on your data collected.

Important Note: 
        While the code is running in Anaconda prompt or Powershell,
        the user need to enter the batch_size, epochs, learning_rate.
        Here, lr is the learning rate, it should be in float value only (not integer)

To run in Anaconda prompt, 
   >>python main.py --batch 64 --epoch 20 --lr 0.001

"""

# Import 
import sys
import argparse
from Utils.createdataset import CreateDataset
from Utils.encoder import Labeling
from Utils.train import TrainingModel

# Parameters of Batch-size, Epochs and Learning-rate
try:
    parser = argparse.ArgumentParser()
    parser.add_argument("-b", type=int, default=32, help="Enter the Batch Size")
    parser.add_argument("-e", type=int, default=5, help="Enter the Epochs")
    parser.add_argument("-lr", type=float, default=0.0001, help="Enter the Learning rate")

    args = parser.parse_args()

    # Validate batch size
    valid_batch_sizes = [16, 32, 64, 128]
    batch_size = args.b

    if batch_size not in valid_batch_sizes:
        print(f"Invalid batch size: {batch_size}. Please enter a valid value: {valid_batch_sizes}")

    # Validate epochs
    valid_epochs = [5, 10, 20, 25, 50, 100]
    epochs = args.e

    if epochs not in valid_epochs:
        print(f"Invalid number of epochs: {epochs}. Please enter a valid value: {valid_epochs}")

    # Validate learning rate
    valid_learning_rates = [0.00001, 0.0001, 0.001, 0.01, 0.1, 1]
    learning_rate = args.lr

    if learning_rate not in valid_learning_rates:
        print(f"Invalid learning rate: {learning_rate}. Please enter a valid value: {valid_learning_rates}")

    # Exit if any invalid argument is encountered
    if (batch_size not in valid_batch_sizes) or (epochs not in valid_epochs) or (learning_rate not in valid_learning_rates):
        sys.exit()

except argparse.ArgumentError as e:
    print(f"An error occurred while parsing arguments: {e}")
except TypeError as e:
    print(f"Error: {e}")
except Exception as e:
    print(f"An unexpected error occurred: {e}")

# Create a Dataset
preprocess = CreateDataset()
root_folder, log_file_path = preprocess.main_folder()
folder_list = preprocess.path_folders()

# To save the dataframe of each folder
save_df = preprocess.data_frame()
csv_path = preprocess.list_to_dataframe(save_df)

# To show the random_images from the datasets
preprocess.random_images(csv_path)

# Encoding the labels
encode_label = Labeling(root_folder, csv_path) 
data, encoding_label, unique_values, decoding_label = encode_label.encoder_label()

# To show the plot of labels with their counts
encode_label.show_encode_plot()

# No.of Classes for the training neural network
num_classes = encode_label.num_label()

# Getting Images and Controls values
images_path, controls = encode_label.load_data()

# Create an instance of TrainingModel
training_model = TrainingModel(root_folder, images_path, controls, num_classes)

# Split the data
x_train, x_val, x_test, y_train, y_val, y_test = training_model.split_data()

# Create the custom data generator
train_generator = training_model.CustomDataGenerator(x_train, y_train, batch_size)
val_generator = training_model.CustomDataGenerator(x_val, y_val, batch_size)
test_generator = training_model.CustomDataGenerator(x_test, y_test, batch_size)

# Create the model
model = training_model.create_model(learning_rate)

# Train the model
history, best_checkpoint, last_checkpoint = training_model.train(model, epochs, train_generator, val_generator)

# Plot the validation loss and accuracy of model
training_model.figure_val_loss(history)

# Evaluate the model
training_model.evaluate(model, test_generator)

# Predict with the model
true_labels, predict_classes = training_model.predict(model, test_generator)

# Calculate metrics
training_model.conf_matrix(true_labels, predict_classes)
ACCURACY = training_model.train_metrics(true_labels, predict_classes)

# Convert to TFLite
training_model.convert_to_tflite(ACCURACY, best_checkpoint, last_checkpoint)

# Save labels
training_model.save_labels(decoding_label)
