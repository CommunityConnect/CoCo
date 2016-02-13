package net.geant.coco.agent.portal.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import net.geant.coco.agent.portal.dao.NetworkElement.NODE_TYPE;
import net.geant.coco.agent.portal.dao.NetworkInterface.IF_TYPE;

@Component
public class TopologyDao {

	private NamedParameterJdbcTemplate jdbc;

    @Autowired
    public void setDataSource(DataSource jdbc) {
        this.jdbc = new NamedParameterJdbcTemplate(jdbc);
    }

    public List<NetworkInterface> getNetworkInterfaces_ENNI() {
        String query = "select switches.id, switches.name, "
        		+ "ases.id AS as_id, ases.as_name AS as_name, ases.bgp_ip "
        		+ "from switches "
        		+ "INNER JOIN ext_links ON switches.id=ext_links.switch "
        		+ "INNER JOIN ases ON ext_links.as=ases.id;";

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
        String query = "select switches.id, switches.name, "
        		+ "sites.id AS site_id, sites.name AS site_name from switches "
        		+ "INNER JOIN sitelinks ON switches.id=sitelinks.switch "
        		+ "INNER JOIN sites ON sitelinks.site=sites.id;";

        //System.out.println("links query: " + query);

        List<NetworkInterface> networkInterfaces = jdbc.query(query, new RowMapper<NetworkInterface>() {

            @Override
            public NetworkInterface mapRow(ResultSet rs, int rowNum)
                    throws SQLException {
            	
            	NetworkElement networkElementFrom = new NetworkElement(rs.getInt("id"), rs.getString("name"), NODE_TYPE.SWITCH);
            	NetworkElement networkElementTo = new NetworkElement(rs.getInt("site_id"), rs.getString("site_name"), NODE_TYPE.CUSTOMER);
            	
            	NetworkInterface networkLink = new NetworkInterface(networkElementFrom, networkElementTo, IF_TYPE.UNI);

                return networkLink;
            }

        });
       
        return networkInterfaces;
    }
}
