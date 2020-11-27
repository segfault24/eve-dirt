"use strict";

function loadExchangeContracts() {
	if ($.fn.DataTable.isDataTable('#contracts-table')) {
		$('#contracts-table').DataTable().destroy();
		$('#contracts-table').empty();
	}
	var contractsTable = $('#contracts-table').DataTable({
		columns: [
			{title:'', responsivePriority: 2},
			{title:'Location', responsivePriority: 4},
			{title:'Issuer', responsivePriority: 1},
			{title:'Price', responsivePriority: 3},
			{title:'Title', responsivePriority: 5},
			{title:'Date Issued', responsivePriority: 4}
		],
		order: [[5, "desc"]],
		searching: true,
		paging: true,
		pageLength: 25,
		bInfo: false,
		responsive: true,
		select: true
	});
	$.getJSON('/api/contract/corp/exchange', function(result) {
		for(var i=0; i<result.length; i++) {
			contractsTable.row.add([
				'<a class="open-in-game-contract" data-contractid="' + result[i].contractId + '" href="#"><i class="fa fa-magnet fa-fw"></i></a> <a href="contract/' + result[i].contractId + '" target="_blank">Details</a>',
				result[i].locationName,
				result[i].issuerName,
				formatInt(result[i].price),
				result[i].title,
				result[i].dateIssued
			]);
		}
		contractsTable.draw();
		$.fn.dataTable.tables({visible: true, api: true}).columns.adjust();
	})
}

function loadFinishedExchangeContracts() {
	if ($.fn.DataTable.isDataTable('#contracts-table')) {
		$('#contracts-table').DataTable().destroy();
		$('#contracts-table').empty();
	}
	var contractsTable = $('#contracts-table').DataTable({
		columns: [
			{title:'', responsivePriority: 2},
			{title:'Location', responsivePriority: 4},
			{title:'Issuer', responsivePriority: 1},
			{title:'Acceptor', responsivePriority: 1},
			{title:'Price', responsivePriority: 3},
			{title:'Title', responsivePriority: 5},
			{title:'Completed', responsivePriority: 4}
		],
		order: [[6, "desc"]],
		searching: true,
		paging: true,
		pageLength: 25,
		bInfo: false,
		responsive: true,
		select: true
	});
	$.getJSON('/api/contract/corp/exchange/finished', function(result) {
		for(var i=0; i<result.length; i++) {
			contractsTable.row.add([
				'<a class="open-in-game-contract" data-contractid="' + result[i].contractId + '" href="#"><i class="fa fa-magnet fa-fw"></i></a> <a href="contract/' + result[i].contractId + '" target="_blank">Details</a>',
				result[i].locationName,
				result[i].issuerName,
				result[i].acceptorName,
				formatInt(result[i].price),
				result[i].title,
				result[i].dateCompleted
			]);
		}
		contractsTable.draw();
		$.fn.dataTable.tables({visible: true, api: true}).columns.adjust();
	})
}

function loadCourierContracts() {
	if ($.fn.DataTable.isDataTable('#contracts-table')) {
		$('#contracts-table').DataTable().destroy();
		$('#contracts-table').empty();
	}
	var contractsTable = $('#contracts-table').DataTable({
		columns: [
			{title:'', responsivePriority: 2},
			{title:'Issuer', responsivePriority: 4},
			{title:'Start', responsivePriority: 1},
			{title:'End', responsivePriority: 1},
			{title:'Volume', responsivePriority: 2},
			{title:'Collateral', responsivePriority: 3},
			{title:'Reward', responsivePriority: 2},
			{title:'Date Issued', responsivePriority: 5},
			{title:'Days to Complete', responsivePriority: 6}
		],
		order: [[7, "desc"]],
		searching: true,
		paging: true,
		pageLength: 25,
		bInfo: false,
		responsive: true,
		select: true
	});
	$.getJSON('/api/contract/corp/courier', function(result) {
		for(var i=0; i<result.length; i++) {
			contractsTable.row.add([
				'<a class="open-in-game-contract" data-contractid="' + result[i].contractId + '" href="#"><i class="fa fa-magnet fa-fw"></i></a> <a href="contract/' + result[i].contractId + '" target="_blank">Details</a>',
				result[i].issuerName,
				result[i].startLocation,
				result[i].endLocation,
				formatInt(result[i].volume),
				formatInt(result[i].collateral),
				formatInt(result[i].reward),
				result[i].dateIssued,
				result[i].daysToComplete
			]);
		}
		contractsTable.draw();
		$.fn.dataTable.tables({visible: true, api: true}).columns.adjust();
	})
}

