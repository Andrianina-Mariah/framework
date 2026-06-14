<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Test FrontControllerServlet</title>
</head>
<body>
<h2>Testing Simple Spring MVC-like Framework</h2>

<h3>GET Request Test</h3>
<p>Click <a href="${pageContext.request.contextPath}/hello">here</a> to send a GET request to the front controller.</p>

<h3>POST Request Test</h3>
<form action="${pageContext.request.contextPath}/submit" method="post">
    <input type="submit" value="Send POST Request">
</form>

<p>After submitting, the response will display the request URL.</p>
</body>
</html>