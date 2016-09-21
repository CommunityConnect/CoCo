#!/usr/bin/python

# This script creates TNO North/TNO south mininet topology
# author:PZ, inspired by ONOS sdnip tutorial

from mininet.topo import Topo
from mininet.net import Mininet
from mininet.cli import CLI
from mininet.log import setLogLevel, info, debug
from mininet.node import Host, RemoteController
from mininet.node import Switch
from mininet.nodelib import LinuxBridge

import MySQLdb as mdb  # global, used by returnSwitchConnections
from netaddr import *
import sys

# QUAGGA_DIR = '/usr/lib/quagga'
QUAGGA_DIR = '/usr/sbin'
# Must exist and be owned by quagga user (quagga:quagga by default on Ubuntu)
QUAGGA_RUN_DIR = '/var/run/quagga'
CONFIG_DIR = '/home/coco/demo_invitation/bgp_configs'

EXABGP_DIR = '/usr/local/bin/exabgp'
EXABGP_RUN_DIR = '/var/run/exabgp'
EXABGP_LOG_DIR = '/var/log/exabgp'
#/home/coco/demo_invitation/exabgp
#mysql parameters
DB_HOST="localhost"
DB_USER="coco"
DB_PWD="cocorules!"
DB_NAME="CoCoINV"


class SdnIpHost(Host):
    def __init__(self, name, ip, route, *args, **kwargs):
        Host.__init__(self, name, ip=ip, *args, **kwargs)

        self.route = route

    def config(self, **kwargs):
        Host.config(self, **kwargs)

        debug("configuring route %s" % self.route)

        self.cmd('ip route add default via %s' % self.route)


class PingableHost(Host):
    def __init__(self, name, ip, mac, remoteIP, remoteMAC, *args, **kwargs):
        Host.__init__(self, name, ip=ip, mac=mac, *args, **kwargs)

        self.remoteIP = remoteIP
        self.remoteMAC = remoteMAC

    def config(self, **kwargs):
        Host.config(self, **kwargs)

        debug("configuring arp for %s %s" % (self.remoteIP, self.remoteMAC))

        self.setARP(self.remoteIP, self.remoteMAC)


class QBGPRouter(Host):
    def __init__(self, name, quaggaConfFile, zebraConfFile, intfDict, ARPDict, *args, **kwargs):
        Host.__init__(self, name, *args, **kwargs)

        self.quaggaConfFile = quaggaConfFile
        self.zebraConfFile = zebraConfFile
        self.intfDict = intfDict
        # TODO should be optional?
        self.ARPDict = ARPDict

    def config(self, **kwargs):
        Host.config(self, **kwargs)
        self.cmd('sysctl net.ipv4.ip_forward=1')

        for intf, attrs in self.intfDict.items():
            self.cmd('ip addr flush dev %s' % intf)
            if 'mac' in attrs:
                self.cmd('ip link set %s down' % intf)
                self.cmd('ip link set %s address %s' % (intf, attrs['mac']))
                self.cmd('ip link set %s up ' % intf)
                self.nameToIntf[intf].mac=attrs['mac']
            # for addr in attrs['ipAddrs']:
            if 'vlan' in attrs:
                # self.cmd('ip addr flush dev %s')
                self.cmd('ip link add link %s name %s.%s type vlan id %s' % (intf, intf, attrs['vlan'], attrs['vlan']))
                self.cmd('ip addr add %s dev %s.%s' % (attrs['ipAddrs'], intf, attrs['vlan']))
                self.cmd('ip link set dev %s.%s up' % (intf, attrs['vlan']))
            if ('ipAddrs' in attrs) & ('vlan' not in attrs):
                self.cmd('ip addr add %s dev %s' % (attrs['ipAddrs'], intf))
                self.nameToIntf[intf].ip=attrs['ipAddrs'].split('/')[0]
                self.nameToIntf[intf].prefixLen=attrs['ipAddrs'].split('/')[1]


        self.cmd('%s/zebra -d -f %s -z %s/zebra%s.api -i %s/zebra%s.pid' % (
        QUAGGA_DIR, self.zebraConfFile, QUAGGA_RUN_DIR, self.name, QUAGGA_RUN_DIR, self.name))
        self.cmd('%s/bgpd -d -f %s -z %s/zebra%s.api -i %s/bgpd%s.pid' % (
        QUAGGA_DIR, self.quaggaConfFile, QUAGGA_RUN_DIR, self.name, QUAGGA_RUN_DIR, self.name))

        for attrs in self.ARPDict.itervalues():
            if 'localdev' in attrs:
                self.cmd('ip route add %s%s dev %s' % (attrs['remoteIP'], attrs['remoteMask'], attrs['localdev']))
                self.setARP(attrs['remoteIP'], attrs['remoteMAC'])
            elif 'nexthop' in attrs:
                self.cmd('ip route add %s%s via %s' % (attrs['remoteIP'], attrs['remoteMask'], attrs['nexthop']))
                self.setARP(attrs['nexthop'], attrs['remoteMAC'])

    def terminate(self):
        #I don't think it works because it does not read pid number
        #self.cmd("ps ax | egrep 'bgpd%s.pid|zebra%s.pid' | awk '{print $1}' | xargs kill" % (self.name, self.name))
        self.cmd("kill `cat %s/bgpd%s.pid`" % (QUAGGA_RUN_DIR, self.name))
        self.cmd("kill `cat %s/zebra%s.pid`" % (QUAGGA_RUN_DIR, self.name))

        Host.terminate(self)

