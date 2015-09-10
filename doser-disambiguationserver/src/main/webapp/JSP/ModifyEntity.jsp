<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Modify Entity</title>
</head>
<body>
	Modification of ${uri}, search label ${searchlabel}
	<%
		String mode = (String) request.getAttribute("mode");
		if (mode != null && mode.equalsIgnoreCase("new")) {
	%>
	<form
		action="http://theseus.dimis.fim.uni-passau.de:8080/doser-disambiguationserver/StoreNewIndexEntryServlet"
		method="POST">
		<%
			} else {
		%>
		<form
			action="http://theseus.dimis.fim.uni-passau.de:8080/doser-disambiguationserver/StoreIndexModificationServlet"
			method="POST">
			<%
				}
			%>
			<p>
				Label (Changing the label is not recommended):<br> <input name="label" type="text" size="50"
					maxlength="100" value="${label}">
			</p>
			<p>
				Synonymous terms (One term per line):<br>
				<textarea name="synonyms" cols="70" rows="10">${synonyms}</textarea>
			</p>
			<p>
				Description:<br>
				<textarea name="description" cols="70" rows="10">${description}</textarea>
			</p>
			<input type="hidden" name="searchlabel" value="${searchlabel}" />
			<input type="hidden" name="uri" value="${uri}" />
			<input type="hidden" name="newid" value="${newid}" />
			 <input
				type="submit" value="Submit">
		</form>
</body>
</html>