package net.geant.coco.agent.portal.controllers;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

//import lombok.extern.slf4j.Slf4j;

//@Slf4j
@Controller
@Configuration
@PropertySource("classpath:/net/geant/coco/agent/portal/props/config.properties")
public class PortalUserIntent {

	@Autowired
	Environment env;
	
	// redirect for index page
	@RequestMapping(value = { "/", "/static", "/static/" }, method = RequestMethod.GET)
	public String redirect() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		
		// check if the user is logged in
		if (auth.isAuthenticated()){
			return "redirect:/static/index.html";
		}
		else return "redirect:/spring_security_login";
	}
	
	// get the currenty logged in user name
	@RequestMapping(value="/static/getUser", method = RequestMethod.GET)
	public @ResponseBody String getUser() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
	    
		String name = "Null";
		
		if (auth.isAuthenticated()){
			name = auth.getName(); //get logged in username
		}
		
		Map<String, String> data = new HashMap<String, String>();
	    JSONObject json = new JSONObject();
	    data.put( "username", name );
	    json.putAll( data );
	    //System.out.printf( "JSON: %s", json.toString() );
			
		return json.toString();
	}
	
	
	@RequestMapping(value="/logout", method = RequestMethod.GET)
	public String logout() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		
		//logout the user
		auth.setAuthenticated(false);
				
		return "redirect:/spring_security_login?logout";
	}
}
