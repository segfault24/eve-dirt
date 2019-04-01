"use strict";

var region = 0; // default to All Regions
var type = 587; // default to the Rifter
var name = '';

var orderData = null;
var historyData = null;

var sma30;
var sma100;

var sellTable;
var buyTable;
var orderTablesLoaded = false;
var historyTable;

var depthChart = null;
var distributionKMBTx;
var distributionKMBTy;
var historyChart = null;
var historyKMBT;
var volumeChart = null;
var volumeKMBT;

var historyRange = 182;

var menu;

$(document).ready(function(){
	// initialize tables
	buyTable = $('#buyorders').DataTable({
		columns: [
			{title:'Region', responsivePriority: 5},
			{title:'Station', responsivePriority: 2},
			{title:'Range', responsivePriority: 4},
			{title:'Price', responsivePriority: 1},
			{title:'Qt', responsivePriority: 3},
			{title:'Min', responsivePriority: 6}
		],
		order: [[3, "desc"]],
		searching: false,
		paging: true,
		pageLength: 40,
		bLengthChange: false,
		bInfo: false,
		responsive: true,
		select: true
	});
	sellTable = $('#sellorders').DataTable({
		columns: [
			{title:'Region', responsivePriority: 4},
			{title:'Station', responsivePriority: 2},
			{title:'Price', responsivePriority: 1},
			{title:'Qt', responsivePriority: 3}
		],
		order: [[2, "asc"]],
		searching: false,
		paging: true,
		pageLength: 40,
		bLengthChange: false,
		bInfo: false,
		responsive: true,
		select: true
	});
	historyTable = $('#rawhistory').DataTable({
		columns: [
			{title:'Date', responsivePriority: 1},
			{title:'Highest', responsivePriority: 2},
			{title:'Average', responsivePriority: 3},
			{title:'Lowest', responsivePriority: 4},
			{title:'Volume', responsivePriority: 5},
			{title:'Orders', responsivePriority: 6}
		],
		order: [[0, "desc"]],
		paging: false,
		bInfo: false,
		searching: false,
		responsive: true,
		select: true
	});

	// setup the table autoadjust
	$('a[data-toggle="tab"]').on('shown.bs.tab', function(e) {
		$.fn.dataTable.tables({visible: true, api: true}).columns.adjust();
	});

	// parse the initial region and type
	// keep the default if not set
	var t = getUrlParam('type');
	if(t != '') { type = t; }
	var r = getUrlParam('location');
	if(r != '') { region = r; }
	// grab the initial type's typeName
	// only necessary here since the menu passes the typeName on clicks
	$.ajax({
		url: '/api/types/' + type,
		success: function(result) {
			name = result.typeName;
			$('#title').html(name);
			$('#title-img').attr('src', 'https://imageserver.eveonline.com/Type/' + type + '_64.png');
		}
	});

	// setup the history buttons' click handlers
	function resetHistoryTab() {
		if(historyChart != null) {
			historyChart.destroy();
			historyChart = null;
		}
		if(volumeChart != null) {
			volumeChart.destroy();
			volumeChart = null;
		}
		historyTable.clear().draw();
		loadHistoryTab();
	}
	$('#dates-all').click(function(){
		historyRange = 0;
		resetHistoryTab();
	});
	$('#dates-6m').click(function(){
		historyRange = 182;
		resetHistoryTab();
	});
	$('#dates-3m').click(function(){
		historyRange = 91;
		resetHistoryTab();
	});
	$('#dates-1m').click(function(){
		historyRange = 30;
		resetHistoryTab();
	});

	// setup region select handler
	$('#regionselect').val(region);
	$('#regionselect').change(function() {
		region = $('#regionselect').val();
		reloadData();
	});

	$('#oig').click(function() { ajaxOpenInGame(type); });

	// setup the tab click handlers, so that the data and charts
	// that are needed for that tab are loaded on demand
	$('#selltablabel').click(function() { loadOrderTabs(); });
	$('#buytablabel').click(function() { loadOrderTabs(); });
	$('#depthtablabel').click(function() { loadDepthTab(); });
	$('#histtablabel').click(function() { loadHistoryTab(); });

	// generate the sidebar
	menu = new BrowserMenu();
	menu.onItemClick(function(t, n) {
		type = t;
		name = n;
		reloadData();
	});
	$('#open-in-market-browser').click(function() {});
	$('#open-in-import-analyzer').click(function() { window.location = '/import?type=' + type; });
	$('#open-in-station-trader').click(function() {});

	// capture history events
	$(window).bind('popstate', function(event) {
		var state = event.originalEvent.state;
		if(state != null) {
			region = state.location;
			type = state.type;
			name = state.name;
			reloadData();
		}
	});

	// load the data for the initial page
	reloadData();
});

