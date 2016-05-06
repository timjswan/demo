<%@ taglib prefix="r"  uri="/WEB-INF/taglibs/render.tld"  %>
<%@ taglib prefix="c" uri="jstlCore"%>
<%@ taglib prefix="fn" uri="jstlFunctions"%>
<%@ taglib prefix="session" uri="http://www.eidosmedia.com/tags/session"%>
<%@ taglib prefix="scc" uri="http://www.eidosmedia.com/tags/siteConfigCache"%>
<%-- <%@ taglib prefix="propHelper" uri="http://www.eidosmedia.com/tags/property"%> --%>
<%@ taglib prefix="nuc" uri="/WEB-INF/taglibs/newUseCase.tld" %>

<%@ attribute name="skey" type="java.lang.String" required="true"%>
<%@ attribute name="titleName" type="java.lang.String" required="true"%>
<%@ attribute name="channels" type="java.lang.String" required="true"%>
<%@ attribute name="version" type="java.lang.String" required="true"%>
<%@ attribute name="product" type="java.lang.String" required="true"%>
<%@ attribute name="scope" type="java.lang.String" required="true"%>

<%-- <c:set var="skey" value="${param.skey}" /> --%>
<c:if test="${not empty skey}">
	<c:set var="sdata" value="${session:getSessionData(skey)}" />
	<c:set var="db" value="${sdata.eomDb}" />
	<%-- <c:out value="${sysName}" />
	<c:out value="${channels}" />
	<c:out value="${version}" />
	<c:out value="${db}" />
	<c:out value="${product}" />
	<c:out value="${scope}" /> --%>
	<nuc:newUseCase titleName="${titleName}" channels="${channels}" version="${version}" db="${db}" product="${product}" scope="${scope}" />
</c:if>