function loadInProgressCourierContracts() {
	if ($.fn.DataTable.isDataTable('#contracts-table')) {
		$('#contracts-table').DataTable().destroy();
		$('#contracts-table').empty();
	}
	var contractsTable = $('#contracts-table').DataTable({
		columns: [
			{title:'', responsivePriority: 2},
			{title:'Issuer', responsivePriority: 4},
			{title:'Start', responsivePriority: 1},
			{title:'End', responsivePriority: 1},
			{title:'Volume', responsivePriority: 2},
			{title:'Collateral', responsivePriority: 3},
			{title:'Reward', responsivePriority: 2},
			{title:'Date Issued', responsivePriority: 5},
			{title:'Acceptor', responsivePriority: 6}
		],
		order: [[7, "desc"]],
		searching: true,
		paging: true,
		pageLength: 25,
		bInfo: false,
		responsive: true,
		select: true
	});
	$.getJSON('/api/contract/corp/courier/in-progress', function(result) {
		for(var i=0; i<result.length; i++) {
			contractsTable.row.add([
				'<a class="open-in-game-contract" data-contractid="' + result[i].contractId + '" href="#"><i class="fa fa-magnet fa-fw"></i></a> <a href="contract/' + result[i].contractId + '" target="_blank">Details</a>',
				result[i].issuerName,
				result[i].startLocation,
				result[i].endLocation,
				formatInt(result[i].volume),
				formatInt(result[i].collateral),
				formatInt(result[i].reward),
				result[i].dateIssued,
				result[i].acceptor
			]);
		}
		contractsTable.draw();
		$.fn.dataTable.tables({visible: true, api: true}).columns.adjust();
	})
}

function loadFinishedCourierContracts() {
	if ($.fn.DataTable.isDataTable('#contracts-table')) {
		$('#contracts-table').DataTable().destroy();
		$('#contracts-table').empty();
	}
	var contractsTable = $('#contracts-table').DataTable({
		columns: [
			{title:'', responsivePriority: 2},
			{title:'Issuer', responsivePriority: 4},
			{title:'Start', responsivePriority: 1},
			{title:'End', responsivePriority: 1},
			{title:'Volume', responsivePriority: 2},
			{title:'Collateral', responsivePriority: 3},
			{title:'Reward', responsivePriority: 2},
			{title:'Date Issued', responsivePriority: 5},
			{title:'Acceptor', responsivePriority: 6},
			{title:'Date Completed', responsivePriority: 6}
		],
		order: [[9, "desc"]],
		searching: true,
		paging: true,
		pageLength: 25,
		bInfo: false,
		responsive: true,
		select: true
	});
	$.getJSON('/api/contract/corp/courier/finished', function(result) {
		for(var i=0; i<result.length; i++) {
			contractsTable.row.add([
				'<a class="open-in-game-contract" data-contractid="' + result[i].contractId + '" href="#"><i class="fa fa-magnet fa-fw"></i></a> <a href="contract/' + result[i].contractId + '" target="_blank">Details</a>',
				result[i].issuerName,
				result[i].startLocation,
				result[i].endLocation,
				formatInt(result[i].volume),
				formatInt(result[i].collateral),
				formatInt(result[i].reward),
				result[i].dateIssued,
				result[i].acceptor,
				result[i].dateCompleted
			]);
		}
		contractsTable.draw();
		$.fn.dataTable.tables({visible: true, api: true}).columns.adjust();
	})
}

