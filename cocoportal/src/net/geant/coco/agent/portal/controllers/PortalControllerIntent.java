package net.geant.coco.agent.portal.controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import lombok.extern.slf4j.Slf4j;
import net.geant.coco.agent.portal.dao.NetworkElement;
import net.geant.coco.agent.portal.dao.NetworkInterface;
import net.geant.coco.agent.portal.dao.NetworkSite;
import net.geant.coco.agent.portal.dao.Vpn;
import net.geant.coco.agent.portal.rest.RestSite;
import net.geant.coco.agent.portal.rest.RestVpn;
import net.geant.coco.agent.portal.rest.RestVpnURIConstants;
import net.geant.coco.agent.portal.service.NetworkSitesService;
import net.geant.coco.agent.portal.service.TopologyService;
import net.geant.coco.agent.portal.service.VpnsService;
import net.geant.coco.agent.portal.utils.RestUtils;
import net.geant.coco.agent.portal.utils.VpnProvisioner;

@Slf4j
@Controller
@Configuration
@PropertySource("classpath:/net/geant/coco/agent/portal/props/config.properties")
public class PortalControllerIntent {

	@Autowired
	Environment env;
	
	private NetworkSitesService networkSitesService;
	private VpnsService vpnsService;
	private TopologyService topologyService;
	
	List<NetworkSite> networkSites;
	
	boolean networkChanged = false;
	
	VpnProvisioner vpnProvisioner;
	
	RestUtils restUtils = new RestUtils();
	
	@Autowired
	public void setNetworkSitesService(NetworkSitesService networkSitesService) {
		this.networkSitesService = networkSitesService;
	}

	@Autowired
	public void setVpnsService(VpnsService vpnsService) {
		this.vpnsService = vpnsService;
	}

	@Autowired
	public void setTopologyService(TopologyService topologyService) {
		this.topologyService = topologyService;
	}
	private NetworkSite findNetworkSiteByName(String siteName) {
		for (NetworkSite networkSite : networkSitesService.getNetworkSites()) {
			if (networkSite.getName().equals(siteName)) {
				return networkSite;
			}
		}
		
		return null;	
	}
	
	private void networkAddSiteToVpn(String vpnName, String addSiteName) {
		vpnsService.addSite(vpnName, addSiteName);
		NetworkSite networkSite = findNetworkSiteByName(addSiteName);
		int status = vpnProvisioner.addSite(vpnName, addSiteName, networkSite.getIpv4Prefix(),
				networkSite.getProviderSwitch() + ":" + networkSite.getProviderPort());
		log.info("addSite returns: " + status);

	}

