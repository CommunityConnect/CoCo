package net.geant.coco.agent.portal.dao;

import java.io.Serializable;
import java.util.List;

public class Domain implements Serializable {

		
	/**
	 * 
	 */
	private static final long serialVersionUID = -5322380691835288515L;
	
	private int id;
    private String bgp_peer;
    private String portal_address;
    private String bgp_ip;
    private int as_num;
    private String as_name;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getBgp_peer() {
		return bgp_peer;
	}

	public void setBgp_peer(String bgp_peer) {
		this.bgp_peer = bgp_peer;
	}

	public String getPortal_address() {
		return portal_address;
	}

	public void setPortal_address(String portal_address) {
		this.portal_address = portal_address;
	}

	public String getBgp_ip() {
		return bgp_ip;
	}

	public void setBgp_ip(String bgp_ip) {
		this.bgp_ip = bgp_ip;
	}

	public int getAs_num() {
		return as_num;
	}

	public void setAs_num(int as_num) {
		this.as_num = as_num;
	}

	public String getAs_name() {
		return as_name;
	}

	public void setAs_name(String as_name) {
		this.as_name = as_name;
	}
	
}
