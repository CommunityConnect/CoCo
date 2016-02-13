<%@ page language="java" contentType="text/html; charset=US-ASCII"
	pageEncoding="US-ASCII"%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="sf" uri="http://www.springframework.org/tags/form"%>

<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=US-ASCII">
<title>Manage VPNs</title>
</head>
<body>

	<h2>
		Managing VPN:
		<c:out value="${vpnname}"></c:out>
	</h2>

	<sf:form method="post"
		action="${pageContext.request.contextPath}/doupdatevpn"
		commandName="vpn">

		<button name="deletevpn" value="<c:out value="${vpnname}"></c:out>">delete
			VPN</button>

		<h3>Sites in this VPN:</h3>

		<input type="hidden" name="vpn"
			value="<c:out value="${vpnname}"></c:out>">
		<c:forEach var="site" items="${sites}">
			<c:out value="${site.name}"></c:out>
			<button name="deletesite"
				value="<c:out value="${site.name}"></c:out>">delete</button>
			<br>
		</c:forEach>

		<h3>Free sites:</h3>
		<c:forEach var="site" items="${freesites}">
			<c:out value="${site.name}"></c:out>
			<button name="addsite" value="<c:out value="${site.name}"></c:out>">add</button>
			<br>
		</c:forEach>
		
		
		<h3>External switches:</h3>
		<c:forEach var="sw" items="${ext_switches}">
			<c:out value="${sw.name}"></c:out> - BGP neighbour: <c:out value="${sw.bgpIp}"></c:out>
			<button name="addswitch" value="<c:out value="${sw.name}"></c:out>">add</button>
			<br>
		</c:forEach> 
		<p>
			<button name="done" value="done">done</button>
		</p>
	</sf:form>

</body>
</html>