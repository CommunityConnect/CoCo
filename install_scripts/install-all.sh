#!/bin/bash

NUM=1

ID=$NUM

IP_ADDR=10.0.0.$NUM

echo $ID
echo $IP_ADDR

#./scripts/install-hostname.sh $NUM
#./scripts/install-eth.sh $NUM
./scripts/initial-config.sh
#./scripts/install-chrome.sh
./scripts/install-quagga.sh
./scripts/install-robot.sh
./scripts/install-mininet-and-ovs.sh
./scripts/install-odl.sh $IP_ADDR
