<%@ page import="com.sap.sailing.server.RacingEventService" %>
<%@ page import="com.sap.sailing.domain.base.*" %>
<html>
<head>
</head>
<body>
<%="Hello RacingEventService... !"%>

<%
	RacingEventService racingEventService = (RacingEventService) request.getAttribute("racingEventService");
	for(Event event: racingEventService.getAllEvents()) {
%>
	<h3><%= event.getName() %></h3>
<%    
	}
%>

</body>
</html>
