var nodes = new vis.DataSet();
var edges = new vis.DataSet();
// alert($.getJSON("./json/getNetwork"));

// $.getJSON("./json/getNetwork", function(data) {
$.getJSON("../topology/vis", function(data) {
	console.log("success");
	nodes.add(data.nodes);
	edges.add(data.edges);

	drawNet(nodes, edges);
}).done(function() {
	console.log("second success");
}).fail(function() {
	console.log("error");
	alert('could not get topology !');
}).always(function() {
	console.log("complete");

});

function drawContinuosly() {
	
	var shouldRedraw = false;
	
	//TODO debug and fix redrawing
	$.getJSON("../topology/isChanged", function(data) {
		if (data == true) {
			shouldRedraw = true;
		}
		else {
			shouldRedraw = false;
		}
	});
	
	if (shouldRedraw) {
		var nodes = new vis.DataSet();
		var edges = new vis.DataSet();
	
		$.getJSON("../topology/vis", function(data) {
			console.log("success");
			nodes.add(data.nodes);
			edges.add(data.edges);
	
			drawNet(nodes, edges);
		}).done(function() {
			console.log("second success");
		}).fail(function() {
			console.log("error");
			alert('could not get topology !');
		}).always(function() {
			console.log("complete");
	
		});
	}
}
  
  
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
	
    clusterSwitches(network);
    
    //colorNodeWithId(1);

	/*
	network.on("click", function (params) {
        params.event = "[original event]";
        document.getElementById('eventSpan').innerHTML = '<h2>Click event:</h2>' + JSON.stringify(params, null, 4);
		//document.getElementById('eventSpan').innerHTML( '<h2>Click event:</h2>' + JSON.stringify(params, null, 4) );
    }); */
};

function colorNodeWithId(idNumber, typeOfColoring) {
	var newColor;
	if (typeOfColoring == 1) {
		newColor = '#D2E5FF';
	}
	else {
		newColor = '#FF9289';
	}
    
    nodes.update([{id: idNumber, color:{background:newColor}}]);
}

function clusterSwitches(network) {
    var clusterOptions = {
        joinCondition:function(childOptions) {
            return childOptions.group == "SWITCH";
        },
        clusterNodeProperties: {id:'network', label:'network', borderWidth:3, shape:'eclipse', image:'images/cloud.png'}
    };
    network.cluster(clusterOptions);
}