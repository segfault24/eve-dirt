"use strict";

// This whole thing is still garbage

function BrowserMenu() {

	var groups = null;
	var types = null;

	var loadedMarketGroups = [];
	var clickCB;

	this.onItemClick = function(func) {
		clickCB = func;
	}

	this.expandTo = function(marketGroupId) {
		// expand the sidebar to the current item
	}

	////////////////////////////////
	// private internal functions //
	////////////////////////////////

	function getGroupsInGroup(id) {
		if(groups == null) {
			return [];
		}
		if(id == 0) {
			id = null;
		}

		var g = [];
		var len = groups.length;
		for(var i=0; i<len; i++) {
			if(groups[i].parentGroupID == id) {
				g.push(groups[i]);
			}
		}
		return g;
	}

	function getTypesInGroup(id) {
		if(types == null) {
			return [];
		}
		if(id == 0) {
			id = null;
		}

		var t = [];
		var len = types.length;
		for(var i=0; i<len; i++) {
			if(types[i].marketGroupID == id) {
				t.push(types[i]);
			}
		}
		return t;
	}

	var loadMarketGroup = function(id) {
		// prevent reloading an already loaded marketgroup
		if($.inArray(id, loadedMarketGroups)!=-1) {
			return;
		}

		var blah = '';

		var g = getGroupsInGroup(id);
		for(var i=0; i<g.length; i++) {
			var mg_hdr = 'g' + g[i].marketGroupID + 'h';
			var mg_col = 'g' + g[i].marketGroupID + 'l';
			var mg_crd = 'g' + g[i].marketGroupID + 'b';
			blah += '<div class="card no-border">';
			blah += '	<div class="card-header browse-card-header" id="' + mg_hdr + '">';
			blah += '		<button class="btn btn-link browse-btn" type="button" data-toggle="collapse" data-target="#' + mg_col + '" aria-expanded="true" aria-controls="#' + mg_col + '">' + g[i].marketGroupName + '</button>';
			blah += '	</div>';
			blah += '	<div class="collapse browse-collapse" id="' + mg_col + '" name="' + g[i].marketGroupID + '" aria-labelledby="' + mg_hdr + '" data-parent="#g' + id + 'b">';
			blah += '		<div class="card-body browse-card-body">';
			blah += '			<div class="accordian" id="' + mg_crd + '">';
			blah += '			</div>';
			blah += '		</div>';
			blah += '	</div>';
			blah += '</div>';
		}

		var t = getTypesInGroup(id);
		for(var i=0; i<t.length; i++) {
			blah += '<div class="card no-border">'
			blah += '	<div class="card-body browse-card-body">';
			blah += '		<a class="market-item" name="' + t[i].typeID + '" href="/browse?type=' + t[i].typeID + '">' + t[i].typeName + '</a>';
			blah += '	</div>';
			blah += '</div>';
			//blah += '<hr class="no-margin">';
		}

		$('#g' + id + 'b').html(blah);

		loadedMarketGroups.push(id);
	}

	// grab the market groups
	$.ajax({
		url: '/api/market-groups',
		success: function(result) {
			groups = result;

			// load the top level groups
			loadMarketGroup(0);
		}
	});

	// grab the types
	$.ajax({
		url: '/api/market-types',
		success: function(result) {
			types = result;
		}
	});

	// setup the handlers
	$(document).on('show.bs.collapse', '.browse-collapse', function() {
		loadMarketGroup($(this).attr('name'));
	});
	$(document).on('click', '.market-item', function(e) {
		e.preventDefault();
		clickCB($(this).attr('name'), $(this).text());
	});
}