// this function is called when the type or region is changed
// it only loads the currently visible tab, to reduce the
// amount of work done initially
function reloadData() {
	// null all old data
	orderData = null;
	historyData = null;

	// clear title, tables, and charts
	$('#title').html('Market Browser');
	sellTable.clear().draw();
	buyTable.clear().draw();
	orderTablesLoaded = false;
	historyTable.clear().draw();
	if(depthChart != null) {
		depthChart.destroy();
		depthChart = null;
	}
	if(historyChart != null) {
		historyChart.destroy();
		historyChart = null;
	}
	if(volumeChart != null) {
		volumeChart.destroy();
		volumeChart = null;
	}

	// update page title
	$('#title').html(name);
	$('#title-img').attr('src', 'https://imageserver.eveonline.com/Type/' + type + '_64.png');
	// expand menu to current item
	menu.expandTo(type);

	// find the active tab and load its data now
	switch($('.nav-tabs .active').text()) {
		case ' Sell':
		case ' Buy':
			loadOrderTabs();
			break;
		case ' Depth':
			loadDepthTab();
			break;
		case ' History':
			loadHistoryTab();
			break;
	}

	// push history state
	history.pushState(
		{'location':region, 'type':type, 'name':name},
		'',
		'/browse?location=' + region + '&type=' + type
	);
}

function loadOrderTabs() {
	if(orderData == null) {
		ajaxOrderData(loadOrderTabs);
	} else if(orderTablesLoaded) {
		return; // dont waste time re-rendering
	} else {
		// if there was no orders returned
		if(orderData.length == 0) {
			return;
		}

		for(var i=0; i<orderData.length; i++) {
			if(orderData[i].isBuyOrder==1) {
				buyTable.row.add([
					orderData[i].regionName,
					orderData[i].sName,
					orderData[i].range,
					formatIsk(orderData[i].price),
					formatInt(orderData[i].volumeRemain),
					formatInt(orderData[i].minVolume)
				]);
			} else {
				sellTable.row.add([
					orderData[i].regionName,
					orderData[i].sName,
					formatIsk(orderData[i].price),
					formatInt(orderData[i].volumeRemain)
				]);
			}
		}

		buyTable.draw();
		sellTable.draw();
		$.fn.dataTable.tables({visible: true, api: true}).columns.adjust();

		orderTablesLoaded = true;
	}
}

function loadDepthTab() {
	if(orderData == null) {
		ajaxOrderData(loadDepthTab);
	} else if(depthChart != null) {
		return; // dont waste time re-rendering
	} else {
		// if there was no orders returned
		if(orderData.length == 0) {
			return;
		}

		// sort the orders by price
		orderData.sort(function(x, y) {
			return x.price - y.price;
		});

		// make the datasets
		var b = [];
		var s = [];
		var ymax = 1;

		var sum = 0;
		for(var i=0; i<orderData.length; i++) {
			if(orderData[i].isBuyOrder == 0) {
				sum += parseInt(orderData[i].volumeRemain);
				s.push({x:parseFloat(orderData[i].price), y:sum});
				if(s.length>1 && parseFloat(orderData[i].price)>1.2*s[0].x+5) {
					break;
				}
			}
		}
		if(sum > ymax) {
			ymax = sum;
		}

		sum = 0;
		for(var i=orderData.length-1; i>=0; i--) {
			if(orderData[i].isBuyOrder == 1) {
				sum += parseInt(orderData[i].volumeRemain);
				b.push({x:parseFloat(orderData[i].price), y:sum});
				if(b.length>1 && parseFloat(orderData[i].price)<0.8*b[0].x-5) {
					break;
				}
			}
		}
		if(sum > ymax) {
			ymax = sum;
		}

		// set the final bounds
		var xmin = 0.8*b[0].x - 5; if(xmin<0){xmin=0};
		var xmax = 1.2*s[0].x + 5;

		// calculate the parameters for axis labels
		distributionKMBTx = getKMBTParams([{y:xmin},{y:xmax}]);
		distributionKMBTy = getKMBTParams([{y:0},{y:ymax}]);

		// make the chart
		depthChart = new Chart($("#orderdepth"), {
			type: 'line',
			data: {
				datasets: [
					{
						label: 'Buy Vol',
						data: b,
						pointRadius: 4,
						lineTension: 0,
						borderWidth: myBorderWidth,
						borderColor: myColors.data[0].border,
						backgroundColor: myColors.data[0].fill
					},
					{
						label: 'Sell Vol',
						data: s,
						pointRadius: 4,
						lineTension: 0,
						borderWidth: myBorderWidth,
						borderColor: myColors.data[1].border,
						backgroundColor: myColors.data[1].fill
					}
				]
			},
			options: {
				responsive: true,
				scales: {
					xAxes: [{
						type: 'linear',
						position: 'bottom',
						ticks: {
							min: xmin,
							max: xmax,
							callback: function(label, index, labels) {
								return formatKMBT(label, distributionKMBTx);
							}
						}
					}],
					yAxes: [{
						ticks: {
							min: 0,
							max: ymax,
							callback: function(label, index, labels) {
								return formatKMBT(label, distributionKMBTy);
							}
						}
					}]
				},
				legend: {
					display: false
				}
			}
		});
	}
}

