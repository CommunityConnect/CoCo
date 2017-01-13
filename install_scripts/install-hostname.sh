#!/bin/bash

ID=$1

sudo sed -i "s/${HOSTNAME}/${HOSTNAME}${ID}/g" /etc/hostname 
sudo sed -i "s/${HOSTNAME}/${HOSTNAME}${ID}/g" /etc/hosts
sudo hostname ${HOSTNAME}${ID}

AUTH_COOKIE="$(xauth list)"
AUTH_COOKIE_ARRAY=($AUTH_COOKIE)
xauth add "$(hostname)/unix:0" MIT-MAGIC-COOKIE-1 ${AUTH_COOKIE_ARRAY[2]}