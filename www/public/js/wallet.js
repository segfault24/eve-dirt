"use strict";

$(document).ready(function(){

	// initialize tables
	var sellOrdersTable = $('#sell-orders-table').DataTable({
		columns: [
			{title:'Character', responsivePriority: 4},
			{title:'Region', responsivePriority: 6},
			{title:'Station', responsivePriority: 4},
			{title:'Type', responsivePriority: 1},
			{title:'Price', responsivePriority: 2},
			{title:'Qt Stock', responsivePriority: 3},
			{title:'Qt Total', responsivePriority: 5}
		],
		order: [[2, "asc"]],
		searching: true,
		paging: true,
		pageLength: 40,
		bLengthChange: false,
		bInfo: false,
		responsive: true,
		select: true
	});
	var buyOrdersTable = $('#buy-orders-table').DataTable({
		columns: [
			{title:'Character', responsivePriority: 4},
			{title:'Region', responsivePriority: 6},
			{title:'Station', responsivePriority: 4},
			{title:'Type', responsivePriority: 1},
			{title:'Price', responsivePriority: 2},
			{title:'Qt Unfilled', responsivePriority: 3},
			{title:'Qt Total', responsivePriority: 5},
			{title:'Range', responsivePriority: 4}
		],
		order: [[2, "asc"]],
		searching: true,
		paging: true,
		pageLength: 40,
		bLengthChange: false,
		bInfo: false,
		responsive: true,
		select: true
	});
	var transactionsTable = $('#transactions-table').DataTable({
		columns: [
			{title:'Date', responsivePriority: 5},
			{title:'Character', responsivePriority: 6},
			{title:'Type', responsivePriority: 1},
			{title:'Buy/Sell', responsivePriority: 4},
			{title:'Quantity', responsivePriority: 3},
			{title:'Unit Price', responsivePriority: 2},
			{title:'Ext Price', responsivePriority: 2}
		],
		order: [[0, "desc"]],
		searching: true,
		paging: true,
		pageLength: 40,
		bLengthChange: false,
		bInfo: false,
		responsive: true,
		select: true
	});
	var journalTable = $('#journal-table').DataTable({
		columns: [
			{title:'Date', responsivePriority: 3},
			{title:'Character', responsivePriority: 4},
			{title:'Type', responsivePriority: 1},
			{title:'Amount', responsivePriority: 1},
			{title:'Balance', responsivePriority: 2},
			{title:'Description', responsivePriority: 3}
		],
		order: [[0, "desc"]],
		searching: true,
		paging: true,
		pageLength: 40,
		bLengthChange: false,
		bInfo: false,
		responsive: true,
		select: true
	});

	// setup the table autoadjust
	$('a[data-toggle="tab"]').on('shown.bs.tab', function(e) {
		$.fn.dataTable.tables({visible: true, api: true}).columns.adjust();
	});

	var ordersLoaded = false;
	$('#sell-orders-label').click(function() {
		if(!ordersLoaded) {
			loadOrders();
		}
	});
	$('#buy-orders-label').click(function() {
		if(!ordersLoaded) {
			loadOrders();
		}
	});
	var transactionsLoaded = false;
	$('#transactions-label').click(function() {
		if(!transactionsLoaded) {
			myAjax('wallet/transactions', function(result) {
				for(var i=0; i<result.length; i++) {
					transactionsTable.row.add([
						result[i].date,
						result[i].charName,
						'<a class="open-in-game" data-typeId="' + result[i].typeID + '" href="#"><i class="fa fa-magnet fa-fw"></i></a> <a href="browse?type=' + result[i].typeID + '" target="_blank">' + result[i].typeName + '</a>',
						result[i].isBuy,
						formatInt(result[i].quantity),
						formatIsk(result[i].unitPrice),
						formatInt(result[i].quantity*result[i].unitPrice)
					]);
				}
				transactionsTable.draw();
				$.fn.dataTable.tables({visible: true, api: true}).columns.adjust();
				transactionsLoaded = true;
			});
		}
	});
	var journalLoaded = false;
	$('#journal-label').click(function() {
		if(!journalLoaded) {
			myAjax('wallet/journal', function(result) {
				for(var i=0; i<result.length; i++) {
					journalTable.row.add([
						result[i].date,
						result[i].charName,
						result[i].refType,
						formatIsk(result[i].amount),
						formatIsk(result[i].balance),
						result[i].description
					]);
				}
				journalTable.draw();
				$.fn.dataTable.tables({visible: true, api: true}).columns.adjust();
				journalLoaded = true;
			});
		}
	});

	// do the initial order load
	loadOrders();

	function loadOrders() {
		myAjax('wallet/orders', function(result) {
			for(var i=0; i<result.length; i++) {
				if(result[i].isBuyOrder==1) {
					buyOrdersTable.row.add([
						result[i].charName,
						result[i].regionName,
						result[i].sName,
						'<a class="open-in-game" data-typeid="' + result[i].typeId + '" href="#"><i class="fa fa-magnet fa-fw"></i></a> <a href="browse?type=' + result[i].typeId + '" target="_blank">' + result[i].typeName + '</a>',
						formatIsk(result[i].price),
						formatInt(result[i].volumeRemain),
						formatInt(result[i].volumeTotal),
						result[i].range
					]);
				} else {
					sellOrdersTable.row.add([
						result[i].charName,
						result[i].regionName,
						result[i].sName,
						'<a class="open-in-game" data-typeid="' + result[i].typeId + '" href="#"><i class="fa fa-magnet fa-fw"></i></a> <a href="browse?type=' + result[i].typeId + '" target="_blank">' + result[i].typeName + '</a>',
						formatIsk(result[i].price),
						formatInt(result[i].volumeRemain),
						formatInt(result[i].volumeTotal)
					]);
				}
			}
			buyOrdersTable.draw();
			sellOrdersTable.draw();
			$.fn.dataTable.tables({visible: true, api: true}).columns.adjust();
			ordersLoaded = true;
		});
	}

});

