/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {Event} from '../index';

export default interface PropertyChangeEvent<Value = any, Source = any> extends Event<Source> {
  propertyName: string;
  oldValue: Value;
  newValue: Value;
}