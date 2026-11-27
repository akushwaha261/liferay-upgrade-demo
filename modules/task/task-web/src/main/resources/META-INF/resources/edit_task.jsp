<%@ include file="/init.jsp" %>

<portlet:renderURL var="viewURL" />

<portlet:actionURL name="/task/add_task" var="addTaskURL" />

<liferay-ui:error key="titleRequired" message="please-enter-a-title" />

<aui:form action="<%= addTaskURL %>" method="post" name="fm">
	<aui:input name="redirect" type="hidden" value="<%= viewURL %>" />

	<aui:input label="title" name="title" type="text" />

	<aui:input label="description" name="description" type="textarea" />

	<div class="form-group">
		<label for="<portlet:namespace />dueDate">
			<liferay-ui:message key="due-date" />
		</label>

		<input class="form-control" id="<portlet:namespace />dueDate" name="<portlet:namespace />dueDate" type="date" />
	</div>

	<aui:button-row>
		<aui:button type="submit" value="save" />

		<aui:button href="<%= viewURL %>" type="cancel" />
	</aui:button-row>
</aui:form>
