package net.geant.coco.agent.portal.controllers;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.validation.Valid;

import net.geant.coco.agent.portal.bgp.BgpRouteEntry;
import net.geant.coco.agent.portal.bgp.BgpRouter;
import net.geant.coco.agent.portal.dao.NetworkElement;
import net.geant.coco.agent.portal.dao.NetworkInterface;
import net.geant.coco.agent.portal.dao.NetworkLink;
import net.geant.coco.agent.portal.dao.NetworkSite;
import net.geant.coco.agent.portal.dao.NetworkSwitch;
import net.geant.coco.agent.portal.dao.Vpn;
import net.geant.coco.agent.portal.rest.RestVpnURIConstants;
import net.geant.coco.agent.portal.rest.RestSite;
import net.geant.coco.agent.portal.rest.RestVpn;
import net.geant.coco.agent.portal.service.NetworkLinksService;
import net.geant.coco.agent.portal.service.NetworkSitesService;
import net.geant.coco.agent.portal.service.NetworkSwitchesService;
import net.geant.coco.agent.portal.service.TopologyService;
import net.geant.coco.agent.portal.service.VpnsService;
import net.geant.coco.agent.portal.utils.NodeType;
import net.geant.coco.agent.portal.utils.Pce;
import net.geant.coco.agent.portal.utils.RestClient;
import net.geant.coco.agent.portal.utils.VpnProvisioner;
import net.geant.coco.agent.portal.threads.BgpThread;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@Configuration
@PropertySource("classpath:/net/geant/coco/agent/portal/props/config.properties")
public class PortalController {

    @Autowired
    Environment env;

    private NetworkSwitchesService networkSwitchesService;
    private NetworkLinksService networkLinksService;
    private NetworkSitesService networkSitesService;
    private VpnsService vpnsService;
    private TopologyService topologyService;

    List<NetworkSwitch> networkSwitches;
    List<NetworkSwitch> networkSwitchesWithEnni;
    List<NetworkLink> networkLinks;
    List<NetworkSite> networkSites;
    List<Vpn> vpns;

    private String neighborIp;
    private String neighborName;

    Map<String, String> sitesNameToPrefixMap;

    boolean networkChanged = false;
    //BgpRouter bgpRouter;
    
    VpnProvisioner vpnProvisioner;

    @Autowired
    public void setNetworkSwitchService(
            NetworkSwitchesService networkSwitchesService) {
        this.networkSwitchesService = networkSwitchesService;
    }

    @Autowired
    public void setNetworkLinkService(NetworkLinksService networkLinksService) {
        this.networkLinksService = networkLinksService;
    }

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

    private void restAddSiteToVpn(String vpnName, String addSiteName) {
        RestSite restSiteToAdd = getRestSiteByName(addSiteName);
        RestVpn restVpn = getRestVpnByName(vpnName);
        restVpn.addSiteToVpn(restSiteToAdd);
    }

    private void restDeleteSiteFromVpn(String vpnName, String deleteSiteName) {
        RestSite restSiteToDelete = getRestSiteByName(deleteSiteName);
        RestVpn restVpn = getRestVpnByName(vpnName);
        restVpn.deleteSiteFromVpn(restSiteToDelete);
    }

    private void networkAddSiteToVpn(String vpnName, String addSiteName) {

        vpnsService.addSite(vpnName, addSiteName);
        // find site object
        for (NetworkSite networkSite : networkSitesService.getNetworkSites()) {
            if (networkSite.getName().equals(addSiteName)) {
                Vpn vpn = vpnsService.getVpn(vpnName);
                log.info("MPLS label for " + vpnName + " is "
                        + vpn.getMplsLabel());

                int status = vpnProvisioner.addSite(vpnName, addSiteName,
                        networkSite.getIpv4Prefix(), networkSite.getProviderSwitch()
                                + ":" + networkSite.getProviderPort());
                log.info("addSite returns: " + status);
            }
        }

    }

