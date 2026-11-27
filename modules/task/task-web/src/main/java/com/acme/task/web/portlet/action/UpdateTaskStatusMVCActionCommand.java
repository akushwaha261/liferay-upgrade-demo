package com.acme.task.web.portlet.action;

import com.acme.task.model.Task;
import com.acme.task.service.TaskLocalService;
import com.acme.task.web.constants.TaskWebPortletKeys;

import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCActionCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCActionCommand;
import com.liferay.portal.kernel.util.ParamUtil;

import java.util.Date;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(
	property = {
		"javax.portlet.name=" + TaskWebPortletKeys.TASKWEB,
		"mvc.command.name=/task/update_status"
	},
	service = MVCActionCommand.class
)
public class UpdateTaskStatusMVCActionCommand extends BaseMVCActionCommand {

	@Override
	protected void doProcessAction(
			ActionRequest actionRequest, ActionResponse actionResponse)
		throws Exception {

		long taskId = ParamUtil.getLong(actionRequest, "taskId");
		int status = ParamUtil.getInteger(actionRequest, "status");

		Task task = _taskLocalService.getTask(taskId);

		task.setStatus(status);
		task.setModifiedDate(new Date());

		_taskLocalService.updateTask(task);
	}

	@Reference
	private TaskLocalService _taskLocalService;

}
