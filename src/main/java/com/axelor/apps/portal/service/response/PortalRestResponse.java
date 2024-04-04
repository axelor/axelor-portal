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
package com.axelor.apps.portal.service.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.google.common.base.Throwables;
import java.sql.BatchUpdateException;

@JsonInclude(Include.NON_EMPTY)
public class PortalRestResponse {

  public static final int STATUS_SUCCESS = 0;
  public static final int STATUS_FAILURE = -1;

  private Object data;

  private int status;

  private String message;

  public PortalRestResponse success() {
    this.status = STATUS_SUCCESS;
    return this;
  }

  public PortalRestResponse fail() {
    this.status = STATUS_FAILURE;
    return this;
  }

  public Object getData() {
    return data;
  }

  public PortalRestResponse setData(Object data) {
    this.data = data;
    return this;
  }

  public int getStatus() {
    return status;
  }

  public void setStatus(int status) {
    this.status = status;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public void setException(Throwable throwable) {

    Throwable cause = Throwables.getRootCause(throwable);
    if (cause instanceof BatchUpdateException) {
      cause = ((BatchUpdateException) cause).getNextException();
    }

    String message = throwable.getMessage();
    if (message == null || message.startsWith(cause.getClass().getName())) {
      message = cause.getMessage();
    }
    this.setMessage(message);
    this.setStatus(STATUS_FAILURE);
  }
}
