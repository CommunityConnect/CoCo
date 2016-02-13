package net.geant.coco.agent.portal.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class VpnDao {
    private NamedParameterJdbcTemplate jdbc;

    @Autowired
    public void setDataSource(DataSource jdbc) {
        this.jdbc = new NamedParameterJdbcTemplate(jdbc);
    }

    public List<Vpn> getVpns() {
        // VPN with id equal to 1 is special; it contains all free sites
        return jdbc.query("SELECT * FROM vpns WHERE id != 1",
                new RowMapper<Vpn>() {
                    @Override
                    public Vpn mapRow(ResultSet rs, int rowNum)
                            throws SQLException {
                        Vpn vpn = new Vpn();

                        vpn.setId(rs.getInt("id"));
                        vpn.setName(rs.getString("name"));
                        vpn.setMplsLabel(rs.getInt("mpls_label"));

                        return vpn;
                    }
                });
    }

    public Vpn getVpn(String vpnName) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("name", vpnName);
        String query = "SELECT * FROM vpns WHERE name = :name ;";
        log.trace(query);
        List<Vpn> vpns = jdbc.query(query, params, new RowMapper<Vpn>() {
            @Override
            public Vpn mapRow(ResultSet rs, int rowNum) throws SQLException {
                Vpn vpn = new Vpn();

                vpn.setId(rs.getInt("id"));
                vpn.setName(rs.getString("name"));
                vpn.setMplsLabel(rs.getInt("mpls_label"));

                return vpn;
            }
        });
        if (vpns.isEmpty()) {
            return null;
        }
        return vpns.get(0);
    }

    public Vpn getVpn(int vpnID) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("name", vpnID);
        String query = "SELECT * FROM vpns WHERE id = :name ;";
        log.trace(query);
        List<Vpn> vpns = jdbc.query(query, params, new RowMapper<Vpn>() {
            @Override
            public Vpn mapRow(ResultSet rs, int rowNum) throws SQLException {
                Vpn vpn = new Vpn();

                vpn.setId(rs.getInt("id"));
                vpn.setName(rs.getString("name"));
                vpn.setMplsLabel(rs.getInt("mpls_label"));

                return vpn;
            }
        });
        if (vpns.isEmpty()) {
            return null;
        }
        return vpns.get(0);
    }
    
    public boolean addSite(String vpnName, String siteName) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("vpn", vpnName);
        params.addValue("site", siteName);

        String query = "UPDATE site2vpn "
                + "INNER JOIN vpns ON site2vpn.vpnid = vpns.id "
                + "INNER JOIN sites ON site2vpn.siteid = sites.id, "
                + "(SELECT id FROM vpns WHERE name=:vpn) vpn  "
                + "SET site2vpn.vpnid = vpn.id  WHERE sites.name = :site ;";
        log.trace("vpnDao addSite: " + query);
        return jdbc.update(query, params) == 1;
    }

    public boolean deleteSite(String siteName) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("site", siteName);

        String query = "UPDATE site2vpn "
                + "INNER JOIN vpns ON site2vpn.vpnid = vpns.id "
                + "INNER JOIN sites ON site2vpn.siteid = sites.id "
                + "SET site2vpn.vpnid = 1  WHERE sites.name = :site ;";
        log.trace("vpnDao deleteSite: " + query);
        return jdbc.update(query, params) == 1;
    }
}
