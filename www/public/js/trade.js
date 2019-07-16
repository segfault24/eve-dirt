"use strict";

$(document).ready(function() {

	// initialize table
	var tradeTable = $('#trade-table').DataTable({
		columns: [
			{title:'Item', responsivePriority: 1},
			{title:'Qt', responsivePriority: 3},
			{title:'Source', responsivePriority: 3},
			{title:'Destination', responsivePriority: 3},
			{title:'Freight/Fees', responsivePriority: 4},
			{title:'Profit', responsivePriority: 2},
			{title:'ROI', responsivePriority: 2},
			{title:'Profit/m3', responsivePriority: 2},
			{title:'Max Profit', responsivePriority: 2}
		],
		order: [[8, "desc"]],
		searching: true,
		paging: true,
		pageLength: 20,
		bLengthChange: false,
		bInfo: false,
		responsive: true,
		select: true
	});

	var srcRegion = 10000025;
	var srcStruct = 1028573403974;
	var dstRegion = 10000002;
	var dstStruct = 60003760;
	var tradeType = 'sell-buy';
	var data;

	var preferredStructs = [
		{ region: 10000002, struct: 60003760 },
		{ region: 10000043, struct: 60008494 },
		{ region: 10000032, struct: 60011866 }
	];

	function fillStructs(region, select) {
		$.getJSON('/api/trade/structs-by-region/' + region + '/', function(result) {
			$('#' + select).empty();
			$('#' + select).append('<option value="' + region + '">All Structures</option>');
			for(var i=0; i<result.length; i++) {
				$('#' + select).append('<option value="' + result[i].sId + '">' + result[i].sName + '</option>');
			}
			for(var i=0; i<preferredStructs.length; i++) {
				if(preferredStructs[i].region == region) {
					$('#' + select).val(preferredStructs[i].struct);
				}
			}
		});
	}

	$('#src-region-select').val(srcRegion);
	fillStructs(srcRegion, 'src-struct-select');
	$('#src-region-select').change(function() {
		srcRegion = $('#src-region-select').val();
		fillStructs(srcRegion, 'src-struct-select');
	});
	$('#dst-region-select').val(dstRegion);
	fillStructs(dstRegion, 'dst-struct-select');
	$('#dst-region-select').change(function() {
		dstRegion = $('#dst-region-select').val();
		fillStructs(dstRegion, 'dst-struct-select');
	});

	$('#refresh-data').click(function() {
		srcStruct = $('#src-struct-select').val();
		dstStruct = $('#dst-struct-select').val();
		tradeType = $('#trade-type-select').val();
		reloadData();
	});

	$('#apply-btn').click(function() {
		reloadTable();
	});

	function reloadData(callback) {
		tradeTable.clear().draw();
		$.getJSON('/api/trade/' + tradeType + '/' + srcStruct + '/' + dstStruct, function(result) {
			data = result;
			reloadTable();
		});
	}

	function reloadTable() {
		var freightRouteRate = $('#freight-route').val();
		var freightCollateralRate = $('#freight-collat').val()/100;
		var salesTax = $('#sales-tax').val()/100;
		var brokerFee = $('#broker-fee').val()/100;
		var filterIsk = $('#filter-profit-isk').val();
		var filterPercent = $('#filter-profit-percent').val();
		var filterRoiPerVol = $('#filter-roipervol-isk').val();
		if (tradeType == 'sell-buy') {
			brokerFee = 0;
		}
		populateTable(tradeTable, data, freightRouteRate, freightCollateralRate, salesTax, brokerFee, filterIsk, filterPercent, filterRoiPerVol);
	}

	// shipping_rate = isk/m3
	function populateTable(table, result, shipping_rate, shipping_collat, sales_tax, broker_fee, filterIsk, filterPercent, filterRoiPerVol) {
		table.clear();
		for(var i=0; i<result.length; i++) {
			var freight = result[i].volume*shipping_rate + result[i].source*shipping_collat;
			var tax = (sales_tax + broker_fee)*result[i].dest;
			var margin = result[i].dest - result[i].source - freight - tax;
			var marginPercent = parseInt((margin/result[i].source*100).toFixed(0));
			var roiPerVol = margin/result[i].volume;
			if (margin > 0 && margin > filterIsk && marginPercent > filterPercent && roiPerVol > filterRoiPerVol) {
				table.row.add([
					'<a class="open-in-game" data-typeId="' + result[i].typeId + '" href="#"><i class="fa fa-magnet fa-fw"></i></a> <a href="browse?type=' + result[i].typeId + '" target="_blank">' + result[i].typeName + '</a>',
					result[i].qt,
					formatIsk(result[i].source),
					formatIsk(result[i].dest),
					formatIsk(freight + tax),
					formatIsk(margin),
					marginPercent,
					formatIsk(roiPerVol),
					formatIsk(result[i].qt*margin)
				]);
			}
		}
		table.draw();
		$.fn.dataTable.tables({visible: true, api: true}).columns.adjust();
	}

});
