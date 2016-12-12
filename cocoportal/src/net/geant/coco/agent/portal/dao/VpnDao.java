package net.geant.coco.agent.portal.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;
import org.apache.commons.dbcp.BasicDataSource;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class VpnDao {
    private NamedParameterJdbcTemplate jdbc;

    @Autowired
    public void setDataSource(DataSource jdbc) {
    	//log.info("Init datesource " + ((BasicDataSource) jdbc).getUrl());
        this.jdbc = new NamedParameterJdbcTemplate(jdbc);
        try {
            log.info("Using database: " + jdbc.getConnection().getCatalog());
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    public List<Vpn> getVpns(int userId) {
    	MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("userId", "" + userId);
        
    	String query = "SELECT DISTINCT vpns.* FROM vpns "
    			+ "LEFT JOIN vpnSubnet ON vpnSubnet.vpn=vpns.id "
    			+ "LEFT JOIN subnetUsers ON vpnSubnet.subnet=subnetUsers.subnet "
    			+ "WHERE subnetUsers.user = :userId or vpns.owner = :userId ;";
    	
    	log.debug(query.replace(":userId", "" + userId));
        
        List<Vpn> vpns = getVpnList(query, params);
        
        return vpns;
    }

    public List<Vpn> getVpns() {    	
    	String query = "SELECT * FROM vpns";
        log.debug(query);
        
        List<Vpn> vpns = getVpnList(query, null);
        
        return vpns;
    }
    
    public Vpn getVpn(String vpnName) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("name", vpnName);
        
        String query = "SELECT * FROM vpns WHERE name = :name ;";
        log.debug(query);
        
        List<Vpn> vpns = getVpnList(query, params);
        
        if (vpns.isEmpty()) {
            return null;
        }
        
        return vpns.get(0);
    }

    public Vpn getVpn(int vpnID) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("name", vpnID);
        
        String query = "SELECT * FROM vpns WHERE id = :name ;";
        log.debug(query.replace(":name", "\"" + vpnID +  "\""));
        
        List<Vpn> vpns = getVpnList(query, params);
        
        if (vpns.isEmpty()) {
            return null;
        }
        
        return vpns.get(0);
    }
    
    private List<Vpn> getVpnList(String query, SqlParameterSource params) {
    	if (params == null) {
    		return jdbc.query(query, new RowMapper<Vpn>() {
                @Override
                public Vpn mapRow(ResultSet rs, int rowNum) throws SQLException {
                    Vpn vpn = new Vpn();

                    vpn.setId(rs.getInt("id"));
                    vpn.setName(rs.getString("name"));
                    vpn.setPathProtection(rs.getString("pathProtection"));
                    vpn.setFailoverType(rs.getString("failoverType"));

                    return vpn;
                }
            });
    	}
    	else {
    		return jdbc.query(query, params, new RowMapper<Vpn>() {
                @Override
                public Vpn mapRow(ResultSet rs, int rowNum) throws SQLException {
                    Vpn vpn = new Vpn();

                    vpn.setId(rs.getInt("id"));
                    vpn.setName(rs.getString("name"));
                    vpn.setPathProtection(rs.getString("pathProtection"));
                    vpn.setFailoverType(rs.getString("failoverType"));

                    return vpn;
                }
            });
    	}
    	
    }
    
    /**
     * Insert a new VPN in the MySQL database.
     * 
     * @param name
     *            Name of the VPN to be inserted
     * @return Return true if the insertion succeeded, false otherwise
     */
    public boolean createVpn(Vpn vpn) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("name", vpn.getName());
        params.addValue("pathProtection", vpn.getPathProtection());
        params.addValue("failoverType", vpn.getFailoverType());
        params.addValue("isPublic", 0);
        params.addValue("owner", vpn.getOwner_id());
        params.addValue("domain", vpn.getDomain_id());
        
        String query = "INSERT INTO vpns (`name`, `pathProtection`, `failoverType`, `isPublic`, `owner`, `domain`) "
                + "VALUES (:name, :pathProtection, :failoverType, :isPublic, :owner, :domain);";
        log.info("createVpn " + query);
        
        for (Map.Entry<String, Object> entry : params.getValues().entrySet()) {
        	log.info("createVpn Key = " + entry.getKey() + ", Value = " + entry.getValue());
        }
        //log.info("createVpn " + params.getValues());
        //log.info("createVpn name=" + vpn.getName());
        //log.info("createVpn name=" + vpn.getPathProtection());
        //log.info("createVpn name=" + vpn.getFailoverType());
        
        return (jdbc.update(query, params) == 1);
    }
    
    /**
     * Deletes VPN with a given id from a database.
     * On the database side removing sites from this VPN should be implemented.
     * @param vpnId
     * @return
     */
    public boolean deleteVpn(int vpnId) {
    	 MapSqlParameterSource params = new MapSqlParameterSource();
         params.addValue("id", vpnId);
         
         String query = "DELETE FROM vpns WHERE `id` = :id ;";
         log.info("deleteVpn: " + query.replace(":id", ""+vpnId));
         
         return (jdbc.update(query, params) == 1);
	}
    
    @Deprecated // we want a user ID assosiated with a subnet in a VPN (for user management, i.e. delete)
    public boolean addSubnetToVpn(String vpnName, String subnet) {
    
    	return this.addSubnetToVpn(vpnName, subnet, -1);
    }

    public boolean addSubnetToVpn(String vpnName, String subnet, int userID) {
    	MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("vpnName", vpnName);
        params.addValue("subnet", subnet);
        if (userID <= -1){
        	params.addValue("user", "NULL");
        } else {
        	params.addValue("user", userID);
        }

        String query = "INSERT INTO vpnSubnet (`vpn`, `subnet`, `user`) "
                + "VALUES ("
                + "(SELECT id FROM vpns WHERE `name` = :vpnName), "
                + "(SELECT id FROM subnets WHERE `subnet` = :subnet), "
                + ":user "
                + ");";
        log.debug("vpnDao addSite: " + query);
        return jdbc.update(query, params) == 1;
    }
    
    @Deprecated
    public boolean addSiteToVpn(String vpnName, String siteName) {
    	return this.addSubnetToVpn(vpnName, siteName);
    	
//        MapSqlParameterSource params = new MapSqlParameterSource();
//        params.addValue("vpnName", vpnName);
//        params.addValue("siteName", siteName);
//
//        String query = "INSERT INTO site2vpn (`vpnid`, `siteid`)"
//                + "VALUES ("
//                + "(SELECT id FROM vpns WHERE `name` = :vpnName),"
//                + "(SELECT id FROM sites WHERE `name` = :siteName)"
//                + ");";
//        log.debug("vpnDao addSite: " + query);
//        return jdbc.update(query, params) == 1;
    }

    public boolean deleteSubnetFromVpn(String vpnName, String subnet) {
    	MapSqlParameterSource params = new MapSqlParameterSource();
    	params.addValue("vpnName", vpnName);
    	params.addValue("subnet", subnet);
    	
    	String query = "DELETE FROM vpnSubnet "
    			+ "WHERE vpn = (SELECT id FROM vpns WHERE vpns.name = :vpnName) "
    			+ "AND subnet = (SELECT id FROM subnets WHERE subnets.subnet = :subnet);";

    	log.debug("vpnDao deleteSite: " + query.replace(":vpnName", vpnName).replace(":subnet",subnet));
    	return jdbc.update(query, params) == 1;
    }
    
    @Deprecated
    public boolean deleteSiteFromVpn(String vpnName, String siteName) {
    	return this.deleteSubnetFromVpn(vpnName, siteName);
//    	MapSqlParameterSource params = new MapSqlParameterSource();
//        params.addValue("vpnName", vpnName);
//        params.addValue("siteName", siteName);
//        
//        String query = "DELETE s2v FROM site2vpn s2v "
//                + "INNER JOIN vpns v ON s2v.vpnid = v.id "
//                + "INNER JOIN sites s ON s2v.siteid = s.id "
//                + "WHERE s.name = :siteName AND v.name = :vpnName ;";
//        
//        log.debug("vpnDao deleteSite: " + query);
//        return jdbc.update(query, params) == 1;
    }
}
