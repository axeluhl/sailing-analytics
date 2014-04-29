/**
 * Streamlets
 * 
 * Visualizes a vector field using an animated swarm of particles
 * where each particle moves along the flow of the vector field
 * 
 * - integrated with Google Maps v3
 * - support vector fields unaligned with lat/lng-grid
 * - support time-dependent vector fields
 * - integrated with GWT for Strategy Simulator
 *
 * @author Christopher Ronnewinkel (D036654)
 * 
 */

var Vector = function(x, y) {
	this.x = x;
	this.y = y;
};

Vector.prototype.length = function() {
	return Math.sqrt(this.x * this.x + this.y * this.y);
};

Vector.prototype.setLength = function(length) {
	var current = this.length();
	if (current) {
		var scale = length / current;
		this.x *= scale;
		this.y *= scale;
	}
	return this;
};

var Mercator = function(map, canvas, field_opt) {
	this.canvas = canvas;
	this.alpha = 0.0;
	this.beta = 0.0;
	this.gamma = 0.0;
	this.delta = 0.0;
	this.map = map;
	if (field_opt) {
		this.fieldNW = new google.maps.LatLng(field_opt.y1, field_opt.x0);
		this.fieldSE = new google.maps.LatLng(field_opt.y0, field_opt.x1);
	}
	this.calibrate();
};

Mercator.prototype.calibrate = function() {

	var pointSW;
	var pointNE;
	
	if (this.fieldNW === undefined) {

		var canvasWidth = this.map.getDiv().clientWidth;
		var canvasHeight = this.map.getDiv().clientHeight;

		this.canvas.style.width = canvasWidth + "px";
		this.canvas.style.height = canvasHeight + "px";
		this.canvas.width = canvasWidth;
		this.canvas.height = canvasHeight;

		var sw = swarmCanvasProjection.fromLatLngToDivPixel(this.map.getBounds()
				.getSouthWest());
		var ne = swarmCanvasProjection.fromLatLngToDivPixel(this.map.getBounds()
				.getNorthEast());

		this.canvas.style.position = "absolute";
		this.canvas.style.left = sw.x + "px";
		this.canvas.style.top = ne.y + "px";

		var mapSW = this.map.getBounds().getSouthWest();
		var mapNE = this.map.getBounds().getNorthEast();
		
		pointSW = this.sphere2plane(mapSW.lat(), mapSW.lng());
		pointNE = this.sphere2plane(mapNE.lat(), mapNE.lng());

	} else {

		var fieldNWpx = swarmCanvasProjection.fromLatLngToDivPixel(this.fieldNW);
		var fieldSEpx = swarmCanvasProjection.fromLatLngToDivPixel(this.fieldSE);

		this.canvas.style.left = fieldNWpx.x+"px";
		this.canvas.style.top = fieldNWpx.y+"px";
		var canvasWidth = fieldSEpx.x - fieldNWpx.x;
		var canvasHeight = fieldSEpx.y - fieldNWpx.y;
		this.canvas.style.width = canvasWidth + "px";
		this.canvas.style.height = canvasHeight + "px";
		this.canvas.width = canvasWidth;
		this.canvas.height = canvasHeight;

		pointSW = this.sphere2plane(this.fieldSE.lat(), this.fieldNW.lng());
		pointNE = this.sphere2plane(this.fieldNW.lat(), this.fieldSE.lng());

	}
		
	if (pointNE.x < pointSW.x) {
		pointSW.x -= 2*Math.PI;
	}
	
	this.alpha = this.canvas.height / (pointNE.y - pointSW.y);
	this.beta = pointSW.x;
	
	this.gamma = - this.alpha;
	this.delta = pointNE.y;	
}; 

Mercator.prototype.sphere2plane = function(lat, lng) {
	var p = lng*Math.PI/180.0;
	var latsin = Math.sin(lat*Math.PI/180.0);
	var q = 0.5*Math.log((1.0+latsin)/(1.0-latsin));
	return {x:p, y:q};
};

