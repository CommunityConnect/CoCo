package net.geant.coco.agent.portal.utils;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status.Family;

import org.springframework.context.annotation.PropertySource;

import javax.ws.rs.core.UriBuilder;

import lombok.extern.slf4j.Slf4j;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;

/**
 * The VpnProvisioner class implements the interaction with the OpenDaylight
 * vpnintent framework. It serializes data in JSON format and sends it to the
 * OpenDaylight vpnintent REST interface via POST messages. Data returned with
 * GET calls is de-serialzed into Java classes and the individual items are
 * returned to the caller.
 * 
 * @author Ronald van der Pol <rvdp@rvdp.org>
 *
 */
@Slf4j
@PropertySource("classpath:/net/geant/coco/agent/portal/props/config.properties")
public class VpnProvisioner {
    private WebResource service;

    private class Vpn {
        @SerializedName("vpn-name")
        private String vpnName;
        @SerializedName("path-protection")
        private String pathProtection;
        @SerializedName("failover-type")
        private String failoverType;

        private Vpn(String vpnName, String pathProtection, String failoverType) {
            this.vpnName = vpnName;
            this.pathProtection = pathProtection;
            this.failoverType = failoverType;
        }
    }

    private class DeleteVpnWrapper {
        @SerializedName("input")
        private DeleteVpn vpn;

        private DeleteVpnWrapper(String vpnName) {
            this.vpn = new DeleteVpn(vpnName);
        }
    }

    private class DeleteVpn {
        @SerializedName("vpn-name")
        private String vpnName;

        private DeleteVpn(String vpnName) {
            this.vpnName = vpnName;
        }
    }

    private class VpnIntents {
        @SerializedName("vpn-intents")
        private List<Vpn> vpnIntents = new ArrayList<Vpn>();

        private VpnIntents(Vpn vpn) {
            this.vpnIntents.add(vpn);
        }
    }

    private class Vpns {
        @SerializedName("vpns")
        private VpnIntents vpnIntents;

        private Vpns(VpnIntents vpnIntents) {
            this.vpnIntents = vpnIntents;
        }
    }

    private class AddSite {
        @SerializedName("vpn-name")
        private String vpnName;
        @SerializedName("site-name")
        private String siteName;
        @SerializedName("ip-prefix")
        private String ipPrefix;
        @SerializedName("switch-port-id")
        private String switchPortId;
        
        // transient makes it non-serializable property
        //@SerializedName("server-mac-address")
        private transient String macAddress;

        private AddSite(String vpnName, String siteName, String ipPrefix,
                String switchPortId, String macAddress) {
            this.vpnName = vpnName;
            this.siteName = siteName;
            this.ipPrefix = ipPrefix;
            this.switchPortId = switchPortId;
            this.macAddress = macAddress;
        }
    }

    private class AddVpnEndpoint {
        @SerializedName("input")
        private AddSite vpnSite;

        private AddVpnEndpoint(String vpnName, String siteName,
                String ipPrefix, String switchPortId, String macAddress) {
            vpnSite = new AddSite(vpnName, siteName, ipPrefix, switchPortId, macAddress);
        }
    }

    private class DeleteSite {
        @SerializedName("vpn-name")
        private String vpnName;
        @SerializedName("site-name")
        private String siteName;

        private DeleteSite(String vpnName, String siteName) {
            this.vpnName = vpnName;
            this.siteName = siteName;
        }
    }

    private class DeleteVpnEndpoint {
        @SerializedName("input")
        private DeleteSite vpnSite;

        private DeleteVpnEndpoint(String vpnName, String siteName) {
            vpnSite = new DeleteSite(vpnName, siteName);
        }
    }

    private int TIMEOUT = 3000;

    /**
     * Initializes VpnProvisioner object. Set URL of OpenDaylight controller.
     * Set connecting and reading timeouts.
     * 
     * @param controllerUrl
     *            URL of the OpenDaylight controller, e.g.
     *            http://127.0.0.1:8181/restconf
     */
    public VpnProvisioner(String controllerUrl) {
        ClientConfig config = new DefaultClientConfig();
        Client client = Client.create(config);
        client.setConnectTimeout(TIMEOUT);
        client.setReadTimeout(TIMEOUT);
        client.addFilter(new HTTPBasicAuthFilter("admin", "admin"));
        log.info("controller URL is " + controllerUrl);
        URI uri = UriBuilder.fromUri(controllerUrl).build();
        service = client.resource(uri);
    }

