
#include <SoftwareSerial.h>
#include <Servo.h>

char value;
String input = "";
int angle;

Servo servoX;
Servo servoY;
int SERVO_PIN_X = 11;
int SERVO_PIN_Y = 10;
SoftwareSerial mySerial(0, 1); // RX, TX

   
void setup() {
  mySerial.begin(9600);
  servoX.attach(SERVO_PIN_X);
  servoY.attach(SERVO_PIN_Y);
  servoX.write(90);
  servoY.write(90);
}
 
void loop()  {
  while(mySerial.available() > 0){
    value = mySerial.read();
    input = input + value;
  }
  if (input.length() > 3){
    angle = input.substring(2).toInt();
    if (input.charAt(0) == 'x'){
      servoX.write(angle);
    } else if (input.charAt(0) == 'y'){
      servoY.write(angle);
    }
    input = "";
  }
}