Mercator.prototype.plane2sphere = function(x,y) {
	var lng = x*180.0/Math.PI;
	var lat = Math.atan(Math.sinh(y))*180.0/Math.PI;
	return {lat:lat, lng:lng};
};

Mercator.prototype.latlng2pixel = function(lat, lng) {
	var proj = this.sphere2plane(lat,lng);
	var x = this.alpha * (proj.x - this.beta);
	var y = this.gamma * (proj.y - this.delta);
	return {x:x, y:y};
};

Mercator.prototype.pixel2latlng = function(x, y) {
	var p = x / this.alpha + this.beta;
	var q = y / this.gamma + this.delta;
	return this.plane2sphere({x:p, y:q});
};

Mercator.prototype.clearCanvas = function() {
	var w = this.canvas.width;
	var h = this.canvas.height;
	var g = this.canvas.getContext('2d');
	g.clearRect(0, 0, w, h);
};

var VectorField = function(data) {

	this.data = data;
	this.step = 0;
	this.rcStart = data.rcStart;
	this.rcEnd = data.rcEnd;
	this.resY = data.resY;
	this.resX = data.resX;
	this.borderY = data.borderY;
	this.borderX = data.borderX;
	this.bdXi = (data.borderY + 0.5) / (this.resY - 1);
	this.bdPhi = 1.0 + 2*this.bdXi;
	this.bdA = { lat:this.rcEnd.lat+(this.rcEnd.lat-this.rcStart.lat)*this.bdXi, lng:this.rcEnd.lng+(this.rcEnd.lng-this.rcStart.lng)*this.bdXi };
	this.bdB = { lat:(this.rcStart.lat-this.rcEnd.lat)*this.bdPhi, lng:(this.rcStart.lng-this.rcEnd.lng)*this.bdPhi };
	this.xScale = data.xScale;
	this.x0 = data.boundsSW.lng;
	this.x1 = data.boundsNE.lng;
	this.y0 = data.boundsSW.lat;
	this.y1 = data.boundsNE.lat;
	this.visX0 = 0;
	this.visY0 = 0;
	this.visX1 = 0;
	this.visY1 = 0;
	this.maxLength = data.maxLength;
	this.numParticleFactor = 2.0;
	
	var latAvg = (this.rcEnd.lat + this.rcStart.lat) / 2.;
	this.lngScale = Math.cos(latAvg * Math.PI / 180.0);
	
	var difLat = this.rcEnd.lat - this.rcStart.lat;
	var difLng = (this.rcEnd.lng - this.rcStart.lng) * this.lngScale;
	var difLen = Math.sqrt(difLat*difLat + difLng*difLng);
	this.nvY = { lat: difLat/difLen/difLen*(this.resY-1), lng: difLng/difLen/difLen*(this.resY-1) };
	
    var nrmLat = -difLng/difLen;
    var nrmLng = difLat/difLen;
    this.nvX = { lat: nrmLat/this.xScale/difLen*(this.resX-1), lng: nrmLng/this.xScale/difLen*(this.resX-1) };
    this.gvX = { lat: nrmLat*this.xScale*difLen, lng: nrmLng/this.lngScale*this.xScale*difLen };
	this.bdC = { lat:this.gvX.lat*(this.resX+2*this.borderX-1)/(this.resX-1), lng:this.gvX.lng*(this.resX+2*this.borderX-1)/(this.resX-1)};    
};

VectorField.prototype.getRandomPosition = function() {
	var rndY = Math.random();
	var rndX = Math.random() - 0.5;
	var latP = this.bdA.lat + rndY * this.bdB.lat + rndX * this.bdC.lat;
	var lngP = this.bdA.lng + rndY * this.bdB.lng + rndX * this.bdC.lng;
	
	if (swarmDebug&&(!this.inBounds(lngP, latP))) {
		console.log("random-position: out of bounds");
	}
	
	return {lat:latP, lng:lngP};
};


