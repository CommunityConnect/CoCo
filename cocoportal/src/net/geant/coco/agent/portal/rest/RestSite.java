package net.geant.coco.agent.portal.rest;

import net.geant.coco.agent.portal.dao.NetworkSite;
import net.geant.coco.agent.portal.dao.Vpn;

public class RestSite {

	private int id;
	private String name;

	 public RestSite() {
	    }
	    
    public RestSite(NetworkSite site) {
    	this.id = site.getId();
    	this.name = site.getName();
    }
	    
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

	public String toString() {
		return "site(" + String.valueOf(id) + "," + name + ")";
	}
	
	@Override
	public boolean equals(Object other){
	    if (other == null) return false;
	    if (other == this) return true;
	    if (!(other instanceof RestSite))return false;
	    RestSite otherMyClass = (RestSite)other;
	    if (this.id == otherMyClass.id && this.name.equals(otherMyClass.getName())) {
	    	return true;
	    }
	    else {
	    	return false;
	    }
	}

}
