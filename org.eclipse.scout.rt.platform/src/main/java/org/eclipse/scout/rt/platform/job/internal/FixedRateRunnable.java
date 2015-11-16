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
package org.eclipse.scout.rt.platform.job.internal;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Runnable to run the given {@link JobFutureTask} periodically at a 'fixed-rate', meaning that if the period is 2
 * seconds that the Runnable is executed every 2 seconds. If an execution takes longer than the period, the subsequent
 * execution starts immediately.
 * <p/>
 * This class is necessary because {@link ScheduledThreadPoolExecutor} is not applicable for {@link JobManager} due to
 * its fixed-size thread pool. That means, that once the <code>core-pool-size</code> is exceeded, the creation of
 * on-demand threads up to a <code>maximum-pool-size</code> would not be supported.
 *
 * @since 5.1
 * @see ScheduledExecutorService#scheduleAtFixedRate(Runnable, long, long, TimeUnit)
 * @see FixedDelayRunnable
 */
class FixedRateRunnable implements Runnable {

  private final DelayedExecutor m_delayedExecutor;
  private final JobFutureTask<?> m_futureTask;
  private final long m_periodNanos;

  public FixedRateRunnable(final DelayedExecutor delayedExecutor, final JobFutureTask<?> futureTask, final long periodMillis) {
    m_delayedExecutor = delayedExecutor;
    m_futureTask = futureTask;
    m_periodNanos = TimeUnit.MILLISECONDS.toNanos(periodMillis);
  }

  @Override
  public void run() {
    // check whether the task is in 'done-state', either due to cancellation or an unhandled exception.
    if (m_futureTask.isDone()) {
      return;
    }

    final long startTimeNanos = System.nanoTime();

    m_futureTask.run();

    // re-schedule the task if still in 'done-state'.
    if (!m_futureTask.isDone()) {
      final long elapsedNanos = System.nanoTime() - startTimeNanos;
      final long remainingNanos = m_periodNanos - elapsedNanos;
      m_delayedExecutor.schedule(this, remainingNanos, TimeUnit.NANOSECONDS); // run the job once the remaining delay is expired.
    }
  }
}
