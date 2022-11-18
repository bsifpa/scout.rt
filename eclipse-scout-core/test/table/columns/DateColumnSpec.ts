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
import {DateColumn, dates} from '../../../src/index';
import {TableSpecHelper} from '../../../src/testing/testing-index';

describe('DateColumn', () => {
  let session: SandboxSession;
  let helper: TableSpecHelper;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new TableSpecHelper(session);
    jasmine.Ajax.install();
    jasmine.clock().install();
  });

  afterEach(() => {
    session = null;
    jasmine.Ajax.uninstall();
    jasmine.clock().uninstall();
  });

  describe('format', () => {
    it('updates the value and the display text if the format changes', () => {
      let testDate = dates.create('2017-01-01 13:01');
      let model = helper.createModelSingleColumnByValues([testDate], 'DateColumn');
      let table = helper.createTable(model);
      let column0 = table.columns[0] as DateColumn;
      column0.setFormat(undefined);
      table.render();

      expect(column0.cell(table.rows[0]).text).toBe('01.01.2017');
      expect(column0.cell(table.rows[0]).value).toBe(testDate);

      column0.setFormat('yyyy-MM-dd');
      expect(column0.cell(table.rows[0]).text).toBe('2017-01-01');
    });
  });
});
