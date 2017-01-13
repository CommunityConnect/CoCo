#!/bin/bash
#installs wireshark in the most recent stable version
#warning: if you just use sudo apt-get install wireshark on ubuntu 14.04 
#it will install version 1.10 which does not have OpenFlow dissector

sudo add-apt-repository ppa:wireshark-dev/stable
sudo apt-get update
sudo apt-get install wireshark -y

#normally non-root cannot sniff, this is a fix (agree)
sudo dpkg-reconfigure wireshark-common

echo "==========================="
sudo adduser $USER wireshark 
echo "==========================="
echo "==========================="
echo "logoff/logon may be needed"

