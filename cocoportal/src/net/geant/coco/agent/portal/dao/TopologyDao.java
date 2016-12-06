package net.geant.coco.agent.portal.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import net.geant.coco.agent.portal.dao.NetworkElement.NODE_TYPE;
import net.geant.coco.agent.portal.dao.NetworkInterface.IF_TYPE;

@Slf4j
@Component
public class TopologyDao {

	private NamedParameterJdbcTemplate jdbc;

    @Autowired
    public void setDataSource(DataSource jdbc) {
        this.jdbc = new NamedParameterJdbcTemplate(jdbc);
    }

    public List<NetworkInterface> getNetworkInterfaces_ENNI() {
    	// old code with ases
//        String query = "select switches.id, switches.name, "
//        		+ "ases.id AS as_id, ases.as_name AS as_name, ases.bgp_ip "
//        		+ "from switches "
//        		+ "INNER JOIN extlinks ON switches.id=extlinks.switch "
//        		+ "INNER JOIN ases ON extlinks.as=ases.id;";
        
        String query = "select switches.id, switches.name, "
        		+ "domains.as_num AS as_id, domains.as_name AS as_name, domains.bgp_ip "
        		+ "from switches "
        		+ "INNER JOIN extLinks ON switches.id=extLinks.switch "
        		+ "INNER JOIN domains ON extLinks.domain=domains.id;";

        //System.out.println("links query: " + query);

        List<NetworkInterface> networkInterfaces = jdbc.query(query, new RowMapper<NetworkInterface>() {

            @Override
            public NetworkInterface mapRow(ResultSet rs, int rowNum)
                    throws SQLException {
            	
            	NetworkElement networkElementFrom = new NetworkElement(rs.getInt("id"), rs.getString("name"), NODE_TYPE.SWITCH);
            	NetworkElement networkElementTo = new NetworkElement(rs.getInt("as_id"), rs.getString("as_name"), NODE_TYPE.EXTERNAL_AS);
            	
            	NetworkInterface networkLink = new NetworkInterface(networkElementFrom, networkElementTo, IF_TYPE.ENNI);

                return networkLink;
            }

        });
       
        return networkInterfaces;
    }
    
    public List<NetworkInterface> getNetworkInterfaces_INNI() {
        String query = ""
        		+ "select switches.id, switches.name, switches_to.id AS id_to, switches_to.name AS name_to from switches "
        		+ "INNER JOIN links ON switches.id=links.from "
        		+ "INNER JOIN switches AS switches_to ON links.to=switches_to.id ";
        		/*
        		+ "UNION "
        		+ "select switches.id, switches.name, switches_to.id AS id_to, switches_to.name AS name_to from switches "
        		+ "INNER JOIN links ON switches.id=links.to "
        		+ "INNER JOIN switches AS switches_to ON links.from=switches_to.id;";*/

     
        
        //System.out.println("links query: " + query);

        List<NetworkInterface> networkInterfaces = jdbc.query(query, new RowMapper<NetworkInterface>() {

            @Override
            public NetworkInterface mapRow(ResultSet rs, int rowNum)
                    throws SQLException {
            	
            	NetworkElement networkElementFrom = new NetworkElement(rs.getInt("id"), rs.getString("name"), NODE_TYPE.SWITCH);
            	NetworkElement networkElementTo = new NetworkElement(rs.getInt("id_to"), rs.getString("name_to"), NODE_TYPE.SWITCH);
            	
            	NetworkInterface networkLink = new NetworkInterface(networkElementFrom, networkElementTo, IF_TYPE.INNI);

                return networkLink;
            }

        });
       
        return networkInterfaces;
    }
    
    public List<NetworkInterface> getNetworkInterfaces_UNI() {
    	Authentication auth = SecurityContextHolder.getContext().getAuthentication();
	    
		String userName = "Null";
		
		if (auth.isAuthenticated()){
			userName = auth.getName(); //get logged in username
		}
    	
		MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("name", userName);
		
    	// TODO: Simon, Check if this is correct, we change from a side to a subnet here...
        String query = "select switches.id, switches.name, sites.id AS site_id, sites.name AS site_name, subnets.id AS sub_id, subnets.subnet AS sub_name, users.email from switches "
        		//+ "INNER JOIN sitelinks ON switches.id=sitelinks.switch "
        		//+ "INNER JOIN sites ON sitelinks.site=sites.id "
        		+ "INNER JOIN sites ON sites.switch=switches.id "
        		+ "INNER JOIN subnets ON subnets.site=sites.id "
        		+ "INNER JOIN subnetUsers ON subnets.id=subnetUsers.subnet "
        		+ "INNER JOIN users ON users.id=subnetUsers.user "
        		+ "WHERE users.email = :name OR users.name = :name;";

        //System.out.println("links query: " + query);

        List<NetworkInterface> networkInterfaces = jdbc.query(query, params, new RowMapper<NetworkInterface>() {

            @Override
            public NetworkInterface mapRow(ResultSet rs, int rowNum)
                    throws SQLException {
            	
            	NetworkElement networkElementFrom = new NetworkElement(rs.getInt("id"), rs.getString("name"), NODE_TYPE.SWITCH);
            	NetworkElement networkElementTo = new NetworkElement(rs.getInt("sub_id"), rs.getString("sub_name"), NODE_TYPE.CUSTOMER);
            	
            	NetworkInterface networkLink = new NetworkInterface(networkElementFrom, networkElementTo, IF_TYPE.UNI);

                return networkLink;
            }

        });
       
        return networkInterfaces;
    }
    
