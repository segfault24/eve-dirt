"use strict";

var srcRegion = 10000002; // default to The Forge
var dstRegion = 10000042; // default to Metropolis
var type = 29668; // default to PLEX

var itemData = null;
var srcHistoryData = null;
var dstHistoryData = null;

var historyChart = null;
var magnitude;

var menu;

$(document).ready(function() {
	var t = getUrlParam('type');
	if(t != '') { type = t; }
	var s = getUrlParam('src');
	if(s != '') { srcRegion = s; }
	var d = getUrlParam('dst');
	if(d != '') { dstRegion = d; }

	// setup region select handlers
	$('#srcregionselect').val(srcRegion);
	$('#srcregionselect').change(function() {
		srcRegion = $('#srcregionselect').val();
		reloadData();
	});
	$('#dstregionselect').val(dstRegion);
	$('#dstregionselect').change(function() {
		dstRegion = $('#dstregionselect').val();
		reloadData();
	});

	// setup the tab click handlers, so that the data and charts
	// that are needed for that tab are loaded on demand
	$('#analysistablabel').click(function() { loadAnalysisTab(); });
	$('#histtablabel').click(function() { loadHistoryChart(); });

	// generate the sidebar
	menu = new BrowserMenu();
	menu.onItemClick(function(t) {
		type = t;
		reloadData();
	});
	$('#open-in-market-browser').click(function() { window.location = '/browse?type=' + type; });
	$('#open-in-import-analyzer').click(function() {});
	$('#open-in-station-trader').click(function() {});

	// load the data for the initial page
	reloadData();
});

function reloadData() {
	// null all old data
	itemData = null;
	srcHistoryData = null;
	dstHistoryData = null;

	// clear title, tables, and charts
	$('#title').text('Import Analysis');
	if(historyChart != null) {
		historyChart.destroy();
		historyChart = null;
	}

	// load the item's info
	loadItemInfo();

	// find the active tab and load its data now
	switch($('.nav-tabs .active').text()) {
		case ' Analysis':
			loadAnalysisTab();
			break;
		case ' Historical':
			loadHistoryChart();
			break;
	}

	// push history state
	history.pushState(
		{"srcregionid":srcRegion, "dstregionid":dstRegion, "typeid":type},
		'',
		'/import?src=' + srcRegion + '&dst=' + dstRegion + '&type=' + type
	);
}

function loadItemInfo() {
	if(itemData == null) {
		ajaxItemData(loadItemInfo);
	} else {
		// update page title
		$('#title').html('<img id="title-img"> ' + itemData.typeName);
		$('#title-img').attr('src', 'https://imageserver.eveonline.com/Type/' + type + '_64.png');

		// expand menu to current item
		menu.expandTo(itemData.m);
	}
}

function loadAnalysisTab() {

}

function loadHistoryChart() {
	if(srcHistoryData == null) {
		ajaxSrc(loadHistoryChart);
	} else if(dstHistoryData == null) {
		ajaxDst(loadHistoryChart);
	} else if(historyChart != null) {
		return; // dont waste time re-rendering
	} else {
		// if there was no history returned
		if(srcHistoryData.length == 0 || dstHistoryData.length == 0) {
			return;
		}

		var diff = [];
		for(var i=0; i<srcHistoryData.length && i<dstHistoryData.length; i++) {
			var d = (1-0.0361)*parseFloat(dstHistoryData[i].highest) - parseFloat(srcHistoryData[i].highest);
			diff.push({x:i, y:d});
		}

		var bounds = getOutlierBounds(diff);
		var clean = removeOutliers(diff, bounds);

		var sma50 = sma(clean, 50);
		var sma100 = sma(clean, 100);

		magnitude = getKMBTParams(clean);

		historyChart = new Chart($("#analysis-graph"), {
			type: 'line',
			data: {
				datasets: [
					{
						label: 'Diff',
						data: clean,
						pointRadius: 0,
						borderWidth: myBorderWidth,
						borderColor: myColors.data[0].border,
						backgroundColor: myColors.data[0].fill
					},
					{
						label: 'SMA50',
						data: sma50,
						fill: false,
						pointRadius: 0,
						borderWidth: myBorderWidth,
						borderColor: myColors.stat[0].border,
						backgroundColor: myColors.stat[0].fill
					},
					{
						label: 'SMA100',
						data: sma100,
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
							beginAtZero: true,
							min: Math.round(bounds.lower),
							max: Math.round(bounds.upper),
							callback: function(label, index, labels) {
								return formatKMBT(label, magnitude);
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

function ajaxItemData(callback) {
	$.ajax({
		url: '/api/types/' + type,
		async: true,
		success: function(result) {
			itemData = result;

			if(callback != null) {
				callback();
			}
		}
	});
}

function ajaxSrc(callback) {
	$.ajax({
		url: '/api/market/history/' + srcRegion+ '/type/' + type,
		async: true,
		success: function(result) {
			srcHistoryData = result;

			if(callback != null) {
				callback();
			}
		}
	});
}

function ajaxDst(callback) {
	$.ajax({
		url: '/api/market/history/' + dstRegion+ '/type/' + type,
		async: true,
		success: function(result) {
			dstHistoryData = result;

			if(callback != null) {
				callback();
			}
		}
	});
}
