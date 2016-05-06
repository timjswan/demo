<%@ taglib prefix="c" uri="jstlCore"%>
<%@ taglib prefix="e" uri="etag"%>
<%@ taglib prefix="fn" uri="jstlFunctions"%>
<%@ taglib prefix="progid" uri="http://www.eidosmedia.com/tags/ProgressHelper" %>
<%@ taglib prefix="campaignid" uri="http://www.eidosmedia.com/tags/CampaignIdHelper" %>
<%@ taglib prefix="cmp" tagdir="/WEB-INF/tags/campaign"%>

<c:set var="progressId" value="${progid:generateId('10', '99', '%s%d', '2')}" scope="application" />

<e:methodesession>
<html>
	<head>
		<meta http-equiv="X-UA-Compatible" content="IE=Edge"></meta>
		<!-- Force IE9 standard document mode -->
		<title>Campaign</title>
		<link rel="stylesheet" href="<e:clienturl value="/eweb/css/reset.css" />" type="text/css">
		<link rel="stylesheet" href="<e:clienturl value="/eweb/css/campaign.css" />" type="text/css">	
		
		<script src="http://ajax.googleapis.com/ajax/libs/jquery/1/jquery.js" type="text/javascript"></script>
		<script type="text/javascript" src="<e:clienturl value="/eweb/js/dsmHelper.js" />"></script>
		<script type="text/javascript" src="<e:clienturl value="/eweb/js/campaign.js" />"></script>
		<script type="text/javascript" src="<e:clienturl value="/eweb/js/firebug-lite.js" />"></script>
	</head>
	<body class="form">
		<!-- /methode/meth01/methode-servlets/webclient/WEB-INF/tags/campaign/campaignInterface.tag -->
		<cmp:campaignInterface id="one" />
		<cmp:campaignInterface id="two" />
		<cmp:campaignInterface id="three" />
		<cmp:campaignInterface id="four" />
		<form id="newCampaignForm" name="newCampaignForm" action="useCase.jsp">
			<fieldset>
				<legend>Campaign No: <c:out value="${campaignid:getId()}" /></legend>
				<div id="formBody">
					<label for="product">Product: </label>
					<select id="product" name="product"></select>
					<label for="scope">Scope: </label>
					<select id="scope" name="scope"></select>
					<br />
					<label>Version: <input type="text" id="version" name="version" /></label>
					<br />
					<label for="regionScope">Region scope: </label>
					<select id="regionScope" name="regionScope">
						<option value="whole" selected="selected">Whole region</option>
						<option value="individual">Individual region</option>
					</select>
					<div id="regionTree">
						<!-- /methode/meth01/methode-servlets/webclient/WEB-INF/tags/campaign/buildRegionTree.tag -->
						<cmp:buildRegionTree />
					</div>
				</div>
				<button id="submit" type="button">Create</button><span id="error" class="clearable"></span><span id="progress" class="clearable"></span>
			</fieldset>
		</form>
	</body>
</html>
</e:methodesession>