class EXABGPRouteReflector(Host):
    def __init__(self, name, exabgpIniFile, exabgpConfFile, intfDict, ARPDict, *args, **kwargs):
        Host.__init__(self, name, *args, **kwargs)

        self.exabgpConfFile = exabgpConfFile
        self.exabgpIniFile = exabgpIniFile
        self.intfDict = intfDict
        # TODO should be optional?
        self.ARPDict = ARPDict

    def config(self, **kwargs):
        Host.config(self, **kwargs)
        self.cmd('sysctl net.ipv4.ip_forward=1')

        for intf, attrs in self.intfDict.items():
            self.cmd('ip addr flush dev %s' % intf)
            if 'mac' in attrs:
                self.cmd('ip link set %s down' % intf)
                self.cmd('ip link set %s address %s' % (intf, attrs['mac']))
                self.cmd('ip link set %s up ' % intf)
                self.nameToIntf[intf].mac=attrs['mac']
            # for addr in attrs['ipAddrs']:
            if 'vlan' in attrs:
                # self.cmd('ip addr flush dev %s')
                self.cmd('ip link add link %s name %s.%s type vlan id %s' % (intf, intf, attrs['vlan'], attrs['vlan']))
                self.cmd('ip addr add %s dev %s.%s' % (attrs['ipAddrs'], intf, attrs['vlan']))
                self.cmd('ip link set dev %s.%s up' % (intf, attrs['vlan']))
            if ('ipAddrs' in attrs) & ('vlan' not in attrs):
                self.cmd('ip addr add %s dev %s' % (attrs['ipAddrs'], intf))
                self.nameToIntf[intf].ip=attrs['ipAddrs'].split('/')[0]
                self.nameToIntf[intf].prefixLen=attrs['ipAddrs'].split('/')[1]


        self.cmd('env exabgp.daemon.pid=%s/exabgp%s.pid exabgp.log.destination=%s/exabgp%s.log exabgp -e %s %s' %(
            EXABGP_RUN_DIR, self.name, EXABGP_LOG_DIR, self.name, self.exabgpIniFile, self.exabgpConfFile
        ))

#        self.cmd('env exabgp.daemon.pid=%s/exabgp%s.pid exabgp -e %s %s' %(
#            EXABGP_RUN_DIR, self.name, self.exabgpIniFile, self.exabgpConfFile
#        ))

        for attrs in self.ARPDict.itervalues():
            if 'localdev' in attrs:
                self.cmd('ip route add %s%s dev %s' % (attrs['remoteIP'], attrs['remoteMask'], attrs['localdev']))
                self.setARP(attrs['remoteIP'], attrs['remoteMAC'])
            elif 'nexthop' in attrs:
                self.cmd('ip route add %s%s via %s' % (attrs['remoteIP'], attrs['remoteMask'], attrs['nexthop']))
                self.setARP(attrs['nexthop'], attrs['remoteMAC'])

    def terminate(self):
        self.cmd("mv %s/exabgp%s.log %s/exabgp%s_`cat %s/exabgp%s.pid`.log" %(
            EXABGP_LOG_DIR, self.name, EXABGP_LOG_DIR, self.name, EXABGP_RUN_DIR, self.name))
        self.cmd("kill `cat %s/exabgp%s.pid`" % (EXABGP_RUN_DIR, self.name))


        Host.terminate(self)


