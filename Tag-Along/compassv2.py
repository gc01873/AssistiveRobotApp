#2 main variables Distance and Cardinal Direction

# L_STOP
# L_ACC_FOR
# L_DEC_FOR
# L_ACC_REV
# L_DEC_REV

# R_STOP
# R_ACC_FOR
# R_DEC_FOR
# R_ACC_REV
# R_DEC_REV

# LR_STOP
# LR_ACC_FOR
# LR_DEC_FOR
# LR_ACC_REV
# LR_DEC_REV

# L_ACC_FOR_R_ACC_REV
# L_ACC_FOR_R_DEC_FOR
# L_ACC_FOR_R_DEC_REV

# L_ACC_REV_R_ACC_FOR
# L_ACC_REV_R_DEC_FOR
# L_ACC_REV_R_DEC_REV

# L_DEC_FOR_R_ACC_FOR
# L_DEC_FOR_R_ACC_REV
# L_DEC_FOR_R_DEC_REV

# L_DEC_REV_R_ACC_FOR
# L_DEC_REV_R_ACC_REV
# L_DEC_REV_R_DEC_FOR


import sense_hat
import serial
import time

from sense_hat import *
from time import sleep

phone = int(90)
#phone is the angle to robot needs to be facing

sense = SenseHat()
sense.clear




while True:
    
      sense1=int(sense.get_compass())
      #sense1 is the angle corolated to cardinal directions that the front of the bot is facing
      
      print(sense1)
      sense1=int(sense.get_compass())
        
    