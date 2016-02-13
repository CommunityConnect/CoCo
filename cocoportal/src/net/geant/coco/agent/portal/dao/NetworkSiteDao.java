package net.geant.coco.agent.portal.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NetworkSiteDao {
    private NamedParameterJdbcTemplate jdbc;

    @Autowired
    public void setDataSource(DataSource jdbc) {
        this.jdbc = new NamedParameterJdbcTemplate(jdbc);
    }

    public List<NetworkSite> getNetworkSites() {
        String query = "SELECT sites.*, " + "switches.name AS switch_name, "
                + "vpns.name AS vpn_name " + "FROM sites "
                + "INNER JOIN switches ON sites.switch = switches.id "
                + "INNER JOIN site2vpn ON sites.id = site2vpn.siteid "
                + "INNER JOIN vpns ON vpns.id = site2vpn.vpnid;";
        // System.out.println(query);
        return jdbc.query(query, new RowMapper<NetworkSite>() {

            @Override
            public NetworkSite mapRow(ResultSet rs, int rowNum)
                    throws SQLException {
                NetworkSite networkSite = new NetworkSite();

                networkSite.setId(rs.getInt("id"));
                networkSite.setName(rs.getString("name"));
                networkSite.setX(rs.getInt("x"));
                networkSite.setY(rs.getInt("y"));
                networkSite.setProviderSwitch(rs.getString("switch_name"));
                networkSite.setProviderPort(rs.getInt("remote_port"));
                networkSite.setCustomerPort(rs.getInt("local_port"));
                networkSite.setVlanId(rs.getInt("vlanid"));
                networkSite.setIpv4Prefix(rs.getString("ipv4prefix"));
                networkSite.setMacAddress(rs.getString("mac_address"));
                networkSite.setVpnName(rs.getString("vpn_name"));

                return networkSite;
            }

        });
    }

    public List<NetworkSite> getNetworkSites(int id) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("id", id);

        String query = "SELECT sites.*, " + "switches.name AS switch_name "
                + "FROM sites " + "JOIN switches WHERE switch = switches.id "
                + "AND sites.id = :id;";
        return jdbc.query(query, params, new RowMapper<NetworkSite>() {

            @Override
            public NetworkSite mapRow(ResultSet rs, int rowNum)
                    throws SQLException {
                NetworkSite networkSite = new NetworkSite();

                networkSite.setId(rs.getInt("id"));
                networkSite.setName(rs.getString("name"));
                networkSite.setX(rs.getInt("x"));
                networkSite.setY(rs.getInt("y"));
                networkSite.setProviderSwitch(rs.getString("switch_name"));
                networkSite.setProviderPort(rs.getInt("remote_port"));
                networkSite.setCustomerPort(rs.getInt("local_port"));
                networkSite.setVlanId(rs.getInt("vlanid"));
                networkSite.setIpv4Prefix(rs.getString("ipv4prefix"));
                networkSite.setMacAddress(rs.getString("mac_address"));
                networkSite.setVpnName(rs.getString("vpn_name"));

                return networkSite;
            }

        });
    }

    public List<NetworkSite> getNetworkSites(String vpnName) {
        if (vpnName == null) {
            vpnName = "all";
        }
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("vpn", vpnName);

        String query = "SELECT sites.*, " + "switches.name AS switch_name, "
                + "vpns.name AS vpn_name " + "FROM sites "
                + "INNER JOIN switches ON sites.switch = switches.id "
                + "INNER JOIN site2vpn ON sites.id = site2vpn.siteid "
                + "INNER JOIN vpns ON vpns.id = site2vpn.vpnid "
                + "AND vpns.name = :vpn ;";
        return jdbc.query(query, params, new RowMapper<NetworkSite>() {

            @Override
            public NetworkSite mapRow(ResultSet rs, int rowNum)
                    throws SQLException {
                NetworkSite networkSite = new NetworkSite();

                networkSite.setId(rs.getInt("id"));
                networkSite.setName(rs.getString("name"));
                networkSite.setX(rs.getInt("x"));
                networkSite.setY(rs.getInt("y"));
                networkSite.setProviderSwitch(rs.getString("switch_name"));
                networkSite.setProviderPort(rs.getInt("remote_port"));
                networkSite.setCustomerPort(rs.getInt("local_port"));
                networkSite.setVlanId(rs.getInt("vlanid"));
                networkSite.setIpv4Prefix(rs.getString("ipv4prefix"));
                networkSite.setMacAddress(rs.getString("mac_address"));
                networkSite.setVpnName(rs.getString("vpn_name"));

                return networkSite;
            }

        });
    }

    public List<NetworkSite> getNetworkSites(String siteName, String vpnName) {
        if (vpnName == null) {
            vpnName = "all";
        }
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("vpn", vpnName);

        String query = "SELECT sites.*, " + "switches.name AS switch_name, "
                + "vpns.name AS vpn_name " + "FROM sites "
                + "INNER JOIN switches ON switch = switches.id "
                + "INNER JOIN site2vpn ON sites.id = site2vpn.siteid "
                + "INNER JOIN vpns ON vpns.id = site2vpn.vpnid "
                + "AND vpns.name = :vpn " + "AND switches.name = :sitename ;";
        return jdbc.query(query, params, new RowMapper<NetworkSite>() {

            @Override
            public NetworkSite mapRow(ResultSet rs, int rowNum)
                    throws SQLException {
                NetworkSite networkSite = new NetworkSite();

                networkSite.setId(rs.getInt("id"));
                networkSite.setName(rs.getString("name"));
                networkSite.setX(rs.getInt("x"));
                networkSite.setY(rs.getInt("y"));
                networkSite.setProviderSwitch(rs.getString("switch_name"));
                networkSite.setProviderPort(rs.getInt("remote_port"));
                networkSite.setCustomerPort(rs.getInt("local_port"));
                networkSite.setVlanId(rs.getInt("vlanid"));
                networkSite.setIpv4Prefix(rs.getString("ipv4prefix"));
                networkSite.setMacAddress(rs.getString("mac_address"));
                networkSite.setVpnName(rs.getString("vpn_name"));

                return networkSite;
            }

        });
    }

    public NetworkSite getNetworkSite(String siteName) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("name", siteName);

        String query = "SELECT sites.* FROM sites "
                + "JOIN switches WHERE sites.switch = switches.id "
                + "AND sites.name = :name;";
        log.trace("getNetworkSite " + siteName + "  " + query);
        return jdbc.query(query, params, new ResultSetExtractor<NetworkSite>() {

            @Override
            public NetworkSite extractData(ResultSet rs) throws SQLException {
                NetworkSite networkSite = new NetworkSite();

                networkSite.setId(rs.getInt("id"));
                networkSite.setName(rs.getString("name"));
                networkSite.setX(rs.getInt("x"));
                networkSite.setY(rs.getInt("y"));
                networkSite.setProviderSwitch(rs.getString("switch_name"));
                networkSite.setProviderPort(rs.getInt("remote_port"));
                networkSite.setCustomerPort(rs.getInt("local_port"));
                networkSite.setVlanId(rs.getInt("vlanid"));
                networkSite.setIpv4Prefix(rs.getString("ipv4prefix"));
                networkSite.setMacAddress(rs.getString("mac_address"));
                networkSite.setVpnName(rs.getString("vpn_name"));

                return networkSite;
            }

        });
    }
    
    public int insertNetworkSite(String name, int switchNumber, int remotePort, int localPort, int vlanId, String ipPrefix, String macAddress) {
    	MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("name", name);
        params.addValue("switch", switchNumber);
        params.addValue("remote_port", remotePort);
        params.addValue("local_port", localPort);
        params.addValue("vlanid", vlanId);
        params.addValue("ipv4prefix", ipPrefix);
        params.addValue("mac_address", macAddress);

        String query = "INSERT INTO sites (name, switch, remote_port, local_port, vlanid, ipv4prefix, mac_address) "
        		+ "VALUES (:name, :switch, :remote_port, :local_port, :vlanid, :ipv4prefix, :mac_address);";
        /*String query = "SELECT sites.* FROM sites "
                + "JOIN switches WHERE sites.switch = switches.id "
                + "AND sites.name = :name;";*/
        log.trace("insertNetworkSite " + name + "  " + query);
        
        KeyHolder keyHolder = new GeneratedKeyHolder();
        int updateResultSites = jdbc.update(query, params, keyHolder);
        int newSiteId = keyHolder.getKey().intValue();
        
        params = new MapSqlParameterSource();
        params.addValue("site", newSiteId);
        params.addValue("switch", switchNumber);
        query = "INSERT INTO sitelinks (site, switch) VALUES "
        		+ "(:site, :switch);";
        
        int updateResultSiteLinks = jdbc.update(query, params);
        
        params = new MapSqlParameterSource();
        params.addValue("site", newSiteId);
        query = "INSERT INTO site2vpn (vpnid, siteid) VALUES "
        		+ "(1, :site);";
        
        int updateResultSiteToVpn = jdbc.update(query, params);
        
        return updateResultSites * updateResultSiteLinks * updateResultSiteToVpn;
    }
    
    public int deleteNetworkSite(String ipPrefix) {
    	MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("ipv4prefix", ipPrefix);

        String query = "DELETE FROM  sites WHERE ipv4prefix = :ipv4prefix;";

        /*String query = "SELECT sites.* FROM sites "
                + "JOIN switches WHERE sites.switch = switches.id "
                + "AND sites.name = :name;";*/
        log.trace("deleteNetworkSite " + ipPrefix + "  " + query);
        
        return jdbc.update(query, params);
    }

	public int insertNetworkSite(String prefix, int vlanId, String neighborIp) {
		
		MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("neighbor_ip", neighborIp);
        
        //sites.name, sites.switch, sites.remote_port
        String query = "SELECT sites.name, sites.switch, sites.remote_port "
        		+ "FROM sites INNER JOIN ases ON sites.name = ases.as_name "
        		+ "WHERE ases.bgp_ip = :neighbor_ip;";
        
        
        class AsData {
            public String asSiteName;
            public int asSiteSwitch;
            public int asRemotePort;
        }
        
        AsData asData = jdbc.query(query, params, new ResultSetExtractor<AsData>() {

            @Override
            public AsData extractData(ResultSet rs) throws SQLException {
            	AsData asData = new AsData();
            	
            	if(rs.next()){
	            	asData.asSiteName = rs.getString("name");
			        asData.asSiteSwitch = rs.getInt("switch");
			        asData.asRemotePort = rs.getInt("remote_port");
            	}
            	
                return asData;
            }
        });
        
        String newSiteName = asData.asSiteName + "-" + prefix;
        this.insertNetworkSite(newSiteName, asData.asSiteSwitch, asData.asRemotePort, 1, vlanId, prefix, "00:00:00:00:00:00");
        
		return 0;
	}
}
