from Ears import Ears
from Voice import Voice

def main():
    ears = Ears()
    voice = Voice()
    try:
        while True:
            text = ears.listen()
            print("\n" + text)
            voice.speak(text)

    except KeyboardInterrupt:
        print("Communication stopped.")

main()