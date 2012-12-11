function connectUsersOn(host) { // connect to the host websocket servlet
	var websocketUsersOn;
	if ('WebSocket' in window)
		websocketUsersOn = new WebSocket(host);
	else if ('MozWebSocket' in window)
		websocketUsersOn = new MozWebSocket(host);
	else {
		writeToHistory('Get a real browser which supports WebSocket.');
		return;
	}

	websocketUsersOn.onopen    = function(event){
									console.log("websocket opened");
								};
	websocketUsersOn.onclose   = function(event){
									console.log("websocket closed");
								};
	websocketUsersOn.onmessage = function(message){
									editUsersList(message.data);
								};
	websocketUsersOn.onerror   = function(event){
		console.log(event);
	};
}

function connectNotifications(host) { // connect to the host websocket servlet
	var websocketNotifications;
	if ('WebSocket' in window)
		websocketNotifications = new WebSocket(host);
	else if ('MozWebSocket' in window)
		websocketNotifications = new MozWebSocket(host);
	else {
		writeToHistory('Get a real browser which supports WebSocket.');
		return;
	}

	websocketNotifications.onopen    = function(event){
										console.log("websocket opened");
									};
	websocketNotifications.onclose   = function(event){
										console.log("websocket closed");
									};
	websocketNotifications.onmessage = function(message){
										alertNotification(message.data);
									};
	websocketNotifications.onerror   = function(event){
										console.log(event);
									};
}

var websocketChatroom;
function connectChatroom(host) { // connect to the host websocket servlet
    if ('WebSocket' in window)
        websocketChatroom = new WebSocket(host);
    else if ('MozWebSocket' in window)
        websocketChatroom = new MozWebSocket(host);
    else {
        writeToHistory('Get a real browser which supports WebSocket.');
        return;
    }

    websocketChatroom.onopen    = onOpen; // set the event listeners below
    websocketChatroom.onclose   = onClose;
    websocketChatroom.onmessage = onMessage;
    websocketChatroom.onerror   = onError;
}

function onOpen(event) {
    writeToHistory('Connected to ' + window.location.host + '.');
    document.getElementById('chat').onkeydown = function(key) {
        if (key.keyCode == 13)
            doSend(); // call doSend() on enter key 
    };
}

function onClose(event) {
    writeToHistory('WebSocket closed.');
    document.getElementById('chat').onkeydown = null;
}

function onMessage(message) { // print the received message
    writeToHistory(message.data);
}

function onError(event) {
    writeToHistory('WebSocket error (' + event.data + ').');
    document.getElementById('chat').onkeydown = null;
}

function doSend() {
    var message = document.getElementById('chat').value;
    document.getElementById('chat').value = '';
    if (message != '')
        websocketChatroom.send(message); // send the message
}

function writeToHistory(text) {
    var history = document.getElementById('history');
    var p = document.createElement('p');
    p.style.wordWrap = 'break-word';
    p.innerHTML = text;
    history.appendChild(p);
    while (history.childNodes.length > 25)
        history.removeChild(console.firstChild);
    history.scrollTop = history.scrollHeight;
}

function editUsersList(text) {
	console.log(text);
	var usersList = document.getElementById('users');
	var username = text.substring(1);
	if(text.charAt(0)=='>'){
		if( findUserUsersList(username)==null ){
			var p = document.createElement('p');
			p.innerHTML = username;
			usersList.appendChild(p);			
		}
	}
	else if(text.charAt(0)=='<'){
		var u = findUserUsersList(username);
		if( u!=null )
			usersList.removeChild( u );
	}
}

function findUserUsersList(username){
	var usersList = document.getElementById('users');
	for (var i = 0; i < usersList.children.length; i++) {
		if(usersList.children[i].innerHTML==username){
			return usersList.children[i];
		}
	}	
	return null;
}

function alertNotification(text) {
	alert(text)
}