    public List<NetworkInterface> getNetworkInterfaces_BGP() {
    	// get the network sites that are connected via BGP
    	// TODO: this is wrong... we need  complete different code here
    	Authentication auth = SecurityContextHolder.getContext().getAuthentication();
	    
		String userName = "Null";
		
		if (auth.isAuthenticated()){
			userName = auth.getName(); //get logged in username
		}
    	
		MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("userName", userName);
		
    	String query = "select DISTINCT switches.id, switches.name, sites.id AS site_id, bgps.id AS bgp_id, bgps.announce from switches "
        		+ "INNER JOIN sites ON sites.switch=switches.id "
        		+ "INNER JOIN bgps ON remoteDomain=sites.domain "
        		+ "WHERE bgps.vpn IN (SELECT DISTINCT vpns.id FROM vpns "
        		+ " INNER JOIN vpnSubnet ON vpnSubnet.vpn=vpns.id "
        		+ " INNER JOIN subnetUsers ON vpnSubnet.subnet=subnetUsers.subnet "
        		+ " INNER JOIN users ON users.id=subnetUsers.user "
        		+ " WHERE users.email = :userName OR users.name = :userName) ";

    	log.debug(query.replace(":userName", userName));

        List<NetworkInterface> networkInterfaces = jdbc.query(query, params, new RowMapper<NetworkInterface>() {

            @Override
            public NetworkInterface mapRow(ResultSet rs, int rowNum)
                    throws SQLException {
            	
            	NetworkElement networkElementFrom = new NetworkElement(rs.getInt("id"), rs.getString("name"), NODE_TYPE.SWITCH);
            	NetworkElement networkElementTo = new NetworkElement(rs.getInt("bgp_id"), "BGP" + rs.getString("announce"), NODE_TYPE.CUSTOMER_BGP);
            	
            	
            	NetworkInterface networkLink = new NetworkInterface(networkElementFrom, networkElementTo, IF_TYPE.UNI);

                return networkLink;
            }

        });
       
        return networkInterfaces;
    }
    
    public List<NetworkInterface> getNetworkInterfaces_UNI_NO() {
    	// get the network sites of whom you are not a owner - NO=no ownership
    	Authentication auth = SecurityContextHolder.getContext().getAuthentication();
	    
		String userName = "Null";
		
		if (auth.isAuthenticated()){
			userName = auth.getName(); //get logged in username
		}
    	
		MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("userName", userName);
		
    	String query = "select DISTINCT switches.id, switches.name, sites.id AS site_id, sites.name AS site_name, subnets.id AS sub_id, subnets.subnet AS sub_name, users.email from switches "
        		+ "INNER JOIN sites ON sites.switch=switches.id "
        		+ "INNER JOIN subnets ON subnets.site=sites.id "
        		+ "INNER JOIN subnetUsers ON subnets.id=subnetUsers.subnet "
        		+ "INNER JOIN users ON users.id=subnetUsers.user "
        		+ "INNER JOIN vpnSubnet ON vpnSubnet.subnet=subnets.id "
        		+ "INNER JOIN vpns ON vpnSubnet.vpn=vpns.id "
        		+ "WHERE users.email != :userName AND users.name != :userName AND "
        		+ "vpns.id IN (SELECT DISTINCT vpns.id FROM vpns "
        		+ " INNER JOIN vpnSubnet ON vpnSubnet.vpn=vpns.id "
        		+ " INNER JOIN subnetUsers ON vpnSubnet.subnet=subnetUsers.subnet "
        		+ " INNER JOIN users ON users.id=subnetUsers.user "
        		+ " WHERE users.email = :userName OR users.name = :userName) "
        		+   "AND subnets.id NOT IN (SELECT DISTINCT subnets.id from subnets "
        		+ " INNER JOIN subnetUsers ON subnets.id=subnetUsers.subnet "
        		+ " INNER JOIN users ON users.id=subnetUsers.user "
        		+ " WHERE users.email = :userName OR users.name = :userName);";
        		
        List<NetworkInterface> networkInterfaces = jdbc.query(query, params, new RowMapper<NetworkInterface>() {

            @Override
            public NetworkInterface mapRow(ResultSet rs, int rowNum)
                    throws SQLException {
            	
            	NetworkElement networkElementFrom = new NetworkElement(rs.getInt("id"), rs.getString("name"), NODE_TYPE.SWITCH);
            	NetworkElement networkElementTo = new NetworkElement(rs.getInt("sub_id"), rs.getString("sub_name"), NODE_TYPE.CUSTOMER_NO);
            	
            	NetworkInterface networkLink = new NetworkInterface(networkElementFrom, networkElementTo, IF_TYPE.UNI);

                return networkLink;
            }

        });
       
        return networkInterfaces;
    }
}
