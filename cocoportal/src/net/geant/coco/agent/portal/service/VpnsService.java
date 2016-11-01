package net.geant.coco.agent.portal.service;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import lombok.extern.slf4j.Slf4j;
import net.geant.coco.agent.portal.bgp.BgpRouter;
import net.geant.coco.agent.portal.bgp.BgpRouterFactory;
import net.geant.coco.agent.portal.bgp.BgpRouterInterface;
import net.geant.coco.agent.portal.dao.Domain;
import net.geant.coco.agent.portal.dao.DomainDao;
import net.geant.coco.agent.portal.dao.NetworkSite;
import net.geant.coco.agent.portal.dao.NetworkSiteDao;
import net.geant.coco.agent.portal.dao.User;
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
    private DomainDao domainDao;
    
    VpnProvisioner vpnProvisioner;
    
    @Autowired
	Environment env;
	
    BgpRouterInterface bgpRouter;
    
	@PostConstruct
	public void setVpnProvisioner() {
	    log.info("setVpnProvisioner");  // Displays as expected
	    String controllerUrl = env.getProperty("controller.url");
		vpnProvisioner = new VpnProvisioner(controllerUrl);
	}
	
	@PostConstruct
	public void setBgpRouter() {
	    log.info("setBgpRouter");  // Displays as expected
	    String bgpIp = env.getProperty("ip");
	    bgpRouter = BgpRouterFactory.create(bgpIp, 7644);
	}
	
	@Autowired
    public void setDomainDao(DomainDao domainDao) {
        this.domainDao = domainDao;
    }
	
    @Autowired
    public void setVpnDao(VpnDao vpnDao) {
        this.vpnDao = vpnDao;
    }
    
    @Autowired
    public void setNetworkSiteDao(NetworkSiteDao networkSiteDao) {
        this.networkSiteDao = networkSiteDao;
    }
    
    private void manage_vpn_fk(List<Vpn> vpns){
    	for (Vpn vpn : vpns) {
    		this.manage_vpn_fk(vpn);
    	}
    }
    
    private void manage_vpn_fk(Vpn vpn){
    	List<NetworkSite> networkSitesFromVpn = networkSiteDao.getNetworkSites(vpn.getName());
		vpn.setSites(networkSitesFromVpn);
		
		Domain domain = domainDao.getDomain(vpn.getDomain_id());
		if (domain != null && domain instanceof Domain){
			vpn.setDomain(domain);
		}
    }
    
    public List<Vpn> getVpns(User user) {
    	List<Vpn> vpns = vpnDao.getVpns(user.getId());
    	
    	this.manage_vpn_fk(vpns);
    	
        return vpns;
    }

    public List<Vpn> getVpns() {
    	List<Vpn> vpns = vpnDao.getVpns();
    	
    	this.manage_vpn_fk(vpns);
    	
        return vpns;
    }

    public Vpn getVpn(String vpnName) {
    	Vpn vpn = vpnDao.getVpn(vpnName);
    	
    	this.manage_vpn_fk(vpn);
		
		return vpn;
    }
    
    public Vpn getVpn(int vpnId) {
    	Vpn vpn = vpnDao.getVpn(vpnId);
    	
    	this.manage_vpn_fk(vpn);
		
		return vpn;
    }

    public boolean createVpn(Vpn vpn) {
    	log.info("createVpn " + vpn.toString());
    	// TODO: fix VPN provisioner
    	vpnProvisioner.createVpn(vpn.getName(), vpn.getPathProtectionBoolean(), vpn.getFailoverType());
    	
        return vpnDao.createVpn(vpn);
    }
    
    @Deprecated // we want a user ID here 
    public boolean addSiteToVpn(String vpnName, String siteName) {
    	return this.addSiteToVpn(vpnName, siteName, -1);
    }

    public boolean addSiteToVpn(String vpnName, String siteName, int userID) {
    	log.info("addSiteToVpn - vpn: " + vpnName + " site: " + siteName);
    	
    	NetworkSite site = networkSiteDao.getNetworkSite(siteName);
    	Vpn currentVpn = vpnDao.getVpn(vpnName);
    	
    	vpnProvisioner.addSite(vpnName, site.getName(), site.getIpv4Prefix(), site.getProviderSwitch() + ":" + site.getProviderPort(), site.getMacAddress());
        
    	bgpRouter.addPeer(site.getIpv4Prefix(), Integer.parseInt(env.getProperty("asNumber")));
    	
    	//TODO fix the neighbour IP address, how to handle multiple neighbours?
    	bgpRouter.addSiteToVpn(site.getIpv4Prefix(), "0.0.0.0", currentVpn.getId());
    	
    	return vpnDao.addSubnetToVpn(vpnName, siteName, userID);
    }

    public boolean deleteSiteFromVpn(String vpnName, String siteName) {
    	log.info("deleteSiteFromVpn - vpn: " + vpnName + " site: " + siteName);
    	
    	//NetworkSite site = networkSiteDao.getNetworkSite(siteName);
    	Vpn currentVpn = vpnDao.getVpn(vpnName);
    	
    	vpnProvisioner.deleteSite(vpnName, siteName);
    	
    	//TODO fix the neighbour IP address, how to handle multiple neighbours?
    	bgpRouter.delSiteFromVpn(siteName, "0.0.0.0", currentVpn.getId());

        return vpnDao.deleteSiteFromVpn(vpnName, siteName);
    }

	public boolean deleteVpn(int vpnId) {
		log.info("deleteVpn " + vpnId);
		
		Vpn vpn = getVpn(vpnId);
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
