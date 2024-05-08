// ColorSensor Test-Code for CVPRO Competition Kit
// for testing Peripherals
// Meritus Team - 16-10-2023
// Meritus Team. All rights reserved.
// This program is the intellectual property of Meritus AI, and may not be distributed or reproduced without explicit authorization from the copyright holder.

// Library
#include <Wire.h>
#include "Adafruit_TCS34725.h"

///////////////////color sensor///////////////////

#define PCA9548A_ADDRESS 0x70  //mux address 0x70
#define TCS3414CS_ADDRESS 0x29 //ColorSensor address 0x29

Adafruit_TCS34725 tcs1 = Adafruit_TCS34725(TCS34725_INTEGRATIONTIME_154MS, TCS34725_GAIN_1X); // Initializing ColorSensor
Adafruit_TCS34725 tcs2 = Adafruit_TCS34725(TCS34725_INTEGRATIONTIME_154MS, TCS34725_GAIN_1X);

void setup() {
  Serial.begin(115200);
  analogReadResolution(12);       // Set ADC resolution to 12 bits (0-4095)
  analogSetAttenuation(ADC_0db);  // Set attenuation to 0dB (for full-scale voltage range)
}
void loop() {
  frontcoloursensor();
  backcoloursensor();
}
// Assigning the channel for I2C Communication
void TCA9548A(uint8_t bus) {
  Wire.beginTransmission(PCA9548A_ADDRESS);
  Wire.write(1 << bus);
  Wire.endTransmission();
  // delay(100);
}

//ColorSensor Function
void frontcoloursensor() {
  TCA9548A(0);
  //delay(100);
  uint16_t r, g, b, c;
  // Read the color data from the TCS34725 sensor
  tcs1.getRawData(&r, &g, &b, &c);
  // Calculate color temperature and lux
  uint16_t colorTemp = tcs1.calculateColorTemperature(r, g, b);
  uint16_t lux = tcs1.calculateLux(r, g, b);
  //Print the color data
  Serial.print("Red: ");
  Serial.print(r);
  Serial.print(" ");
  Serial.print("Green: ");
  Serial.print(g);
  Serial.print(" ");
  Serial.print("Blue: ");
  Serial.print(b);
  Serial.print(" ");
  Serial.print("Clear: ");
  Serial.print(c);
  Serial.print(" ");
  Serial.print("Color Temperature: ");
  Serial.print(colorTemp, DEC);
  Serial.print(" K ");
  Serial.print("Lux: ");
  Serial.println(lux, DEC);
  delay(1000);
  //Perform the logic here
}

void backcoloursensor() {
  TCA9548A(1);
  //delay(100);
  uint16_t r, g, b, c;
  // Read the color data from the TCS34725 sensor
  tcs1.getRawData(&r, &g, &b, &c);
  // Calculate color temperature and lux
  uint16_t colorTemp = tcs2.calculateColorTemperature(r, g, b);
  uint16_t lux = tcs2.calculateLux(r, g, b);
  //Print the color data
  Serial.print("Red: ");
  Serial.print(r);
  Serial.print(" ");
  Serial.print("Green: ");
  Serial.print(g);
  Serial.print(" ");
  Serial.print("Blue: ");
  Serial.print(b);
  Serial.print(" ");
  Serial.print("Clear: ");
  Serial.print(c);
  Serial.print(" ");
  Serial.print("Color Temperature: ");
  Serial.print(colorTemp, DEC);
  Serial.print(" K ");
  Serial.print("Lux: ");
  Serial.println(lux, DEC);
  delay(1000);
  //Perform the logic here as needed
}
