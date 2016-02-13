package net.geant.coco.agent.portal.rest;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.DateSerializer;

import net.geant.coco.agent.portal.dao.Vpn;
 
public class RestVpn implements Serializable{
 
    private static final long serialVersionUID = -7788619177798333712L;
     
    private int id;
    private String name;
    private List<RestSite> sites;
    private boolean isPublic;
    
    public RestVpn() {
    	this.id = -1;
    	this.name = "whereDoIUseThisActually?";
    	sites = new ArrayList<RestSite>();
    	isPublic = false;
    }
    
    public RestVpn(Vpn vpn) {
    	this.id = vpn.getId();
    	this.name = vpn.getName();
    	sites = new ArrayList<RestSite>();
    	isPublic = false;
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
    
    public List<RestSite> getSites() {
        return sites;
    }
    
    public void setSites(List<RestSite> sites) {
        this.sites = sites;
    }
    
    public boolean getIsPublic() {
    	return this.isPublic;
    }
    
    public void setPublic(boolean isPublic) {
    	this.isPublic = isPublic;
    }
    
    public void addSiteToVpn(RestSite site) {
    	sites.add(site);
    }
    
    public void deleteSiteFromVpn(RestSite site) {
    	sites.remove(site);
    }
    
    public String toString() {
		return "vpn(" + String.valueOf(id) + "," + name + ",sites:"+ sites.toString() + ")";
	}
    
    /*
    @JsonSerialize(using=DateSerializer.class)
    public Date getCreatedDate() {
        return createdDate;
    }
    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }*/
     
     
}