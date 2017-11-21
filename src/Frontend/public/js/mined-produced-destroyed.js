"use strict";

var data;
var pmdChart;

$(document).ready(function(){

	// get the data
	$.ajax({
		url: '/api/economic-reports/mined-produced-destroyed',
		async: true,
		success: function(result) {
			data = result;
			generateGraph(data, 90);
		}
	});

	$('#dates-all').click(function(){ generateGraph(data, 0); });
	$('#dates-3y').click(function(){ generateGraph(data, 1095); });
	$('#dates-1y').click(function(){ generateGraph(data, 365); });
	$('#dates-6m').click(function(){ generateGraph(data, 182); });
	$('#dates-3m').click(function(){ generateGraph(data, 90); });
	$('#dates-1m').click(function(){ generateGraph(data, 30); });
});

function generateGraph(data, range) {

	if(range<=0) {
		range = data.length;
	}

	var produced = [];
	var mined = [];
	var destroyed = [];
	for(var i=data.length-range; i<data.length; i++) {
		produced.push({x:data[i].date, y:data[i].produced});
		mined.push({x:data[i].date, y:data[i].mined});
		destroyed.push({x:data[i].date, y:data[i].destroyed});
	}

	var magnitude = getKMBTParams(produced);

	if(typeof pmdChart!='undefined') {
		pmdChart.destroy();
	}

	pmdChart = new Chart($('#mined-produced-destroyed'), {
		type: 'line',
		data: {
			datasets: [
				{
					label: 'Produced',
					data: produced,
					fill: false,
					pointRadius: 0,
					borderWidth: myBorderWidth,
					borderColor: myColors.data[0].border,
					backgroundColor: myColors.data[0].fill
				},
				{
					label: 'Mined',
					data: mined,
					fill: false,
					pointRadius: 0,
					borderWidth: myBorderWidth,
					borderColor: myColors.data[1].border,
					backgroundColor: myColors.data[1].fill
				},
				{
					label: 'Destroyed',
					data: destroyed,
					fill: false,
					pointRadius: 0,
					borderWidth: myBorderWidth,
					borderColor: myColors.data[2].border,
					backgroundColor: myColors.data[2].fill
				}
			]
		},
		options: {
			responsive: true,
			maintainAspectRatio: false,
			scales: {
				xAxes: [{
					type: 'time',
					time: {
						displayFormats: myDisplayFormatAll
					},
					position: 'bottom'
				}],
				yAxes: [{
					scaleLabel: {
						display: true,
						labelString: 'Isk'
					},
					ticks: {
						beginAtZero: true,
						callback: function(label, index, labels) {
							//return label/1000000000000 + 't';
							return formatKMBT(label, magnitude);
						}
					}
				}]
			},
			legend: {
				display: true
			}/*,
			tooltips: {
				callbacks: {
					title: function(tooltipItems, data) {
						return tooltipItems[0].xLabel.substring(0, 10);
					},
					label: function(tooltipItem, data) {
						return 'Velocity: ' + (tooltipItem.yLabel/1000000000000).toFixed(2) + 't';
					}
				}
			}*/
		}
	});
}
