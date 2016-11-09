#!/bin/bash

if [[ "$1" ==  "tn" ]]
then

	#bridge between mininet and VM host interface
	if [[ $2 == "all" ]]
	then
		sudo ifconfig root-eth0 0.0.0.0
        	sudo brctl addif TN-CE1 root-eth0
        	sudo ip link set dev TN-CE1 up
	
		sudo ifconfig root-eth1 0.0.0.0
        	sudo brctl addif TN-CE2 root-eth1
        	sudo ip link set dev TN-CE2 up

		sudo ifconfig root-eth2 10.2.0.100
        	sudo brctl addif TN-BGP1 root-eth2
        	sudo ip link set dev TN-BGP1 up
	else
		sudo ifconfig root-eth0 0.0.0.0
		sudo brctl addif TN-$2 root-eth0
		sudo ip link set dev TN-$2 up
		if [[ $2 == "BGP1" ]]
		then
			sudo ifconfig root-eth0 10.2.0.100
		fi
	fi

	if [[ $2 =~ "CE"* ]] || [[ $2 == "all" ]]
	then
		sudo bash ./tnflows_mac.sh $2
	fi
fi

if [[ "$1" ==  "ts" ]]
then

        #bridge between mininet and VM host interface
        if [[ $2 == "all" ]]
        then
                sudo ifconfig root-eth0 0.0.0.0
                sudo brctl addif TS-CE1 root-eth0
                sudo ip link set dev TS-CE1 up

                sudo ifconfig root-eth1 10.3.0.100
                sudo brctl addif TS-BGP1 root-eth1
                sudo ip link set dev TS-BGP1 up
        else
                sudo ifconfig root-eth0 0.0.0.0
                sudo brctl addif TS-$2 root-eth0
                sudo ip link set dev TS-$2 up

		if [[ $2 == "BGP1" ]]
                then
			sudo ifconfig root-eth0 10.3.0.100
		fi
        fi

        if [[ $2 =~ "CE"* ]] || [[ $2 == "all" ]]
        then
                sudo bash ./tsflows_mac.sh $2
        fi
fi

