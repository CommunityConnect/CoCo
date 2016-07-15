package net.geant.coco.agent.portal.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;
import net.geant.coco.agent.portal.dao.NetworkSite;
import net.geant.coco.agent.portal.dao.Vpn;
import net.geant.coco.agent.portal.rest.RestVpnURIConstants;
import net.geant.coco.agent.portal.service.TopologyService;
import net.geant.coco.agent.portal.service.VpnsService;

@Slf4j
@RestController
@Configuration
@PropertySource("classpath:/net/geant/coco/agent/portal/props/config.properties")
public class PortalControllerIntent {

	@Autowired
	Environment env;

	private VpnsService vpnsService;
	private TopologyService topologyService;
	
	Map<String, NetworkSite> networkSites;
	
	boolean networkChanged = false;

	@Autowired
	public void setVpnsService(VpnsService vpnsService) {
		this.vpnsService = vpnsService;
	}

	@Autowired
	public void setTopologyService(TopologyService topologyService) {
		this.topologyService = topologyService;
	}
	
	private void networkAddSiteToVpn(String vpnName, String addSiteName) {
		log.info("networkAddSiteToVpn vpnName=" + vpnName + "siteName=" + addSiteName);
		
		vpnsService.addSiteToVpn(vpnName, addSiteName);
	}

	private void networkDeleteSiteFromVpn(String vpnName, String deleteSiteName) {
		log.info("networkAddSiteToVpn vpnName=" + vpnName + "siteName=" + deleteSiteName);
		
		vpnsService.deleteSiteFromVpn(vpnName, deleteSiteName);
	}
	
	
	@RequestMapping(value = RestVpnURIConstants.GET_TOPOLOGY_VIS, method = RequestMethod.GET)
	public String getTopologyVis() {
		log.info("getTopologyVis");
		
		return topologyService.getTopologyJsonVis();
	}

	@RequestMapping(value = RestVpnURIConstants.GET_TOPOLOGY_IS_CHANGED, method = RequestMethod.GET)
	public boolean getTopologyChange() {

		if (networkChanged == true) {
			networkChanged = false;
			return true;
		}

		return networkChanged;
	}
	
	@RequestMapping(value = RestVpnURIConstants.GET_VPN, method = RequestMethod.GET)
	public Vpn getVpn(@PathVariable("id") int vpnId) {
		log.info("getVpn ID=" + vpnId);
		return vpnsService.getVpn(vpnId);
	}

	@RequestMapping(value = RestVpnURIConstants.UPDATE_VPN, method = RequestMethod.POST)
	public Vpn updateVpn(@PathVariable("id") int vpnId, @RequestBody Vpn vpn) {
		log.info("updateVpn ID=" + vpnId);

		Assert.isTrue(vpn.getId() == vpnId, "VPN id and ID from the rest path are not the same. REST PATH:"
				+ String.valueOf(vpnId) + " VPN ID" + String.valueOf(vpn.getId()));

		Vpn vpnNew = vpn;
		Vpn vpnCurrent = vpnsService.getVpn(vpnId);

		List<NetworkSite> sitesToAdd = new ArrayList<NetworkSite>(vpnNew.getSites());
		sitesToAdd.removeAll(vpnCurrent.getSites());
		List<NetworkSite> sitesToRemove = new ArrayList<NetworkSite>(vpnCurrent.getSites());
		sitesToRemove.removeAll(vpnNew.getSites());


		for (NetworkSite site : sitesToAdd) {
			networkAddSiteToVpn(vpnCurrent.getName(), site.getName());
		}
		
		for (NetworkSite site : sitesToRemove) {
			networkDeleteSiteFromVpn(vpnCurrent.getName(), site.getName());
		}

		vpnNew = vpnsService.getVpn(vpnId);
		return vpnNew;
	}
	
	
	
//	@PostConstruct
//	@RequestMapping("/setupAll")
//	public @ResponseBody String initializeEverything() {
//		log.info("Initialize everything Intent version");
//
//		this.networkChanged = true;
//		
//		return "everything initialized succesfully";
//
//	}
	
	@RequestMapping(value = RestVpnURIConstants.GET_ALL_VPN, method = RequestMethod.GET)
	public List<Vpn> getAllVpns() {
		log.info("Start getAllVpns.");
		
		return vpnsService.getVpns();
	}

	@RequestMapping(value = RestVpnURIConstants.GET_ALL_SITES, method = RequestMethod.GET)
	public List<NetworkSite> getSitesInVpn(@PathVariable("id") int vpnID) {
		log.info("Start getall sites.");

		return vpnsService.getSitesInVpn(vpnID);
	}

	@RequestMapping(value = RestVpnURIConstants.CREATE_VPN, method = RequestMethod.POST)
	public boolean createVpn(@RequestBody Vpn vpn) {
		log.info("Start createVpn.");

		return vpnsService.createVpn(vpn);
	}

	@RequestMapping(value = RestVpnURIConstants.DELETE_VPN, method = RequestMethod.POST)
	public boolean deleteVpn(@PathVariable("id") int vpnId) {
		log.info("Start deleteVpn.");
		
		return vpnsService.deleteVpn(vpnId);
	}
	
}