VectorField.prototype.inBounds = function(lng, lat) {
	var idx = this.getIndex(lat, lng);
	var inBool = (idx.x >= 0) && (idx.x < (this.resX+2*this.borderX)) && (idx.y >= 0) && (idx.y < (this.resY+2*this.borderY));
	return inBool;
};

VectorField.prototype.interpolate = function(lat, lng) {

	var idx = this.getNeighbors(lat, lng);
	
	if (swarmDebug&&((idx.xTop >= (this.resX+2*this.borderX))||(idx.yTop >= (this.resY+2*this.borderY)))) {
		console.log("interpolate: out of range: " + idx.xTop + "  " + idx.yTop);
	}
	
	var avgX = this.data.data[this.step][idx.yBot][2*idx.xBot] * (1 - idx.yMod) * (1 - idx.xMod) + this.data.data[this.step][idx.yTop][2*idx.xBot] * idx.yMod * (1 - idx.xMod)
				+ this.data.data[this.step][idx.yBot][2*idx.xTop] * (1 - idx.yMod) * idx.xMod + this.data.data[this.step][idx.yTop][2*idx.xTop] * idx.yMod * idx.xMod;
	var avgY = this.data.data[this.step][idx.yBot][2*idx.xBot+1] * (1 - idx.yMod) * (1 - idx.xMod) + this.data.data[this.step][idx.yTop][2*idx.xBot+1] * idx.yMod * (1 - idx.xMod)
				+ this.data.data[this.step][idx.yBot][2*idx.xTop+1] * (1 - idx.yMod) * idx.xMod + this.data.data[this.step][idx.yTop][2*idx.xTop+1] * idx.yMod * idx.xMod;
	
	return {x:avgX / this.lngScale, y:avgY};	
};

VectorField.prototype.interpolate2 = function(lat, lng) {

	var idx = this.getNeighbors(lat, lng);
	
	if (swarmDebug&&((idx.xTop >= (this.resX+2*this.borderX))||(idx.yTop >= (this.resY+2*this.borderY)))) {
		console.log("interpolate: out of range: " + idx.xTop + "  " + idx.yTop);
	}
	
	var idxBase = this.step*this.resX*this.resY;
	var yBotxBot = getWindfromSimulator(idxBase + idx.yBot*this.resY + idx.xBot);
	var yTopxBot = getWindfromSimulator(idxBase + idx.yTop*this.resY + idx.xBot);
	var yBotxTop = getWindfromSimulator(idxBase + idx.yBot*this.resY + idx.xTop);
	var yTopxTop = getWindfromSimulator(idxBase + idx.yTop*this.resY + idx.xTop);
	
	var avgX = yBotxBot.x * (1 - idx.yMod) * (1 - idx.xMod) + yTopxBot.x * idx.yMod * (1 - idx.xMod) + yBotxTop.x * (1 - idx.yMod) * idx.xMod + yTopxTop.x * idx.yMod * idx.xMod;
	var avgY = yBotxBot.y * (1 - idx.yMod) * (1 - idx.xMod) + yTopxBot.y * idx.yMod * (1 - idx.xMod) + yBotxTop.y * (1 - idx.yMod) * idx.xMod + yTopxTop.y * idx.yMod * idx.xMod;

	return {x:avgX / this.lngScale, y:avgY};
};

VectorField.prototype.setStep = function(step) {
	if (step < 0) {
		this.step = 0;
	} else if (step >= this.data.data.length) {
		this.step = this.data.data.length-1;
	} else {
		this.step = step;
	}
};

VectorField.prototype.nextStep = function() {
	if (this.step < (this.data.data.length-1)) {
		this.step++;
	}
};

VectorField.prototype.prevStep = function() {
	if (this.step > 0) {
		this.step--;
	}
};

