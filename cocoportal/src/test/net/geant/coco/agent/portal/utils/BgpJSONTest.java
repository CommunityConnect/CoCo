package test.net.geant.coco.agent.portal.utils;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import net.geant.coco.agent.portal.utils.VpnProvisioner;

public class BgpJSONTest {

    private static String BGP_JSON = new String(
    		"{ \"exabgp\": \"3.4.8\", \"time\": 1478009901, \"host\" : \"TS2-ODL\", \"pid\" : \"2009\", \"ppid\" : \"1\", \"counter\": 94, \"type\": \"update\", \"neighbor\": { \"ip\": \"10.2.0.254\", \"address\": { \"local\": \"10.3.0.254\", \"peer\": \"10.2.0.254\"}, \"asn\": { \"local\": \"65030\", \"peer\": \"65020\"}, \"message\": { \"update\": { \"attribute\": { \"origin\": \"igp\", \"as-path\": [ 65020 ], \"confederation-path\": [], \"extended-community\": [ 842122827661313, 9997075210298048239 ] }, \"announce\": { \"ipv4 unicast\": { \"10.2.0.254\": { \"10.2.1.128/25\": { } } } } } }} }");

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
    
    //9,223,372,036,854,775,807
    //9,997,075,210,298,048,239
    public static void main(String[] args) {
    	
    	Pattern pattern = Pattern.compile("[0-9]{11,20}");
    	Matcher matcher = pattern.matcher(BGP_JSON);
    	
    	while(matcher.find()){
    		String match = matcher.group();
    		BigInteger bigint = new BigInteger(match);
    		byte [] bytes = bigint.toByteArray();
    		String hex = bytesToHex(bytes);
    		System.out.println(String.format("Found new match %s turn into \"%s\" !", match, hex));
    		
    		BGP_JSON = BGP_JSON.replace(match, String.format("\"%s\"", hex));
    	}
    	
    	JSONParser parser = new JSONParser();
		try {
			JSONObject json = (JSONObject) parser.parse(BGP_JSON);
			String type = (String) json.get("type");
			//check if we got the correct message 
			if (type.equals("update")){
				//"exabgp": "3.4.8", "time": 1478009901, "host" : "TS2-ODL"
				String version = (String) json.get("exabgp");
				//int time = (int) json.get("time");
				String host = (String) json.get("host");
				Map<String, Object> neighbor = (Map<String, Object>) json.get("neighbor");
				Map<String, String> address = (Map<String, String>) neighbor.get("address");
				String local = address.get("local");
				String peer = address.get("peer");
				// get this domain with local
				// get remote domain with peer
				
				
				for (String key : neighbor.keySet()){
					System.out.println("Found key " + key);
				}
				
				Map<String, Object> message = (Map<String, Object>) neighbor.get("message");
				Map<String, Object> update = (Map<String, Object>) message.get("update");
				Map<String, Object> attribute = (Map<String, Object>) update.get("attribute");
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
						// TODO: Is this a good idea to remove all 0 in the beginning of the hex value as currently there might be a missmatch
						obj_hex = obj_hex.replaceAll("^[0]*", "");
						
						// lets check weather the obj is the none or hash
						if (obj_hex.substring(0, 2).equals("6B") || obj_hex.substring(0, 2).equals("8A") ){
							nonce = obj_hex;
						} else {
							target = obj_hex;
						}
					}
				}
				
				System.out.println("got bgp update - " + host + " - " + target + " - " + nonce);
			} else {
				// lets ignore the message
				System.out.println("type is wrong - " + type);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

}
