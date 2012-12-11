<%
if ( session.getAttribute("rmi")==null || session.getAttribute("authCode")==null )
{
%>
    <jsp:forward page="/login.jsp"></jsp:forward>
<%
} 
%>