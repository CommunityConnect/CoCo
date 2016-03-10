package net.geant.coco.agent.portal.dao;

import java.io.Serializable;
import java.util.List;

public class Vpn implements Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = -259509495211419615L;

    private int id;
    private String name;
    private String pathProtection;
    private String failoverType;
    private List<NetworkSite> sites;
    
    private static final String pathProtectionOnFromPortal = "on";
    
	public Vpn() {
    }
	
    public Vpn(String name) {
        super();
        this.name = name;
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
    
    public String getPathProtection() {
        return pathProtection;
    }
    
    public boolean getPathProtectionBoolean() {
    	if (getPathProtection().equals(pathProtectionOnFromPortal)) {
    		return true;
    	}
    	else {
    		return false;
    	}
    }

    public void setPathProtection(String pathProtection) {
        this.pathProtection = pathProtection;
    }
    
    public String getFailoverType() {
        return failoverType;
    }

    public void setFailoverType(String failoverType) {
        this.failoverType = failoverType;
    }
    
    public List<NetworkSite> getSites() {
        return sites;
    }
    
    public void setSites(List<NetworkSite> sites) {
        this.sites = sites;
    }

    public String toString() {
		return "vpn(" + String.valueOf(id) + "," + name + ",sites:"+ sites.toString() + ")";
	}
}
