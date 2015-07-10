angular.module("dashboard", ["chart.js"])
    .controller("DashboardController", ["$scope", "$http", function ($scope, $http) {

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
//	});

	// Initialize position map
	var map = L.map("map").setView([49.87168, 8.66083], 13);
	var mapboxId = "luelistan.kp794i2e";
	var myMapLayer = L.tileLayer('https://api.tiles.mapbox.com/v4/' + mapboxId + '/{z}/{x}/{y}.jpg?access_token=pk.eyJ1IjoibHVlbGlzdGFuIiwiYSI6InFsbmR0bUEifQ.4TLJEcdIV4X_j9H8fspQrQ', { attribution: 'Mapbox | &copy; <a href="http://osm.org/copyright">OpenStreetMap</a> contributors' });
	myMapLayer.addTo(map);
	
//	$http.get("/api/v1/stats?mode=device").success(function(result) {
	    var devices = result.data;
	    for(var i in devices) {
		var d = devices[i];
		L.marker([+d.lat, +d.lng]).addTo(map);
	    }
	});
    }]);
    
