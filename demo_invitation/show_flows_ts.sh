#!/bin/sh

#set -x

for i in ts_pe1 ts_pc1 ts_pe2 ts_gw_tn
do
	echo $i
	sudo ovs-ofctl -OOpenFlow13 dump-flows $i | sed '/goto/d' 
done

echo ===all goto_tables omitted, use show_flows_t?_all.sh to see them===

