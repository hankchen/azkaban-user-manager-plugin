package azkaban.user;

import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by meng on 2018/4/22.
 */
public class DataBaseUtils {
    private static final Logger logger = Logger.getLogger(DataBaseUtils.class);

    public static boolean executeWithoutResult(Connection conn,String sql) throws SQLException {
        Statement stmt =conn.createStatement();
        return stmt.execute(sql);
    }

    public static Object executeWithScalar(Connection conn,String sql) throws SQLException {
        Statement stmt=conn.createStatement();
        Object rs = null;
        ResultSet result=stmt.executeQuery(sql);
        logger.info("Exec sql :"+sql);
        if(result.next()){
            rs = result.getObject(1);
        }
        return rs;
    }

    public static  List<String>executeWithList(Connection conn, String sql) throws SQLException{
        List<String> ls=new ArrayList<String>();
        Statement stmt=conn.createStatement();
        ResultSet result=stmt.executeQuery(sql);
        logger.info("Exec sql :"+sql);
        while (result.next()){
            ls.add(result.getString(1));
        }
        return ls;
    }
    public static void closeConnection(Connection conn, PreparedStatement statment, ResultSet result) throws SQLException {

        DbUtils.close(result);
        DbUtils.close(statment);
        DbUtils.close(conn);

    }


}
