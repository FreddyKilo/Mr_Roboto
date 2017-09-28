#!/usr/bin/env python

import XboxController
import serial
import math
import os, sys
import RPi.GPIO as GPIO
from time import sleep
from SoundBites import soundBite

OFF = 0
ON = 1
LIGHTS = OFF
CAMERA = OFF
# SERIAL = serial.Serial("/dev/ttyAMA0", 9600)
CAMERA_X_ANGLE = 90
CAMERA_Y_ANGLE = 90
CURRENT_X_ANGLE = CAMERA_X_ANGLE
CURRENT_Y_ANGLE = CAMERA_Y_ANGLE

CURRENT_LEFT_STICK_X = 0
CURRENT_LEFT_STICK_Y = 0

ANALOG_THRESHOLD = 20

GPIO.setwarnings(False)
GPIO.setmode(GPIO.BOARD)

# Set up left side motors
IN1 = 3  # RPi pin numbers
IN2 = 5
GPIO.setup(IN1, GPIO.OUT)
GPIO.setup(IN2, GPIO.OUT)
PWM_IN1 = GPIO.PWM(IN1, 1000)
PWM_IN2 = GPIO.PWM(IN2, 1000)
PWM_IN1.start(0)
PWM_IN2.start(0)

# Set up right side motors
IN3 = 8
IN4 = 10
GPIO.setup(IN3, GPIO.OUT)
GPIO.setup(IN4, GPIO.OUT)
PWM_IN3 = GPIO.PWM(IN3, 1000)
PWM_IN4 = GPIO.PWM(IN4, 1000)
PWM_IN3.start(0)
PWM_IN4.start(0)

BLUE_LED = 7
GPIO.setup(BLUE_LED, GPIO.OUT)

HEAD_LIGHTS = 11
GPIO.setup(HEAD_LIGHTS, GPIO.OUT)
GPIO.output(HEAD_LIGHTS, OFF)

soundBite = soundBite()


def controlCallBack(xboxControlId, value):
    if xboxControlId in list(options):
        options[xboxControlId](value)


def aButton(value):
    if value == 1:
        soundBite.randomBite()


def bButton(value):
    if value == 1:
        soundBite.randomShat()


def xButton(value):
    if value == 1:
        soundBite.playR2d2()


def yButton(value):
    if value == 1:
        soundBite.randomQuote()


def dPad(value):
    if value[0] == 1:
        pass
    elif value[0] == -1:
        pass
    elif value[1] == 1:
        pass
    elif value[1] == -1:
        pass


'''
This functionality has been replaced by a motion tracking Android service. If decide to use,
the serial output would have to talk to a Pololu Micro Maestro
'''
def cameraY(value):
    global CAMERA_Y_ANGLE
    global CURRENT_Y_ANGLE
    angle = (value / 2 + CAMERA_Y_ANGLE)
    output = str(angle).split(".")[0]
    if angle >= 10 and angle <= 170 and int(output) != CURRENT_Y_ANGLE:
        # SERIAL.write("y:" + output)
        CURRENT_Y_ANGLE = int(output)


'''
This functionality has been replaced by a motion tracking Android service. If decide to use,
the serial output would have to talk to a Pololu Micro Maestro
'''
def cameraX(value):
    global CAMERA_X_ANGLE
    global CURRENT_X_ANGLE
    angle = (value / 1.5 + CAMERA_X_ANGLE)
    output = str(angle).split(".")[0]
    if angle >= 0 and angle <= 180 and int(output) != CURRENT_X_ANGLE:
        # SERIAL.write("x:" + output)
        CURRENT_X_ANGLE = int(output)


def leftStickX(value):
    global CURRENT_LEFT_STICK_X
    CURRENT_LEFT_STICK_X = value
    setRightMotor(value, CURRENT_LEFT_STICK_Y)
    setLeftMotor(value, CURRENT_LEFT_STICK_Y)


def leftStickY(value):
    global CURRENT_LEFT_STICK_Y
    CURRENT_LEFT_STICK_Y = value
    setRightMotor(CURRENT_LEFT_STICK_X, value)
    setLeftMotor(CURRENT_LEFT_STICK_X, value)