VectorField.prototype.getValue = function(lat, lng, opt_result) {

	var v = this.interpolate(lat, lng);
	if (opt_result) {
		opt_result.x = v.x;
		opt_result.y = v.y;
		return opt_result;
	}
	return new Vector(v.x, v.y);
};

VectorField.prototype.getValue2 = function(lat, lng, opt_result) {

	var v = this.interpolate(lat, lng); 
	if (opt_result) {
		opt_result.x = v.x;
		opt_result.y = v.y;
		return opt_result;
	}
	return new Vector(v.x, v.y);
};

VectorField.prototype.getIndex = function(lat, lng) {
	
	// calculate grid indexes
	var posR = { lat: lat - this.rcStart.lat, lng: (lng - this.rcStart.lng) * this.lngScale };

	// closest grid point
	var yIdx = Math.round( posR.lat * this.nvY.lat + posR.lng * this.nvY.lng ) + this.borderY;
	var xIdx = Math.round( posR.lat * this.nvX.lat + posR.lng * this.nvX.lng + (this.resX - 1) / 2. ) + this.borderX;

	return { x: xIdx, y: yIdx };
};

VectorField.prototype.getNeighbors = function(lat, lng) {
	
	// calculate grid indexes
	var posR = { lat: lat - this.rcStart.lat, lng: (lng - this.rcStart.lng) * this.lngScale };
	
	// surrounding grid points
	var yFlt = posR.lat * this.nvY.lat + posR.lng * this.nvY.lng + this.borderY;
	var xFlt = posR.lat * this.nvX.lat + posR.lng * this.nvX.lng + (this.resX - 1) / 2. + this.borderX;
	var yBot = Math.floor( yFlt );
	var xBot = Math.floor( xFlt );
	var yTop = Math.ceil( yFlt );
	var xTop = Math.ceil( xFlt );
	var yMod = yFlt - yBot;
	var xMod = xFlt - xBot;
	
	if (xBot < 0) {
		xBot = 0;
	}

	if (yBot < 0) {
		yBot = 0;
	}

	if (xTop >= (this.resX+2*this.borderX)) {
		xTop = this.resX+2*this.borderX-1;
	}

	if (yTop >= (this.resY+2*this.borderY)) {
		yTop = this.resY+2*this.borderY-1;
	}

	return { xTop: xTop, yTop:yTop, xBot:xBot, yBot:yBot, xMod:xMod, yMod:yMod };	
};

VectorField.prototype.motionScale = function(zoomLevel) {
	return 0.08 * Math.pow(1.6, Math.min(1.0, 6.0 - zoomLevel));
};

VectorField.prototype.particleWeight = function(p,v) {
	return v.length() / this.maxLength + 0.1;	
};

VectorField.prototype.getColors = function() {
	var colors = [];
	var alpha = 0.7;
	var greyValue = 255;
	for (var i = 0; i < 256; i++) {
		colors[i] = 'rgba(' + (greyValue) + ',' + (greyValue) + ',' + (greyValue) + ',' + (alpha*i/255.0) + ')';
		//this.colors[i] = 'hsla(' + 360*(0.55+0.9*(0.5-i/255)) + ',' + (100) + '% ,' + (50) + '%,' + (i/255) + ')';
	}
	return colors;
};

VectorField.prototype.lineWidth = function(s) {
	return 1.0;
};

/**
 * for compatibility with vector fields aligned with lat/lng-grid
 **/
var RectField = function(field, x0, y0, x1, y1) {
	this.x0 = x0;
	this.x1 = x1;
	this.y0 = y0;
	this.y1 = y1;

	this.visX0 = 0;
	this.visY0 = 0;
	this.visX1 = 0;
	this.visY1 = 0;
	
	this.field = field;
	this.w = field.length;
	this.h = field[0].length;
	this.maxLength = 0;
	this.numParticleFactor = 4.5;
	var mx = 0;
	var my = 0;
	for (var i = 0; i < this.w; i++) {
		for (var j = 0; j < this.h; j++) {
			if (field[i][j].length() > this.maxLength) {
				mx = i;
				my = j;
			}
			this.maxLength = Math.max(this.maxLength, field[i][j].length());
		}
	}
	mx = (mx / this.w) * (x1 - x0) + x0;
	my = (my / this.h) * (y1 - y0) + y0;
};