class MDCoCoTopoNorth(Topo):
    # TODO : call MDCoCoTopo with domain name parameter
    # we were not successful with
    # def __init__(self, domainname):
    #    Topo.__init__(self, domainname)
    #    self.domainname = domainname

    "Multidomain CoCo topology - TNO North"

    def build(self):
        domID = 2

        tn_pe1 = self.addSwitch('tn_pe1', dpid='0000000000000021', datapath='user')
        tn_pc1 = self.addSwitch('tn_pc1', dpid='0000000000000022', datapath='user')
        tn_pe2 = self.addSwitch('tn_pe2', dpid='0000000000000023', datapath='user')
        tn_gw_ts = self.addSwitch('tn_gw_ts', dpid='0000000000000024')

        # tn_pe1 = self.addSwitch('tn_pe1', dpid='0000000000000021')
        # tn_pc1 = self.addSwitch('tn_pc1', dpid='0000000000000022')
        # tn_pe2 = self.addSwitch('tn_pe2', dpid='0000000000000023')

        # [PZ] perhaps we will add s4 later; now we want to avoid loop problems
        #        s4 = self.addSwitch('s4', dpid='0000000000000004')

        # add pingable hosts at the edges
        # TODO : for any kind of multihoming pingable hosts should have a dictionary of arp entries, not just one?
        pinghost = self.addHost('tn_ph_sn', cls=PingableHost, ip='10.0.0.2/24', mac='00:10:00:00:00:02',
                                remoteIP='10.0.0.1', remoteMAC='00:10:00:00:00:01')
        self.addLink(tn_pe1, pinghost)

        pinghost = self.addHost('tn_ph_ts', cls=PingableHost, ip='10.0.0.3/24', mac='00:10:00:00:00:03',
                                remoteIP='10.0.0.4', remoteMAC='00:10:00:00:00:04')
        self.addLink(tn_pe2, pinghost)

        zebraConf = '%s/zebra.conf' % CONFIG_DIR

        # Switches we want to attach our routers to, in the correct order
        attachmentSwitches = [tn_pe1, tn_pe2]
        nRouters = 2
        nHosts = 2

        # Set up the internal BGP speaker
        bgpEth0 = {'mac': '00:10:0%s:00:02:54' % domID,
                   'ipAddrs': '10.%s.0.254/24' % domID}
        bgpEth1 = {'ipAddrs': '10.10.10.1/24'}
        bgpIntfs = {'tn_bgp1-eth0': bgpEth0,
                    'tn_bgp1-eth1': bgpEth1}

        ts_bgp1 = {'remoteMAC': '00:10:03:00:02:54',
                   'remoteIP': '10.3.0.254',
                   'remoteMask': '/32',
                   'localdev': 'tn_bgp1-eth0'}

        # TODO sould be generated!!
        tn_ce1_arp = {'remoteMAC': '00:10:02:00:00:01',
                      'remoteIP': '10.2.0.1',
                      'remoteMask': '/32',
                      'localdev': 'tn_bgp1-eth0'}

        # TODO sould be generated!!
        tn_ce2_arp = {'remoteMAC': '00:10:02:00:00:02',
                      'remoteIP': '10.2.0.2',
                      'remoteMask': '/32',
                      'localdev': 'tn_bgp1-eth0'}

        ARPBGPpeers = {'ts_bgp1': ts_bgp1,
                       'tn_ce1_arp': tn_ce1_arp,
                       'tn_ce2_arp': tn_ce2_arp}

#        bgp = self.addHost("tn_bgp1", cls=QBGPRouter,
#                           quaggaConfFile='%s/tn_bgp1.conf' % CONFIG_DIR,
#                           zebraConfFile=zebraConf,
#                           intfDict=bgpIntfs,
#                           ARPDict=ARPBGPpeers)
        bgp = self.addHost("tn_bgp1", cls=EXABGPRouteReflector,
                           exabgpIniFile='%s/exabgp_config.ini' % CONFIG_DIR,
                           exabgpConfFile='%s/exabgp_tn2_simplehttp.conf' % CONFIG_DIR,
#                           exabgpConfFile='%s/exabgp_tn2_api-add-remove.conf' % CONFIG_DIR,
                           intfDict=bgpIntfs,
                           ARPDict=ARPBGPpeers)

        for i in range(1, nRouters + 1):
            name = 'tn_ce%s' % i
            # drop vlans
            #            eth0 = { 'mac' : '00:10:0%s:00:00:0%s' % (domID, i),
            #                     'ipAddrs' : '10.%s.0.%s/24' % (domID, i),
            #                     'vlan' : '%s%s' % (domID, i)}
            eth0 = {'mac': '00:10:0%s:00:00:0%s' % (domID, i),
                    'ipAddrs': '10.%s.0.%s/24' % (domID, i)}
            eth1 = {'mac': '00:10:0%s:0%s:02:54' % (domID, i),
                    'ipAddrs': '10.%s.%s.254/24' % (domID, i)}

            intfs = {'%s-eth0' % name: eth0,
                     '%s-eth1' % name: eth1}

            bgpgw = {'remoteMAC': bgpEth0['mac'],
                     'remoteIP': '10.0.0.0',
                     'remoteMask': '/8',
                     'nexthop': bgpEth0['ipAddrs'].split('/')[0]}

            ARPfakegw = {'bgpgw': bgpgw}

            quaggaConf = '%s/tn_ce%s.conf' % (CONFIG_DIR, i)

            router = self.addHost(name, cls=QBGPRouter, quaggaConfFile=quaggaConf,
                                  zebraConfFile=zebraConf, intfDict=intfs, ARPDict=ARPfakegw)
            self.addLink(router, attachmentSwitches[i - 1])

            # learning switch sitting in each customer AS
            # you may need 'sudo apt-get install bridge-utils' for this:
            sw = self.addSwitch('tn_s%s' % i, cls=LinuxBridge)

            for j in range(1, nHosts + 1):
                host = self.addHost('tn_h%s%s' % (i, j), cls=SdnIpHost, ip='10.%s.%s.%s/24' % (domID, i, j),
                                    route='10.%s.%s.254' % (domID, i))
                self.addLink(host, sw)

            self.addLink(router, sw)

        self.addLink(bgp, tn_pc1)

        # Connect BGP speaker to the root namespace
        root = self.addHost('root', inNamespace=False, ip='10.10.10.2/24')
        self.addLink(root, bgp)



        # Wire up the switches in the topology
        self.addLink(tn_pe1, tn_pc1)
        self.addLink(tn_pc1, tn_pe2)
        self.addLink(tn_pe2, tn_gw_ts)