    /**
     * Create a new VPN.
     * 
     * @param name
     *            Name of the VPN to be created.
     * @param isProtected
     *            True when a protected path needs to be provisioned, false when
     *            an unprotected path needs to be provisioned.
     * @param failover
     *            Not implemented yet, slow rerouting is always used.
     * @return true if the VPN insertion succeeded, false otherwise
     */
    public boolean createVpn(String name, boolean isProtected, String failover) {
        boolean isSuccessful = true;
        String pathProtection;
        Gson gson = new Gson();
        if (isProtected) {
            pathProtection = "true";
        } else {
            pathProtection = "false";
        }
        // fast-reroute is not implemented, always use "slow-reroute"
        Vpn vpn = new Vpn(name, pathProtection, "slow-reroute");
        VpnIntents vpnIntents = new VpnIntents(vpn);
        String jsonData = gson.toJson(vpnIntents);

        // jsonData = "{ \"vpns\": " + jsonData + " }";

        log.info("json data = " + jsonData);

        ClientResponse response = null;
        try {
            response = service.path("config/vpnintent:vpns")
                    .type(javax.ws.rs.core.MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .post(ClientResponse.class, jsonData);
            log.info("json vpn response is " + response.getStatus());
            if (response.getStatusInfo().getFamily() != Family.SUCCESSFUL) {
                isSuccessful = false;
            }
        } catch (UniformInterfaceException e) {
            log.info(e.getMessage());
            isSuccessful = false;
        } catch (ClientHandlerException e) {
            log.info(e.getMessage());
            isSuccessful = false;
        }

        return isSuccessful;
    }

    /**
     * This method get a list of all known VPN names in the OpenDaylight
     * controller and returns a list of names.
     * 
     * @return Returns a List<String> containing all existing VPN names or null
     *         when no VPNs are configured yet in OpenDaylight.
     */
    public List<String> getVpnNames() {
        boolean isSuccessful = true;
        Gson gson = new Gson();
        List<String> vpnNames = new ArrayList<String>();
        try {
            String s = service.path("config/vpnintent:vpns")
                    .type(javax.ws.rs.core.MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON).get(String.class);
            log.info("json vpn response is " + s);
            Vpns vpns = gson.fromJson(s, Vpns.class);
            for (Vpn vpn : vpns.vpnIntents.vpnIntents) {
                log.info("vpns = " + vpn.vpnName);
                vpnNames.add(vpn.vpnName);
            }
        } catch (UniformInterfaceException e) {
            log.info(e.getMessage());
            isSuccessful = false;
        } catch (ClientHandlerException e) {
            log.info(e.getMessage());
            isSuccessful = false;
        }
        if (isSuccessful) {
            return vpnNames;
        } else {
            return null;
        }
    }

    public boolean deleteVpn(String name) {
        boolean isSuccessful = true;
        Gson gson = new Gson();

        DeleteVpnWrapper vpn = new DeleteVpnWrapper(name);
        String jsonData = gson.toJson(vpn);

        // jsonData = "{ \"vpns\": " + jsonData + " }";

        log.info("deleteVpn: json data = " + jsonData);

        ClientResponse response = null;
        try {
            response = service.path("operations/vpnintent:remove-vpn")
                    .type(javax.ws.rs.core.MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .post(ClientResponse.class, jsonData);
            log.info("json vpn response is " + response.getStatus());
            if (response.getStatusInfo().getFamily() != Family.SUCCESSFUL) {
                isSuccessful = false;
            }
        } catch (UniformInterfaceException e) {
            log.info(e.getMessage());
            isSuccessful = false;
        } catch (ClientHandlerException e) {
            log.info(e.getMessage());
            isSuccessful = false;
        }

        return isSuccessful;
    }

    /**
     * Add a site to a VPN.
     * 
     * @param vpnName
     *            Name of the VPN.
     * @param siteName
     *            Name of the site.
     * @param ipPrefix
     *            IPv4 prefix used at the site, e.g. 10.0.0.0/24.
     * @param switchPortId
     *            Concatenation of the OpenFlow switch name and port id, e.g.
     *            openflow:1:1.
     * @param macAddress
     *            MAC address of the router/host at the site.
     * @return HTTP return status code (int)
     */
    public int addSite(String vpnName, String siteName, String ipPrefix,
            String switchPortId, String macAddress) {
        Gson gson = new Gson();
        AddVpnEndpoint endpoint = new AddVpnEndpoint(vpnName, siteName,
                ipPrefix, switchPortId, macAddress);
        String jsonData = gson.toJson(endpoint);
        log.info("json data = " + jsonData);
        ClientResponse response = service
                .path("operations/vpnintent:add-vpn-endpoint")
                .type(javax.ws.rs.core.MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .post(ClientResponse.class, jsonData);
        log.info("json vpn response is " + response.getStatus());
        return response.getStatus();
    }

    public int deleteSite(String vpnName, String siteName) {
        Gson gson = new Gson();
        DeleteVpnEndpoint endpoint = new DeleteVpnEndpoint(vpnName, siteName);
        String jsonData = gson.toJson(endpoint);
        log.info("json data = " + jsonData);
        ClientResponse response = service
                .path("operations/vpnintent:remove-vpn-endpoint")
                .type(javax.ws.rs.core.MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .post(ClientResponse.class, jsonData);
        log.info("json vpn response is " + response.getStatus());
        return response.getStatus();
    }
}
