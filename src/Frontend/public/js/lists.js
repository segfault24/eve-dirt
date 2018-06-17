"use strict";

function myListsLoad() {
	var listTable = $('#list-table').DataTable({
		order: [[0, "desc"]],
		searching: false,
		paging: false,
		bInfo: false,
		responsive: true,
		select: true,
		language: {
			emptyTable: "You have no lists"
		},
		dom: 'Bfrtip',
		buttons: [
			{
				text: 'Add',
				className: 'btn btn-success',
				action: function (e, dt, node, config) {
					$('#addListModal').modal();
				}
			},
			{
				text: 'Edit',
				className: 'btn btn-info',
				action: function (e, dt, node, config) {
					console.log('edit not implemented yet');
				}
			},
			{
				text: 'Delete',
				className: 'btn btn-danger',
				action: function (e, dt, node, config) {
					if (listTable.rows({selected:true}).count() > 0) {
						$('#deleteListModal').modal();
					}
				}
			}
		]
	});

	$(document).on('click', '#list-add-button', function(e) {
		e.preventDefault();

		var listnm = $('#list-add-name').val();
		var listvis = $('#list-add-visibility').val();
		$.ajax({
			url: '/api/lists/',
			type: 'POST',
			data: {"info":{"name":listnm,"public":listvis}},
			success: function(result) {
				$('#list-add-name').val('');
				$('#list-add-visibility').val(0);
				loadListTable(listTable);
			},
			error: function() {},
			complete: function() {}
		});
	});

	$(document).on('click', '#list-delete-button', function(e) {
		e.preventDefault();
		
		$('#list-table').dataTable().$('tr.selected').each(function() {
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
				var link = $('<a>', {className:'list-select', text:lists[i].name , href:'list-detail?listid='+lists[i].listId });
				listTable.row.add([
					link[0].outerHTML,
					lists[i].typeCount,
					lists[i].public==1?'Public':'Private'
				]).node().id = lists[i].listId;
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
		pageLength: 25,
		language: {
			emptyTable: "There are no items in this list"
		},
		/*dom: 'Bfrtip',
		buttons: [
			{
				text: 'Add',
				className: 'btn btn-success',
				action: function (e, dt, node, config) {
					$('#addItemModal').modal();
				}
			},
			{
				text: 'Edit',
				className: 'btn btn-info',
				action: function (e, dt, node, config) {
					console.log('edit not implemented yet');
				}
			},
			{
				text: 'Delete',
				className: 'btn btn-danger',
				action: function (e, dt, node, config) {
					$('#deleteItemModal').modal();
				}
			}
		]*/
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