RectField.read = function(data, correctForSphere) {
	var field = [];
	var w = data.gridWidth;
	var h = data.gridHeight;

	var i = 0;
	for (var x = 0; x < w; x++) {
		field[x] = [];
		for (var y = 0; y < h; y++) {
			var vx = data.field[i++];
			var vy = data.field[i++];
			var v = new Vector(vx, vy);
			if (correctForSphere) {
				var uy = y / (h - 1);
				var lat = data.y0 * (1 - uy) + data.y1 * uy;
				var m = Math.PI * lat / 180;
				var length = v.length();
				v.x /= Math.cos(m);
				v.setLength(length);
			}
			field[x][y] = v;
		}
	}
	var result = new RectField(field, data.x0, data.y0, data.x1, data.y1);
	return result;
};

RectField.prototype.getRandomPosition = function() {
	var rndY = Math.random();
	var rndX = Math.random();
	var y = rndY * this.visY0 + (1 - rndY) * this.visY1;
	var x = rndX * this.visX0 + (1 - rndX) * this.visX1;
	return {lat:y, lng:x};
};

RectField.prototype.inBounds = function(x, y) {
	return x >= this.x0 && x < this.x1 && y >= this.y0 && y < this.y1;
};

RectField.prototype.interpolate = function(a, b) {
	var na = Math.floor(a);
	var nb = Math.floor(b);
	var ma = Math.ceil(a);
	var mb = Math.ceil(b);
	var fa = a - na;
	var fb = b - nb;

	var avgX = this.field[na][nb]['x'] * (1 - fa) * (1 - fb) + this.field[ma][nb]['x'] * fa * (1 - fb) + this.field[na][mb]['x'] * (1 - fa) * fb + this.field[ma][mb]['x'] * fa * fb;
	var avgY = this.field[na][nb]['y'] * (1 - fa) * (1 - fb) + this.field[ma][nb]['y'] * fa * (1 - fb) + this.field[na][mb]['y'] * (1 - fa) * fb + this.field[ma][mb]['y'] * fa * fb;

	return {x:avgX, y:avgY};	
};


RectField.prototype.getValue = function(y, x, opt_result) {
	var a = (this.w - 1 - 1e-6) * (x - this.x0) / (this.x1 - this.x0);
	var b = (this.h - 1 - 1e-6) * (y - this.y0) / (this.y1 - this.y0);
	var v = this.interpolate(a, b);
	if (opt_result) {
		opt_result.x = v.x;
		opt_result.y = v.y;
		return opt_result;
	}
	return new Vector(v.x, v.y);
};


RectField.prototype.motionScale = function(zoomLevel) {
	return 0.9 * Math.pow(1.7, Math.min(1.0, 6.0 - zoomLevel));
};

RectField.prototype.particleWeight = function(p,v) {
	return 1.0 - v.length() / this.maxLength;	
};

RectField.prototype.getColors = function() {
	var colors = [];
	var alpha = 1.0;
	var greyValue = 255;
	for (var i = 0; i < 256; i++) {
		colors[i] = 'rgba(' + (greyValue) + ',' + (greyValue) + ',' + (greyValue) + ',' + (alpha*i/255.0) + ')';
		//this.colors[i] = 'hsla(' + 360*(0.55+0.9*(0.5-i/255)) + ',' + (100) + '% ,' + (50) + '%,' + (i/255) + ')';
	}
	return colors;
};

RectField.prototype.lineWidth = function(s) {
	return 1.0;
};

