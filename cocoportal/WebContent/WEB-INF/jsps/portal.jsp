<%@ page language="java" contentType="text/html; charset=US-ASCII"
	pageEncoding="US-ASCII"%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="sf" uri="http://www.springframework.org/tags/form"%>

<!DOCTYPE html>
<html>
<head>
<link href="${pageContext.request.contextPath}/static/css/main.css"
	rel="stylesheet" type="text/css" />
<link href="${pageContext.request.contextPath}/static/css/coco.css"
	rel="stylesheet" type="text/css" />
	
	<style>
	    #mynetwork {
            width: 100%;
            height: 400px;
            border: none;
            background-color: none;
        }
	</style>    
	<script src="${pageContext.request.contextPath}/static/vis/dist/vis.js"></script>
  	<link href="${pageContext.request.contextPath}/static/vis/dist/vis.css" rel="stylesheet" type="text/css" />

    <script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.3/jquery.min.js"></script>
    
<meta http-equiv="Content-Type" content="text/html; charset=US-ASCII">
<title>Insert title here</title>
</head>
<body>
	<div id="logos">
		<div class="logo">
			<img
				src="${pageContext.request.contextPath}/static/images/SURFnet.pdf"
				height="63" style="margin-bottom: 10px" />
		</div>
		<div class="logo"></div>
		<div class="logo">
			<img src="${pageContext.request.contextPath}/static/images/tno.png"
				height="63" style="margin-bottom: 10px" />
		</div>
		<div class="logo">
			<img src="${pageContext.request.contextPath}/static/images/geant.png"
				height="63" style="margin-bottom: 10px" />
		</div>
		<div class="logo">
			<img src="${pageContext.request.contextPath}/static/images/UvA.pdf"
				height="63" style="margin-bottom: 10px" />
		</div>
		<div>
			<img src="${pageContext.request.contextPath}/static/images/pica.png"
				height="63" style="margin-bottom: 10px" />
		</div>
	</div>
	
	<div id="mynetwork"></div>
	<script type="text/javascript">

	var nodes
	var edges
		//alert($.getJSON("./json/getNetwork"));
		
		//$.getJSON("./json/getNetwork2", function(data) {
		//$.getJSON("http://localhost:8080/CoCo-agent/topology/vis", function(data) {
		$.getJSON("${pageContext.request.contextPath}/topology/vis", function(data) {
	  console.log( "success" );
	  	nodes = data.nodes;
		edges = data.edges;
		
		drawNet(nodes,edges);
	})
	  .done(function() {
	    console.log( "second success" );
	  })
	  .fail(function() {
	    console.log( "error" );
		alert('could not get topology !');
	  })
	  .always(function() {
	    console.log( "complete" );
		

		
		
	  });

	  
	  
	function drawNet(nodes,edges) {  
	    var color = 'gray';
	    var len = undefined;

		/*
	     var nodes = [
	        {id: "a", label: "Site A", group: 'site'},
	        {id: 1, label: "Site B", group: 'site'},
	        {id: 2, label: "Switch a", group: 'netswitch'},
	        {id: 3, label: "Switch b", group: 'netswitch'},
	        {id: 4, label: "Switch c", group: 'netswitch'},
	        {id: 5, label: "Enni", group: 'enni'},
	      
	    ];
	    var edges = [
	        {from: 2, to: 3},
	        {from: 3, to: 4},
	        {from: 4, to: 5},
	        {from: "a", to: 2},
			{from: 1, to: 4},
	        
	    ]	*/
	  
	    // create a network
	    var container = document.getElementById('mynetwork');
	    var data = {
	        nodes: nodes,
	        edges: edges
	    };
	    var options = {
	        interaction: {
				zoomView: false,
				dragView: false,
				multiselect: false,
				selectConnectedEdges: false
			},
			layout: {
				randomSeed:2
			},
			nodes: {
	            shape: 'dot',
	            size: 20,
	            font: {
	                size: 15,
	                color: '#000000'
	            },
	            borderWidth: 2
	        },
	        edges: {
	            width: 2
	        },
	        groups: {
	            site: {
	                color: {background:'red',border:'black'},
	                shape: 'diamond'
	            },
	            netswitch: {
	                label: "I'm a dot!",
	                shape: 'dot',
	                color: 'cyan'
	            },
	            enni: {color:'rgb(0,255,140)'},
	            
	        }
	    };
	    network = new vis.Network(container, data, options);
		
		network.on("click", function (params) {
	        params.event = "[original event]";
	        document.getElementById('eventSpan').innerHTML = '<h2>Click event:</h2>' + JSON.stringify(params, null, 4);
			//document.getElementById('eventSpan').innerHTML( '<h2>Click event:</h2>' + JSON.stringify(params, null, 4) );
	    });
	};
	</script>

	<h1>User controlled provisioning of VPNs on OpenFlow switches
		using OpenDaylight.</h1>

	<div id="legend">

		<div id="vpns">
			<div id="vpns-inner">
				<h3>Provisioned VPNs:</h3>
				<form action="${pageContext.request.contextPath}/updatevpn"
					method="post">
					<c:forEach var="vpn" items="${vpns}">
						<input type='radio' name='showvpn'
							value='<c:out value="${vpn.name}"></c:out>'
							onclick='this.form.submit()'>
						<c:out value="${vpn.name}"></c:out>
						<button name="vpn" value="<c:out value="${vpn.name}"></c:out>">update</button>
						<br>
					</c:forEach>
					<button name="newvpn" value="newvpn">Create new VPN</button>
				</form>
			</div>
		</div>
	</div>

