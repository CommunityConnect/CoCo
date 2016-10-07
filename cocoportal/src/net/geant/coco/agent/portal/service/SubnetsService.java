package net.geant.coco.agent.portal.service;

import java.util.List;

import net.geant.coco.agent.portal.dao.Domain;
import net.geant.coco.agent.portal.dao.NetworkLink;
import net.geant.coco.agent.portal.dao.NetworkLinkDao;
import net.geant.coco.agent.portal.dao.Subnet;
import net.geant.coco.agent.portal.dao.SubnetDao;
import net.geant.coco.agent.portal.dao.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Service;

@Service("subnetsService")
public class SubnetsService {
    private SubnetDao subnetDao;

    
    @Autowired
    public void setSubnetDao(SubnetDao subnetDao) {
		this.subnetDao = subnetDao;
	}

    public List<Subnet> getSubnets() {
        return subnetDao.getSubnets();
    }
}