'''
Left stick button
'''
def lsButton(value):
    if value == 1:
        soundBite.playHorn()


'''
Right stick button
'''
def rsButton(value):
    global LIGHTS
    if LIGHTS == OFF and value == 1:
        GPIO.output(HEAD_LIGHTS, ON)
        LIGHTS = ON
    elif LIGHTS == ON and value == 1:
        GPIO.output(HEAD_LIGHTS, OFF)
        LIGHTS = OFF


def mapValue(value):
    mappedValue = (100.0 / (100 - ANALOG_THRESHOLD)) * (value - ANALOG_THRESHOLD)
    if (mappedValue > 100.0): # Dont return anything over 100, pwm duty cycle no likey
        return 100
    else:
        return int(mappedValue)


def setLeftMotor(x, y):
    # Get the speed and direction of the motor based on analog stick position
    if (y <= 0 and x >= 0) or (y >= 0 and x <= 0):  # upper left quadrant or lower right quadrant
        value = y + x
    elif (y <= 0 and x <= 0) or (y >= 0 and x >= 0):  # upper right quadrant or lower left quadrant
        if y > 0:
            value = math.sqrt(x ** 2 + y ** 2)
        else:
            value = -math.sqrt(x ** 2 + y ** 2)
    else:
        value = 0

    # Now use the calculated value to set the duty cycle
    if (value >= ANALOG_THRESHOLD):
        PWM_IN1.ChangeDutyCycle(mapValue(value))
        PWM_IN2.ChangeDutyCycle(0)
    elif (value <= -ANALOG_THRESHOLD):
        PWM_IN1.ChangeDutyCycle(0)
        PWM_IN2.ChangeDutyCycle(mapValue(-value))
    else:
        PWM_IN1.ChangeDutyCycle(0)
        PWM_IN2.ChangeDutyCycle(0)


def setRightMotor(x, y):
    # Get the speed and direction of the motor based on analog stick position
    if (y <= 0 and x <= 0) or (y >= 0 and x >= 0):  # lower left quadrant or upper right quadrant
        value = y - x
    elif (y <= 0 and x >= 0) or (y >= 0 and x <= 0):  # lower right quadrant or upper left quadrant
        if y > x:
            value = math.sqrt(x ** 2 + y ** 2)
        else:
            value = -math.sqrt(x ** 2 + y ** 2)
    else:
        value = 0

    # Now use the calculated value to set the duty cycle
    if (value >= ANALOG_THRESHOLD):
        PWM_IN3.ChangeDutyCycle(0)
        PWM_IN4.ChangeDutyCycle(mapValue(value))
    elif (value <= -ANALOG_THRESHOLD):
        PWM_IN3.ChangeDutyCycle(mapValue(-value))
        PWM_IN4.ChangeDutyCycle(0)
    else:
        PWM_IN3.ChangeDutyCycle(0)
        PWM_IN4.ChangeDutyCycle(0)


def statusLightSignal():
    GPIO.output(BLUE_LED, ON)
    sleep(.01)
    GPIO.output(BLUE_LED, OFF)
    sleep(.15)


# Functions in this map get called by controlCallBack based on the xboxControlId
options = {
    6: aButton,  # A
    7: bButton,  # B
    8: xButton,  # X
    9: yButton,  # Y
    15: lsButton,  # LEFT STICK BUTTON
    16: rsButton,  # RIGHT STICK BUTTON
    17: dPad,  # D-PAD
    0: leftStickX,  # LEFT THUMB X
    1: leftStickY,  # LEFT THUMB Y
    2: cameraX,  # RIGHT THUMB X
    3: cameraY  # RIGHT THUMB Y
}

xbox360 = XboxController.XboxController(controlCallBack, deadzone=ANALOG_THRESHOLD, scale=100, invertYAxis=True)

try:
    xbox360.start()
    print "User controls running..."
    while True:
        statusLightSignal()
        statusLightSignal()
        sleep(1.5)

except KeyboardInterrupt:
    print "User controls stopped."

except:
    print "Unexpected error:", sys.exc_info()[0]
    raise

finally:
    xbox360.stop()
    GPIO.cleanup()
