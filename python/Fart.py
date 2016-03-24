from random import randint
import os

class Fart:

    def specifyStank(self, num):
        if num >= 0 and num < 16:
            os.system("afplay ~/Mr_Roboto/audio/Fart_{}.mp3".format(num))

    def randomShat(self):
        os.system("afplay ~/Mr_Roboto/audio/Fart_{}.mp3".format(randint(0,15)))