    private void networkDeleteSiteFromVpn(String vpnName, String deleteSiteName) {
        vpnsService.deleteSite(deleteSiteName);
        // find site object
        for (NetworkSite networkSite : networkSitesService.getNetworkSites()) {
            if (networkSite.getName().equals(deleteSiteName)) {
                Vpn vpn = vpnsService.getVpn(vpnName);
                log.info("MPLS label for " + vpnName + " is "
                        + vpn.getMplsLabel());
                int status = vpnProvisioner.deleteSite(vpnName, deleteSiteName,
                        networkSite.getIpv4Prefix(), networkSite.getProviderSwitch()
                                + ":" + networkSite.getProviderPort());
                log.info("addSite returns: " + status);
            }
        }
    }

    @RequestMapping(value = RestVpnURIConstants.GET_TOPOLOGY, method = RequestMethod.GET)
    public @ResponseBody List<NetworkInterface> getTopology() {
        return topologyService.getNetworkInterfaces();
    }

    @RequestMapping(value = RestVpnURIConstants.GET_TOPOLOGY_VIS, method = RequestMethod.GET)
    public @ResponseBody String getTopologyVis() {
        StringBuilder visJson = new StringBuilder();

        Set<NetworkElement> nodeSet = new HashSet<NetworkElement>();

        List<NetworkInterface> networkInterfaces = topologyService
                .getNetworkInterfaces();

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
            if (networkElement.nodeType
                    .equals(NetworkElement.NODE_TYPE.CUSTOMER)) {
                fakeId = networkElement.id;
            } else if (networkElement.nodeType
                    .equals(NetworkElement.NODE_TYPE.EXTERNAL_AS)) {
                fakeId = 100 + networkElement.id;
                
                //TODO continue to ignore external as sites
                continue;
                
            } else if (networkElement.nodeType
                    .equals(NetworkElement.NODE_TYPE.SWITCH)) {
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
            if (networkElement.nodeType
                    .equals(NetworkElement.NODE_TYPE.CUSTOMER)) {
                fakeId = networkElement.id;
            } else if (networkElement.nodeType
                    .equals(NetworkElement.NODE_TYPE.EXTERNAL_AS)) {
                fakeId = 100 + networkElement.id;
            } else if (networkElement.nodeType
                    .equals(NetworkElement.NODE_TYPE.SWITCH)) {
                fakeId = 200 + networkElement.id;
            }
            visJson.append("{\"from\": \"");
            visJson.append(fakeId);

            networkElement = networkInterface.neighbour;
            fakeId = 0;
            if (networkElement.nodeType
                    .equals(NetworkElement.NODE_TYPE.CUSTOMER)) {
                fakeId = networkElement.id;
            } else if (networkElement.nodeType
                    .equals(NetworkElement.NODE_TYPE.EXTERNAL_AS)) {
                fakeId = 100 + networkElement.id;
            } else if (networkElement.nodeType
                    .equals(NetworkElement.NODE_TYPE.SWITCH)) {
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


    // Map to store vpns, ideally we should use database
    Map<Integer, RestVpn> restVpnData = new HashMap<Integer, RestVpn>();

    // Map to store sites, ideally we should use database
    Map<Integer, RestSite> restSiteData = new HashMap<Integer, RestSite>();

    @RequestMapping(value = RestVpnURIConstants.DUMMY_VPN, method = RequestMethod.GET)
    public @ResponseBody RestVpn getDummyVpn() {
        log.info("Start getDummyVpn");
        RestVpn vpn = new RestVpn();
        vpn.setId(9999);
        vpn.setName("Dummy");
        // emp.setCreatedDate(new Date());
        restVpnData.put(9999, vpn);
        return vpn;
    }

    @RequestMapping(value = RestVpnURIConstants.GET_VPN, method = RequestMethod.GET)
    public @ResponseBody RestVpn getVpn(@PathVariable("id") int vpnId) {
        log.info("Start getVpn. ID=" + vpnId);

        return restVpnData.get(vpnId);
    }

    @RequestMapping(value = RestVpnURIConstants.UPDATE_VPN, method = RequestMethod.POST)
    public @ResponseBody RestVpn updateVpn(@PathVariable("id") int vpnId,
            @RequestBody RestVpn vpn) {
        log.info("Start updateVpn. ID=" + vpnId);

        Assert.isTrue(
                vpn.getId() == vpnId,
                "VPN id and ID from the rest path are not the same. REST PATH:"
                        + String.valueOf(vpnId) + " VPN ID"
                        + String.valueOf(vpn.getId()));

        RestVpn vpnNew = vpn;
        RestVpn vpnCurrent = restVpnData.get(vpnId);

        List<RestSite> sitesToAdd = new ArrayList<RestSite>(vpnNew.getSites());
        sitesToAdd.removeAll(vpnCurrent.getSites());
        List<RestSite> sitesToRemove = new ArrayList<RestSite>(
                vpnCurrent.getSites());
        sitesToRemove.removeAll(vpnNew.getSites());

        for (RestSite restSite : sitesToAdd) {
    		restAddSiteToVpn(vpnCurrent.getName(), restSite.getName());
		}
    	
    	for (RestSite restSite : sitesToRemove) {
    		restDeleteSiteFromVpn(vpnCurrent.getName(), restSite.getName());	
		}
    	
    	for (RestSite restSite : sitesToAdd) {
        	networkAddSiteToVpn(vpnCurrent.getName(), restSite.getName());
		}
    	for (RestSite restSite : sitesToRemove) {
    		networkDeleteSiteFromVpn(vpnCurrent.getName(), restSite.getName());
		}

        return restVpnData.get(vpnId);
    }

    @RequestMapping(value = RestVpnURIConstants.SET_VPN_PRIVACY, method = RequestMethod.POST)
    public @ResponseBody RestVpn setVpnPrivacy(@PathVariable("id") int vpnId,
            @RequestBody RestVpn vpn) {
        log.info("Start setVpnPrivacy. ID=" + vpnId);

        Assert.isTrue(
                vpn.getId() == vpnId,
                "VPN id and ID from the rest path are not the same. REST PATH:"
                        + String.valueOf(vpnId) + " VPN ID"
                        + String.valueOf(vpn.getId()));

        RestVpn vpnCurrent = restVpnData.get(vpnId);
        vpnCurrent.setPublic(vpn.getIsPublic());

        if (vpn.getIsPublic()) {
            // make it public to all neighbors using BGP
            log.info("making vpn public");

            List<RestSite> sites = vpn.getSites();
            for (RestSite restSite : sites) {
                String siteName = restSite.getName();
                if (!siteName.contains(neighborName)) {
                    String siteIpPrefix = this.sitesNameToPrefixMap
                            .get(siteName);
                    //bgpRouter.addVpn(siteIpPrefix, this.neighborIp, vpnId);
                }
            }
        } else {
            // make it not public to all neighbors using BGP
            log.info("making vpn private");

            List<RestSite> sites = vpn.getSites();
            for (RestSite restSite : sites) {
                String siteName = restSite.getName();
                if (!siteName.contains(neighborName)) {
                    String siteIpPrefix = this.sitesNameToPrefixMap
                            .get(siteName);
                    //bgpRouter.delVpn(siteIpPrefix, this.neighborIp, vpnId);
                }
            }
        }

        return vpn;

    }

    
    public void initializeNetworkSitesData(boolean doGetSites) {
        if (doGetSites) {
            this.networkSites = networkSitesService.getNetworkSites();
        }
        
        String controllerUrl = env.getProperty("controller.url");
        vpnProvisioner = new VpnProvisioner(controllerUrl);

        List<Vpn> vpnsFromDao = vpnsService.getVpns();

        log.info("Initialize rest data");
        // TODO - this is from database, synch with state in vpnData
        // where should I get the data from?
        for (Vpn vpnFromDao : vpnsFromDao) {
            List<NetworkSite> networkSites = networkSitesService
                    .getNetworkSites(vpnFromDao.getName());
            RestVpn restVpn = new RestVpn(vpnFromDao);
            restVpn.setSites(siteToRest(networkSites));
            restVpnData.put(restVpn.getId(), restVpn);
        }

        List<RestSite> restSites = siteToRest(networkSites);

        for (RestSite restSite : restSites) {
            restSiteData.put(restSite.getId(), restSite);
        }

        Map<String, String> sitesNameToPrefixMap = new HashMap<String, String>();

        for (NetworkSite site : networkSites) {
            sitesNameToPrefixMap.put(site.getName(), site.getIpv4Prefix());
        }

        this.sitesNameToPrefixMap = sitesNameToPrefixMap;

        this.networkChanged = true;
    }

    @PostConstruct
    @RequestMapping("/setupAll")
    public @ResponseBody String initializeEverything() {
        log.info("Initialize everything");

        log.warn(env.getProperty("ip"));

        log.info("Initialize network elements");
        this.networkSwitches = networkSwitchesService.getNetworkSwitches();
        this.networkSwitchesWithEnni = networkSwitchesService
                .getNetworkSwitchesWithNni();
        this.networkLinks = networkLinksService.getNetworkLinks();
        this.networkSites = networkSitesService.getNetworkSites();
        this.vpns = vpnsService.getVpns();

        log.info("Initialize PCE object");
        //String bgpIp = env.getProperty("ip");
        //bgpRouter = new BgpRouter(bgpIp, 7644);

        initializeNetworkSitesData(false);

        // INTENT - disable bgp
//        Runnable bgpThreadRunnable = new BgpThread(networkSwitchesService,
//                networkLinksService, networkSitesService, bgpRouter, this);
//        log.debug("Starting bgp thread");
//        new Thread(bgpThreadRunnable).start();
//        log.debug("Started bgp thread");

        this.neighborIp = env.getProperty("bgpNeighborIps");
        this.neighborName = env.getProperty("neighborName");
        
        vpnProvisioner.createVpn("vpn1");
        return "everything initialized succesfully";

    }

    @RequestMapping(value = RestVpnURIConstants.GET_ALL_VPN, method = RequestMethod.GET)
    public @ResponseBody List<RestVpn> getAllVpns() {
        log.info("Start getAllVpns.");

        return new ArrayList<RestVpn>(restVpnData.values());
    }

    @RequestMapping(value = RestVpnURIConstants.GET_ALL_SITES, method = RequestMethod.GET)
    public @ResponseBody List<NetworkSite> getAllSites(
            @PathVariable("id") int vpnID) {
        log.info("Start getall sites.");

        Vpn vpn = vpnsService.getVpn(vpnID);
        return networkSitesService.getNetworkSites(vpn.getName());
    }

    @RequestMapping(value = RestVpnURIConstants.CREATE_VPN, method = RequestMethod.POST)
    public @ResponseBody RestVpn createVpn(@RequestBody RestVpn vpn) {
        log.info("Start createVpn.");

        restVpnData.put(vpn.getId(), vpn);
        return vpn;
    }

    @RequestMapping(value = RestVpnURIConstants.DELETE_VPN, method = RequestMethod.PUT)
    public @ResponseBody RestVpn deleteVpn(@PathVariable("id") int vpnId) {
        log.info("Start deleteVpn.");
        RestVpn vpn = restVpnData.get(vpnId);
        restVpnData.remove(vpnId);
        return vpn;
    }

    private List<RestVpn> vpnToRest(List<Vpn> vpns) {
        List<RestVpn> restVpns = new ArrayList<RestVpn>();

        for (Vpn vpn : vpns) {
            restVpns.add(new RestVpn(vpn));
        }

        return restVpns;
    }

    private List<RestSite> siteToRest(List<NetworkSite> sites) {
        List<RestSite> restSites = new ArrayList<RestSite>();

        for (NetworkSite site : sites) {
            restSites.add(new RestSite(site));
        }

        return restSites;
    }
}
