package net.geant.coco.agent.portal.dao;

public class NetworkElement {

	private String name;
	private int id;
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public enum NODE_TYPE {
		CUSTOMER, SWITCH, EXTERNAL_AS
	}
	
	public NODE_TYPE nodeType;
	
	//private List<NetworkInterface> interfaces;
	
	public NetworkElement() {

	}
	
	public NetworkElement(int id, String name, NODE_TYPE nodeType) {
		this.id = id;
		this.name = name;
		this.nodeType = nodeType;
		//interfaces = new ArrayList<NetworkInterface>();
	}
	
	@Override
	public boolean equals(Object other) {
	    if (!(other instanceof NetworkElement)) {
	        return false;
	    }

	    NetworkElement that = (NetworkElement) other;

	    // Custom equality check here.
	    return (this.id == that.id)
	        && this.name.equals(that.name)
	        && (this.nodeType == that.nodeType);
	}
	
	@Override
	public int hashCode() {
	    int hashCode = 1;

	    hashCode = hashCode * 37 + this.id;
	    hashCode = hashCode * 37 + this.name.hashCode();
	    hashCode = hashCode * 37 + this.nodeType.hashCode();
	    
	    return hashCode;
	}
}
