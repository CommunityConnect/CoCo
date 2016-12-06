package net.geant.coco.agent.portal.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.geant.coco.agent.portal.dao.Bgp;
import net.geant.coco.agent.portal.dao.BgpDao;
import net.geant.coco.agent.portal.dao.Domain;
import net.geant.coco.agent.portal.dao.DomainDao;
import net.geant.coco.agent.portal.dao.NetworkSite;
import net.geant.coco.agent.portal.dao.Subnet;
import net.geant.coco.agent.portal.dao.SubnetDao;
import net.geant.coco.agent.portal.dao.User;
import net.geant.coco.agent.portal.dao.Vpn;
import net.geant.coco.agent.portal.dao.VpnDao;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service("bgpService")
public class BpgService {
    private BgpDao bgpDao;
    private DomainDao domainDao;
    private SubnetDao subnetDao;
    private UsersService usersService;
    private NetworkSitesService networkSitesService;
    private VpnsService vpnsService;

    final private static String BGPSERVER = "http://10.10.10.1:5001/";
    
    @Autowired
	public void setBgpDao(BgpDao bgpDao) {
		this.bgpDao = bgpDao;
	}
    
    @Autowired
    public void setDomainDao(DomainDao domainDao) {
		this.domainDao = domainDao;
	}

    @Autowired
	public void setSubnetDao(SubnetDao subnetDao) {
		this.subnetDao = subnetDao;
	}
    
    @Autowired
	public void setUsersService(UsersService usersService) {
		this.usersService = usersService;
	}
    
    @Autowired
	public void setNetworkSitesService(NetworkSitesService networkSitesService) {
		this.networkSitesService = networkSitesService;
	}
    
    @Autowired
	public void setVpnsService(VpnsService vpnsService) {
		this.vpnsService = vpnsService;
	}

	private Bgp setupBgp(Bgp bgp){
    	if (bgp == null){
    		return null;
    	}
    	
    	if (bgp.getLocal_domain_id() >= 0){
			bgp.setLocal_domain(domainDao.getDomain(bgp.getLocal_domain_id()));
		}
		if (bgp.getRemote_domain_id() >= 0){
			bgp.setRemote_domain(domainDao.getDomain(bgp.getRemote_domain_id()));
		}
		if (bgp.getVpn_id() >= 0){
			bgp.setVpn(vpnsService.getVpn(bgp.getVpn_id()));
		}
		if (bgp.getSubnet_id() >= 0){
			// we need to check if a subnet exists, as it is not required
			Subnet sub = subnetDao.getSubnet(bgp.getSubnet_id());
			if (sub != null){
				bgp.setSubnet(sub);
			}
		}
		
		return bgp;
    }
    
    public List<Bgp> getBgps() {
    	List<Bgp> bgps = bgpDao.getBgps();
    	for (Bgp bgp : bgps){
    		this.setupBgp(bgp);
    	}
    	return bgps;
    }
    
    public List<Bgp> getBgps(int vpn_id) {
    	List<Bgp> bgps = bgpDao.getBgps(vpn_id);
    	for (Bgp bgp : bgps){
    		this.setupBgp(bgp);
    	}
    	return bgps;
    }
    
    
    
    public Bgp getBgpByTarget(String target) {
    	return this.setupBgp(bgpDao.getBgpByTarget(target));
    }
    
    public Bgp getBgp(int bgp_id) {
    	return this.setupBgp(bgpDao.getBgp(bgp_id));
    }
    
    private String generateNonce(){
    	// nonce prefix + random hex string
    	String nonce_prefix = "C0C0";
    	String hex = BpgService.getRandomHexString(6);
    	
    	return String.format("%s%s", nonce_prefix, hex) ;
    }
    
    private String createTarget(int as_num, int vpn_id){
    	// we need 0x00 02 XXXX YYYYYYYY
    	// 1st byte is empty
    	// 2nd byte is the indentifier for route target
    	// 3+4th byte is for autonomous system number
    	// rest 4 bytes are for vpn ID 
    	String target_prefix = "0002";
    	
    	String as_num_str = Integer.toHexString(as_num);
    	// lets cut the HEX to 2 byte -> 4 HEX characters
    	as_num_str = as_num_str.substring(as_num_str.length()-4);
    	String vpn_id_str  = Integer.toHexString(vpn_id);
    	String empty = "00000000";
    	// lets fill the HEX to 4 byte -> 8 HEX characters
    	vpn_id_str = empty.substring(vpn_id_str.length()) + vpn_id_str;
    	
    	return String.format("%s%s%s", target_prefix, as_num_str, vpn_id_str).toUpperCase();
    }
    
