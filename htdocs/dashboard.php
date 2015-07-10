<html ng-app="dashboard">
  <head>
    <meta charset="utf8">
    <title>Dashboard - Bonfire Chat </title>
    <meta rel="icon" href="/icon.png">
    <link rel="stylesheet" href="bower_components/bootstrap/dist/css/bootstrap.min.css">
    <link rel="stylesheet" href="bower_components/angular-chart.js/dist/angular-chart.css">
    <link rel="stylesheet" href="bower_components/leaflet/dist/leaflet.css">
    <script src="bower_components/angular/angular.min.js"></script>
    <script src="bower_components/Chart.js/Chart.min.js"></script>
    <script src="bower_components/angular-chart.js/angular-chart.js"></script>
    <script src="bower_components/leaflet/dist/leaflet.js"></script>
    <script src="dashboard.js"></script>
    <style>

    </style>
    <link rel="stylesheet" href="https://static.luelistan.net/bootstrap-3.3.2-dist/css/bootstrap.min.css">
  </head>
  <body>
    <nav class="navbar navbar-default">
      <div class="navbar-header">
	<a class="navbar-brand" href="javascript:">Bonfire Dashboard</a>
      </div>
    </nav>
    <div class="container" ng-controller="DashboardController as dashboard">

      <div class="row">
	<div class="col-md-5">
	  <div class="panel panel-default">
	    <div class="panel-heading">Power Usage over Time</div>
	    <div class="panel-body">
	      <canvas id="line" class="chart chart-line" data="data" labels="labels" legend="true" 
		      click="onClick" hover="onHover" series="series" colours='["#F7464A", "#97BBCD", "#46BFBD"]'></canvas>
	    </div>
	  </div>
	  
	  

	</div>
	<div class="col-md-5">
	  <div class="panel panel-default">
	    <div class="panel-heading">Locations</div>
	    <div class="panel-body">
	      <div id="map" style="height:380px"></div>

	    </div>
	  </div>
	  
	</div>
	<div class="col-md-2">
	  <ul class="nav nav-pills nav-stacked">
	    <li ng-repeat="device in devices" ng-class="{ active: device == curDevice }">
	      <a href="javascript://">{{device.name}}</a>
	    </li>
	  </ul>
	</div>
      </div>
      
    </div>
  </body>
</html>

