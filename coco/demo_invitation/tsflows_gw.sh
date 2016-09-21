#!/bin/bash

TS_IP=134.221.121.201
TN_IP=134.221.121.202


#WARNING: we have to guess gre port number
#it is not simply the last+1 because we have eth10 already added :/
#remedy: in mininet add pinghost first, then rest of the links, 
#then calculate how many ports we have

NPORTS=`sudo ovs-vsctl list-ports ts_gw_tn | wc -l`

#and then sudo ...output:$((NPORTS+1))

GRE_TS_TN=$((NPORTS+1))
sudo ovs-vsctl add-port ts_gw_tn ts_gw_tn-gre1 -- set interface ts_gw_tn-gre1 type=gre options:remote_ip=${TN_IP} options:local_ip=${TS_IP} options:link_speed=1G

#sudo ovs-ofctl add-flow ts_pe1 -O OpenFlow13 ip,in_port=${GRE_TS_TN},nw_src=10.0.0.3,nw_dst=10.0.0.4,actions=output:1
#sudo ovs-ofctl add-flow ts_pe1 -O OpenFlow13 ip,in_port=1,nw_src=10.0.0.4,nw_dst=10.0.0.3,actions=output:${GRE_TS_TN}

sudo ovs-ofctl add-flow ts_pe1 -O OpenFlow13 ip,in_port=4,nw_src=10.0.0.3,nw_dst=10.0.0.4,actions=output:1
sudo ovs-ofctl add-flow ts_pe1 -O OpenFlow13 ip,in_port=1,nw_src=10.0.0.4,nw_dst=10.0.0.3,actions=output:4

sudo ovs-ofctl add-flow ts_gw_tn -O OpenFlow13 in_port=${GRE_TS_TN},actions=output:1
sudo ovs-ofctl add-flow ts_gw_tn -O OpenFlow13 in_port=1,actions=output:${GRE_TS_TN}

#sudo ip route add 10.3.0.0/24 dev root-eth0

sudo ovs-ofctl add-flow ts_pe1 -O OpenFlow13 arp,actions=FLOOD

#sudo ovs-ofctl add-flow ts_pe1 -O OpenFlow13 priority=101,arp,in_port=2,actions=pop_vlan,FLOOD
#sudo ovs-ofctl add-flow ts_pe1 -O OpenFlow13 priority=101,arp,in_port=3,dl_type=0x0800,actions=push_vlan:0x8100,mod_vlan_vid:21,output:2

#sudo ovs-ofctl add-flow ts_pc1 -O OpenFlow13 arp,actions=FLOOD
#sudo ovs-ofctl add-flow ts_pe2 -O OpenFlow13 arp,actions=FLOOD

sudo ovs-ofctl add-flow ts_pe1 -O OpenFlow13 priority=101,dl_type=0x88cc,actions=CONTROLLER
#sudo ovs-ofctl add-flow ts_pc1 -O OpenFlow13 priority=101,dl_type=0x88cc,actions=CONTROLLER
#sudo ovs-ofctl add-flow ts_pe2 -O OpenFlow13 priority=101,dl_type=0x88cc,actions=CONTROLLER


#BGP with customers and domain BGP speaker

sudo ovs-ofctl add-flow ts_pe1 -O OpenFlow13 priority=101,tcp,in_port=2,nw_src=10.3.0.1,nw_dst=10.3.0.254,tp_dst=179,actions=output:3
sudo ovs-ofctl add-flow ts_pe1 -O OpenFlow13 priority=101,tcp,in_port=3,dl_type=0x0800,nw_src=10.3.0.254,nw_dst=10.3.0.1,tp_dst=179,actions=output:2
sudo ovs-ofctl add-flow ts_pe1 -O OpenFlow13 priority=101,tcp,in_port=3,dl_type=0x0800,nw_src=10.3.0.254,nw_dst=10.3.0.1,tp_src=179,actions=output:2
sudo ovs-ofctl add-flow ts_pe1 -O OpenFlow13 priority=101,tcp,in_port=2,nw_src=10.3.0.1,nw_dst=10.3.0.254,tp_src=179,actions=output:3


