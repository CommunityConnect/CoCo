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
    // DB mapping
    private int owner_id;
    private int domain_id;
    // TODO object mapping done in VpnsService
    private User owner;
    private Domain domain;
    
    private static final String pathProtectionOnFromPortal = "true";
    
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
    
    public int getOwner_id() {
    	if (this.owner != null){
    		return this.owner.getId();
    	}
		return owner_id;
	}

	public void setOwner_id(int owner_id) {
		this.owner_id = owner_id;
		this.owner = null;
	}

	public int getDomain_id() {
		if (domain != null){
    		return domain.getId();
    	}
		return domain_id;
	}

	public void setDomain_id(int domain_id) {
		this.domain_id = domain_id;
		this.domain = null;
	}

	public User getOwner() {
		return owner;
	}

	public void setOwner(User owner) {
		this.owner = owner;
		this.owner_id = owner.getId();
	}

	public Domain getDomain() {
		return domain;
	}

	public void setDomain(Domain domain) {
		this.domain = domain;
		this.domain_id = domain.getId();
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
