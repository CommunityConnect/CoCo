package net.geant.coco.agent.portal.utils;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class CoCoNode implements Comparable<CoCoNode> {

	private String id;
	private NodeType type = NodeType.P; /* Default type is Provider */
	private String vlan;
	private String mac;
	private String ipv4Prefix;
	private String peMplsLabel;
	private int xPos;
	private int yPos;
	private HashSet<String> flowIds = new HashSet<String>();
	private String colour;
	private boolean inUse = false;
	private HashMap<String, CoCoTerminationPoint> tpMap = new HashMap<String, CoCoTerminationPoint>();
	
	private String peSwitch;
	
	public String getPeSwitch() {
		return peSwitch;
	}

	public void setPeSwitch(String peSwitch) {
		this.peSwitch = peSwitch;
	}

	public CoCoNode(String id) {
		this.id = id;
	}
	
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        String NEW_LINE = "_";

        result.append(this.getId() + NEW_LINE);
        //result.append(this.getIpv4Prefix() + NEW_LINE);
        //result.append(this.getPeMplsLabel() + NEW_LINE);
        //result.append(this.getVlan() + NEW_LINE);
        
        return result.toString();
      }
	
	public Collection<CoCoTerminationPoint> getTermPoints() {
		return tpMap.values();
	}

	public void addTp(String id) {
		CoCoTerminationPoint tp = new CoCoTerminationPoint(id);
		this.tpMap.put(id, tp);
	}
	
	public CoCoTerminationPoint getTp(String id) {
		return tpMap.get(id);
	}


	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public NodeType getType() {
		return type;
	}

	public void setType(NodeType type) {
		this.type = type;
	}

	public String getVlan() {
		return vlan;
	}

	public void setVlan(String vlan) {
		this.vlan = vlan;
	}

	public String getMac() {
		return mac;
	}

	public void setMac(String mac) {
		this.mac = mac;
	}

	public String getIpv4Prefix() {
		return ipv4Prefix;
	}

	public void setIpv4Prefix(String ipv4Prefix) {
		this.ipv4Prefix = ipv4Prefix;
	}

	public String getPeMplsLabel() {
		return peMplsLabel;
	}

	public void setPeMplsLabel(String peMplsLabel) {
		this.peMplsLabel = peMplsLabel;
	}

	public int getxPos() {
		return xPos;
	}

	public void setxPos(int xPos) {
		this.xPos = xPos;
	}
	
	public void emptyFlowIds() {
	    flowIds.clear();
	}
	
	public void addToFlowIds(String id) {
	    flowIds.add(id);
	}
	
	public void deleteFromFlowIds(String id) {
	    flowIds.remove(id);
	}
	
	public HashSet<String> getFlowIds() {
	    return flowIds;
	}

	public int getyPos() {
		return yPos;
	}

	public void setyPos(int yPos) {
		this.yPos = yPos;
	}

	public String getColour() {
		return colour;
	}

	public void setColour(String colour) {
		this.colour = colour;
	}

	public boolean isInUse() {
		return inUse;
	}

	public void setInUse(boolean inUse) {
		this.inUse = inUse;
	}

	public int compareTo(CoCoNode o) {
		return this.id.compareTo(o.id);
	}
	
}
