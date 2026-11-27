package com.acme.task.web.portlet.action;

import com.acme.task.model.Task;
import com.acme.task.service.TaskLocalService;
import com.acme.task.web.constants.TaskWebPortletKeys;

import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCActionCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCActionCommand;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextFactory;
import com.liferay.portal.kernel.servlet.SessionErrors;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.DateFormatFactoryUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;

import java.util.Date;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(
	property = {
		"javax.portlet.name=" + TaskWebPortletKeys.TASKWEB,
		"mvc.command.name=/task/add_task"
	},
	service = MVCActionCommand.class
)
public class AddTaskMVCActionCommand extends BaseMVCActionCommand {

	@Override
	protected void doProcessAction(
			ActionRequest actionRequest, ActionResponse actionResponse)
		throws Exception {

		ThemeDisplay themeDisplay = (ThemeDisplay)actionRequest.getAttribute(
			WebKeys.THEME_DISPLAY);

		String title = ParamUtil.getString(actionRequest, "title");
		String description = ParamUtil.getString(actionRequest, "description");
		Date dueDate = ParamUtil.getDate(
			actionRequest, "dueDate",
			DateFormatFactoryUtil.getSimpleDateFormat("yyyy-MM-dd"), null);

		if (Validator.isNull(title)) {
			SessionErrors.add(actionRequest, "titleRequired");

			// show the form again instead of the list

			actionResponse.getRenderParameters().setValue(
				"mvcRenderCommandName", "/task/edit_task");

			return;
		}

		ServiceContext serviceContext = ServiceContextFactory.getInstance(
			Task.class.getName(), actionRequest);

		_taskLocalService.addTask(
			themeDisplay.getUserId(), themeDisplay.getScopeGroupId(), title,
			description, dueDate, serviceContext);
	}

	@Reference
	private TaskLocalService _taskLocalService;

}
