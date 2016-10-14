package net.geant.coco.agent.portal.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

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
public class SubnetDao {
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

    public List<Subnet> getSubnets() {    	
    	String query = "SELECT * FROM subnets";
        log.trace(query);
        
        List<Subnet> subnets = getSubnetList(query, null);
        
        return subnets;
    }
    
    public Subnet getSubnet(String subnetName) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("name", subnetName);
        
        String query = "SELECT * FROM subnets WHERE name = :name ;";
        log.trace(query);
        
        List<Subnet> subnets = getSubnetList(query, params);
        
        if (subnets.isEmpty()) {
            return null;
        }
        
        return subnets.get(0);
    }

    public Subnet getSubnet(int subnetID) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("name", subnetID);
        
        String query = "SELECT * FROM subnets WHERE id = :name ;";
        log.trace(query);
        
        List<Subnet> subnets = getSubnetList(query, params);
        
        if (subnets.isEmpty()) {
            return null;
        }
        
        return subnets.get(0);
    }
    
    public List<Subnet> getUserSubnets(User user){
    	return this.getUserSubnets(user.getId());
    }
    
    public List<Subnet> getUserSubnets(int user_id){
    	MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("user_id", user_id);
        
        String query = "SELECT * FROM subnets "
                + "INNER JOIN subnetUsers ON subnets.id = subnetUsers.subnet "
                + "AND subnetUsers.user = :user_id ;";
        log.trace(query);
        
        List<Subnet> subnets = getSubnetList(query, params);
        
        if (subnets.isEmpty()) {
            return null;
        }
        return subnets;
    }
    
    public List<Subnet> getVpnSubnets(Vpn vpn){
    	return this.getVpnSubnets(vpn.getId());
    }
    
    public List<Subnet> getVpnSubnets(int vpn_id){
    	MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("vpn_id", vpn_id);
        
        String query = "SELECT * FROM subnets "
                + "INNER JOIN vpnSubnet ON subnets.id = vpnSubnet.subnet "
                + "AND vpnSubnet.vpn_id = :vpn_id ;";
        log.trace(query);
        
        List<Subnet> subnets = getSubnetList(query, params);
        
        if (subnets.isEmpty()) {
            return null;
        }
        return subnets;
    }
    
    private List<Subnet> getSubnetList(String query, SqlParameterSource params) {
    	if (params == null) {
    		return jdbc.query(query, new RowMapper<Subnet>() {
                @Override
                public Subnet mapRow(ResultSet rs, int rowNum) throws SQLException {
                    Subnet subnet = new Subnet();

                    subnet.setId(rs.getInt("id"));
                    subnet.setSubnet(rs.getString("subnet"));
                    subnet.setSite_id(rs.getInt("site"));
                    

                    return subnet;
                }
            });
    	}
    	else {
    		return jdbc.query(query, params, new RowMapper<Subnet>() {
                @Override
                public Subnet mapRow(ResultSet rs, int rowNum) throws SQLException {
                    Subnet subnet = new Subnet();

                    subnet.setId(rs.getInt("id"));
                    subnet.setSubnet(rs.getString("subnet"));
                    subnet.setSite_id(rs.getInt("site"));

                    return subnet;
                }
            });
    	}
    	
    }
}
