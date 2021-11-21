#coding: utf-8
import socket
#import bluetooth socket
import threading
import subprocess
#import bluetooth
from bluetooth import *
import time as t
import os
import GPSService2
import re
import concurrent.futures
from concurrent.futures import ThreadPoolExecutor, wait, ALL_COMPLETED,ProcessPoolExecutor
import sys
import traceback
import arduino_m


from queue import Queue
#Queues to hold data that will be transferred to and fro the various devices
#Pi_to_Android 
pi_to_and= Queue(maxsize = 50)
#Pi to Arduino
pi_to_ard = Queue(maxsize = 50)

and_to_pi = Queue(maxsize = 50)
mode = "inactive"
myGps = None
myArduino = None
switch = False
androidLat = 0.0
androidLon = 0.0
ard_com =None
client_sock,client_info = 1,1
startbtooth = t.time()
while True:
    try:
        t.sleep(1)
        print("sleeping..")
        if(t.time()>(startbtooth+7)):
            #os.system("sudo sdptool add sp")
            #os.system("sudo hcitool dev")
            #os.system("sudo hciconfig hci0 piscan")
            #try this if there is an issue with the gps
            print("DEVICE REGISTERED..")
            
            #os.system("sudo chmod o+rw /var/run/sdp")
            
            break
    except:
        print("Ran into an error!")
        sys.exit(1)   
#try this if there is an issue with the gps
subprocess.run(["sudo", "sdptool", "add", "sp"])
subprocess.run(["sudo", "hcitool", "dev"])
subprocess.run(["sudo", "hciconfig", "hci0", "piscan"])
subprocess.run(["sudo", "chmod", "o+rw", "/var/run/sdp"])
# os.system("sudo chmod o+rw /var/run/sdp")
# os.system("sudo sdptool add sp")
# os.system("sudo hcitool dev")
# os.system("sudo hciconfig hci0 piscan")
# os.system("sudo chmod o+rw /var/run/sdp")
# os.close()
#server_sock = bluetooth.BluetoothSocket(bluetooth.RFCOMM)
server_sock = BluetoothSocket(RFCOMM)
server_sock.bind(("",PORT_ANY))
server_sock.listen(1)
port = server_sock.getsockname()[1]
uuid = "94f39d29-7d6d-437d-973b-fba39e49d4ee"
DISCONNECT_MESSAGE = "DISCONNECT"
FORMAT = "utf-8"


#The format of the string message to be sent from Android to Pi will be mode,Lat,Long,angle
#The format to be sent from the pi to android mode, Pi Lat, Pi Lon, distance, Pi angle
#The format for the arduino tbd 


print("Initiating Bluetooth Socket")
starttime=t.time()
while True:
    try:
        advertise_service(server_sock, "AssistiveRobotServer", service_id=uuid,
                            service_classes=[uuid,SERIAL_PORT_CLASS],
                            profiles=[SERIAL_PORT_PROFILE],
                            # protocols=[bluetooth.OBEX_UUID]
                            )
        break
    except:
        t.sleep(1)
        print("Waiting on bluetooth services...")
    if(t.time()>(starttime+30)):
        print("unable to catch bluetooth services")
        sys.exit(1)
        


def write_client(client_sock,client_info,msg):
    msg = msg.encode(FORMAT)
    client_sock.send(msg)
    print(f"Pi sent: {msg}")
    
                
########## CONTINUE HERE
def read_client(client_sock,client_info):
    try:
        while True:
            print("read_client")#needs to constantly 
            data = client_sock.recv(1024).decode(FORMAT)
            print(str(data))
    except:
         print("Error Reading Code in Read method")


                #return data    
def isValidDevice(client_sock,client_info,uuid):
    
    try:
        write_client(client_sock,client_info,uuid)
       
        
        connected = True
        while connected:
            
            data = client_sock.recv(1024).decode(FORMAT)
            if data:
                if data == "True":
                    print(f"[{client_info}] {data}")
                    message = data
                    msg = message.encode(FORMAT)
                    client_sock.send(msg)
                    return True
                
                else:
                    message = "False"
                    msg = message.encode(FORMAT)
                    write_client(client_sock,client_info,msg)
                    
                    return False
    except OSError:
        print("Oops we ran into an unknown error!")



def send_pi_and(client_sock,client_info):
    #Method to send the calculations to the android
    #Android should display location on maps, 
    #Android should also tell if robot is wandering away
    print("Entered into send_pi_and\n") 
    while True:
        if switch == True:
            break
        if pi_to_and.empty():
            #print("Nothing to write to android yet")
            #t.sleep(2)
            pass
            
            
        else:
            try:
                msg = str(pi_to_and.get())
                
                write_client(client_sock,client_info,msg)
                print("Writing to android: "+ msg)
            except:
                print("There was an error in send_pi_and method")
                pass

