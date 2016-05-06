<%@ taglib prefix="e" uri="etag"%>
<%@ taglib prefix="cmp" tagdir="/WEB-INF/tags/campaign"%>

<e:methodesession>
	<cmp:newCampaign id="${request.getParameter('id')}" />
</e:methodesession>