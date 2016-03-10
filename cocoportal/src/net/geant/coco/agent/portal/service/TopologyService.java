package net.geant.coco.agent.portal.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.geant.coco.agent.portal.dao.NetworkElement;
import net.geant.coco.agent.portal.dao.NetworkInterface;
import net.geant.coco.agent.portal.dao.TopologyDao;

@Service("topologyService")
public class TopologyService {

	private TopologyDao topologyDao;

	@Autowired
	public void setNetworkSwitchDao(TopologyDao topologyDao) {
		this.topologyDao = topologyDao;
	}

	private List<NetworkInterface> getNetworkInterfaces() {
		List<NetworkInterface> if_enni = topologyDao.getNetworkInterfaces_INNI();
		List<NetworkInterface> if_inni = topologyDao.getNetworkInterfaces_ENNI();
		List<NetworkInterface> if_uni = topologyDao.getNetworkInterfaces_UNI();

		List<NetworkInterface> interfaces = new ArrayList<NetworkInterface>();
		interfaces.addAll(if_enni);
		interfaces.addAll(if_inni);
		interfaces.addAll(if_uni);

		return interfaces;
	}

	public String getTopologyJsonVis() {
		StringBuilder visJson = new StringBuilder();

		Set<NetworkElement> nodeSet = new HashSet<NetworkElement>();

		List<NetworkInterface> networkInterfaces = getNetworkInterfaces();

		for (NetworkInterface networkInterface : networkInterfaces) {
			if (!nodeSet.contains(networkInterface.source)) {
				nodeSet.add(networkInterface.source);
			}

			if (!nodeSet.contains(networkInterface.neighbour)) {
				nodeSet.add(networkInterface.neighbour);
			}
		}

		visJson.append("{\"nodes\" : [ ");
		for (NetworkElement networkElement : nodeSet) {
			int fakeId = 0;
			if (networkElement.nodeType.equals(NetworkElement.NODE_TYPE.CUSTOMER)) {
				fakeId = networkElement.getId();
			} else if (networkElement.nodeType.equals(NetworkElement.NODE_TYPE.EXTERNAL_AS)) {
				fakeId = 100 + networkElement.getId();

				// TODO continue to ignore external as sites
				continue;

			} else if (networkElement.nodeType.equals(NetworkElement.NODE_TYPE.SWITCH)) {
				fakeId = 200 + networkElement.getId();
			}
			visJson.append("{\"id\": \"");
			visJson.append(fakeId);
			visJson.append("\", \"label\": \"");
			visJson.append(networkElement.getName());
			visJson.append("\", \"group\": \"");
			visJson.append(networkElement.nodeType);
			visJson.append("\"}, ");
		}

		visJson.deleteCharAt(visJson.lastIndexOf(","));
		visJson.append("],");
		visJson.append("\"edges\" : [");

		for (NetworkInterface networkInterface : networkInterfaces) {
			int fakeId = 0;
			NetworkElement networkElement;

			networkElement = networkInterface.source;
			fakeId = 0;
			if (networkElement.nodeType.equals(NetworkElement.NODE_TYPE.CUSTOMER)) {
				fakeId = networkElement.getId();
			} else if (networkElement.nodeType.equals(NetworkElement.NODE_TYPE.EXTERNAL_AS)) {
				fakeId = 100 + networkElement.getId();
			} else if (networkElement.nodeType.equals(NetworkElement.NODE_TYPE.SWITCH)) {
				fakeId = 200 + networkElement.getId();
			}
			visJson.append("{\"from\": \"");
			visJson.append(fakeId);

			networkElement = networkInterface.neighbour;
			fakeId = 0;
			if (networkElement.nodeType.equals(NetworkElement.NODE_TYPE.CUSTOMER)) {
				fakeId = networkElement.getId();
			} else if (networkElement.nodeType.equals(NetworkElement.NODE_TYPE.EXTERNAL_AS)) {
				fakeId = 100 + networkElement.getId();
			} else if (networkElement.nodeType.equals(NetworkElement.NODE_TYPE.SWITCH)) {
				fakeId = 200 + networkElement.getId();
			}
			visJson.append("\", \"to\": \"");
			visJson.append(fakeId);
			visJson.append("\"}, ");
		}
		visJson.deleteCharAt(visJson.lastIndexOf(","));
		visJson.append("]}");

		return visJson.toString();
	}
}
