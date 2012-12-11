<jsp:include page="auth.jsp"></jsp:include>
<%@ page import="socnet1.RMIClient"%>
<%@ page import="socnet1.Post"%>
<%@ page import="java.util.ArrayList"%>

<% RMIClient c = (RMIClient) session.getAttribute("rmi");
   ArrayList<Post> posts = c.rmiServer.getAllOrderedPostsRMI(c.username);
   Boolean owner;
   %>

<html>
  <head>	
    <title></title>	
    <link rel="stylesheet" type="text/css" href="css/style.css"/>	
    <script type="text/javascript">
      function outputPost(source, text, sentDate, replyLevel, owner, ID, fileName){
      var rl = parseInt(replyLevel);
      var html =  "<div class='timeline_div' id='"+ID+"' style='position:relative;left:"+(rl*50)+"px'>";

	if(fileName!="null"){
	html += "<div style='float:left;'>"+
	  "<img src='images/"+fileName+"' width='100'>"+
	  "</div >";
	}
	html += "<div class='post_div'>";
	html += "FROM "+source+" <br>"+
	"date:"+ sentDate +" "+
	"<p>"+text+"</p>"+
	"<button onclick=\"newReply('"+ID+"','"+(rl+1)+"')\">Reply</button>";

	if(owner=="true"){
	html += "<button onclick=\"editPost('"+ID+"')\"> Edit </button>" +
	"<form action='deletePost' method='post'>"+
	  "<input type='hidden' name='id' value="+ID+"> "+
	  "<input type='submit' value='Delete'>"+
	  "</form>";
	}

	html += "</div></div></div><br>";				

document.write(html);
}

function newPost(divID){
var html = 	"<div>"+
  "<form action='newPost' method='post' enctype='multipart/form-data'>"+
    "<textarea name='text' rows='2' cols='30' placeholder='New post here'>"+
      "</textarea>"+
    "<br>"+
    "<input type='file' name='pic' accept='image/*'>"+
    "<input type='submit' value='Submit'>"+
    "</form>"+
  "</div>";

document.write(html);
}
function newReply(parent, replyLvl){
div = document.createElement('div');
div.innerHTML = "<form action='newReply' method='post'>"+
  "<textarea name='text' rows='2' cols='30'>"+
    "</textarea>"+
  "<br>"+
  "<input type='hidden' name='parent' value='"+parent+"'>"+
  "<input type='hidden' name='replyLvl' value='"+replyLvl+"'>"+
  "<input type='submit' value='Submit'>"+
  "</form>";
document.getElementById(parent).appendChild(div);
}
function editPost(divID){
id = parseInt(divID);
div = document.createElement('div');
div.innerHTML = "<form action='editPost' method='post'>"+
  "<textarea name='text' rows='2' cols='30' placeholder='Edit post here'>"+
    "</textarea>"+
  "<br>"+
  "<input type='hidden' name='postID' value='"+id+"'>"+
  "<input type='submit' value='Submit'>"+
  "</form>";
document.getElementById(divID).appendChild(div);			
}
</script>	
</head>
<body>
  <div class="sub_div">
  <h1>Timeline</h1>
  
  <script type="text/javascript"> newPost(); </script>
  
  <% for(Post p : posts) { %>
  <% owner = c.username.equals(p.getSource()); %>
  <script type="text/javascript"> outputPost( "<%=p.getSource()%>", "<%=p.getText()%>", "<%=p.getSentDate()%>", "<%=p.getReplyLevel()%>", "<%=owner%>", "<%=p.getID()%>", "<%=p.getFileName()%>" );</script>
  <% } %>
  </div>

</body>
</html>
