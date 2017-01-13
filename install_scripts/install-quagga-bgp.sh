#!/bin/bash
#installs quagga in dev version and patches for bgp vnc
#(ugly) hack: actually before running this script it is a good idea to run
#install-quagga.sh
#it will create all the config files and set correct permissions
cd ~/quagga

git clone http://git.savannah.gnu.org/cgit/quagga.git
mv quagga quagga-bgp
cd quagga-bgp

for PATCH in 1935 1934 1939 1933 1937 1936 1940 1932 1938 1941; do
  wget http://patchwork.quagga.net/patch/${PATCH}/raw/ -O ${PATCH}.patch
  git apply ${PATCH}.patch
done

autoreconf -i
chmod +x configure
#for vtysh
sudo apt-get install libreadline-dev -y
./configure --prefix=/usr/ --sysconfdir=/etc/quagga/ --enable-multipath=10 --localstatedir=/var/run/quagga --enable-bgp-vnc
make -j8
sudo make install

sudo cp /etc/init.d/quagga /etc/init.d/quagga.bak

sudo sed -i 's/D_PATH=\/usr\/lib\/quagga/D_PATH=\/usr\/sbin/g' /etc/init.d/quagga

sudo /etc/init.d/quagga restart
