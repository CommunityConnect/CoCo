package net.geant.coco.agent.portal.bgp;

public class BgpRouteEntry {
	
	private String prefix;
	private String rd;
	private String nexthop;
	private int label;
	private int routeTarget;
	
	public BgpRouteEntry(String prefix, String rd, String nexthop, int label, int routeTarget) {
		super();
		this.prefix = prefix;
		this.rd = rd;
		this.nexthop = nexthop;
		this.label = label;
		this.routeTarget = routeTarget;
	}

	public String getPrefix()
	{
		return this.prefix;
	}
	
	public String getRd()
	{
		return this.rd;
	}
	
	public String getNexthop()
	{
		return this.nexthop;		
	}
	
	public int getLabel()
	{
		return this.label;
	}
	
	public int getRouteTarget()
	{
		return this.routeTarget;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + label;
		result = prime * result + ((nexthop == null) ? 0 : nexthop.hashCode());
		result = prime * result + ((prefix == null) ? 0 : prefix.hashCode());
		result = prime * result + ((rd == null) ? 0 : rd.hashCode());
		result = prime * result + routeTarget;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BgpRouteEntry other = (BgpRouteEntry) obj;
		if (label != other.label)
			return false;
		if (nexthop == null) {
			if (other.nexthop != null)
				return false;
		} else if (!nexthop.equals(other.nexthop))
			return false;
		if (prefix == null) {
			if (other.prefix != null)
				return false;
		} else if (!prefix.equals(other.prefix))
			return false;
		if (rd == null) {
			if (other.rd != null)
				return false;
		} else if (!rd.equals(other.rd))
			return false;
		if (routeTarget != other.routeTarget)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "BgpRouteEntry [prefix=" + prefix + ", rd=" + rd + ", nexthop=" + nexthop + ", label=" + label + ", routeTarget=" + routeTarget + "]";
	}

	
	
}
