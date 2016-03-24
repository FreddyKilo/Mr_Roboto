
#include <SoftwareSerial.h>
#include <Servo.h>

char value;
String number = "";

Servo myServo;
SoftwareSerial mySerial(0, 1); // RX, TX
int LED_PIN = 13;
int SERVO_PIN = 6;
   
void setup() {
  mySerial.begin(9600);
  myServo.attach(SERVO_PIN);
  myServo.write(90);
  pinMode(LED_PIN, OUTPUT);
}
 
void loop()  {
  while(mySerial.available() > 0){
    value = mySerial.read();
    number = number + value;
  }
  if (number.length() > 0){
    myServo.write(number.toInt());
    number = ""; 
  }
}

