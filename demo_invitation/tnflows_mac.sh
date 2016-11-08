#!/bin/bash

if [[ $1 =~ "CE"* ]]
then
        index=${1:2:1}
        #switch configuration - to modify MAC addresses
        sudo ovs-ofctl add-flow tn_mac_ce$index -O OpenFlow13 ip,nw_dst=10.2.$index.0/24,in_port=2,actions=mod_dl_dst:00:10:02:00:00:0$index,output:1
        sudo ovs-ofctl add-flow tn_mac_ce$index -O OpenFlow13 in_port=1,actions=output:2
        sudo ovs-ofctl add-flow tn_mac_ce$index -O OpenFlow13 ip,nw_dst=10.2.0.0/24,in_port=2,actions=output:1
fi

if [[ $1 == "all" ]]
then
        #switch configuration - to modify MAC addresses
        sudo ovs-ofctl add-flow tn_mac_ce1 -O OpenFlow13 ip,nw_dst=10.2.1.0/24,in_port=2,actions=mod_dl_dst:00:10:02:00:00:01,output:1
        sudo ovs-ofctl add-flow tn_mac_ce1 -O OpenFlow13 in_port=1,actions=output:2
        sudo ovs-ofctl add-flow tn_mac_ce1 -O OpenFlow13 ip,nw_dst=10.2.0.0/24,in_port=2,actions=output:1

	sudo ovs-ofctl add-flow tn_mac_ce2 -O OpenFlow13 ip,nw_dst=10.2.2.0/24,in_port=2,actions=mod_dl_dst:00:10:02:00:00:02,output:1
        sudo ovs-ofctl add-flow tn_mac_ce2 -O OpenFlow13 in_port=1,actions=output:2
        sudo ovs-ofctl add-flow tn_mac_ce2 -O OpenFlow13 ip,nw_dst=10.2.0.0/24,in_port=2,actions=output:1
fi

