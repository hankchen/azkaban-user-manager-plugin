package azkaban.webapp.servlet;

import azkaban.database.AzkabanDataSource;
import azkaban.database.DataSourceUtils;
import azkaban.server.HttpRequestUtils;
import azkaban.server.session.Session;
import azkaban.user.*;
import azkaban.utils.Props;
import azkaban.webapp.AzkabanWebServer;
import org.apache.log4j.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;

/**
 * Created by meng on 2018/4/21.
 */
public class UserManagerServlet extends LoginAbstractAzkabanServlet {
    static final String ERROR_PARAM = "error";
    private static final Logger logger = Logger.getLogger(UserManagerServlet.class.getName());


    private UserManager usermanager;

    private AzkabanDataSource datasource;
    private JdbcUserManager jdbcUM;

    public UserManagerServlet(Props props) {


    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        final AzkabanWebServer server = (AzkabanWebServer) getApplication();
        this.usermanager = server.getUserManager();
        jdbcUM = (JdbcUserManager) usermanager;
        datasource = DataSourceUtils.getDataSource(getApplication().getServerProps());


    }

    @Override
    protected void handleGet(HttpServletRequest request, HttpServletResponse response, Session session) throws ServletException, IOException {
        handleUserListView(request, response, session);


    }

    @Override
    protected void handlePost(HttpServletRequest request, HttpServletResponse response, Session session)
            throws ServletException, IOException {

        if (hasParam(request, "userAction")) {
            final String action = getParam(request, "userAction");

            if (action.equals("createUser")) {
                handleAddUserView(request, response, session);
            } else if (action.equals("deleteUser")) {
                handleDeleteUserView(request, response, session);
            }
        } else {
            handleGet(request, response, session);
        }

    }

    private void handleDeleteUserView(HttpServletRequest req,
                                      HttpServletResponse resp, Session session) throws IOException {
        User user = session.getUser();
        String message = null;
        String status = ERROR_PARAM;
        String action = null;
        HashMap<String, Object> params = new HashMap<String, Object>();
        if (!user.isInGroup("admin")) {
            message = "User " + user.getUserId()
                    + " doesn't have no privilege for user manager.";
            logger.info(message);
            status = ERROR_PARAM;
            params.put(status, message);
        } else {
            try {
                final String username = getParam(req, "userId");
                jdbcUM.deleteUser(username);
                status = "success";

                action = "redirect";
                final String redirect = "usermanager";
                params.put("path", redirect);
                params.put("action", action);
                params.put("status", status);
            } catch (ServletException e) {
                message = e.getMessage();
                status = ERROR_PARAM;
                params.put("status", status);
                params.put("message", message);
            } catch (UserManagerException e) {
                message = e.getMessage();
                status = ERROR_PARAM;
                params.put("status", status);
                params.put("message", message);
            }

        }
        this.writeJSON(resp, params);
    }
        private void handleAddUserView (HttpServletRequest req,
                HttpServletResponse resp, Session session) throws IOException {
            User user = session.getUser();
            String message = null;
            String status = ERROR_PARAM;
            String action = null;
            HashMap<String, Object> params = new HashMap<String, Object>();
            if (!user.isInGroup("admin")) {
                message = "User " + user.getUserId()
                        + " doesn't have no privilege for user manager.";
                logger.info(message);
                status = ERROR_PARAM;
                params.put(status, message);
            } else {
                try {
                    final String username = getParam(req, "userName");
                    final String email = getParam(req, "email");
                    final String password = getParam(req, "passwd");
                    final String roles=getParam(req,"roles");
                    final String agentUser=getParam(req,"proxyuser");
                    logger.info("username:" + username + ";");
                    jdbcUM.addUser(username, email, password,roles,agentUser);
                    status = "success";
                    action = "redirect";
                    final String redirect = "usermanager";
                    params.put("path", redirect);
                    params.put("action", action);
                    params.put("status", status);
                } catch (final Exception e) {
                    message = e.getMessage();
                    status = ERROR_PARAM;
                    params.put("status", status);
                    params.put("message", message);

                }
            }
            this.writeJSON(resp, params);


        }


        private void handleChangePasswdView (HttpServletRequest req,
                HttpServletResponse resp, Session session){
            User user = session.getUser();
            Page page = newPage(req, resp, session, "azkaban/webapp/servlet/velocity/changepasswd.vm");
            if (!user.isInGroup("admin")) {
                page.add("errorMsg", "You have no privilege for user manager!");
            }
            page.render();
        }

        private void handleUserListView (HttpServletRequest req,
                HttpServletResponse resp, Session session){
            User user = session.getUser();
            Page page = newPage(req, resp, session, "azkaban/webapp/servlet/velocity/userlist.vm");
            if (!user.isInGroup("admin")) {
                page.add("errorMsg", "You have no privilege for user manager!");
            } else {
                if (HttpRequestUtils.hasParam(req, "groups")) {
                    page.add("groupList", jdbcUM.getAllGroup());
                } else if (HttpRequestUtils.hasParam(req, "roles")) {
                    page.add("roleList", jdbcUM.getAllRoles());
                } else if (HttpRequestUtils.hasParam(req, "adduser")) {
                    handleChangePasswdView(req, resp, session);
                } else {
                    page.add("userList", jdbcUM.getAllUsers());
                }

            }
            page.render();
        }


    }
