<html ng-app="dashboard">
  <head>
    <meta charset="utf8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />

    <title>Dashboard - Bonfire Chat </title>

    <meta rel="icon" href="/icon.png">

    <link rel="stylesheet" href="bower_components/bootstrap/dist/css/bootstrap.min.css">
    <link rel="stylesheet" href="bower_components/angular-chart.js/dist/angular-chart.css">
    <link rel="stylesheet" href="bower_components/leaflet/dist/leaflet.css">
  </head>
  <body>
    <div class="container">
      <nav class="navbar navbar-default">
        <div class="navbar-header">
          <a class="navbar-brand" href="javascript:">Bonfire Dashboard</a>
        </div>
        <ul class="nav navbar-nav">
	  <li><a href="/dashboard?p=main">Dashboard</a></li>
	  <li><a href="/dashboard?p=trace">Trace</a></li>
          <li class="pull-right"><a href="../">Zurück zur Homepage</a></li>
        </ul>
      </nav>

<?php if (!isset($_GET["p"]) || $_GET["p"] == "main"): ?>
      <div class="row" ng-controller="DashboardController as dashboard">
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
<?php endif; ?>

<?php if ($_GET["p"] == "trace"): ?>

<div class="row" ng-controller="TraceController ">

        <div class="col-md-2">
          <ul class="nav nav-pills nav-stacked">
            <li ng-repeat="tr in traceroutes" ng-class="{ active: tr.uuid == currentUuid }">
              <a href="javascript://" ng-click="loadTrace(tr.uuid)">{{tr.uuid|shorten}}</a>
            </li>
          </ul>
        </div>
<div class="col-md-10">
<div ng-hide="traceroutes">Eile mit Weile</div>
<div id="trcontent">

<p>
<button ng-repeat="retr in retrs" ng-click="filterRetr(retr)">{{retr}}</button>
</p>

<div id="cyto" style="height: 300px; border: 2px inset #888;"></div>

<table class="table">
<tr ng-repeat="line in tracecontent" class="action-{{line.action}}">
<td>{{line.reporter}}</td><td>{{line.action}}</td>
<td>{{line.client_ts|date:"HH:mm:ss"}}</td><td>{{line.protocol}}</td><td>{{line.peer}}</td><td>{{line.traceroute}}</td>
</tr></table>

</div>
</div>
</div>
<?php endif; ?>
<style>
#trcontent .action-RIGN td { color: #bbb; } /* dim ignored packets */

#trcontent span { display: block; font-size: 8pt; background: #eee; }
#trcontent span time {float: right; }
</style>

    </div>

    <script src="bower_components/angular/angular.min.js"></script>
    <script src="bower_components/Chart.js/Chart.min.js"></script>
    <script src="bower_components/angular-chart.js/angular-chart.js"></script>
    <script src="bower_components/leaflet/dist/leaflet.js"></script>
    <script src="bower_components/cytoscape/dist/cytoscape.min.js"></script>
    <script src="dashboard.js"></script>
  </body>
</html>