# [PZ] perhaps we will add s4 later; now we want to avoid loop problems
#        self.addLink( s1, s4 )
#        self.addLink( s4, s3 )






class MDCoCoTopoSouth(Topo):
    # TODO : call MDCoCoTopo with domain name parameter
    # we were not successful with
    # def __init__(self, domainname):
    #    Topo.__init__(self, domainname)
    #    self.domainname = domainname

    "Multidomain CoCo topology - TNO South"

    def build(self):
        domID = 3

        ts_pe1 = self.addSwitch('ts_pe1', dpid='0000000000000001', datapath='user')
        #        ts_pc1 = self.addSwitch('ts_pc1', dpid='0000000000000002')
        #        ts_pe2 = self.addSwitch('ts_pe2', dpid='0000000000000003')
        ts_gw_tn = self.addSwitch('ts_gw_tn', dpid='0000000000000024')

        pinghost = self.addHost('ts_ph_tn', cls=PingableHost, ip='10.0.0.4/24', mac='00:10:00:00:00:04',
                                remoteIP='10.0.0.3', remoteMAC='00:10:00:00:00:03')
        self.addLink(ts_pe1, pinghost)

        zebraConf = '%s/zebra.conf' % CONFIG_DIR

        # Switches we want to attach our routers to, in the correct order
        attachmentSwitches = [ts_pe1]
        nRouters = 1
        nHosts = 2

        # Set up the internal BGP speaker
        bgpEth0 = {'mac': '00:10:0%s:00:02:54' % domID,
                   'ipAddrs': '10.%s.0.254/24' % domID}
        bgpEth1 = {'ipAddrs': '10.10.10.1/24'}
        bgpIntfs = {'ts_bgp1-eth0': bgpEth0,
                    'ts_bgp1-eth1': bgpEth1}

        tn_bgp1 = {'remoteMAC': '00:10:02:00:02:54',
                   'remoteIP': '10.2.0.254',
                   'remoteMask': '/32',
                   'localdev': 'ts_bgp1-eth0'}

        # TODO sould be generated!!
        ts_ce1_arp = {'remoteMAC': '00:10:03:00:00:01',
                      'remoteIP': '10.3.0.1',
                      'remoteMask': '/32',
                      'localdev': 'ts_bgp1-eth0'}

        ARPBGPpeers = {'tn_bgp1': tn_bgp1,
                       'ts_ce1': ts_ce1_arp}

