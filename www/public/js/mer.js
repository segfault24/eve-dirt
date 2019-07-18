"use strict";

$(document).ready(function(){
	var months = [
		'January', 'February', 'March', 'April',
		'May', 'June', 'July', 'August',
		'September', 'October', 'November', 'December'
	];

	// url navigation for tabs
	if (document.location.hash) {
		$('.nav-tabs a[href="' + document.location.hash + '"]').tab('show');
		switch (document.location.hash) {
			case '#mined-produced-destroyed':
				loadPDM();
				break;
			case '#money-supply':
				loadMoneySupply();
				break;
			case '#velocity-of-isk':
				loadIskVelocity();
				break;
			case '#faucets-sinks':
				loadFaucetsSinks();
				break;
		}
	} else {
		loadPDM();
	}
	$('a[data-toggle="tab"]').on('shown.bs.tab', function (e) {
		history.pushState({}, '', e.target.hash);
	});

	var pdmLoaded = false;
	var iskvolLoaded = false;
	var monsupLoaded = false;
	var sinksFaucetsLoaded = false;
	var sinksFaucetsMonthsLoaded = false;
	$('#pdmtablabel').click(function() { loadPDM(); });
	$('#iskvoltablabel').click(function() { loadIskVelocity(); });
	$('#monsuptablabel').click(function() { loadMoneySupply(); });
	$('#faucetsinktablabel').click(function() { loadFaucetsSinks(); });

	$('#faucet-sink-months').change(function() {
		sinksFaucetsLoaded = false;
		loadFaucetsSinks();
	});

	function loadPDM() {
		if(pdmLoaded) {
			return;
		}
		console.log("loading mined, produced, destroyed");
		$.getJSON('/api/economic-reports/mined-produced-destroyed', function (data) {
			var mineData = [];
			var prodData = [];
			var destData = [];
			for(var i=0; i<data.length; i++) {
				var d = Date.parse(data[i].date);
				mineData.push([d, parseInt(data[i].mined)]);
				prodData.push([d, parseInt(data[i].produced)]);
				destData.push([d, parseInt(data[i].destroyed)]);
			}
			var seriesOptions = [
				{ name: 'Mined', data: mineData },
				{ name: 'Produced', data: prodData },
				{ name: 'Destroyed', data: destData }
			];

			Highcharts.stockChart('pdmchart', {
				rangeSelector: {
					selected: 2
				},
				yAxis: {
					labels: {
						formatter: function () {
							return (this.value > 0 ? ' + ' : '') + this.value + '%';
						}
					},
					plotLines: [{
						value: 0,
						width: 2,
						color: 'silver'
					}]
				},
				plotOptions: {
					series: {
						compare: 'percent',
						showInNavigator: true
					}
				},
				tooltip: {
					pointFormat: '<span style="color:{series.color}">{series.name}</span>: <b>{point.y}</b> ({point.change}%)<br/>',
					valueDecimals: 0,
					split: true
				},
				series: seriesOptions,
				legend: {
					enabled: true
				},
				exporting: {
					enabled: false
				}
			});

			pdmLoaded = true;
		});
	}

	function loadIskVelocity() {
		if(iskvolLoaded) {
			return;
		}
		console.log("loading isk velocity");
		$.getJSON('/api/economic-reports/velocity-of-isk', function (data) {
			var iskData = [];
			for(var i=0; i<data.length; i++) {
				iskData.push([Date.parse(data[i].date), parseInt(data[i].volume)]);
			}
			var seriesOptions = [
				{
					name: 'Velocity',
					id: 'velocity',
					data: iskData
				},
				{
					type: 'sma',
					linkedTo: 'velocity',
					zIndex: 1,
					marker: {
						enabled: false
					},
					params: {
						period: 30
					}
				}
			];

			Highcharts.stockChart('iskvolchart', {
				rangeSelector: {
					selected: 4
				},
				yAxis: {
					labels: {
						formatter: function () {
							return (this.value > 0 ? ' + ' : '') + this.value + '%';
						}
					},
					plotLines: [{
						value: 0,
						width: 2,
						color: 'silver'
					}]
				},
				plotOptions: {
					series: {
						compare: 'percent',
						showInNavigator: true
					}
				},
				tooltip: {
					pointFormat: '<span style="color:{series.color}">{series.name}</span>: <b>{point.y}</b> ({point.change}%)<br/>',
					valueDecimals: 0,
					split: true
				},
				series: seriesOptions,
				exporting: {
					enabled: false
				}
			});

			iskvolLoaded = true;
		});
	}

	function loadMoneySupply() {
		if(monsupLoaded) {
			return;
		}
		console.log('loading money supply');
		$.getJSON('/api/economic-reports/money-supply', function (data) {
			var charData = [];
			var corpData = [];
			var totalData = [];
			for(var i=0; i<data.length; i++) {
				var d = Date.parse(data[i].date);
				charData.push([d, parseInt(data[i].character)]);
				corpData.push([d, parseInt(data[i].corporation)]);
				totalData.push([d, parseInt(data[i].total)]);
			}
			var seriesOptions = [
				{ name: 'Character', data: charData },
				{ name: 'Corporation', data: corpData },
				{ name: 'Total', data: totalData }
			];

			Highcharts.stockChart('monsupchart', {
				rangeSelector: {
					selected: 4
				},
				yAxis: {
					labels: {
						formatter: function () {
							return (this.value > 0 ? ' + ' : '') + this.value + '%';
						}
					},
					plotLines: [{
						value: 0,
						width: 2,
						color: 'silver'
					}]
				},
				plotOptions: {
					series: {
						compare: 'percent',
						showInNavigator: true
					}
				},
				tooltip: {
					pointFormat: '<span style="color:{series.color}">{series.name}</span>: <b>{point.y}</b> ({point.change}%)<br/>',
					valueDecimals: 0,
					split: true
				},
				series: seriesOptions,
				legend: {
					enabled: true
				},
				exporting: {
					enabled: false
				}
			});

			monsupLoaded = true;
		});
	}

	function loadFaucetsSinks() {
		if (!sinksFaucetsMonthsLoaded) {
			console.log('loading faucets & sinks months');
			$.getJSON('/api/economic-reports/faucets-sinks', function (data) {
				$('#faucet-sink-months').empty();
				for(var i=0; i<data.length; i++) {
					var yr = data[i].date.split('-')[0];
					var mo = parseInt(data[i].date.split('-')[1]);
					$('#faucet-sink-months').append('<option value="' + data[i].date + '">' + yr + ' ' + months[mo] + '</option>');
				}
				if (data.length > 0) {
					year = parseInt(data[0].date.split('-')[0]);
					month = parseInt(data[0].date.split('-')[1]);
				}
				sinksFaucetsMonthsLoaded = true;
			});
		}
		if(sinksFaucetsLoaded) {
			return;
		}

		var d = new Date();
		d.setDate(d.getDate() - 45);
		var year = d.getFullYear();
		var month = d.getMonth();
		if($('#faucet-sink-months').val() != null) {
			var p = $('#faucet-sink-months').val().split('-');
			year = p[0];
			month = p[1];
		}

		console.log('loading faucets & sinks ' + year + ' ' + month);
		$.getJSON('/api/economic-reports/faucets-sinks/' + year + '/' + month, function (data) {
			var categories = [];
			var faucets = [];
			var sinks = [];
			for(var i=0; i<data.length; i++) {
				categories.push(data[i].keyText);
				faucets.push(parseInt(data[i].faucet));
				sinks.push(parseInt(data[i].sink));
			}

			Highcharts.chart('faucetsinkchart', {
				chart: {
					type: 'bar'
				},
				title: {
					text: ''
				},
				xAxis: {
					categories: categories,
					reversed: false,
					labels: {
						step: 1
					}
				},
				yAxis: {
					title: {
						text: null
					},
					labels: {
						formatter: function () {
							return this.value/1000000000000 + ' T';
						}
					}
				},
				plotOptions: {
					series: {
						stacking: 'normal'
					}
				},
				tooltip: {
					formatter: function () {
						return '<b>' + this.point.category + '</b><br/>' +
							Highcharts.numberFormat(this.point.y/1000000000, 0) + ' billion';
					}
				},
				series: [{
					name: 'Faucets',
					data: faucets
				}, {
					name: 'Sinks',
					data: sinks
				}],
				exporting: {
					enabled: false
				}
			});

			sinksFaucetsLoaded = true;
		});
	}

});