function loadFailedCourierContracts() {
	if ($.fn.DataTable.isDataTable('#contracts-table')) {
		$('#contracts-table').DataTable().destroy();
		$('#contracts-table').empty();
	}
	var contractsTable = $('#contracts-table').DataTable({
		columns: [
			{title:'', responsivePriority: 2},
			{title:'Issuer', responsivePriority: 4},
			{title:'Start', responsivePriority: 1},
			{title:'End', responsivePriority: 1},
			{title:'Volume', responsivePriority: 2},
			{title:'Collateral', responsivePriority: 3},
			{title:'Reward', responsivePriority: 2},
			{title:'Date Issued', responsivePriority: 5},
			{title:'Acceptor', responsivePriority: 6},
			{title:'Date Completed', responsivePriority: 6}
		],
		order: [[9, "desc"]],
		searching: true,
		paging: true,
		pageLength: 25,
		bInfo: false,
		responsive: true,
		select: true
	});
	$.getJSON('/api/contract/corp/courier/failed', function(result) {
		for(var i=0; i<result.length; i++) {
			contractsTable.row.add([
				'<a class="open-in-game-contract" data-contractid="' + result[i].contractId + '" href="#"><i class="fa fa-magnet fa-fw"></i></a> <a href="contract/' + result[i].contractId + '" target="_blank">Details</a>',
				result[i].issuerName,
				result[i].startLocation,
				result[i].endLocation,
				formatInt(result[i].volume),
				formatInt(result[i].collateral),
				formatInt(result[i].reward),
				result[i].dateIssued,
				result[i].acceptor,
				result[i].dateCompleted
			]);
		}
		contractsTable.draw();
		$.fn.dataTable.tables({visible: true, api: true}).columns.adjust();
	})
}

function loadOpenAuctionContracts() {
	if ($.fn.DataTable.isDataTable('#contracts-table')) {
		$('#contracts-table').DataTable().destroy();
		$('#contracts-table').empty();
	}
}

function loadFinishedAuctionContracts() {
	if ($.fn.DataTable.isDataTable('#contracts-table')) {
		$('#contracts-table').DataTable().destroy();
		$('#contracts-table').empty();
	}
}

function loadOpenCapContracts() {
	if ($.fn.DataTable.isDataTable('#contracts-table')) {
		$('#contracts-table').DataTable().destroy();
		$('#contracts-table').empty();
	}
	var contractsTable = $('#contracts-table').DataTable({
		columns: [
			{title:'Hull Type', responsivePriority: 1},
			{title:'Location', responsivePriority: 2},
			{title:'Contractor', responsivePriority: 5},
			{title:'Price', responsivePriority: 1},
			{title:'Fittings', responsivePriority: 3},
			{title:'Hull Estimate', responsivePriority: 4},
			{title:'Issued', responsivePriority: 5}
		],
		order: [[5, "asc"]],
		searching: true,
		paging: true,
		pageLength: 25,
		bInfo: false,
		responsive: true,
		select: true
	});
	$.getJSON('/api/contract/capital/outstanding', function(result) {
		for(var i=0; i<result.length; i++) {
			contractsTable.row.add([
				'<a class="open-in-game-contract" data-contractid="' + result[i].contractId + '" href="#"><i class="fa fa-magnet fa-fw"></i></a> <a href="contract/' + result[i].contractId + '" target="_blank">' + result[i].typeName + '</a>',
				result[i].locationName,
				result[i].issuer,
				formatInt(result[i].price),
				result[i].fittings !== null ? formatInt(result[i].fittings) : 0,
				result[i].hullvalue !== null ? formatInt(result[i].hullvalue) : formatInt(result[i].price),
				result[i].dateIssued
			]);
		}
		contractsTable.draw();
		$.fn.dataTable.tables({visible: true, api: true}).columns.adjust();
	})
}

