package azkaban.user;

import azkaban.database.AzkabanDataSource;
import azkaban.database.DataSourceUtils;
import azkaban.db.DatabaseOperator;
import azkaban.utils.Pair;
import azkaban.utils.Props;
import azkaban.utils.Triple;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.apache.log4j.Logger;
import azkaban.user.JdbcUserHandlerSet.*;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by meng on 2018/4/21.
 */
public class JdbcUserManager implements UserManager {

    private static final Logger logger = Logger.getLogger(JdbcUserManager.class);
    private  DatabaseOperator dbOperator;
    private final AzkabanDataSource dataSource;



    public JdbcUserManager(Props props)  {

        dataSource = DataSourceUtils.getDataSource(props);
        this.dbOperator=new DatabaseOperator(new QueryRunner(dataSource));

    }
    public void initTables() {

    }

    private List<Pair<Integer,String>> getUserGroup(int userId) throws UserManagerException {
        List<Pair<Integer,String>> groupList= Collections.emptyList();
        try {
            groupList=this.dbOperator.query(GroupResultHandler.SELECT_GROUP_BY_USER,new GroupResultHandler(),userId);
        } catch (SQLException e) {
            logger.error("Get group ERROR", e.fillInStackTrace());
            throw new UserManagerException("Get user group error.",e);
        }
        return groupList;
    }
    private List<String> getUserGroupRoles(int userId) throws UserManagerException {

        String sql="select r.name as role_name from user_groups ug join `groups` g on ug.group_id=g.id join group_role gr on g.id=gr.group_id join roles r on gr.role_id = r.id where ug.user_id=" + userId;
        List<String> ls = null;
        return ls;

    }


    public User getUser(String username, String password) throws UserManagerException {
        User user = null;
        UserResultHandler handler=new UserResultHandler();
        if (username == null || username.trim().isEmpty()) {
            throw new UserManagerException("user name is required.");
        } else if (password == null || password.trim().isEmpty()) {
            throw new UserManagerException("password is required");
        }
        try {
           List<Triple<Integer,String,User>> userList= this.dbOperator.query(
                   UserResultHandler.SELECT_USER_BY_NAME_PASSWD,handler,username,stringToMD5(password));
            if (userList.size()==1) {
                Triple<Integer,String,User> u=userList.get(0);
                int userId = u.getFirst();
                user = u.getThird();
                String agentUser=u.getSecond();
                resolveGroupRoles(userId, user);
                user.setPermissions(new User.UserPermissions() {
                    @Override
                    public boolean hasPermission(final String permission) {
                        return true;
                        //TODO
                    }

                    @Override
                    public void addPermission(final String permission) {
                        //TODO
                    }
                });
            }
//            else{
//                throw new UserManagerException("Invalid user name or password.");
//            }


        } catch (SQLException e) {
            logger.error("Get user ERROR", e.fillInStackTrace());
            throw new UserManagerException("Get user ERROR",e);
        }
        return user;
    }


    private void resolveGroupRoles(int userId, User user) {
        try {
            List<Pair<Integer,String>> groupList=this.dbOperator.query(GroupResultHandler.SELECT_GROUP_BY_USER,new GroupResultHandler(),userId);
            for (Pair<Integer,String> g:groupList){
                user.addGroup(g.getSecond());
                logger.info(g.getSecond());
            }

        } catch (SQLException e) {
            logger.error("Get group ERROR", e.fillInStackTrace());
        }
    }

    public void deleteUser(String userName) throws UserManagerException{
        try {
            this.dbOperator.update(UpdateHandler.DELETE_USER,userName);
        } catch (SQLException e) {
            logger.error("Delete user ERROR", e.fillInStackTrace());
            throw new UserManagerException("Delete user error." ,e);
        }
    }


    public void addUser(String userName,String email,String passwd,String roles,String agent) throws UserManagerException {
        try {
            this.dbOperator.update(UpdateHandler.INSERT_USER,userName,email,stringToMD5(passwd),roles,agent);
        } catch (SQLException e) {
            logger.error("Add user ERROR", e.fillInStackTrace());
            throw new UserManagerException("Add user error." ,e);
        }

    }
    public boolean validateUser(String username) {
        Boolean isUser= false;
        try {
            String userName=this.dbOperator.query(ScalarHandlerQuery.SELECT_USER_BY_NAME,new ScalarHandler<String>(),username);
            if (userName!=null){
                isUser=true;
            }
        } catch (SQLException e) {
            logger.error(e.fillInStackTrace());
        }
        return isUser;
    }

    public boolean validateGroup(String group) {
        Boolean isGroup= false;
        try {
            String groupName=this.dbOperator.query(ScalarHandlerQuery.SELECT_GROUP_BY_NAME,new ScalarHandler<String>(),group);
            if (groupName!=null){
                isGroup=true;
            }
        } catch (SQLException e) {
            logger.error(e.fillInStackTrace());
        }
        return isGroup;
    }

    public Role getRole(String roleName) {
        //TODO
        String sql = "select * from roles where role='%s'";
        return new Role("admin",new Permission(Permission.Type.ADMIN));
    }

    public boolean validateProxyUser(String proxyUser, User user) {
        //TODO
        return true;
    }


    public List<User> getAllUsers () {
        List<User> users = new ArrayList<User>();
        final UserResultHandler handler=new UserResultHandler();
        try {
          List<Triple<Integer,String,User>> userSet=  this.dbOperator.query(UserResultHandler.SELECT_ALL_USER,handler);
          for(Triple<Integer,String,User> u: userSet){
              users.add(u.getThird());
          }
        } catch (SQLException e) {
            logger.error(e.getMessage());
//            throw new UserManagerException("Fetch all users error.",e);
        }
        return users;
    }


    public List<String> getAllGroup() {
        List<String> groupList = new ArrayList<String>();
        final GroupResultHandler handler=new GroupResultHandler();
        try {
            List<Pair<Integer,String>> groups=this.dbOperator.query(GroupResultHandler.SELECT_All_GROUP,handler);
            for(Pair<Integer,String> g:groups){
                groupList.add(g.getSecond());
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
//            throw new UserManagerException("Fetch all groups error.",e);
        }
        return groupList;
    }

    public List<String> getAllRoles() {
        List<String> rolesList = new ArrayList<String>();
        final RoleResultHandler handler=new RoleResultHandler();
        try {
            List<Pair<Integer,String>> roles=this.dbOperator.query(RoleResultHandler.SELECT_All_ROLE,handler);
            for(Pair<Integer,String> r:roles){
                rolesList.add(r.getSecond());
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
//            throw new UserManagerException("Fetch all roles error.",e);
        }
        return rolesList;
    }


    public static String stringToMD5(String plainText) {
        byte[] secretBytes = null;
        try {
            secretBytes = MessageDigest.getInstance("md5").digest(
                    plainText.getBytes());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Encrypt error");
        }
        String md5code = new BigInteger(1, secretBytes).toString(16);
        for (int i = 0; i < 32 - md5code.length(); i++) {
            md5code = "0" + md5code;
        }
        return new StringBuffer(md5code).reverse().toString();
    }
}
