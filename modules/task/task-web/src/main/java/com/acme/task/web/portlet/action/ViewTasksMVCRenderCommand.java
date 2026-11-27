package com.acme.task.web.portlet.action;

import com.acme.task.model.Task;
import com.acme.task.service.TaskLocalService;
import com.acme.task.web.constants.TaskWebPortletKeys;

import com.liferay.portal.kernel.portlet.bridges.mvc.MVCRenderCommand;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.WebKeys;

import java.util.List;

import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(
	property = {
		"javax.portlet.name=" + TaskWebPortletKeys.TASKWEB,
		"mvc.command.name=/", "mvc.command.name=/task/view_tasks"
	},
	service = MVCRenderCommand.class
)
public class ViewTasksMVCRenderCommand implements MVCRenderCommand {

	@Override
	public String render(
			RenderRequest renderRequest, RenderResponse renderResponse)
		throws PortletException {

		ThemeDisplay themeDisplay = (ThemeDisplay)renderRequest.getAttribute(
			WebKeys.THEME_DISPLAY);

		int status = ParamUtil.getInteger(renderRequest, "status", 0);

		List<Task> tasks = _taskLocalService.getTasksByStatus(
			themeDisplay.getScopeGroupId(), status);

		renderRequest.setAttribute("tasks", tasks);
		renderRequest.setAttribute("status", status);

		return "/view.jsp";
	}

	@Reference
	private TaskLocalService _taskLocalService;

}
