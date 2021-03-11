package azkaban.user;

import azkaban.utils.Pair;
import azkaban.utils.Triple;
import org.apache.commons.dbutils.ResultSetHandler;


import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class JdbcUserHandlerSet {
    public static class UserResultHandler implements ResultSetHandler<List<Triple<Integer,String,User>>>{
        public static String SELECT_USER_BY_NAME_PASSWD =
                "SELECT id, name, password, email, agent_user, roles FROM users WHERE name= ? and  password= ? ";

        public static String SELECT_ALL_USER=
                "SELECT id, name, password, email, agent_user, roles FROM users ";

        public static String SELECT_USER_BY_NAME =
                "SELECT id, name, password, email, agent_user, roles FROM users WHERE name= ? ";


        @Override
        public List<Triple<Integer,String,User>> handle(ResultSet rs) throws SQLException {
            if (!rs.next()) {
                return Collections.emptyList();
            }
            final List<Triple<Integer, String, User>> user = new ArrayList<Triple<Integer, String, User>>();
            do {
                final Integer userId=rs.getInt(1);
                final String agentUser=rs.getString(5);
                User u=new User(rs.getString(2));
                u.setEmail(rs.getString(4));
                if (rs.getString(6)!=null){
                    String[] roles=rs.getString(6).split(",");
                    for(String r: roles){
                        u.addRole(r);
                    }
                }


                user.add(new Triple<Integer, String, User>(userId, agentUser, u));
            } while (rs.next());
            return user;
        }
    }



    public static class GroupResultHandler implements ResultSetHandler<List<Pair<Integer,String>>>{
        public static String SELECT_All_GROUP =
                "SELECT id,name FROM groups";
        public static String SELECT_GROUP_BY_USER =
                "select g.id,g.name from user_groups ug join groups g on ug.group_id=g.id where ug.user_id=?";
        @Override
        public List<Pair<Integer,String>> handle(ResultSet rs) throws SQLException {
            if(!rs.next())
                return Collections.emptyList();
            List<Pair<Integer,String>> groupList = new ArrayList<Pair<Integer,String>>();
            do{
                groupList.add(new Pair<Integer,String>(rs.getInt(1),rs.getString(2)));
            }while (rs.next());
            return groupList;
        }
    }

    public static class RoleResultHandler implements ResultSetHandler<List<Pair<Integer,String>>>{
        public static String SELECT_All_ROLE =
                "SELECT id,name FROM roles";

        public static String SELECT_ROLE_BY_USER=
                "SELECT id,name FROM roles";

        public static String SELECT_ROLE_BY_GROUP=
                "SELECT id,name FROM roles";

        @Override
        public List<Pair<Integer,String>> handle(ResultSet rs) throws SQLException {
            if(!rs.next())
                return Collections.emptyList();
            List<Pair<Integer,String>> roleList = new ArrayList<Pair<Integer,String>>();
            do{
                roleList.add(new Pair<Integer,String>(rs.getInt(1),rs.getString(2)));
            }while (rs.next());
            return roleList;
        }
    }

    public static class ScalarHandlerQuery {
        public static String SELECT_USER_BY_NAME=
                "select name from users where name=?";
        public static String SELECT_GROUP_BY_NAME=
                "select name from groups where name=?";

    }
    public static class UpdateHandler{
        public static String INSERT_USER =
                "insert into users(name,email,password,roles,agent_user) values (?,?, ?,?,?)";

        public static String DELETE_USER=
                "delete from users where name=?";
    }

}
