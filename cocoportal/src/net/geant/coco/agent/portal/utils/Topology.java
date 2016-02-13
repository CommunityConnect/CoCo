/**
 * 
 */
package net.geant.coco.agent.portal.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;
import net.geant.coco.agent.portal.dao.NetworkSite;
import net.geant.coco.agent.portal.dao.NetworkSwitch;
import net.geant.coco.agent.portal.service.NetworkSitesService;
import net.geant.coco.agent.portal.service.NetworkSwitchesService;

import org.jgrapht.DirectedGraph;
import org.jgrapht.Graph;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 * @author rvdp
 *
 */
@Slf4j
public class Topology {

	//private DirectedGraph<CoCoNode, CoCoLink> graph = new DefaultDirectedWeightedGraph<CoCoNode, CoCoLink>(CoCoLink.class);
	// adding weights to break symmetry in the topology
    private DefaultDirectedWeightedGraph<CoCoNode, CoCoLink> graph = new DefaultDirectedWeightedGraph<CoCoNode, CoCoLink>(
            CoCoLink.class);
    private HashMap<String, CoCoNode> nodeMap = new HashMap<String, CoCoNode>();
    private List<CoCoLink> edges = new ArrayList<CoCoLink>();
    private List<CoCoVPN> vpns = new ArrayList<CoCoVPN>();
    private String id;
    private static int flowId = 1;
    private int activeVpn = 1;
    
    private List<CoCoNode> listOfEndNodes = new ArrayList<CoCoNode>();
    private List<CoCoNode> listOfEdgeSwitches = new ArrayList<CoCoNode>();

    private RestClient restClient;
    
