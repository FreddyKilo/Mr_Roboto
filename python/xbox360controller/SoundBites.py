from random import randint
import os

class soundBite:

    global getUpperBoundFromFiles
    global killOmx
    global play

    def __init__(self):
        killOmx(5)

    def randomShat(self):
        play("Fart_")

    def randomQuote(self):
        play("Quote_")

    def playHorn(self):
        os.system("omxplayer /home/pi/Mr_Roboto/audio/Horn.mp3 &")

    def playR2d2(self):
        play("R2D2_")

    def randomBite(self):
        play("Sound_")

    def play(filePrefix):
        os.system("omxplayer /home/pi/Mr_Roboto/audio/" + filePrefix + "{}.mp3 &".format(randint(0, getUpperBoundFromFiles(filePrefix))))

    def specifyQuote(self, num):
        if num >= 0 and num < 16:
            os.system("omxplayer /home/pi/Mr_Roboto/audio/" + "Quote_{}.mp3 &".format(num))

    '''
    Count the number of files with filePrefix and return the count minus one.
    This int gets passed to randint() for the upper bound index
    '''
    def getUpperBoundFromFiles(filePrefix):
        count = -1 # Adjust file count to index number
        for file in os.listdir("/home/pi/Mr_Roboto/audio/"):
            if file.startswith(filePrefix):
                count+=1
        return count

    '''
    Kill all omxplayer processes if there are more than maxProcesses running
    '''
    def killOmx(maxProcesses):
        pidCount = 0
        pids = [pid for pid in os.listdir('/proc') if pid.isdigit()]
        for pid in pids:
            try:
                pidname = open(os.path.join('/proc', pid, 'cmdline'), 'rb').read()
                if "omxplayer" in pidname:
                    print pidname
                    pidCount+=1
            except IOError:
                continue

        if pidCount > maxProcesses:
            os.system("sudo killall -9 omxplayer.bin")
