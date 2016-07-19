package net.geant.coco.agent.portal.bgp;

import java.util.List;

public interface BgpRouterInterface {
	
	public void addPeer(String ipAddress, int asNumber);
	public void addSiteToVpn(String prefix, String neighborIpAddress, int vpnNum);
	public void delSiteFromVpn(String prefix, String neighborIpAddress, int vpnNum);
	public List<BgpRouteEntry> getVpns();
	public String getRouteTarget(String prefix);
	
}
