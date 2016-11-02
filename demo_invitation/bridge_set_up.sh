#!/bin/bash

if [[ "$1" =  "tn" ]]
then

	#bridge between mininet and VM host interface
	if [[ $2 == "all" ]]
	then
		sudo ifconfig root-eth0 0.0.0.0
        	brctl addif TN-CE1 root-eth0
        	ip link set dev TN-CE1 up
	
		sudo ifconfig root-eth1 0.0.0.0
        	brctl addif TN-CE2 root-eth1
        	ip link set dev TN-CE2 up

		sudo ifconfig root-eth2 0.0.0.0
        	brctl addif TN-BGP1 root-eth2
        	ip link set dev TN-BGP1 up
	else
		sudo ifconfig root-eth0 0.0.0.0
		brctl addif TN-$2 root-eth0
		ip link set dev TN-$2 up
	fi

	if [[ $2 =~ "CE"* ]] || [[ $2 == "all" ]]
	then
		sudo bash ./tnflows_mac.sh $2
	fi
fi

if [[ "$1" =  "ts" ]]
then

        #bridge between mininet and VM host interface
        if [[ $2 == "all" ]]
        then
                sudo ifconfig root-eth0 0.0.0.0
                brctl addif TS-CE1 root-eth0
                ip link set dev TN-CE1 up

                sudo ifconfig root-eth2 0.0.0.0
                brctl addif TS-BGP1 root-eth2
                ip link set dev TN-BGP1 up
        else
                sudo ifconfig root-eth0 0.0.0.0
                brctl addif TS-$2 root-eth0
                ip link set dev TS-$2 up
        fi

        if [[ $2 =~ "CE"* ]] || [[ $2 == "all" ]]
        then
                sudo bash ./tsflows_mac.sh $2
        fi
fi

