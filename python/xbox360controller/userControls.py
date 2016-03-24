#!/usr/bin/env python

import XboxController
import serial
from time import sleep
import os, sys
import RPi.GPIO as GPIO

OFF = 0
ON = 1
LIGHTS_PIN = 7
LIGHTS = OFF
CAMERA = OFF
SERIAL = serial.Serial("/dev/ttyAMA0", 9600)
GPIO.setwarnings(False)
GPIO.setmode(GPIO.BOARD)
GPIO.setup(LIGHTS_PIN, GPIO.OUT)
CAMERA_ANGLE = 90

def controlCallBack(xboxControlId, value):
    print("Control ID: " + str(xboxControlId))
    options[xboxControlId](value)

def switchLights(value):
    global LIGHTS
    if LIGHTS == OFF and value == 1:
        GPIO.output(LIGHTS_PIN, ON)
        LIGHTS = ON
    elif LIGHTS == ON and value == 1:
        GPIO.output(LIGHTS_PIN, OFF)
        LIGHTS = OFF

def switchCamera(value):
    global CAMERA
    if CAMERA == OFF and value == 1:
        CAMERA = ON
    elif CAMERA == ON and value == 1:
        CAMERA = OFF

def adjustCamera(value):
    if value[1] == 0:
        return None
    global CAMERA_ANGLE
    if value[1] == 1 and CAMERA_ANGLE < 170:
        CAMERA_ANGLE += 5
        SERIAL.write(str(CAMERA_ANGLE))
    elif value[1] == -1 and CAMERA_ANGLE > 10:
        CAMERA_ANGLE -= 5
        SERIAL.write(str(CAMERA_ANGLE))
    print "Camera angle: {}".format(CAMERA_ANGLE)

def cameraY(value):
    global CAMERA_ANGLE
    angle = str(value/2 + CAMERA_ANGLE).split(".")[0]
    print("Camera angle: " + angle)
    SERIAL.write(angle)

def leftStickX(value):
    pass

options = {6 : switchLights,
           9 : switchCamera,
          17 : adjustCamera,
           1 : cameraY,
           0 : leftStickX}

xbox360 = XboxController.XboxController(controlCallBack, deadzone = 0, scale = 90, invertYAxis = True)

try:
    xbox360.start()
    print "xbox controller running"
    while True:
        sleep(1)

except KeyboardInterrupt:
    print "User cancelled"

except:
    print "Unexpected error:", sys.exc_info()[0]
    raise

finally:
    xbox360.stop()
    GPIO.cleanup()
