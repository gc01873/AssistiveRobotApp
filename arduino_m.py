import sense_hat
import serial
import time

from sense_hat import *
from time import sleep

try:
    sense = SenseHat()
    sense.clear
except:
    print("No senseHat detected")
    pass
    


# FORWARD = int(0)
# REVERSE = int(0)
# LEFT    = int(0)
# RIGHT   = int(0)
# ser = serial.Serial('/dev/ttyACM0',9600, timeout = 1)
#     ser.flush()


#ARDUINO COMMANDS

# ser.write(b"FORWARD_25\n")
# ser.write(b"REVERSE_25\n")
# ser.write(b"SPIN_RIGHT_25\n")
# ser.write(b"SPIN_LEFT_25\n")
# ser.write(b"LR_STOP\n")

class arduino_interface:
    def _init_(self):
        self.FORWARD = int(0)
        self.REVERSE = int(0)
        self.LEFT    = int(0)
        self.RIGHT   = int(0)
        self.ser = None
        self.DEMO1 = int(0)
        self.DEMO2 = int(1)
        self.TEST1 = int(0)
        self.TEST2 = int(0)

        self.Current_direction = int(sense.get_compass())
#this is the direction the robot is currently looking
        self.Needed_direction = int(90)
#This is the direction the robot needs to face inorder to go towards the user in a straight line
        self.right = self.Needed_direction - self.Current_direction
#this is how the robot calculates if turning left or right is more efficient 
        self.left = 360-self.Needed_direction + self.Current_direction
        self.RANGEPOSITIVE= self.Needed_direction + 30
        self.RANGENEGATIVE = self.Needed_direction - 30
        
    def connect_arduino(self):
        self.ser = serial.Serial('/dev/ttyACM0',9600, timeout = 1)
        self.ser.flush()
        print("Arduino Connected to USB Port and ser.flush() activated")
    def hey_world(self,var):
        print("HELLO WORLD!!"+ str(var))
        
    def arduino_move(self,var):
        if var=="up":
            print("Moving Forward")
            self.ser.write(b"FORWARD_25\n")
#            time.sleep(1)
        elif var =="down":
            self.ser.write(b"REVERSE_25\n")
            print("Moving Backward")
#            time.sleep(1)
        elif var == "left":
            self.ser.write(b"SPIN_LEFT_25\n")
            print("Spinning Left")
#            time.sleep(1)
        elif var=="right":
            self.ser.write(b"SPIN_RIGHT_25\n")
            print("Spinning Right")
#             time.sleep(1)
            
        elif var=="stop":
            print("IN THE STOP")
            self.ser.write(b"LR_STOP\n")
            time.sleep(1.5)
        
        else:
            self.ser.write(b"LR_STOP\n")
            
    def do_demo1(self):
        print("do_DEMOOOOOOOOO")
#         if (DEMO1 == 1):

        for i in range(0,3):
            self.ser.write(b"SPIN_RIGHT_35\n")
            time.sleep(1)
        self.ser.write(b"LR_STOP\n")
        time.sleep(1)
        for i in range(0,3):
            self.ser.write(b"SPIN_LEFT_35\n")
            time.sleep(1)
        self.ser.write(b"LR_STOP\n")
        time.sleep(1)
        for i in range(0,2):
            self.ser.write(b"FORWARD_25\n")
            time.sleep(1)
        self.ser.write(b"LR_STOP\n")
        time.sleep(1)
        for i in range(0,2):
            self.ser.write(b"REVERSE_25\n")
            time.sleep(1)
        self.ser.write(b"LR_STOP\n")
        time.sleep(1)
        
        return "done1"
        
        
    def do_demo2(self):
        print("In demo2 func")
        while True:
            #ser.write(b"FORWARD_25\n")
            #time.sleep(1)
            #ser.write(b"REVERSE_25\n")
            #time.sleep(1)
            #ser.write(b"LR_STOP\n")
            #time.sleep(1)
            self.Current_direction=int(sense.get_compass())
            print(self.self.Current_direction)
            if self.Current_direction > self.Needed_direction:
                
                self.left = self.Needed_direction - self.Current_direction
                self.right =360-self.Needed_direction + self.Current_direction
                #print("HIGH")
                #print(self.Current_direction)
    
            else:
                self.right = self.Needed_direction - self.Current_direction

                self.left = 360-self.Needed_direction + self.Current_direction
                #print("LOW")
                #print(self.Current_direction)
            if (self.RANGEPOSITIVE >= 360):
                self.RANGEPOSITIVE = self.RANGEPOSITIVE - 360
            if (self.RANGENEGATIVE <0):
                self.RANGENEGATIVE= self.RANGENEGATIVE + 360
            if (self.Current_direction < self.RANGEPOSITIVE and self.Current_direction >self.RANGENEGATIVE):
                self.TEST2=0
                print(self.Current_direction)
                print("stop stop")

                if (self.TEST1 == 0):
                    self.ser.write(b"LR_STOP\n")
                    self.TEST1 =1
                return "done2"
            elif not(self.Current_direction < self.RANGEPOSITIVE and self.Current_direction > self.RANGENEGATIVE):
                
                self.TEST1 =0
                if (self.TEST2 == 0):
                    print("bitch")
                    if self.right <= self.left:
                    
                    
                        print("go right")    
                        self.ser.write(b"SPIN_RIGHT_25\n")
                    #self.TEST2=1
                         
                    #ser.write(b"LR_STOP\n")
                    self.Current_direction=int(sense.get_compass())
              
                    if self.left < self.right:
                        
                    
                        print ("go left")
                        self.ser.write(b"SPIN_LEFT_25\n")
                    #self.TEST2=1
                  
                     #ser.write(b"LR_STOP\n")
                    self.Current_direction=int(sense.get_compass()) 
                
                    
             
              
            self.Current_direction=int(sense.get_compass())
            print (self.Current_direction)
        
            
    
       
            
            
            
        
            
        
        
            
            
            
            
            
        
        
        
        
    
    
        
    