	private void networkDeleteSiteFromVpn(String vpnName, String deleteSiteName) {
		vpnsService.deleteSite(deleteSiteName);
		NetworkSite networkSite = findNetworkSiteByName(deleteSiteName);
		int status = vpnProvisioner.deleteSite(vpnName, deleteSiteName, networkSite.getIpv4Prefix(),
				networkSite.getProviderSwitch() + ":" + networkSite.getProviderPort());
		log.info("addSite returns: " + status);
	}
	
	
	@RequestMapping(value = RestVpnURIConstants.GET_TOPOLOGY_VIS, method = RequestMethod.GET)
	public @ResponseBody String getTopologyVis() {
		StringBuilder visJson = new StringBuilder();

		Set<NetworkElement> nodeSet = new HashSet<NetworkElement>();

		List<NetworkInterface> networkInterfaces = topologyService.getNetworkInterfaces();

		for (NetworkInterface networkInterface : networkInterfaces) {
			if (!nodeSet.contains(networkInterface.source)) {
				nodeSet.add(networkInterface.source);
			}

			if (!nodeSet.contains(networkInterface.neighbour)) {
				nodeSet.add(networkInterface.neighbour);
			}
		}

		visJson.append("{\"nodes\" : [ ");
		for (NetworkElement networkElement : nodeSet) {
			int fakeId = 0;
			if (networkElement.nodeType.equals(NetworkElement.NODE_TYPE.CUSTOMER)) {
				fakeId = networkElement.id;
			} else if (networkElement.nodeType.equals(NetworkElement.NODE_TYPE.EXTERNAL_AS)) {
				fakeId = 100 + networkElement.id;

				// TODO continue to ignore external as sites
				continue;

			} else if (networkElement.nodeType.equals(NetworkElement.NODE_TYPE.SWITCH)) {
				fakeId = 200 + networkElement.id;
			}
			visJson.append("{\"id\": \"");
			visJson.append(fakeId);
			visJson.append("\", \"label\": \"");
			visJson.append(networkElement.name);
			visJson.append("\", \"group\": \"");
			visJson.append(networkElement.nodeType);
			visJson.append("\"}, ");
		}

		visJson.deleteCharAt(visJson.lastIndexOf(","));
		visJson.append("],");
		visJson.append("\"edges\" : [");

		for (NetworkInterface networkInterface : networkInterfaces) {
			int fakeId = 0;
			NetworkElement networkElement;

			networkElement = networkInterface.source;
			fakeId = 0;
			if (networkElement.nodeType.equals(NetworkElement.NODE_TYPE.CUSTOMER)) {
				fakeId = networkElement.id;
			} else if (networkElement.nodeType.equals(NetworkElement.NODE_TYPE.EXTERNAL_AS)) {
				fakeId = 100 + networkElement.id;
			} else if (networkElement.nodeType.equals(NetworkElement.NODE_TYPE.SWITCH)) {
				fakeId = 200 + networkElement.id;
			}
			visJson.append("{\"from\": \"");
			visJson.append(fakeId);

			networkElement = networkInterface.neighbour;
			fakeId = 0;
			if (networkElement.nodeType.equals(NetworkElement.NODE_TYPE.CUSTOMER)) {
				fakeId = networkElement.id;
			} else if (networkElement.nodeType.equals(NetworkElement.NODE_TYPE.EXTERNAL_AS)) {
				fakeId = 100 + networkElement.id;
			} else if (networkElement.nodeType.equals(NetworkElement.NODE_TYPE.SWITCH)) {
				fakeId = 200 + networkElement.id;
			}
			visJson.append("\", \"to\": \"");
			visJson.append(fakeId);
			visJson.append("\"}, ");
		}
		visJson.deleteCharAt(visJson.lastIndexOf(","));
		visJson.append("]}");

		return visJson.toString();
	}

	@RequestMapping(value = RestVpnURIConstants.GET_TOPOLOGY_IS_CHANGED, method = RequestMethod.GET)
	public @ResponseBody boolean getTopologyChange() {

		if (networkChanged == true) {
			networkChanged = false;
			return true;
		}

		return networkChanged;
	}
	
	@RequestMapping(value = RestVpnURIConstants.GET_VPN, method = RequestMethod.GET)
	public @ResponseBody RestVpn getVpn(@PathVariable("id") int vpnId) {
		log.info("Start getVpn. ID=" + vpnId);
		return restUtils.restVpnData.get(vpnId);
	}

	@RequestMapping(value = RestVpnURIConstants.UPDATE_VPN, method = RequestMethod.POST)
	public @ResponseBody RestVpn updateVpn(@PathVariable("id") int vpnId, @RequestBody RestVpn vpn) {
		log.info("Start updateVpn. ID=" + vpnId);

		Assert.isTrue(vpn.getId() == vpnId, "VPN id and ID from the rest path are not the same. REST PATH:"
				+ String.valueOf(vpnId) + " VPN ID" + String.valueOf(vpn.getId()));

		RestVpn vpnNew = vpn;
		RestVpn vpnCurrent = restUtils.restVpnData.get(vpnId);

		List<RestSite> sitesToAdd = new ArrayList<RestSite>(vpnNew.getSites());
		sitesToAdd.removeAll(vpnCurrent.getSites());
		List<RestSite> sitesToRemove = new ArrayList<RestSite>(vpnCurrent.getSites());
		sitesToRemove.removeAll(vpnNew.getSites());

		for (RestSite restSite : sitesToAdd) {
			restUtils.restAddSiteToVpn(vpnCurrent.getName(), restSite.getName());
		}

		for (RestSite restSite : sitesToRemove) {
			restUtils.restDeleteSiteFromVpn(vpnCurrent.getName(), restSite.getName());
		}

		for (RestSite restSite : sitesToAdd) {
			networkAddSiteToVpn(vpnCurrent.getName(), restSite.getName());
		}
		for (RestSite restSite : sitesToRemove) {
			networkDeleteSiteFromVpn(vpnCurrent.getName(), restSite.getName());
		}

		return restUtils.restVpnData.get(vpnId);
	}
	
	
	
