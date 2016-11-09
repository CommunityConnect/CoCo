package test.net.geant.coco.agent.portal.utils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import net.geant.coco.agent.portal.utils.VpnProvisioner;

public class BgpJSONSender {

    private static String BGP_JSON = new String(
    		"{ \"exabgp\": \"3.4.8\", \"time\": 1478009901, \"host\" : \"TS2-ODL\", \"pid\" : \"2009\", \"ppid\" : \"1\", \"counter\": 94, \"type\": \"update\", \"neighbor\": { \"ip\": \"10.2.0.254\", \"address\": { \"local\": \"10.3.0.254\", \"peer\": \"10.2.0.254\"}, \"asn\": { \"local\": \"65030\", \"peer\": \"65020\"}, \"message\": { \"update\": { \"attribute\": { \"origin\": \"igp\", \"as-path\": [ 65020 ], \"confederation-path\": [], \"extended-community\": [ 13889186639680674000, 842208727007241 ] }, \"announce\": { \"ipv4 unicast\": { \"10.2.0.254\": { \"10.2.1.128/25\": { } } } } } }} }");

    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
    
    public static String postJSON(String targetURL, String urlParameters) {
    	  HttpURLConnection connection = null;

    	  try {
    	    //Create connection
    	    URL url = new URL(targetURL);
    	    connection = (HttpURLConnection) url.openConnection();
    	    connection.setRequestMethod("POST");
    	    connection.setRequestProperty("Content-Type", 
    	        "application/json; charset=UTF-8");

    	    connection.setRequestProperty("Content-Length", 
    	        Integer.toString(urlParameters.getBytes().length));
    	    connection.setRequestProperty("Content-Language", "en-US");  

    	    connection.setUseCaches(false);
    	    connection.setDoOutput(true);

    	    //Send request
    	    DataOutputStream wr = new DataOutputStream (
    	        connection.getOutputStream());
    	    wr.writeBytes(urlParameters);
    	    wr.close();

    	    //Get Response  
    	    InputStream is = connection.getInputStream();
    	    BufferedReader rd = new BufferedReader(new InputStreamReader(is));
    	    StringBuilder response = new StringBuilder(); // or StringBuffer if Java version 5+
    	    String line;
    	    while ((line = rd.readLine()) != null) {
    	      response.append(line);
    	      response.append('\r');
    	    }
    	    rd.close();
    	    return response.toString();
    	  } catch (Exception e) {
    	    e.printStackTrace();
    	    return null;
    	  } finally {
    	    if (connection != null) {
    	      connection.disconnect();
    	    }
    	  }
    	}
    
    public static void main(String[] args) {
    	postJSON("http://localhost:8080/CoCo-agent/rest/bgp/update", BGP_JSON);
    }

}
