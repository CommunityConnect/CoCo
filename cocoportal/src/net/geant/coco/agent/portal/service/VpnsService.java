package net.geant.coco.agent.portal.service;

import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import net.geant.coco.agent.portal.dao.Vpn;
import net.geant.coco.agent.portal.dao.VpnDao;
import net.geant.coco.agent.portal.utils.VpnProvisioner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service("vpnsService")
public class VpnsService {
    private VpnDao vpnDao;

    @Autowired
    public void setVpnDao(VpnDao vpnDao) {
        this.vpnDao = vpnDao;
    }

    /**
     * @param controllerUrl
     *            URL of the OpenDaylight controller, e.g.
     *            http://127.0.0.1:8181/restconf
     * @return List<Vpn> of all VPNs configured in OpenDaylight, or null when no
     *         VPN is configured yet
     */
    public List<Vpn> getVpns(String controllerUrl) {
        VpnProvisioner vpnProvisioner = new VpnProvisioner(controllerUrl);
        List<String> vpnNames = vpnProvisioner.getVpnNames();
        if (vpnNames == null) {
            return null;
        }
        List<Vpn> vpns = new ArrayList<Vpn>();
        for (String name : vpnNames) {
            Vpn vpn = new Vpn();
            vpn.setId(0); // not used
            vpn.setName(name);
            vpn.setMplsLabel(0); // not used
            vpns.add(vpn);
        }
        // return vpnDao.getVpns();
        return vpns;
    }

    public Vpn getVpn(String vpnName) {
        return vpnDao.getVpn(vpnName);
    }

    /**
     * Insert new VPN in the database.
     * 
     * @param controllerUrl
     *            URL of the OpenDaylight controller (e.g.
     *            http://127.0.0.1:8181/restconf)
     * @param name
     *            Name of the VPN to be inserted.
     * @return Return true if the insertion succeeded, false otherwise.
     */
    public boolean createVpn(String controllerUrl, String name) {
        VpnProvisioner vpnProvisioner = new VpnProvisioner(controllerUrl);
        boolean result = vpnProvisioner.createVpn(name);
        log.info("createVpn returns: " + result);
        // return vpnDao.createVpn(name);
        return result;
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