function loadFinishedCapContracts() {
	if ($.fn.DataTable.isDataTable('#contracts-table')) {
		$('#contracts-table').DataTable().destroy();
		$('#contracts-table').empty();
	}
	var contractsTable = $('#contracts-table').DataTable({
		columns: [
			{title:'Hull Type', responsivePriority: 1},
			{title:'Location', responsivePriority: 2},
			{title:'Contractor', responsivePriority: 5},
			{title:'Price', responsivePriority: 1},
			{title:'Fittings', responsivePriority: 3},
			{title:'Hull Estimate', responsivePriority: 4},
			{title:'Completed', responsivePriority: 5}
		],
		order: [[6, "desc"]],
		searching: true,
		paging: true,
		pageLength: 25,
		bInfo: false,
		responsive: true,
		select: true
	});
	$.getJSON('/api/contract/capital/finished', function(result) {
		for(var i=0; i<result.length; i++) {
			contractsTable.row.add([
				'<a class="open-in-game-contract" data-contractid="' + result[i].contractId + '" href="#"><i class="fa fa-magnet fa-fw"></i></a> <a href="contract/' + result[i].contractId + '" target="_blank">' + result[i].typeName + '</a>',
				result[i].locationName,
				result[i].issuer,
				formatInt(result[i].price),
				result[i].fittings !== null ? formatInt(result[i].fittings) : 0,
				result[i].hullvalue !== null ? formatInt(result[i].hullvalue) : formatInt(result[i].price),
				result[i].dateCompleted
			]);
		}
		contractsTable.draw();
		$.fn.dataTable.tables({visible: true, api: true}).columns.adjust();
	})
}

function loadOpenCorpLogiContracts() {
	if ($.fn.DataTable.isDataTable('#contracts-table')) {
		$('#contracts-table').DataTable().destroy();
		$('#contracts-table').empty();
	}
	var contractsTable = $('#contracts-table').DataTable({
		columns: [
			{title:'', responsivePriority: 2},
			{title:'Location', responsivePriority: 1},
			{title:'Issuer', responsivePriority: 1},
			{title:'Issuer Corp', responsivePriority: 3},
			{title:'Assignee', responsivePriority: 3},
			{title:'Title', responsivePriority: 4},
			{title:'Date Issued', responsivePriority: 5}
		],
		order: [[6, "desc"]],
		searching: true,
		paging: true,
		pageLength: 25,
		bInfo: false,
		responsive: true,
		select: true
	});
	$.getJSON('/api/contract/corp/business/open', function(result) {
		for(var i=0; i<result.length; i++) {
			contractsTable.row.add([
				'<a class="open-in-game-contract" data-contractid="' + result[i].contractId + '" href="#"><i class="fa fa-magnet fa-fw"></i></a> <a href="contract/' + result[i].contractId + '" target="_blank">Details</a>',
				result[i].locationName,
				result[i].issuer,
				result[i].issuerCorp,
				result[i].assignee,
				result[i].title,
				result[i].dateIssued
			]);
		}
		contractsTable.draw();
		$.fn.dataTable.tables({visible: true, api: true}).columns.adjust();
	})
}

