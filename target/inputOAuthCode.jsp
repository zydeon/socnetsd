<%@ page import="socnet1.RMIClient"%>
<% RMIClient c = (RMIClient) session.getAttribute("rmi"); %>

<div>
	<iframe src="<%=c.getAuthUrl()%>"></iframe>
</div>
<br>
<div>
	<form action="oauthCode" method="post">
		Insert code: <input type="text" name="code">
		<input type="submit" value="Enter">
	</form>
</div>