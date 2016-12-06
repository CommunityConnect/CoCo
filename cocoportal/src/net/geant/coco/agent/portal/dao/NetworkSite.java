package net.geant.coco.agent.portal.dao;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NetworkSite extends NetworkElement {
    public NetworkSite() {

    }

    public NetworkSite(int id, String name, String providerSwitch,
            int providerPort, int customerPort, int vlanId, String ipv4Prefix,
            String macAddress, String vpnName) {
        super(id, name, NODE_TYPE.CUSTOMER);
        this.providerSwitch = providerSwitch;
        this.providerPort = providerPort;
        this.customerPort = customerPort;
        this.vlanId = vlanId;
        this.ipv4Prefix = ipv4Prefix;
        this.macAddress = macAddress;
        this.vpnName = vpnName;
    }
    
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        String NEW_LINE = "_";
        
        result.append("Site" + NEW_LINE);

        //TODO: do we have to add something here, is the name unique (we currently use the string to compare sites)
        if (this.getName() != null){
        	result.append(this.getName() + NEW_LINE);
        }
        if (this.getIpv4Prefix() != null){
        	result.append(this.getIpv4Prefix() + NEW_LINE);
        }
        //result.append(this.getMacAddress() + NEW_LINE);

        return result.toString();
      }
    
    @Override
    public boolean equals(Object obj) {
       if (!(obj instanceof NetworkSite)){
    	   //log.debug("Compare sites: false");
    	   return false;
       }
       if (obj == this){
    	   //log.debug("Compare sites: false");
    	   return true;
       }

       boolean ret_val = this.toString().equals(obj.toString());
        
       //log.debug("Compare sites: " + this + " with " + obj + " = " + ret_val);
        
       return ret_val;
    }

    private String providerSwitch;
    private int providerPort;
    private int customerPort;
    private int vlanId;
    private String ipv4Prefix;
    private String macAddress;
    private String vpnName;
    private User user;

    public String getProviderSwitch() {
        return providerSwitch;
    }

    public void setProviderSwitch(String providerSwitch) {
        this.providerSwitch = providerSwitch;
    }

    public int getProviderPort() {
        return providerPort;
    }

    public void setProviderPort(int providerPort) {
        this.providerPort = providerPort;
    }

    public int getCustomerPort() {
        return customerPort;
    }

    public void setCustomerPort(int customerPort) {
        this.customerPort = customerPort;
    }

    public int getVlanId() {
        return vlanId;
    }

    public void setVlanId(int vlanId) {
        this.vlanId = vlanId;
    }

    public String getIpv4Prefix() {
        return ipv4Prefix;
    }

    public void setIpv4Prefix(String ipv4Prefix) {
        this.ipv4Prefix = ipv4Prefix;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getVpnName() {
        return vpnName;
    }

    public void setVpnName(String vpnName) {
        this.vpnName = vpnName;
    }
    
    
}
