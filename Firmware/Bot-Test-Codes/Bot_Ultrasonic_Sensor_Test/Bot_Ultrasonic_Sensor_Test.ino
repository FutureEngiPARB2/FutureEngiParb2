// Ultrasonic Sensor Test-Code for CVPRO Competition Kit
// for testing Peripherals
// Code Developed by Meritus Team - 16-10-2023
// Copyright © 2023 Meritus Team. All rights reserved.
// This program is the intellectual property of Meritus AI, and may not be distributed or reproduced without explicit authorization from the copyright holder.

#include <NewPing.h>

#define FRONT_TRIGGER 4  // Arduino pin tied to the trigger pin on the ultrasonic sensor.
#define RIGHT_TRIGGER 2
#define LEFT_TRIGGER  5
#define FRONT_ECHO  12  // Arduino pin tied to the echo pin on the ultrasonic sensor.
#define RIGHT_ECHO  18
#define LEFT_ECHO  23
#define BACK_TRIGGER 17
#define BACK_ECHO 19
#define MAX_DISTANCE1 400 // Maximum distance we want to ping for (in microseconds). Maximum sensor distance is rated at 400-500cm.
#define MAX_DISTANCE2 400

NewPing sonar1(FRONT_TRIGGER, FRONT_ECHO, MAX_DISTANCE1); 
NewPing sonar2(RIGHT_TRIGGER, RIGHT_ECHO, MAX_DISTANCE2); 
NewPing sonar3(LEFT_TRIGGER, LEFT_ECHO, MAX_DISTANCE2);
NewPing sonar4(BACK_TRIGGER, BACK_ECHO, MAX_DISTANCE1); 

void setup() {
  Serial.begin(115200); // Open serial monitor at 9600 baud to see the results.
}

void loop() {
  // Read raw data (pulse duration) from the ultrasonic sensors.
  unsigned int frontSensorValue = sonar1.ping_cm(); 
  unsigned int leftSensorValue = sonar2.ping_cm(); 
  unsigned int rightSensorValue = sonar3.ping_cm(); 
  unsigned int backSensorValue = sonar4.ping_cm();

  // Print distance to the serial monitor.
  Serial.print("Front Sensor (cm): ");
  Serial.print(frontSensorValue);
  Serial.print(" cm\t");

  Serial.print("Left Sensor (cm): ");
  Serial.print(leftSensorValue);
  Serial.print(" cm\t");

  Serial.print("Right Sensor (cm): ");
  Serial.print(rightSensorValue);
  Serial.print(" cm\t");

  Serial.print("Back Sensor (cm): ");
  Serial.print(backSensorValue);
  Serial.print(" cm\t");

  Serial.println();

  // Add a delay
  delay(1000);
}
