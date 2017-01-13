#!/bin/bash
#installs quagga in dev version
#(ugly) hack: actually before running this script it is a good idea to run
#install-quagga.sh
#it will create all the config files and set correct permissions
cd
cd Downloads

git clone -b 0.99.24.1-mplsvpn+encap https://github.com/LabNConsulting/quagga-vnc.git
cd quagga-vnc

autoreconf -i
chmod +x configure
#for vtysh
sudo apt-get install libreadline-dev -y
./configure --prefix=/usr/ --sysconfdir=/etc/quagga/ --enable-multipath=10 --localstatedir=/var/run/quagga
make -j8
sudo make install

sudo cp /etc/init.d/quagga /etc/init.d/quagga.bak

sudo sed -i 's/D_PATH=\/usr\/lib\/quagga/D_PATH=\/usr\/sbin/g' /etc/init.d/quagga

sudo /etc/init.d/quagga restart
