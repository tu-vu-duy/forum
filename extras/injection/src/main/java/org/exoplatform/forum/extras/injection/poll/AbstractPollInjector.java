package org.exoplatform.forum.extras.injection.poll;

import java.util.HashMap;

import org.exoplatform.services.bench.DataInjector;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

public abstract class AbstractPollInjector extends DataInjector {

  /** . */
  private static Log LOG = ExoLogger.getLogger(AbstractPollInjector.class);
  
  
  @Override
  public Object execute(HashMap<String, String> stringStringHashMap) throws Exception {
    return null;
  }

  @Override
  public void reject(HashMap<String, String> stringStringHashMap) throws Exception {
  }
  
  @Override
  public Log getLog() {
    return ExoLogger.getExoLogger(this.getClass());
  }

}
