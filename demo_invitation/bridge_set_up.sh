#!/bin/bash

#bridge between mininet and VM host interface
sudo ifconfig root-eth0 0.0.0.0
brctl addif TN-$1 root-eth0
ip link set dev TN-$1 up

if [[ $1 =~ "CE"* ]]
then
	index=${1:2:1}
	#switch configuration - to modify MAC addresses
	sudo ovs-ofctl add-flow tn_mac_ce$index -O OpenFlow13 ip,nw_dst=10.2.$index.0/24,in_port=2,actions=mod_dl_dst:00:10:02:00:00:0$index,output:1
	sudo ovs-ofctl add-flow tn_mac_ce$index -O OpenFlow13 in_port=1,actions=output:2
	sudo ovs-ofctl add-flow tn_mac_ce$index -O OpenFlow13 ip,nw_dst=10.2.0.0/24,in_port=2,actions=output:1
fi