    public Topology(RestClient restClient, List<NetworkSite> networkSites, List<NetworkSwitch> networkSwitches, List<NetworkSwitch> networkSwitchesWithEnni) {
        // remove all forwarding entries from switches
        // RestClient.clearAll();

    	this.restClient = restClient;
        log.info("Topology init");
        // make REST call to OpenDaylight to get topology info in JSON format
        String jsonTopo = this.restClient.getJsonTopo();

        double edgeWeight = 1;
        
        // Parse the JSON info in 'jsonTopo'
        try {
            // Store nodes in HashMap 'nodeMap' with 'node-id' as key
            // Element are CoCoNode objects that also contain the node's
            // termination points
            JSONParser jsonParser = new JSONParser();
            JSONObject json = (JSONObject) jsonParser.parse(jsonTopo);
            JSONObject networkTopology = (JSONObject) json.get("network-topology");
            JSONArray topologyList = (JSONArray) networkTopology.get("topology");
            log.info("Topology - store nodes");
            for (int i = 0; i < topologyList.size(); i++) {
                JSONObject topo = (JSONObject) topologyList.get(i);
                JSONArray nodeList = (JSONArray) topo.get("node");
                for (int j = 0; j < nodeList.size(); j++) {
                    JSONObject node = (JSONObject) nodeList.get(j);
                    String nodeId = (String) node.get("node-id");
                    CoCoNode n = new CoCoNode(nodeId);
                    JSONArray tpList = (JSONArray) node
                            .get("termination-point");
                    log.trace(String.format("Topology node %s: ", nodeId));
                    // add all termination points to the CoCoNode object n
                    for (int k = 0; k < tpList.size(); k++) {
                        JSONObject tp = (JSONObject) tpList.get(k);
                        String tpId = (String) tp.get("tp-id");
                        log.trace(String.format("%s ", tpId));
                        n.addTp(tpId);
                    }
                    nodeMap.put(nodeId, n);
                    // Store the node in the graph too
                    graph.addVertex(n);
                }

                log.info("Topology - store links");
                // Parse JSON info and store links
                JSONArray linkList = (JSONArray) topo.get("link");
                for (int m = 0; m < linkList.size(); m++) {
                	log.info(Integer.toString(m) + " out of " + Integer.toString(linkList.size()));
                    JSONObject link = (JSONObject) linkList.get(m);
                    String linkId = (String) link.get("link-id");

                    JSONObject source = (JSONObject) link.get("source");
                    String sourceNode = (String) source.get("source-node");
                    String sourceTp = (String) source.get("source-tp");

                    JSONObject destination = (JSONObject) link
                            .get("destination");
                    String dstNode = (String) destination.get("dest-node");
                    String dstTp = (String) destination.get("dest-tp");

                    // create an edge between sourceNode and dstNode and add it
                    // to the graph
                    CoCoLink e = new CoCoLink(linkId, sourceNode, sourceTp,
                            dstNode, dstTp);

                    String sourceTpNr = getPortName(sourceNode, sourceTp);
                    String dstTpNr = getPortName(dstNode, dstTp);
                    e.setSrcTpNr(sourceTpNr);
                    e.setDstTpNr(dstTpNr);
                    log.trace(String.format(
                            "link %s: from %s port %s to %s port %s\n", linkId,
                            sourceNode, sourceTpNr, dstNode, dstTpNr));

                    CoCoNode src = nodeMap.get(sourceNode);
                    CoCoNode dst = nodeMap.get(dstNode);
                    graph.addEdge(src, dst, e);
                    
                    CoCoLink oppositeEdge = graph.getEdge(dst, src);
                    
                    if (oppositeEdge != null) {
                    	graph.setEdgeWeight(e, oppositeEdge.weight);
                    	e.weight = oppositeEdge.weight;
                    }
                    else {
                    	graph.setEdgeWeight(e, edgeWeight);
                    	e.weight = edgeWeight;
                        edgeWeight = edgeWeight*2;
                    }
                    
                    edges.add(e);

                    // update interface types of termination points in src and
                    // dst node
                    CoCoTerminationPoint cctpSrc = src.getTp(sourceTp);
                    cctpSrc.setType(InterfaceType.NNI);
                    CoCoTerminationPoint cctpDst = dst.getTp(dstTp);
                    cctpDst.setType(InterfaceType.NNI);
                }

            }
        } catch (Exception e) {
            log.trace(e.getMessage());
        }

        try {

            String siteName;
            CoCoNode site;
            CoCoLink srcdst;
            CoCoLink dstsrc;
            CoCoNode src;
            CoCoNode dst;
            //int MplsLabel = 5100;

            // add sites to graph
            for (NetworkSite s : networkSites) {
            	log.info("Topology - setting up site " + s.getName());
                siteName = s.getName();
                site = new CoCoNode(siteName);
                // Node type is Customer Edge
                site.setType(NodeType.CE);
                site.setVlan(Integer.toOctalString(s.getVlanId()));
                site.setIpv4Prefix(s.getIpv4Prefix());
                site.setMac(s.getMacAddress());
                nodeMap.put(s.getName(), site);
                nodeMap.get(s.getProviderSwitch()).setType(NodeType.PE);
                
                site.setPeSwitch(s.getProviderSwitch());
                
                CoCoNode currentSwitch = nodeMap.get(s.getProviderSwitch());
               
                for (NetworkSwitch netSwitch : networkSwitches) {
                	if (netSwitch.getName().equalsIgnoreCase(currentSwitch.getId())) {
                		currentSwitch.setPeMplsLabel(Integer.toString(netSwitch.getMplsLabel()));
                	}
                }
                
                //curentSwitch.setPeMplsLabel(Integer.toString(MplsLabel++));
                
                
                srcdst = new CoCoLink(siteName, siteName, siteName
                        + Integer.toHexString(s.getCustomerPort()),
                        s.getProviderSwitch(), s.getProviderSwitch()
                                + s.getProviderPort());
                srcdst.setSrcTpNr(Integer.toString(s.getCustomerPort()));
                srcdst.setDstTpNr(Integer.toString(s.getProviderPort()));
                dstsrc = new CoCoLink(siteName, s.getProviderSwitch(),
                        s.getProviderSwitch() + s.getProviderPort(), siteName,
                        siteName + Integer.toString(s.getCustomerPort()));
                dstsrc.setSrcTpNr(Integer.toString(s.getProviderPort()));
                dstsrc.setDstTpNr(Integer.toString(s.getCustomerPort()));
                src = nodeMap.get(siteName);
                dst = nodeMap.get(s.getProviderSwitch());
                graph.addVertex(src);
                graph.addEdge(src, dst, srcdst);
                graph.setEdgeWeight(srcdst, edgeWeight);
                srcdst.weight = edgeWeight;
                edges.add(srcdst);
                graph.addEdge(dst, src, dstsrc);
                graph.setEdgeWeight(dstsrc, edgeWeight);
                dstsrc.weight = edgeWeight;
                edgeWeight = edgeWeight*2;
                edges.add(dstsrc);
                log.trace("addsite: " + src.getId() + " to "
                        + dst.getId());
            }

            log.trace("graph = " + graph.toString());
        } catch (Exception e) {
            log.trace(e.getMessage());
        }
        
        
       	for (CoCoNode node : nodeMap.values()) {
       		if (node.getType() == NodeType.CE) {
       			listOfEndNodes.add(node);
       		}
       	}

       	for (CoCoNode node : nodeMap.values()) {
       		if (node.getType() == NodeType.PE) {
       			listOfEdgeSwitches.add(node);
       		}
       	}
       	
        for (NetworkSwitch netSwitch : networkSwitchesWithEnni) {
        	log.debug("Setting up PE on network switch");
        	
        	CoCoNode switchWithEnni = nodeMap.get(netSwitch.getName());
        	log.debug("Setting up PE on network switch " + switchWithEnni.getId());
        	switchWithEnni.setType(NodeType.PE);
        	nodeMap.put(switchWithEnni.getId(), switchWithEnni);
        	listOfEdgeSwitches.add(switchWithEnni);
        }
       	
    }

