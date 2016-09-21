#!/bin/sh

#set -x

for i in tn_pe1 tn_pe2 tn_pc1
do
	echo $i
	sudo ovs-ofctl -OOpenFlow13 dump-flows $i | sed '/goto/d' 
done

echo ===all goto_tables omitted, use show_flows_t?_all.sh to see them===