	@PostConstruct
	@RequestMapping("/setupAll")
	public @ResponseBody String initializeEverything() {
		log.info("Initialize everything Intent version");

		this.networkSites = networkSitesService.getNetworkSites();
		
		String controllerUrl = env.getProperty("controller.url");
		vpnProvisioner = new VpnProvisioner(controllerUrl);
		
		log.info("Initialize rest data");
		List<Vpn> vpnsFromDao = vpnsService.getVpns(controllerUrl);

		// TODO - this is from database, synch with state in vpnData
		// where should I get the data from?
		for (Vpn vpnFromDao : vpnsFromDao) {
			List<NetworkSite> networkSites = networkSitesService.getNetworkSites(vpnFromDao.getName());
			RestVpn restVpn = new RestVpn(vpnFromDao);
			restVpn.setSites(siteToRest(networkSites));
			restUtils.restVpnData.put(restVpn.getId(), restVpn);
		}

		List<RestSite> restSites = siteToRest(networkSites);

		for (RestSite restSite : restSites) {
			restUtils.restSiteData.put(restSite.getId(), restSite);
		}

		Map<String, String> sitesNameToPrefixMap = new HashMap<String, String>();

		for (NetworkSite site : networkSites) {
			sitesNameToPrefixMap.put(site.getName(), site.getIpv4Prefix());
		}

		// TODO what to do with it?
		//this.sitesNameToPrefixMap = sitesNameToPrefixMap;

		this.networkChanged = true;
		
		return "everything initialized succesfully";

	}
	
	@RequestMapping(value = RestVpnURIConstants.GET_ALL_VPN, method = RequestMethod.GET)
	public @ResponseBody List<RestVpn> getAllVpns() {
		log.info("Start getAllVpns.");

		return new ArrayList<RestVpn>(restUtils.restVpnData.values());
	}

	@RequestMapping(value = RestVpnURIConstants.GET_ALL_SITES, method = RequestMethod.GET)
	public @ResponseBody List<NetworkSite> getAllSites(@PathVariable("id") int vpnID) {
		log.info("Start getall sites.");

		Vpn vpn = vpnsService.getVpn(vpnID);

		if (vpn == null) {
			return new ArrayList<NetworkSite>();
		}

		return networkSitesService.getNetworkSites(vpn.getName());
	}

	@RequestMapping(value = RestVpnURIConstants.CREATE_VPN, method = RequestMethod.POST)
	public @ResponseBody RestVpn createVpn(@RequestBody RestVpn vpn) {
		log.info("Start createVpn.");

		restUtils.restVpnData.put(vpn.getId(), vpn);
		return vpn;
	}

	@RequestMapping(value = RestVpnURIConstants.DELETE_VPN, method = RequestMethod.PUT)
	public @ResponseBody RestVpn deleteVpn(@PathVariable("id") int vpnId) {
		log.info("Start deleteVpn.");
		RestVpn vpn = restUtils.restVpnData.get(vpnId);
		restUtils.restVpnData.remove(vpnId);
		return vpn;
	}

	private List<RestSite> siteToRest(List<NetworkSite> sites) {
		List<RestSite> restSites = new ArrayList<RestSite>();

		for (NetworkSite site : sites) {
			restSites.add(new RestSite(site));
		}

		return restSites;
	}
	
}
