var serverAddress = window.location.href.split("/")[0] + "//" + window.location.href.split("/")[2];
var servoOffset;

$(document).ready(function () {
  $.get(serverAddress + "/car/calibration", function(data,status) {
    servoOffset = Number.parseInt(data);
    $("#offset_value").text(data);
  });

  window.addEventListener("keydown", keyDown, false);
  window.addEventListener("keyup", keyUp, false);

  $('#offset_add').mouseup(function() {
    servoOffset += 5;
    $.ajax({
      url:'http://192.168.0.15:8090/car/calibration',
      method: 'POST',
      contentType: 'application/json',
      data: JSON.stringify(servoOffset)}).done(function(data) {
        $("#offset_value").text(data);
      });
  });

  $('#offset_sub').mouseup(function() {
    servoOffset -= 5;
    $.ajax({
      url:'http://192.168.0.15:8090/car/calibration',
      method: 'POST',
      contentType: 'application/json',
      data: JSON.stringify(servoOffset)}).done(function(data) {
        $("#offset_value").text(data);
      });
  });

  setInterval(function() {
    $.get(serverAddress + "/car/front/collision/state", function(data) {
      $("#front_collision_warning").removeClass(data === true ? 'hide' : 'show');
      $("#front_collision_warning").addClass(data === true ? 'show' : 'hide');
    });
    $.get(serverAddress + "/car/front/obstacle/distance", function(data) {
      $('#front_obstacle_distance_value').text(data+" mm");
    });
  }, 1000);

  $('#turn_left').mousedown(function () {
    keyDown({keyCode: 37});
  });
  $('#turn_right').mousedown(function () {
    keyDown({keyCode: 39});
  });
  $('#accelerate').mousedown(function () {
    keyDown({keyCode: 38});
  });
  $('#decelerate').mousedown(function () {
    keyDown({keyCode: 40});
  });
  $('#stop').mousedown(function () {
    keyDown({keyCode: 96});
  });
  $('#front_light').mousedown(function () {
    keyDown({keyCode: 76});
  });

  $('#front_light').mouseup(function () {
    keyUp({keyCode: 76});
  });
  $('#turn_left').mouseup(function () {
    keyUp({keyCode: 37});
  });
  $('#turn_right').mouseup(function () {
    keyUp({keyCode: 39});
  });
  $('#accelerate').mouseup(function () {
    keyUp({keyCode: 38});
  });
  $('#decelerate').mouseup(function () {
    keyUp({keyCode: 40});
  });
  $('#stop').mouseup(function () {
    keyUp({keyCode: 96});
  });

});

function sendCommand(command) {
  $.get(serverAddress + "/car/" + command, function (data, status) {
    console.log('Result(' + status + ') ' + data);
  });
}

function setSpeed(speed) {
  $.ajax({
    url: serverAddress + "/car/speed/" + speed,
    type: "GET",
    success: function (data, status) {
      console.log('Result(' + status + ') ' + data);
    },
    error: function (jqXHR, textStatus) {
      console.log('Error in post req: ' + textStatus);
    }
  });
}

const __MAX_SPEED = 100;
const __MIN_SPEED = 50;

var activeCommands = [];
var currentSpeed = 70;
var timer = null;
var frontLight = false;

var stopCommand = {
  command: 'stop',     // NUM-PAD 0
  incremental: false,
  onActive: function () {
    console.log('Call the stop REST-Api');
    if (timer !== null) {
      clearInterval(timer);
      timer = null;
    }
    sendCommand("stop");
  },
  onIncrement: null,
  onDecrement: null,
  onReverse: function () {    // no reverse action necessary
    console.log('do nothing');
  }
};

