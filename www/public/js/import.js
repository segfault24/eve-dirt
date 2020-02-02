"use strict";

$(document).ready(function() {

	// initialize table
	var importTable = $('#import-table').DataTable({
		columns: [
			{title:'Item', responsivePriority: 1},
			{title:'Source', responsivePriority: 3},
			{title:'Destination', responsivePriority: 3},
			{title:'Daily Vol', responsivePriority: 3},
			{title:'Stock', responsivePriority: 3},
			{title:'Exhausts', responsivePriority: 2},
			{title:'Freight/Fees', responsivePriority: 4},
			{title:'Profit', responsivePriority: 2},
			{title:'ROI', responsivePriority: 2},
			{title:'Max Daily Profit', responsivePriority: 2}
		],
		order: [[9, "desc"]],
		searching: true,
		paging: true,
		pageLength: 20,
		bLengthChange: false,
		bInfo: false,
		responsive: true,
		select: true
	});

	var srcRegion = 10000002;
	var srcStruct = 1028573403974;
	var dstRegion = 10000039;
	var dstStruct = 1024004680659;
	var data;

	var preferredStructs = [
		{ region: 10000002, struct: 60003760 },
		{ region: 10000043, struct: 60008494 },
		{ region: 10000032, struct: 60011866 },
		{ region: 10000025, struct: 1028573403974 },
		{ region: 10000039, struct: 1024004680659 }
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
		$("#refresh-data").attr("disabled", true);
		srcStruct = $('#src-struct-select').val();
		dstStruct = $('#dst-struct-select').val();
		reloadData();
	});

	$('#apply-btn').click(function() {
		reloadTable();
	});

	function reloadData(callback) {
		importTable.clear().draw();
		$.getJSON('/api/trade/import/' + srcStruct + '/' + dstStruct, function(result) {
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
		populateTable(importTable, data, freightRouteRate, freightCollateralRate, salesTax, brokerFee, filterIsk, filterPercent);
		$("#refresh-data").attr("disabled", false);
	}

	// shipping_rate = isk/m3
	function populateTable(table, result, shipping_rate, shipping_collat, sales_tax, broker_fee, filterIsk, filterPercent) {
		table.clear();
		for(var i=0; i<result.length; i++) {
			var freight = result[i].volume*shipping_rate + result[i].source*shipping_collat;
			var tax = (sales_tax + broker_fee)*result[i].dest;
			var margin = result[i].dest - result[i].source - freight - tax;
			var marginPercent = parseInt((margin/result[i].source*100).toFixed(0));
			if (margin > 0 && margin > filterIsk && marginPercent > filterPercent) {
				table.row.add([
					'<a class="open-in-game" data-typeId="' + result[i].typeId + '" href="#"><i class="fa fa-magnet fa-fw"></i></a> <a href="browse?type=' + result[i].typeId + '" target="_blank">' + result[i].typeName + '</a>',
					formatIsk(result[i].source),
					formatIsk(result[i].dest),
					result[i].ma30,
					result[i].stock,
					Math.ceil(result[i].stock / result[i].ma30),
					formatIsk(freight + tax),
					formatIsk(margin),
					marginPercent,
					formatIsk(result[i].ma30*margin)
				]);
			}
		}
		table.draw();
		$.fn.dataTable.tables({visible: true, api: true}).columns.adjust();
	}

});
