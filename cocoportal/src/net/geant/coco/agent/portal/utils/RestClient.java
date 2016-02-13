package net.geant.coco.agent.portal.utils;

import java.net.URI;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

import lombok.extern.slf4j.Slf4j;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;

@Slf4j
@Configuration
public class RestClient {
    private int TIMEOUT = 3000;

    private String controllerUrl;
    private URI controllerUri;
    
    public void clearAll() {
        // Delete all flows on switches
        sendtoSwitch("openflow:1", "clear", null, null);
        sendtoSwitch("openflow:2", "clear", null, null);
        sendtoSwitch("openflow:3", "clear", null, null);
        sendtoSwitch("openflow:4", "clear", null, null);
    }
    
    public RestClient(String controllerUrl) {
    	this.controllerUrl = controllerUrl;
    	controllerUri = UriBuilder.fromUri(controllerUrl).build();
    }

    public String getJsonTopo() {
        try {
            ClientConfig config = new DefaultClientConfig();
            Client client = Client.create(config);
            client.setConnectTimeout(TIMEOUT);
            client.setReadTimeout(TIMEOUT);

            ClientResponse r;
            client.addFilter(new HTTPBasicAuthFilter("admin", "admin"));
            WebResource service = client.resource(controllerUri);
            r = service.path("operational/network-topology:network-topology")
                    .type(javax.ws.rs.core.MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .get(ClientResponse.class);
            int status = r.getStatus();
            String out = r.getEntity(String.class);

            if (status == 200) {
                return out;
            }
        } catch (Exception e) {
        	log.warn(e.getMessage());
        }
        return null;
    }

    public String getJSONPortInfo(String nodeId, String portId) {
        try {
            ClientConfig config = new DefaultClientConfig();
            Client client = Client.create(config);
            ClientResponse r;
            client.addFilter(new HTTPBasicAuthFilter("admin", "admin"));
            WebResource service = client.resource(controllerUri);
            String path = String.format("%s/node-connector/%s", nodeId, portId);
            r = service.path("operational/opendaylight-inventory:nodes/node")
                    .path(path)
                    .type(javax.ws.rs.core.MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .get(ClientResponse.class);
            int status = r.getStatus();
            String out = r.getEntity(String.class);
            if (status == 200) {
                return out;
            }
        } catch (Exception e) {
            log.trace(e.getMessage());
        }
        return null;
    }

    public void sendtoSwitch(String switchID, String command,
            String flow, String flowNr) {
        try {
            ClientConfig config = new DefaultClientConfig();
            Client client = Client.create(config);
            ClientResponse r;
            String error = "";
            client.addFilter(new HTTPBasicAuthFilter("admin", "admin"));
            WebResource service = client.resource(controllerUri);

            if (command.equals("clear")) {
                // Get inventory first. Needed to avoid error on DELETE???
                log.trace("inventory on switch " + switchID);
                r = service.path("operational/opendaylight-inventory:nodes")
                        .type(javax.ws.rs.core.MediaType.APPLICATION_XML)
                        .accept(MediaType.APPLICATION_XML)
                        .get(ClientResponse.class);

                // Delete all flows on switch
                log.trace("clear on switch " + switchID);
                String path = String.format(
                        "opendaylight-inventory:nodes/node/%s", switchID);
                r = service.path("config").path(path)
                        .type(javax.ws.rs.core.MediaType.APPLICATION_XML)
                        .accept(MediaType.APPLICATION_XML)
                        .delete(ClientResponse.class);
            } else if (command.equals("add")) {
                log.trace("config on switch " + switchID);

                String path = String
                        .format("config/opendaylight-inventory:nodes/node/%s/table/0/flow/",
                                switchID);
                //log.trace(flow);
                System.out.flush();
                r = service.path(path).path(flowNr)
                        .type(javax.ws.rs.core.MediaType.APPLICATION_XML)
                        .accept(MediaType.APPLICATION_XML)
                        .put(ClientResponse.class, flow);
                error = r.getEntity(String.class);
                log.trace(Integer.toString(r.getStatus()));
            } else if (command.equals("delete")) {

                String path = String
                        .format("config/opendaylight-inventory:nodes/node/%s",
                                flow);
                log.trace(flow);
                System.out.flush();
                r = service.path(path)
                        .type(javax.ws.rs.core.MediaType.APPLICATION_XML)
                        .accept(MediaType.APPLICATION_XML)
                        .delete(ClientResponse.class);
                //service.header("X-HTTP-Method-Override", "DELETE");
                error = r.getEntity(String.class);
                log.trace(Integer.toString(r.getStatus()));
            } else {
                error = "unknown command";
            }
            log.trace(error);
        } catch (Exception e) {
            log.trace(e.getMessage());
        }
    }
    
    
    public void sendtoSwitch(String switchID, String command,
            String flow, String flowNr, String tableId) {
        try {
            ClientConfig config = new DefaultClientConfig();
            Client client = Client.create(config);
            ClientResponse r;
            String error = "";
            client.addFilter(new HTTPBasicAuthFilter("admin", "admin"));
            WebResource service = client.resource(controllerUri);

            if (command.equals("clear")) {
                // Get inventory first. Needed to avoid error on DELETE???
                log.trace("inventory on switch " + switchID);
                r = service.path("operational/opendaylight-inventory:nodes")
                        .type(javax.ws.rs.core.MediaType.APPLICATION_XML)
                        .accept(MediaType.APPLICATION_XML)
                        .get(ClientResponse.class);

                // Delete all flows on switch
                log.trace("clear on switch " + switchID);
                String path = String.format(
                        "opendaylight-inventory:nodes/node/%s", switchID);
                r = service.path("config").path(path)
                        .type(javax.ws.rs.core.MediaType.APPLICATION_XML)
                        .accept(MediaType.APPLICATION_XML)
                        .delete(ClientResponse.class);
            } else if (command.equals("add")) {
                log.trace("config on switch " + switchID);

                String path = String
                        .format("config/opendaylight-inventory:nodes/node/%s/table/%s/flow/",
                                switchID, tableId);
                log.trace(flow);
                System.out.flush();
                r = service.path(path).path(flowNr)
                        .type(javax.ws.rs.core.MediaType.APPLICATION_XML)
                        .accept(MediaType.APPLICATION_XML)
                        .put(ClientResponse.class, flow);
                error = r.getEntity(String.class);
                log.trace(Integer.toString(r.getStatus()));
            } else if (command.equals("delete")) {

                String path = String
                        .format("config/opendaylight-inventory:nodes/node/%s",
                                flow);
                log.trace(flow);
                System.out.flush();
                r = service.path(path)
                        .type(javax.ws.rs.core.MediaType.APPLICATION_XML)
                        .accept(MediaType.APPLICATION_XML)
                        .delete(ClientResponse.class);
                //service.header("X-HTTP-Method-Override", "DELETE");
                error = r.getEntity(String.class);
                log.trace(Integer.toString(r.getStatus()));
            } else {
                error = "unknown command";
            }
            log.trace(error);
        } catch (Exception e) {
            log.trace(e.getMessage());
        }
    }
}
