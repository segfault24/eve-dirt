Highcharts.theme = {
	colors: ['#2b908f', '#90ee7e', '#f45b5b', '#7798bf', '#aaeeee', '#ff0066',
		'#eeaaee', '#55bf3b', '#df5353', '#7798bf', '#aaeeee'],
	chart: {
		backgroundColor: '#343a40',
		plotBorderColor: '#606060'
	},
	title: {
		style: {
			color: '#e0e0e0',
			textTransform: 'uppercase',
			fontSize: '20px'
		}
	},
	subtitle: {
		style: {
			color: '#e0e0e0',
			textTransform: 'uppercase'
		}
	},
	xAxis: {
		gridLineColor: '#707070',
		labels: {
			style: {
				color: '#e0e0e0'
			}
		},
		lineColor: '#707070',
		minorGridLineColor: '#505050',
		tickColor: '#707070',
		title: {
			style: {
				color: '#a0a0a0'
			}
		}
	},
	yAxis: {
		gridLineColor: '#707070',
		labels: {
			style: {
				color: '#e0e0e0'
			}
		},
		lineColor: '#707070',
		minorGridLineColor: '#505050',
		tickColor: '#707070',
		tickWidth: 1,
		title: {
			style: {
				color: '#a0a0a0'
			}
		}
	},
	tooltip: {
		backgroundColor: 'rgba(0, 0, 0, 0.85)',
		style: {
			color: '#f0f0f0'
		}
	},
	plotOptions: {
		series: {
			dataLabels: {
				color: '#b0b0b0'
			},
			marker: {
				lineColor: '#333'
			}
		},
		column: {
			borderColor: 'none'
		},
		boxplot: {
			fillColor: '#505050'
		},
		candlestick: {
			lineColor: '#d0d0d0'
		},
		errorbar: {
			color: '#d0d0d0'
		}
	},
	legend: {
		itemStyle: {
			color: '#e0e0e0'
		},
		itemHoverStyle: {
			color: '#fff'
		},
		itemHiddenStyle: {
			color: '#606060'
		}
	},
	credits: {
		style: {
			color: '#707070'
		}
	},
	labels: {
		style: {
			color: '#707070'
		}
	},
	drilldown: {
		activeAxisLabelStyle: {
			color: '#f0f0f0'
		},
		activeDataLabelStyle: {
			color: '#f0f0f0'
		}
	},

	navigation: {
		buttonOptions: {
			symbolStroke: '#dddddd',
			theme: {
				fill: '#505050'
			}
		}
	},

	// scroll charts
	rangeSelector: {
		buttonTheme: {
			fill: '#505050',
			stroke: '#000000',
			style: {
				color: '#ccc'
			},
			states: {
				hover: {
					fill: '#707070',
					stroke: '#000000',
					style: {
						color: 'white'
					}
				},
				select: {
					fill: '#000000',
					stroke: '#000000',
					style: {
						color: 'white'
					}
				}
			}
		},
		inputBoxBorderColor: '#505050',
		inputStyle: {
			backgroundColor: '#333',
			color: 'silver'
		},
		labelStyle: {
			color: 'silver'
		}
	},

	navigator: {
		handles: {
			backgroundColor: '#666',
			borderColor: '#aaa'
		},
		outlineColor: '#ccc',
		maskFill: 'rgba(255,255,255,0.1)',
		series: {
			color: '#7798bf',
			lineColor: '#a6c7ed'
		},
		xAxis: {
			gridLineColor: '#505050'
		}
	},

	scrollbar: {
		barBackgroundColor: '#808080',
		barBorderColor: '#808080',
		buttonArrowColor: '#ccc',
		buttonBackgroundColor: '#606060',
		buttonBorderColor: '#606060',
		rifleColor: '#fff',
		trackBackgroundColor: '#404040',
		trackBorderColor: '#404040'
	},

	// special colors for some of the
	legendBackgroundColor: 'rgba(0, 0, 0, 0.5)',
	background2: '#505050',
	dataLabelsColor: '#b0b0b0',
	textColor: '#c0c0c0',
	contrastTextColor: '#f0f0f0',
	maskColor: 'rgba(255,255,255,0.3)'
};

Highcharts.setOptions(Highcharts.theme);
