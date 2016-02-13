package net.geant.coco.agent.portal.utils;

import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;

public class DijkstraTests {

	public static DefaultDirectedWeightedGraph<String, DefaultWeightedEdge> graph = new DefaultDirectedWeightedGraph<String, DefaultWeightedEdge>(DefaultWeightedEdge.class);
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		graph.addEdge("s1", "s2");
		

	}

}
