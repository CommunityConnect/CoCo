#!/bin/bash

#configure client VM (first parameter of the script}

if [ $# -eq 0 ]
  then
    echo "parameter of the script must be a number 1-9"
fi


#network part
sudo ip addr add 10.0.0.${1}/24 dev ens192
sudo ifconfig ens192 hw addr 00:10:00:00:00:0${1}
sudo ifconfig ens192 up
sudo arp -s 10.0.0.254 00:10:00:00:02:54

sudo apt-get install nano git git-gui -y

cd
mkdir DANE-SDN
cd DANE-SDN/
git clone http://gitlab.tnomedialab.nl/DANE-SDN/dane-dashjs


cd
git clone http://github.com/piotrekz79/install_scripts

cd install_scripts

./install-vnc.sh
./install-pip.sh 
sudo pip install flask

./install-chrome.sh

echo ###########################
echo install https://chrome.google.com/webstore/detail/allow-control-allow-origi/nlfbmbojpeacfghkpbjhddihlkkiljbi