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
			blah += '<div class="panel panel-default browse-panel">';
			blah += '	<div class="panel-heading browse-panel-heading">';
			blah += '		<p class="panel-title browse-panel-title">';
			blah += '			<a class="market-group" data-toggle="collapse" data-parent="#g' + id + 'b" href="#g' + g[i].marketGroupID + 'c">' + g[i].marketGroupName + '</a>';
			blah += '		</p>';
			blah += '	</div>';
			blah += '	<div id="g' + g[i].marketGroupID + 'c" name="' + g[i].marketGroupID + '" class="panel-collapse collapse browse-collapse">';
			blah += '		<div id="g' + g[i].marketGroupID + 'b" class="panel-body browse-panel-body">';
			blah += '		</div>';
			blah += '	</div>';
			blah += '</div>';
		}

		var t = getTypesInGroup(id);
		for(var i=0; i<t.length; i++) {
			blah += '<div class="panel-body no-padding browse-panel-item">'
			blah += '	<a class="market-item" name="' + t[i].typeID + '" href="/browse?type=' + t[i].typeID + '">' + t[i].typeName + '</a>';
			blah += '</div>';
			blah += '<hr class="no-margin">';
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

