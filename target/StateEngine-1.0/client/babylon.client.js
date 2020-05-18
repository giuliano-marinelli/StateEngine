var canvas = document.getElementById("renderCanvas"); // Get the canvas element 
var engine = new BABYLON.Engine(canvas, true);

var socket;
var socketID = "";

var players = [];

window.onload = function () {
    
    //crea la conexion con WebSocket
    var page = document.createElement('a');
    page.href = window.location.href;
    
    //define la url del servidor como la hostname de la pagina y el puerto definido 8080 del ws
    var url = "ws://" + page.hostname + ":8080";
    socket = new WebSocket(url + "/StateEngine/GameWebSocket");
    socket.onmessage = stateUpdate;

    //actualiza la vista del juego cuando recive un nuevo estado desde el servidor
    function stateUpdate(event) {
        //console.log(socket);
        console.log(event.data);
        var gameState = JSON.parse(event.data);
        console.log(gameState);

        var i = 0;
        while (typeof gameState[i] !== "undefined") {
            if (typeof gameState[i]["Remove"] !== "undefined") {
                var id = gameState[i]["Remove"]["id"];
                if (players[id] != null) {
                    players[id].dispose();
                    players[id] = null;
                }
            } else if (typeof gameState[i]["Player"] !== "undefined") {
                var id = gameState[i]["Player"]["super"]["Entity"]["super"]["State"]["id"];
                var playerId = gameState[i]["Player"]["id"];
                var destroy = gameState[i]["Player"]["super"]["Entity"]["super"]["State"]["destroy"];
                var leave = gameState[i]["Player"]["leave"];
                var x = gameState[i]["Player"]["super"]["Entity"]["x"];
                var y = gameState[i]["Player"]["super"]["Entity"]["y"];
                // Create a sphere that we will be moved by the keyboard
                if (players[id] == null) {
                    players[id] = BABYLON.Mesh.CreateSphere(id, 16, 1, scene);
                }
                players[id].position.y = 1;
                players[id].position.x = x;
                players[id].position.z = y;

                if (leave) {
                    players[id].dispose();
                }
                if (destroy) {
                    players[id].dispose();
                    players[id] = null;
                }
            }
            i++;
        }
    }
}

/******* Add the create scene function ******/
var createScene = function () {
    // Setup the scene
    var scene = new BABYLON.Scene(engine);
    var camera = new BABYLON.FreeCamera("camera1", new BABYLON.Vector3(0, 25, 0), scene);
    camera.setTarget(BABYLON.Vector3.Zero());
    var light = new BABYLON.HemisphericLight("light1", new BABYLON.Vector3(0, 1, 0), scene);

    // Keyboard events
    var inputMap = {};
    scene.actionManager = new BABYLON.ActionManager(scene);
    scene.actionManager.registerAction(new BABYLON.ExecuteCodeAction(BABYLON.ActionManager.OnKeyDownTrigger, function (evt) {
        inputMap[evt.sourceEvent.key] = evt.sourceEvent.type == "keydown";
    }));
    scene.actionManager.registerAction(new BABYLON.ExecuteCodeAction(BABYLON.ActionManager.OnKeyUpTrigger, function (evt) {
        inputMap[evt.sourceEvent.key] = evt.sourceEvent.type == "keydown";
    }));

    // Game/Render loop
    scene.onBeforeRenderObservable.add(() => {
        if (inputMap["w"] || inputMap["ArrowUp"]) {
            socket.send("up");
            //sphere.position.z -= 0.1
        }
        if (inputMap["a"] || inputMap["ArrowLeft"]) {
            socket.send("left");
            //sphere.position.x += 0.1
        }
        if (inputMap["s"] || inputMap["ArrowDown"]) {
            socket.send("down");
            //sphere.position.z += 0.1
        }
        if (inputMap["d"] || inputMap["ArrowRight"]) {
            socket.send("right");
            //sphere.position.x -= 0.1
        }
    })
    return scene;
};
/******* End of the create scene function ******/

var scene = createScene(); //Call the createScene function

// Register a render loop to repeatedly render the scene
engine.runRenderLoop(function () {
    scene.render();
});

// Watch for browser/canvas resize events
window.addEventListener("resize", function () {
    engine.resize();
});