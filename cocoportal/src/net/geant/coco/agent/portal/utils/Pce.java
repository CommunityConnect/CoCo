package net.geant.coco.agent.portal.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.scheduling.annotation.Async;

import lombok.extern.slf4j.Slf4j;
import net.geant.coco.agent.portal.dao.NetworkSite;
import net.geant.coco.agent.portal.dao.NetworkSwitch;
import net.geant.coco.agent.portal.dao.NetworkSwitchExt;
import net.geant.coco.agent.portal.service.NetworkSitesService;

@Slf4j
public class Pce {
	private List<NetworkSwitch> networkSwitches;
	private List<NetworkSite> networkSites;
	private List<NetworkSwitch> networkSwitchesWithEnni;
	
	private Topology topology;

	private RestClient restClient;
	
	public Topology getTopology() {
		return topology;
	}

	// private Topology topology = new Topology(networkSites);
	private static int flowId = 1;

	public int getFlowId() {
		return flowId;
	}

	private int getNextFlowId() {
		flowId++;
		return flowId;
	}
	
	public Pce(RestClient restClient, List<NetworkSwitch> networkSwitches, List<NetworkSite> networkSites, List<NetworkSwitch> networkSwitchesWithEnni) {
		this.restClient = restClient;
		this.networkSwitches = networkSwitches;
		this.networkSites = networkSites;
		this.networkSwitchesWithEnni = networkSwitchesWithEnni;
		this.topology = new Topology(restClient, networkSites, networkSwitches, networkSwitchesWithEnni);
	}
	
	public void updatePceElement(List<NetworkSite> networkSites) {
		this.networkSites = networkSites;
		this.topology = new Topology(restClient, this.networkSites, networkSwitches, networkSwitchesWithEnni);
	}

	/*
	public Pce(List<NetworkSwitch> networkSwitches, List<NetworkSite> networkSites, int lastFlowId) {
		this.flowId = lastFlowId;
		this.networkSwitches = networkSwitches;
		this.networkSites = networkSites;
		this.topology = new Topology(networkSites, networkSwitches, new ArrayList<NetworkSwitch>());
	}*/

