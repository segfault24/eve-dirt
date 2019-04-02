"use strict";

$(document).ready(function() {

	// initialize tables
	var ss2jbTable = $('#ss2jb').DataTable({
		columns: [
			{title:'Item', responsivePriority: 1},
			{title:'Staging Sell', responsivePriority: 3},
			{title:'Jita Buy', responsivePriority: 3},
			{title:'Freight', responsivePriority: 4},
			{title:'Profit', responsivePriority: 2},
			{title:'%', responsivePriority: 2},
			{title:'m&#179;', responsivePriority: 5}
		],
		order: [[5, "desc"]],
		searching: false,
		paging: true,
		pageLength: 20,
		bLengthChange: false,
		bInfo: false,
		responsive: true,
		select: true
	});
	var ss2jsTable = $('#ss2js').DataTable({
		columns: [
			{title:'Item', responsivePriority: 1},
			{title:'Staging Sell', responsivePriority: 3},
			{title:'Jita Sell', responsivePriority: 3},
			{title:'Freight', responsivePriority: 4},
			{title:'Profit', responsivePriority: 2},
			{title:'%', responsivePriority: 2},
			{title:'m&#179;', responsivePriority: 5}
		],
		order: [[5, "desc"]],
		searching: false,
		paging: true,
		pageLength: 20,
		bLengthChange: false,
		bInfo: false,
		responsive: true,
		select: true
	});
	var hs2jbTable = $('#hs2jb').DataTable({
		columns: [
			{title:'Item', responsivePriority: 1},
			{title:'Home Sell', responsivePriority: 3},
			{title:'Jita Buy', responsivePriority: 3},
			{title:'Freight', responsivePriority: 4},
			{title:'Profit', responsivePriority: 2},
			{title:'%', responsivePriority: 2},
			{title:'m&#179;', responsivePriority: 5}
		],
		order: [[5, "desc"]],
		searching: false,
		paging: true,
		pageLength: 20,
		bLengthChange: false,
		bInfo: false,
		responsive: true,
		select: true
	});
	var hs2jsTable = $('#hs2js').DataTable({
		columns: [
			{title:'Item', responsivePriority: 1},
			{title:'Home Sell', responsivePriority: 3},
			{title:'Jita Sell', responsivePriority: 3},
			{title:'Freight', responsivePriority: 4},
			{title:'Profit', responsivePriority: 2},
			{title:'%', responsivePriority: 2},
			{title:'m&#179;', responsivePriority: 5}
		],
		order: [[5, "desc"]],
		searching: false,
		paging: true,
		pageLength: 20,
		bLengthChange: false,
		bInfo: false,
		responsive: true,
		select: true
	});

	// generate the sidebar
	var menu = new BrowserMenu();

	myAjax('staging-sell-to-jita-buy', function(result) {
		populateTable(ss2jbTable, result);
	});
	myAjax('staging-sell-to-jita-sell', function(result) {
		populateTable(ss2jsTable, result);
	});
	myAjax('home-sell-to-jita-buy', function(result) {
		populateTable(hs2jbTable, result);
	});
	myAjax('home-sell-to-jita-sell', function(result) {
		populateTable(hs2jsTable, result);
	});

});

function populateTable(table, result) {
	for(var i=0; i<result.length; i++) {
		var freight = result[i].volume*1500 + result[i].source*0.01;
		var margin = result[i].dest - result[i].source - freight;
		if (margin > 0) {
			table.row.add([
				'<a href="browse?type=' + result[i].typeId + '" target="_blank">' + result[i].typeName + '</a>',
				formatIsk(result[i].source),
				formatIsk(result[i].dest),
				formatIsk(freight),
				formatIsk(margin),
				(margin/result[i].source*100).toFixed(0),
				result[i].volume
			]);
		}
	}
	table.draw();
	$.fn.dataTable.tables({visible: true, api: true}).columns.adjust();
}
