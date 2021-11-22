from bluetooth import *
import os
import time as t

startbtooth=t.time()
while True:
    try:
        t.sleep(1)
        print("sleeping..")
        if(t.time()>(startbtooth+5)):
            os.system("sudo sdptool add sp")
            os.system("sudo hcitool dev")
            os.system("sudo hciconfig hci0 piscan")
            #try this if there is an issue with the gps
            os.system("sudo chmod o+rw /var/run/sdp")
            os.system("sudo chmod o+rw /home/pi/Desktop/AssistiveRobot/Robot-prototype.py")
            print("DEVICE REGISTERED..")
            
            #os.system("sudo chmod o+rw /var/run/sdp")
            
            break
    except:
        print("Ran into an error!")
        sys.exit(1) 