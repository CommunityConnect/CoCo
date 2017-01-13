#!/bin/bash
set +x
sudo touch /etc/apt/sources.list.d/sourcegear.list
echo "deb http://debian.sourcegear.com/ubuntu precise main" | sudo tee -a /etc/apt/sources.list.d/sourcegear.list
#will not work if 'trusty' is used (14.04)
sudo wget -O - http://debian.sourcegear.com/SOURCEGEAR-GPG-KEY | sudo apt-key add -

sudo apt-get update

sudo apt-get install diffmerge -y
