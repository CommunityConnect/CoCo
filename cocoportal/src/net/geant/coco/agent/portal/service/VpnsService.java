package net.geant.coco.agent.portal.service;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import lombok.extern.slf4j.Slf4j;
import net.geant.coco.agent.portal.dao.NetworkSite;
import net.geant.coco.agent.portal.dao.NetworkSiteDao;
import net.geant.coco.agent.portal.dao.Vpn;
import net.geant.coco.agent.portal.dao.VpnDao;
import net.geant.coco.agent.portal.utils.VpnProvisioner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Slf4j
@Service("vpnsService")
@Configuration
@PropertySource("classpath:/net/geant/coco/agent/portal/props/config.properties")
public class VpnsService {
    private VpnDao vpnDao;
    private NetworkSiteDao networkSiteDao;
    
    VpnProvisioner vpnProvisioner;
    
    @Autowired
	Environment env;
	
	@PostConstruct
	public void setVpnProvisioner() {
	    log.info("setVpnProvisioner");  // Displays as expected
	    String controllerUrl = env.getProperty("controller.url");
		vpnProvisioner = new VpnProvisioner(controllerUrl);
	}
	
    @Autowired
    public void setVpnDao(VpnDao vpnDao) {
        this.vpnDao = vpnDao;
    }
    
    @Autowired
    public void setNetworkSiteDao(NetworkSiteDao networkSiteDao) {
        this.networkSiteDao = networkSiteDao;
    }

    public List<Vpn> getVpns() {
    	List<Vpn> vpns = vpnDao.getVpns();
    	
    	for (Vpn vpn : vpns) {
			List<NetworkSite> networkSitesFromVpn = networkSiteDao.getNetworkSites(vpn.getName());
			vpn.setSites(networkSitesFromVpn);
		}
    	
        return vpns;
    }

    public Vpn getVpn(String vpnName) {
    	Vpn vpn = vpnDao.getVpn(vpnName);
    	
    	List<NetworkSite> networkSitesFromVpn = networkSiteDao.getNetworkSites(vpn.getName());
		vpn.setSites(networkSitesFromVpn);
		
		return vpn;
    }
    
    public Vpn getVpn(int vpnId) {
    	Vpn vpn = vpnDao.getVpn(vpnId);
    	
    	List<NetworkSite> networkSitesFromVpn = networkSiteDao.getNetworkSites(vpn.getName());
		vpn.setSites(networkSitesFromVpn);
		
		return vpn;
    }

    public boolean createVpn(Vpn vpn) {
    	log.info("createVpn " + vpn.toString());
    	vpnProvisioner.createVpn(vpn.getName(), vpn.getPathProtectionBoolean(), vpn.getFailoverType());
    	
        return vpnDao.createVpn(vpn);
    }

    public boolean addSiteToVpn(String vpnName, String siteName) {
        return vpnDao.addSiteToVpn(vpnName, siteName);
    }

    public boolean deleteSiteFromVpn(String vpnName, String siteName) {
        return vpnDao.deleteSiteFromVpn(vpnName, siteName);
    }

	public boolean deleteVpn(int vpnId) {
		log.info("deleteVpn " + vpnId);
		
		
		Vpn vpn = getVpn(vpnId);
		// TODO remove sites from VPN here?
		vpnProvisioner.deleteVpn(vpn.getName());
		
		return vpnDao.deleteVpn(vpnId);
	}

	public List<NetworkSite> getSitesInVpn(int vpnID) {
		Vpn vpn = vpnDao.getVpn(vpnID);
		
		if (vpn == null) {
			return new ArrayList<NetworkSite>();
		}

		return networkSiteDao.getNetworkSites(vpn.getName());
	}
}
