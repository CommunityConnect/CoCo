#!/bin/bash

TS_IP=134.221.121.201
TN_IP=134.221.121.202

#WARNING: we have to guess gre port number
#it is not simply the last+1 because we have eth10 already added :/
#remedy: in mininet add pinghost first, then rest of the links, 
#then calculate how many ports we have

NPORTS=`sudo ovs-vsctl list-ports tn_gw_ts | wc -l`

#and then sudo ...output:$((NPORTS+1))

sudo ovs-vsctl add-port tn_gw_ts tn_gw_ts-gre1 -- set interface tn_gw_ts-gre1 type=gre options:remote_ip=${TS_IP} options:local_ip=${TN_IP}

GRE_TN_TS=$((NPORTS+1))
#commented out because we have to use gateway switch
#sudo ovs-ofctl add-flow tn_pe2 -O OpenFlow13 ip,in_port=1,nw_src=10.0.0.3,nw_dst=10.0.0.4,actions=output:${GRE_TN_TS}
#sudo ovs-ofctl add-flow tn_pe2 -O OpenFlow13 ip,in_port=${GRE_TN_TS},nw_src=10.0.0.4,nw_dst=10.0.0.3,actions=output:1

sudo ovs-ofctl add-flow tn_pe2 -O OpenFlow13 ip,in_port=1,nw_src=10.0.0.3,nw_dst=10.0.0.4,actions=output:4
sudo ovs-ofctl add-flow tn_pe2 -O OpenFlow13 ip,in_port=4,nw_src=10.0.0.4,nw_dst=10.0.0.3,actions=output:1

#gateway switch is transparent ; it has to be in kernel space
sudo ovs-ofctl add-flow tn_gw_ts -O OpenFlow13 in_port=1,actions=output:${GRE_TN_TS}
sudo ovs-ofctl add-flow tn_gw_ts -O OpenFlow13 in_port=${GRE_TN_TS},actions=output:1


#sudo ip route add 10.2.0.0/24 dev root-eth0

#TODO rather dangerous in case of loop
sudo ovs-ofctl add-flow tn_pe1 -O OpenFlow13 arp,actions=FLOOD
sudo ovs-ofctl add-flow tn_pc1 -O OpenFlow13 arp,actions=FLOOD
sudo ovs-ofctl add-flow tn_pe2 -O OpenFlow13 arp,actions=FLOOD

sudo ovs-ofctl add-flow tn_pe1 -O OpenFlow13 priority=101,dl_type=0x88cc,actions=CONTROLLER
sudo ovs-ofctl add-flow tn_pc1 -O OpenFlow13 priority=101,dl_type=0x88cc,actions=CONTROLLER
sudo ovs-ofctl add-flow tn_pe2 -O OpenFlow13 priority=101,dl_type=0x88cc,actions=CONTROLLER


#BGP between client and domain BGP speaker

sudo ovs-ofctl add-flow tn_pe1 -O OpenFlow13 priority=101,tcp,in_port=2,nw_src=10.2.0.1,nw_dst=10.2.0.254,tp_dst=179,actions=output:3
sudo ovs-ofctl add-flow tn_pe1 -O OpenFlow13 priority=101,tcp,in_port=3,dl_type=0x0800,nw_src=10.2.0.254,nw_dst=10.2.0.1,tp_dst=179,actions=output:2
sudo ovs-ofctl add-flow tn_pe1 -O OpenFlow13 priority=101,tcp,in_port=3,dl_type=0x0800,nw_src=10.2.0.254,nw_dst=10.2.0.1,tp_src=179,actions=output:2
sudo ovs-ofctl add-flow tn_pe1 -O OpenFlow13 priority=101,tcp,in_port=2,nw_src=10.2.0.1,nw_dst=10.2.0.254,tp_src=179,actions=output:3


sudo ovs-ofctl add-flow tn_pc1 -O OpenFlow13 priority=101,tcp,in_port=2,nw_src=10.2.0.1,nw_dst=10.2.0.254,tp_dst=179,actions=output:1
sudo ovs-ofctl add-flow tn_pc1 -O OpenFlow13 priority=101,tcp,in_port=1,nw_src=10.2.0.254,nw_dst=10.2.0.1,tp_dst=179,actions=output:2
sudo ovs-ofctl add-flow tn_pc1 -O OpenFlow13 priority=101,tcp,in_port=1,nw_src=10.2.0.254,nw_dst=10.2.0.1,tp_src=179,actions=output:2
sudo ovs-ofctl add-flow tn_pc1 -O OpenFlow13 priority=101,tcp,in_port=2,nw_src=10.2.0.1,nw_dst=10.2.0.254,tp_src=179,actions=output:1


