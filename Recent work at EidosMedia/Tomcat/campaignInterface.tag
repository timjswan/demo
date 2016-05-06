<%@ taglib prefix="r"  uri="/WEB-INF/taglibs/render.tld"  %>
<%@ taglib prefix="c" uri="jstlCore"%>
<%@ taglib prefix="fn" uri="jstlFunctions"%>
<%@ taglib prefix="session" uri="http://www.eidosmedia.com/tags/session"%>
<%@ taglib prefix="scc" uri="http://www.eidosmedia.com/tags/siteConfigCache"%>
<%@ taglib prefix="ched" uri="/WEB-INF/taglibs/channelEditionName.tld" %>

<%@ attribute name="id" type="java.lang.String" required="true"%>

<c:set var="skey" value="${param.skey}" />
<c:if test="${not empty skey}">
	<c:set var="sdata" value="${session:getSessionData(skey)}" />
	<c:set var="db" value="${sdata.eomDb}" />
	<c:set var="siteConfig" value="${scc:getSiteConfig(db.userName, db, '/SysConfig/siteConfig.cfg')}" />
	<c:set var="outChannels" value="${siteConfig.outputChannels}" />
	
	<!-- Generate the dropdown HTML as a var -->
	<c:set var="select">
		<select id="${id}-channel" class="channel">
			
			<!-- Loop through the output channels -->
			<c:forEach var="outChannel" items="${outChannels}">
				
				<!-- Get the channel name and its editions -->
				<ched:channelName channel="${outChannel}" var="channelName" />
				<ched:obtainEditions channel="${outChannel}" var="editions" />
				
				<!-- Create a string of the edition names with '|' as a delimiter -->
				<c:set var="editionList">		
					<c:forEach var="outEdition" items="${editions}" varStatus="loopCounter">
						<c:if test="${outEdition ne 'None'}">
							<ched:editionName edition="${outEdition}" var="edName" />
							<c:choose>
								<!-- Don't need to add | to the last item -->
								<c:when test="${fn:length(editions) eq loopCounter.count}">
									<c:out value="${edName}" />
								</c:when>
								<c:otherwise>
									<c:out value="${edName}|" />
								</c:otherwise>
							</c:choose>
						</c:if>
					</c:forEach>
				</c:set>
				<!-- Add the list of editions to the option rel -->
				<option rel="${editionList}">${channelName}</option>
			</c:forEach>
		</select>
	</c:set>
	<div class="selectGroup">
		<c:out value="${select}" escapeXml="false" />
		<select id="${id}-edition" class="edition"></select>
	</div>
</c:if>