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
package com.axelor.apps.portal.web;

import com.axelor.apps.project.db.ProjectTask;
import com.axelor.db.annotations.Track;
import com.axelor.db.annotations.TrackField;
import com.axelor.i18n.I18n;
import com.axelor.meta.db.MetaField;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.meta.schema.actions.ActionView.ActionViewBuilder;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ProjectMenuPortalController {

  public void trackProjectTaskFields(ActionRequest request, ActionResponse response) {

    Track track = ProjectTask.class.getAnnotation(Track.class);
    List<String> trackFieldNameList = Collections.emptyList();

    if (track != null && track.fields() != null) {
      trackFieldNameList =
          Arrays.stream(track.fields()).map(TrackField::name).collect(Collectors.toList());
    }

    ActionViewBuilder builder =
        ActionView.define(I18n.get("Track project task fields"))
            .model(MetaField.class.getName())
            .add("grid", "meta-field-track-grid")
            .domain(
                "self.metaModel.fullName = :_metaModelName AND self.name IN (:_trackFieldNameList)")
            .context("_metaModelName", ProjectTask.class.getName())
            .context("_trackFieldNameList", trackFieldNameList);

    response.setView(builder.map());
  }
}
