"use strict";

var type = 29668; // default to PLEX

$(document).ready(function() {

	// generate the sidebar
	var menu = new BrowserMenu();

	$('#open-in-market-browser').click(function() { window.location = '/browse?type=' + type; });
	$('#open-in-import-analyzer').click(function() { window.location = '/import?type=' + type; });
	$('#open-in-station-trader').click(function() { window.location = '/station-trade?type=' + type; });

});
