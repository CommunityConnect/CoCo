package net.geant.coco.agent.portal.controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.parser.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;
import net.geant.coco.agent.portal.dao.Bgp;
import net.geant.coco.agent.portal.dao.NetworkSite;
import net.geant.coco.agent.portal.dao.Subnet;
import net.geant.coco.agent.portal.dao.SubnetDao;
import net.geant.coco.agent.portal.dao.User;
import net.geant.coco.agent.portal.dao.UserDao;
import net.geant.coco.agent.portal.dao.Vpn;
import net.geant.coco.agent.portal.dao.VpnDao;
import net.geant.coco.agent.portal.dao.VpnInvite;
import net.geant.coco.agent.portal.dao.VpnInviteAccept;
import net.geant.coco.agent.portal.rest.RestVpnURIConstants;
import net.geant.coco.agent.portal.service.BpgService;
import net.geant.coco.agent.portal.service.NetworkSitesService;
import net.geant.coco.agent.portal.service.TopologyService;
import net.geant.coco.agent.portal.service.UsersService;
import net.geant.coco.agent.portal.service.VpnsService;
import net.geant.coco.agent.portal.utils.CoCoMail;

@Slf4j
@RestController
@Configuration
@PropertySource("classpath:/net/geant/coco/agent/portal/props/config.properties")
public class PortalControllerIntent {

	@Autowired
	Environment env;

	private VpnsService vpnsService;
	private TopologyService topologyService;
	private BpgService bgpService;
	private UsersService userService;
	private SubnetDao subnetDao;
	private NetworkSitesService networkSiteService;
	
	boolean networkChanged = false;

	@Autowired
	public void setUserService(UsersService userService) {
		this.userService = userService;
	}
	
	@Autowired
	public void setVpnsService(VpnsService vpnsService) {
		this.vpnsService = vpnsService;
	}

	@Autowired
	public void setTopologyService(TopologyService topologyService) {
		this.topologyService = topologyService;
	}
	
	@Autowired
	public void setSubnetDao(SubnetDao subnetDao) {
		this.subnetDao = subnetDao;
	}
	
	@Autowired
	public void setBgpService(BpgService bgpService) {
		this.bgpService = bgpService;
	}
	
	@Autowired
	public void setNetworkSiteService(NetworkSitesService networkSiteService) {
		this.networkSiteService = networkSiteService;
	}

	private User getCurrentUser(){
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
	    
		String userName = "Null";
		
		if (auth.isAuthenticated()){
			userName = auth.getName(); //get logged in username
		} else {
			return null;
		}
		
		return userService.getUser(userName);
	}
	
	@RequestMapping(value="/emailtest", method = RequestMethod.GET)
	public @ResponseBody String emailtest() {
		String recipient = "SimonGunkel@googlemail.com";
		String mail_subject = "Test Mail From CoCo Service";
		String mail_message = "Hello Simon \n\n This is a testmail. \n Best regards!";
		
		boolean result = CoCoMail.sendMail(recipient, mail_subject, mail_message);
				
		return "The mail was send " + result;
	}
	
	@RequestMapping(value="/userdaotest", method = RequestMethod.GET)
	public @ResponseBody String userdaotest() {
		
		List<User> users = userService.getUsers();
		
		String users_string = "users <br>";

		for (User user : users){
			users_string += "USER: " + user.toString() + " <br>";
		}
				
		return users_string;
	}
	
	@RequestMapping(value="/static/getAllUsers", method = RequestMethod.GET)
	public @ResponseBody String getAllUsers() {
		List<User> users = this.userService.getUsers();
		
		//ArrayList<Map<String, String>> user_list = new ArrayList<Map<String, String>>();
		Map<String, String> user_strings = new HashMap<String, String>();
		
		for (User user : users){
			user_strings.put( user.getEmail(), user.getName() );
		}
		
	    JSONObject json = new JSONObject();
	    
	    json.putAll( user_strings );
	    //System.out.printf( "JSON: %s", json.toString() );
			
		return json.toString();
	}
	
