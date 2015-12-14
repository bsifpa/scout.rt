/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html.json.table;

import org.eclipse.scout.rt.client.ui.basic.table.columns.IStringColumn;
import org.eclipse.scout.rt.client.ui.basic.table.userfilter.ColumnUserFilterState;
import org.eclipse.scout.rt.ui.html.json.table.userfilter.JsonTextColumnUserFilter;
import org.json.JSONObject;

public class JsonStringColumn<STRING_COLUMN extends IStringColumn> extends JsonColumn<STRING_COLUMN> {

  public JsonStringColumn(STRING_COLUMN model) {
    super(model);
  }

  @Override
  public String getObjectType() {
    return "StringColumn";
  }

  @Override
  protected ColumnUserFilterState createFilterStateFromJson(JSONObject json) {
    return new JsonTextColumnUserFilter(null).createFilterStateFromJson(getColumn(), json);
  }

  @Override
  public JSONObject toJson() {
    JSONObject json = super.toJson();
    json.put("textWrap", getColumn().isTextWrap());
    return json;
  }
}