function loadHistoryTab() {
	if(historyData == null) {
		ajaxHistoryData(loadHistoryTab);
	} else if(historyChart != null) {
		return; // dont waste time re-rendering
	} else {
		// if there was no history returned
		if(historyData.length == 0) {
			return;
		}

		// range = 0 == show all data
		if(historyRange<=0) { historyRange = historyData.length; }

		// make the datasets
		var h = [];
		var a = [];
		var l = [];
		var v = [];
		var sma30_temp = [];
		var sma100_temp = [];
		var start = historyData.length-historyRange;
		var end = historyData.length;
		for(var i=start; i<end; i++) {
			var day = historyData[i];
			//dateSqlToJs(day.date);
			h.push({x:i, y:parseFloat(day.highest)});
			a.push({x:i, y:parseFloat(day.average)});
			l.push({x:i, y:parseFloat(day.lowest)});
			v.push({x:i, y:parseInt(day.volume)});
			sma30_temp.push(sma30[i]);
			sma100_temp.push(sma100[i]);

			historyTable.row.add([
				(day.date).split(' ')[0],
				formatIsk(day.highest),
				formatIsk(day.average),
				formatIsk(day.lowest),
				day.volume,
				day.orderCount
			]);
		}

		var bounds = getOutlierBounds(a);
		var clean = removeOutliers(a, bounds);

		historyKMBT = getKMBTParams(clean);
		volumeKMBT = getKMBTParams(v);

		// historical prices
		historyChart = new Chart($("#itemhistory"), {
			type: 'line',
			data: {
				datasets: [
					{
						label: 'Highest',
						data: h,
						//data: removeOutliers(h, bounds),
						fill: false,
						pointRadius: 0,
						lineTension: 0,
						borderWidth: myBorderWidth,
						borderColor: myColors.data[0].border,
						backgroundColor: myColors.data[0].fill
					},
					{
						label: 'Average',
						data: a,
						//data: clean,
						fill: false,
						pointRadius: 0,
						lineTension: 0,
						borderWidth: myBorderWidth,
						borderColor: myColors.data[1].border,
						backgroundColor: myColors.data[1].fill
					},
					{
						label: 'Lowest',
						data: l,
						//data: removeOutliers(l, bounds),
						fill: false,
						pointRadius: 0,
						lineTension: 0,
						borderWidth: myBorderWidth,
						borderColor: myColors.data[2].border,
						backgroundColor: myColors.data[2].fill
					},
					{
						label: 'SMA30',
						data: sma30_temp,
						fill: false,
						pointRadius: 0,
						borderWidth: myBorderWidth,
						borderColor: myColors.stat[0].border,
						backgroundColor: myColors.stat[0].fill
					},
					{
						label: 'SMA100',
						data: sma100_temp,
						fill: false,
						pointRadius: 0,
						borderWidth: myBorderWidth,
						borderColor: myColors.stat[1].border,
						backgroundColor: myColors.stat[1].fill
					}
				]
			},
			options: {
				responsive: true,
				scales: {
					xAxes: [{
						type: 'time',
						time: {
							displayFormats: myDisplayFormatYr
						},
						position: 'bottom'
					}],
					yAxes: [{
						ticks: {
							//min: bounds.lower,
							//max: bounds.upper,
							callback: function(label, index, labels) {
								return formatKMBT(label, historyKMBT);
							}
						}
					}]
				},
				legend: {
					display: true
				}
			}
		});

		// trade volume chart
		volumeChart = new Chart($("#tradevolume"), {
			type: 'line',
			data: {
				datasets: [
					{
						label: 'Volume',
						data: v,
						pointRadius: 0,
						borderWidth: myBorderWidth,
						borderColor: myColors.data[0].border,
						backgroundColor: myColors.data[0].fill
					}
				]
			},
			options: {
				responsive: true,
				scales: {
					xAxes: [{
						type: 'time',
						time: {
							displayFormats: myDisplayFormatYr
						},
						position: 'bottom'
					}],
					yAxes: [{
						ticks: {
							beginAtZero: true,
							callback: function(label, index, labels) {
								return formatKMBT(label, volumeKMBT);
							}
						}
					}]
				},
				legend: {
					display: false
				}
			}
		});

		// raw data table
		historyTable.draw();
		$.fn.dataTable.tables({visible: true, api: true}).columns.adjust();
	}
}

function ajaxOrderData(callback) {
	$.ajax({
		url: '/api/market/orders/' + region + '/type/' + type,
		async: true,
		success: function(result) {
			orderData = result;

			if(callback != null) {
				callback();
			}
		}
	});
}

function ajaxHistoryData(callback) {
	$.ajax({
		url: '/api/market/history/' + region + '/type/' + type,
		async: true,
		success: function(result) {
			historyData = result;

			// one-time processing
			var a = [];
			for(var i=0; i<historyData.length; i++) {
				a.push({x:i, y:parseFloat(historyData[i].average)});
			}

			var bounds = getOutlierBounds(a);
			var clean = removeOutliers(a, bounds);

			sma30 = sma(clean, 30);
			sma100 = sma(clean, 100);

			if(callback != null) {
				callback();
			}
		}
	});
}

function ajaxOpenInGame(type) {
	$.ajax({
		url: '/api/market/open-in-game/' + type,
		async: true
	});
}