    public String createBGPHash(String id, String nonce, String target){
    	//a.	invitee’s ID
    	//b.	6B nonce N1
    	//c.	VPN Route Target
    	
    	String complete_string = String.format("%s%s%s", id.toUpperCase(), nonce.toUpperCase(), target.toUpperCase());
    	int hash_int = complete_string.hashCode();
    	
    	String hash = Integer.toHexString(hash_int);
    	
    	log.debug(String.format("Create new hash - %s - hash - %s", complete_string, hash));
    	
    	return hash.toUpperCase();
    }
    
    public String createLocalHash(String owner_id, String invitee_id, String vpn_id){
    	String complete_string = String.format("%s%s%s", owner_id.toUpperCase(), invitee_id.toUpperCase(), vpn_id.toUpperCase());
    	int hash_int = complete_string.hashCode();
    	
    	String hash = Integer.toHexString(hash_int);
    	
    	return hash.toUpperCase();
    }
    
    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
    
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
    
    public static String getRandomHexString(int numOfBytes){
        byte [] bytes = new byte [numOfBytes];
    	
        Random r = new Random();
        r.nextBytes(bytes);
        
        return bytesToHex(bytes);
    }
    
    private boolean cURL(String url, String parameter){
    	log.debug(url + " - " + parameter);
//    	if (true){
//    		log.debug(url + " - " + parameter);
//    		return true;
//    	}
    	
    	try {
    		// NOTE: dont use " for the parameter in @ProcessBuilder
    		ProcessBuilder pb = new ProcessBuilder("curl", "--form", parameter, url);
    		
    		pb.redirectErrorStream(true); // equivalent of 2>&1
    		Process p = pb.start();
    		
    		String curl_cmd = "curl --form \"" + parameter + "\" " + url;
    		//log.debug(curl_cmd);
			//Process p = Runtime.getRuntime().exec(curl_cmd);
    		
			log.debug("Waiting for curl to finish: " + curl_cmd);
		    p.waitFor();
			/* Note: if i want to read the process output i get stuck... */
		    InputStream is = p.getInputStream();
		    InputStreamReader isr = new InputStreamReader(is);
		    BufferedReader br = new BufferedReader(isr);
		    String line, lastline = null;

		    while ((line = br.readLine()) != null) {
		    	log.debug(line);
		    	lastline = line;
		    }
		    
		    if (lastline != null && lastline.contains("Success")){
		    	log.debug("CURL finished with success!");
		    } else {
		    	log.error("CURL failed!");
		    	return false;
		    }
			
		} catch (IOException | InterruptedException e) {
			log.error(e.getMessage());
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
//        // code to evaluate if the command was ok
//    	p.waitFor();
//
//        BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
//
//        StringBuffer sb = new StringBuffer();
//        String line = "";
//        while ((line = reader.readLine())!= null) {
//        	sb.append(line + "\n");
//        }
    	
        // java code - currently not used
//    	HttpURLConnection con;
//		try {
//			con = (HttpURLConnection) new URL(url).openConnection();
//			
//			con.setRequestMethod("POST");
//	    	con.getOutputStream().write(parameter.getBytes("UTF-8")); 
//	    	con.getInputStream();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
    	
    	return true;
    }
    
    public boolean acceptRoute(User invitee, Subnet subnet, Vpn vpn, String hash, Bgp bgp){
    	// this function is called if a user accepts an bgp invite
    	String bgp_hash = this.createBGPHash("" + invitee.getId(), bgp.getNonce(), bgp.getTarget());
    	
    	if (bgp_hash.equals(hash)){
    		bgp.setHash(bgp_hash);
    		
    		String neighbor = bgp.getRemote_domain().getBgp_ip();
        	String route = subnet.getSubnet();
        	String next_hop = bgp.getLocal_domain().getBgp_ip();
        	String target = bgp.getTarget(); //this.createTarget(bgp.getLocal_domain().getAs_num(), vpn.getId());
    		
        	//NOTE: we treat the hex value as string - add 0x before sending over curl
    		String parameter = String.format("command=neighbor %s announce route %s next-hop %s extended-community 0x%s extended-community 0x%s", neighbor, route, next_hop, bgp.getNonce(), target);
    		
    		return this.cURL(BGPSERVER, parameter);
    	}
    	else {
    		log.error("acceptRoute with wrong hash " + hash + " expected " + bgp_hash);
    	}
    	return false;
    }
    
    public Bgp annouceRoute(User creator, User invitee, Subnet subnet, Vpn vpn){
    	// this function is called invite another user via BGP
    	// curl dummy command
    	// curl --form "command=neighbor 10.3.0.254 announce route 10.2.1.128/25 next-hop 10.2.0.254 extended-community 0x0002FDE800000001 extended-community 0x8ABCBEEFDEADBEEF" http://10.10.10.1:5001/
    	
    	Domain local_domain = domainDao.getDomain(creator.getDomain_id());
    	Domain remote_domain = domainDao.getDomain(invitee.getDomain_id());
    	
    	String neighbor = remote_domain.getBgp_ip();
    	String route = subnet.getSubnet();
    	String next_hop = local_domain.getBgp_ip();
    	
    	// 6B nonce
    	String nonce = this.generateNonce();
    	
    	String target = this.createTarget(local_domain.getAs_num(), vpn.getId());
    	//
    	String hash = this.createBGPHash("" + invitee.getId(), nonce, target);
    	
    	//NOTE: we tread the hex value as string - add 0x before sending over curl
    	String parameter = String.format("command=neighbor %s announce route %s next-hop %s extended-community 0x%s extended-community 0x%s", neighbor, route, next_hop, nonce, target);
    	
    	//send the information to the 
    	if (this.cURL(BGPSERVER, parameter)){
    		Bgp new_bgp = new Bgp();
    		new_bgp.setHash(hash);
    		new_bgp.setLocal_domain(local_domain);
    		new_bgp.setRemote_domain(remote_domain);
    		new_bgp.setNonce(nonce);
    		new_bgp.setSubnet(subnet);
    		new_bgp.setVpn(vpn);
    		new_bgp.setTarget(target);
    		
    		bgpDao.updateBgp(new_bgp);
    		
    		return new_bgp;
    	}
    	
    	return null;
    }
    
    public boolean updateBgp(Bgp bgp) {
    	return bgpDao.updateBgp(bgp);
    }
    
    public boolean bgpServerUpdate(String update_json){
    	// first lets reformat the hex entries
    	Pattern pattern = Pattern.compile("[0-9]{11,20}");
    	Matcher matcher = pattern.matcher(update_json);
    	
    	while(matcher.find()){
    		String match = matcher.group();
    		BigInteger bigint = new BigInteger(match);
    		byte [] bytes = bigint.toByteArray();
    		String hex = bytesToHex(bytes);
    		log.debug(String.format("bgpServerUpdate - Found new match %s turn into \"%s\" !", match, hex));
    		
    		// reformat the hex to be correct and 16 char long!
    		if (hex.length() < 16){
    			String empty = "0000000000000000";
    	    	String new_hex = empty.substring(hex.length()) + hex;
    	    	log.debug(String.format("bgpServerUpdate - String is to short %d new hex = \"%s\" !", hex.length(), new_hex));
    	    	hex = new_hex;
    		} else if (hex.length() > 16){
    	    	String new_hex = hex.substring(hex.length()-16);
    	    	log.debug(String.format("bgpServerUpdate - String is to long %d new hex = \"%s\" !", hex.length(), new_hex));
    	    	hex = new_hex;
    		}
    		
    		update_json = update_json.replace(match, String.format("\"%s\"", hex));
    	}
    	
    	JSONParser parser = new JSONParser();
		try {
			JSONObject json = (JSONObject) parser.parse(update_json);
			String type = (String) json.get("type");
			//check if we got the correct message 
			if (type.equals("update")){
				
				//String version = (String) json.get("exabgp");
				//int time = (int) json.get("time");
				String host = (String) json.get("host");
				Map<String, Object> neighbor = (Map<String, Object>) json.get("neighbor");
				Map<String, String> address = (Map<String, String>) neighbor.get("address");
				// note: the bgp message has local as origin
				// 		 this local domain is the destination
				String peer_string = address.get("peer");
				String local_string = address.get("local");
				
				// find the subnet in the json
				pattern = Pattern.compile("[0-9]{1,3}.[0-9]{1,3}.[0-9]{1,3}.[0-9]{1,3}/[0-9]{1,3}");
				matcher = pattern.matcher(update_json);
		    	String subnet_string = "";
		    	while(matcher.find()){
		    		subnet_string = matcher.group();
		    	}

				
		    	if (neighbor.get("message") == null){
		    		log.warn("BGP update without 'message'");
		    		return false;
		    	}
				Map<String, Object> message = (Map<String, Object>) neighbor.get("message");
				if (message.get("update") == null){
		    		log.warn("BGP update without 'update'");
		    		return false;
		    	}
				Map<String, Object> update = (Map<String, Object>) message.get("update");
				if (update.get("attribute") == null){
		    		log.warn("BGP update without 'attribute'");
		    		return false;
		    	}
				Map<String, Object> attribute = (Map<String, Object>) update.get("attribute");
				if (attribute.get("extended-community") == null){
		    		log.warn("BGP update without 'extended-community'");
		    		return false;
		    	}
				List<Object> community = (List<Object>) attribute.get("extended-community");
				
				String target = null;
				String nonce = null;
				for (Object obj : community){
					String obj_hex = null;
					if (obj instanceof String){
						obj_hex = (String) obj;
						//TODO we have to check here if we really have a hex string
					}
					if (obj_hex != null){
						// lets check and correct the size of the hex string
						if (obj_hex.length() > 16){
							obj_hex = obj_hex.substring(obj_hex.length()-16);
						};
						
						
						// lets check weather the obj is the none or hash
						if (obj_hex.substring(0, 4).equals("C0C0") ){
							nonce = obj_hex;
						} else if (obj_hex.substring(0, 4).equals("0002")) {
							target = obj_hex;
						} else {
							log.error("extended-community parameter not recognized - " + obj_hex);
						}
					}
				}
				
				// First check if this is an accept or new offer
				// Thus check if we already have a BGP entry with this target - target is universal uinique ID
				if (target != null){
					Bgp bgp = bgpDao.getBgpByTarget(target);
					if (bgp != null){
						// this is an accept of an offer we sent
						log.info("got bgp update - accept of offer " + host + " - " + target + " - " + nonce);
						
						// update bgp - subnet
						bgp.setAnnounce(subnet_string);
						bgpDao.updateBgp(bgp);
						
						// add bgp site to VPN
						Vpn vpn = vpnsService.getVpn(bgp.getVpn_id());
						bgp.setVpn(vpn);
						
						NetworkSite bgpSite = networkSitesService.getExternalNetworkSite(bgp);
						
						// NOTE: we have to manually add the bgp site here, as it doesn't fit in the regular process
						vpnsService.addSiteToVpn(vpn, bgpSite);
						
						return true;
					}
				}
				
				if (target != null && nonce != null){
					log.info("got bgp update - " + host + " - " + target + " - " + nonce);
					
					Domain localdomain = domainDao.getDomainByBgp(local_string);
					
					Vpn vpn = new Vpn(String.format("BGP - %s", target));
					vpn.setDomain(localdomain);
					vpn.setFailoverType("slow-reroute");
					vpn.setPathProtection("false");
					User admin = usersService.getDomainAdmin(localdomain);
					
					if (admin == null){
						log.error("ERROR no admin for local domain with ID - " + localdomain.getId());
						return false;
					}
					
					vpn.setOwner(admin);
					if (vpnsService.createVpn(vpn)){
						vpn = vpnsService.getVpn(vpn.getName());
						
						Bgp new_bgp = new Bgp();
			    		new_bgp.setLocal_domain(localdomain);
			    		new_bgp.setRemote_domain(domainDao.getDomainByBgp(peer_string));
			    		new_bgp.setNonce(nonce);
			    		new_bgp.setTarget(target);
			    		new_bgp.setAnnounce(subnet_string);
			    		new_bgp.setVpn(vpn);
			    		
			    		return bgpDao.updateBgp(new_bgp);
					} else {
						log.error("ERROR VPN was not created - " + vpn.toString());
					}
				} else {
					log.error("ERROR something went wrong we have not a target and a nonce ");
				}
			} else {
				// lets ignore the message
				log.warn("BGP has wrong type - " + type);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
    }
}
