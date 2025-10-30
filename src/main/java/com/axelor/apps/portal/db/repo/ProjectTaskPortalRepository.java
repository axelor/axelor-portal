package com.axelor.apps.portal.db.repo;

import com.axelor.apps.base.db.MailMessageFile;
import com.axelor.apps.base.db.repo.MailMessageFileRepository;
import com.axelor.apps.hr.db.repo.ProjectTaskHRRepository;
import com.axelor.apps.portal.service.NotificationService;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.repo.ProjectTaskRepository;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import java.util.List;
import java.util.Map;

public class ProjectTaskPortalRepository extends ProjectTaskHRRepository {

  @Inject
  public ProjectTaskPortalRepository() {}

  @Override
  public ProjectTask save(ProjectTask projectTask) {
    projectTask = super.save(projectTask);

    if (projectTask.getTypeSelect().equals(ProjectTaskRepository.TYPE_TICKET)
        && projectTask.getProject() != null
        && projectTask.getProject().getPortalWorkspace() != null) {
      Beans.get(NotificationService.class)
          .notifyUser(
              "ticketing",
              projectTask.getId(),
              projectTask.getClass().getName(),
              projectTask.getProject().getPortalWorkspace());
    }

    return projectTask;
  }

  @Override
  public Map<String, Object> populate(Map<String, Object> json, Map<String, Object> context) {

    ProjectTask projectTask = this.find((Long) json.get("id"));

    if (!context.containsKey("_model")) {
      List<MailMessageFile> mailMessageFilePreviewList =
          Beans.get(MailMessageFileRepository.class)
              .all()
              .filter(
                  "self.relatedMailMessage.relatedModel = ?1 and self.relatedMailMessage.relatedId = ?2",
                  ProjectTask.class.getName(),
                  projectTask.getId())
              .fetch();

      json.put("$mailMessageFilePreviewList", mailMessageFilePreviewList);
    }

    return super.populate(json, context);
  }
}
