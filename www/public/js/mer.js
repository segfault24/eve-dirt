"use strict";

$(document).ready(function(){
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
	$('#pdmtablabel').click(function() {
		loadPDM();
	});
	$('#iskvoltablabel').click(function() {
		loadIskVelocity();
	});
	$('#monsuptablabel').click(function() {
		loadMoneySupply();
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
					valueDecimals: 2,
					split: true
				},
				series: seriesOptions
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
				{ name: 'Velocity', data: iskData }
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
					valueDecimals: 2,
					split: true
				},
				series: seriesOptions
			});

			iskvolLoaded = true;
		});
	}

	function loadMoneySupply() {
		if(monsupLoaded) {
			return;
		}
		console.log("loading money supply");
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
					valueDecimals: 2,
					split: true
				},
				series: seriesOptions
			});

			monsupLoaded = true;
		});
	}
});

