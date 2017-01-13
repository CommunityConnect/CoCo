sudo stop network-manager
echo "manual" | sudo tee /etc/init/network-manager.override

echo =========
echo "Possibly needed now"
echo "sudo nano /etc/network/interfaces"
echo "auto ethX"
echo "iface ethX inet dhcp"
echo "or"
echo "iface ethX inet static"
echo "address 1.2.3.4"
echo "netmask 255.255.255.0"
echo "gateway 1.2.3.254"
echo "dns-nameservers 1.2.3.5"

echo "sudo ifconfig ethX up may also be needed"
