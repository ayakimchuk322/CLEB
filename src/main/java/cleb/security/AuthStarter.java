package cleb.security;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.config.IniSecurityManagerFactory;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.util.Factory;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

/**
 * This utility servlet class initializes
 * <a href="http://shiro.apache.org/">Apache Shiro</a> at server start-up.
 */
public class AuthStarter extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    public void init(ServletConfig config) throws ServletException {
        Factory<SecurityManager> factory = new IniSecurityManagerFactory();

        SecurityManager securityManager = factory.getInstance();

        SecurityUtils.setSecurityManager(securityManager);
    }

}
