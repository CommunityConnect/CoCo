# CoCo
testing
HOW TO RUN COCO:

https://www.dropbox.com/s/tbusvk9wr9khjec/howToRunCoco.pdf?dl=0

TODO:

<<<<<<< HEAD
Demo
1. Start ODL controller

 /home/coco/distribution-karaf-0.4.2-Beryllium-SR2-vpnintent/bin/karaf

 if installing clean ODL, needed features are

 feature:install odl-vpn-service-intent
 feature:install odl-dlux-all


2. start mysql server ( if not running already: sudo service mysql status )

 The database (CoCoINV) is automatically populated based on mininet topology. 


3. Note: to run mininet (next step) for standard python installation we may need
 sudo pip install netaddr

 sudo apt-get install python-mysqldb

 or even before it

 sudo apt-get install libmysqlclient-dev

 exabgp will need this for communication with portal 

 sudo pip install httplib2

[DEBUG] to run test server
 sudo pip install tornado
 then run
 /home/coco/demo_invitation/exabgp_tmpdev/tornado-web.py
 this server listens on 5003 and accepts POST and dumps them to the console
 it pretends to be a portal module which will really 
 accept and process updates from BGP

4. Start mininet with predefined topologies


 To run TNO North topology, start
 ./mntn.sh

 then to add initial configuration (BGP flows, GRE), run
 ./tnflows_gw.sh


 To run TNO North topology, start
 ./mnts.sh

 then to add initial configuration (BGP flows, GRE), run
 ./tsflows_gw.sh


 Note: GRE tunnel endpoints configured in t?flows_gw.sh are likely to be reconfigured



5. Starting minites starts also exabgp route reflector (within a domain). It accepts REST calls on http://10.10.10.1:5001
which will originate from portal

[DEBUG]
To test, issue on tn

curl --form "command=neighbor 10.3.0.254 announce route 2.2.0.0/24 next-hop 10.2.0.254 extended-community 0x0002FDE800000001 extended-community 0x8ABCBEEFDEADBEEF" http://10.10.10.1:5001/

on ts tornado server we should see json string with the BGP update








=== GARBAGE
 Demo
1) start ODL
/home/coco/vpnservice-karaf-intent/distribution-karaf-0.4.2-Berylium-SR2-clean/bin/karaf

needed features are

feature:install odl-vpn-service-intent
feature:install odl-dlux-all


2) start mysql
...

Note: if using topo_bbf1_db.py (recommended, see below) then the database is automatically
populated based on mininet topology. As a backup option, there is 
demo_bbf_clean.sql file which contains all needed but empty tables.
Use it in case of troubles, when 'factory reset' is needed etc. or when
you comment out line 402 in topo_bbf1db.py
402 |	databaseDump(net)
 

#DON'T RUN UNLESS YOU KNOW WHAT YOU ARE DOING

3) start mininet
cd /home/coco/demo_bbf
sudo python topo_bbf1_db.py

we may need
sudo pip install netaddr

sudo apt-get install python-mysqldb

or even before it

sudo apt-get install libmysqlclient-dev



4) kill arp flows (static arps installed on the hosts)
/home/coco/demo_bbf/del_arp_flows.sh

5) start CoCo portal
        5a) run Eclipse
               /home/coco/eclipse_ee/eclipse/eclipse
               use workspace  /home/coco/workspace_cocoIntent
        5b) within Eclipse go to "Servers" tab (bottom panel)
        5c) Right click "Tomcat v7.0 Server at localhost" and click "Start"

               note: dafault 8080 port conflicts with ODL; double click on the server name 
               in "Servers" tab and change http 1.1 port to, say, 9090

               IP/database name/password  may need to be adjusted
               navigate to  CoCo-agent/src/net/geant/coco/agent/portal/props
               adjust the values in config.properties and jdbc.properties

               also adjust Servers/Tomcat 7.0/context.xml

 

6)use the following link to start coco portal 
http://localhost:9090/CoCo-agent/static/index.html 

7) connect hosts (e.g., h1 and h2) in CoCo portal and check connectivity from mininet
mininet> xterm h1
ping 10.0.2.1
=======
- should one site be allowed in multiple VPNs?
  - it works on a flow level, database doesnt allow that
- what about vpn 'all'?
>>>>>>> 2c6a122593e32a964dc737cfd636410ca23e7ca5
