# demo_invitation

This is a demonstration of multidomain CoCo with email-based VPN invitation system.
Under the hood, vpnintent framework is used.


Demo
1. Start ODL controller
 /home/coco/vpnservice-karaf-intent/distribution-karaf-0.4.2-Berylium-SR2-clean/bin/karaf

 needed features are

 feature:install odl-vpn-service-intent
 feature:install odl-dlux-all


2. start mysql server ( if not running already: sudo service mysql status )

 The database (CoCoINV) is automatically populated based on mininet topology. 


3. Note: to run mininet (next step) for standard python installation we may need
 sudo pip install netaddr

 sudo apt-get install python-mysqldb

 or even before it

 sudo apt-get install libmysqlclient-dev


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
