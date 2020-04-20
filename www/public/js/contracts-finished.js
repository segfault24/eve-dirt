"use strict";

$(document).ready(function(){

	var contractsLoaded = false;

	// initialize tables
	var contractsTable = $('#contracts-table').DataTable({
		columns: [
			{title:'', responsivePriority: 2},
			{title:'Location', responsivePriority: 4},
			{title:'Issuer', responsivePriority: 1},
			{title:'Acceptor', responsivePriority: 1},
			{title:'Price', responsivePriority: 3},
			{title:'Title', responsivePriority: 5},
			{title:'Date Issued', responsivePriority: 4}
		],
		order: [[6, "desc"]],
		searching: true,
		paging: true,
		pageLength: 25,
		bInfo: false,
		responsive: true,
		select: true
	});

	function loadContracts() {
		if(contractsLoaded) {
			return;
		}
		$.getJSON('/api/contracts/exchange-finished', function(result) {
			for(var i=0; i<result.length; i++) {
				contractsTable.row.add([
					'<a class="open-in-game-contract" data-contractid="' + result[i].contractId + '" href="#"><i class="fa fa-magnet fa-fw"></i></a> <a href="contract?contract=' + result[i].contractId + '" target="_blank">Details</a>',
					result[i].locationId,
					result[i].issuerName,
					result[i].acceptorName,
					formatInt(result[i].price),
					result[i].title,
					result[i].dateAccepted
				]);
			}
			contractsTable.draw();
			$.fn.dataTable.tables({visible: true, api: true}).columns.adjust();
			contractsLoaded = true;
		})
	}

	loadContracts();

});

