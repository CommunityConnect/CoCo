#!/bin/bash

sudo apt-get install -y git

#install mininet 2.2.1
cd ~/
git clone git://github.com/mininet/mininet
cd mininet
git checkout -b 2.2.1
cd ..
./mininet/util/install.sh -nfv
./mininet/util/install.sh -V 2.4.0

