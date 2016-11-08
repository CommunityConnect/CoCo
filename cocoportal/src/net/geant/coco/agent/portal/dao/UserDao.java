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
public class UserDao {
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

    public List<User> getUsers() {    	
    	String query = "SELECT * FROM users";
        log.trace(query);
        
        List<User> users = getUserList(query, null);
        
        return users;
    }

    public User getDomainAdmin(Domain domain){
    	MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("domainID", domain.getId());
        String query = "SELECT * FROM users WHERE domain = :domainID and admin = 1;";
        
        log.trace(query.replace(":domainID", "" + domain.getId()));
        
        List<User> users = getUserList(query, params);
        
        if (users.isEmpty()) {
            return null;
        }
        
        return users.get(0);
    }
    
    public User getUser(String userName) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("name", userName);
        
        String query = "SELECT * FROM users WHERE name = :name or email = :name;";
        log.trace(query.replace(":name", userName));
        
        List<User> users = getUserList(query, params);
        
        if (users.isEmpty()) {
            return null;
        }
        
        return users.get(0);
    }

    public User getUser(int userID) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("name", userID);
        
        String query = "SELECT * FROM users WHERE id = :name ;";
        log.trace(query.replace(":name", "'" + userID + "'"));
        
        List<User> users = getUserList(query, params);
        
        if (users.isEmpty()) {
            return null;
        }
        
        return users.get(0);
    }
    
    private List<User> getUserList(String query, SqlParameterSource params) {
    	if (params == null) {
    		return jdbc.query(query, new RowMapper<User>() {
                @Override
                public User mapRow(ResultSet rs, int rowNum) throws SQLException {
                    User user = new User();

                    user.setId(rs.getInt("id"));
                    user.setName(rs.getString("name"));
                    user.setAdmin(rs.getBoolean("admin"));
                    user.setDomain_id(rs.getInt("domain"));
                    user.setEmail(rs.getString("email"));
                    user.setSite_id(rs.getInt("site"));

                    return user;
                }
            });
    	}
    	else {
    		return jdbc.query(query, params, new RowMapper<User>() {
                @Override
                public User mapRow(ResultSet rs, int rowNum) throws SQLException {
                    User user = new User();

                    user.setId(rs.getInt("id"));
                    user.setName(rs.getString("name"));
                    user.setAdmin(rs.getBoolean("admin"));
                    user.setDomain_id(rs.getInt("domain"));
                    user.setEmail(rs.getString("email"));
                    user.setSite_id(rs.getInt("site"));


                    return user;
                }
            });
    	}
    	
    }
    
    /**
     * Insert a new USER in the MySQL database.
     * 
     * @param name
     *            Name of the USER to be inserted
     * @return Return true if the insertion succeeded, false otherwise
     */
    public boolean createUser(User user) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("name", user.getName());
        params.addValue("isPublic", 0);
        
        String query = "INSERT INTO users (`name`, `pathProtection`, `failoverType`, `isPublic`) "
                + "VALUES (:name, :pathProtection, :failoverType, :isPublic);";
        log.info("createUser " + query);
        log.info("createUser name=" + user.getName());
        
        return (jdbc.update(query, params) == 1);
    }
    
    /**
     * Deletes USER with a given id from a database.
     * On the database side removing sites from this USER should be implemented.
     * @param userId
     * @return
     */
    public boolean deleteUser(int userId) {
    	 MapSqlParameterSource params = new MapSqlParameterSource();
         params.addValue("id", userId);
         
         String query = "DELETE FROM users WHERE `id` = :id ;";
         log.info("deleteUser " + query);
         
         return (jdbc.update(query, params) == 1);
	}

    public boolean addSiteToUser(String userName, String siteName) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("userName", userName);
        params.addValue("siteName", siteName);

        String query = "INSERT INTO site2user (`userid`, `siteid`)"
                + "VALUES ("
                + "(SELECT id FROM users WHERE `name` = :userName),"
                + "(SELECT id FROM sites WHERE `name` = :siteName)"
                + ");";
        log.trace("userDao addSite: " + query);
        return jdbc.update(query, params) == 1;
    }

    public boolean deleteSiteFromUser(String userName, String siteName) {
    	MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("userName", userName);
        params.addValue("siteName", siteName);
        
        String query = "DELETE s2v FROM site2user s2v "
                + "INNER JOIN users v ON s2v.userid = v.id "
                + "INNER JOIN sites s ON s2v.siteid = s.id "
                + "WHERE s.name = :siteName AND v.name = :userName ;";
        
        log.trace("userDao deleteSite: " + query);
        return jdbc.update(query, params) == 1;
    }
}
