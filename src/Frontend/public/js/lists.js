"use strict";

var listTable;
var itemTable;
var curSelectedList = null;

function disableControls() {
	$('#list-add-name').prop('disabled', true);
	$('#list-add-button').prop('disabled', true);
	$('.list-delete-button').prop('disabled', true);
	$('.list-select').prop('disabled', true);
	$('#item-add-name').prop('disabled', true);
	$('#item-add-button').prop('disabled', true);
	$('.item-delete-button').prop('disabled', true);
}

function enableControls() {
	$('#list-add-name').prop('disabled', false);
	$('#list-add-button').prop('disabled', false);
	$('.list-delete-button').prop('disabled', false);
	$('.list-select').prop('disabled', false);
	if(curSelectedList != null) {
		$('#item-add-name').prop('disabled', false);
		$('#item-add-button').prop('disabled', false);
		$('.item-delete-button').prop('disabled', false);
	}
}

function clearListTable(redraw) {
	listTable.clear();
	if(redraw) {
		listTable.draw();
	}
}

function loadListTable() {
	clearListTable(false);

	$.ajax({
		url: '/api/lists/',
		type: 'GET',
		success: function(result) {
			var lists = result;
			for(var i=0; i<lists.length; i++) {
				listTable.row.add([
					'<a id="' + lists[i].listId + '" class="list-select" href="#">' + lists[i].name + '</a>',
					lists[i].public==1?'Public':'Private',
					'<button type="submit" id="' + lists[i].listId + '" class="btn btn-xs btn-danger list-delete-button">Delete</button>'
				]);
			}
			listTable.draw();
		}
	});
}

function clearItemTable(redraw) {
	curSelectedList = null;
	$('#list-name').text('');
	itemTable.clear();
	if(redraw) {
		itemTable.draw();
	}
}

function loadItemTable(listid) {
	disableControls();

	clearItemTable(false);
	curSelectedList = listid;

	$.ajax({
		url: '/api/lists/' + listid,
		type: 'GET',
		success: function(result) {
			$('#list-name').text(result.name);
		}
	});

	$.ajax({
		url: '/api/lists/' + listid + '/types/',
		type: 'GET',
		success: function(result) {
			var items = result;
			for(var i=0; i<items.length; i++) {
				itemTable.row.add([
					'<a target="_blank" href="/browse?type=' + items[i].typeId + '">' + items[i].typeName + '</a>',
					items[i].quantity,
					'<button type="submit" id="' + items[i].typeId + '" class="btn btn-xs btn-danger item-delete-button">Remove</button>'
				]);
			}
			itemTable.draw();
			$.fn.dataTable.tables({visible: true, api: true}).columns.adjust();
			enableControls();
		}
	});
}

$(function() {
	listTable = $('#list-table').DataTable({
		order: [[0, "desc"]],
		searching: false,
		paging: false,
		bInfo: false,
		responsive: true,
		language: {
			emptyTable: "You have no lists"
		}
	});

	itemTable = $('#item-table').DataTable({
		order: [[0, "asc"]],
		searching: true,
		responsive: true,
		language: {
			emptyTable: "There are no items in this list"
		}
	});

	$(document).on('click', '#list-add-button', function(e) {
		e.preventDefault();
		disableControls();

		var listname = $('#list-add-name').val();
		$.ajax({
			url: '/api/lists/',
			type: 'POST',
			data: {"info":{"name":listname}},
			success: function(result) {
				$('#list-add-name').val('');
				loadListTable();
			},
			error: function() {

			},
			complete: function() {
				enableControls();
			}
		});
	});

	$(document).on('click', '.list-delete-button', function(e) {
		e.preventDefault();
		disableControls();

		var listid = $(this).attr('id');
		$.ajax({
			url: '/api/lists/' + listid,
			type: 'DELETE',
			success: function(result) {
				loadListTable();
				if(listid == curSelectedList) {
					clearItemTable(true);
				}
			},
			error: function() {

			},
			complete: function() {
				enableControls();
			}
		});
	});

	$(document).on('click', '.list-select', function(e) {
		e.preventDefault();

		clearItemTable(true);
		loadItemTable($(this).attr('id'));
	});

	$(document).on('click', '#item-add-button', function(e) {
		e.preventDefault();
		disableControls();

		$.ajax({
			url: '/api/lists/' + curSelectedList + '/types/0',
			type: 'PUT',
			data: {typeName:$('#item-add-name').val(),quantity:1},
			success: function(result) {
				loadItemTable(curSelectedList);
				$('#item-add-name').val('');
			},
			error: function() {

			},
			complete: function() {
				enableControls();
			}
		});
	});

	$(document).on('click', '.item-delete-button', function(e) {
		e.preventDefault();
		disableControls();

		var typeid = $(this).attr('id');
		$.ajax({
			url: '/api/lists/' + curSelectedList + '/types/' + typeid,
			type: 'DELETE',
			success: function(result) {
				loadItemTable(curSelectedList);
			},
			error: function() {

			},
			complete: function() {
				enableControls();
			}
		});
	});

	$.getJSON('/api/search-types', function(data) {
		$('#item-add-name').autocomplete({
			source: data,
			minLength: 5,
			select: function(e, ui) {
				e.preventDefault();
				$('#item-add-name').val(ui.item.label);
			}
		});
	});

	loadListTable();
});
