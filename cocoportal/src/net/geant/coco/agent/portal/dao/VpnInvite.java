package net.geant.coco.agent.portal.dao;

import java.io.Serializable;

public class VpnInvite implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1192325788306616872L;
	
	private int vpn = -1;
	private String receiver;
	private String invite_text;
	
	
	public int getVpn() {
		return vpn;
	}
	public void setVpn(int vpn) {
		this.vpn = vpn;
	}
	public String getReceiver() {
		return receiver;
	}
	public void setReceiver(String receiver) {
		this.receiver = receiver;
	}
	public String getInvite_text() {
		return invite_text;
	}
	public void setInvite_text(String invite_text) {
		this.invite_text = invite_text;
	}
	
	public String toString() {
		return "invite(" + String.valueOf(vpn) + "," + receiver + "," + invite_text + ")";
	}
}
