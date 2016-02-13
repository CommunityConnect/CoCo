/**
 * 
 */
package net.geant.coco.agent.portal.utils;

import lombok.extern.slf4j.Slf4j;

/**
 * @author rvdp@surfnet.nl
 *
 */
@Slf4j
public class Flow {
    private String device = null;
    private int id = -1;
    private String flow = null;
    private String inport = null;
    private String outport = null;
    private String dlvlan = null;
    private int ethertype = -1;
    private String matchMplsLabel = null;
    private String matchDstIpv4Prefix = null;
    private String matchDlDst = null;
    private String pushVlanId = null;
    private String newVlanId = null;
    private String pushPeMplsLabel = null;
    private String pushVpnMplsLabel = null;
    private String dstMAC = null;
    private boolean stripvlan = false;
    private boolean popTwoMplsLabels = false;

    public Flow(String device, int id) {
        this.device = device;
        this.id = id;
        flow = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>"
                + "<flow xmlns=\"urn:opendaylight:flow:inventory\">"
                + "<strict>true</strict><hard-timeout>0</hard-timeout><idle-timeout>0</idle-timeout>";
    }

    public void inPort(String inport) {
        this.inport = inport;
    }

    public void matchEthertype(int ethertype) {
        this.ethertype = ethertype;
    }

    public void matchVlan(String dlvlan) {
        this.dlvlan = dlvlan;
    }

    public void matchDlDst(String dldst) {
        this.matchDlDst = dldst;
    }

    public void matchMplsLabel(String mplsLabel) {
        this.matchMplsLabel = mplsLabel;
    }

    public void matchDstIpv4Prefix(String matchDstIpv4Prefix) {
        this.matchDstIpv4Prefix = matchDstIpv4Prefix;
    }

    public void stripVlan() {
        this.stripvlan = true;
    }

    public void setDstMAC(String mac) {
        this.dstMAC = mac;
    }

    public void outPort(String outport) {
        this.outport = outport;
    }

    public void pushVlan(String vlanid) {
        this.pushVlanId = vlanid;
    }

    public void modVlan(String newVlanId) {
        this.newVlanId = newVlanId;
    }

    public void pushPeMplsLabel(String peMplsLabel) {
    	log.trace("PE MPLS label is " + peMplsLabel);
        this.pushPeMplsLabel = peMplsLabel;
    }

    public void popTwoMplsLabels() {
        this.popTwoMplsLabels = true;
    }

    public void pushVpnMplsLabel(String vpnMplsLabel) {
        log.trace("VPN MPLS label is " + vpnMplsLabel);
        this.pushVpnMplsLabel = vpnMplsLabel;
    }
    
