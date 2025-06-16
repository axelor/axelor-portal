/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.portal.service;

import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.ProjectTaskLink;
import com.axelor.apps.project.db.repo.ProjectTaskLinkRepository;
import com.axelor.apps.project.service.taskLink.ProjectTaskLinkServiceImpl;
import com.axelor.common.ObjectUtils;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ProjectTaskLinkPortalServiceImpl extends ProjectTaskLinkServiceImpl {

  @Inject
  public ProjectTaskLinkPortalServiceImpl(ProjectTaskLinkRepository projectTaskLinkRepository) {

    super(projectTaskLinkRepository);
  }

  @Override
  public String getProjectTaskDomain(ProjectTask projectTask) {

    List<Long> unselectableTaskIdList = new ArrayList<>();
    if (projectTask.getId() != null) {
      unselectableTaskIdList.add(projectTask.getId());
    } else {
      unselectableTaskIdList.add(0L);
    }

    if (!ObjectUtils.isEmpty(projectTask.getProjectTaskLinkList())) {
      unselectableTaskIdList.addAll(
          projectTask.getProjectTaskLinkList().stream()
              .map(ProjectTaskLink::getRelatedTask)
              .map(ProjectTask::getId)
              .collect(Collectors.toList()));
    }

    // Portal change: update filter to fetch tasks with same type and project
    return String.format(
        "self.id NOT IN (%s) AND self.typeSelect = '%s' AND self.project.id = %s",
        unselectableTaskIdList.stream().map(String::valueOf).collect(Collectors.joining(",")),
        projectTask.getTypeSelect(),
        projectTask.getProject().getId());
  }
}
