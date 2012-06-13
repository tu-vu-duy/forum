package org.exoplatform.forum.extras.injection.faq;

import java.util.HashMap;
import java.util.Random;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.faq.service.Category;
import org.exoplatform.faq.service.FAQNodeTypes;
import org.exoplatform.faq.service.FAQService;
import org.exoplatform.faq.service.Utils;
import org.exoplatform.forum.extras.injection.utils.LoremIpsum4J;
import org.exoplatform.forum.extras.injection.utils.NameGenerator;
import org.exoplatform.ks.common.CommonUtils;
import org.exoplatform.ks.common.jcr.KSDataLocation;
import org.exoplatform.services.bench.DataInjector;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.Query;
import org.exoplatform.services.organization.UserHandler;

abstract public class AbstractFAQInjector extends DataInjector {

  /** . */
  private static Log LOG = ExoLogger.getLogger(AbstractFAQInjector.class);
  
  /** . */
  private final static String DEFAULT_USER_BASE = "bench.user";
  
  /** . */
  private final static String DEFAULT_CATEGORY_BASE = "bench.cat";
  
  /** . */
  protected final static String PASSWORD = "exo";

  /** . */
  protected final static String DOMAIN = "exoplatform.int";
  
  /** . */
  protected String userBase;
  
  /** . */
  protected String categoryBase;
  
  /** . */
  protected final OrganizationService organizationService;

  /** . */
  protected final FAQService faqService;
  
  /** . */
  protected final KSDataLocation locator;

  /** . */
  protected final UserHandler userHandler;
  
  /** . */
  protected int userNumber;
  
  /** . */
  protected int categoryNumber;

  
  /** . */
  protected final Random random;

  /** . */
  protected NameGenerator nameGenerator;

  /** . */
  protected LoremIpsum4J lorem;
  
  /** . */
  private Category categoryRoot  = null;
  
  public AbstractFAQInjector() {
    PortalContainer c = PortalContainer.getInstance();
    this.faqService = (FAQService) c.getComponentInstanceOfType(FAQService.class);
    this.organizationService = (OrganizationService) c.getComponentInstanceOfType(OrganizationService.class);
    this.locator = (KSDataLocation) c.getComponentInstanceOfType(KSDataLocation.class);

    //
    this.userHandler = organizationService.getUserHandler();
    this.nameGenerator = new NameGenerator();
    this.random = new Random();
    this.lorem = new LoremIpsum4J();
  }
  
  public void init(String userPrefix, String categoryPrefix, String forumPrefix, String topicPrefix, String postPrefix, int byteSize) {

    //
    userBase = (userPrefix == null ? DEFAULT_USER_BASE : userPrefix);
    categoryBase = (categoryPrefix == null ? DEFAULT_CATEGORY_BASE : categoryPrefix);

    //
    categoryNumber = 0;

    try {
      userNumber = userNumber(userBase);
      categoryNumber = categoryNumber(categoryBase);
       
    }
    catch (Exception e) {
      // If no user is existing, set keep 0 as value.
    }

    //
    LOG.info("Initial user number : " + userNumber);
    LOG.info("Initial category number : " + categoryNumber);

  }
  
  public int userNumber(String base) throws Exception {
    Query query = new Query();
    query.setUserName(base + "*");
    
    return userHandler.findUsersByQuery(query).getSize();
  }
  
  public int categoryNumber(String base) throws Exception {
    StringBuffer sb = new StringBuffer(FAQNodeTypes.JCR_ROOT);
    sb.append("/").append(locator.getForumCategoriesLocation()).append("/element(*,");
    sb.append(Utils.CATEGORY_HOME).append(")[jcr:like(exo:name, '%").append(base).append("%')]");

    return (int)search(sb.toString()).getSize();
  }
  
  protected Category getCategoryRoot(boolean isUpdate) {
    try {
      if (isUpdate || categoryRoot == null) {
        categoryRoot = faqService.getCategoryById(KSDataLocation.Locations.FAQ_CATEGORIES_HOME);
      }
      return categoryRoot;
    } catch (Exception e) {
      return null;
    }
  } 
  
  protected NodeIterator search(String queryString) throws Exception {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    try {
      QueryManager qm = getFAQHomeNode(sProvider).getSession().getWorkspace().getQueryManager();
      javax.jcr.query.Query query = qm.createQuery(queryString, javax.jcr.query.Query.XPATH);
      QueryResult result = query.execute();
      return result.getNodes();
    } catch (Exception e) {
      LOG.error("Failed to search", e);
    }
    return null;
  }
  
  protected Node getFAQHomeNode(SessionProvider sProvider) throws Exception {
    String path = locator.getForumHomeLocation();
    return locator.getSessionManager().getSession(sProvider).getRootNode().getNode(path);
  }
  
  protected String userName() {
    return userBase + userNumber;
  }
  
  protected String categoryName() {
    return categoryBase + categoryNumber;
  }
  
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
  
  protected int param(HashMap<String, String> params, String name) {

    //
    if (params == null) {
      throw new NullPointerException();
    }

    //
    if (name == null) {
      throw new NullPointerException();
    }

    //
    try {
      String value = params.get(name);
      if (value != null) {
        return Integer.valueOf(value);
      }
    } catch (NumberFormatException e) {
      LOG.warn("Integer number expected for property " + name);
    }
    return 0;
    
  }

}
