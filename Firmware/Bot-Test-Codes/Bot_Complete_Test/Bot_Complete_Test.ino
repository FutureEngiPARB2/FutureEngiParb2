// All Combination Test-Code for CVPRO Competition Kit
// for testing Peripherals
// Code Developed by Meritus Team - 16-10-2023
// Copyright © 2023 Meritus Team. All rights reserved.
// This program is the intellectual property of Meritus AI, and may not be distributed or reproduced without explicit authorization from the copyright holder.

#include <NewPing.h>
#include <Wire.h>
#include "Adafruit_TCS34725.h"
#include <ESP32Servo.h>

// Define the trigger and echo pins for ultrasonic sensors
#define FRONT_TRIGGER 12
#define RIGHT_TRIGGER 2
#define LEFT_TRIGGER 5
#define FRONT_ECHO 4
#define RIGHT_ECHO 23
#define LEFT_ECHO 18
#define BACK_TRIGGER 17
#define BACK_ECHO 19

// Define maximum distance values for ultrasonic sensors
#define MAX_DISTANCE1 400
#define MAX_DISTANCE2 400

// Define the I2C address for the PCA9548A multiplexer
#define PCA9548A_ADDRESS 0x70

// Create NewPing objects for ultrasonic sensors
NewPing sonar1(FRONT_TRIGGER, FRONT_ECHO, MAX_DISTANCE1);
NewPing sonar2(RIGHT_TRIGGER, RIGHT_ECHO, MAX_DISTANCE2);
NewPing sonar3(LEFT_TRIGGER, LEFT_ECHO, MAX_DISTANCE2);
NewPing sonar4(BACK_TRIGGER, BACK_ECHO, MAX_DISTANCE1);

// Create Adafruit_TCS34725 objects for color sensors
Adafruit_TCS34725 tcs1 = Adafruit_TCS34725(TCS34725_INTEGRATIONTIME_154MS, TCS34725_GAIN_1X);
Adafruit_TCS34725 tcs2 = Adafruit_TCS34725(TCS34725_INTEGRATIONTIME_154MS, TCS34725_GAIN_1X);

// Define motor control pins, speed, and enable pin
const int motorPin1 = 33;
const int motorPin2 = 32;
const int motorSpeed = 255;
const int motorEnablePin = 13;

// Create a Servo object
Servo myServo;

// Function prototypes
void frontColorSensor();
void backColorSensor();

void setup() {
  // Start serial communication for debugging
  Serial.begin(115200);
  
  // Initialize the I2C bus
  Wire.begin();

  // Set analog read resolution and attenuation
  analogReadResolution(12);
  analogSetAttenuation(ADC_0db);

  // Set the motor enable pin as an output and enable the motors
  pinMode(motorEnablePin, OUTPUT);
  digitalWrite(motorEnablePin, HIGH);

  // Configure LEDC PWM channels for motor control
  ledcSetup(0, 5000, 8);
  ledcSetup(1, 5000, 8);

  // Attach the LEDC channels to the motor pins
  ledcAttachPin(motorPin1, 0);
  ledcAttachPin(motorPin2, 1);

  // Attach the servo to a pin
  myServo.attach(27);
}

void loop() {
  // Read ultrasonic sensor values
  readUltrasonicSensors();

  // Read front color sensor
  frontColorSensor();

  // Read back color sensor
  backColorSensor();

  // Move the motors forward for 2 seconds
  moveForward();
  delay(2000);

  // Move the motors backward for 2 seconds
  moveBackward();
  delay(2000);

  // Stop the motors for 1 second
  stopMotors();
  delay(1000);

  // Adjust servo angle based on input (if available)
  adjustServoAngle();
}

void readUltrasonicSensors() {
  // Read values from ultrasonic sensors
  unsigned int frontSensorValue = sonar1.ping_cm();
  unsigned int leftSensorValue = sonar2.ping_cm();
  unsigned int rightSensorValue = sonar3.ping_cm();
  unsigned int backSensorValue = sonar4.ping_cm();

  // Print sensor values to the serial monitor
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
}

void frontColorSensor() {
  // Select the front color sensor via the multiplexer
  TCA9548A(0);

  // Read and print color sensor values
  uint16_t r, g, b, c;
  tcs1.getRawData(&r, &g, &b, &c);
  uint16_t colorTemp = tcs1.calculateColorTemperature(r, g, b);
  uint16_t lux = tcs1.calculateLux(r, g, b);

  Serial.print("Front Color Sensor - Red: ");
  Serial.print(r);
  Serial.print(" Green: ");
  Serial.print(g);
  Serial.print(" Blue: ");
  Serial.print(b);
  Serial.print(" Clear: ");
  Serial.print(c);
  Serial.print(" Color Temperature: ");
  Serial.print(colorTemp);
  Serial.print(" K Lux: ");
  Serial.println(lux);
}

void backColorSensor() {
  // Select the back color sensor via the multiplexer
  TCA9548A(1);

  // Read and print color sensor values
  uint16_t r, g, b, c;
  tcs2.getRawData(&r, &g, &b, &c);
  uint16_t colorTemp = tcs2.calculateColorTemperature(r, g, b);
  uint16_t lux = tcs2.calculateLux(r, g, b);

  Serial.print("Back Color Sensor - Red: ");
  Serial.print(r);
  Serial.print(" Green: ");
  Serial.print(g);
  Serial.print(" Blue: ");
  Serial.print(b);
  Serial.print(" Clear: ");
  Serial.print(c);
  Serial.print(" Color Temperature: ");
  Serial.print(colorTemp);
  Serial.print(" K Lux: ");
  Serial.println(lux);
}

void TCA9548A(uint8_t bus) {
  // Select a specific bus (sensor) via the multiplexer
  Wire.beginTransmission(PCA9548A_ADDRESS);
  Wire.write(1 << bus);
  Wire.endTransmission();
}

void moveForward() {
  // Move the motors forward
  ledcWrite(0, motorSpeed);
  ledcWrite(1, 0);
}

void moveBackward() {
  // Move the motors backward
  ledcWrite(0, 0);
  ledcWrite(1, motorSpeed);
}

void stopMotors() {
  // Stop the motors
  ledcWrite(0, 0);
  ledcWrite(1, 0);
}

void adjustServoAngle() {
  // Adjust the servo angle based on input from the serial monitor (if available)
  if (Serial.available() > 0) {
    String input = Serial.readStringUntil('\n');
    input.trim();
    int angle = input.toInt();
    angle = constrain(angle, 0, 180);
    myServo.write(angle);
    delay(10);
  }
}
