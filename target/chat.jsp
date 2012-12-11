<jsp:include page="auth.jsp"></jsp:include>
<%@ page import="socnet1.RMIClient"%>
<% RMIClient c = (RMIClient) session.getAttribute("rmi"); %>
<% String chat_name = request.getParameter("name"); %>

<!DOCTYPE html>
<html>
<head>
	<title>WebSocket Chat</title>
	<link rel="stylesheet" type="text/css" href="css/style.css">
	<script type="text/javascript" src="websockets.js"></script>
	<script type="text/javascript">
	window.onload = function() { // execute once the page loads
		document.getElementById("chat").focus();
		var chatroom_name = document.getElementById("chat_name").value;
		var username = document.getElementById("username").value;
		if(chatroom_name!="null")
			connectChatroom('ws://' + window.location.host + '/socnetSD/chat?name='+chatroom_name+'&username='+username);
	}
	function displayChatroom(){
		var chatroom_list = document.getElementById('chatroom_list'); 
		var cr = chatroom_list.options[chatroom_list.selectedIndex].innerHTML;
		var username = document.getElementById("username").value;		  
		window.location='/socnetSD/chat.jsp?name='+cr+'&username='+username;
	}
	</script>
</head>
<body>
	<div class="sub_div">
		<input type="hidden" id="chat_name" value='<%=chat_name%>'>
		<input type="hidden" id="username" value='<%=c.username%>'>

		<noscript>JavaScript must be enabled for WebSockets to work.</noscript>

	  <div style="background-color:#000000;float:left;">
	  	<select id="chatroom_list" size="30" onchange="displayChatroom()" style="width:90px;">
	  		<% for( String cr : c.rmiServer.getChatrooms() ) { %>
	  		<option><%=cr%></option>
	  		<% } %>
	  	</select>
	  </div>

	  <% if(chat_name!=null) {%>
		  <div style="float:left;">
		  	<div id="container"> <div id="history"></div> </div>
		  	<input type="text" placeholder="type to chat" id="chat">
		  </div>
	  <%} else {%>
	  		<p><- Select a chatroom !</p>
	  <% } %>
	</div>
</div>
</body>
</html>

<!-- style="width:auto;color:white;" -->
