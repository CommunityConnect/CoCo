#!/bin/bash

IP_ADDR=$1
echo $IP_ADDR

# install tests
git clone https://git.opendaylight.org/gerrit/p/integration/test.git

# install JDK
sudo apt-get install openjdk-7-jre -y
echo "" | tee -a ~/.bashrc
echo "export JAVA_HOME=/usr/lib/jvm/java-1.7.0-openjdk-amd64" | tee -a ~/.bashrc
source ~/.bashrc

# get ODL
wget https://nexus.opendaylight.org/content/repositories/opendaylight.snapshot/org/opendaylight/integration/distribution-karaf/0.3.2-SNAPSHOT/distribution-karaf-0.3.2-20151009.051242-148.tar.gz
tar -xzf distribution-karaf-0.3.2-20151009.051242-148.tar.gz

# configure BGP
echo "" | tee -a ~/distribution-karaf-0.3.2-SNAPSHOT/etc/custom.properties
echo "vpnservice.bgpspeaker.host.name = $IP_ADDR" | tee -a ~/distribution-karaf-0.3.2-SNAPSHOT/etc/custom.properties

# run karaf
sudo apt-get install sshpass -y

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

sshpass -p karaf ssh -n -p 8101 -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no karaf@localhost "feature:install odl-vpnservice-api    odl-vpnservice-impl    odl-vpnservice-impl-rest    odl-vpnservice-impl-ui    odl-vpnservice-core"


sshpass -p karaf ssh -n -p 8101 -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no karaf@localhost "feature:repo-add mvn:org.opendaylight.integration/features-integration-test/0.3.2-SNAPSHOT/xml/features"
