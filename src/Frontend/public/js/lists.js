"use strict";

function myListsLoad() {
    var listTable = $('#list-table').DataTable({
		order: [[0, "desc"]],
		searching: false,
		paging: false,
		bInfo: false,
		responsive: true,
		language: {
			emptyTable: "You have no lists"
		}
	});
	
	$(document).on('click', '#list-add-button', function(e) {
		e.preventDefault();

		var listname = $('#list-add-name').val();
		$.ajax({
			url: '/api/lists/',
			type: 'POST',
			data: {"info":{"name":listname}},
			success: function(result) {
				$('#list-add-name').val('');
				loadListTable(listTable);
			},
			error: function() {},
			complete: function() {}
		});
	});

	$(document).on('click', '.list-delete-button', function(e) {
		e.preventDefault();

		var listid = $(this).attr('id');
		$.ajax({
			url: '/api/lists/' + listid,
			type: 'DELETE',
			success: function(result) {
				loadListTable(listTable);
			},
			error: function() {},
			complete: function() {}
		});
	});

    loadListTable(listTable);
}

function loadListTable(listTable) {
    $.ajax({
		url: '/api/lists/',
		type: 'GET',
		success: function(result) {
		    listTable.clear();
			var lists = result;
			for(var i=0; i<lists.length; i++) {
				listTable.row.add([
					'<a id="' + lists[i].listId + '" class="list-select" href="list-detail?listid=' + lists[i].listId + '">' + lists[i].name + '</a>',
					lists[i].public==1?'Public':'Private',
					'<button type="submit" id="' + lists[i].listId + '" class="btn btn-xs btn-danger list-delete-button">Delete</button>'
				]);
			}
			listTable.draw();
		}
	});
}

function listDetailLoad() {
	
	var listId = getUrlParam('listid');	

    var itemTable = $('#item-table').DataTable({
        order: [[0, "asc"]],
		searching: true,
		responsive: true,
		language: {
			emptyTable: "There are no items in this list"
		}
	});
	
	$(document).on('click', '#item-add-button', function(e) {
		e.preventDefault();

		$.ajax({
			url: '/api/lists/' + listId + '/types/0',
			type: 'PUT',
			data: {typeName:$('#item-add-name').val(),quantity:1},
			success: function(result) {
				$('#item-add-name').val('');
				loadItemTable(itemTable, listId);
			},
			error: function() {},
			complete: function() {}
		});
	});
	
    $(document).on('click', '.item-delete-button', function(e) {
		e.preventDefault();

		var typeid = $(this).attr('id');
		$.ajax({
			url: '/api/lists/' + listId + '/types/' + typeid,
			type: 'DELETE',
			success: function(result) {
				loadItemTable(itemTable, listId);
			},
			error: function() {},
			complete: function() {}
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
	
	$.ajax({
		url: '/api/lists/' + listId,
		type: 'GET',
		success: function(result) {
			$('#list-name').text(result.name);
		}
	});

	loadItemTable(itemTable, listId);
}

function loadItemTable(itemTable, listId) {
    $.ajax({
		url: '/api/lists/' + listId + '/types/',
		type: 'GET',
		success: function(result) {
		    itemTable.clear();
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
		}
	});
}

