from random import randint
import os

class gas:

    def specifyStank(self, num):
        if num >= 0 and num < 16:
            os.system("omxplayer /home/pi/Mr_Roboto/audio/Fart_{}.mp3 &".format(num))

    def randomShat(self):
        os.system("omxplayer /home/pi/Mr_Roboto/audio/Fart_{}.mp3 &".format(randint(0,15)))
