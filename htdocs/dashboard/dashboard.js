angular.module("dashboard", ["chart.js"])
    .controller("DashboardController", ["$scope", "$http", "$interval", function ($scope, $http, $interval) {

	$scope.curDevice = null;
	
	// Initialize Power consumption chart
	$http.get("/api/v1/stats?mode=chart").then(function(result) {
	    var data = result.data;
	    
	    $scope.labels = [];
	    $scope.data = [ [], [], []  ];
	    function add(max, avg, min) {
		$scope.data[0].unshift(max);
		$scope.data[1].unshift(avg);
		$scope.data[2].unshift(min);
	    }
	    
	    var hour = (new Date()).getHours();
	    for(var i = 0; i < 24; i++) {
		$scope.labels.unshift(hour+":00");
		var line = data.filter(function(x) { return x.stathour == hour })[0]; 
		if (line) add(Math.round(line.max_powerusage*10)/10, Math.round(line.avg_powerusage*10)/10, Math.round(line.min_powerusage*10)/10);
		else add(null, null, null);
		
		hour --; if(hour == -1) hour=23;
	    }
	    $scope.series = ['Max', 'Avg', 'Min'];

	    

	});

	$http.get("/api/v1/stats?mode=devices").then(function(result) {
	    $scope.devices = result.data;

	

	// Initialize position map
	var map = L.map("map").setView([49.87168, 8.66083], 13);
	var mapboxId = "luelistan.kp794i2e";
	var myMapLayer = L.tileLayer('https://api.tiles.mapbox.com/v4/' + mapboxId + '/{z}/{x}/{y}.jpg?access_token=pk.eyJ1IjoibHVlbGlzdGFuIiwiYSI6InFsbmR0bUEifQ.4TLJEcdIV4X_j9H8fspQrQ', { attribution: 'Mapbox | &copy; <a href="http://osm.org/copyright">OpenStreetMap</a> contributors' });
	myMapLayer.addTo(map);
	

	    var devices = result.data;
	    for(var i in devices) {
		var d = devices[i];
		L.marker([+d.lat, +d.lng]).addTo(map);
	    }
	});
    }])

.controller("TraceController", ["$scope", "$http", "$interval", function ($scope, $http, $interval) {
  var lastCount = -1;

  $interval(function() {
    loadList();
    if ($scope.currentUuid) $scope.loadTrace($scope.currentUuid);
  }, 5000);
  
  function loadList() {
    $http.get("/api/v1/traceroute").success(function(ok) {
      $scope.traceroutes = ok;
    });
  }

  loadList();

  $scope.currentUuid = "";
  $scope.loadTrace = function(uuid) {
    if (uuid != $scope.currentUuid) lastCount = -1;
    $scope.currentUuid = uuid;
    $http.get("/api/v1/traceroute?uuid=" + uuid).success(function (html) {
      if (html.length == lastCount) return;
      lastCount = html.length;
      
      $scope.tracecontent = html;
      initCytoScape(html);
      
    });
  }
  
  function initCytoScape(traceData) {
    var insertedNodes = {};
    var nodes = [];
    var edges = [];
    
    var index = 0;
    for(var i in traceData) {
      var row = traceData[i];
      if (row.hop_from && !insertedNodes[row.hop_from])
        nodes.push({ data: { id: row.hop_from }});
      if (row.hop_to && !insertedNodes[row.hop_to])
        nodes.push({ data: { id: row.hop_to }});
      
      insertedNodes[row.hop_from] = true;
      insertedNodes[row.hop_to] = true;

      var classes = "";
      if (row.traceroute.indexOf(':ACK')>0) classes += "ack";
      if (row.action=='RIGN') classes += "ign";
      edges.push({ 
        data: { id: 'edge_' + index, source: row.hop_from, target: row.hop_to },
        classes: classes
      });
      index++;
    }
    
    var cy = cytoscape({
      container: document.getElementById("cyto"),
      
      elements: {
        nodes: nodes,
        edges: edges,
      },
    
      // so we can see the ids
      style: [
        {
          selector: 'node',
          style: {
            'content': 'data(id)'
          }
        },
        {
          selector: 'edge',
          style: {
            'target-arrow-shape': 'triangle',
            'line-color': 'black', 'target-arrow-color': 'black'
          }
        },
        {
          selector: 'edge.ack',
          style: {
            'line-color': 'green', 'target-arrow-color': 'green'
          }
        },
        {
          selector: 'edge.ign',
          style: {
            'line-color': '#bbb', 'target-arrow-color': 'gray'
          }
        }
      ]
    });
    cy.layout({
      
      layout: 'breadthfirst',
    });
    
  }

    


}])


.config(function($filterProvider) {
   $filterProvider.register("shorten", function() {
	return function(x) {
		return x.substr(0,8);
	};
   });
})

;
    
