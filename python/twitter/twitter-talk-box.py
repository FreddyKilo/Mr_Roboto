from twitter import *
from gtts import gTTS
import os

OAUTH = os.path.expanduser('/home/pi/Mr_Roboto/python/twitter/auth/.twitter_oauth')
CONSUMER_KEYS = os.path.expanduser('/home/pi/Mr_Roboto/python/twitter/auth/.twitter_keys')
USERNAME = '@c0nt41n3d'

oauth_token, oauth_secret = read_token_file(OAUTH)
con_key, con_secret = read_token_file(CONSUMER_KEYS)
auth = OAuth(consumer_key=con_key, consumer_secret=con_secret, token=oauth_token, token_secret=oauth_secret)
stream = TwitterStream(auth=auth, domain='userstream.twitter.com')

def main():
    for tweet in stream.user():
        text = tweet.get('text')
        print(text)
        if text is not None:
            if USERNAME in text:
                text = text[len(USERNAME) + 1:]  # This is the actual message sent to Mr_Roboto
            user = tweet.get('user').get('name') # This is the name of the user that is sending the message
            # print(text)
            speak(text)

def speak(string):
    fileName = "audio.mp3"
    tts = gTTS(text=string, lang="en")
    tts.save("/home/pi/Mr_Roboto/python/twitter/" + fileName)
    os.system("omxplayer {}{}".format("/home/pi/Mr_Roboto/python/twitter/", fileName))

main()
