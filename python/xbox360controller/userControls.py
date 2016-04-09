#!/usr/bin/env python

import XboxController
import serial
import os, sys
import RPi.GPIO as GPIO
from time import sleep
from Fart import gas

OFF = 0
ON = 1
LIGHTS = OFF
CAMERA = OFF
SERIAL = serial.Serial("/dev/ttyAMA0", 9600)
CAMERA_X_ANGLE = 90
CAMERA_Y_ANGLE = 90
CURRENT_X_ANGLE = CAMERA_X_ANGLE
CURRENT_Y_ANGLE = CAMERA_Y_ANGLE

"""this sets up the GPIO pins (GPIO.BOARD) to be used
with a TB6612FNG motor controller as labeled"""
AIN1 = 3 # left side motor
AIN2 = 5 # left side motor
PWMA = 11 # left side PWM

# BIN1 = 8 # right side motor
# BIN2 = 10 # right side motor
# PWMB = 12 # right side PWM

HEAD_LIGHTS = 16

GPIO.setwarnings(False)
GPIO.setmode(GPIO.BOARD)
GPIO.setup(HEAD_LIGHTS, GPIO.OUT)
GPIO.output(HEAD_LIGHTS, OFF)

GPIO.setup(AIN1, GPIO.OUT)
GPIO.setup(AIN2, GPIO.OUT)
GPIO.setup(PWMA, GPIO.OUT)
PWM_A = GPIO.PWM(PWMA, 100)
PWM_A.start(0)

# GPIO.setup(BIN1, GPIO.OUT)
# GPIO.setup(BIN2, GPIO.OUT)
# GPIO.setup(PWMB, GPIO.OUT)
# PWM_B = GPIO.PWM(PWMB, 100)
# PWM_B.start(0)

def controlCallBack(xboxControlId, value):
    print("Control ID: " + str(xboxControlId))
    if xboxControlId in list(options):
        options[xboxControlId](value)

def headLights(value):
    global LIGHTS
    if LIGHTS == OFF and value == 1:
        GPIO.output(HEAD_LIGHTS, ON)
        LIGHTS = ON
    elif LIGHTS == ON and value == 1:
        GPIO.output(HEAD_LIGHTS, OFF)
        LIGHTS = OFF

def passGas(value):
    if value == 1:
        g = gas()
        g.randomShat()

def switchCamera(value):
    global CAMERA
    if CAMERA == OFF and value == 1:
        os.system("raspivid -f -t 0 -rot 180 &")
        CAMERA = ON
    elif CAMERA == ON and value == 1:
        os.system("sudo killall -9 raspivid")
        CAMERA = OFF

def cameraNeutralPos(value):
    global CAMERA_Y_ANGLE
    global CAMERA_X_ANGLE
    if value[1] == 1 and CAMERA_Y_ANGLE < 170:
        CAMERA_Y_ANGLE += 5
        SERIAL.write("y:" + str(CAMERA_Y_ANGLE))
        print "Camera angle Y: {}".format(CAMERA_Y_ANGLE)
    elif value[1] == -1 and CAMERA_Y_ANGLE > 10:
        CAMERA_Y_ANGLE -= 5
        SERIAL.write("y:" + str(CAMERA_Y_ANGLE))
        print "Camera angle Y: {}".format(CAMERA_Y_ANGLE)
    elif value[0] == 1 and CAMERA_X_ANGLE < 170:
        CAMERA_X_ANGLE += 5
        SERIAL.write("x:" + str(CAMERA_X_ANGLE))
        print "Camera angle X: {}".format(CAMERA_X_ANGLE)
    elif value[0] == -1 and CAMERA_X_ANGLE > 10:
        CAMERA_X_ANGLE -= 5
        SERIAL.write("x:" + str(CAMERA_X_ANGLE))
        print "Camera angle X: {}".format(CAMERA_X_ANGLE)

def cameraY(value):
    global CAMERA_Y_ANGLE
    global CURRENT_Y_ANGLE
    angle = (value/2 + CAMERA_Y_ANGLE)
    output = str(angle).split(".")[0]
    if angle >= 10 and angle <= 170 and int(output) != CURRENT_Y_ANGLE:
        print("CURRENT_Y_ANGLE: {}\nangle: {}".format(CURRENT_Y_ANGLE, output))
        SERIAL.write("y:" + output)
        CURRENT_Y_ANGLE = int(output)

def cameraX(value):
    global CAMERA_X_ANGLE
    global CURRENT_X_ANGLE
    angle = (value/1.5 + CAMERA_X_ANGLE)
    output = str(angle).split(".")[0]
    if angle >= 0 and angle <= 180 and int(output) != CURRENT_X_ANGLE:
        print("CURRENT_X_ANGLE: {}\nangle: {}".format(CURRENT_X_ANGLE, output))
        SERIAL.write("x:" + output)
        CURRENT_X_ANGLE = int(output)

def leftStickX(value):
    pass

def mototTest(value):
    print value
    if(value >= 15):
        if(value > 100):
            value = 100
        PWM_A.ChangeDutyCycle(value)
        GPIO.output(AIN1, 0)
        GPIO.output(AIN2, 1)
    elif(value < -15):
        if(value < -100):
            value = -100
        PWM_A.ChangeDutyCycle(value * -1)
        GPIO.output(AIN1, 1)
        GPIO.output(AIN2, 0)
    else:
        PWM_A.ChangeDutyCycle(0)

options = {6 : headLights,       # A
           7 : passGas,          # B
           1 : mototTest,        # X
           9 : switchCamera,     # Y
           17: cameraNeutralPos, # D-PAD
           2 : cameraX,          # RIGHT THUMB X
           3 : cameraY,          # RIGHT THUMB Y
           0 : leftStickX}       # LEFT THUMB X

xbox360 = XboxController.XboxController(controlCallBack, deadzone = 0, scale = 100, invertYAxis = True)

try:
    xbox360.start()
    print "User controls running..."
    while True:
        sleep(1)

except KeyboardInterrupt:
    print "User controls stopped."

except:
    print "Unexpected error:", sys.exc_info()[0]
    raise

finally:
    xbox360.stop()
    GPIO.cleanup()
