sudo mn -c
sudo killall -KILL exabgp
sudo rm /var/run/exabgp/*

sudo ifconfig tn_s1 down
sudo ifconfig tn_s2 down
sudo ifconfig ts_s1 down
sudo ifconfig ts_s1 down

sudo brctl delbr tn_s1
sudo brctl delbr tn_s2
sudo brctl delbr ts_s1
sudo brctl delbr ts_s2


