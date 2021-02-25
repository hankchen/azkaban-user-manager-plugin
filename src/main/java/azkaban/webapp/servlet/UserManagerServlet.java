package azkaban.webapp.servlet;

import azkaban.database.AzkabanDataSource;
import azkaban.database.DataSourceUtils;
import azkaban.server.HttpRequestUtils;
import azkaban.server.session.Session;
import azkaban.user.*;
import azkaban.utils.Props;
import org.apache.log4j.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
        usermanager = getApplication().getUserManager();
        jdbcUM=(JdbcUserManager) usermanager;
        datasource = DataSourceUtils.getDataSource(getApplication().getServerProps());

    }

    @Override
    protected void handleGet(HttpServletRequest request, HttpServletResponse response, Session session)
            throws ServletException, IOException {
        logger.info("=============handleGet===============");
//        if (HttpRequestUtils.hasParam(request, "adduser")) {
//            handleAddUserView(request, response, session);
//        } else if (HttpRequestUtils.hasParam(request, "passwd")) {
//            handleChangePasswdView(request, response, session);
//        } else {
            handleUserListView(request, response, session);
//        }


    }

    @Override
    protected void handlePost(HttpServletRequest request, HttpServletResponse response, Session session)
            throws ServletException, IOException {
        logger.info("=============handlePost===============");
        if (hasParam(request, "userAction")) {
            final String action = getParam(request, "userAction");

            if (action.equals("createuser")) {
                handleAddUserView(request, response, session);
            }
        }

    }

    private void handleAddUserView(HttpServletRequest req,
                                   HttpServletResponse resp, Session session) throws  IOException {
        User user = session.getUser();
        String message = null;
        String status = ERROR_PARAM;
        String action=null;
        HashMap<String, Object> params = new HashMap<String, Object>();
        if (!user.isInGroup("admin")) {
            message = "User " + user.getUserId()
                            + " doesn't have no privilege for user manager.";
            logger.info(message);
            status = ERROR_PARAM;
            params.put(status,message);
        }


        else {
            try {
                final String username=getParam(req, "userName");
                final String email=getParam(req, "email");
                final String password=getParam(req, "passwd");
                logger.info("username:"+username+";");
                jdbcUM.addUser(username,email,password);
                status = "success";

                action = "redirect";
                final String redirect = "usermanager" ;
                params.put("path", redirect);
                params.put("action",action);
                params.put("status",status);
            } catch (final Exception e) {
                message = e.getMessage();
                status = ERROR_PARAM;
                params.put("status",status);
                params.put("message",message);

            }
        }
        this.writeJSON(resp,params);


    }


    private void handleChangePasswdView(HttpServletRequest req,
                                        HttpServletResponse resp, Session session) {
        User user = session.getUser();
        Page page = newPage(req, resp, session, "azkaban/webapp/servlet/velocity/changepasswd.vm");
        if (!user.isInGroup("admin")) {
            page.add("errorMsg", "You have no privilege for user manager!");
        }
        page.render();
    }

    private void handleUserListView(HttpServletRequest req,
                                    HttpServletResponse resp, Session session) {
        User user = session.getUser();
        Page page = newPage(req, resp, session, "azkaban/webapp/servlet/velocity/userlist.vm");
        if (!user.isInGroup("admin")) {
            page.add("errorMsg", "You have no privilege for user manager!");
        } else {
            if (HttpRequestUtils.hasParam(req, "groups")) {
                page.add("groupList", jdbcUM.fatchAllGroup());
            } else if (HttpRequestUtils.hasParam(req, "roles")) {
                page.add("roleList", jdbcUM.fetchAllRoles());
            }else if(HttpRequestUtils.hasParam(req, "adduser")){
                handleChangePasswdView(req,resp,session);
            }
            else{
                page.add("userList", jdbcUM.fatchAllUsers());
            }

        }
        page.render();
    }





}