#sudo ovs-ofctl add-flow ts_pc1 -O OpenFlow13 tcp,in_port=2,nw_src=10.3.0.1,nw_dst=10.3.0.254,tp_dst=179,actions=output:1
#sudo ovs-ofctl add-flow ts_pc1 -O OpenFlow13 tcp,in_port=1,nw_src=10.3.0.254,nw_dst=10.3.0.1,tp_dst=179,actions=output:2
#sudo ovs-ofctl add-flow ts_pc1 -O OpenFlow13 tcp,in_port=1,nw_src=10.3.0.254,nw_dst=10.3.0.1,tp_src=179,actions=output:2
#sudo ovs-ofctl add-flow ts_pc1 -O OpenFlow13 tcp,in_port=2,nw_src=10.3.0.1,nw_dst=10.3.0.254,tp_src=179,actions=output:1


#sudo ovs-ofctl add-flow ts_pc1 -O OpenFlow13 tcp,in_port=3,nw_src=10.3.0.2,nw_dst=10.3.0.254,tp_dst=179,actions=output:1
#sudo ovs-ofctl add-flow ts_pc1 -O OpenFlow13 tcp,in_port=1,nw_src=10.3.0.254,nw_dst=10.3.0.2,tp_dst=179,actions=output:3
#sudo ovs-ofctl add-flow ts_pc1 -O OpenFlow13 tcp,in_port=3,nw_src=10.3.0.2,nw_dst=10.3.0.254,tp_src=179,actions=output:1
#sudo ovs-ofctl add-flow ts_pc1 -O OpenFlow13 tcp,in_port=1,nw_src=10.3.0.254,nw_dst=10.3.0.2,tp_src=179,actions=output:3



#sudo ovs-ofctl add-flow ts_pe2 -O OpenFlow13 tcp,in_port=2,nw_src=10.3.0.254,nw_dst=10.3.0.2,tp_dst=179,actions=output:1
#sudo ovs-ofctl add-flow ts_pe2 -O OpenFlow13 tcp,in_port=1,nw_src=10.3.0.2,nw_dst=10.3.0.254,tp_dst=179,actions=output:2
#sudo ovs-ofctl add-flow ts_pe2 -O OpenFlow13 tcp,in_port=1,nw_src=10.3.0.2,nw_dst=10.3.0.254,tp_src=179,actions=output:2
#sudo ovs-ofctl add-flow ts_pe2 -O OpenFlow13 tcp,in_port=2,nw_src=10.3.0.254,nw_dst=10.3.0.2,tp_src=179,actions=output:1


#BGP betweendomain BGP speakers in TS and TN

#sudo ovs-ofctl add-flow ts_pe1 -O OpenFlow13 tcp,in_port=${GRE_TS_TN},nw_src=10.2.0.254,nw_dst=10.3.0.254,tp_dst=179,actions=output:3
#sudo ovs-ofctl add-flow ts_pe1 -O OpenFlow13 tcp,in_port=3,nw_src=10.3.0.254,nw_dst=10.2.0.254,tp_dst=179,actions=output:${GRE_TS_TN}
#sudo ovs-ofctl add-flow ts_pe1 -O OpenFlow13 tcp,in_port=3,nw_src=10.3.0.254,nw_dst=10.2.0.254,tp_src=179,actions=output:${GRE_TS_TN}
#sudo ovs-ofctl add-flow ts_pe1 -O OpenFlow13 tcp,in_port=${GRE_TS_TN},nw_src=10.2.0.254,nw_dst=10.3.0.254,tp_src=179,actions=output:3

sudo ovs-ofctl add-flow ts_pe1 -O OpenFlow13 tcp,in_port=4,nw_src=10.2.0.254,nw_dst=10.3.0.254,tp_dst=179,actions=output:3
sudo ovs-ofctl add-flow ts_pe1 -O OpenFlow13 tcp,in_port=3,nw_src=10.3.0.254,nw_dst=10.2.0.254,tp_dst=179,actions=output:4
sudo ovs-ofctl add-flow ts_pe1 -O OpenFlow13 tcp,in_port=3,nw_src=10.3.0.254,nw_dst=10.2.0.254,tp_src=179,actions=output:4
sudo ovs-ofctl add-flow ts_pe1 -O OpenFlow13 tcp,in_port=4,nw_src=10.2.0.254,nw_dst=10.3.0.254,tp_src=179,actions=output:3




