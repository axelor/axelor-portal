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
package com.axelor.apps.portal.exception;

public final class PortalExceptionMessage {

  private PortalExceptionMessage() {}

  public static final String CUSTOMER_MISSING = /*$$(*/ "Customer is missing" /*)*/;
  public static final String ADDRESS_MISSING = /*$$(*/ "Address not specified" /*)*/;
  public static final String QTY_ERROR = /*$$(*/ "Quantity must be greater than 0" /*)*/;

  public static final String PRODUCT_MISSING = /*$$(*/ "Product not found" /*)*/;
  public static final String COMPANY_MISSING = /*$$(*/ "Company not found" /*)*/;

  public static final String PRODUCT_LIST_EMPTY = /*$$(*/ "Products empty" /*)*/;

  public static final String INVOICE_NOT_FOUND = /*$$(*/ "Invoice not found" /*)*/;

  public static final String EVENT_MISSING = /*$$(*/ "Event not found" /*)*/;
  public static final String WORKSPACE_MISSING = /*$$(*/ "Workspace not found" /*)*/;
  public static final String PARTICIPANT_MISSING = /*$$(*/ "Participant not found" /*)*/;
  public static final String INVOICE_EXISTS = /*$$(*/
      "Invoice already created for this registration" /*)*/;
  public static final String INVALID_INVOICE = /*$$(*/ "Invoice is not valid for payment" /*)*/;
}
