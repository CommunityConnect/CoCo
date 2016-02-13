package net.geant.coco.agent.portal.service;

import java.util.List;

import net.geant.coco.agent.portal.dao.NetworkSwitch;
import net.geant.coco.agent.portal.dao.NetworkSwitchDao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("networkSwitchesService")
public class NetworkSwitchesService {
    private NetworkSwitchDao networkSwitchDao;

    @Autowired
    public void setNetworkSwitchDao(NetworkSwitchDao networkSwitchDao) {
        this.networkSwitchDao = networkSwitchDao;
    }

    public List<NetworkSwitch> getNetworkSwitches() {
        return networkSwitchDao.getNetworkSwitches();
    }
    
    public List<NetworkSwitch> getNetworkSwitchesWithNni() {
        return networkSwitchDao.getNetworkSwitchesWithNni();
    }
}
