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

public class BgpJSONcurlTester {

	private boolean cURL(String url, String parameter){
    	System.out.println(url + " - " + parameter);
    	
    	try {
			Process p = Runtime.getRuntime().exec("curl --form \"" + parameter + "\" " + url);
		} catch (IOException e) {
			
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
    	
    	return true;
    }
    
    public static void main(String[] args) {
    	try {
			Process p = Runtime.getRuntime().exec("calc");
		} catch (IOException e) {
			
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

}
