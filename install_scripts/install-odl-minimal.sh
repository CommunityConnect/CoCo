#!/bin/bash

IP_ADDR=$1
echo $IP_ADDR

ODL_VER=distribution-karaf-0.3.2-Lithium-SR2

# install tests
git clone https://git.opendaylight.org/gerrit/p/integration/test.git

# install JDK
sudo apt-get install openjdk-7-jre -y
echo "" | tee -a ~/.bashrc
echo "export JAVA_HOME=/usr/lib/jvm/java-1.7.0-openjdk-amd64" | tee -a ~/.bashrc
source ~/.bashrc

# get ODL
wget https://nexus.opendaylight.org/content/repositories/opendaylight.release/org/opendaylight/integration/distribution-karaf/${ODL_VER}.tar.gz
tar -xzf ${ODL_VER}.tar.gz


# run karaf
sudo apt-get install sshpass -y

~/${ODL_VER}/bin/start

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

sshpass -p karaf ssh -n -p 8101 -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no karaf@localhost "feature:odl-openflowplugin-all odl-dlux-all"


sshpass -p karaf ssh -n -p 8101 -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no karaf@localhost "feature:repo-add mvn:org.opendaylight.integration/features-integration-test/0.3.2-Lithium-SR2/xml/features"
