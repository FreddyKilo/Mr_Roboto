from gtts import gTTS
import os

class Voice:

    global AUDIO_DIRECTORY
    global isFileSaved

    def __init__(self):
        AUDIO_DIRECTORY = "/home/pi/Mr_Roboto/python/speech/audio/"

    # def speak(self, string):
    #     fileName = "yourwelcome.mp3"
    #     if isFileSaved(fileName) == False:
    #         tts = gTTS(text=string+".", lang="en")
    #         tts.save("/home/pi/Mr_Roboto/python/speech/audio/" + fileName)
    #         print("\n===================\nSaved " + fileName + "\n===================\n")
    #         print("omxplayer {}{}".format("/home/pi/Mr_Roboto/python/speech/audio/", fileName))
    #     os.system("omxplayer {}{}".format("/home/pi/Mr_Roboto/python/speech/audio/", fileName))

    def speak(self, string):
        fileName = "test.mp3"
        tts = gTTS(text=string, lang="en")
        tts.save("/home/pi/Mr_Roboto/python/speech/audio/" + fileName)
        os.system("omxplayer {}{}".format("/home/pi/Mr_Roboto/python/speech/audio/", fileName))

    def isFileSaved(fileName):
        for file in os.listdir("/home/pi/Mr_Roboto/python/speech/audio/"):
            if file == fileName:
                return True
        return False
