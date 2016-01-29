/* Copyright (C) 2007 Richard Atterer, richardÂ©atterer.net
   This program is free software; you can redistribute it and/or modify it
   under the terms of the GNU General Public License, version 2. See the file
   COPYING for details. */

var imageNr = 0; // Serial number of current image
var finished = new Array(); // References to img objects which have finished downloading
var paused = false;
var serverImgAddress = window.location.href.split("/")[0] + "//" + window.location.href.split("/")[2].split(':')[0];

function createImageLayer() {
  var img = new Image();
  img.className = 'carStream';
  img.onload = imageOnload;
  img.onclick = imageOnclick;
  img.src = serverImgAddress+":8080/?action=snapshot&n=" + (++imageNr);

  var webcam = $(document.getElementById("frontView"));
  webcam.append(img);
}

// Two layers are always present (except at the very beginning), to avoid flicker
function imageOnload() {
  this.style.zIndex = imageNr; // Image finished, bring to front!
  while (1 < finished.length) {
    var del = finished.shift(); // Delete old image(s) from document
    del.parentNode.removeChild(del);
  }
  finished.push(this);
  if (!paused) createImageLayer();
}

function imageOnclick() { // Clicking on the image will pause the stream
  paused = !paused;
  if (!paused) createImageLayer();
}

$(document).ready(function(){
  createImageLayer();
});
