"use strict";

$(document).ready(function(){

	// url navigation for tabs
	if (document.location.hash) {
		$('.nav-tabs a[href="' + document.location.hash + '"]').tab('show');
		switch (document.location.hash) {
			case '#transactions':
				loadTransactions();
				break;
			case '#journal':
				loadJournal();
				break;
			case '#sell-orders':
			case '#buy-orders':
				loadOrders();
				break;
			case '#contracts':
				loadContracts();
				break;
			case '#roi':
				loadRoi();
				break;
		}
	} else {
		loadTransactions();
	}
	$('a[data-toggle="tab"]').on('shown.bs.tab', function (e) {
		history.pushState({}, '', e.target.hash);
	});

	var ordersLoaded = false;
	var transactionsLoaded = false;
	var journalLoaded = false;
	var contractsLoaded = false;
	var roiLoaded = false;

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
	var contractsTable = $('#contracts-table').DataTable({
		columns: [
			{title:'Type', responsivePriority: 2},
			{title:'Issuer', responsivePriority: 1},
			{title:'Status', responsivePriority: 3},
			{title:'Acceptor', responsivePriority: 5},
			{title:'Issued', responsivePriority: 4},
			{title:'Completed', responsivePriority: 4}
		],
		order: [[4, "desc"]],
		searching: true,
		paging: true,
		pageLength: 40,
		bLengthChange: false,
		bInfo: false,
		responsive: true,
		select: true
	});
	var roiTable = $('#roi-table').DataTable({
		columns: [
			{title:'Date', responsivePriority: 4},
			{title:'Type', responsivePriority: 1},
			{title:'Buy', responsivePriority: 5},
			{title:'Sell', responsivePriority: 4},
			{title:'Unit Profit', responsivePriority: 2},
			{title:'ROI', responsivePriority: 3}
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

	$('#transactions-label').click(function() { loadTransactions(); });
	$('#journal-label').click(function() { loadJournal(); });
	$('#sell-orders-label').click(function() { loadOrders(); });
	$('#buy-orders-label').click(function() { loadOrders(); });
	$('#contracts-label').click(function() { loadContracts(); });
	$('#roi-label').click(function() { loadRoi(); });

	$('#refresh-data').click(function() {
		transactionsTable.clear().draw();
		journalTable.clear().draw()
		buyOrdersTable.clear().draw();
		sellOrdersTable.clear().draw();
		contractsTable.clear().draw();
		roiTable.clear().draw();
		ordersLoaded = false;
		transactionsLoaded = false;
		journalLoaded = false;
		contractsLoaded = false;
		roiLoaded = false;

		// find the active tab and load its data
		switch($('.nav-tabs .active').text()) {
			case ' Transactions':
				loadTransactions();
				break;
			case ' Journal':
				loadJournal();
				break;
			case ' Sell Orders':
			case ' Buy Orders':
				loadOrders();
				break;
			case ' Contracts':
				loadContracts();
				break;
			case ' Item ROI':
				loadRoi();
				break;
		}
	});

	function loadTransactions() {
		if(transactionsLoaded) {
			return;
		}
		$.getJSON('/api/wallet/transactions', function(result) {
			for(var i=0; i<result.length; i++) {
				var buySell = 'sell';
				if (result[i].isBuy==1) {
					buySell = 'buy';
				}
				transactionsTable.row.add([
					result[i].date,
					result[i].charName,
					'<a class="open-in-game" data-typeId="' + result[i].typeId + '" href="#"><i class="fa fa-magnet fa-fw"></i></a> <a href="browse?type=' + result[i].typeId + '" target="_blank">' + result[i].typeName + '</a>',
					buySell,
					formatInt(result[i].quantity),
					formatIsk(result[i].unitPrice),
					formatIsk(result[i].quantity*result[i].unitPrice)
				]);
			}
			transactionsTable.draw();
			$.fn.dataTable.tables({visible: true, api: true}).columns.adjust();
			transactionsLoaded = true;
		});
	}

	function loadJournal() {
		if(journalLoaded) {
			return;
		}
		$.getJSON('/api/wallet/journal', function(result) {
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

	function loadOrders() {
		if(ordersLoaded) {
			return;
		}
		$.getJSON('/api/wallet/orders', function(result) {
			for(var i=0; i<result.length; i++) {
				if(result[i].isBuyOrder==1) {
					buyOrdersTable.row.add([
						result[i].charName,
						result[i].regionName,
						result[i].locationName,
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
						result[i].locationName,
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

	function loadContracts() {
		if(contractsLoaded) {
			return;
		}
		$.getJSON('/api/wallet/contracts', function(result) {
			for(var i=0; i<result.length; i++) {
				contractsTable.row.add([
					'<a class="open-in-game-contract" data-contractid="' + result[i].contractId + '" href="#"><i class="fa fa-magnet fa-fw"></i></a> <a href="contract/' + result[i].contractId + '" target="_blank">' +prettyType(result[i].type) + '</a>',
					result[i].issuerName,
					prettyStatus(result[i].status),
					result[i].acceptorName,
					result[i].dateIssued,
					result[i].dateCompleted
				]);
			}
			contractsTable.draw();
			$.fn.dataTable.tables({visible: true, api: true}).columns.adjust();
			contractsLoaded = true;
		})
	}

	function loadRoi() {
		if(roiLoaded) {
			return;
		}
		$.getJSON('/api/wallet/returns', function(result) {
			for(var i=0; i<result.length; i++) {
				var unitProfit = result[i].sell - result[i].buy;
				var roi = unitProfit/result[i].buy*100;
				roiTable.row.add([
					result[i].date,
					'<a class="open-in-game" data-typeid="' + result[i].typeId + '" href="#"><i class="fa fa-magnet fa-fw"></i></a> <a href="browse?type=' + result[i].typeId + '" target="_blank">' + result[i].typeName + '</a>',
					formatIsk(result[i].buy),
					formatIsk(result[i].sell),
					formatIsk(unitProfit),
					formatInt(roi)
				]);
			}
			roiTable.draw();
			$.fn.dataTable.tables({visible: true, api: true}).columns.adjust();
			roiLoaded = true;
		});
	}

	function prettyType(type) {
		switch(type) {
			case '1':
				return 'Unknown';
			case '2':
				return 'Item Exchange';
			case '3':
				return 'Auction';
			case '4':
				return 'Courier';
			case '5':
				return 'Loan';
		}
		return "Unknown";
	}

	function prettyStatus(status) {
		switch(status) {
			case '1':
				return 'Outstanding';
			case '2':
				return 'In Progress';
			case '3':
				return 'Finished Issuer';
			case '4':
				return 'Finished Contractor';
			case '5':
				return 'Finished';
			case '6':
				return 'Cancelled';
			case '7':
				return 'Rejected';
			case '8':
				return 'Failed';
			case '9':
				return 'Deleted';
			case '10':
				return 'Reversed';
			case '11':
				return 'Unknown';
		}
		return "Unknown";
	}

});