def send_pi_ard():
    global  client_sock,client_info,mode,myArduino,pi_to_ard
    #Method to send the calculations to the android
    #Android should display location on maps, 
    #Android should also tell if robot is wandering away
    pi_ard_data =None
    print("Entered into send_pi_ard\n")
    try:
        while True:
            if switch == True:
                break
            if pi_to_ard.empty():
#                 print("IF PI TO ARD EMPTY")
#                 myArduino.hey_world("stop")
#                 myArduino.arduino_move("stop")
                  pass

                
                
                
                
                
            else:
                print("PI TO ARD NOT EMPTY")
                pi_ard_data = pi_to_ard.get()
                if mode=="controlMe":
                    print("IN THE CONTRO ME IOF")
                    myArduino.arduino_move(pi_ard_data)
#                     myArduino.hey_world("stop")
                    print("WROTE A COMMAND TO ARDUINO")
                    
                    print("Writing to Arduino: "+ pi_ard_data)
                elif mode=="demoMe":
                    print("congratulations we are in the demos")
                    if(pi_ard_data=="demo1"):
                        print("pi_ard_data: "+ pi_ard_data)
                        send_back = myArduino.do_demo1()
                        print("we finished a demo!")
                        pi_to_and.put(send_back)
                    if(pi_ard_data=="demo2"):
                        print("we're starting demo 2")
                        send_back = myArduino.do_demo2()
                        print("we finished a demo2!")
                        pi_to_and.put(send_back)
                    
                    
#                 t.sleep(2)
    except:
        print("Error in send_pi_ard func")
        pass
    

def read_and_pi(client_sock,client_info):
    try:
        print("Entered into read and pi")     
        while True:
            if switch == True:
                print("in the read_and_pi method before xiiting because switch is true?!")
                break
            
            data = client_sock.recv(1024).decode(FORMAT)
            #print("From read_and_pi: "+str(data))
            
            #if data != "" or data != None:
            if not(and_to_pi.full()):
                and_to_pi.put(data)
                #print(f"Data from and-to-pi: {data}")
                t.sleep(1)
            
    except IOException:
        print("Error with the socket")


    except:
        print("Error in read_and_pi please check bluetooth connection")



def check_mode_delegate():
    global mode,androidLat,androidLon,switch,ard_com
    
    while True:
        #print(mode)
        #print(switch)
        
        t.sleep(0.5)
        
        if switch == True:
            print("MODE CHANGE")
            break
        if not(and_to_pi.empty()):
            print("and to pi is not empty!")
            android_data = and_to_pi.get()
            print("The Android data: "+android_data)
            android_data = re.split(',|\n',android_data)
#             print(android_data[0])
            
            if not(android_data[0]== mode):
#                 print(type(android_data[0]))
#                  print(type(mode))      
                mode = str(android_data[0])
                
                switch =True
                #print(mode)
                androidLat = float(android_data[1])
                androidLon = float(android_data[2])
                print("Switch is be true")
                #if(len(android_data)>=5):
                        #ard_com = android_data[5]
                        #pi_to_ard.put(ard_com)
            else:
                try:

                    androidLat = float(android_data[1])
                    androidLon = float(android_data[2])
                    if mode=="controlMe":
                        print("THE LENGTH OF DATA:"+ str(android_data[4]))
                        pi_to_ard.put(android_data[4])
                    if mode=="demoMe":
                        print("THE LENGTH OF DATA:"+ str(android_data[4]))
                        pi_to_ard.put(android_data[4])
                        

                   
                    #NOT PERMANENT
                    #if(len(android_data)>=5):
                        #ard_com = android_data[5]
                        #pi_to_ard.put(ard_com)
                        
                        
#                     print("The mode is " + str(mode)+ ",androidLat:  "+  str(androidLat) +", androidLon: " + str(androidLon))
                except:
                    print("Possible invalid Code Being read from android:")
            

                #switch = True
                
        else:
            print("and to pi Is empty!")
            
                        
                    

