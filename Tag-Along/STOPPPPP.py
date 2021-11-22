
import serial
import time

if __name__=='__main__':
    ser = serial.Serial('/dev/ttyACM0',9600, timeout = 1)
    ser.flush()
    
    while True:
        ser.write(b"LR_STOP\n")
        time.sleep(5)