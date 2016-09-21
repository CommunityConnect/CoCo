#!/bin/sh

#set -x

for i in ts_pe1 ts_gw_tn
do
	echo $i
	sudo ovs-ofctl -OOpenFlow13 dump-flows $i  
done

