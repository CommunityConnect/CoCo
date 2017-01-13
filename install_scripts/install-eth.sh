#!/bin/bash

IP_ADDR=10.0.1.$1
echo $IP_ADDR

sudo ifdown eth1

if ! sudo grep -q "auto eth1" /etc/network/interfaces; then
	echo "" | sudo tee -a /etc/network/interfaces
	echo "auto eth1" | sudo tee -a /etc/network/interfaces
	echo "iface eth1 inet static" | sudo tee -a /etc/network/interfaces
	echo "address $IP_ADDR" | sudo tee -a /etc/network/interfaces
	echo "netmask 255.255.255.0" | sudo tee -a /etc/network/interfaces
fi

sudo ifup eth1