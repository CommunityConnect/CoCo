#!/bin/bash

sudo apt-get install quagga -y


#[PZ] quagga/ubuntu setup file based on
#[1] https://wiki.ubuntu.com/JonathanFerguson/Quagga

# we assume quagga is already installed


##Enable IPv4 Forwarding: 

echo "net.ipv4.conf.all.forwarding=1" | sudo tee -a /etc/sysctl.conf
echo "net.ipv4.conf.default.forwarding=1" | sudo tee -a /etc/sysctl.conf
sudo sysctl -p



##Create the configuration files: 

sudo touch /etc/quagga/babeld.conf
sudo touch /etc/quagga/bgpd.conf
sudo touch /etc/quagga/isisd.conf
sudo touch /etc/quagga/ospf6d.conf
sudo touch /etc/quagga/ospfd.conf
sudo touch /etc/quagga/ripd.conf
sudo touch /etc/quagga/ripngd.conf
sudo touch /etc/quagga/vtysh.conf
sudo touch /etc/quagga/zebra.conf

#[PZ] note: different to [1]; All daemons confings in separate files

echo "no service integrated-vtysh-config" | sudo tee -a /etc/quagga/vtysh.conf
echo "username quagga nopassword" | sudo tee -a /etc/quagga/vtysh.conf

##Change the owner and the mode of the configuration files:
sudo chown quagga:quagga /etc/quagga/babeld.conf && sudo chmod 640 /etc/quagga/babeld.conf
sudo chown quagga:quagga /etc/quagga/bgpd.conf && sudo chmod 640 /etc/quagga/bgpd.conf
sudo chown quagga:quagga /etc/quagga/isisd.conf && sudo chmod 640 /etc/quagga/isisd.conf
sudo chown quagga:quagga /etc/quagga/ospf6d.conf && sudo chmod 640 /etc/quagga/ospf6d.conf
sudo chown quagga:quagga /etc/quagga/ospfd.conf && sudo chmod 640 /etc/quagga/ospfd.conf
sudo chown quagga:quagga /etc/quagga/ripd.conf && sudo chmod 640 /etc/quagga/ripd.conf
sudo chown quagga:quagga /etc/quagga/ripngd.conf && sudo chmod 640 /etc/quagga/ripngd.conf
sudo chown quagga:quaggavty /etc/quagga/vtysh.conf && sudo chmod 660 /etc/quagga/vtysh.conf
sudo chown quagga:quagga /etc/quagga/zebra.conf && sudo chmod 640 /etc/quagga/zebra.conf 

##Edit which routing protocols are to run: 
#lazy: start all
sudo sed -i s/=no/=yes/ /etc/quagga/daemons

#uncomment and edit to turn on only the wanted ones
#for TMPPROT in zebra bgpd ospfd ospf6d ripd ripngd isisd babeld; do
#	sudo sed -i s/${TMPPROT}=no/${TMPPROT}=yes/ /etc/quagga/daemons
#done


##Edit telnet access and the retaining of routes over restarts: 

sudo sed -i 's/zebra_options="  --daemon -A 127.0.0.1"/zebra_options=" --daemon -A 127.0.0.1 -P 2601 -u quagga -g quagga --keep_kernel --retain"/' /etc/quagga/debian.conf 
sudo sed -i 's/bgpd_options="   --daemon -A 127.0.0.1"/bgpd_options=" --daemon -A 127.0.0.1 -P 2605 -u quagga -g quagga --retain -p 179"/' /etc/quagga/debian.conf  
sudo sed -i 's/ospfd_options="  --daemon -A 127.0.0.1"/ospfd_options=" --daemon -A 127.0.0.1 -P 2604 -u quagga -g quagga"/' /etc/quagga/debian.conf  
sudo sed -i 's/ospf6d_options=" --daemon -A ::1"/ospf6d_options=" --daemon -A ::1 -P 2606 -u quagga -g quagga"/' /etc/quagga/debian.conf  
sudo sed -i 's/ripd_options="   --daemon -A 127.0.0.1"/ripd_options=" --daemon -A 127.0.0.1 -P 2602 -u quagga -g quagga --retain"/' /etc/quagga/debian.conf  
sudo sed -i 's/ripngd_options=" --daemon -A ::1"/ripngd_options=" --daemon -A ::1 -P 2603 -u quagga -g quagga --retain"/' /etc/quagga/debian.conf  
sudo sed -i 's/isisd_options="  --daemon -A 127.0.0.1"/isisd_options=" --daemon -A 127.0.0.1 -P 2608 -u quagga -g quagga"/' /etc/quagga/debian.conf  
sudo sed -i 's/babeld_options=" --daemon -A 127.0.0.1"/babeld_options=" --daemon -A 127.0.0.1 -P 2609 -u quagga -g quagga"/' /etc/quagga/debian.conf  

##get ridof END and bank screen 
echo "export VTYSH_PAGER=more" | sudo tee -a /etc/bash.bashrc


sudo /etc/init.d/quagga restart 

echo "remember that line vty and password  in /etc/quagga/*.conf have to be set to telnet to given deamon"

