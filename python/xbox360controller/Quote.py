from random import randint
import os

class quote:

    def specifyQuote(self, num):
        if num >= 0 and num < 16:
            os.system("omxplayer /home/pi/Mr_Roboto/audio/Quote_{}.mp3 &".format(num))

    def randomQuote(self):
        os.system("omxplayer /home/pi/Mr_Roboto/audio/Quote_{}.mp3 &".format(randint(0,0)))
