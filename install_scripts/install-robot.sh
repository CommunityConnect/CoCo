#!/bin/bash
#run as sudo
sudo apt-get install python-pip -y
sudo pip install --upgrade pip
sudo apt-get install python-paramiko -y
sudo apt-get install python-wxgtk2.8 -y

#pip 8 is needed probably to fetch it correctly, 
#that's why we upgrade. 
#If we proceed with pip 7 we see requestlibrary on red in ride
sudo pip install -U requests 
sudo pip install -U robotframework
sudo pip install -U robotframework-sshlibrary
sudo pip install -U robotframework-requests
sudo pip install --upgrade robotframework-httplibrary
#for code highlight
sudo pip install pygments


sudo pip install robotframework-ride


#to run, type
#>ride.py
