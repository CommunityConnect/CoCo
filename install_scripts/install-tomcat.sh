#!/bin/bash

sudo apt-get install  -y tomcat7

sudo service tomcat7 stop

cd /usr/share/tomcat7
sudo ln -s /var/lib/tomcat7/conf conf
sudo ln -s /etc/tomcat7/policy.d/03catalina.policy conf/catalina.policy
sudo ln -s /var/log/tomcat7 log
sudo chmod -R 777 /usr/share/tomcat7/conf


sudo ln -s /var/lib/tomcat7/common common
sudo ln -s /var/lib/tomcat7/server server
sudo ln -s /var/lib/tomcat7/shared shared

cd  ~
