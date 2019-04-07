"use strict";

var type = 44992; // default to PLEX

$(document).ready(function() {

	// generate the sidebar
	var menu = new BrowserMenu();

	$('#open-in-market-browser').click(function() { window.location = '/browse?type=' + type; });
	$('#open-in-import-analyzer').click(function() { window.location = '/import?type=' + type; });
	$('#open-in-station-trader').click(function() { window.location = '/station-trade?type=' + type; });
	$('#refresh-data').click(function() { reloadData(); });
	$('#open-in-game').click(function() { myAjax('market/open-in-game/' + type, null); });

	function reloadData() {
	}

});
