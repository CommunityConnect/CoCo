#!/bin/bash

#source this file to get $DISTRIB_CODENAME
. /etc/lsb-release

sudo touch /etc/apt/sources.list.d/nginx.list
echo "deb http://nginx.org/packages/ubuntu/ $DISTRIB_CODENAME nginx" | sudo tee -a /etc/apt/sources.list.d/nginx.list

sudo echo "deb-src http://nginx.org/packages/ubuntu/ $DISTRIB_CODENAME nginx" | sudo tee -a /etc/apt/sources.list.d/nginx.list

sudo apt-get update
sudo apt-get install nginx -y
