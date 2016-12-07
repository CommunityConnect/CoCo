package net.geant.coco.agent.portal.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.geant.coco.agent.portal.dao.Bgp;
import net.geant.coco.agent.portal.dao.NetworkSite;
import net.geant.coco.agent.portal.dao.NetworkSiteDao;
import net.geant.coco.agent.portal.dao.Vpn;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("networkSitesService")
public class NetworkSitesService {
    private NetworkSiteDao networkSiteDao;

    @Autowired
    public void setNetworkSiteDao(NetworkSiteDao networkSiteDao) {
        this.networkSiteDao = networkSiteDao;
    }

    public Map<String, NetworkSite> getNetworkSites() {
        List<NetworkSite> networkSiteList = networkSiteDao.getNetworkSites();
        
        Map<String, NetworkSite> networkSiteToNameMap = new HashMap<>();
        for (NetworkSite networkSite : networkSiteList) {
        	networkSiteToNameMap.put(networkSite.getName(), networkSite);
		}
        
		return networkSiteToNameMap;	
    }
    
    public List<NetworkSite> getNetworkSites(String vpnName) {
        return networkSiteDao.getNetworkSites(vpnName);
    }
    
    public NetworkSite getNetworkSite(String siteName) {
        return networkSiteDao.getNetworkSite(siteName);
    }
    
    public NetworkSite getExternalNetworkSite(Bgp bgp, Vpn vpn) {
        return networkSiteDao.getExternalNetworkSite(bgp, vpn);
    }
    
    public int insertNetworkSite(String name, int switchNumber, int remotePort, int localPort, int vlanId, String ipPrefix, String macAddress) {
    	return networkSiteDao.insertNetworkSite(name, switchNumber, remotePort, localPort, vlanId, ipPrefix, macAddress);
    }

	public int deleteNetworkSite(String ipPrefix) {
		return networkSiteDao.deleteNetworkSite(ipPrefix);
	}

	public int insertNetworkSite(String prefix, int vlanId, String neighborIp) {
		return networkSiteDao.insertNetworkSite(prefix, vlanId, neighborIp);
	}
}
