/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.jackson.dataobject.fixture;

import java.util.Locale;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "_type")
@JsonTypeName("pojo-with-locale-property")
@JsonPropertyOrder(alphabetic = true)
public class TestPojoWithLocaleProperties {
  protected Locale m_locale1;
  protected Locale m_locale2;
  protected Map<Locale, String> m_localeStringMap;

  public Locale getLocale1() {
    return m_locale1;
  }

  public void setLocale1(Locale locale1) {
    m_locale1 = locale1;
  }

  public Locale getLocale2() {
    return m_locale2;
  }

  public void setLocale2(Locale locale2) {
    m_locale2 = locale2;
  }

  public Map<Locale, String> getLocaleStringMap() {
    return m_localeStringMap;
  }

  public void setLocaleStringMap(Map<Locale, String> localeStringMap) {
    m_localeStringMap = localeStringMap;
  }
}