    public String buildFlow() {
        // add flow identifier
        if (outport != null) {
            flow = flow + String.format("<flow-name>flow%d</flow-name>", id);
        }
        // flow = flow + "<hard-timeout>0</hard-timeout>";
        // flow = flow + "<idle-timeout>0</idle-timeout>";

        // add match part
        flow += "<match>";
        if (inport != null) {
            flow = flow + String.format("<in-port>%s</in-port>", inport);
        }
        if (dlvlan != null) {
            flow += "<vlan-match><vlan-id>";
            flow += "<vlan-id-present>true</vlan-id-present>";
            flow += String.format("<vlan-id>%s</vlan-id>", dlvlan);
            flow += "</vlan-id></vlan-match>";
        }
        flow += "<ethernet-match>";
        if (ethertype != -1) {
            flow += "<ethernet-type>";
            flow += String.format("<type>%s</type>", String.valueOf(ethertype));
            flow += "</ethernet-type>";
        }
        if (matchDlDst != null) {
            flow += String
                    .format("<ethernet-destination><address>%s</address></ethernet-destination>",
                            matchDlDst);
        }
        flow += "</ethernet-match>";
        if (matchMplsLabel != null) {

            flow += "<protocol-match-fields>";
            flow += String
                    .format("<mpls-label>%s</mpls-label>", matchMplsLabel);
            flow += "</protocol-match-fields>";
        }
        if (matchDstIpv4Prefix != null) {
            flow += String.format("<ipv4-destination>%s</ipv4-destination>",
                    matchDstIpv4Prefix);
        }
        flow += "</match>";
        if (outport != null) {
            flow += String.format("<id>%s</id>", id);
        }
        flow += "<table_id>0</table_id>";

        // add actions part, only present if outport != null
        if (outport != null) {
            flow += "<instructions><instruction><order>0</order><apply-actions>";
            int order = 0;
            if (stripvlan) {
                flow += String.format(
                        "<action><order>%d</order><pop-vlan-action/></action>",
                        order);
                order++;
            }
            if (pushVlanId != null) {
                flow += String.format(
                        "<action><order>%d</order><push-vlan-action>", order);
                order++;
                flow += String.format("<ethernet-type>%s</ethernet-type>",
                        String.valueOf(0x8100));
                flow += "</push-vlan-action></action>";
                flow += String
                        .format("<action><order>%d</order><set-field><vlan-match><vlan-id>",
                                order);
                order++;
                flow += "<vlan-id-present>true</vlan-id-present>";
                flow += String.format("<vlan-id>%s</vlan-id>", pushVlanId);
                flow += "</vlan-id></vlan-match></set-field></action>";
            }
            if (newVlanId != null) {
                flow += String
                        .format("<action><order>%d</order><set-field><vlan-match><vlan-id>",
                                order);
                order++;
                flow += "<vlan-id-present>true</vlan-id-present>";
                flow += String.format("<vlan-id>%s</vlan-id>", newVlanId);
                flow += "</vlan-id></vlan-match></set-field></action>";
            }
            if (pushPeMplsLabel != null) {
                flow += String.format(
                        "<action><order>%d</order><push-mpls-action>", order);
                order++;
                flow += String.format("<ethernet-type>%s</ethernet-type>",
                        String.valueOf(0x8847));
                flow += "</push-mpls-action></action>";
                flow += String
                        .format("<action><order>%d</order><set-field><protocol-match-fields>",
                                order);
                order++;
                flow += String.format("<mpls-label>%s</mpls-label>",
                        pushPeMplsLabel);
                flow += "</protocol-match-fields></set-field></action>";
            }
            if (popTwoMplsLabels) {
                flow += String.format(
                        "<action><order>%d</order><pop-mpls-action>", order);
                flow += String.format("<ethernet-type>%s</ethernet-type>",
                        String.valueOf(0x0800));
                flow += "</pop-mpls-action></action>";
                order++;
            }
            if (dstMAC != null) {
                flow += String
                        .format("<action><order>%d</order><set-field><ethernet-match><ethernet-destination>",
                                order);
                order++;
                flow += String.format("<address>%s</address>", dstMAC);
                flow += "</ethernet-destination></ethernet-match></set-field></action>";
            }
            if (outport != null) {
                flow += String.format(
                        "<action><order>%d</order><output-action>", order);
                order++;
                flow += String.format(
                        "<output-node-connector>%s</output-node-connector>",
                        outport);
                flow += "<max-length>60</max-length></output-action></action>";
            }
            flow += "</apply-actions></instruction></instructions>";
        }
        flow += "</flow>";

        return (flow);
    }
    
    
    public String buildFlow2(int gotoTable, int tableId) {
        // add flow identifier
        if (outport != null) {
            flow = flow + String.format("<flow-name>flow%d</flow-name>", id);
        }
        
        flow += "<match>";
        if (inport != null) {
            flow = flow + String.format("<in-port>%s</in-port>", inport);
        }
        if (dlvlan != null) {
            flow += "<vlan-match><vlan-id>";
            flow += "<vlan-id-present>true</vlan-id-present>";
            flow += String.format("<vlan-id>%s</vlan-id>", dlvlan);
            flow += "</vlan-id></vlan-match>";
        }
        flow += "<ethernet-match>";
        if (ethertype != -1) {
            flow += "<ethernet-type>";
            flow += String.format("<type>%s</type>", String.valueOf(ethertype));
            flow += "</ethernet-type>";
        }
        if (matchDlDst != null) {
            flow += String
                    .format("<ethernet-destination><address>%s</address></ethernet-destination>",
                            matchDlDst);
        }
        flow += "</ethernet-match>";
        if (matchMplsLabel != null) {

            flow += "<protocol-match-fields>";
            flow += String
                    .format("<mpls-label>%s</mpls-label>", matchMplsLabel);
            flow += "</protocol-match-fields>";
        }
        if (matchDstIpv4Prefix != null) {
            flow += String.format("<ipv4-destination>%s</ipv4-destination>",
                    matchDstIpv4Prefix);
        }
        flow += "</match>";

        flow += String.format("<id>%s</id>", id);
        
        flow += String.format("<table_id>%s</table_id>", tableId);
                
        
            flow += "<instructions><instruction><order>0</order><apply-actions>";
            int order = 0;
            if (stripvlan) {
                flow += String.format(
                        "<action><order>%d</order><pop-vlan-action/></action>",
                        order);
                order++;
            }
            if (pushVlanId != null) {
                flow += String.format(
                        "<action><order>%d</order><push-vlan-action>", order);
                order++;
                flow += String.format("<ethernet-type>%s</ethernet-type>",
                        String.valueOf(0x8100));
                flow += "</push-vlan-action></action>";
                flow += String
                        .format("<action><order>%d</order><set-field><vlan-match><vlan-id>",
                                order);
                order++;
                flow += "<vlan-id-present>true</vlan-id-present>";
                flow += String.format("<vlan-id>%s</vlan-id>", pushVlanId);
                flow += "</vlan-id></vlan-match></set-field></action>";
            }
            if (newVlanId != null) {
                flow += String
                        .format("<action><order>%d</order><set-field><vlan-match><vlan-id>",
                                order);
                order++;
                flow += "<vlan-id-present>true</vlan-id-present>";
                flow += String.format("<vlan-id>%s</vlan-id>", newVlanId);
                flow += "</vlan-id></vlan-match></set-field></action>";
            }
            if (pushPeMplsLabel != null) {
                flow += String.format(
                        "<action><order>%d</order><push-mpls-action>", order);
                order++;
                flow += String.format("<ethernet-type>%s</ethernet-type>",
                        String.valueOf(0x8847));
                flow += "</push-mpls-action></action>";
                flow += String
                        .format("<action><order>%d</order><set-field><protocol-match-fields>",
                                order);
                order++;
                flow += String.format("<mpls-label>%s</mpls-label>",
                        pushPeMplsLabel);
                flow += "</protocol-match-fields></set-field></action>";
            }
            if (popTwoMplsLabels) {
                flow += String.format(
                        "<action><order>%d</order><pop-mpls-action>", order);
                flow += String.format("<ethernet-type>%s</ethernet-type>",
                        String.valueOf(0x0800));
                flow += "</pop-mpls-action></action>";
                order++;
            }
            if (dstMAC != null) {
                flow += String
                        .format("<action><order>%d</order><set-field><ethernet-match><ethernet-destination>",
                                order);
                order++;
                flow += String.format("<address>%s</address>", dstMAC);
                flow += "</ethernet-destination></ethernet-match></set-field></action>";
            }
            if (outport != null) {
                flow += String.format(
                        "<action><order>%d</order><output-action>", order);
                order++;
                flow += String.format(
                        "<output-node-connector>%s</output-node-connector>",
                        outport);
                flow += "<max-length>60</max-length></output-action></action>";
            }
            flow += "</apply-actions></instruction>";

            flow += String.format(
                    "<instruction><order>%d</order><go-to-table>", order);
            order++;
            
            flow += String.format("<table_id>%s</table_id>", gotoTable);
            
            flow += "</go-to-table></instruction></instructions>";
        
        flow += "</flow>";

        return (flow);
    }
    
    
    public String buildFlow3(int tableId) {
        // add flow identifier
        if (outport != null) {
            flow = flow + String.format("<flow-name>flow%d</flow-name>", id);
        }
        
        flow += "<match>";
        if (inport != null) {
            flow = flow + String.format("<in-port>%s</in-port>", inport);
        }
        if (dlvlan != null) {
            flow += "<vlan-match><vlan-id>";
            flow += "<vlan-id-present>true</vlan-id-present>";
            flow += String.format("<vlan-id>%s</vlan-id>", dlvlan);
            flow += "</vlan-id></vlan-match>";
        }
        flow += "<ethernet-match>";
        if (ethertype != -1) {
            flow += "<ethernet-type>";
            flow += String.format("<type>%s</type>", String.valueOf(ethertype));
            flow += "</ethernet-type>";
        }
        if (matchDlDst != null) {
            flow += String
                    .format("<ethernet-destination><address>%s</address></ethernet-destination>",
                            matchDlDst);
        }
        flow += "</ethernet-match>";
        if (matchMplsLabel != null) {

            flow += "<protocol-match-fields>";
            flow += String
                    .format("<mpls-label>%s</mpls-label>", matchMplsLabel);
            flow += "</protocol-match-fields>";
        }
        if (matchDstIpv4Prefix != null) {
            flow += String.format("<ipv4-destination>%s</ipv4-destination>",
                    matchDstIpv4Prefix);
        }
        flow += "</match>";
        if (outport != null) {
            flow += String.format("<id>%s</id>", id);
        }
        flow += String.format("<table_id>%s</table_id>", tableId);
                
        
        flow += "<instructions><instruction><order>0</order><apply-actions>";
        int order = 0;
        if (stripvlan) {
            flow += String.format(
                    "<action><order>%d</order><pop-vlan-action/></action>",
                    order);
            order++;
        }
        if (pushVlanId != null) {
            flow += String.format(
                    "<action><order>%d</order><push-vlan-action>", order);
            order++;
            flow += String.format("<ethernet-type>%s</ethernet-type>",
                    String.valueOf(0x8100));
            flow += "</push-vlan-action></action>";
            flow += String
                    .format("<action><order>%d</order><set-field><vlan-match><vlan-id>",
                            order);
            order++;
            flow += "<vlan-id-present>true</vlan-id-present>";
            flow += String.format("<vlan-id>%s</vlan-id>", pushVlanId);
            flow += "</vlan-id></vlan-match></set-field></action>";
        }
        if (newVlanId != null) {
            flow += String
                    .format("<action><order>%d</order><set-field><vlan-match><vlan-id>",
                            order);
            order++;
            flow += "<vlan-id-present>true</vlan-id-present>";
            flow += String.format("<vlan-id>%s</vlan-id>", newVlanId);
            flow += "</vlan-id></vlan-match></set-field></action>";
        }
        if (pushPeMplsLabel != null) {
            flow += String.format(
                    "<action><order>%d</order><push-mpls-action>", order);
            order++;
            flow += String.format("<ethernet-type>%s</ethernet-type>",
                    String.valueOf(0x8847));
            flow += "</push-mpls-action></action>";
            flow += String
                    .format("<action><order>%d</order><set-field><protocol-match-fields>",
                            order);
            order++;
            flow += String.format("<mpls-label>%s</mpls-label>",
                    pushPeMplsLabel);
            flow += "</protocol-match-fields></set-field></action>";
        }
        if (popTwoMplsLabels) {
            flow += String.format(
                    "<action><order>%d</order><pop-mpls-action>", order);
            flow += String.format("<ethernet-type>%s</ethernet-type>",
                    String.valueOf(0x0800));
            flow += "</pop-mpls-action></action>";
            order++;
        }
        if (dstMAC != null) {
            flow += String
                    .format("<action><order>%d</order><set-field><ethernet-match><ethernet-destination>",
                            order);
            order++;
            flow += String.format("<address>%s</address>", dstMAC);
            flow += "</ethernet-destination></ethernet-match></set-field></action>";
        }
        if (outport != null) {
            flow += String.format(
                    "<action><order>%d</order><output-action>", order);
            order++;
            flow += String.format(
                    "<output-node-connector>%s</output-node-connector>",
                    outport);
            flow += "<max-length>60</max-length></output-action></action>";
        }
        flow += "</apply-actions></instruction></instructions>";
        flow += "</flow>";

        return (flow);
    }

}
