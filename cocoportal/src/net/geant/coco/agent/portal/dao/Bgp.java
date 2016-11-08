package net.geant.coco.agent.portal.dao;

import java.io.Serializable;

public class Bgp implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4934166879580438037L;
	
	// id of bgp
	private int id = -1;
	// user hash #($inviteeID$nonce$target)
	private String hash;
	// bgp nonce from sender
	private String nonce;
	// bgp target 0002$AS$VPNID
	private String target;
	// always local domain
	private int local_domain_id = -1;
	private Domain local_domain;
	// always remote domain
	private int remote_domain_id = -1;
	private Domain remote_domain;
	// is the bgp allocated to a local VPN
	private int vpn_id = -1;
	private Vpn vpn;
	// is a local subnet allocated to the vpn
	private int subnet_id = -1;
	private Subnet subnet;
	// remote ipv4 unicast subnet identifier
	private String announce;
	
	public Bgp(){
		
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getHash() {
		return hash;
	}
	public void setHash(String hash) {
		this.hash = hash;
	}
	public String getNonce() {
		return nonce;
	}
	public void setNonce(String nonce) {
		this.nonce = nonce;
	}
	public int getLocal_domain_id() {
		return local_domain_id;
	}
	public void setLocal_domain_id(int own_domain_id) {
		this.local_domain_id = own_domain_id;
		this.local_domain = null;
	}
	public Domain getLocal_domain() {
		return local_domain;
	}
	public void setLocal_domain(Domain own_domain) {
		this.local_domain = own_domain;
		this.local_domain_id = own_domain.getId();
	}
	public int getRemote_domain_id() {
		return remote_domain_id;
	}
	public void setRemote_domain_id(int bgp_domain_id) {
		this.remote_domain_id = bgp_domain_id;
		this.remote_domain = null;
	}
	public Domain getRemote_domain() {
		return remote_domain;
	}
	public void setRemote_domain(Domain bgp_domain) {
		this.remote_domain = bgp_domain;
		this.remote_domain_id = bgp_domain.getId();
	}
	public int getVpn_id() {
		return vpn_id;
	}
	public void setVpn_id(int vpn_id) {
		this.vpn_id = vpn_id;
		this.vpn = null;
	}
	public Vpn getVpn() {
		return vpn;
	}
	public void setVpn(Vpn vpn) {
		this.vpn = vpn;
		this.vpn_id = vpn.getId();
	}
	public int getSubnet_id() {
		return subnet_id;
	}
	public void setSubnet_id(int subnet_id) {
		this.subnet_id = subnet_id;
		this.subnet = null;
	}
	public Subnet getSubnet() {
		return subnet;
	}
	public void setSubnet(Subnet subnet) {
		this.subnet = subnet;
		this.subnet_id = subnet.getId();
	}
	public String getTarget() {
		return target;
	}
	public void setTarget(String target) {
		this.target = target;
	}
	public String getAnnounce() {
		return announce;
	}
	public void setAnnounce(String announce) {
		this.announce = announce;
	}
}
