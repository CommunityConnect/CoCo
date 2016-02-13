package net.geant.coco.agent.portal.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.geant.coco.agent.portal.dao.NetworkInterface;
import net.geant.coco.agent.portal.dao.TopologyDao;

@Service("topologyService")
public class TopologyService {

	private TopologyDao topologyDao;
	
	 @Autowired
	    public void setNetworkSwitchDao(TopologyDao topologyDao) {
	        this.topologyDao = topologyDao;
	    }

	    public List<NetworkInterface> getNetworkInterfaces() {
	    	List<NetworkInterface> if_enni = topologyDao.getNetworkInterfaces_INNI();
	    	List<NetworkInterface> if_inni = topologyDao.getNetworkInterfaces_ENNI();
	    	List<NetworkInterface> if_uni = topologyDao.getNetworkInterfaces_UNI();
	    	
	    	List<NetworkInterface> interfaces = new ArrayList<NetworkInterface>();
	    	interfaces.addAll(if_enni);
	    	interfaces.addAll(if_inni);
	    	interfaces.addAll(if_uni);
	    	
	    	return interfaces;
	    }
}