var Animator = function() {
	this.animating = true;
	this.state = 'animate';
	this.pause = 0;
	this.listeners = [];
	this.dx = 0;
	this.dy = 0;
	this.scale = 1;
	this.zoomProgress = 0;
	this.scaleTarget = 1;
	this.scaleStart = 1;
};


Animator.prototype.add = function(listener) {
	this.listeners.push(listener);
};


Animator.prototype.notify = function(message) {
	if (this.animFunc && !this.animFunc()) {
		return;
	}
	for (var i = 0; i < this.listeners.length; i++) {
		var listener = this.listeners[i];
		if (listener[message]) {
			listener[message].call(listener, this);
		}
	}
};

Animator.prototype.start = function(opt_millis) {
	var millis = opt_millis || 20;
	var self = this;
	function go() {
		var start = new Date();
		if (self.loop()) {
			return;
		}
		var time = new Date() - start;
		setTimeout(go, Math.max(10, millis - time));
	}
	go();
};


Animator.prototype.loop = function() {
	if (this.state == 'stop') {
		return true;
	}
	if (this.pause > 1) {
		this.pause--;
	} else if (this.pause == 1){
		this.state = 'animate';
		this.pause = 0;
	}
	if (this.state == 'animate') {
		this.notify('animate');
		return false;
	}
	if ((this.state == 'pause')&&(this.pause == 0)) {
		this.pause = 5;
	}
	return false;
};

var Particle = function(x, y, age) {
	this.x = x;
	this.y = y;
	this.oldX = -1;
	this.oldY = -1;
	this.age = age;
	this.rnd = Math.random();
};

var swarmOffScreen = false;

var Swarm = function(canvas, field, opt_projection) {
	this.canvas = canvas;
	this.projection = opt_projection || IDProjection;
	this.field = field;
	this.numParticles = 0;
	this.first = true;
	this.maxLength = field.maxLength;
	this.speedScale = 1;
	this.renderState = 'normal';
	
	this.boundsSW = new google.maps.LatLng(this.field.y0, this.field.x0);
	this.boundsNE = new google.maps.LatLng(this.field.y1, this.field.x1);
	
	this.updateBounds(null);
	
	this.makeNewParticles(null, true);
	this.rgb = '40, 40, 40';
	this.background = 'rgb(' + this.rgb + ')';
	this.colors = this.field.getColors();

	if (this.projection) {
		this.startOffsetX = this.projection.offsetX;
		this.startOffsetY = this.projection.offsetY;
		this.startScale = this.projection.scale;
	}
};