def calculate_parameters():
                        
    #NEED TO ACCOUNT FOR IF GPS DATA NOT AVAILABLE
    global mode,androidLat,androidLon,myGps
    print("Entered into calculate_parameters: ")
    while True:
        if switch == True:
            break
        print("Calculating Distance..")
        report = myGps.gpsd.next()
        #print(report)
        if  report['class']=='TPV':
            print("Report was found!!")
            piLat = myGps.getLat()
            piLon = myGps.getLon()
            piSpeed = myGps.getSpeed()
            [Distance,Bearing,pythag,d_f] = myGps.calculateDistance(androidLat,androidLon)
            #pythag_val =myGps.pythag_distance(androidLat,androidLon)
            print("Distance: "+str(Distance))
            print("Bearing:" +str(Bearing))
            print("Pythag Distance: "+str(pythag))
            print("Plan B Distance: "+ str(d_f))
            print("Out of this method")
            
            #Dont forget to add the angle as a parameter
            #calculated_params = [str(mode),str(piLat),str(piLon),str(Distance),str(piSpeed)]
            #calculated_params = str(mode)+","+str(piLat)+","+str(piLon)+","+str(Distance)+","+ str(Bearing)+","+str(piSpeed) +","+str(androidLat)+","+str(androidLon)
                      
            #print(calculate_params)

            #WE can actually write to GPS now
            #pi_to_and.put(calculate_params)
            #pi_to_ard.put(calculate_params)
            
                      

        else:
            calculated_params = "Calculating Distances.."
            print("Calculating Distances while waiting for gpsd")
            #WE can actually write to GPS now
            #pi_to_and.put(calculate_params)
            #pi_to_ard.put(calculate_params)
            #t.sleep(1)

def div(x):
    print(x/10) 
    #controller function will observe the mode of robot
def controller(client_sock,client_info):
    global switch
    
    while True:
        
        print("controller: "+ str(mode))
        switch = False
        print("Switch in Controller: "+str(switch))
                        
        
        try:


            if mode=="followMe":
                    #Because we use with the threads will automatically wait 
                    #for all of them to shut down to continue
                with ThreadPoolExecutor(max_workers=5) as e:
                #with ProcessPoolExecutor(max_workers=5) as e:
                    e.submit(calculate_parameters)
                    e.submit(send_pi_and,client_sock,client_info)
                    e.submit(send_pi_ard)
                    e.submit(read_and_pi,client_sock,client_info)
                    e.submit(check_mode_delegate)
                    
                #Switch Variable is back false, meaning no more ending  
                switch=False

            if mode=="controlMe":
                #No difference yet from follow me
                #with ProcessPoolExecutor(max_workers=5) as e:
                with ThreadPoolExecutor(max_workers=5) as e:
#                     e.submit(calculate_parameters)
                    e.submit(send_pi_and,client_sock,client_info)
                    e.submit(send_pi_ard)
                    e.submit(read_and_pi,client_sock,client_info)
                    e.submit(check_mode_delegate)
                    switch = False
                    
            if mode=="demoMe":
                #No difference yet from follow me
                #with ProcessPoolExecutor(max_workers=5) as e:
                with ThreadPoolExecutor(max_workers=5) as e:
#                     e.submit(calculate_parameters)
                    e.submit(send_pi_and,client_sock,client_info)
                    e.submit(send_pi_ard)
                    e.submit(read_and_pi,client_sock,client_info)
                    e.submit(check_mode_delegate)
                    switch = False

            if mode=="inactive":
                with ThreadPoolExecutor(max_workers=5) as e:
                #with ProcessPoolExecutor(max_workers=5) as e:
                    e.submit(check_mode_delegate)
                    e.submit(read_and_pi,client_sock,client_info)
                    switch = False
                    
                
            if mode =="turnOff":
                print("Robot is turning off")
                break

        except:
            print("possible error! Please Review Code!")
            break
            




            



def main():
    while True:
        print("Waiting for connection on RFCOMM channel", port)
        global Client_sock,client_info,myGps,myArduino
        client_sock,client_info = server_sock.accept()
        print("Accepted connection from", client_info)
        if isValidDevice(client_sock,client_info,uuid):
        #if 1==1:
            try:
                myGps = GPSService2.GPSServer()
                print("part 1")
                myGps.OSKillandStart()
                print("part 2")
                myArduino = arduino_m.arduino_interface()
                myArduino.connect_arduino()
                print("Arduino successfully created")
                #print(myGps.gpsd.next())
                t.sleep(2)
                with ThreadPoolExecutor(max_workers=5) as ex:
                    ex.submit(controller,client_sock,client_info)
                    #ex.submit(div(),5)
                    #ex.submit(read_client,client_sock,client_info)

                        
                
            except Exception:
                raise sys.exc_info()[0](traceback.format_exc())
            except:
                print("Error initiating GPS please make sure GPS is properly connected")
                client_sock.close()
                break

        #will continue code
        else:
                print("Unsupported Device Please use a compatible device!")
                client_sock.close()

        #code for isnt
    print("Code completed")
        
     
main()




                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              