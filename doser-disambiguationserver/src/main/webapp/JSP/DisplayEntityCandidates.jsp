<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Potential Candidates</title>
</head>
<body>
	<%
		String info = (String) request.getAttribute("info");
		if (info != null) {
	%>
	<h3>${info}</h3>
	<%
		}
	%>
	<p>
	<h1>Search Entity:</h1>
	<form
		action="http://theseus.dimis.fim.uni-passau.de:8080/doser-disambiguationserver/DisplayEntityCandidatesServlet"
		method="POST">
		<p>
			<%
				String searchlabel = "";
				searchlabel = (request.getAttribute("searchlabel") == null) ? ""
						: (String) request.getAttribute("searchlabel");
			%>
			Label:<br> <input name="searchlabel" type="text" size="45"
				maxlength="100" value="<%=searchlabel%>">
		</p>
		<p>
			<input type="submit" value="Submit">
		</p>
	</form>
	<h1>Add and search entity</h1>
	<form
		action="http://theseus.dimis.fim.uni-passau.de:8080/doser-disambiguationserver/ShowIndexEntityServlet"
		method="POST">
		<p>
			URL:<br> <input name="newurl" type="text" size="45"
				maxlength="100">
		</p>
		<p>
			<input type="hidden" name="searchlabel" value="<%=searchlabel%>" /> <input
				type="submit" value="Search">
		</p>
	</form>
	<h1>Entity Suggestions</h1>
	<%
		int maxDocuments = Integer.valueOf((String) request
				.getAttribute("candidatesize"));
		System.out.println("MaxDocuments: " + maxDocuments);
		for (int i = 0; i < maxDocuments; i++) {
			String url = (String) request.getAttribute("candidate"
					+ String.valueOf(i));
	%>
	<a
		href="http://theseus.dimis.fim.uni-passau.de:8080/doser-disambiguationserver/ShowIndexEntityServlet?entry=<%=url%>&searchlabel=<%=searchlabel%>">modify</a>&nbsp;&nbsp;<%=url%>
	<hr>
	<br>
	<%
		}
	%>
</body>
</html>
