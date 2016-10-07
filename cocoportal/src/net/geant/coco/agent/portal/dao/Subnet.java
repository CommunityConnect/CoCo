package net.geant.coco.agent.portal.dao;

import java.io.Serializable;
import java.util.List;

public class Subnet implements Serializable {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3600291337415822912L;
	
	private int id;
    private String subnet;
    
    //DB mapping
    private int site_id;
    // TODO object mapping done in @SubnetService
    private NetworkSite site;
    private List<Vpn> vpns;
    private List<User> users;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getSubnet() {
		return subnet;
	}

	public void setSubnet(String subnet) {
		this.subnet = subnet;
	}

	public int getSite_id() {
		return site_id;
	}

	public void setSite_id(int site_id) {
		this.site_id = site_id;
	}

	public NetworkSite getSite() {
		return site;
	}

	public void setSite(NetworkSite site) {
		this.site = site;
	}

	public List<Vpn> getVpns() {
		return vpns;
	}

	public void setVpns(List<Vpn> vpns) {
		this.vpns = vpns;
	}

	public List<User> getUsers() {
		return users;
	}

	public void setUsers(List<User> users) {
		this.users = users;
	}
}
