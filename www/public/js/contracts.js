"use strict";

$(document).ready(function(){

	var contractsLoaded = false;

	// initialize tables
	var contractsTable = $('#contracts-table').DataTable({
		columns: [
			{title:'', responsivePriority: 2},
			{title:'Issuer', responsivePriority: 1},
			{title:'Price (m)', responsivePriority: 3},
			{title:'Title', responsivePriority: 5},
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
		$.getJSON('/api/contracts/exchange', function(result) {
			for(var i=0; i<result.length; i++) {
				contractsTable.row.add([
					'<a class="open-in-game-contract" data-contractid="' + result[i].contractId + '" href="#"><i class="fa fa-magnet fa-fw"></i></a> <a href="contract?contract=' + result[i].contractId + '" target="_blank">Details</a>',
					result[i].issuerName,
					formatInt(result[i].price),
					result[i].title,
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

	loadContracts();

});

