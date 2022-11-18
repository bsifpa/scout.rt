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
import {keys, KeyStroke} from '../../src/index';
import {JQueryTesting} from '../../src/testing/testing-index';

describe('KeyStroke', () => {
  let session: SandboxSession;

  let spyable = () => {
    // nop
  };

  class TestingKeyStroke extends KeyStroke {
    constructor() {
      super();
      this.parseAndSetKeyStroke('enter');
    }

    override handle() {
      expect(false).toBe(true);
      spyable();
    }
  }

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
  });

  afterEach(() => {
    session = null;
  });

  describe('unrepeatability', () => {

    let testImpl = keyDownCount => {
      let keyStroke = new TestingKeyStroke();

      session.desktop.keyStrokeContext.registerKeyStroke(keyStroke);

      spyOn(keyStroke, 'handle');

      while (keyDownCount-- > 0) {
        JQueryTesting.triggerKeyDownCapture(session.desktop.$container, keys.ENTER);
      }
      JQueryTesting.triggerKeyUpCapture(session.desktop.$container, keys.ENTER);

      // @ts-expect-error
      expect(keyStroke.handle.calls.count())
        .toEqual(1, 'because an unrepeatable keystroke should only be invoked once before the closing keyup event');
      // @ts-expect-error
      expect(keyStroke._handleExecuted)
        .toBe(false, 'because an unrepeatable keystroke should be reset after the closing keyup event');
    };

    it('means that an unrepeatable KeyStroke is triggered exactly once per keyup event, even given three keydown events', () => {
      testImpl(3);
    });

    it('means that an unrepeatable KeyStroke is triggered exactly once given the sequence (keydown, keyup)', () => {
      testImpl(1);
    });
  });
});
