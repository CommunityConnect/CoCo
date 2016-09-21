ExaBGP can be used to injecting BGP updates (with e.g. extended community attributes)

1) download exabgp
we have used

git clone https://github.com/Exa-Networks/exabgp.git .
git checkout 3.4

2) prepare configuration file  e.g.,
ts2.conf
where we also give a name of a script to be run
api-add-remove.run

remember to chmod +x api-add-remove.run

3)start exabgp

~/exabgp/3.4/sbin/exabgp ~/demo_invitation/exabgp/ts2.conf

4) it is possible to send commands to exabgp via REST
start

~/exabgp/3.4/sbin/exabgp ~/demo_invitation/exabgp/ts2_simplehttp.conf

which calls 

simplehttp_api.py;

and use

curl --form "command=neighbor 134.221.121.202 withdraw route 166.10.0.0/24 next-hop self" http://localhost:5001/
curl --form "command=neighbor 10.2.0.1 announce route 2.2.0.0/24 next-hop 10.2.0.254" http://10.10.10.1:5001/
curl --form "command=neighbor 10.2.0.1 withdraw route 2.2.0.0/24 next-hop 10.2.0.254" http://10.10.10.1:5001/

curl --form "command=neighbor 10.2.0.1 announce route 2.2.0.0/24 next-hop  10.2.0.254 extended-community 0x0002FDE800000001 extended-community 0x8ABCBEEFDEADBEEF" http://10.10.10.1:5001/

curl --form "command=neighbor 10.3.0.254 announce route 2.2.0.0/24 next-hop  10.2.0.254 extended-community 0x0002FDE800000001 extended-community 0x8ABCBEEFDEADBEEF" http://10.10.10.1:5001/
curl --form "command=neighbor 10.3.0.254 announce route 2.2.0.0/24 next-hop  10.2.0.254 extended-community 0x0002FDE800000001 extended-community 0x8ABCBEEFDEADBEEF" http://10.10.10.1:5001/


