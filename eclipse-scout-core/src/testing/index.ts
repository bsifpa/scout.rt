/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
// /////////////////////////////////////////////////////////////////
// TEST SUPPORT - DO NOT USE IN PRODUCTION CODE
// /////////////////////////////////////////////////////////////////
import {ObjectFactory} from '../index';
import * as self from './index';

export {default as TestingApp} from './TestingApp';
export {default as JasmineScout} from './JasmineScout';
export {JasmineScoutUtil} from './JasmineScoutUtil';
export {default as LocaleSpecHelper} from './text/LocaleSpecHelper';
export {default as MenuSpecHelper} from './menu/MenuSpecHelper';
export {default as TreeSpecHelper} from './tree/TreeSpecHelper';
export {default as TableSpecHelper} from './table/TableSpecHelper';
export {default as FormSpecHelper} from './form/FormSpecHelper';
export {default as CloneSpecHelper} from './form/fields/CloneSpecHelper';
export {default as TabBoxSpecHelper} from './form/fields/tabbox/TabBoxSpecHelper';
export {default as OutlineSpecHelper} from './desktop/outline/OutlineSpecHelper';
export {default as DummyLookupCall} from './lookup/DummyLookupCall';
export {default as AbortableMicrotaskStaticLookupCall} from './lookup/AbortableMicrotaskStaticLookupCall';
export {default as ActiveDummyLookupCall} from './lookup/ActiveDummyLookupCall';
export {default as ColumnDescriptorDummyLookupCall} from './lookup/ColumnDescriptorDummyLookupCall';
export {default as LanguageDummyLookupCall} from './lookup/LanguageDummyLookupCall';
export {default as FocusManagerSpecHelper} from './focus/FocusManagerSpecHelper';
export {default as GroupBoxSpecHelper} from './form/fields/groupbox/GroupBoxSpecHelper';
export {default as JQueryTesting} from './jquery-testing';
export default self;
ObjectFactory.get().registerNamespace('scout', self);