#        bgp = self.addHost("ts_bgp1", cls=QBGPRouter,
#                           quaggaConfFile='%s/ts_bgp1.conf' % CONFIG_DIR,
#                           zebraConfFile=zebraConf,
#                           intfDict=bgpIntfs,
#                           ARPDict=ARPBGPpeers)

        bgp = self.addHost("ts_bgp1", cls=EXABGPRouteReflector,
                           exabgpIniFile='%s/exabgp_config.ini' % CONFIG_DIR,
                           exabgpConfFile='%s/exabgp_ts2_simplehttp.conf' % CONFIG_DIR,
                           intfDict=bgpIntfs,
                           ARPDict=ARPBGPpeers)

        for i in range(1, nRouters + 1):
            name = 'ts_ce%s' % i

            # drop vlans
            #            eth0 = { 'mac' : '00:10:0%s:00:00:0%s' % (domID, i),
            #                     'ipAddrs' : '10.%s.0.%s/24' % (domID, i),
            #                     'vlan' : '%s%s' % (domID, i)}
            eth0 = {'mac': '00:10:0%s:00:00:0%s' % (domID, i),
                    'ipAddrs': '10.%s.0.%s/24' % (domID, i)}

            eth1 = {'mac': '00:10:0%s:0%s:02:54' % (domID, i),
                    'ipAddrs': '10.%s.%s.254/24' % (domID, i)}

            intfs = {'%s-eth0' % name: eth0,
                     '%s-eth1' % name: eth1}

            bgpgw = {'remoteMAC': bgpEth0['mac'],
                     'remoteIP': '10.0.0.0',
                     'remoteMask': '/8',
                     'nexthop': bgpEth0['ipAddrs'].split('/')[0]}  # only ip, get rid of mask

            ARPfakegw = {'bgpgw': bgpgw}

            quaggaConf = '%s/ts_ce%s.conf' % (CONFIG_DIR, i)

            router = self.addHost(name, cls=QBGPRouter, quaggaConfFile=quaggaConf,
                                  zebraConfFile=zebraConf, intfDict=intfs, ARPDict=ARPfakegw)
            self.addLink(router, attachmentSwitches[i - 1])

            # learning switch sitting in each customer AS
            # you may need sudo apt-get install bridge-utils for this:
            sw = self.addSwitch('ts_s%s' % i, cls=LinuxBridge)

            for j in range(1, nHosts + 1):
                host = self.addHost('ts_h%s%s' % (i, j), cls=SdnIpHost, ip='10.%s.%s.%s/24' % (domID, i, j),
                                    route='10.%s.%s.254' % (domID, i))
                self.addLink(host, sw)

            self.addLink(router, sw)

        self.addLink(bgp, ts_pe1)

        # Connect BGP speaker to the root namespace
        root = self.addHost('root', inNamespace=False, ip='10.10.10.2/24')
        self.addLink(root, bgp)



        # Wire up the switches in the topology
        ##for a moment only one switch is present in TNO south
        self.addLink(ts_pe1, ts_gw_tn)

        # self.addLink( ts_pc1, ts_pe2 )


def returnSwitchConnections(mn_topo, switches, operSwNames):
    "Dump connections to/from nodes."

    def returnConnections(sw, swID):
        "Helper function: dump connections to node"
        global mplsVal
        switchtable = []
        # switchtable.append(int(sw.name.split('s')[1]))
        switchtable.append(int(swID))
        # expacted name has form openflow:<num> - this is form which
        # is retrieved form openflow
        # we cannot therefore use switchtable.append(sw.name)

        switchtable.append('openflow:' + str(int(sw.dpid,16))) #get hex switch id and convert it to dec string
        switchtable.append(sw.name) #
        switchtable.append(0)
        switchtable.append(0)
        switchtable.append(0)
        return (switchtable)

    # mn_topo.ports[sw.name].values() stores neighbours (and their ports),e.g.
    # [('s3', 2), ('s2', 2)]
    # check if there is a host on the list
    # print sw.name
    # print mn_topo.ports[sw.name].values()
    # neighs=zip(*mn_topo.ports[sw.name].values())[0] #('h2', 's1', 'h1')
    # if any('h' in s for s in neighs):
    #    mplsVal+=1
    #    switchtable.append(mplsVal)
    # else: #core switch
    #    switchtable.append(0)


    bigswitchtable = []
#    for swID, sw in enumerate(switches):
#        bigswitchtable.append(returnConnections(sw, swID + 1))
    for sw in switches:
        if sw.name in operSwNames:
            swID=operSwNames.index(sw.name)
            bigswitchtable.append(returnConnections(sw, swID + 1))

    return bigswitchtable


def returnNodeConnections(nodes, operSwNames,cocoSiteNames):
    "Dump connections to/from nodes."

    def returnConnections(node, operSwNames):
        "Helper function: dump connections to node"
        hosttable=['', 0, 0, 0, 0, 0, 0, '', '']
        #0:name, 1:x, 2:y, 3:switch, 4:remote_port, 5:local_port, 6:vlanid, 7:ipv4prefix, 8:mac_address

        # TODO we assume host/site has only one link pointing to the core (no multihome)
        # TODO ugly workaround: if we have a router not a host, part of information (switch, remote port)
        # TODO comes from iface pointing to the core and part (ip prefix) comes from iface pointing to LAN
        # TODO We should migrate to pandas dataframe probably
        # note mn_topo.ports cannot be used as it does not return subifs
        for locintf in node.intfList():

            if locintf.link:
                intfs = [locintf.link.intf1, locintf.link.intf2]
                intfs.remove(locintf) #infts will store only far end now
                swNamePort = intfs[0].name  # has form like s2-eth3
                swNamePort=swNamePort.split('-eth') # ['s2','3'] as we get rid of eth
                swName=swNamePort[0]
                hosttable[0]=node.name

                if swName in operSwNames: #interface pointing to WAN
                # TODO it may happen that two hosts are connected back-to-back, then far end is not a switch
                    hosttable[3]=operSwNames.index(swName) + 1
                    hosttable[4]=int(swNamePort[1]) #number after eth
                    hosttable[8]=locintf.mac

                else: #interface pointing to LAN
                    # no vlans
                    # subint = locintf.name.split('eth')[1].split('.') # split 0.1001 into 0 and 1001
                    # hosttable.append(int(subint[0]))
                    # hosttable.append(int(subint[1]))
                    locintnum = locintf.name.split('-eth')[1]
                    hosttable[5]=int(locintnum)
                    # fake vlan 0
                    hostnet = IPNetwork(locintf.ip + '/' + locintf.prefixLen)
                    hosttable[7]=str(hostnet.cidr)

            else:
                sys.exit('host has no links')
        return (hosttable)


    bighosttable = []

    for node in nodes:
        if node.name in cocoSiteNames:
            conn=returnConnections(node, operSwNames)
            if conn: #may be empty in conrer cases like 2 hosts connected to each other
                bighosttable.append(conn)
    return bighosttable

