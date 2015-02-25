package org.eclipse.scout.net;

import java.io.IOException;
import java.net.URL;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test for {@link NetActivator}
 */
public class NetActivatorTest {
  private static final String TEST_URL = "http://www.eclipse.org";

  private NetModule m_netModule;

  @Before
  public void before() {
    m_netModule.start();
  }

  @After
  public void after() {
    m_netModule.stop();
  }

  /**
   * Test connecting to an url using {@link NetActivator}
   *
   * @throws IOException
   */
  @Test
  public void testNetActivator() throws IOException {
    URL url = new URL(TEST_URL);
    url.openConnection().getInputStream();
  }

}
