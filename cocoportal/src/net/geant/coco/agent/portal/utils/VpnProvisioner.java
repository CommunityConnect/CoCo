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

    private class VpnSite {
        @SerializedName("vpn-name")
        private String vpnName;
        @SerializedName("site-name")
        private String siteName;
        @SerializedName("ip-prefix")
        private String ipPrefix;
        @SerializedName("switch-port-id")
        private String switchPortId;

        private VpnSite(String vpnName, String siteName, String ipPrefix,
                String switchPortId) {
            this.vpnName = vpnName;
            this.siteName = siteName;
            this.ipPrefix = ipPrefix;
            this.switchPortId = switchPortId;
        }
    }

    private class Input {
        @SerializedName("input")
        private VpnSite vpnSite;

        private Input(String vpnName, String siteName, String ipPrefix,
                String switchPortId) {
            vpnSite = new VpnSite(vpnName, siteName, ipPrefix, switchPortId);
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
     * @return true if the VPN insertion succeeded, false otherwise
     */
    public boolean createVpn(String name) {
        boolean isSuccessful = true;
        Gson gson = new Gson();
        Vpn vpn = new Vpn(name, "true", "fast-reroute");
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

    /**
     * Add a site to a VPN.
     * 
     * @param vpnName
     *            Name of the VPN.
     * @param siteName
     *            Name of the site.
     * @param ipPrefix
     *            IPv4 prefix used at the site.
     * @param switchPortId
     *            Concatenation of the OpenFlow switch name and port id (e.g.
     *            openflow:1:1).
     * @return HTTP return status code (int)
     */
    public int addSite(String vpnName, String siteName, String ipPrefix,
            String switchPortId) {
        Gson gson = new Gson();
        Input input = new Input(vpnName, siteName, ipPrefix, switchPortId);
        String jsonData = gson.toJson(input);
        log.info("json data = " + jsonData);
        ClientResponse response = service
                .path("operations/vpnintent:add-vpn-endpoint")
                .type(javax.ws.rs.core.MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .post(ClientResponse.class, jsonData);
        log.info("json vpn response is " + response.getStatus());
        return response.getStatus();
    }

    public int deleteSite(String vpnName, String siteName, String ipPrefix,
            String switchPortId) {
        Gson gson = new Gson();
        Input input = new Input(vpnName, siteName, ipPrefix, switchPortId);
        String jsonData = gson.toJson(input);
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
