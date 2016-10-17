#!/bin/bash
sudo ifconfig root-eth0 0.0.0.0
brctl addif TN-$1 root-eth0
ip link set dev TN-$1 up
