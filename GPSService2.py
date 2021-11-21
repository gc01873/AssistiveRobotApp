from gps import *
import time as t
import os
from math import radians,cos,sin,asin,sqrt
import numpy as np

class GPSServer:
    
    
    
    def _init_(self):
            self.gpsd = 1
            
            
    def OSKillandStart(self):
         os.system("sudo systemctl stop gpsd.socket")
         os.system("sudo systemctl disable gpsd.socket")
         os.system("sudo killall gpsd")
         os.system("sudo gpsd /dev/ttyUSB0 -F/var/run/gpsd.sock")
         self.gpsd = gps("localhost", "2947")
         self.gpsd.stream(WATCH_ENABLE | WATCH_NEWSTYLE)
         print("Hello!!!")

            
      
        
    
    def getLat(self):
        report = self.gpsd.next()
        if report['class'] == 'TPV':
            print("latitude: "+str( getattr(report,'lat',0.0))+"\t") 
            return getattr(report,'lat',0.0)
    
    
    def getLon(self):
        report = self.gpsd.next()
        if report['class'] == 'TPV':
            print("longitude: "+str(getattr(report,'lon',0.0))+"\t") 
            return getattr(report,'lon',0.0)
        
    def getTime(self):
        report = self.gpsd.next()
        if report['class'] == 'TPV':
            print("time: "+str(getattr(report,'time',''))+"\t")
            return getattr(report,'time','')
        
    def getSpeed(self):
        report = self.gpsd.next()
        if report['class'] == 'TPV':
            print("speed: "+str(getattr(report,'speed',''))+"\t")
            return getattr(report,'speed','')
    def pythag_distance(self,androidLat,androidLon):
        while True:
            report = self.gpsd.next()
            if report['class'] != 'TPV':
                print("Calculating..")
                
               # return "Calculating Distance."
            if report['class'] == 'TPV':
                #radians will convert from radians to degrees
                
                piLon = radians(getattr(report,'lon',0.0))
                
                piLat = radians(getattr(report,'lat',0.0))
                lon2 = math.radians(androidLon)
                lat2 = math.radians(androidLat)
                
                r=3956.0
                
                x1 = r*math.sin(piLat)*math.cos(piLon)
                y1 = r*math.sin(piLat)*math.sin(piLon)
                z1 = r*math.cos(piLat)
                
                x2 = r*math.sin(lat2)*math.cos(lon2)
                y2=r*math.sin(lat2)*math.sin(lon2)
                z1 = r*math.cos(lat2)
                
                ans=math.sqrt(((x1-x2)**2)+((y1-y2)**2)+((z1-z2)**2))
            return ans
    
    def get_Bearing(self,androidLat,androidLon,piLat,piLon):     
        dLon = (piLon-androidLon)
        x= math.cos(math.radians(piLat)) * math.sin(math.radians(dLon))
        y= math.cos(math.radians(androidLat))* math.sin(math.radians(piLat)) -math.sin(math.radians(androidLat))*math.cos(math.radians(piLat))*math.cos(math.radians(dLon))
        brng = np.arctan2(x,y)
        brng = np.degrees(brng)
        return brng    
       #This will end up being a sting
    
    def get_bearing_tuple(self,pointA,pointB):
        if(type(pointA)!= tuple or type(pointB)!=tuple):
            raise TypeError("Put it in a tuple. only tuples are supported")
        lat1=math.radians(pointA[0])
        lat2=math.radians(pointB[0])
        
        diffLong = math.radians(pointB[1]-pointA[1])
        
        x = math.sin(diffLong)*math.cos(lat2)
        
        y=math.cos(lat1)*math.sin(lat2)-(math.sin(lat1)*math.cos(lat2)*math.cos(diffLong))
        
        initial_bearing =math.atan2(x,y)
        
        initial_bearing = math.degrees(initial_bearing)
        
        compass_bearing = (initial_bearing+360)%360
        
        return compass_bearing
    def calculateDistance(self,androidLat,androidLon):
        while True:
            report = self.gpsd.next()
            if report['class'] != 'TPV':
                print("Calculating Distance.. IN CALCULATE DISTANCE")
                
               # return "Calculating Distance."
            if report['class'] == 'TPV':
                #radians will convert from radians to degrees
                
                piLon = radians(getattr(report,'lon',0.0))
                
                piLat = radians(getattr(report,'lat',0.0))
                lon2 = math.radians(androidLon)
                lat2 = math.radians(androidLat)
                
                dlon = lon2 - piLon
                dlat = lat2 - piLat
                
                a = math.sin(dlat/2)**2 +math.cos(piLat)*math.cos(lat2)*math.sin(dlon/2)**2
                c=2*math.asin(math.sqrt(a))
#                 print("In the method where the magic happens")
                #radius of earth in miles 3956 use 6371 for km
                r=3956.0
#                 print(r)
                
                
                bearing = self.get_Bearing(androidLat,androidLon,piLat,piLon)
                
                bearing1 = self.get_bearing_tuple((androidLat,androidLon),(getattr(report,'lat',0.0),getattr(report,'lon',0.0)))
                
                
                x1 = r*math.sin(piLat)*math.cos(piLon)
                
                y1 = r*math.sin(piLat)*math.sin(piLon)
                
                z1 = r*math.cos(piLat)
                
                
                x2 = r*math.sin(lat2)*math.cos(lon2)
                y2=r*math.sin(lat2)*math.sin(lon2)
                z2 = r*math.cos(lat2)
                
                pythag=math.sqrt(((x1-x2)**2)+((y1-y2)**2)+((z1-z2)**2))
               
                
                d_ew = (piLon - lon2)*math.cos(lat2) #(math.sum(lat2,piLat)/2.0)
                
                d_ns=(piLat - lat2)
                
                d = math.sqrt((d_ew*d_ew)+((d_ns*d_ns)))
                
                     
                d_f = d*r
                
                dist = c*r
                
                return[dist,bearing1,pythag,d_f]
                
                #HaversineFormula
    
                
                
   
        
                
             
                
            
        
    
            
        
    
