package net.geant.coco.agent.portal.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.geant.coco.agent.portal.dao.Domain;
import net.geant.coco.agent.portal.dao.DomainDao;
import net.geant.coco.agent.portal.dao.NetworkSite;
import net.geant.coco.agent.portal.dao.NetworkSiteDao;
import net.geant.coco.agent.portal.dao.User;
import net.geant.coco.agent.portal.dao.UserDao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("UsersService")
public class UsersService {
    private UserDao userDao;
    private DomainDao domainDao;
    private NetworkSiteDao siteDao;

    @Autowired
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }
    
    @Autowired
    public void setDomainDao(DomainDao domainDao) {
		this.domainDao = domainDao;
	}

    @Autowired
	public void setSiteDao(NetworkSiteDao siteDao) {
		this.siteDao = siteDao;
	}
    
    public User getDomainAdmin(Domain domain){
    	return userDao.getDomainAdmin(domain);
    }
    
    public User getUser(String userName) {
    	User user = userDao.getUser(userName);
    	user.setDomain(domainDao.getDomain(user.getDomain_id()));
		user.setSite(siteDao.getNetworkSite(user.getSite_id()));
		return user;
    }

    public User getUser(int userID) {
    	User user = userDao.getUser(userID);
    	user.setDomain(domainDao.getDomain(user.getDomain_id()));
		user.setSite(siteDao.getNetworkSite(user.getSite_id()));
		return user;
    }
    
	public Map<String, User> getUsersMap() {
    	List<User> usersList = this.getUsers();
        
    	Map<String, User> usersMap = new HashMap<>();
        for (User user : usersList) {
        	usersMap.put(user.getName(), user);
		}
        
		return usersMap;	
    }
    
    public List<User> getUsers() {
    	List<User> usersList = userDao.getUsers();
    	for (User user : usersList) {
    		user.setDomain(domainDao.getDomain(user.getDomain_id()));
    		user.setSite(siteDao.getNetworkSite(user.getSite_id()));
		}
        return usersList;
    }
}