    private String getPortName(String nodeId, String portId) {
        try {
            String r = this.restClient.getJSONPortInfo(nodeId, portId);

            JSONParser jsonParser = new JSONParser();
            JSONObject json = (JSONObject) jsonParser.parse(r);
            JSONArray nodeConnectorList = (JSONArray) json
                    .get("node-connector");
            for (int i = 0; i < nodeConnectorList.size(); i++) {
                JSONObject nodeConnector = (JSONObject) nodeConnectorList
                        .get(i);
                String name = (String) nodeConnector.get("id");
                if (name.equals(portId)) {
                    String portName = (String) nodeConnector
                            .get("flow-node-inventory:port-number");
                    return portName;
                }
            }

        } catch (Exception e) {
            log.trace(e.getMessage());
        }
        return null;
    }

    public void printGraph() {
        Set<CoCoNode> vertices = graph.vertexSet();
        Iterator<CoCoNode> iterator = vertices.iterator();
        while (iterator.hasNext()) {
            CoCoNode n = iterator.next();
            System.out.printf("Node %s: ", n.getId());
            Collection<CoCoTerminationPoint> termPoints = n.getTermPoints();
            Iterator<CoCoTerminationPoint> tpIter = termPoints.iterator();
            while (tpIter.hasNext()) {
                CoCoTerminationPoint tp = tpIter.next();
                System.out.printf("%s (%s) ", tp.getId(), tp.getType());
            }
            System.out.println();
        }
    }
    
    public Graph<CoCoNode, CoCoLink> getGraph() {
    	return graph;
    }

    public Set<String> getUniPorts() {
        // Collection<CoCoTerminationPoint> uniPorts;
        Set<String> uniPorts = new HashSet<String>();
        Collection<CoCoNode> nodes = nodeMap.values();
        Iterator<CoCoNode> iter = nodes.iterator();
        while (iter.hasNext()) {
            CoCoNode n = iter.next();
            Collection<CoCoTerminationPoint> termPoints = n.getTermPoints();
            Iterator<CoCoTerminationPoint> i = termPoints.iterator();
            while (i.hasNext()) {
                CoCoTerminationPoint tp = i.next();

            }
        }
        return uniPorts;
    }

