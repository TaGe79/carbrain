$(document).ready(function() {
//    $.ajax({
//        url: "http://rest-service.guides.spring.io/greeting"
//    }).then(function(data) {
//       $('.greeting-id').append(data.id);
//       $('.greeting-content').append(data.content);
//    });

    window.addEventListener("keydown", keyDown, false);
    window.addEventListener("keyup", keyUp, false);
});

var activeCommands = [];
var currentSpeed = 0;

var commandMap = {
    keycodes: [38,40,37,39,        // forward,backward,left,right
                96,                // stop
                76,                // light switch
                65,68,83,87],      // camera pan/tilt commands
    commands: [
        {
            command:'forward',
            incremental:true,
            onActive: function() {
               console.log('Call the forward REST-Api');
            },
            onIncrement: function() {
               console.log('Call the speed set REST-Api with increasing speed');
            },
            onDecrement: function() {
               console.log('Call the speed set REST-Api with decreasing speed');
            },
            onReverse: null},
        {
            command:'backward',
            incremental:true,
            onActive: function() {
               console.log('Call the backward REST-Api (and set the speed to a minimum)');
            },
            onIncrement: function() {
               console.log('Call the speed set REST-Api with increasing speed');
            },
            onDecrement: function() {
               console.log('Call the speed set REST-Api with decreasing speed');
            },
            onReverse: null},
        {
            command:'left',
            incremental:false,
            onActive: function() {
                console.log('Call the left-turn REST-Api');
            },
            onIncrement: null,
            onDecrement: null,
            onReverse: function() {
                console.log('Call the straight REST-Api');
            }},
        {
            command:'right',
            incremental:false,
             onActive: function() {
                 console.log('Call the left-turn REST-Api');
             },
             onIncrement: null,
             onDecrement: null,
             onReverse: function() {
                 console.log('Call the straight REST-Api');
        }},
        {
            command:'stop',     // NUM-PAD 0
            incremental:false,
             onActive: function() {
                 console.log('Call the stop REST-Api');
             },
             onIncrement: null,
             onDecrement: null,
             onReverse: function() {    // no reverse action necessary
                 console.log('do nothing');
        }},
        {
            command:'switchLight',     // 'l'
            incremental:false,
             onActive: function() {
                 console.log('Call the light on/off REST-Api according to current light state');
             },
             onIncrement: null,
             onDecrement: null,
             onReverse: function() {    // no reverse action necessary
                 console.log('do nothing');
        }},
        {
            command:'camPanLeft',
            incremental:false,
            onActive: function() {
                console.log('Call the camera-left-pan REST-Api');
            },
            onIncrement: null,
            onDecrement: null,
            onReverse: function() {     // no reverse action necessary
                console.log('do nothing');
        }},
        {
             command:'camPanRight',
             incremental:false,
             onActive: function() {
                 console.log('Call the camera-right-pan REST-Api');
             },
             onIncrement: null,
             onDecrement: null,
             onReverse: function() {     // no reverse action necessary
                 console.log('do nothing');
        }},
        {
            command:'camTiltDown',
            incremental:false,
            onActive: function() {
                console.log('Call the camera-down-tilt REST-Api');
            },
            onIncrement: null,
            onDecrement: null,
            onReverse: function() {     // no reverse action necessary
                console.log('do nothing');
        }},
        {
            command:'camTiltUp',
            incremental:false,
            onActive: function() {
                console.log('Call the camera-up-tilt REST-Api');
            },
            onIncrement: null,
            onDecrement: null,
            onReverse: function() {     // no reverse action necessary
                console.log('do nothing');
        }}]
    }

function keyDown(e) {
    // send the command only once
    var currentKeyCodeIdx = commandMap.keycodes.indexOf(e.keyCode);
    if ( currentKeyCodeIdx === -1) {
        console.log("Unknown command: "+e.keyCode);
        return;
    }

    var currentCommand = commandMap.commands[currentKeyCodeIdx];

    var activeCmdIdx = activeCommands.indexOf(currentCommand);

    if ( activeCmdIdx > -1 && !currentCommand.incremental) {
        console.log('not incremental command! Break and wait for releasing the key!')
        return;
    }

    lastKeyDown = e.keyCode
    console.log('Key down: '+e.keyCode);
    $('#carCommand').text(currentCommand.command);


    if ( activeCmdIdx == -1 ) {
        currentCommand.onActive();
        activeCommands.push(currentCommand);
    } else {
        currentCommand.onIncrement();
    }
}


function keyUp(e) {
    var currentKeyCodeIdx = commandMap.keycodes.indexOf(e.keyCode);
    if ( currentKeyCodeIdx === -1) {
        console.log("Unknown command: "+e.keyCode);
        return;
    }

    var currentCommand = commandMap.commands[currentKeyCodeIdx];

    var activeCommandIdx = activeCommands.indexOf(currentCommand);
    activeCommands.splice(activeCommandIdx, 1);

    console.log('Key up: '+e.keyCode+'  '+e.charCode);

    if (currentCommand.incremental == true ) {
        currentCommand.onDecrement();
    } else {
        currentCommand.onReverse();
    }
    // TODO Stop the car!

    // Clear command indicator
    $('#carCommand').text('');
}
