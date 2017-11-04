"use strict";

var data;
var velIskChart;

var magnitude;

$(document).ready(function(){

	// get the data
	$.ajax({
		url: '/api/economic-reports/velocity-of-isk',
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

	var points = [];
	for(var i=data.length-range; i<data.length; i++) {
		points.push({x:data[i].date, y:data[i].volume});
	}

	magnitude = getKMBTParams(points);

	if(typeof velIskChart!='undefined') {
		velIskChart.destroy();
	}

	velIskChart = new Chart($('#velocity-of-isk'), {
		type: 'line',
		data: {
			datasets: [{
				label: 'Velocity of Isk',
				data: points,
				fill: false,
				pointRadius: 0,
				borderWidth: myBorderWidth,
				borderColor: myColors.data[0].border,
				backgroundColor: myColors.data[0].fill
			}]
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
							return formatKMBT(label, magnitude);
						}
					}
				}]
			},
			legend: {
				display: false
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