    public void addVpn(String sites[]) {
        try {
            int sequence = 1;
            // String sites[] = vpn.getSites();
            CoCoVPN vpn = new CoCoVPN();
            vpn.setSites(sites);
            for (int i = 0; i < sites.length; i++) {
                for (int j = 0; j < sites.length; j++) {
                    if (i != j) {
                        log.trace(String.format("find path %s to %s\n", sites[i], sites[j]));
                        CoCoNode src = nodeMap.get(sites[i]);
                        src.setInUse(true);
                        CoCoNode dst = nodeMap.get(sites[j]);
                        // log.trace("getpath site1 " + src);
                        // log.trace("getpath site3 " + dst);
                        // DijkstraShortestPath<CoCoNode, CoCoLink> path = new
                        // DijkstraShortestPath<CoCoNode, CoCoLink>(
                        // graph, src, dst);
                        // List<CoCoLink> edges = path.getPathEdgeList();
                        List<CoCoLink> newEdges = new ArrayList<CoCoLink>();
                        List<CoCoLink> edges = DijkstraShortestPath
                                .findPathBetween(graph, src, dst);
                        log.trace("edges = " + edges);
                        newEdges.addAll(edges);
                        newEdges.addAll(vpn.getEdges());
                        vpn.setEdges(newEdges);
                        String inPort = null;
                        Flow flow;
                        String f;
                        Iterator<CoCoLink> iter = edges.iterator();
                        while (iter.hasNext()) {
                            CoCoLink edge = iter.next();

                            log.trace(String.format(
                                    "path link: %s port %s to %s port %s\n",
                                    edge.getSrcNode(), edge.getSrcTpNr(),
                                    edge.getDstNode(), edge.getDstTpNr()));

                            if (inPort != null) {
                                CoCoNode s = nodeMap.get(edge.getSrcNode());
                                CoCoNode d = nodeMap.get(edge.getDstNode());

                                if (s.getType() == NodeType.PE) {
                                    // Provision Provider Edge switches
                                    if (s.getTp(edge.getSrcTp()).getType() == InterfaceType.NNI) {
                                        // Provision ingress flow
                                    	log.trace(String.format("flow rule on %s match inport %s, vlan %s, dst prefix %s, action set_vlan %s, add_mpls %s, output %s\n",
                                                        s.getId(), inPort,
                                                        src.getVlan(),
                                                        dst.getIpv4Prefix(),
                                                        vpn.getVpnVlanId(),
                                                        dst.getPeMplsLabel(),
                                                        edge.getSrcTpNr()));
                                        flow = new Flow(s.getId(), flowId);
                                        flow.inPort(inPort);
                                        flow.matchVlan(src.getVlan());
                                        flow.matchEthertype(0x0800);
                                        flow.matchDstIpv4Prefix(dst
                                                .getIpv4Prefix());
                                        flow.modVlan(String.valueOf(vpn
                                                .getVpnVlanId()));
                                        // only push MPLS label if next switch
                                        // is a P switch
                                        if (d.getType() == NodeType.P) {
                                            flow.pushPeMplsLabel(dst
                                                    .getPeMplsLabel());
                                        }
                                        flow.setDstMAC(dst.getMac());
                                        flow.outPort(edge.getSrcTpNr());
                                        f = flow.buildFlow();
                                        this.restClient.sendtoSwitch(s.getId(),
                                                "add", f,
                                                String.valueOf(flowId));
                                    } else {
                                        // Provision egress flow
                                    	log.trace(String.format("flow rule on %s match inport %s, mpls %s, action set_vlan %s, pop_mpls, set_mac %s, output %s\n",
                                                        s.getId(), inPort,
                                                        dst.getPeMplsLabel(),
                                                        dst.getVlan(),
                                                        dst.getMac(),
                                                        edge.getSrcTpNr()));
                                        flow = new Flow(s.getId(), flowId);
                                        flow.inPort(inPort);
                                        flow.matchEthertype(0x0800);
                                        flow.matchDstIpv4Prefix(dst
                                                .getIpv4Prefix());
                                        // flow.matchMplsLabel(dst
                                        // .getPeMplsLabel());
                                        // flow.popMplsLabel();
                                        flow.modVlan(dst.getVlan());
                                        // flow.setDstMAC(dst.getMac());
                                        flow.outPort(edge.getSrcTpNr());
                                        f = flow.buildFlow();
                                        this.restClient.sendtoSwitch(s.getId(),
                                                "add", f,
                                                String.valueOf(flowId));
                                    }
                                } else {
                                    // Provision Provider (P) switches
                                	log.trace(String.format("flow rule on %s match %s, action to %s\n",
                                                    s.getId(),
                                                    dst.getPeMplsLabel(),
                                                    edge.getSrcTpNr()));
                                    flow = new Flow(s.getId(), flowId);
                                    flow.inPort(inPort);
                                    flow.matchEthertype(0x8847);
                                    flow.matchMplsLabel(dst.getPeMplsLabel());
                                    // rvdp test begin
                                    // flow.matchVlan(src.getVlan());
                                    // flow.matchDlDst(dst.getMac());
                                    // rvdp test end
                                    if (d.getType() == NodeType.PE) {
                                        // remove MPLS label when forwarding to
                                        // P switch
                                        flow.popTwoMplsLabels();
                                    }
                                    flow.outPort(edge.getSrcTpNr());
                                    f = flow.buildFlow();
                                    this.restClient.sendtoSwitch(s.getId(), "add",
                                            f, String.valueOf(flowId));
                                }
                            }
                            inPort = edge.getDstTpNr();
                            sequence++;
                            flowId++;
                        }
                    }
                }
            }
            activeVpn = vpn.getId();
            vpns.add(vpn);

        } catch (Exception e) {
            log.trace(e.getMessage());
        }
    }