	@RequestMapping(value = RestVpnURIConstants.GET_USER_SUBNETS, method = RequestMethod.GET)
	public List<Subnet> getUserSubnets() {
		//log.info("Start getall sites.");
		
		User user = this.getCurrentUser();

		return subnetDao.getUserSubnets(user);
	}
	
		
	@RequestMapping(value = RestVpnURIConstants.GET_TOPOLOGY_VIS, method = RequestMethod.GET)
	public String getTopologyVis() {
		log.info("getTopologyVis");
		
		return topologyService.getTopologyJsonVis();
	}

	@RequestMapping(value = RestVpnURIConstants.GET_TOPOLOGY_IS_CHANGED, method = RequestMethod.GET)
	public boolean getTopologyChange() {

		if (networkChanged == true) {
			networkChanged = false;
			return true;
		}

		return networkChanged;
	}
	
	@RequestMapping(value = RestVpnURIConstants.GET_VPN, method = RequestMethod.GET)
	public Vpn getVpn(@PathVariable("id") int vpnId) {
		log.info("getVpn ID=" + vpnId);
		return vpnsService.getVpn(vpnId);
	}

	@RequestMapping(value = RestVpnURIConstants.UPDATE_VPN, method = RequestMethod.POST)
	public Vpn updateVpn(@PathVariable("id") int vpnId, @RequestBody Vpn vpn) {
		log.info("updateVpn ID=" + vpnId);

		Assert.isTrue(vpn.getId() == vpnId, "VPN id and ID from the rest path are not the same. REST PATH:"
				+ String.valueOf(vpnId) + " VPN ID" + String.valueOf(vpn.getId()));

		Vpn vpnNew = vpn;
		Vpn vpnCurrent = vpnsService.getVpn(vpnId);

		// Note: it is very important to create a new List here to make this work!
		List<NetworkSite> sitesToAdd = new ArrayList<NetworkSite>(vpnNew.getSites());
		sitesToAdd.removeAll(vpnCurrent.getSites());

		//TODO: something is going very wrong here :/
		
		List<NetworkSite> sitesToRemove = new ArrayList<NetworkSite>(vpnCurrent.getSites());
		sitesToRemove.removeAll(vpnNew.getSites());

		User user = this.getCurrentUser();
		
		for (NetworkSite site : sitesToAdd) {
			log.info("Add site = " + site);
			if (user != null) {
				vpnsService.addSiteToVpn(vpnCurrent.getName(), site.getName(), user.getId());
			} else {
				vpnsService.addSiteToVpn(vpnCurrent.getName(), site.getName());
			}
		}
		
		for (NetworkSite site : sitesToRemove) {
			log.info("Remove site = " + site);
			
			vpnsService.deleteSiteFromVpn(vpnCurrent.getName(), site.getName());
		}
		//return null;

		vpnNew = vpnsService.getVpn(vpnId);
		return vpnNew;
	}
	
	//TODO: Fix so that you only get VPN that you have access to, beside admin!?
	@RequestMapping(value = RestVpnURIConstants.GET_ALL_VPN, method = RequestMethod.GET)
	public List<Vpn> getAllVpns() {
		log.info("Start getAllVpns.");
		
		User current_user = this.getCurrentUser();
		
		if (current_user == null || current_user.isAdmin()){
			//TODO: this is a dev fallback if no user is looged in
			return vpnsService.getVpns();
		}
		
		return vpnsService.getVpns(current_user);
	}

	@RequestMapping(value = RestVpnURIConstants.GET_ALL_SITES, method = RequestMethod.GET)
	public List<NetworkSite> getSitesInVpn(@PathVariable("id") int vpnID) {
		log.info("Start getall sites.");

		return vpnsService.getSitesInVpn(vpnID);
	}

