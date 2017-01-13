#!/bin/bash
~/distribution-karaf-0.3.2-SNAPSHOT/bin/start

echo "Waiting OpenDaylight to launch on 8101..."

time_elapsed=0
while ! nc -z localhost 8101; do   
  sleep 1 # wait for 1/10 of the second before check again
  time_elapsed=$((time_elapsed+1))
  echo -ne "Waiting $time_elapsed seconds...\r"
done

echo
echo -e "Waited $time_elapsed seconds."

#logging
logfile_loc=`dirname $BASH_SOURCE`
logfile_loc=$logfile_loc"/odl.log"
echo "ODL start time: $time_elapsed" >> $logfile_loc

echo "OpenDaylight launched"

ssh -p 8101 -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no karaf@localhost