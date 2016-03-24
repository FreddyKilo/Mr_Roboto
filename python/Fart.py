from random import randint
import os

class Fart:

    def __init__(self):
        pass#gas

    def randomShat(self):
        specifyStank(randint(0,15))

    def specifyStank(self, num):
        if num >= 0 and num < 16:
            os.system("omxplayer ~/Mr_Roboto/audio/Fart_" + num)