	@RequestMapping(value = RestVpnURIConstants.INVITE_VPN, method = RequestMethod.POST)
	public boolean inviteVpn(@RequestBody VpnInvite invite) {
		log.info("Invite user to VPN" + invite);
		
		User sender = this.getCurrentUser();
		//log.info("User " + sender);
		User receiver = userService.getUser(invite.getReceiver());
		
		Vpn vpn = vpnsService.getVpn(invite.getVpn());
		
		boolean is_subnet_in_vpn = false;
		// check if the subnet is in the VPN
		for (NetworkSite site : vpn.getSites()){
			if (site.getIpv4Prefix().equals(invite.getSubnet())){
				is_subnet_in_vpn = true;
				break;
			}
		}
		if (!is_subnet_in_vpn){
			log.error(String.format("ERROR in inviteVpn - subnet (%s) not in vpn (%s) ", invite.getSubnet(), vpn.toString()));
			return false;
		}
		
		Subnet subnet = subnetDao.getSubnet(invite.getSubnet());
		
		String url = "[url]?vpn=[vpn_id]&join=[hash]";
		
		url = url.replace("[url]", receiver.getDomain().getPortal_address());
		
		// lets check if both users are in the same domain
		if (sender.getDomain_id() == receiver.getDomain_id()){
			// the users are in the same domain
			url = url.replace("[vpn_id]", ""+vpn.getId());
			String hash = bgpService.createLocalHash( "" + vpn.getOwner_id(), "" + receiver.getId(), "" + vpn.getId());
			hash.hashCode();
			url = url.replace("[hash]", hash);
			
		} else {		
			// old code - we get the subnet from the user now - Subnet subnet = subnetDao.getSubnet(vpn.getSites().get(0).getIpv4Prefix());
			if (subnet != null){
				String hash = bgpService.annouceRoute(sender, receiver, subnet, vpn);
				
				if (hash == null){
					return false;
				}
				
				url = url.replace("[vpn_id]", "BGP");
				url = url.replace("[hash]", hash);
				
			} else {
				return false;
			}
		}
		
		
		log.info("New Invite {"+ invite +"} from {" + sender + "} to {" + receiver + "} url {"+ url +";");
		
		return false;
	}
	
	@RequestMapping(value = RestVpnURIConstants.ACCEPT_VPN, method = RequestMethod.POST)
	public boolean inviteAcceptVpn(@RequestBody VpnInviteAccept accept) {
		
		User user = this.getCurrentUser();
		Vpn vpn = vpnsService.getVpn(accept.getVpn());
		String hash = accept.getHash();
		NetworkSite site = networkSiteService.getNetworkSite(accept.getSubnet());
		Subnet subnet = subnetDao.getSubnet(accept.getSubnet());
		
		Bgp bgp = bgpService.getBgp(hash);
		
		if (bgp == null){
			// we are in the same netowork / check if we have the correct hash
			String comp_hash = bgpService.createLocalHash("" + vpn.getOwner_id(), "" + user.getId(), "" + accept.getVpn());
			if (comp_hash.equals(hash)){
				vpn.getSites().add(site);
				updateVpn(vpn.getId(), vpn);
			} else {
				// THIS IS AN ERROR - HASH DOES NOT MATCH
				log.error(String.format("ERROR inviteAcceptVpn - HASH DOES NOT MATCH req:%s !- comp:%s",hash, comp_hash));
			}
		} else {
			// we have to setup a bgp connection
			// hash is matching the bgp -> lets proceed
			// TODO: Check if the vpn does not match
			
			//if (vpn.getId() != bgp.getVpn_id()){
			//	log.error(String.format("ERROR inviteAcceptVpn - VPN does not match accept.vpn = %d != bgp.vpn = %d", vpn.getId(), bgp.getVpn_id()));
			//}
			
			if (bgpService.acceptRoute(user, subnet, vpn, hash, bgp)){
				vpn = bgp.getVpn();
				vpn.getSites().add(site);
				updateVpn(vpn.getId(), vpn);
			}
		}
		
		return true;
	}
	
	@RequestMapping(value = RestVpnURIConstants.CREATE_VPN, method = RequestMethod.POST)
	public boolean createVpn(@RequestBody Vpn vpn) {
		log.info("Start createVpn.");
		
		User user = this.getCurrentUser();
		
		vpn.setDomain_id(user.getDomain_id());
		vpn.setOwner_id(user.getId());
		
		return vpnsService.createVpn(vpn);
	}

	// TODO: Check if you are the right user of the VPN or Admin
	@RequestMapping(value = RestVpnURIConstants.DELETE_VPN, method = RequestMethod.POST)
	public boolean deleteVpn(@PathVariable("id") int vpnId) {
		log.info("Start deleteVpn.");
		
		return vpnsService.deleteVpn(vpnId);
	}
	
	@RequestMapping(value = RestVpnURIConstants.UPDATE_BGP, method = RequestMethod.POST)
	public boolean updateBgp(@RequestBody String json_bgp) {
		
		return bgpService.bgpServerUpdate(json_bgp);
	}
	
}
