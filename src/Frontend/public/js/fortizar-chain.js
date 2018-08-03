"use strict";

var systems = [];
var midX = 0;
var midZ = 0;
var xRad = 1;
var zRad = 1;

$(document).ready(function(){
	$('#mapCanvas').resize(function() {
		resizeCanvas();
	});
	
	resizeCanvas();
	
	$.ajax({
		url: '/api/fortizar-chain',
		async: true,
		success: function(result) {
			systems = result;
			recalcParams();
			redrawMap();
		}
	});
});

function resizeCanvas() {
	var h = $('#canvasWrapper').height();
	var w = $('#canvasWrapper').width();
	var ctx = $('#mapCanvas')[0].getContext('2d');
	ctx.canvas.height = h;
	ctx.canvas.width = w;
	redrawMap();
}

function recalcParams() {
	var minX = Number.MAX_VALUE;
	var maxX = Number.MIN_VALUE;
	var minZ = Number.MAX_VALUE;
	var maxZ = Number.MIN_VALUE;
	
	for (var i=0; i<systems.length; i++) {
		var x = Number(systems[i].x);
		if (x < minX) { minX = x; }
		if (x > maxX) { maxX = x; }
		var z = Number(systems[i].z);
		if (z < minZ) { minZ = z; }
		if (z > maxZ) { maxZ = z; }
	}
	
	midX = (minX + maxX)/2;
	midZ = (minZ + maxZ)/2;
	xRad = maxX - midX;
	zRad = maxZ - midZ;
}

function redrawMap() {
	var ctx = $('#mapCanvas')[0].getContext('2d');
	var w = ctx.canvas.width;
	var h = ctx.canvas.height;
	
	//console.log("w,h: " + w + "," + h);
	
	for (var i=0; i<systems.length; i++) {
		var x = Number(systems[i].x);
		var z = Number(systems[i].z);
		//console.log("raw: " + x + "," + z);
		
		var cenX = x - midX;
		var cenZ = z - midZ;
		//console.log("cen: " + cenX + "," + cenZ);
		
		var nx = (x - midX) / zRad;
		var nz = (z - midZ) / zRad;
		//console.log("norm: " + nx + "," + nz);
		
		var dx = nx*w/2 + w/2;
		var dz = h - (nz*h/2 + h/2);
		//console.log("draw: " + dx + "," + dz);
		
		systems[i].drawX = dx;
		systems[i].drawZ = dz;
	}
	
	for (var i=0; i<systems.length; i++) {
		for (var j=0; j<systems.length; j++) {
			var ly = distLY(
					Number(systems[i].x),
					Number(systems[i].y),
					Number(systems[i].z),
					Number(systems[j].x),
					Number(systems[j].y),
					Number(systems[j].z));
			//console.log(ly);
			
			if (ly <= 7) {
				ctx.beginPath();
				ctx.moveTo(systems[i].drawX, systems[i].drawZ);
				ctx.lineTo(systems[j].drawX, systems[j].drawZ);
				if (ly <= 6) {
					// all caps
					ctx.strokeStyle = 'blue';
				} else {
					// no supers
					//console.log('death to all supers: ' + ly);
					ctx.strokeStyle = 'red';
				}
				ctx.stroke();
				ctx.closePath();
			}
		}
	}

	for (var i=0; i<systems.length; i++) {
		ctx.beginPath();
		ctx.arc(systems[i].drawX, systems[i].drawZ, 4, 0, 2*Math.PI);
		if (systems[i].superDocking == 1) {
			ctx.fillStyle = '#2AFF00';
		} else {
			ctx.fillStyle = 'black';
		}
		ctx.fill();
		ctx.stroke();
		ctx.closePath();
	}
}

function distLY(x1, y1, z1, x2, y2, z2) {
	var c = Number("9.461e15");
	var ly = Math.sqrt(Math.pow(x2/c-x1/c, 2) + Math.pow(y2/c-y1/c, 2) + Math.pow(z2/c-z1/c, 2));
	return ly;
}