function loadFinishedCorpLogiContracts() {
	if ($.fn.DataTable.isDataTable('#contracts-table')) {
		$('#contracts-table').DataTable().destroy();
		$('#contracts-table').empty();
	}
	var contractsTable = $('#contracts-table').DataTable({
		columns: [
			{title:'', responsivePriority: 2},
			{title:'Location', responsivePriority: 1},
			{title:'Issuer', responsivePriority: 1},
			{title:'Issuer Corp', responsivePriority: 3},
			{title:'Assignee', responsivePriority: 3},
			{title:'Title', responsivePriority: 4},
			{title:'Completed', responsivePriority: 5}
		],
		order: [[6, "desc"]],
		searching: true,
		paging: true,
		pageLength: 25,
		bInfo: false,
		responsive: true,
		select: true
	});
	$.getJSON('/api/contract/corp/business/finished', function(result) {
		for(var i=0; i<result.length; i++) {
			contractsTable.row.add([
				'<a class="open-in-game-contract" data-contractid="' + result[i].contractId + '" href="#"><i class="fa fa-magnet fa-fw"></i></a> <a href="contract/' + result[i].contractId + '" target="_blank">Details</a>',
				result[i].locationName,
				result[i].issuer,
				result[i].issuerCorp,
				result[i].assignee,
				result[i].title,
				result[i].dateCompleted
			]);
		}
		contractsTable.draw();
		$.fn.dataTable.tables({visible: true, api: true}).columns.adjust();
	})
}

function loadTopContractors() {
	if ($.fn.DataTable.isDataTable('#contracts-table')) {
		$('#contracts-table').DataTable().destroy();
		$('#contracts-table').empty();
	}
	var contractsTable = $('#contracts-table').DataTable({
		columns: [
			{title:'Contractor', responsivePriority: 1},
			{title:'Isk Total', responsivePriority: 1},
			{title:'Count', responsivePriority: 2}
		],
		order: [[1, "desc"]],
		searching: true,
		paging: true,
		pageLength: 25,
		bInfo: false,
		responsive: true,
		select: true
	});
	$.getJSON('/api/contract/topcontractors', function(result) {
		for(var i=0; i<result.length; i++) {
			contractsTable.row.add([
				result[i].contractor,
				formatInt(result[i].total),
				result[i].count
			]);
		}
		contractsTable.draw();
		$.fn.dataTable.tables({visible: true, api: true}).columns.adjust();
	})
}

function loadTopShippers() {
	if ($.fn.DataTable.isDataTable('#contracts-table')) {
		$('#contracts-table').DataTable().destroy();
		$('#contracts-table').empty();
	}
	var contractsTable = $('#contracts-table').DataTable({
		columns: [
			{title:'Contractor', responsivePriority: 1},
			{title:'Reward', responsivePriority: 3},
			{title:'Collateral', responsivePriority: 3},
			{title:'Volume', responsivePriority: 2}
		],
		order: [[3, "desc"]],
		searching: true,
		paging: true,
		pageLength: 25,
		bInfo: false,
		responsive: true,
		select: true
	});
	$.getJSON('/api/contract/topshippers', function(result) {
		for(var i=0; i<result.length; i++) {
			contractsTable.row.add([
				result[i].contractor,
				formatInt(result[i].totalreward),
				formatInt(result[i].totalcollat),
				formatInt(result[i].totalvolume)
			]);
		}
		contractsTable.draw();
		$.fn.dataTable.tables({visible: true, api: true}).columns.adjust();
	})
}

function loadTopHaulers() {
	if ($.fn.DataTable.isDataTable('#contracts-table')) {
		$('#contracts-table').DataTable().destroy();
		$('#contracts-table').empty();
	}
	var contractsTable = $('#contracts-table').DataTable({
		columns: [
			{title:'Hauler', responsivePriority: 1},
			{title:'Reward', responsivePriority: 3},
			{title:'Collateral', responsivePriority: 3},
			{title:'Volume', responsivePriority: 2}
		],
		order: [[3, "desc"]],
		searching: true,
		paging: true,
		pageLength: 25,
		bInfo: false,
		responsive: true,
		select: true
	});
	$.getJSON('/api/contract/tophaulers', function(result) {
		for(var i=0; i<result.length; i++) {
			contractsTable.row.add([
				result[i].contractor,
				formatInt(result[i].totalreward),
				formatInt(result[i].totalcollat),
				formatInt(result[i].totalvolume)
			]);
		}
		contractsTable.draw();
		$.fn.dataTable.tables({visible: true, api: true}).columns.adjust();
	})
}
