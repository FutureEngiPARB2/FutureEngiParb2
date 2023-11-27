// CVPRO Competition. 
// Code Developed by Meritus R & D Team - 07-11-2023
// Copyright © 2023. Meritus R & D Team. All rights reserved.

#include <ESP32Servo.h>

Servo myServo;

const int motorPin1 = 33;  // Input 1 for motor control
const int motorPin2 = 32;  // Input 2 for motor control
const int motorEnablePin = 13;

void setup() {
  // WRITE_PERI_REG(RTC_CNTL_BROWN_OUT_REG, 0);
  // Initialize Serial Monitor
  Serial.begin(115200);
  
  pinMode(motorPin1, OUTPUT);
  pinMode(motorPin2, OUTPUT);
  pinMode(motorEnablePin, OUTPUT);
  digitalWrite(motorEnablePin, HIGH);  // Enable motors initially
  myServo.attach(27);
}


// Function to move both motors forward
void moveForward() {
  digitalWrite(motorPin1, HIGH);
  digitalWrite(motorPin2, LOW);
}

void movebackward() {
  digitalWrite(motorPin1, LOW);
  digitalWrite(motorPin2, HIGH);
}

void stopmotors() {
  digitalWrite(motorPin1, HIGH);
  digitalWrite(motorPin2, HIGH);
}

void loop() {
 
  if (Serial.available() > 0) {
    // String rc = Serial.readStringUntil('\n');
    char rc = Serial.read();

    if (rc == 'w') {
       myServo.write(100);
       moveForward();
     }

    if (rc == 'a') {
      myServo.write(80);
      moveForward();
    }

    if (rc == 'd') {
      myServo.write(120);
      moveForward();
    }

    if (rc == 's') {
      myServo.write(100);
      movebackward();
    }

    if (rc == 'o') {
      myServo.write(100);
      stopmotors();
    }    

  }
}
