/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.jackson.dataobject.fixture;

import java.util.Collection;
import java.util.Map;

import javax.annotation.Generated;

import org.eclipse.scout.rt.dataobject.DoCollection;
import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.TypeName;
import org.eclipse.scout.rt.dataobject.fixture.FixtureStringId;
import org.eclipse.scout.rt.dataobject.id.IId;

@TypeName("scout.TestEntityWithVariousIds")
public class TestEntityWithVariousIdsDo extends DoEntity {

  public DoValue<FixtureStringId> stringId() {
    return doValue("stringId");
  }

  public DoValue<IId> iId() {
    return doValue("iId");
  }

  public DoCollection<FixtureStringId> stringIds() {
    return doCollection("stringIds");
  }

  public DoCollection<IId> iIds() {
    return doCollection("iIds");
  }

  public DoValue<Collection<FixtureStringId>> manualStringIds() {
    return doValue("manualStringIds");
  }

  public DoValue<Collection<IId>> manualIIds() {
    return doValue("manualIIds");
  }

  public DoValue<Map<FixtureStringId, String>> stringIdKeyMap() {
    return doValue("stringIdKeyMap");
  }

  public DoValue<Map<IId, String>> iIdKeyMap() {
    return doValue("iIdKeyMap");
  }

  public DoValue<Map<String, FixtureStringId>> stringIdValueMap() {
    return doValue("stringIdValueMap");
  }

  public DoValue<Map<String, IId>> iIdValueMap() {
    return doValue("iIdValueMap");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public TestEntityWithVariousIdsDo withStringId(FixtureStringId stringId) {
    stringId().set(stringId);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public FixtureStringId getStringId() {
    return stringId().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestEntityWithVariousIdsDo withIId(IId iId) {
    iId().set(iId);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public IId getIId() {
    return iId().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestEntityWithVariousIdsDo withStringIds(Collection<? extends FixtureStringId> stringIds) {
    stringIds().updateAll(stringIds);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestEntityWithVariousIdsDo withStringIds(FixtureStringId... stringIds) {
    stringIds().updateAll(stringIds);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Collection<FixtureStringId> getStringIds() {
    return stringIds().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestEntityWithVariousIdsDo withIIds(Collection<? extends IId> iIds) {
    iIds().updateAll(iIds);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestEntityWithVariousIdsDo withIIds(IId... iIds) {
    iIds().updateAll(iIds);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Collection<IId> getIIds() {
    return iIds().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestEntityWithVariousIdsDo withManualStringIds(Collection<FixtureStringId> manualStringIds) {
    manualStringIds().set(manualStringIds);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Collection<FixtureStringId> getManualStringIds() {
    return manualStringIds().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestEntityWithVariousIdsDo withManualIIds(Collection<IId> manualIIds) {
    manualIIds().set(manualIIds);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Collection<IId> getManualIIds() {
    return manualIIds().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestEntityWithVariousIdsDo withStringIdKeyMap(Map<FixtureStringId, String> stringIdKeyMap) {
    stringIdKeyMap().set(stringIdKeyMap);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Map<FixtureStringId, String> getStringIdKeyMap() {
    return stringIdKeyMap().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestEntityWithVariousIdsDo withIIdKeyMap(Map<IId, String> iIdKeyMap) {
    iIdKeyMap().set(iIdKeyMap);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Map<IId, String> getIIdKeyMap() {
    return iIdKeyMap().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestEntityWithVariousIdsDo withStringIdValueMap(Map<String, FixtureStringId> stringIdValueMap) {
    stringIdValueMap().set(stringIdValueMap);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Map<String, FixtureStringId> getStringIdValueMap() {
    return stringIdValueMap().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestEntityWithVariousIdsDo withIIdValueMap(Map<String, IId> iIdValueMap) {
    iIdValueMap().set(iIdValueMap);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Map<String, IId> getIIdValueMap() {
    return iIdValueMap().get();
  }
}
