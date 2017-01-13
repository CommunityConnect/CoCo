#!/bin/bash
#add env variables to .bashrc
#WARNING: check versions of installed packages

echo 'export JAVA_HOME=/usr/lib/jvm/java-1.7.0-openjdk-amd64' >> ~/.bashrc

echo 'export PYTHONPATH="/usr/local/lib/python2.7/dist-packages/mininet-2.2.1-py2.7.egg:$PYTHONPATH"' >> ~/.bashrc

source ~/.bashrc
