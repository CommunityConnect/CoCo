package net.geant.coco.agent.portal.bgp;

import java.util.List;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BgpRouterDummy implements BgpRouterInterface {

	
	public BgpRouterDummy() {
		log.info("BgpRouterDummy create");
		
	}

	
	@Override
	public void addPeer(String ipAddress, int asNumber) {
		log.info("addPeer " + ipAddress + " " +asNumber);
		
	}

	@Override
	public void addSiteToVpn(String prefix, String neighborIpAddress, int vpnNum) {
		log.info("addVpn " + prefix + " " + vpnNum);
		
	}

	@Override
	public void delSiteFromVpn(String prefix, String neighborIpAddress, int vpnNum) {
		log.info("delVpn " + prefix + " " + vpnNum);
		
	}

	@Override
	public List<BgpRouteEntry> getVpns() {
		log.info("getVpns");
		return null;
	}

	@Override
	public String getRouteTarget(String prefix) {
		log.info("getRouteTarget " + prefix);
		return null;
	}

}