var commandMap = {
  keycodes: [38, 40, 37, 39,        // forward,backward,left,right
    96, 48,                         // stop
    76,                             // light switch
    65, 68, 83, 87],                // camera pan/tilt commands
  commands: [
    {
      command: 'forward',
      incremental: false,
      onActive: function () {
        console.log('Call the forward REST-Api');
        // If the car is going backward stop it
//        if (currentSpeed < 0) {
//          sendCommand("stop");
//          currentSpeed = 10;
//          setSpeed(currentSpeed);
//        }
        setSpeed(Math.abs(currentSpeed));
        sendCommand("go/forward");
      },
      onIncrement: function () {
        console.log('Call the speed set REST-Api with increasing speed');
        if (timer !== null) {
          clearInterval(timer);
          timer = null;
        }
        timer = setInterval(function () {
          currentSpeed = Math.min(currentSpeed + 10, __MAX_SPEED);
          setSpeed(currentSpeed);
          if (currentSpeed === __MAX_SPEED) {
            clearInterval(timer);
            timer = null;
          }
        }, 1000);
      },
      onDecrement: function () {
        console.log('Call the speed set REST-Api with decreasing speed');
        if (timer !== null) {
          clearInterval(timer);
        }
        timer = setInterval(function () {
          currentSpeed = Math.max(currentSpeed - 10, 0);
          setSpeed(currentSpeed);
          if (currentSpeed === 0) {
            clearInterval(timer);
            timer = null;
          }
        }, 1000);
      },
      onReverse: function() {sendCommand("stop");}
    },
    {
      command: 'backward',
      incremental: false,
      onActive: function () {
        console.log('Call the backward REST-Api (and set the speed to a minimum)');
        // If the car is going backward stop it
//        if (currentSpeed > 0) {
//          sendCommand("stop");
//          currentSpeed = -5;
//          setSpeed(Math.abs(currentSpeed));
//        }
        setSpeed(Math.abs(currentSpeed));
        sendCommand("go/backward");
      },
      onIncrement: function () {
        console.log('Call the speed set REST-Api with increasing speed');
        if (timer !== null) {
          clearInterval(timer);
          timer = null;
        }
        timer = setInterval(function () {
          currentSpeed = Math.max(currentSpeed - 5, __MIN_SPEED);
          setSpeed(Math.abs(currentSpeed));
          if (currentSpeed === __MAX_SPEED) {
            clearInterval(timer);
            timer = null;
          }
        }, 1000);
      },
      onDecrement: function () {
        console.log('Call the speed set REST-Api with decreasing speed');
        if (timer !== null) {
          clearInterval(timer);
        }
        timer = setInterval(function () {
          currentSpeed = Math.min(currentSpeed + 5, 0);
          setSpeed(Math.abs(currentSpeed));
          if (currentSpeed === 0) {
            clearInterval(timer);
            timer = null;
          }
        }, 1000);
      },
      onReverse: function() {sendCommand("stop");}
    },
    {
      command: 'left',
      incremental: false,
      onActive: function () {
        sendCommand("turn/left");
      },
      onIncrement: null,
      onDecrement: null,
      onReverse: function () {
        console.log('Call the straight REST-Api');
        sendCommand("turn/straight");
      }
    },
    {
      command: 'right',
      incremental: false,
      onActive: function () {
        console.log('Call the left-turn REST-Api');
        sendCommand("turn/right");
      },
      onIncrement: null,
      onDecrement: null,
      onReverse: function () {
        console.log('Call the straight REST-Api');
        sendCommand("turn/straight");
      }
    },
    stopCommand,
    stopCommand,
    {
      command: 'switchLight',     // 'l'
      incremental: false,
      onActive: function
        () {
        console.log('Call the light on/off REST-Api according to current light state');
        $('#front_light').removeClass((frontLight ? "on" : "off"));
        frontLight = !frontLight;
        sendCommand("front/light/" + (frontLight ? "on" : "off"));
        $('#front_light').addClass((frontLight ? "on" : "off"));
      }
      ,
      onIncrement: null,
      onDecrement: null,
      onReverse: function () {    // no reverse action necessary
        console.log('do nothing');
      }
    },
    {
      command: 'camPanLeft',
      incremental: false,
      onActive: function () {
        console.log('Call the camera-left-pan REST-Api');
      },
      onIncrement: null,
      onDecrement: null,
      onReverse: function () {     // no reverse action necessary
        console.log('do nothing');
      }
    },
    {
      command: 'camPanRight',
      incremental: false,
      onActive: function () {
        console.log('Call the camera-right-pan REST-Api');
      },
      onIncrement: null,
      onDecrement: null,
      onReverse: function () {     // no reverse action necessary
        console.log('do nothing');
      }
    },
    {
      command: 'camTiltDown',
      incremental: false,
      onActive: function () {
        console.log('Call the camera-down-tilt REST-Api');
      },
      onIncrement: null,
      onDecrement: null,
      onReverse: function () {     // no reverse action necessary
        console.log('do nothing');
      }
    },
    {
      command: 'camTiltUp',
      incremental: false,
      onActive: function () {
        console.log('Call the camera-up-tilt REST-Api');
      },
      onIncrement: null,
      onDecrement: null,
      onReverse: function () {     // no reverse action necessary
        console.log('do nothing');
      }
    }]
}

function keyDown(e) {
  console.log('bubu');
  // send the command only once
  var currentKeyCodeIdx = commandMap.keycodes.indexOf(e.keyCode);
  if (currentKeyCodeIdx === -1) {
    console.log("Unknown command: " + e.keyCode);
    return;
  }

  var currentCommand = commandMap.commands[currentKeyCodeIdx];

  var activeCmdIdx = activeCommands.indexOf(currentCommand);

  if (activeCmdIdx > -1) {
    console.log(' Break and wait for releasing the key!');
    return;
  }

  console.log('Key down: ' + e.keyCode);
  $('#carCommand').text(currentCommand.command);


  if (activeCmdIdx == -1) {
    currentCommand.onActive();
    activeCommands.push(currentCommand);
    if (currentCommand.incremental === true) {
      currentCommand.onIncrement();
    }
  }
}


function keyUp(e) {
  var currentKeyCodeIdx = commandMap.keycodes.indexOf(e.keyCode);
  if (currentKeyCodeIdx === -1) {
    console.log("Unknown command: " + e.keyCode);
    return;
  }

  var currentCommand = commandMap.commands[currentKeyCodeIdx];

  var activeCommandIdx = activeCommands.indexOf(currentCommand);
  activeCommands.splice(activeCommandIdx, 1);

  console.log('Key up: ' + e.keyCode);

  if (currentCommand.incremental == true) {
    currentCommand.onDecrement();
  } else {
    currentCommand.onReverse();
  }
  // TODO Stop the car!

  // Clear command indicator
  $('#carCommand').text('');
}
