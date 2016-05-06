<%@ taglib prefix="c" uri="jstlCore"%>
<c:if test="${not empty applicationScope[progressAttributeName]}">
	<c:out value="${applicationScope[statusAttributeName]}" /> - <c:out value="${applicationScope[progressAttributeName]}" />
</c:if>