$(document).ready(function () {
//    $.ajax({
//        url: "http://rest-service.guides.spring.io/greeting"
//    }).then(function(data) {
//       $('.greeting-id').append(data.id);
//       $('.greeting-content').append(data.content);
//    });

  window.addEventListener("keydown", keyDown, false);
  window.addEventListener("keyup", keyUp, false);

  $('#left').mousedown(function () {
    keyDown({keyCode: 37});
  });
  $('#right').mousedown(function () {
    keyDown({keyCode: 39});
  });
  $('#forward').mousedown(function () {
    keyDown({keyCode: 38});
  });
  $('#backward').mousedown(function () {
    keyDown({keyCode: 40});
  });

  $('#left').mouseup(function () {
    keyUp({keyCode: 37});
  });
  $('#right').mouseup(function () {
    keyUp({keyCode: 39});
  });
  $('#forward').mouseup(function () {
    keyUp({keyCode: 38});
  });
  $('#backward').mouseup(function () {
    keyUp({keyCode: 40});
  });

});

var serverAddress = window.location.href.split("/")[0] + "//" + window.location.href.split("/")[2];
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
var currentSpeed = 0;
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
      incremental: true,
      onActive: function () {
        console.log('Call the forward REST-Api');
        // If the car is going backward stop it
        if (currentSpeed < 0) {
          sendCommand("stop");
          currentSpeed = 10;
          setSpeed(currentSpeed);
        }
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
      onReverse: null
    },
    {
      command: 'backward',
      incremental: true,
      onActive: function () {
        console.log('Call the backward REST-Api (and set the speed to a minimum)');
        // If the car is going backward stop it
        if (currentSpeed > 0) {
          sendCommand("stop");
          currentSpeed = -5;
          setSpeed(Math.abs(currentSpeed));
        }
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
      onReverse: null
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
        frontLight = !frontLight;
        sendCommand("front/light/" + (frontLight ? "on" : "off"));
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
