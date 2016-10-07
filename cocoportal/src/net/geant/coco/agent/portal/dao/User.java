package net.geant.coco.agent.portal.dao;

import java.io.Serializable;
import java.util.List;

public class User implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4863393123246349002L;
	
	private int id;
    private String name;
    private String email;
    //TODO fix domain and site
    private int domain_id;
    private int site_id;
    // object mapping
    private Domain domain;
    private NetworkSite site;
    
    private boolean admin;
    
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
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	
	public int getDomain_id() {
		if (domain != null){
			return domain.getId();
		}
		return domain_id;
	}
	public void setDomain_id(int domain_id) {
		this.domain_id = domain_id;
	}
	public int getSite_id() {
		if (site != null){
			return site.getId();
		}
		return site_id;
	}
	public void setSite_id(int site_id) {
		this.site_id = site_id;
	}
	public Domain getDomain() {
		return domain;
	}
	public void setDomain(Domain domain) {
		this.domain = domain;
	}
	public NetworkSite getSite() {
		return site;
	}
	public void setSite(NetworkSite site) {
		this.site = site;
	}
	public boolean isAdmin() {
		return admin;
	}
	public void setAdmin(boolean admin) {
		this.admin = admin;
	}
    
	public String toString() {
		return "user(" + String.valueOf(id) + "," + name + ","+ email + ")";
	}
	
}
