package cleb.security;

import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SaltedAuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.realm.jdbc.JdbcRealm;

import cleb.security.dao.UserDAO;
import cleb.security.tables.User;

// TODO add javadoc
public class ClebRealm extends JdbcRealm {

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(
        AuthenticationToken token) {
        // Identify account to log to
        UsernamePasswordToken userPassToken = (UsernamePasswordToken) token;
        final String username = userPassToken.getUsername();

        if (username == null) {
            System.out.println("Username is null.");
            return null;
        }

        // Read password hash and salt from db
        final User user = UserDAO.getUserByEmail(username);

        if (user == null) {
            System.out.println("No account found for user [" + username + "]");
            return null;
        }

        // Return salted credentials
        SaltedAuthenticationInfo info = new ClebSaltedAuthenticationInfo(
            username, user.getPassword(), user.getSalt());

        return info;
    }
}
