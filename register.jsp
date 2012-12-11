<%@ page import="socnet1.RMIClient"%>
<% String msg = RMIClient.filter( request.getParameter("msg") ); %>
<% if (msg != null) { %>
  <span style='color:red'>*<%=msg%></span>
<% } %>

<!DOCTYPE html>
<html lang="en">
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <link rel="stylesheet" type="text/css" href="css/style.css"/>
    <title>soc.net</title>  
  </head>

  <body>
    <br>
    <div class="main_div">
      <h1>soc.net</h1>
      <div class="reg_div">
	<h4>Registration</h4>
	<form action="register" method="post"  onsubmit="return checkfields();">
	  Username:<br>
	  <input type="text" name="user" id="user"> <br><br>
	  Password:<br>
	  <input type="password" name="password" id="password"> <br><br>
	  Confirm Password:<br>
	  <input type="password" name="cpassword" id="cpassword"><br><br>
	  <input type="submit" name="register" value="Register">
	</form>

      </div>
    </div>
  </body>

<script type="text/javascript">
    
function checkfields(){ 
    if(document.getElementById('user').value=='' || document.getElementById('user').value==null){
	alert("Invalid Username");
	document.getElementById('user').focus();
	return false; 
    }
    
    if(document.getElementById('password').value=='' || document.getElementById('password').value==null){
	alert("Invalid Password");
	document.getElementById('password').focus();
	return false; 
    }
    
    if(document.getElementById('password').value != document.getElementById('cpassword').value) { 
	alert("Your Passwords do not match.");
	document.getElementById('password').focus();
	return false; 
    }
    
}

</script>
</html>
