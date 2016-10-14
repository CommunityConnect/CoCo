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
public class DomainDao {
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

    public List<Domain> getDomains() {    	
    	//String query = "SELECT * FROM domains";
        String query = "SELECT * FROM domains";
                //+ "INNER JOIN ases ON domains.as = ases.id ";
        log.trace(query);
        
        List<Domain> domains = getDomainList(query, null);
        
        return domains;
    }
    
    @Deprecated
    public Domain getDomain(String domainName) {
    	// domain name is not supported anymore
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("name", domainName);
        
        //String query = "SELECT * FROM domains WHERE name = :name ;";
        String query = "SELECT * FROM domains"
                //+ "INNER JOIN ases ON domains.as = ases.id "
                + "AND name = :name ;";
        log.trace(query);
        
        List<Domain> domains = getDomainList(query, params);
        
        if (domains.isEmpty()) {
            return null;
        }
        
        return domains.get(0);
    }

    public Domain getDomain(int domainID) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("name", domainID);
        
        //String query = "SELECT * FROM domains WHERE id = :name ;";
        String query = "SELECT * FROM domains"
                //+ "INNER JOIN ases ON domains.as = ases.id "
                + "AND id = :name ;";
        log.trace(query);
        
        List<Domain> domains = getDomainList(query, params);
        
        if (domains.isEmpty()) {
            return null;
        }
        
        return domains.get(0);
    }
    
    private List<Domain> getDomainList(String query, SqlParameterSource params) {
    	if (params == null) {
    		return jdbc.query(query, new RowMapper<Domain>() {
                @Override
                public Domain mapRow(ResultSet rs, int rowNum) throws SQLException {
                    Domain domain = new Domain();

                    domain.setId(rs.getInt("id"));
                    domain.setBgp_ip(rs.getString("bgp_ip"));
                    //domain.setBgp_peer(rs.getString("bgp_peer"));
                    domain.setPortal_address(rs.getString("portal_address"));
                    domain.setAs_name(rs.getString("as_name"));
                    domain.setAs_num(rs.getInt("as_num"));

                    return domain;
                }
            });
    	}
    	else {
    		return jdbc.query(query, params, new RowMapper<Domain>() {
                @Override
                public Domain mapRow(ResultSet rs, int rowNum) throws SQLException {
                    Domain domain = new Domain();

                    domain.setId(rs.getInt("id"));
                    domain.setBgp_ip(rs.getString("bgp_ip"));
                    //domain.setBgp_peer(rs.getString("bgp_peer"));
                    domain.setPortal_address(rs.getString("portal_address"));
                    domain.setAs_name(rs.getString("as_name"));
                    domain.setAs_num(rs.getInt("as_num"));

                    return domain;
                }
            });
    	}
    	
    }
}
