"use strict";

// global chart formatting information
var myColors = {
	data: [
		{border: "rgba(0,0,255,0.4)", fill: "rgba(0,0,255,0.2)"},
		{border: "rgba(0,255,0,0.4)", fill: "rgba(0,255,0,0.2)"},
		{border: "rgba(255,0,0,0.4)", fill: "rgba(255,0,0,0.2)"}
	],
	stat: [
		{border: "rgba(20,20,20,0.4)", fill: "rgba(20,20,20,0.2)"},
		{border: "rgba(100,100,100,0.4)", fill: "rgba(100,100,100,0.2)"},
	]
};
var myBorderWidth = 2;
var myPointRadius = 3;

var myDisplayFormatYr = {
	'millisecond': 'MMM DD',
	'second': 'MMM DD',
	'minute': 'MMM DD',
	'hour': 'MMM DD',
	'day': 'MMM DD',
	'week': 'MMM DD',
	'month': 'MMM DD',
	'quarter': 'MMM DD',
	'year': 'MMM DD'
};
var myDisplayFormatAll = {
	'millisecond': 'MMM DD YYYY',
	'second': 'MMM DD YYYY',
	'minute': 'MMM DD YYYY',
	'hour': 'MMM DD YYYY',
	'day': 'MMM DD YYYY',
	'week': 'MMM DD YYYY',
	'month': 'MMM DD YYYY',
	'quarter': 'MMM DD YYYY',
	'year': 'MMM DD YYYY'
};

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

function dateSqlToJs(sqldate) {
	var t = sqldate.split(/[- :]/);
	return new Date(Date.UTC(t[0], t[1]-1, t[2], t[3], t[4], t[5]));
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

// given an array of evenly spaced {x:, y:} pairs, this function returns an
// array of {x:, y:} pairs representing a simple moving average of length 'len'
function sma(data, len) {
	if(data.length < len) {
		return [];
	}

	var sma = [];

	// calculate the sma for the first possible point
	var init = 0;
	for(var i=0; i<len; i++) {
		init += data[i].y;
	}
	sma.push({x:data[len-1].x, y:init/len});

	// calculate the successive sma's
	var prev = sma[0].y;
	for(var i=len; i<data.length; i++) {
		var cur = prev + data[i].y/len - data[i-len].y/len;
		sma.push({x:data[i].x, y:cur});
		prev = cur;
	}

	return sma;
}

function myAjax(endpoint, callback) {
	$.ajax({
		url: '/api/' + endpoint,
		async: true,
		success: function(result) {
			if(callback != null) {
				callback(result);
			}
		}
	});
}

$(function() {
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
});
