package com.acme.task.web.portlet.action;

import com.acme.task.web.constants.TaskWebPortletKeys;

import com.liferay.portal.kernel.portlet.bridges.mvc.MVCRenderCommand;

import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.osgi.service.component.annotations.Component;

@Component(
	property = {
		"javax.portlet.name=" + TaskWebPortletKeys.TASKWEB,
		"mvc.command.name=/task/edit_task"
	},
	service = MVCRenderCommand.class
)
public class EditTaskMVCRenderCommand implements MVCRenderCommand {

	@Override
	public String render(
		RenderRequest renderRequest, RenderResponse renderResponse) {

		return "/edit_task.jsp";
	}

}
