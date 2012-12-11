<jsp:include page="auth.jsp"></jsp:include>
<%@ page import="socnet1.RMIClient"%>
<%@ page import="socnet1.PM"%>
<%@ page import="java.util.ArrayList"%>

<%  RMIClient c = (RMIClient) session.getAttribute("rmi");
    ArrayList<String> usernames = c.rmiServer.getUserNames();
    usernames.remove(c.username);
    PM[] pms = c.rmiServer.getAllPMs(c.username);
    %>

<html>
  <head>	
    <link rel="stylesheet" type="text/css" href="css/style.css"/>
    <title></title>
    <script type="text/javascript">
      function outputPM(source, text, date){
      var div = document.createElement("div");
      div.innerHTML
      // div.style.border="1px solid 0xCCCCCC";
      div.innerHTML += "FROM "+source+"<br>"+
      "date: "+date+"<br>"+
      "<p>"+text+"</p>";

      document.getElementById('showPMs').appendChild(div);
      }
    </script>
  </head>
  <body>


    <h1>PMS</h1>
    
    <div id="sendPM" style="float:left;">
      Choose User:
      <form action="newPM" method="post">
	<select name="dest" style="float:left">
	  <% for(String user : usernames) {%>
	  <option value="<%=user%>"><%=user%></option>
	  <% } %>
	</select>
	
	<textarea name="text" placeholder="Write PM here"></textarea> <br>
	<input type="submit" value="Send">
      </form>
    </div>

    <div id="showPMs" style="float:left;">
      <!-- remover esta linha (meter num style.css a parte digo eu) -->
      <style type="text/css">
	div { border: 1px solid white; }
      </style>
      <% for( PM pm : pms ) { %>
      <script type="text/javascript"> outputPM( "<%=pm.getSource()%>", "<%=pm.getText()%>", "<%=pm.getSentDate()%>" ) </script>
      <% } %>
    </div>

  </body>
</html>
