"use strict";

// https://stackoverflow.com/questions/149055/how-can-i-format-numbers-as-money-in-javascript

function formatInt(n) {
	var c = 0,
		d = ".",
		t = ",",
		s = n < 0 ? "-" : "",
		i = String(parseInt(n = Math.abs(Number(n) || 0).toFixed(c))),
		j = (j = i.length) > 3 ? j % 3 : 0;
	return s + (j ? i.substr(0, j) + t : "") + i.substr(j).replace(/(\d{3})(?=\d)/g, "$1" + t) + (c ? d + Math.abs(n - i).toFixed(c).slice(2) : "");
};

function formatIsk(n) {
	var c = 2,
		d = ".",
		t = ",",
		s = n < 0 ? "-" : "",
		i = String(parseInt(n = Math.abs(Number(n) || 0).toFixed(c))),
		j = (j = i.length) > 3 ? j % 3 : 0;
	return s + (j ? i.substr(0, j) + t : "") + i.substr(j).replace(/(\d{3})(?=\d)/g, "$1" + t) + (c ? d + Math.abs(n - i).toFixed(c).slice(2) : "");
};

function avgArr(arr, cb) {
	var sum = 0;
	for(var i=0; i<arr.length; i++) {
		sum += cb(arr[i])/arr.length;
	}
	return sum;
}

// accepts an array of {x:,y:} pairs and returns parameters
// used in formatKMBT() for shortnening numbers (ex. 1000 to 1k)
function getKMBTParams(arr) {
	var max = Math.max.apply(null, arr.map(function(o) {return o.y}));
	var min = Math.min.apply(null, arr.map(function(o) {return o.y}));

	var maxmag = 0;
	while(Math.floor(max/10) > 1) {
		max /= 10;
		maxmag++;
	}
	var minmag = 0;
	while(Math.floor(min/10) < -1) {
		min /= 10;
		minmag++;
	}

	return {
		magnitude: Math.max(Math.abs(minmag), Math.abs(maxmag)),
		range: maxmag - minmag
	};
}

// 10^magnitude
function formatKMBT(value, params) {
	var m = 3*Math.floor(params.magnitude/3); // sanitize to a multiple of 3

	var ret = parseFloat(value)/Math.pow(10, m);
	if(params.magnitude%3 == 0) {
		ret = ret.toFixed(1);
	} else {
		ret = ret.toFixed(0);
	}

	switch(m) {
		case 3:
			ret += 'k';
			break;
		case 6:
			ret += 'm';
			break;
		case 9:
			ret += 'b';
			break;
		case 12:
			ret += 't';
			break;
		case 16:
			ret += 'q';
			break;
		default:
			break;
	}

	return ret;
}

function getUrlParam(key) {
	var url = window.location.search.substring(1);
	var paramPairs = url.split('&');
	for(var i=0; i<paramPairs.length; i++) {
		var paramPair = paramPairs[i].split('=');
		if(paramPair[0] == key) {
			return paramPair[1];
		}
	}
	return '';
}

// for tukey's test
function getOutlierBounds(arr) {
	var k = 2;
	var l = arr.length;

	// cant do this for tiny arrays
	if(l<3) { return null; }

	// duplicate and sort
	var a = arr.concat().sort(function(a,b){return a.y - b.y;});

	// find the first and third quartiles
	// here be dragons
	var q1, q3;
	switch(l%4) {
		case 0:
			q1 = a[l/4-1].y;
			q3 = a[l*3/4].y;
			break;
		case 1:
			q1 = ( a[(l-1)/4-1].y + a[(l-1)/4].y )/2;
			q3 = ( a[(l-1)*3/4].y + a[(l-1)*3/4+1].y )/2;
			break;
		case 2:
			q1 = ( a[(l-2)/4-1].y + a[(l-2)/4].y )/2;
			q3 = ( a[(l-2)*3/4+1].y + a[(l-2)*3/4+2].y )/2;
			break;
		case 3:
			q1 = a[(l-3)/4].y;
			q3 = a[(l-3)*3/4+2].y;
			break;
	}

	// calculate and return the bounds
	return {lower: q1-k*(q3-q1), upper: q3+k*(q3-q1)};
}

function removeOutliers(arr, bounds) {
	var narr = [];

	for(var i=0; i<arr.length; i++) {
		// check if it's out of bounds
		if(arr[i].y < bounds.lower || arr[i].y > bounds.upper) {
			// out of bounds
			// check if we're at the beginning of the array
			if(narr.length==0) {
				// skip until we find something that's not out of bounds
				continue;
			} else {
				// not at the beginning, just use the previous value
				narr.push({x:arr[i].x, y:narr[narr.length-1].y});
			}
		} else {
			// not out of bounds
			narr.push(arr[i]);
		}
	}

	return narr;
}

$(function() {
	// click handler for opening types in game market
	$(document).on('click', '.open-in-game', function(e) {
		e.preventDefault();
		$.getJSON('/api/market/open-in-game/' + $(this).attr('data-typeid'), null);
	});

	// click handler for opening types in game contract
	$(document).on('click', '.open-in-game-contract', function(e) {
		e.preventDefault();
		$.getJSON('/api/market/open-in-game-contract/' + $(this).attr('data-contractid'), null);
	});

	$.getJSON('/api/search-types', function(data) {
		$('#search').autocomplete({
			source: data,
			minLength: 4,
			select: function(e, ui) {
				e.preventDefault();
				$('#search').val(ui.item.label);
				window.location = '/browse?type=' + ui.item.value;
			}
		});
	});

	Notification.requestPermission().then(function(result) {
		if (Notification.permission === "granted") {
			pollNotifications();
		}
	});

	let audio = new Audio('/res/notif.mp3');
	function pollNotifications() {
		$.getJSON('/api/notifications/new', function(data) {
			if (data.length == 1) {
				var options = { body: data[0].text };
				if (data[0].typeId != null) {
					options.icon = 'https://imageserver.eveonline.com/Type/' + data[0].typeId + '_64.png';
				}
				var notif = new Notification(data[0].title, options);
				audio.play();
				//setTimeout(notif.close.bind(notif), 6000);
			} else if (data.length > 1) {
				var notif = new Notification("Multiple New Notifications");
				audio.play();
				//setTimeout(notif.close.bind(notif), 6000);
			}
		});
		setTimeout(pollNotifications, 60000);
	}

});
