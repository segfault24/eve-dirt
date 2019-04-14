"use strict";

var region = 0; // default to All Regions
var type = 44992; // default to PLEX
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
		paging: true,
		pageLength: 30,
		bLengthChange: false,
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
	myAjax('types/' + type, function(result) {
		name = result.typeName;
		$('#title').html(name);
		$('head title', window.parent.document).text(name);
		$('#title-img').attr('src', 'https://imageserver.eveonline.com/Type/' + type + '_64.png');
		$('#open-in-game').attr('data-typeid', type);
	});

	// setup the history buttons' click handlers
	function resetHistoryTab(range) {
		historyRange = range;
		if(historyChart != null) {
			historyChart.destroy();
			historyChart = null;
		}
		historyTable.clear().draw();
		loadHistoryTab();
	}
	$('#dates-all').click(function(){ resetHistoryTab(0); });
	$('#dates-1y').click(function(){ resetHistoryTab(365); });
	$('#dates-6m').click(function(){ resetHistoryTab(182); });
	$('#dates-3m').click(function(){ resetHistoryTab(91); });
	$('#dates-1m').click(function(){ resetHistoryTab(30); });

	// setup region select handler
	$('#regionselect').val(region);
	$('#regionselect').change(function() {
		region = $('#regionselect').val();
		reloadData();
	});

	// demand load the tables and chart tabs
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
	$('#open-in-market-browser').click(function() { window.location = '/browse?type=' + type; });
	$('#open-in-import-analyzer').click(function() { window.location = '/import?type=' + type; });
	$('#open-in-station-trader').click(function() { window.location = '/station-trade?type=' + type; });
	$('#refresh-data').click(function() { reloadData(); });

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

	// update page title
	$('#title').text(name);
	$('head title', window.parent.document).text(name);
	$('#title-img').attr('src', 'https://imageserver.eveonline.com/Type/' + type + '_64.png');
	$('#open-in-game').attr('data-typeid', type);
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
		var xmin = 0.8*b[0].x - 5;
		var xmax = 1.2*s[0].x + 5;
		if (b[0].x > s[0].x) {
			xmin = 0.8*s[0].x - 5;
			xmax = 1.2*b[1].x + 5;
		}
		if(xmin < 0){ xmin = 0 };

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
						pointRadius: 0,
						lineTension: 0,
						borderWidth: myBorderWidth,
						borderColor: myColors.data[0].border,
						backgroundColor: myColors.data[0].fill
					},
					{
						label: 'Sell Vol',
						data: s,
						pointRadius: 0,
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
							fontColor: '#dddddd',
							callback: function(label, index, labels) {
								return formatKMBT(label, distributionKMBTx);
							}
						}
					}],
					yAxes: [{
						ticks: {
							min: 0,
							max: ymax,
							fontColor: '#dddddd',
							callback: function(label, index, labels) {
								return formatKMBT(label, distributionKMBTy);
							}
						}
					}]
				},
				legend: {
					display: false
				},
				tooltips: {
					mode: 'index',
					intersect: false
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
			h.push({t:day.date, y:parseFloat(day.highest)});
			a.push({t:day.date, y:parseFloat(day.average)});
			l.push({t:day.date, y:parseFloat(day.lowest)});
			v.push({t:day.date, y:parseInt(day.volume)});
			//sma30_temp.push(sma30[i]);
			//sma100_temp.push(sma100[i]);

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
						borderColor: myColors.data[1].border,
						backgroundColor: myColors.data[1].fill,
						yAxisID: 'y-axis-1'
					},
					{
						label: 'Average',
						data: a,
						//data: clean,
						fill: false,
						pointRadius: 0,
						lineTension: 0,
						borderWidth: myBorderWidth,
						borderColor: myColors.data[0].border,
						backgroundColor: myColors.data[0].fill,
						yAxisID: 'y-axis-1'
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
						backgroundColor: myColors.data[2].fill,
						yAxisID: 'y-axis-1'
					},
					{
						label: 'SMA30',
						data: sma30_temp,
						fill: false,
						pointRadius: 0,
						borderWidth: myBorderWidth,
						borderColor: myColors.stat[0].border,
						backgroundColor: myColors.stat[0].fill,
						yAxisID: 'y-axis-1'
					},
					{
						label: 'SMA100',
						data: sma100_temp,
						fill: false,
						pointRadius: 0,
						borderWidth: myBorderWidth,
						borderColor: myColors.stat[1].border,
						backgroundColor: myColors.stat[1].fill,
						yAxisID: 'y-axis-1'
					},
					{
						label: 'Volume',
						data: v,
						pointRadius: 0,
						borderWidth: myBorderWidth,
						borderColor: myColors.data[0].border,
						backgroundColor: myColors.data[0].fill,
						yAxisID: 'y-axis-2'
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
						position: 'bottom',
						ticks: {
							fontColor: '#dddddd'
						}
					}],
					yAxes: [
					{
						ticks: {
							//min: bounds.lower,
							//max: bounds.upper,
							fontColor: '#dddddd',
							callback: function(label, index, labels) {
								return formatKMBT(label, historyKMBT);
							}
						},
						id: "y-axis-1"
					},{
						type: "linear",
						display: true,
						position: "right",
						ticks: {
							fontColor: '#dddddd',
							callback: function(label, index, labels) {
								return formatKMBT(label, volumeKMBT);
							}
						},
						gridLines: {
							drawOnChartArea: false
						},
						id: "y-axis-2"
					}]
				},
				legend: {
					display: true,
					labels: {
						fontColor: '#dddddd'
					}
				},
				tooltips: {
					mode: 'index',
					intersect: false
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
	var tmpregion = region;
	if (tmpregion == 0) {
		// default to The Forge if "All Regions" is selected
		tmpregion = 10000002;
	}
	$.ajax({
		url: '/api/market/history/' + tmpregion + '/type/' + type,
		async: true,
		success: function(result) {
			historyData = result;

			// one-time processing
			var a = [];
			for(var i=0; i<historyData.length; i++) {
				a.push({t:historyData[i].date, y:parseFloat(historyData[i].average)});
			}

			var bounds = getOutlierBounds(a);
			var clean = removeOutliers(a, bounds);

			//sma30 = sma(clean, 30);
			//sma100 = sma(clean, 100);

			if(callback != null) {
				callback();
			}
		}
	});
}
