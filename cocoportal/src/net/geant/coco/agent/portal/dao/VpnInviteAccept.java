package net.geant.coco.agent.portal.dao;

import java.io.Serializable;

public class VpnInviteAccept implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8479725335529598006L;
	
	
	private int vpn = -1;
	private String subnet;
	private String hash;
	private String target;
	
	
	public int getVpn() {
		return vpn;
	}
	public void setVpn(int vpn) {
		this.vpn = vpn;
	}
	public String getSubnet() {
		return subnet;
	}
	public void setSubnet(String subnet) {
		this.subnet = subnet;
	}
	public String getHash() {
		return hash;
	}
	public void setHash(String hash) {
		this.hash = hash;
	}
	public String getTarget() {
		return target;
	}
	public void setTarget(String target) {
		this.target = target;
	}
	
	public String toString() {
		return "VpnInviteAccept(" + String.valueOf(vpn) + "," + subnet + ","+ hash + ","+ target +")";
	}
}