def operswitch(element): #to pick only operator's switches
        return ('_pc' in element) | ('_pe' in element) # no gateway switch needed in db| ('_gw' in element)

def cocosite(element): #to pick only actual coco sites
        return ('_ce' in element)

def databaseDump(net, domain):

    operSwNames = [sw.name for i, sw in enumerate(net.switches)]
    operSwNames  = filter(operswitch, operSwNames) #get rid of switches in customer domains

    cocoSiteNames = [h.name for i, h in enumerate(net.hosts)]
    cocoSiteNames = filter(cocosite, cocoSiteNames) #get rid of all hosts not being actual coco sites (hosts/ce-routers)




    db = mdb.connect(DB_HOST, DB_USER, DB_PWD, DB_NAME)
    cursor = db.cursor()
    cursor.execute('SET FOREIGN_KEY_CHECKS=0;')

    ###############   switches - create first - otherwise sites cannot be created as they reference to
    # the key present here (errno 150)

    # Drop table if it already exist using execute() method.
    cursor.execute('DROP TABLE IF EXISTS `switches`;')
    sql = """CREATE TABLE `switches` (`id` int(11) NOT NULL,  `name` varchar(45) NOT NULL, `mininetname` varchar(45) NOT NULL,  `x` int(10) unsigned NOT NULL,  `y` int(10) unsigned NOT NULL,  `mpls_label` int(10) unsigned NOT NULL ,  PRIMARY KEY (`id`,`name`),  UNIQUE KEY `id_UNIQUE` (`id`),  UNIQUE KEY `name_UNIQUE` (`name`)  ) ENGINE=InnoDB DEFAULT CHARSET=latin1;"""
    cursor.execute(sql)

    bigswitchtable = returnSwitchConnections(net, net.switches, operSwNames)

    for trow in range(len(bigswitchtable)):
        # Prepare SQL query to INSERT a record into the database.
        crow = bigswitchtable[trow]
        sql = """INSERT INTO `switches` (id, name, mininetname, x, y, mpls_label) VALUES ('%d', '%s', '%s', '%d', '%d', '%d' )""" % (
            crow[0], crow[1], crow[2], crow[3], crow[4], crow[5])

        try:
            # Execute the SQL command
            cursor.execute(sql)
            # Commit your changes in the database
            db.commit()
        except:
            # Rollback in case there is any error
            db.rollback()



    ###############   sites

    # Drop table if it already exist using execute() method.
    cursor.execute('DROP TABLE IF EXISTS sites')

    sql = """CREATE TABLE `sites` (  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,  `name` varchar(45) NOT NULL,  `x` int(11) NOT NULL,  `y` int(11) NOT NULL,  `switch` int(11) NOT NULL,  `remote_port` int(10) unsigned NOT NULL,  `local_port` int(10) unsigned NOT NULL,  `vlanid` int(10) unsigned NOT NULL,  `ipv4prefix` varchar(45) NOT NULL,  `mac_address` varchar(45) NOT NULL,  PRIMARY KEY (`id`),  UNIQUE KEY `id_UNIQUE` (`id`),  UNIQUE KEY `name_UNIQUE` (`name`),  KEY `switch_idx` (`switch`),  CONSTRAINT `switch_id` FOREIGN KEY (`switch`) REFERENCES `switches` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION) ENGINE=InnoDB DEFAULT CHARSET=latin1;"""

    cursor.execute(sql)

    bighosttable = returnNodeConnections(net.hosts, operSwNames, cocoSiteNames)
    for trow in range(len(bighosttable)):
        # Prepare SQL query to INSERT a record into the database.
        crow = bighosttable[trow]
        sql = """INSERT INTO sites (name, x, y, switch, remote_port, local_port, vlanid, ipv4prefix, mac_address)  VALUES ('%s', '%d', '%d', '%d', '%d', '%d', '%d', '%s', '%s' )""" % (
            crow[0], crow[1], crow[2], crow[3], crow[4], crow[5], crow[6], crow[7], crow[8])

        try:
            # Execute the SQL command
            cursor.execute(sql)
            # Commit your changes in the database
            db.commit()
        except:
            # Rollback in case there is any error
            db.rollback()


    ############### vpns

    cursor.execute('DROP TABLE IF EXISTS vpns;')

    sql = """CREATE TABLE `vpns` (  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,  `name` varchar(45) NOT NULL,  `pathProtection` varchar(45) DEFAULT NULL,  `failoverType` varchar(45) DEFAULT NULL,  `isPublic` tinyint(1) NOT NULL,  PRIMARY KEY (`id`),  UNIQUE KEY `id_UNIQUE` (`id`),  UNIQUE KEY `name_UNIQUE` (`name`)) ENGINE=InnoDB DEFAULT CHARSET=latin1;"""
    cursor.execute(sql)


    ############## site2vpn
    cursor.execute('DROP TABLE IF EXISTS `site2vpn`;')
    sql = """CREATE TABLE `site2vpn` (	  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,	  `vpnid` int(10) unsigned DEFAULT NULL,	  `siteid` int(10) unsigned DEFAULT NULL,	  PRIMARY KEY (`id`),	  KEY `site_idx` (`siteid`),	  KEY `vpn_idx` (`vpnid`),	  CONSTRAINT `site` FOREIGN KEY (`siteid`) REFERENCES `sites` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,	  CONSTRAINT `vpn` FOREIGN KEY (`vpnid`) REFERENCES `vpns` (`id`) ON DELETE CASCADE ON UPDATE CASCADE	) ENGINE=InnoDB DEFAULT CHARSET=latin1;"""
    cursor.execute(sql)


    ############ links
    cursor.execute('DROP TABLE IF EXISTS `links`;')
    sql = """CREATE TABLE `links` (  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,  `from` int(11) NOT NULL,  `to` int(11) NOT NULL,  PRIMARY KEY (`id`),  UNIQUE KEY `id_UNIQUE` (`id`),  KEY `from_idx` (`from`),  KEY `to_idx` (`to`),  CONSTRAINT `from` FOREIGN KEY (`from`) REFERENCES `switches` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,  CONSTRAINT `to` FOREIGN KEY (`to`) REFERENCES `switches` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION) ENGINE=InnoDB  DEFAULT CHARSET=latin1;"""
    cursor.execute(sql)

    # TODO actually we should query DB for IDs!!

    for currLink in net.links:
        currFrom = currLink.intf1.name.split('-eth')[0]  # get, say s1 from s1-eth1
        currTo = currLink.intf2.name.split('-eth')[0]

        if (currFrom in cocoSiteNames) | (currTo in cocoSiteNames):  # switch-to-host is not interesting
            continue
        if (currFrom not in operSwNames) | (currTo not in operSwNames):  # we are only interested in connections between oper switches
            continue

        currFromID = operSwNames.index(currFrom) + 1
        currToID = operSwNames.index(currTo) + 1
        # we REALLY  need backquotes here, otherwise from is interpreted as sql keyword
        sql = """INSERT INTO `links` (`from`, `to`) VALUES ('%d', '%d')""" % (currFromID, currToID)

        try:
            # Execute the SQL command
            cursor.execute(sql)
            # Commit your changes in the database
            db.commit()
        except:
            # Rollback in case there is any error
            db.rollback()


    ############ sitelinks
    cursor.execute('DROP TABLE IF EXISTS `sitelinks`;')
    sql = """CREATE TABLE `sitelinks` (  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,  `site` int(10) unsigned NOT NULL,  `switch` int(11) NOT NULL,  PRIMARY KEY (`id`),  UNIQUE KEY `id_UNIQUE` (`id`),  KEY `site_idx` (`site`),  KEY `switch_idx` (`switch`),  CONSTRAINT `from_site` FOREIGN KEY (`site`) REFERENCES `sites` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,  CONSTRAINT `to_switch` FOREIGN KEY (`switch`) REFERENCES `switches` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION) ENGINE=InnoDB DEFAULT CHARSET=latin1;"""
    cursor.execute(sql)

    # TODO actually we should query DB for IDs!!

    for currLink in net.links:
        currFrom = currLink.intf1.name.split('-eth')[0]  # get, say s1 from s1-eth1
        currTo = currLink.intf2.name.split('-eth')[0]

        if (currFrom in cocoSiteNames) & (currTo in operSwNames):
            currSiteID = cocoSiteNames.index(currFrom) + 1
            currSwitchID = operSwNames.index(currTo) + 1
        elif (currTo in cocoSiteNames) & (currFrom in operSwNames):
            currSiteID = cocoSiteNames.index(currTo) + 1
            currSwitchID = operSwNames.index(currFrom) + 1
        else:  # switch to switch connection - not interesting
            continue

        # we REALLY  need backquotes here, otherwise from is interpreted as sql keyword
        sql = """INSERT INTO `sitelinks` (`site`, `switch`) VALUES ('%d', '%d')""" % (currSiteID, currSwitchID)

        try:
            # Execute the SQL command
            cursor.execute(sql)
            # Commit your changes in the database
            db.commit()
        except:
            # Rollback in case there is any error
            db.rollback()



    ############ ASes
    cursor.execute('DROP TABLE IF EXISTS `ases`;')
    sql = """CREATE TABLE `ases` (  `id` int(10) unsigned NOT NULL,  `bgp_ip` varchar(45) DEFAULT NULL,  `as_num` int(11) DEFAULT NULL,  `as_name` varchar(45) DEFAULT NULL,  PRIMARY KEY (`id`)) ENGINE=InnoDB DEFAULT CHARSET=latin1;"""
    cursor.execute(sql)

    #TODO read from topology (can we do it?)
    sql = """INSERT INTO `ases` VALUES (1,'10.2.0.254',65020,'tno-north'),(2,'10.3.0.254',65030,'tno-south');"""
    try:
        # Execute the SQL command
        cursor.execute(sql)
        # Commit your changes in the database
        db.commit()
    except:
        # Rollback in case there is any error
        db.rollback()



    #################`ext_links`


    cursor.execute('DROP TABLE IF EXISTS `ext_links`;')
    sql = """CREATE TABLE `ext_links` (  `id` int(11) NOT NULL AUTO_INCREMENT,  `switch` int(11) DEFAULT NULL,  `as` int(10) unsigned DEFAULT NULL,  PRIMARY KEY (`id`),  KEY `switch_idx` (`switch`),  KEY `as_idx` (`as`),  CONSTRAINT `as_fk` FOREIGN KEY (`as`) REFERENCES `ases` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,  CONSTRAINT `switch_fk` FOREIGN KEY (`switch`) REFERENCES `switches` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION) ENGINE=InnoDB DEFAULT CHARSET=latin1;"""
    cursor.execute(sql)

    if domain == 'MDCoCoTopoNorth':
        gwSwitchID = operSwNames.index('tn_pe2') + 1
        sql = """SELECT `id` FROM %s.ases WHERE  `as_name` LIKE 'tno-south';""" % DB_NAME

    if domain == 'MDCoCoTopoSouth':
        gwSwitchID = operSwNames.index('ts_pe1') + 1
        sql = """SELECT `id` FROM %s.ases WHERE  `as_name` LIKE 'tno-north';""" % DB_NAME

    cursor.execute(sql)
    for remoteASID in cursor:
        #TODO find better way to access cursor content; now we assume there is only one entry
        #print(remoteASID)

        sql = """INSERT INTO `ext_links` (`switch`, `as`) VALUES ('%d', '%d')""" % (gwSwitchID, int(remoteASID[0]))

        try:
            # Execute the SQL command
            cursor.execute(sql)
            # Commit your changes in the database
            db.commit()
        except:
            # Rollback in case there is any error
            db.rollback()


    ##database processing ends
    cursor.execute('SET FOREIGN_KEY_CHECKS=1;')
    db.close()


