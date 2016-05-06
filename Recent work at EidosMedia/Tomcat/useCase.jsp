<%@ taglib prefix="e" uri="etag"%>
<%@ taglib prefix="c" uri="jstlCore"%>
<%@ taglib prefix="x" uri="jstlXml"%>
<%@ taglib prefix="fn" uri="jstlFunctions"%>
<%@ taglib prefix="cmp" tagdir="/WEB-INF/tags/campaign"%>

<e:methodesession>
	<%-- <c:out value="${param.regions}" />
	<c:out value="${param.version}" />
	<c:out value="${param.product}" />
	<c:out value="${param.scope}" />
	<c:out value="${param.skey}" />
	<c:out value="${param.sysname}" /> --%>
	<!-- /methode/meth01/methode-servlets/webclient/WEB-INF/tags/campaign/newUseCase.tag -->
	<cmp:newUseCase channels="${param.regions}" version="${param.version}" product="${param.product}" scope="${param.scope}" skey="${param.skey}" titleName="${param.titlename}"/>
</e:methodesession>