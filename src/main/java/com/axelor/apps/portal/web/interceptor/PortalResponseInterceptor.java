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
package com.axelor.apps.portal.web.interceptor;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.portal.service.response.PortalRestResponse;
import com.axelor.auth.AuthSecurityException;
import com.google.common.base.Throwables;
import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PortalResponseInterceptor implements MethodInterceptor {

  private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Override
  public Object invoke(MethodInvocation invocation) throws Throwable {
    logger.trace("Web Service: {}", invocation.getMethod());

    PortalRestResponse response = null;

    try {
      response = (PortalRestResponse) invocation.proceed();
    } catch (Exception e) {
      response = new PortalRestResponse();
      response = onException(e, response);
    }
    return response;
  }

  private PortalRestResponse onException(Throwable throwable, PortalRestResponse response) {
    final Throwable cause = throwable.getCause();
    final Throwable root = Throwables.getRootCause(throwable);
    for (Throwable ex : Arrays.asList(throwable, cause, root)) {
      if (ex instanceof AxelorException) {
        return onAxelorException((AxelorException) ex, response);
      }
      if (ex instanceof AuthSecurityException) {
        return onAuthSecurityException((AuthSecurityException) ex, response);
      }
    }
    logger.error("Error: {}", throwable.getMessage());
    response.setException(throwable);
    TraceBackService.trace(throwable);
    return response;
  }

  private PortalRestResponse onAuthSecurityException(
      AuthSecurityException e, PortalRestResponse response) {
    logger.error("Access Error: {}", e.getMessage());
    response.setException(e);
    return response;
  }

  private PortalRestResponse onAxelorException(AxelorException ex, PortalRestResponse response) {
    logger.error("Error: {}", ex.getMessage());
    TraceBackService.trace(ex);
    response.setException(ex);
    return response;
  }
}
