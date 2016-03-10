package test.net.geant.coco.agent.portal.utils;

import java.io.IOException;

import net.geant.coco.agent.portal.utils.VpnProvisioner;

public class VpnProvisionerTest {

    private static String controllerUrl = new String(
            "http://192.168.56.125:8181/restconf");

    private static void hitKey() {
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // TODO Auto-generated method stub
        VpnProvisioner vpnProvisioner = new VpnProvisioner(controllerUrl);

        System.out.println("create VPN foo");
        vpnProvisioner.createVpn("foo", true, "slow-reroute");

        System.out.println("\nhit enter to add site foobar1 to VPN foo");
        hitKey();
        vpnProvisioner.addSite("foo", "foobar1", "10.20.30.0/24",
                "openflow:1:5", "00:10:30:40:50:60");
        
        System.out.println("\nhit enter to add site foobar2 to VPN foo");
        hitKey();
        vpnProvisioner.addSite("foo", "foobar2", "10.0.0.0/24",
                "openflow:1:42", "00:10:10:10:10:10");

        System.out.println("\nhit enter to delete site foobar1 from VPN foo");
        hitKey();
        vpnProvisioner.deleteSite("foo", "foobar1");
        
        System.out.println("\nhit enter to delete VPN foo");
        hitKey();
        vpnProvisioner.deleteVpn("foo");
    }

}
