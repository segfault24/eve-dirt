"use strict";

$(document).ready(function(){

	var region = 0; // default to All Regions
	var type = 44992; // default to PLEX
	var name = '';

	// parse the initial region and type
	// keep the default if not set
	var t = getUrlParam('type');
	if(t != '') { type = t; }
	var r = getUrlParam('location');
	if(r != '') { region = r; }

	// initialize tables
	var buyTable = $('#buyorders').DataTable({
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
	var sellTable = $('#sellorders').DataTable({
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
	var historyTable = $('#rawhistory').DataTable({
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

	// grab the initial type's typeName
	// only necessary here since the menu passes the typeName on clicks
	$.getJSON('/api/types/' + type, function(result) {
		name = result.typeName;
		$('#title').html(name);
		$('head title', window.parent.document).text(name);
		$('#title-img').attr('src', 'https://imageserver.eveonline.com/Type/' + type + '_64.png');
		$('#open-in-game').attr('data-typeid', type);
	});

	// setup region select handler
	$('#regionselect').val(region);
	$('#regionselect').change(function() {
		region = $('#regionselect').val();
		refreshData();
	});

	// generate the sidebar
	var menu = new BrowserMenu();
	menu.onItemClick(function(t, n) {
		type = t;
		name = n;
		refreshData();
	});

	var ordersLoaded = false;
	var historyLoaded = false;
	$('#selltablabel').click(function() { loadOrders(); });
	$('#buytablabel').click(function() { loadOrders(); });
	$('#depthtablabel').click(function() { loadOrders(); });
	$('#histtablabel').click(function() { loadHistory(); });

	// inter-tool links
	$('#open-in-market-browser').click(function() { window.location = '/browse?type=' + type; });
	$('#open-in-import-analyzer').click(function() { window.location = '/import?type=' + type; });
	$('#open-in-station-trader').click(function() { window.location = '/station-trade?type=' + type; });
	$('#refresh-data').click(function() { refreshData(); });

	// capture history events
	$(window).bind('popstate', function(event) {
		var state = event.originalEvent.state;
		if(state != null) {
			region = state.location;
			type = state.type;
			name = state.name;
			refreshData();
		}
	});

	// this function is called when the type or region is changed
	// it only loads the currently visible tab, to reduce the
	// amount of work done initially
	function refreshData() {
		console.log('refresh ' + type + ' ' + name);

		// clear title, tables, and charts
		sellTable.clear().draw();
		buyTable.clear().draw();
		historyTable.clear().draw();
		ordersLoaded = false;
		historyLoaded = false;

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
			case ' Depth':
				loadOrders();
				break;
			case ' History':
				loadHistory();
				break;
		}

		// push history state
		history.pushState(
			{'location':region, 'type':type, 'name':name},
			'',
			'/browse?location=' + region + '&type=' + type
		);
	}

	function loadOrders() {
		if(ordersLoaded) {
			return;
		}

		var myOrderIds = [];
		$.getJSON('/api/wallet/orderids', function(result) {
			for(const ido of result) {
				myOrderIds.push(ido.orderId);
			}
		});

		$.getJSON('/api/market/orders/' + region + '/type/' + type, function(result) {
			var orderData = result;

			// sort the orders by price
			orderData.sort(function(x, y) {
				return x.price - y.price;
			});

			// build the order tables
			for(var i=0; i<orderData.length; i++) {
				var row;
				if(orderData[i].isBuyOrder==1) {
					row = buyTable.row.add([
						orderData[i].regionName,
						orderData[i].sName,
						orderData[i].range,
						formatIsk(orderData[i].price),
						formatInt(orderData[i].volumeRemain),
						formatInt(orderData[i].minVolume)
					]);
				} else {
					row = sellTable.row.add([
						orderData[i].regionName,
						orderData[i].sName,
						formatIsk(orderData[i].price),
						formatInt(orderData[i].volumeRemain)
					]);
				}
				if (myOrderIds.includes(orderData[i].orderId)) {
					row.node().classList.add('highlight');
				}
			}
			buyTable.draw();
			sellTable.draw();
			$.fn.dataTable.tables({visible: true, api: true}).columns.adjust();

			// generate the depth datasets
			var bids = [];
			var bidsum = 0;
			for(var i=orderData.length-1; i>=0; i--) {
				if(orderData[i].isBuyOrder == 1) {
					if(bids.length<1 || parseFloat(orderData[i].price)>0.75*bids[0][0]-5) {
						bidsum += parseInt(orderData[i].volumeRemain);
						bids.unshift([parseFloat(orderData[i].price), bidsum]);
					}
				}
			}
			var asks = [];
			var asksum = 0;
			for(var i=0; i<orderData.length; i++) {
				if(orderData[i].isBuyOrder == 0) {
					if(asks.length<1 || parseFloat(orderData[i].price)<1.25*asks[0][0]+5) {
						asksum += parseInt(orderData[i].volumeRemain);
						asks.push([parseFloat(orderData[i].price), asksum]);
					}
				}
			}

			Highcharts.chart('depthchart', {
				chart: {
					type: 'area',
					zoomType: 'xy'
				},
				title:{
					text: ''
				},
				xAxis: {
					minPadding: 0,
					maxPadding: 0,
					plotLines: [{
						color: '#888',
						value: 0.1523,
						width: 1,
						label: {
							text: 'Actual price',
							rotation: 90
						}
					}],
					title: {
						text: 'Price'
					}
				},
				yAxis: [{
					lineWidth: 1,
					gridLineWidth: 1,
					title: null,
					tickWidth: 1,
					tickLength: 5,
					tickPosition: 'inside',
					labels: {
						align: 'left',
						x: 8
					}
				}, {
					opposite: true,
					linkedTo: 0,
					lineWidth: 1,
					gridLineWidth: 0,
					title: null,
					tickWidth: 1,
					tickLength: 5,
					tickPosition: 'inside',
					labels: {
						align: 'right',
						x: -8
					}
				}],
				legend: {
					enabled: false
				},
				plotOptions: {
					area: {
						fillOpacity: 0.2,
						lineWidth: 1,
						step: 'center'
					}
				},
				tooltip: {
					headerFormat: '<span style="font-size=10px;">Price: {point.key}</span><br/>',
					valueDecimals: 2
				},
				series: [{
					name: 'Bids',
					data: bids,
					color: '#03a7a8'
				}, {
					name: 'Asks',
					data: asks,
					color: '#fc5857'
				}],
				exporting: {
					enabled: false
				}
			});
			
			ordersLoaded = true;
		});
	}

	function loadHistory() {
		if(historyLoaded) {
			return;
		}

		var tmpregion = region;
		if (tmpregion == 0) {
			// default to The Forge if "All Regions" is selected
			tmpregion = 10000002;
		}

//		// one-time processing
//		var a = [];
//		for(var i=0; i<historyData.length; i++) {
//			a.push({t:historyData[i].date, y:parseFloat(historyData[i].average)});
//		}
//		var bounds = getOutlierBounds(a);
//		var clean = removeOutliers(a, bounds);

		$.getJSON('/api/market/history/' + tmpregion + '/type/' + type, function(result) {
			var historyData = result;

			// make the datasets and populate the table
			var hal = [];
			var vol = [];
			for(var i=0; i<historyData.length; i++) {
				var day = historyData[i];
				var millis = Date.parse(day.date);
				hal.push([
					millis,
					parseFloat(day.average),
					parseFloat(day.highest),
					parseFloat(day.lowest),
					parseFloat(day.average),
				]);
				vol.push([millis, parseInt(day.volume)]);

				historyTable.row.add([
					day.date,
					formatIsk(day.highest),
					formatIsk(day.average),
					formatIsk(day.lowest),
					day.volume,
					day.orderCount
				]);
			}
			historyTable.draw();
			$.fn.dataTable.tables({visible: true, api: true}).columns.adjust();

			Highcharts.chart('historychart', {
				title: {
					text: ''
				},
				xAxis: {
					type: 'datetime'
				},
				yAxis: [{
					startOnTick: false,
					endOnTick: false,
					labels: {
						align: 'right',
						x: -3
					},
					title: {
						text: 'HAL'
					},
					height: '70%',
					lineWidth: 2,
					resize: {
						enabled: true
					}
				}, {
					labels: {
						align: 'right',
						x: -3
					},
					title: {
						text: 'Volume'
					},
					top: '75%',
					height: '25%',
					offset: 0,
					lineWidth: 2
				}],
				rangeSelector: {
					enabled: true,
					selected: 1
				},
				tooltip: {
					split: true
				},
				plotOptions: {
					series: {
						dataGrouping: {
							units: [[
								'week',
								[1, 2]
							], [
								'month',
								[1, 2, 3, 4, 6]
							]]
						}
					}
				},
				series: [{
					type: 'candlestick',
					name: 'High/Average/Low',
					id: 'hal',
					zIndex: 2,
					data: hal
				}, {
					type: 'column',
					name: 'Volume',
					id: 'volume',
					data: vol,
					yAxis: 1
				}, {
					type: 'sma',
					linkedTo: 'hal',
					zIndex: 1,
					marker: {
						enabled: false
					},
					params: {
						period: 10
					}
				}, {
					type: 'sma',
					linkedTo: 'hal',
					zIndex: 1,
					marker: {
						enabled: false
					},
					params: {
						period: 5
					}
				}],
				legend: {
					enabled: false
				},
				exporting: {
					enabled: false
				}
			});

			historyLoaded = true;
		});
	}

	// load the data for the initial page
	refreshData();

});
