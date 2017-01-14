import speech_recognition as sr
import os

class Ears:

    def __init__(self):
        pass

    """
    Listen to an incoming audio source from a usb Microphone, recognize
    the audio as speech, then return the words as text
    """
    def listen(self):
        r = sr.Recognizer() #TODO: this needs to be globalized
        print("\nI'm listening...\n")
        with sr.Microphone(sample_rate = 44100) as source:
            audio = r.listen(source)
        try:
            return r.recognize_google(audio)
        except sr.UnknownValueError:
            # Return a flag indicating that the speech could not be recognized
            return "Could not understand"