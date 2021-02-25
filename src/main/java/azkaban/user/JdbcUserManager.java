package azkaban.user;

import azkaban.database.AzkabanDataSource;
import azkaban.database.DataSourceUtils;
import azkaban.utils.Props;
import org.apache.log4j.Logger;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by meng on 2018/4/21.
 */
public class JdbcUserManager implements UserManager {

    private static final Logger logger = Logger.getLogger(JdbcUserManager.class);

    private final AzkabanDataSource datasource;


    public JdbcUserManager(Props props)  {

        datasource = DataSourceUtils.getDataSource(props);

    }




    public User getUser(String username, String password) throws UserManagerException {
        User user = null;
        Connection conn = null;
        PreparedStatement statment = null;
        ResultSet result = null;

        try {
            conn = datasource.getConnection();
            statment = conn.prepareStatement("select * from users where name= ? and password= ?");
            statment.setString(1, username);
            statment.setString(2, password);
            result = statment.executeQuery();

            if (result.next()) {
                int user_id = result.getInt("id");
                user = new User(result.getString("name"));
                user.setEmail(result.getString("email"));
                resolveGroupRoles(user_id, user);
                user.setPermissions(new User.UserPermissions() {
                    @Override
                    public boolean hasPermission(final String permission) {
                        return true;
                    }

                    @Override
                    public void addPermission(final String permission) {
                    }
                });
            }


        } catch (SQLException e) {
            logger.error("Get User ERROR", e.fillInStackTrace());
        } finally {
            try {
                DataBaseUtils.closeConnection(conn, statment, result);
            } catch (SQLException e) {
                logger.error(e.fillInStackTrace());
            }
        }
        return user;
    }


    private void resolveGroupRoles(int user_id, User user) {
        Connection conn = null;
        PreparedStatement statment = null;
        ResultSet result = null;
        try {
            conn = datasource.getConnection();
            statment = conn.prepareStatement("select name from groups where id in(select group_id from user_groups where user_id = ?)");
            statment.setInt(1, user_id);
            result = statment.executeQuery();
            while (result.next()) {
                user.addGroup(result.getString("name"));
            }
        } catch (SQLException e) {
            logger.error("Get Group ERROR", e.fillInStackTrace());
        } finally {
            try {
                DataBaseUtils.closeConnection(conn, statment, result);
            } catch (SQLException e) {
                logger.error(e.fillInStackTrace());
            }
        }
    }



    public void addUser(String userName,String email,String passwd) throws UserManagerException {
        PreparedStatement statment = null;
        ResultSet result = null;
        Connection conn=null;
        try {
            conn=datasource.getConnection();;
            String sql="insert into users(name,email,password) values (?, ?, ?)";
            statment=conn.prepareStatement(sql);
            statment.setString(1,userName);
            statment.setString(2,email);
            statment.setString(3,passwd);
            statment.execute();
        } catch (SQLException e) {
            logger.error("Add User ERROR", e.fillInStackTrace());
            throw new UserManagerException("Add User SQL ERROR :" +e.getMessage());
        } finally {
            try {
                DataBaseUtils.closeConnection(conn, statment, result);
            } catch (Exception e) {
                logger.error(e.fillInStackTrace());
            }
        }

    }
    public boolean validateUser(String username) {
        Boolean rs= false;
        String sql = String.format("select name from users where name='%s' ", username);
        try {
            Connection conn=datasource.getConnection();
            String name = (String) DataBaseUtils.executeWithScalar(conn,sql);
            if (name!=null){
                rs=true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rs;
    }

    public boolean validateGroup(String group) {
        Boolean rs= false;
        String sql = String.format("select name from groups where name='%s' ", group);
        try {
            Connection conn=datasource.getConnection();
            String name = (String) DataBaseUtils.executeWithScalar(conn,sql);
            if (name!=null){
                rs=true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rs;
    }

    public Role getRole(String roleName) {
        //TODO
        String sql = "select * from roles where role='%s'";
        return null;
    }

    public boolean validateProxyUser(String proxyUser, User user) {
        //TODO
        return true;
    }

    public void initTables() {

    }

    public List<User> fatchAllUsers() {
        List<User> users = new ArrayList<User>();
        Connection conn = null;
        PreparedStatement statment = null;
        ResultSet result = null;

        try {
            conn = datasource.getConnection();
            statment = conn.prepareStatement("select * from users");

            result = statment.executeQuery();

            while (result.next()) {
                User user = new User(result.getString("name"));
                user.setEmail(result.getString("email"));
                users.add(user);
            }

        } catch (SQLException e) {
            logger.error("Fatch All User ERROR", e.fillInStackTrace());
        } finally {
            try {
                DataBaseUtils.closeConnection(conn, statment, result);
            } catch (Exception e) {
                logger.error(e.fillInStackTrace());
            }
        }
        return users;
    }

    public List<String> fatchAllGroup() {
        List<String> groupList = new ArrayList<String>();
        Connection conn = null;
        PreparedStatement statment = null;
        ResultSet result = null;

        try {
            conn = datasource.getConnection();
            statment = conn.prepareStatement("select * from groups");
            result = statment.executeQuery();
            while (result.next()) {
                groupList.add(result.getString("name"));
            }
        } catch (SQLException e) {
            logger.error("Fatch All Group ERROR", e.fillInStackTrace());
        } finally {
            try {
                DataBaseUtils.closeConnection(conn, statment, result);
            } catch (Exception e) {
                logger.error(e.fillInStackTrace());
            }
        }
        return groupList;
    }

    public List<String> fetchAllRoles() {
        List<String> rolesList = new ArrayList<String>();
        Connection conn = null;
        PreparedStatement statment = null;
        ResultSet result = null;

        try {
            conn = datasource.getConnection();
            statment = conn.prepareStatement("select * from roles");
            result = statment.executeQuery();
            while (result.next()) {
                rolesList.add(result.getString("name"));
            }
        } catch (SQLException e) {
            logger.error("Fatch All Roles ERROR", e.fillInStackTrace());
        } finally {
            try {
                DataBaseUtils.closeConnection(conn, statment, result);
            } catch (Exception e) {
                logger.error(e.fillInStackTrace());
            }
        }
        return rolesList;
    }
    public static String stringToMD5(String plainText) {
        byte[] secretBytes = null;
        try {
            secretBytes = MessageDigest.getInstance("md5").digest(
                    plainText.getBytes());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("没有这个md5算法！");
        }
        String md5code = new BigInteger(1, secretBytes).toString(16);
        for (int i = 0; i < 32 - md5code.length(); i++) {
            md5code = "0" + md5code;
        }
        return md5code;
    }
}
