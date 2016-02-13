package net.geant.coco.agent.portal.utils;

import org.jgrapht.graph.DefaultWeightedEdge;


public class CoCoLink extends DefaultWeightedEdge {
	private String id;
	private String srcNode;
	private String srcTp;
	private String srcTpNr;
	private String dstNode;
	private String dstTp;
	private String dstTpNr;
	public double weight;
	
	public CoCoLink(String id, String srcNode, String srcTp, String dstNode, String dstTp) {
		this.id = id;
		this.srcNode = srcNode;
		this.srcTp = srcTp;
		this.dstNode = dstNode;
		this.dstTp = dstTp;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getSrcNode() {
		return srcNode;
	}

	public void setSrcNode(String srcNode) {
		this.srcNode = srcNode;
	}

	public String getSrcTp() {
		return srcTp;
	}

	public void setSrcTp(String srcTp) {
		this.srcTp = srcTp;
	}

	public String getDstNode() {
		return dstNode;
	}

	public void setDstNode(String dstNode) {
		this.dstNode = dstNode;
	}

	public String getDstTp() {
		return dstTp;
	}

	public void setDstTp(String dstTp) {
		this.dstTp = dstTp;
	}

	public String getSrcTpNr() {
		return srcTpNr;
	}

	public void setSrcTpNr(String srcTpNr) {
		this.srcTpNr = srcTpNr;
	}

	public String getDstTpNr() {
		return dstTpNr;
	}

	public void setDstTpNr(String dstTpNr) {
		this.dstTpNr = dstTpNr;
	}
	
}
