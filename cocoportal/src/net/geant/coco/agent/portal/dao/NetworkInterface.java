package net.geant.coco.agent.portal.dao;

public class NetworkInterface {

	public enum IF_TYPE {
		UNI, INNI, ENNI
	}
	
	public IF_TYPE ifType;
	public NetworkElement source;
	public NetworkElement neighbour;
	
	public NetworkInterface(NetworkElement source, NetworkElement neighbour, IF_TYPE ifType) {
		this.source = source;
		this.neighbour = neighbour;
		this.ifType = ifType;
	}

}