Swarm.prototype.updateBounds = function() {

	var mapNE = this.projection.map.getBounds().getNorthEast();
	var mapSW = this.projection.map.getBounds().getSouthWest();

	var fieldNE = new google.maps.LatLng(Math.max(this.field.y0, this.field.y1), Math.max(this.field.x0, this.field.x1));
	var fieldSW = new google.maps.LatLng(Math.min(this.field.y0, this.field.y1), Math.min(this.field.x0, this.field.x1));

	var visibleNE = this.isVisible(fieldNE);
	var visibleSW = this.isVisible(fieldSW);

	var useBoundsNorth = (visibleNE.y == 0);
	var useBoundsEast = (visibleNE.x == 0);
	var useBoundsSouth = (visibleSW.y == 0);
	var useBoundsWest = (visibleSW.x == 0);

	swarmOffScreen = (visibleNE.y > 0)||(visibleSW.y < 0)||(visibleNE.x < 0)||(visibleSW.x > 0);

	if (swarmOffScreen) {

		this.boundsNE = fieldNE;
		this.boundsSW = fieldSW;

	} else {

		if ((!useBoundsNorth)&&(!useBoundsEast)) {
			this.boundsNE = mapNE;
		} else if (!useBoundsNorth) {
			this.boundsNE = new google.maps.LatLng(mapNE.lat(), fieldNE.lng());
		} else if (!useBoundsEast) {
			this.boundsNE = new google.maps.LatLng(fieldNE.lat(), mapNE.lng());		
		} else {
			this.boundsNE = fieldNE;
		}

		if ((!useBoundsSouth)&&(!useBoundsWest)) {
			this.boundsSW = mapSW;
		} else if (!useBoundsSouth) {
			this.boundsSW = new google.maps.LatLng(mapSW.lat(), fieldSW.lng());
		} else if (!useBoundsWest) {
			this.boundsSW = new google.maps.LatLng(fieldSW.lat(), mapSW.lng());		
		} else {
			this.boundsSW = fieldSW;
		}

	}

	this.field.visX0 = this.boundsSW.lng();
	this.field.visY0 = this.boundsSW.lat();
	this.field.visX1 = this.boundsNE.lng();
	this.field.visY1 = this.boundsNE.lat();

	var boundsSWpx = this.projection.latlng2pixel(this.boundsSW.lat(), this.boundsSW.lng());
	var boundsNEpx = this.projection.latlng2pixel(this.boundsNE.lat(), this.boundsNE.lng());

	var boundsWidthpx = Math.abs(boundsNEpx.x - boundsSWpx.x);
	var boundsHeightpx = Math.abs(boundsSWpx.y - boundsNEpx.y);

	this.numParticles = Math.round(Math.sqrt(boundsWidthpx * boundsHeightpx) * this.field.numParticleFactor);
	//this.numParticles = Math.sqrt(boundsWidthpx*boundsWidthpx + boundsHeightpx*boundsHeightpx) * this.field.numParticleFactor;
	if (swarmDebug) {
		console.log("numParticles: "+this.numParticles + " at " + (boundsWidthpx) +"x" + (boundsHeightpx) + "px  (" + (boundsWidthpx * boundsHeightpx) + " pixels)");
	}
};

Swarm.prototype.isVisible = function(pos) {

	// test for visibility of swarm
	var proj = this.projection.latlng2pixel(pos.lat(), pos.lng());

	var xVisible = -(proj.x < 0) + (proj.x > this.canvas.width);
	var yVisible = -(proj.y < 0) + (proj.y > this.canvas.height);

	return {x:xVisible, y:yVisible};
};

Swarm.prototype.makeNewParticles = function() {
	this.particles = [];
	for (var i = 0; i < this.numParticles; i++) {
		particle = this.makeParticle();
		if (particle) {
			this.particles.push(particle);
		} else {
			break;
		}
	}
};

Swarm.prototype.makeParticle = function() {
	var trials = 0;
	while (true) {
		var pos = this.field.getRandomPosition();
		var x = pos.lng;
		var y = pos.lat;
		var v = this.field.getValue(y, x);
		if (this.field.maxLength == 0) {
			return new Particle(x, y, 1 + 40 * Math.random());
		}
		var weight = this.field.particleWeight(pos, v);
		if (weight >= Math.random()) {
			var proj = this.projection.latlng2pixel(y, x);
			if (++trials > 10 || !(proj.x < 0 || proj.y < 0 || proj.x > this.canvas.width || proj.y > this.canvas.height)) {
				if (v.x || v.y) {
					return new Particle(x, y, 1 + 40 * Math.random());
				} else {
					return new Particle(x, y, 0); // "zombie" particles to avoid dead-lock when Google Maps is moved so that only zero-wind-area is visible
				}
			}
		}
	}
};


Swarm.prototype.endMove  = function() {
	this.updateBounds();
	this.makeNewParticles();
};


Swarm.prototype.animate = function() {
	if (!swarmOffScreen) {
		this.moveThings();
		this.draw();
	}
};


