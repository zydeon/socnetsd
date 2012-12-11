<jsp:include page="auth.jsp"></jsp:include>
<%@ page import="socnet1.RMIClient"%>
<% RMIClient c = (RMIClient) session.getAttribute("rmi"); %>

<jsp:include page="auth.jsp"></jsp:include>

<!DOCTYPE html>
<html lang="en">  
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
	<link rel="stylesheet" type="text/css" href="css/style.css"/>
	<script type="text/javascript" src="websockets.js"></script>
	<script type="text/javascript">
	  window.onload = function() { // execute once the page loads
	  	connectUsersOn('ws://' + window.location.host + '/socnetSD/usersOn');
	  	connectNotifications('ws://' + window.location.host + '/socnetSD/notifications');
	  }

	  function toTimeline(){
	  	document.getElementById('chatroom_frame').src = 'timeline.jsp';
	  }
	  function toPms(){
	  	document.getElementById('chatroom_frame').src = 'pms.jsp';
	  }
	  function toChatroom(){
	  	var username = document.getElementById("username").value;	
	  	document.getElementById('chatroom_frame').src = '/socnetSD/chat.jsp';
	  }	
	  </script>
	  <title>soc.net</title>
	</head>

	<body>
		<input type="hidden" id="username" value='<%=c.username%>'>


		<div class="main_div">
			<div class="title_div">
				<h1>soc.net</h1>
				<h2>Welcome, <%=c.username%> !</h2>
			</div>
			<hr>
			<div style="width:100%;">
				<div style="float:left;">
					<button onclick="toChatroom()">Chatrooms</button>
					<button onclick="toPms()">PM's</button>
					<button onclick="toTimeline()">Timeline</button>
				</div>
				<div style="float:right;">
					<form action="logout" method="get"><button>Logout</button></form>
				</div>
			</div>

		<div style="float:left;">
			<iframe width="800" height="500" id="chatroom_frame" src="timeline.jsp"></iframe>
		</div>

		<br>
		<div id="users" style="float:left;border: 1px solid #FFFFFF;">
			<h3>Online users</h3>
			<% for(String user : c.rmiServer.checkOnlineUsers() ) {%>
			<p><%=user%></p>
			<% } %>
		</div>		
	</div>
</body>
</html>

<!--<script type="text/javascript">-->
