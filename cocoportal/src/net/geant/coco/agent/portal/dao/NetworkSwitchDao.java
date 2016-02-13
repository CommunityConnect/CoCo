/**
 * 
 */
package net.geant.coco.agent.portal.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * @author rvdp
 *
 */
@Component
public class NetworkSwitchDao {
    private NamedParameterJdbcTemplate jdbc;
    
    @Autowired
    public void setDataSource(DataSource jdbc) {
        this.jdbc = new NamedParameterJdbcTemplate(jdbc);
    }
    
    public List<NetworkSwitch> getNetworkSwitches() {

        return jdbc.query("select * from switches", new RowMapper<NetworkSwitch>() {

            @Override
            public NetworkSwitch mapRow(ResultSet rs, int rowNum) throws SQLException {
                NetworkSwitch networkSwitch = new NetworkSwitch();

                networkSwitch.setId(rs.getInt("id"));
                networkSwitch.setName(rs.getString("name"));
                networkSwitch.setX(rs.getInt("x"));
                networkSwitch.setY(rs.getInt("y"));
                networkSwitch.setMplsLabel(rs.getInt("mpls_label"));

                return networkSwitch;
            }

        });
    }
    
    public NetworkSwitch getNetworkSwitches(int id) {

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("id", id);

        return jdbc.queryForObject("select * from switches where id = :id",
                params, new RowMapper<NetworkSwitch>() {

                    @Override
                    public NetworkSwitch mapRow(ResultSet rs, int rowNum)
                            throws SQLException {
                        NetworkSwitch networkSwitch = new NetworkSwitch();

                        networkSwitch.setId(rs.getInt("id"));
                        networkSwitch.setName(rs.getString("name"));
                        networkSwitch.setX(rs.getInt("x"));
                        networkSwitch.setY(rs.getInt("y"));
                        networkSwitch.setMplsLabel(rs.getInt("mpls_label"));

                        return networkSwitch;
                    }

                });
    }
    
    public List<NetworkSwitch> getNetworkSwitchesWithNni() {

        MapSqlParameterSource params = new MapSqlParameterSource();

        return jdbc.query("select switches.id, switches.name, switches.x, switches.y, switches.mpls_label, ases.bgp_ip from switches INNER JOIN ext_links ON switches.id=ext_links.switch INNER JOIN ases ON ext_links.as=ases.id",
                new RowMapper<NetworkSwitch>() {

                    @Override
                    public NetworkSwitch mapRow(ResultSet rs, int rowNum)
                            throws SQLException {
                    	NetworkSwitchExt networkSwitch = new NetworkSwitchExt();

                        networkSwitch.setId(rs.getInt("id"));
                        networkSwitch.setName(rs.getString("name"));
                        networkSwitch.setX(rs.getInt("x"));
                        networkSwitch.setY(rs.getInt("y"));
                        networkSwitch.setMplsLabel(rs.getInt("mpls_label"));
                        networkSwitch.setBgpIp(rs.getString("bgp_ip"));
                        return networkSwitch;
                    }

                });
    }


}
