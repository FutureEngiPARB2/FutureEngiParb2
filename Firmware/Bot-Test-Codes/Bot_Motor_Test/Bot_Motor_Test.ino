// Motor Test-Code for CVPRO Competition Kit
// for testing Peripherals
// Code Developed by and Meritus Team - 16-10-2023
// Copyright © 2023 Meritus Team. All rights reserved.
// This program is the intellectual property of Meritus AI, and may not be distributed or reproduced without explicit authorization from the copyright holder.

// Define the pin numbers for the motor driver connections
const int motorPin1 = 32;    // Motor driver IN1 pin
const int motorPin2 = 33;    // Motor driver IN2 pin

// Define the motor speed value (0 to 255) for full speed
const int motorSpeed = 255;  // Motor speed (0 to 255)

// Define the pin number for enabling/disabling the motors
const int motorEnablePin = 13;

void setup() {
  // Set the motor enable pin as an output
  pinMode(motorEnablePin, OUTPUT);

  // Enable the motors initially by setting the enable pin to HIGH
  digitalWrite(motorEnablePin, HIGH);

  // Configure the LEDC PWM library for motor control
  ledcSetup(0, 5000, 8);  // Motor channel 0, frequency 5000 Hz, 8-bit resolution
  ledcSetup(1, 5000, 8);  // Motor channel 1, frequency 5000 Hz, 8-bit resolution

  // Attach the LEDC channels to the motor pins
  ledcAttachPin(motorPin1, 0);  // Attach motorPin1 to LEDC channel 0
  ledcAttachPin(motorPin2, 1);  // Attach motorPin2 to LEDC channel 1

  // Initialize serial communication for debugging (optional)
  Serial.begin(115200);
}

// Function to move both motors forward
void moveForward() {
  // Set the speed of motor 1 to 0 (stop motor 1)
  ledcWrite(0, 0);

  // Set the speed of motor 2 to the specified motorSpeed value for forward motion
  ledcWrite(1, motorSpeed);
}

// Function to stop both motors
void stopMotors() {
  // Set the enable pin to LOW to disable both motors
  // digitalWrite(motorEnablePin, LOW);

  // Set the speed of both motors to 0 to stop them
  ledcWrite(0, 0);
  ledcWrite(1, 0);
}

void loop() {
  // Example usage:

  // Move both motors forward
  moveForward();

  // Keep moving forward for 2 seconds
  delay(2000);

  // Stop the motors
  stopMotors();

  // Pause for 1 second
  delay(1000);

  // You can add additional motor control actions here as needed
}