<!-- 
<div id="visualization"></div>

<script type="text/javascript">
  var container = document.getElementById('visualization');

  // create an array with nodes
  var nodes = new vis.DataSet([
    {id: 1, label: 'Node 1'},
    {id: 2, label: 'Node 2'},
    {id: 3, label: 'Node 3'},
    {id: 4, label: 'Node 4'},
    {id: 5, label: 'Node 5'}
  ]);

  // create an array with edges
  var edges = new vis.DataSet([
    {from: 1, to: 3},
    {from: 1, to: 2},
    {from: 2, to: 4},
    {from: 2, to: 5}
  ]);

  var data = {
    nodes: nodes,
    edges: edges
  };
  var options = {};
  var network = new vis.Network(container, data, options);
</script> -->

	<canvas id="canvas" width="870px" height="924px"></canvas>

	<script>
		var ctx = document.getElementById('canvas').getContext("2d");
		var img = new Image();
		img.src = "${pageContext.request.contextPath}/static/images/nederland.png";
		img.onload = function() {
			ctx.drawImage(img, 0, 0);

			// draw links between switches
			var lineWidth = 3;
			ctx.lineWidth = lineWidth;
			ctx.strokeStyle = 'black';
			ctx.beginPath();
			<c:forEach var="networkLink" items="${links}">
			ctx.moveTo(<c:out value="${networkLink.fromX}"></c:out>,
					<c:out value="${networkLink.fromY}"></c:out>);
			ctx.lineTo(<c:out value="${networkLink.toX}"></c:out>,
					<c:out value="${networkLink.toY}"></c:out>);
			ctx.stroke();
			</c:forEach>

			// draw all switches
			ctx.font = "12px Helvetica";
			ctx.textAlign = 'center';
			var fontHeight = 12;
			var switchWidth = 75;
			var switchHeight = switchWidth / 2;
			<c:forEach var="networkSwitch" items="${switches}">
			ctx.fillStyle = 'red';
			ctx.fillRect(<c:out value="${networkSwitch.x}"></c:out>
					- switchWidth / 2,
					<c:out value="${networkSwitch.y}"></c:out> - switchHeight
							/ 2, switchWidth, switchHeight);
			ctx.fillStyle = 'black';
			ctx
					.fillText('<c:out value="${networkSwitch.name}"></c:out>',
							<c:out value="${networkSwitch.x}"></c:out>,
							<c:out value="${networkSwitch.y}"></c:out>
									+ fontHeight / 6);
			</c:forEach>

			// draw all sites
			ctx.font = "12px Helvetica";
			ctx.textAlign = 'center';
			var fontHeight = 12;
			var siteWidth = 75;
			var siteHeight = siteWidth / 2;
			<c:forEach var="networkSite" items="${sites}">
			ctx.fillStyle = 'lightblue';
			ctx.fillRect(<c:out value="${networkSite.x}"></c:out> - siteWidth
					/ 2, <c:out value="${networkSite.y}"></c:out> - siteHeight
					/ 2, siteWidth, siteHeight);
			ctx.fillStyle = 'black';
			ctx.fillText('<c:out value="${networkSite.name}"></c:out>',
					<c:out value="${networkSite.x}"></c:out>,
					<c:out value="${networkSite.y}"></c:out> + fontHeight / 6);
			</c:forEach>
		}
	</script>
</body>
</html>