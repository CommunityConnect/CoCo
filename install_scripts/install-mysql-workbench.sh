sudo apt-get install mysql-workbench

echo in case of problems with gnome keyring
echo http://askubuntu.com/questions/554280/mysql-workbench-and-gnome-keyring-daemon-problem
echo sudo nano /usr/bin/mysql-workbench
echo 
echo if ls /run/user/`id -u`/keyring-* 1> /dev/null 2>&1; then
echo     gnome-keyring-daemon > /dev/null
echo fi
echo 
echo export GNOME_KEYRING_CONTROL=$(ls -d /run/user/`id -u`/keyring-* | head -n 1)
