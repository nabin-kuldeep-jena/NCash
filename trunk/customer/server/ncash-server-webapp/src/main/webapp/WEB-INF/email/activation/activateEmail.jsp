<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
<title></title>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<script language="javascript">
	var xmlhttp;
	function init() {
		// put more code here in case you are concerned about browsers that do not provide XMLHttpRequest object directly
		xmlhttp = new XMLHttpRequest();
	}
	function verifyDetails() {
		var emailId = document.getElementById("emailId");
		var evtAccessKey = document.getElementById("evtAccessKey");
		var authKey = document.getElementById("authKey");
		var url = "http://localhost:8084/webservicedemo/resources/employee/"
				+ empno.value;
		xmlhttp.open('POST', url, true);
		xmlhttp.setRequestHeader("Content-Type", "application/json");
		xmlhttp.send(JSON.stringify({
			"emailId" : emailId,
			"evtAccessKey" : evtAccessKey,
			"authKey" : authKey
		}));
		xmlhttp.onreadystatechange = function() {
			if (xmlhttp.readyState == 4) {
				if (xmlhttp.status == 200) {
					var det = eval("(" + xmlhttp.responseText + ")");
					if (det.age > 0) {
						empname.value = det.name;
						age.value = det.age;
					} else {
						empname.value = "";
						age.value = "";
						alert("Invalid Details");
					}
				} else
					alert("Error ->" + xmlhttp.responseText);
			}
		};
	}
</script>
</head>
<body>
	<form action="./services/user/activateEmail" method="POST" enctype="application/json">
		<h1>NCash Email Activation</h1>
		<table>
			<tr>
				<td><label for="emailLabel">Email :</label></td>
				<td><input name="emailId" disabled="disabled"
					value=<%=request.getParameter( "eId" )%> />
			</tr>
			<tr>
				<td><label for="evtAccessKeyLable">Access Code :</label></td>
				<td><input name="evtAccessKey" />
			</tr>

			<tr>
				<td></td>
				<td><input type="submit" value="Submit"
					onclick="verifyDetails()" /></td>
			</tr>
		</table>
		<input type="hidden" name="evtToken"
			value=<%=request.getParameter( "authKey" )%> />
	</form>
</body>
</html>