	public void setupCoreForwarding() {
		/*
		 * Loop through all pairs of sites. Using 'to' in outer loop causes
		 * inner loop to have all paths towards a site.
		 */
		// Topology topology = new Topology(networkSites);
		int flowNr;
		for (NetworkSite toSite : networkSites) {
			log.info("Setting up core for site " + toSite.getName());
			for (NetworkSite fromSite : networkSites) {
				// skip if 'from' and 'to' are the same site
				if (fromSite.getName().equals(toSite.getName())) {
					continue;
				}
				List<CoCoLink> edgesList = topology.calculatePath(fromSite.getName(), toSite.getName());
				if (edgesList == null) {
					log.trace("no path");
					continue;
				}
				CoCoLink[] edges = edgesList.toArray(new CoCoLink[0]);
				for (int i = 1; i < edges.length; i++) {
					String switchName = edges[i - 1].getDstNode();
					// check if this is a core P node
					if (topology.getNode(switchName).getType() == NodeType.P) {
						log.trace("P node config on " + switchName + ": " + edges[i - 1].getDstTp() + " -> "
								+ edges[i].getSrcTp());
						flowNr = getNextFlowId();
						Flow entry = new Flow(switchName, flowNr);
						entry.inPort(edges[i - 1].getDstTpNr());
						entry.matchEthertype(0x8847);
						entry.matchMplsLabel(topology.getNode(toSite.getProviderSwitch()).getPeMplsLabel());
						entry.outPort(edges[i].getSrcTpNr());
						restClient.sendtoSwitch(switchName, "add", entry.buildFlow(), String.valueOf(flowNr));
						/*try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}*/
					}
				}
			}
		}
		
		for (NetworkSwitch networkSwitch : networkSwitchesWithEnni) {
			log.debug("Looking for switch with ENNI");
			if (networkSwitch instanceof NetworkSwitchExt) {
				log.debug("Got switch with ENNI");
				
				log.info("Setting up core for switch " + networkSwitch.getName());
				for (NetworkSite fromSite : networkSites) {
					
					List<CoCoLink> edgesList = topology.calculatePath(fromSite.getName(), networkSwitch.getName());
					if (edgesList == null) {
						log.trace("no path");
						continue;
					}
					CoCoLink[] edges = edgesList.toArray(new CoCoLink[0]);
					for (int i = 1; i < edges.length; i++) {
						String switchName = edges[i - 1].getDstNode();
						// check if this is a core P node
						if (topology.getNode(switchName).getType() == NodeType.P) {
							log.trace("P node config on " + switchName + ": " + edges[i - 1].getDstTp() + " -> "
									+ edges[i].getSrcTp());
							flowNr = getNextFlowId();
							Flow entry = new Flow(switchName, flowNr);
							entry.inPort(edges[i - 1].getDstTpNr());
							entry.matchEthertype(0x8847);
							entry.matchMplsLabel(String.valueOf(networkSwitch.getMplsLabel()));
							entry.outPort(edges[i].getSrcTpNr());
							restClient.sendtoSwitch(switchName, "add", entry.buildFlow(), String.valueOf(flowNr));
							try {
								Thread.sleep(100);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
				}
				
			}
		}
	}

	public void addSiteToVpn(NetworkSite toSite, int vpnMplsLabel, List<NetworkSite> vpnSites) {
		Flow flowEntry;
		// Topology topology = new Topology(networkSites);
		int flowNr;

		// loop through all the sites within the VPN
		for (NetworkSite fromSite : vpnSites) {
			if (fromSite.getName().equals(toSite.getName())) {
				continue;
			}

			log.trace("addSiteToVpn: " + toSite.getName());

			// check if both sites are on the same switch
			if (fromSite.getProviderSwitch().equals(toSite.getProviderSwitch())) {
				
				// build incoming flow to 'toSite' on its PE switch
				flowNr = getNextFlowId();
				
				topology.getNode(toSite.getName()).addToFlowIds(toSite.getProviderSwitch() + "/table/0/flow/" + flowNr);
				topology.getNode(fromSite.getName()).addToFlowIds(toSite.getProviderSwitch() + "/table/0/flow/" + flowNr);
				flowEntry = new Flow(toSite.getProviderSwitch(), flowNr);

				flowEntry.inPort(String.valueOf(fromSite.getProviderPort()));
				flowEntry.matchEthertype(0x0800);
				flowEntry.matchVlan(String.valueOf(fromSite.getVlanId()));
				flowEntry.matchDstIpv4Prefix(toSite.getIpv4Prefix());
				flowEntry.setDstMAC(toSite.getMacAddress());
				
				flowEntry.modVlan(String.valueOf(toSite.getVlanId()));
				flowEntry.outPort(String.valueOf(toSite.getProviderPort()));
				
				restClient.sendtoSwitch(toSite.getProviderSwitch(), "add", flowEntry.buildFlow(), String.valueOf(flowNr));

				// build outgoing flow from 'toSite' on its PE switch
				flowNr = getNextFlowId();
				topology.getNode(toSite.getName()).addToFlowIds(toSite.getProviderSwitch() + "/table/0/flow/" + flowNr);
				topology.getNode(fromSite.getName()).addToFlowIds(toSite.getProviderSwitch() + "/table/0/flow/" + flowNr);
				
				flowEntry = new Flow(toSite.getProviderSwitch(), flowNr);
				flowEntry.inPort(String.valueOf(toSite.getProviderPort()));
				flowEntry.matchEthertype(0x0800);
				flowEntry.matchVlan(String.valueOf(toSite.getVlanId()));
				flowEntry.matchDstIpv4Prefix(fromSite.getIpv4Prefix());
				flowEntry.setDstMAC(fromSite.getMacAddress());
				flowEntry.modVlan(String.valueOf(fromSite.getVlanId()));
				flowEntry.outPort(String.valueOf(fromSite.getProviderPort()));
				restClient.sendtoSwitch(toSite.getProviderSwitch(), "add", flowEntry.buildFlow(), String.valueOf(flowNr));
			} else {

				List<CoCoLink> edgesList = topology.calculatePath(fromSite.getName(), toSite.getName());
				CoCoLink[] edges = edgesList.toArray(new CoCoLink[0]);

				log.debug("set fwd entry on " + toSite.getProviderSwitch() + " PE switch of " + fromSite.getName() + " --> " + toSite.getName());
				flowNr = getNextFlowId();
				topology.getNode(toSite.getName()).addToFlowIds(fromSite.getProviderSwitch() + "/table/0/flow/" + flowNr);
				topology.getNode(fromSite.getName()).addToFlowIds(fromSite.getProviderSwitch() + "/table/0/flow/" + flowNr);

				flowEntry = new Flow(fromSite.getProviderSwitch(), flowNr);
				flowEntry.inPort(edges[0].getDstTpNr());
				flowEntry.matchEthertype(0x0800);
				flowEntry.matchVlan(String.valueOf(fromSite.getVlanId()));
				flowEntry.matchDstIpv4Prefix(toSite.getIpv4Prefix());
				flowEntry.setDstMAC(toSite.getMacAddress());
				flowEntry.pushVpnMplsLabel(String.valueOf(vpnMplsLabel));
				flowEntry.pushPeMplsLabel(topology.getNode(toSite.getProviderSwitch()).getPeMplsLabel());
				flowEntry.modVlan(String.valueOf(toSite.getVlanId()));
				flowEntry.outPort(edges[1].getSrcTpNr());
				restClient.sendtoSwitch(fromSite.getProviderSwitch(), "add", flowEntry.buildFlow(), String.valueOf(flowNr));

				log.debug("set ingress fwd entry on PE switch of " + toSite.getName() + " --> " + fromSite.getName());
				flowNr = getNextFlowId();
				topology.getNode(toSite.getName()).addToFlowIds(toSite.getProviderSwitch() + "/table/0/flow/" + flowNr);
				topology.getNode(toSite.getName()).addToFlowIds(fromSite.getProviderSwitch() + "/table/0/flow/" + flowNr);
				
				flowEntry = new Flow(toSite.getProviderSwitch(), flowNr);
				flowEntry.inPort(edges[edges.length - 1].getSrcTpNr());
				flowEntry.matchEthertype(0x0800);
				flowEntry.matchVlan(String.valueOf(toSite.getVlanId()));
				flowEntry.matchDstIpv4Prefix(fromSite.getIpv4Prefix());
				flowEntry.setDstMAC(fromSite.getMacAddress());
				flowEntry.pushVpnMplsLabel(String.valueOf(vpnMplsLabel));
				flowEntry.pushPeMplsLabel(topology.getNode(fromSite.getProviderSwitch()).getPeMplsLabel());
				flowEntry.modVlan(String.valueOf(fromSite.getVlanId()));
				flowEntry.outPort(edges[edges.length - 2].getDstTpNr());
				restClient.sendtoSwitch(toSite.getProviderSwitch(), "add", flowEntry.buildFlow(), String.valueOf(flowNr));

				// Michal addition 2
				log.debug("second entry Michal on PE switch of " + toSite.getName() + " --> " + fromSite.getName());
				flowNr = getNextFlowId();

				topology.getNode(toSite.getName()).addToFlowIds(toSite.getProviderSwitch() + "/table/0/flow/" + flowNr);
				topology.getNode(fromSite.getName()).addToFlowIds(fromSite.getProviderSwitch() + "/table/0/flow/" + flowNr);

				flowEntry = new Flow(toSite.getProviderSwitch(), flowNr);
				flowEntry.inPort(edges[edges.length - 2].getDstTpNr());
				flowEntry.matchEthertype(0x8847);
				flowEntry.matchDlDst(toSite.getMacAddress());
				flowEntry.matchMplsLabel(topology.getNode(edges[edges.length - 1].getSrcNode()).getPeMplsLabel());

				flowEntry.popTwoMplsLabels();
				flowEntry.outPort(edges[edges.length - 1].getSrcTpNr());

				restClient.sendtoSwitch(toSite.getProviderSwitch(), "add", flowEntry.buildFlow(), String.valueOf(flowNr));

				//edgesList = topology.calculatePath(toSite.getName(), fromSite.getName());
				//edges = edgesList.toArray(new CoCoLink[0]);

				// Michal addition 2 - other switch
				log.debug("second entry Michal on PE switch of " + toSite.getName() + " --> " + fromSite.getName());
				flowNr = getNextFlowId();

				topology.getNode(toSite.getName()).addToFlowIds(toSite.getProviderSwitch() + "/table/0/flow/" + flowNr);
				topology.getNode(fromSite.getName()).addToFlowIds(fromSite.getProviderSwitch() + "/table/0/flow/" + flowNr);

				flowEntry = new Flow(fromSite.getProviderSwitch(), flowNr);
				flowEntry.inPort(edges[1].getSrcTpNr());
				flowEntry.matchEthertype(0x8847);
				flowEntry.matchDlDst(fromSite.getMacAddress());
				flowEntry.matchMplsLabel(topology.getNode(edges[1].getSrcNode()).getPeMplsLabel());
				flowEntry.popTwoMplsLabels();
				flowEntry.outPort(edges[0].getDstTpNr());

				restClient.sendtoSwitch(fromSite.getProviderSwitch(), "add", flowEntry.buildFlow(), String.valueOf(flowNr));

			}
		}
	}

	public void deleteSiteFromVpn(NetworkSite toSite, int vpnMplsLabel, List<NetworkSite> vpnSites) {

		// Topology topology = new Topology(networkSites);
		log.trace("deleteSiteFromVpn: " + toSite.getName());
		Set<String> flowIds = new HashSet<String>();
		flowIds.addAll(topology.getNode(toSite.getName()).getFlowIds());
		for (String id : flowIds) {
			restClient.sendtoSwitch("", "delete", id, "");
			topology.getNode(toSite.getName()).deleteFromFlowIds(id);
			// loop through all the sites within the VPN
			for (NetworkSite fromSite : vpnSites) {
				topology.getNode(fromSite.getName()).deleteFromFlowIds(id);
			}
		}
	}
}