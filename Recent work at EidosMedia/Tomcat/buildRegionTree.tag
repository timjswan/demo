<%@ taglib prefix="r"  uri="/WEB-INF/taglibs/render.tld"  %>
<%@ taglib prefix="c" uri="jstlCore"%>
<%@ taglib prefix="fn" uri="jstlFunctions"%>
<%@ taglib prefix="session" uri="http://www.eidosmedia.com/tags/session"%>
<%@ taglib prefix="scc" uri="http://www.eidosmedia.com/tags/siteConfigCache"%>
<%@ taglib prefix="brt" uri="/WEB-INF/taglibs/buildRegionTree.tld" %>

<c:set var="skey" value="${param.skey}" />
<c:if test="${not empty skey}">
	<c:set var="sdata" value="${session:getSessionData(skey)}" />
	<c:set var="db" value="${sdata.eomDb}" />
	<c:set var="siteConfig" value="${scc:getSiteConfig(db.userName, db, '/SysConfig/siteConfig.cfg')}" />
	<c:set var="outChannels" value="${siteConfig.outputChannels}" />
	
	<!-- Generate the map -->
	<brt:buildRegionTree outputChannels="${outChannels}" var="regionTreeMap" />
	
	<!-- Build the HTML as a var -->
	<c:set var="regionList">		
		<ul class="mainList">
		
		<!--  Loop through the region map -->
		<c:forEach var="region" items="${regionTreeMap}" varStatus="loopCounter">
			
			<!-- Create the region list items -->
			<li class="open"><input type="checkbox" name="regionCheck" class="regionCheck regionSelector" value="${fn:trim(region.key)}"/><a href="#">Region Name: <c:out value="${region.key}" /></a>
				
				<!--  Create the sub list items for the products in the region -->
				<ul class="subList">
					<c:forEach var="product" items="${region.value}" varStatus="loopCounter">
						<li><input type="checkbox" name="regionCheck" class="regionCheck" value="${fn:trim(product)}"/> Product Name: <c:out value="${product}"></c:out></li>
					</c:forEach>
				</ul>
			</li>
		</c:forEach>
		</ul>
	</c:set>
	
	<!--  Output the html -->
	<c:out value="${regionList}" escapeXml="false" />
</c:if>