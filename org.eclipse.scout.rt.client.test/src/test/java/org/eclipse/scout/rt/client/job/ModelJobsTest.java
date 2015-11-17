/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.job;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.commons.Assertions.AssertionException;
import org.eclipse.scout.commons.IRunnable;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.context.ClientRunContext;
import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.shared.ISession;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@RunWith(PlatformTestRunner.class)
public class ModelJobsTest {

  @Mock
  private IClientSession m_clientSession;

  @Before
  public void before() {
    MockitoAnnotations.initMocks(this);
  }

  @After
  public void after() {
    ISession.CURRENT.remove();
  }

  @Test
  public void testIsModelJob() {
    IClientSession session1 = mock(IClientSession.class);
    IClientSession session2 = mock(IClientSession.class);

    // not a model job (no Future)
    assertFalse(ModelJobs.isModelJob(null));

    // not a model job (no ClientRunContext)
    assertFalse(ModelJobs.isModelJob(Jobs.schedule(mock(IRunnable.class), Jobs.newInput())));

    // not a model job (no ClientRunContext)
    assertFalse(ModelJobs.isModelJob(Jobs.schedule(mock(IRunnable.class), Jobs.newInput()
        .withRunContext(RunContexts.empty()))));

    // not a model job (no mutex and not session on ClientRunContext)
    assertFalse(ModelJobs.isModelJob(Jobs.schedule(mock(IRunnable.class), Jobs.newInput()
        .withRunContext(ClientRunContexts.empty()))));

    // not a model job (no mutex)
    assertFalse(ModelJobs.isModelJob(Jobs.schedule(mock(IRunnable.class), Jobs.newInput()
        .withRunContext(ClientRunContexts.empty().withSession(session1, false)))));

    // not a model job (wrong mutex type)
    assertFalse(ModelJobs.isModelJob(Jobs.schedule(mock(IRunnable.class), Jobs.newInput()
        .withRunContext(ClientRunContexts.empty().withSession(session1, false))
        .withMutex(new Object()))));

    // not a model job (different session on ClientRunContext and mutex)
    assertFalse(ModelJobs.isModelJob(Jobs.schedule(mock(IRunnable.class), Jobs.newInput()
        .withRunContext(ClientRunContexts.empty()
            .withSession(session1, false))
        .withMutex(session2))));

    // not a model job (no session on ClientRunContext)
    assertFalse(ModelJobs.isModelJob(Jobs.schedule(mock(IRunnable.class), Jobs.newInput()
        .withRunContext(ClientRunContexts.empty()
            .withSession(null, false))
        .withMutex(session1))));

    // this is a model job (same session on ClientRunContext and mutex)
    assertTrue(ModelJobs.isModelJob(Jobs.schedule(mock(IRunnable.class), Jobs.newInput()
        .withRunContext(ClientRunContexts.empty()
            .withSession(session1, false))
        .withMutex(session1))));
  }

  @Test
  public void testIsModelThread() {
    final IClientSession clientSession1 = mock(IClientSession.class);
    final IClientSession clientSession2 = mock(IClientSession.class);

    ModelJobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        // Test model thread for same session (1)
        assertTrue(ModelJobs.isModelThread());

        // Test model thread for same session (2)
        ClientRunContexts.copyCurrent().run(new IRunnable() {

          @Override
          public void run() throws Exception {
            assertTrue(ModelJobs.isModelThread());
          }
        });
        // Test model thread for other session
        ClientRunContexts.copyCurrent().withSession(clientSession2, true).run(new IRunnable() {

          @Override
          public void run() throws Exception {
            assertFalse(ModelJobs.isModelThread());
          }
        });
      }
    }, ModelJobs.newInput(ClientRunContexts.empty().withSession(clientSession1, true))).awaitDoneAndGet();
  }

  @Test
  public void testScheduleWithoutInput() {
    ISession.CURRENT.set(m_clientSession);

    // Test schedule
    IFuture<?> actualFuture = ModelJobs.schedule(new Callable<IFuture<?>>() {

      @Override
      public IFuture<?> call() throws Exception {
        return IFuture.CURRENT.get();
      }
    }, ModelJobs.newInput(ClientRunContexts.copyCurrent()))
        .awaitDoneAndGet();

    assertTrue(ModelJobs.isModelJob(actualFuture));

    // Test schedule with delay
    actualFuture = ModelJobs.schedule(new Callable<IFuture<?>>() {

      @Override
      public IFuture<?> call() throws Exception {
        return IFuture.CURRENT.get();
      }
    }, ModelJobs.newInput(ClientRunContexts.copyCurrent())
        .withSchedulingDelay(0, TimeUnit.MILLISECONDS))
        .awaitDoneAndGet();

    assertTrue(ModelJobs.isModelJob(actualFuture));
    assertTrue(actualFuture.getJobInput().getRunContext() instanceof ClientRunContext);
  }

  @Test(expected = AssertionException.class)
  public void testScheduleWithoutInputWithoutSession() {
    ISession.CURRENT.set(null);
    ModelJobs.schedule(mock(IRunnable.class), ModelJobs.newInput(ClientRunContexts.copyCurrent()));
  }

  @Test
  public void testNewInput() {
    ClientRunContext runContext = ClientRunContexts.empty().withSession(m_clientSession, true);

    assertSame(runContext, ModelJobs.newInput(runContext).getRunContext());
    assertEquals("scout-model-thread", ModelJobs.newInput(runContext).getThreadName());
    assertSame(m_clientSession, ModelJobs.newInput(runContext).getMutex());
  }

  @Test(expected = AssertionException.class)
  public void testNewInputNullInput() {
    ModelJobs.newInput(null);
  }

  @Test(expected = AssertionException.class)
  public void testNewInputNullSession() {
    ModelJobs.newInput(ClientRunContexts.empty().withSession(null, true));
  }
}