topos = {'tnonorth': MDCoCoTopoNorth,
         'tnosouth': MDCoCoTopoSouth}


if __name__ == '__main__':
    #TODO topo name as argument
    #setLogLevel('debug')
    #topo = MDCoCoTopoNorth()
    #topo = MDCoCoTopoSouth()
    if sys.argv[1]=='tn':
        topo = MDCoCoTopoNorth()
    if sys.argv[1]=='ts':
        topo = MDCoCoTopoSouth()
    else:
        print('using default topo tn')
        topo = MDCoCoTopoNorth()


    net = Mininet(topo=topo, controller=RemoteController)
    # hp = net.hosts[-1]
    # info(hp)
    # aww how ugly
    # if hp.name[0:2] == 'ts':
    #    hp.setARP('10.0.0.3', '00:10:00:00:00:03')
    # if hp.name[0:2] == 'tn':
    #    hp.setARP('10.0.0.4', '00:10:00:00:00:04')

    net.start()
    databaseDump(net,type(topo).__name__) #nort or south?
    # hp.cmd('arp -s 10.0.0.4 00:10:00:00:00:04')

    CLI(net)

    net.stop()

    info("done\n")

# topos = { 'tnonorth': ( lambda: MDCoCoTopoNorth() ),
#          'tnosouth': ( lambda: MDCoCoTopoSouth() ) }

# topos = { 'cocotopo': ( lambda: CoCoTopo() ) }
