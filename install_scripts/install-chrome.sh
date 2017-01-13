#!/bin/bash
#chrome installation
wget -q -O - https://dl-ssl.google.com/linux/linux_signing_key.pub | sudo apt-key add -
sudo sh -c 'echo "deb http://dl.google.com/linux/chrome/deb/ stable main" >> /etc/apt/sources.list.d/google-chrome.list'
sudo apt-get update
sudo apt-get install google-chrome-stable -y

echo !!!!!!!!!!!!!!!!!!!!
echo If the browser does not start, check ownership of ~/.config folder
echo should be owned by user, not root
