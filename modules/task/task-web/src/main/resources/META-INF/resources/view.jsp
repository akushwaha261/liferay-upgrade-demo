<%@ include file="/init.jsp" %>

<%
List<Task> tasks = (List<Task>)request.getAttribute("tasks");
int status = (Integer)request.getAttribute("status");

Format dateFormat = FastDateFormatFactoryUtil.getSimpleDateFormat(
	"yyyy-MM-dd", locale, timeZone);
%>

<portlet:renderURL var="addTaskURL">
	<portlet:param name="mvcRenderCommandName" value="/task/edit_task" />
</portlet:renderURL>

<portlet:renderURL var="openURL"><portlet:param name="status" value="0" /></portlet:renderURL>
<portlet:renderURL var="doneURL"><portlet:param name="status" value="1" /></portlet:renderURL>

<p>
	<a href="<%= openURL %>"><liferay-ui:message key="open" /></a> |
	<a href="<%= doneURL %>"><liferay-ui:message key="done" /></a>

	<a class="btn btn-primary btn-sm ml-3" href="<%= addTaskURL %>">
		<liferay-ui:message key="add-task" />
	</a>
</p>

<table class="table table-striped">
	<thead>
		<tr>
			<th>ID</th>
			<th><liferay-ui:message key="title" /></th>
			<th><liferay-ui:message key="due-date" /></th>
			<th></th>
		</tr>
	</thead>

	<tbody>

		<%
		for (Task task : tasks) {
		%>

			<portlet:actionURL name="/task/update_status" var="markDoneURL">
				<portlet:param name="taskId" value="<%= String.valueOf(task.getTaskId()) %>" />
				<portlet:param name="status" value="1" />
			</portlet:actionURL>

			<tr>
				<td><%= task.getTaskId() %></td>
				<td><%= HtmlUtil.escape(task.getTitle()) %></td>
				<td>
					<%= (task.getDueDate() == null) ? "-" : dateFormat.format(task.getDueDate()) %>
				</td>
				<td>

					<%
					if (status == 0) {
					%>

						<form action="<%= markDoneURL %>" method="post">
							<button class="btn btn-secondary btn-sm" type="submit">
								<liferay-ui:message key="mark-done" />
							</button>
						</form>

					<%
					}
					%>

				</td>
			</tr>

		<%
		}
		%>

	</tbody>
</table>