    public List<CoCoLink> calculatePath(String fromSwitch, String toSwitch) {
        List<CoCoLink> path = new ArrayList<CoCoLink>();
        log.trace(String.format("find path %s to %s\n", fromSwitch, toSwitch));
        
        DefaultDirectedWeightedGraph<CoCoNode, CoCoLink> tempGraph = (DefaultDirectedWeightedGraph<CoCoNode, CoCoLink>) graph.clone();
        
        try {
            CoCoNode src = nodeMap.get(fromSwitch);
            CoCoNode dst = nodeMap.get(toSwitch);

           	for (CoCoNode node : listOfEndNodes) {
           		if (!node.getId().equalsIgnoreCase(fromSwitch) && !node.getId().equalsIgnoreCase(toSwitch)) {
           			tempGraph.removeVertex(node);
           		}
           	}

           	
           	for (CoCoNode node : listOfEdgeSwitches) {
           		
           		if ((src.getType() != NodeType.PE && !src.getPeSwitch().equals(node.getId())) && dst.getType() != NodeType.PE && !dst.getPeSwitch().equals(node.getId())) {
           			tempGraph.removeVertex(node);
           		}
           	}

            path = DijkstraShortestPath.findPathBetween(tempGraph, src, dst);
        } catch (Exception e) {
            log.trace(e.getMessage());
        }
        return path;
    }

    public void removeAllVpns() {
        // set all nodes in the VPNs to free
        for (CoCoVPN vpn : vpns) {
            for (String site : vpn.getSites()) {
                vpn.resetCounter();
                nodeMap.get(site).setInUse(false);
            }
        }
        vpns.clear();
        this.restClient.clearAll();
        flowId = 1;
    }

    public List<CoCoVPN> getVpnList() {
        return vpns;
    }

    public List<CoCoLink> getActiveVpnEdges() {
        List<CoCoLink> list = null;
        for (CoCoVPN vpn : vpns) {
            if (vpn.getId() == activeVpn) {
                list = vpn.getEdges();
            }
        }
        return list;
    }

    public List<CoCoNode> getNodes() {
        List<CoCoNode> list;
        list = new ArrayList<CoCoNode>(nodeMap.values());
        return list;
    }

    public CoCoNode getNode(String name) {
        return nodeMap.get(name);
    }

    public List<CoCoLink> getEdges() {
        return edges;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getActiveVpn() {
        return activeVpn;
    }

    public void setActiveVpn(int activeVpn) {
        this.activeVpn = activeVpn;
    }

}
