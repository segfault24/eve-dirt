"use strict";

$(document).ready(function(){

	var itemsLoaded = false;

	var contractId = getUrlParam('contract');

	// initialize tables
	var itemsTable = $('#contract-items-table').DataTable({
		columns: [
			{title:'Type', responsivePriority: 1},
			{title:'Qt', responsivePriority: 1}
		],
		order: [[1, "desc"]],
		searching: true,
		paging: true,
		pageLength: 40,
		bLengthChange: false,
		bInfo: false,
		responsive: true,
		select: true
	});

	function loadItems() {
		if(itemsLoaded) {
			return;
		}
		$.getJSON('/api/contract/' + contractId + '/items/', function(result) {
			for(var i=0; i<result.length; i++) {
				itemsTable.row.add([
					result[i].typeName,
					result[i].quantity
				]);
			}
			itemsTable.draw();
			$.fn.dataTable.tables({visible: true, api: true}).columns.adjust();
			itemsLoaded = true;
		})
	}

	loadItems();

});

