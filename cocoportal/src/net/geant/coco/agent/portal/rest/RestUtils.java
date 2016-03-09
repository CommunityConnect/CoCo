package net.geant.coco.agent.portal.rest;

import java.util.HashMap;
import java.util.Map;

public class RestUtils {

	// Map to store vpns, ideally we should use database
	public Map<Integer, RestVpn> restVpnData = new HashMap<Integer, RestVpn>();

	// Map to store sites, ideally we should use database
	public Map<Integer, RestSite> restSiteData = new HashMap<Integer, RestSite>();
	
	private RestSite getRestSiteByName(String siteName) {
		for (RestSite restSite : restSiteData.values()) {
			if (restSite.getName().equalsIgnoreCase(siteName)) {
				return restSite;
			}
		}
		// FIXME something can go very wrong here
		return null;
	}

	private RestVpn getRestVpnByName(String vpnName) {
		for (RestVpn restVpn : restVpnData.values()) {
			if (restVpn.getName().equalsIgnoreCase(vpnName)) {
				return restVpn;
			}
		}
		// FIXME something can go very wrong here
		return null;
	}

	
	public void restAddSiteToVpn(String vpnName, String addSiteName) {
		RestSite restSiteToAdd = getRestSiteByName(addSiteName);
		RestVpn restVpn = getRestVpnByName(vpnName);
		restVpn.addSiteToVpn(restSiteToAdd);
	}

	public void restDeleteSiteFromVpn(String vpnName, String deleteSiteName) {
		RestSite restSiteToDelete = getRestSiteByName(deleteSiteName);
		RestVpn restVpn = getRestVpnByName(vpnName);
		restVpn.deleteSiteFromVpn(restSiteToDelete);
	}
}
