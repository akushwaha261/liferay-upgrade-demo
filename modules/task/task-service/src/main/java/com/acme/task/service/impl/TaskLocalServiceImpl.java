/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.acme.task.service.impl;

import com.acme.task.model.Task;
import com.acme.task.service.base.TaskLocalServiceBaseImpl;
import com.liferay.portal.aop.AopService;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.UserLocalService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Date;
import java.util.List;

/**
 * @author Brian Wing Shun Chan
 */
@Component(
        property = "model.class.name=com.acme.task.model.Task",
        service = AopService.class
)
public class TaskLocalServiceImpl extends TaskLocalServiceBaseImpl {
    public Task addTask(
            long userId, long groupId, String title, String description,
            Date dueDate, ServiceContext serviceContext)
            throws PortalException {
        User user = _userLocalService.getUser(userId);
        long taskId = counterLocalService.increment(Task.class.getName());
        Task task = taskPersistence.create(taskId);
        Date now = new Date();
        task.setGroupId(groupId);
        task.setCompanyId(user.getCompanyId());
        task.setUserId(userId);
        task.setUserName(user.getFullName());
        task.setCreateDate(now);
        task.setModifiedDate(now);
        task.setTitle(title);
        task.setDescription(description);
        task.setStatus(0); // 0 = open
        task.setDueDate(dueDate);

        return taskPersistence.update(task);
    }

    public List<Task> getTasksByStatus(long groupId, int status) {
        return taskPersistence.findByG_S(groupId, status);
    }

    @Reference
    private UserLocalService _userLocalService;
}