sudo ovs-ofctl add-flow tn_pc1 -O OpenFlow13 priority=101,tcp,in_port=3,nw_src=10.2.0.2,nw_dst=10.2.0.254,tp_dst=179,actions=output:1
sudo ovs-ofctl add-flow tn_pc1 -O OpenFlow13 priority=101,tcp,in_port=1,nw_src=10.2.0.254,nw_dst=10.2.0.2,tp_dst=179,actions=output:3
sudo ovs-ofctl add-flow tn_pc1 -O OpenFlow13 priority=101,tcp,in_port=3,nw_src=10.2.0.2,nw_dst=10.2.0.254,tp_src=179,actions=output:1
sudo ovs-ofctl add-flow tn_pc1 -O OpenFlow13 priority=101,tcp,in_port=1,nw_src=10.2.0.254,nw_dst=10.2.0.2,tp_src=179,actions=output:3

sudo ovs-ofctl add-flow tn_pe2 -O OpenFlow13 priority=101,tcp,in_port=2,nw_src=10.2.0.2,nw_dst=10.2.0.254,tp_dst=179,actions=output:3
sudo ovs-ofctl add-flow tn_pe2 -O OpenFlow13 priority=101,tcp,in_port=3,dl_type=0x0800,nw_src=10.2.0.254,nw_dst=10.2.0.2,tp_dst=179,actions=output:2
sudo ovs-ofctl add-flow tn_pe2 -O OpenFlow13 priority=101,tcp,in_port=3,dl_type=0x0800,nw_src=10.2.0.254,nw_dst=10.2.0.2,tp_src=179,actions=output:2
sudo ovs-ofctl add-flow tn_pe2 -O OpenFlow13 priority=101,tcp,in_port=2,nw_src=10.2.0.2,nw_dst=10.2.0.254,tp_src=179,actions=output:3


#BGP between domain BGP speakers from TN and TS

sudo ovs-ofctl add-flow tn_pc1 -O OpenFlow13 priority=101,tcp,in_port=3,nw_src=10.3.0.254,nw_dst=10.2.0.254,tp_dst=179,actions=output:1
sudo ovs-ofctl add-flow tn_pc1 -O OpenFlow13 priority=101,tcp,in_port=1,nw_src=10.2.0.254,nw_dst=10.3.0.254,tp_dst=179,actions=output:3
sudo ovs-ofctl add-flow tn_pc1 -O OpenFlow13 priority=101,tcp,in_port=3,nw_src=10.3.0.254,nw_dst=10.2.0.254,tp_src=179,actions=output:1
sudo ovs-ofctl add-flow tn_pc1 -O OpenFlow13 priority=101,tcp,in_port=1,nw_src=10.2.0.254,nw_dst=10.3.0.254,tp_src=179,actions=output:3

#sudo ovs-ofctl add-flow tn_pe2 -O OpenFlow13 priority=101,tcp,in_port=3,nw_src=10.2.0.254,nw_dst=10.3.0.254,tp_dst=179,actions=output:${GRE_TN_TS}
#sudo ovs-ofctl add-flow tn_pe2 -O OpenFlow13 priority=101,tcp,in_port=${GRE_TN_TS},nw_src=10.3.0.254,nw_dst=10.2.0.254,tp_dst=179,actions=output:3
#sudo ovs-ofctl add-flow tn_pe2 -O OpenFlow13 priority=101,tcp,in_port=${GRE_TN_TS},nw_src=10.3.0.254,nw_dst=10.2.0.254,tp_src=179,actions=output:3
#sudo ovs-ofctl add-flow tn_pe2 -O OpenFlow13 priority=101,tcp,in_port=3,nw_src=10.2.0.254,nw_dst=10.3.0.254,tp_src=179,actions=output:${GRE_TN_TS}

sudo ovs-ofctl add-flow tn_pe2 -O OpenFlow13 priority=101,tcp,in_port=3,nw_src=10.2.0.254,nw_dst=10.3.0.254,tp_dst=179,actions=output:4
sudo ovs-ofctl add-flow tn_pe2 -O OpenFlow13 priority=101,tcp,in_port=4,nw_src=10.3.0.254,nw_dst=10.2.0.254,tp_dst=179,actions=output:3
sudo ovs-ofctl add-flow tn_pe2 -O OpenFlow13 priority=101,tcp,in_port=4,nw_src=10.3.0.254,nw_dst=10.2.0.254,tp_src=179,actions=output:3
sudo ovs-ofctl add-flow tn_pe2 -O OpenFlow13 priority=101,tcp,in_port=3,nw_src=10.2.0.254,nw_dst=10.3.0.254,tp_src=179,actions=output:4




