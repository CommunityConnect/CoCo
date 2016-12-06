package net.geant.coco.agent.portal.dao;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BgpDao {
    private NamedParameterJdbcTemplate jdbc;
    
    @Autowired
    public void setDataSource(DataSource jdbc) {
        this.jdbc = new NamedParameterJdbcTemplate(jdbc);
    }
    
    public List<Bgp> getBgps() {
        String query = "SELECT * FROM bgps";
        log.debug(query);
        
        return getBgpList(query, null);
    }
    
    public List<Bgp> getBgps(int vpn_id) {
    	MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("vpn_id", vpn_id);
    	
        String query = "SELECT * FROM bgps WHERE vpn = :vpn_id";
        log.debug(query.replace(":vpn_id", "" + vpn_id));
        
        return getBgpList(query, params);
    }
     
    private List<Bgp> getBgpList(String query, SqlParameterSource params) {
    	
    	RowMapper<Bgp> mapper = new RowMapper<Bgp>() {
            @Override
            public Bgp mapRow(ResultSet rs, int rowNum) throws SQLException {
            	Bgp bgp = new Bgp();

            	bgp.setId(rs.getInt("id"));
            	bgp.setLocal_domain_id(rs.getInt("localDomain"));
            	bgp.setRemote_domain_id(rs.getInt("remoteDomain"));
            	bgp.setHash(rs.getString("hash"));
            	bgp.setNonce(rs.getString("nonce"));
            	bgp.setSubnet_id(rs.getInt("subnet"));
            	bgp.setTarget(rs.getString("target"));
            	bgp.setVpn_id(rs.getInt("vpn"));
            	bgp.setAnnounce(rs.getString("announce"));
            	
            	
            	
                return bgp;
            }
        };
    	
    	if (params == null) {
    		return jdbc.query(query, mapper);
    	}
    	else {
    		return jdbc.query(query, params, mapper);
    	}
    	
    }
    
    public Bgp getBgpByTarget(String target) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("target", target);
        
        String query = "SELECT * FROM bgps "
                + "WHERE target = :target ;";
        log.debug(query.replace(":target", "\"" + target + "\""));
        
        List<Bgp> bgps = getBgpList(query, params);
        
        if (bgps.isEmpty()) {
            return null;
        }
        
        return bgps.get(0);
    }
    
    public Bgp getBgp(int bgp_id) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("id", bgp_id);
        
        String query = "SELECT * FROM bgps "
                + "WHERE id = :id ;";
        log.debug(query.replace(":id", ""+ bgp_id));
        
        List<Bgp> bgps = getBgpList(query, params);
        
        if (bgps.isEmpty()) {
            return null;
        }
        
        return bgps.get(0);
    }
    
    public boolean updateBgp(Bgp bgp) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        
        params.addValue("id", bgp.getId());
        params.addValue("localDomain", bgp.getLocal_domain_id());
        params.addValue("remoteDomain", bgp.getRemote_domain_id());
        params.addValue("hash", bgp.getHash());
        params.addValue("nonce", bgp.getNonce());
        params.addValue("subnet", bgp.getSubnet_id());
        params.addValue("target", bgp.getTarget());
        params.addValue("vpn", bgp.getVpn_id());
        params.addValue("announce", bgp.getAnnounce());
        
        // lets check if we are missing parameters
        // in case of missing parameters lets make it null
        for (String key : params.getValues().keySet()){
        	Object value = params.getValues().get(key);
        	if (value == null){
        		params.addValue(key, null);
        	} else if (Integer.class.isInstance(value)){
        		if ((int)value < 0){
        			params.addValue(key, null);
        		}
        	}
        }
        
        
        String query = "INSERT INTO bgps (`id`, `localDomain`, `remoteDomain`, `hash`, `nonce`, `subnet`, `target`, `vpn`, `announce`) "
                + "VALUES (:id, :localDomain, :remoteDomain, :hash, :nonce, :subnet, :target, :vpn, :announce);";
        
        //TODO check if we update or create!
        Bgp old_bgp = this.getBgp(bgp.getId());
        
        if (old_bgp != null){
        	query = "UPDATE bgps SET "
        			+ "`localDomain`=:localDomain, "
        			+ "`remoteDomain`=:remoteDomain, "
        			+ "`hash`=:hash, "
        			+ "`nonce`=:nonce, "
        			+ "`subnet`=:subnet, "
        			+ "`target`=:target, "
        			+ "`vpn`=:vpn, "
        			+ "`announce`=:announce "
        			+ "WHERE `id`=:id;";
        	params.addValue("id", old_bgp.getId());
        }
        
        String query_print = new String(query);
        for (Map.Entry<String, Object> entry : params.getValues().entrySet()) {
        	//log.info("createVpn Key = " + entry.getKey() + ", Value = " + entry.getValue());
        	if (entry.getValue() != null){
        		query_print = query_print.replace(":" + entry.getKey(), entry.getValue().toString());
        	}
        }
        
        log.info("updateBgp: " + query_print);
        
        return (jdbc.update(query, params) == 1);
    }
}
