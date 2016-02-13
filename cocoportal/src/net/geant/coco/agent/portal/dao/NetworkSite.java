package net.geant.coco.agent.portal.dao;

public class NetworkSite extends NetworkElement {
    public NetworkSite() {

    }

    public NetworkSite(int id, String name, int x, int y, String providerSwitch,
            int providerPort, int customerPort, int vlanId, String ipv4Prefix,
            String macAddress, String vpnName) {
        super(id, name, NODE_TYPE.CUSTOMER);
        this.name = name;
        this.x = x;
        this.y = y;
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

        result.append(this.getName() + NEW_LINE);
        result.append(this.getIpv4Prefix() + NEW_LINE);
        result.append(this.getMacAddress() + NEW_LINE);

        return result.toString();
      }

    private int id;
    private String name;
    private int x;
    private int y;
    private String providerSwitch;
    private int providerPort;
    private int customerPort;
    private int vlanId;
    private String ipv4Prefix;
    private String macAddress;
    private String vpnName;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

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
