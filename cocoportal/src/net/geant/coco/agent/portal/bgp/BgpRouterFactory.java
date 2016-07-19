package net.geant.coco.agent.portal.bgp;

import org.springframework.context.annotation.PropertySource;

@PropertySource("classpath:/net/geant/coco/agent/portal/props/config.properties")
public class BgpRouterFactory {
	
	public static final boolean COMPILE_WITH_BGP = false;
	
	public static BgpRouterInterface create(String ipAddress, int port){
		
		if (COMPILE_WITH_BGP) {
			return new BgpRouter(ipAddress, port);
		}
		else {
			return new BgpRouterDummy();
		}            
    }
}
