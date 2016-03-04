package net.geant.coco.agent.portal.service;

import java.util.List;

import net.geant.coco.agent.portal.dao.Vpn;
import net.geant.coco.agent.portal.dao.VpnDao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("vpnsService")
public class VpnsService {
    private VpnDao vpnDao;
    
    @Autowired
    public void setVpnDao(VpnDao vpnDao) {
        this.vpnDao = vpnDao;
    }
    
    public List<Vpn> getVpns() {
        return vpnDao.getVpns();
    }
    
    public Vpn getVpn(String vpnName) {
        return vpnDao.getVpn(vpnName);
    }
    
    /**
     * Insert new VPN in the database.
     * 
     * @param name Name of the VPN to be inserted.
     * @return Return true if the insertion succeeded, false otherwise.
     */
    public boolean createVpn(String name) {
        return vpnDao.createVpn(name);
    }
    
    public Vpn getVpn(int vpnId) {
        return vpnDao.getVpn(vpnId);
    }
    
    public boolean addSite(String vpnName, String siteName) {
        return vpnDao.addSite(vpnName, siteName);
    }
    
    public boolean deleteSite(String siteName) {
        return vpnDao.deleteSite(siteName);
    }
}
