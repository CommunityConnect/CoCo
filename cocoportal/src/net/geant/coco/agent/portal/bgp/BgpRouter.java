package net.geant.coco.agent.portal.bgp;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;

public class BgpRouter {

	private String ipAddress;
	private int port;
		
	public BgpRouter(String ipAddress, int port) {
		super();
		this.ipAddress = ipAddress;
		this.port = port;
	}

	public void addPeer(String ipAddress, int asNumber)
	{
		
		try {
		      TTransport transport;
		      transport = new TSocket(this.ipAddress, this.port);
		      transport.open();

		      TProtocol protocol = new  TBinaryProtocol(transport);
		      BgpConfigurator.Client client = new BgpConfigurator.Client(protocol);

		      client.createPeer(ipAddress, asNumber);

		      transport.close();
		    } catch (TException x) {
		      x.printStackTrace();
		    } 
		
	}
	
	public void addVpn(String prefix, String neighborIpAddress, int vpnNum)
	{
		try {
		      TTransport transport;
		      transport = new TSocket(this.ipAddress, this.port);
		      transport.open();
		      

		      TProtocol protocol = new  TBinaryProtocol(transport);
		      BgpConfigurator.Client client = new BgpConfigurator.Client(protocol);

		      // aclNum and seqNum are the same (per site)
		      // routeMapNum are per neighbor
		      client.pushRoute(prefix, vpnNum, neighborIpAddress);

		      transport.close();
		    } catch (TException x) {
		      x.printStackTrace();
		    } 
		
	}
	
	public void delVpn(String prefix, String neighborIpAddress, int vpnNum) {

		try {
			TTransport transport;
			transport = new TSocket(this.ipAddress, this.port);
			transport.open();

			TProtocol protocol = new TBinaryProtocol(transport);
			BgpConfigurator.Client client = new BgpConfigurator.Client(protocol);

			client.withdrawRoute(prefix, vpnNum, neighborIpAddress);
			
			transport.close();
		} catch (TException x) {
			x.printStackTrace();
		}

	}
	
	public List<BgpRouteEntry> getVpns()
	{
		try {
			TTransport transport;
			transport = new TSocket(this.ipAddress, this.port);
			transport.open();

			TProtocol protocol = new TBinaryProtocol(transport);
			BgpConfigurator.Client client = new BgpConfigurator.Client(protocol);

			Routes routes = client.getRoutes(1, 1);
			List<Update> updates = routes.updates;

			List<BgpRouteEntry> retRoutes = new ArrayList<BgpRouteEntry>();
			
			Iterator<Update> it = updates.iterator();
			while(it.hasNext())
			{
				Update update = it.next();
				String prefixWithLength = update.prefix + "/" + update.prefixlen;
				// TODO update Thrift interface so that route target is int not string
				String routeTargetString = client.getRouteTarget(prefixWithLength);
				
				int routeTargetInt = -1;
				if (routeTargetString.equals("")) {
					routeTargetInt = -1;
				}
				else if (routeTargetString.contains("work")) {
					routeTargetInt = -2;
				}
				else {
					routeTargetInt = Integer.valueOf(routeTargetString);
				}
				
				retRoutes.add(new BgpRouteEntry(update.prefix + "/" + update.prefixlen, update.rd, update.nexthop, update.label, routeTargetInt));

				
			}
			

			transport.close();
			return retRoutes;
		} catch (TException x) {
			x.printStackTrace();
		}
		return null;
	}
	
	public String getRouteTarget(String prefix)
	{
		String routeTarget = "";
		try {
		      TTransport transport;
		      transport = new TSocket(this.ipAddress, this.port);
		      transport.open();
		      

		      TProtocol protocol = new  TBinaryProtocol(transport);
		      BgpConfigurator.Client client = new BgpConfigurator.Client(protocol);

		      routeTarget = client.getRouteTarget(prefix);

		      transport.close();
		    } catch (TException x) {
		      x.printStackTrace();
		    } 
		
		return routeTarget;
		
	}
	
	
}
