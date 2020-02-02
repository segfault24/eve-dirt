"use strict";

$(document).ready(function(){

	var contractsLoaded = false;

	// initialize tables
	var contractsTable = $('#contracts-table').DataTable({
		columns: [
			{title:'Type', responsivePriority: 2},
			{title:'Issuer', responsivePriority: 1},
			{title:'Status', responsivePriority: 3},
			{title:'Acceptor', responsivePriority: 5},
			{title:'Date Issued', responsivePriority: 4}
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

	function loadContracts() {
		if(contractsLoaded) {
			return;
		}
		$.getJSON('/api/market/contracts', function(result) {
			for(var i=0; i<result.length; i++) {
				contractsTable.row.add([
					'<a class="open-in-game-contract" data-contractid="' + result[i].contractId + '" href="#"><i class="fa fa-magnet fa-fw"></i> ' + prettyType(result[i].type) + '</a>',
					result[i].issuerName,
					prettyStatus(result[i].status),
					result[i].acceptorName,
					result[i].dateIssued
				]);
			}
			contractsTable.draw();
			$.fn.dataTable.tables({visible: true, api: true}).columns.adjust();
			contractsLoaded = true;
		})
	}

	function prettyType(type) {
		switch(type) {
			case 'item_exchange':
				return 'Item Exchange';
			case 'courier':
				return 'Courier';
		}
		return "Unknown";
	}

	function prettyStatus(status) {
		switch(status) {
			case 'outstanding':
				return 'Outstanding';
			case 'in_progress':
				return 'In Progress';
			case 'finished':
				return 'Finished';
			case 'deleted':
				return 'Deleted';
			case 'failed':
				return 'Failed';
		}
		return "Unknown";
	}

	loadContracts();

});

