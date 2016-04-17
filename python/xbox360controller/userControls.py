#!/usr/bin/env python

import XboxController
import serial
import os, sys
import RPi.GPIO as GPIO
from time import sleep
from Fart import gas
from Quote import quote

OFF = 0
ON = 1
LIGHTS = OFF
CAMERA = OFF
SERIAL = serial.Serial("/dev/ttyAMA0", 9600)
CAMERA_X_ANGLE = 90
CAMERA_Y_ANGLE = 90
CURRENT_X_ANGLE = CAMERA_X_ANGLE
CURRENT_Y_ANGLE = CAMERA_Y_ANGLE

CURRENT_LEFT_STICK_X = 0
CURRENT_LEFT_STICK_Y = 0

ANALOG_THRESHOLD = 20

"""this sets up the GPIO pins (GPIO.BOARD) to be used
with a TB6612FNG motor controller as labeled"""
AIN1 = 3 # left side motor
AIN2 = 5 # left side motor
PWMA = 7 # left side PWM

BIN1 = 8 # right side motor
BIN2 = 10 # right side motor
PWMB = 12 # right side PWM

HEAD_LIGHTS = 11

GPIO.setwarnings(False)
GPIO.setmode(GPIO.BOARD)
GPIO.setup(HEAD_LIGHTS, GPIO.OUT)
GPIO.output(HEAD_LIGHTS, OFF)

GPIO.setup(AIN1, GPIO.OUT)
GPIO.setup(AIN2, GPIO.OUT)
GPIO.setup(PWMA, GPIO.OUT)
PWM_A = GPIO.PWM(PWMA, 100)
PWM_A.start(0)

GPIO.setup(BIN1, GPIO.OUT)
GPIO.setup(BIN2, GPIO.OUT)
GPIO.setup(PWMB, GPIO.OUT)
PWM_B = GPIO.PWM(PWMB, 100)
PWM_B.start(0)

def controlCallBack(xboxControlId, value):
    # print("Control ID: " + str(xboxControlId))
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
        fart = gas()
        fart.randomShat()

def xButton(value):
    pass

def yButton(value):
    pass

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
    global CURRENT_LEFT_STICK_X
    CURRENT_LEFT_STICK_X = value
    setLeftMotor(value, CURRENT_LEFT_STICK_Y)
    setRightMotor(value, CURRENT_LEFT_STICK_Y)

def leftStickY(value):
    global CURRENT_LEFT_STICK_Y
    CURRENT_LEFT_STICK_Y = value
    setLeftMotor(CURRENT_LEFT_STICK_X, value)
    setRightMotor(CURRENT_LEFT_STICK_X, value)

def setLeftMotor(x, y):
    # Get the speed and direction of the motor based on analog stick position
    if (y <= 0 and x <= 0) or (y >= 0 and x >= 0): # upper left quadrant or lower right quadrant
        value = y - x
    elif (y <= 0 and x >= 0) or (y >= 0 and x <= 0): # upper right quadrant or lower left quadrant
        if abs(y) > abs(x):
            value = y
        else:
            value = -x
    else:
        value = 0

    # Now use the calculated value to set the duty cycle
    if(value >= ANALOG_THRESHOLD):
        if(value > 100): # Sometimes the 360 controller will send a value a fraction over 100, we dont want that
            value = 100
        PWM_A.ChangeDutyCycle(value)
        GPIO.output(AIN1, 0)
        GPIO.output(AIN2, 1)
    elif(value <= -ANALOG_THRESHOLD):
        if(value < -100):
            value = -100
        PWM_A.ChangeDutyCycle(value * -1)
        GPIO.output(AIN1, 1)
        GPIO.output(AIN2, 0)
    else:
        PWM_A.ChangeDutyCycle(0)

def setRightMotor(x, y):
    # Get the speed and direction of the motor based on analog stick position
    if (y <= 0 and x >= 0) or (y >= 0 and x <= 0): # upper right quadrant or lower left quadrant
        value = y + x
    elif (y <= 0 and x <= 0) or (y >= 0 and x >= 0): # upper left quadrant or lower right quadrant
        if abs(y) > abs(x):
            value = y
        else:
            value = x
    else:
        value = 0

    # Now use the calculated value to set the duty cycle
    if(value >= ANALOG_THRESHOLD):
        if(value > 100):
            value = 100
        PWM_B.ChangeDutyCycle(value)
        GPIO.output(BIN1, 0)
        GPIO.output(BIN2, 1)
    elif(value <= -ANALOG_THRESHOLD):
        if(value < -100):
            value = -100
        PWM_B.ChangeDutyCycle(value * -1)
        GPIO.output(BIN1, 1)
        GPIO.output(BIN2, 0)
    else:
        PWM_B.ChangeDutyCycle(0)

options = {
           6 : headLights,       # A
           7 : passGas,          # B
           8 : xButton,
           9 : yButton,          # Y
           17: cameraNeutralPos, # D-PAD
           0 : leftStickX,       # LEFT THUMB X
           1 : leftStickY,       # LEFT THUMB Y
           2 : cameraX,          # RIGHT THUMB X
           3 : cameraY           # RIGHT THUMB Y
           }

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
