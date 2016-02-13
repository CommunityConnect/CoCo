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
