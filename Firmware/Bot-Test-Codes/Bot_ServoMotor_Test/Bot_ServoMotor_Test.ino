// Servo Motor Test-Code for CVPRO Competition Kit
// for testing Peripherals
// Code Developed by Meritus Team - 16-10-2023
// Copyright © 2023 Meritus Team. All rights reserved.
// This program is the intellectual property of Meritus AI, and may not be distributed or reproduced without explicit authorization from the copyright holder.

//Library
#include <ESP32Servo.h>
Servo myServo;

void setup() {
  // Initialize Serial Monitor
  Serial.begin(115200);
  myServo.attach(27); // Servo Pin attached to 27
}

void loop() {
  if (Serial.available() > 0) {
    String input = Serial.readStringUntil('\n');// getting the string data from the serial monitor and saving to a variable input
    input.trim();
    int angle = input.toInt();
    angle = constrain(angle, 0, 180);
    myServo.write(angle);// writing the servo angle to servo
    delay(10);
  }
}