Swarm.prototype.moveThings = function() {
	var speed = .01 * this.speedScale * this.field.motionScale(this.projection.map.getZoom());
	for (var i = 0; i < this.particles.length; i++) {
		var p = this.particles[i];
		if (p.age > 0 && this.field.inBounds(p.x, p.y)) {
			var a = this.field.getValue(p.y, p.x);
			p.x += speed * a.x;
			p.y += speed * a.y;
			p.age--;
		} else {
			this.particles[i] = this.makeParticle();
		}
	}
};


Swarm.prototype.draw = function() {
	var g = this.canvas.getContext('2d');
	var w = this.canvas.width;
	var h = this.canvas.height;

	g.globalCompositeOperation = "destination-out";
	g.globalAlpha = 0.08; 
	g.fillStyle =  this.background;

	g.fillRect(0, 0, w, h);

	g.globalCompositeOperation = "source-over";
	g.globalAlpha = 1.0;

	var proj; // = new Vector(0, 0);
	var val = new Vector(0, 0);
	for (var i = 0; i < this.particles.length; i++) {
		var p = this.particles[i];
		if (!this.field.inBounds(p.x, p.y)) {
			p.age = -2;
			continue;
		}
		proj = this.projection.latlng2pixel(p.y, p.x);
		if (proj.x < 0 || proj.y < 0 || proj.x > w || proj.y > h) {
			p.age = -2;
		}
		if (p.oldX != -1) {
			var wind = this.field.getValue(p.y, p.x, val);
			var s = wind.length() / this.maxLength;
			var c = 90 + Math.round(350 * s); // was 400
			if (c > 255) {
				c = 255;
			} 
			g.globalAlpha = 1.0;
			g.lineWidth = this.field.lineWidth(s);
			g.strokeStyle = this.colors[c];
			g.beginPath();
			g.moveTo(proj.x, proj.y);
			g.lineTo(p.oldX, p.oldY);
			g.stroke();
			g.globalAlpha = 1.0;
		}
		p.oldX = proj.x;
		p.oldY = proj.y;
	}
};

var swarmData;
var swarmField;
var swarmAnimator;
var swarmProjection;
var swarmDebug = false;

function initStreamlets(map, canvas) {

	if (!map) {
		alert("Google Maps Instance required.");
		return;
	}
	if (!canvas) {
		canvas = document.getElementById('swarm-display');
		if (!canvas) {
			alert("canvas 'swarm-display' not available");
		}
	}

	if ("windFieldSim" in window) {
		
		swarmField = new VectorField(windFieldSim);
	
	} else {
	
		if ("swarmDataExt" in window) {
			swarmData = swarmDataExt;

			map.setZoom(5.0);
			var center = new google.maps.LatLng((swarmData.y0+swarmData.y1)/2., (swarmData.x0+swarmData.x1)/2.);
			map.panTo(center);

		};	

		swarmField = RectField.read(swarmData, false);

	};

	swarmProjection = new Mercator(map, canvas);

	var swarm = new Swarm(canvas, swarmField, swarmProjection);

	swarmAnimator = new Animator();
	swarmAnimator.add(swarm);

	updateStreamlets = function(upd_data) {
		if (!(upd_data === undefined)) {
			swarmAnimator.state = 'stop';
			swarmAnimator = new Animator();
			swarmField = new VectorField(windFieldSim);
			swarm = new Swarm(canvas, swarmField, swarmProjection);
			swarmAnimator.add(swarm);	
			swarmAnimator.start(40);
		} else if (swarmAnimator.state == 'stop') {
			return;
		}
		swarmAnimator.state = 'pause';
		swarmProjection.clearCanvas();
		swarmProjection.calibrate();
		swarmAnimator.notify('endMove');
	};
	
	stopStreamlets = function() {
		swarmAnimator.state = 'stop';
		swarmProjection.clearCanvas();
	};
	
	if (swarmProjection.fieldNW === undefined) {
		google.maps.event.addListener(map, 'bounds_changed', updateStreamlets);
	} else {
		google.maps.event.addListener(map, 'zoom_changed', updateStreamlets);
	}

	swarmAnimator.start(40);
}
