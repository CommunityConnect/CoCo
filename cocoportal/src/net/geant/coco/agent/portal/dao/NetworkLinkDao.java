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

@Component
public class NetworkLinkDao {
    private NamedParameterJdbcTemplate jdbc;

    @Autowired
    public void setDataSource(DataSource jdbc) {
        this.jdbc = new NamedParameterJdbcTemplate(jdbc);
    }

    // TODO: (Simon) this still needs to be fixed (remove sitelinks) but i did not found essential code that uses that function
    @Deprecated
    public List<NetworkLink> getNetworkLinks() {
        /* old code with sitelinks
         * String query = "SELECT links.id AS id, "
                + "switch1.x AS fromX, switch1.y AS fromY, "
                + "switch2.x AS toX, switch2.y AS toY " + "FROM links "
                + "INNER JOIN switches AS switch1 "
                + "ON (links.from = switch1.id) "
                + "INNER JOIN switches AS switch2 "
                + "ON (links.to = switch2.id) " + "UNION "
                + "SELECT sitelinks.id AS id, "
                + "sites.x AS fromX, sites.y AS fromY, "
                + "switches.x AS toX, switches.y AS toY " + "FROM sitelinks "
                + "INNER JOIN sites " + "ON (sitelinks.site = sites.id) "
                + "INNER JOIN switches " + "ON (sitelinks.switch = switches.id);";
        */
        
        String query = "SELECT links.id AS id, "
                + "switch1.x AS fromX, switch1.y AS fromY, "
                + "switch2.x AS toX, switch2.y AS toY " + "FROM links "
                + "INNER JOIN switches AS switch1 "
                + "ON (links.from = switch1.id) "
                + "INNER JOIN switches AS switch2 "
                + "ON (links.to = switch2.id) " + "UNION "
                + "SELECT sitelinks.id AS id, "
                + "sites.x AS fromX, sites.y AS fromY, "
                + "switches.x AS toX, switches.y AS toY " + "FROM sitelinks "
                + "INNER JOIN sites " + "ON (sitelinks.site = sites.id) "
                + "INNER JOIN switches " + "ON (sitelinks.switch = switches.id);";

        //System.out.println("links query: " + query);

        return jdbc.query(query, new RowMapper<NetworkLink>() {

            @Override
            public NetworkLink mapRow(ResultSet rs, int rowNum)
                    throws SQLException {
                NetworkLink networkLink = new NetworkLink();

                networkLink.setId(rs.getInt("id"));
                networkLink.setFromX(rs.getInt("fromX"));
                networkLink.setFromY(rs.getInt("fromY"));
                networkLink.setToX(rs.getInt("toX"));
                networkLink.setToY(rs.getInt("toY"));

                return networkLink;
            }

        });
    }

    public NetworkLink getNetworkLinks(int id) {

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("id", id);
        String query = "SELECT links.id AS id, "
                + "switch1.x AS fromX, switch1.y AS fromY, "
                + "switch2.x AS toX, switch2.y AS toY " + "FROM links "
                + "INNER JOIN switches AS switch1 "
                + "ON (links.from = switch1.id) "
                + "INNER JOIN switches AS switch2 "
                + "ON (links.to = switch2.id) " + "WHERE links.id = :id;";

        return jdbc.queryForObject(query, params, new RowMapper<NetworkLink>() {

            @Override
            public NetworkLink mapRow(ResultSet rs, int rowNum)
                    throws SQLException {
                NetworkLink networkLink = new NetworkLink();

                networkLink.setId(rs.getInt("id"));
                networkLink.setFromX(rs.getInt("fromX"));
                networkLink.setFromY(rs.getInt("fromY"));
                networkLink.setToX(rs.getInt("toX"));
                networkLink.setToY(rs.getInt("toY"));

                return networkLink;
            }

        });
    }
}
