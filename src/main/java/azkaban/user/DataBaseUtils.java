package azkaban.user;

import azkaban.database.AzkabanDataSource;
import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;

import java.sql.*;

/**
 * Created by meng on 2018/4/22.
 */
public class DataBaseUtils {
    private static final Logger logger = Logger.getLogger(DataBaseUtils.class);
    public static void executeWithoutResult(Statement statment,String sql){

    }

    public static Object executeWithScalar(Connection conn,String sql) throws SQLException {
        Statement stmt=conn.createStatement();
        Object rs = null;
        ResultSet result=stmt.executeQuery(sql);
        logger.info(sql);
        if(result.next()){
            rs = result.getObject(1);
        }
        return rs;
    }

    public static void closeConnection(Connection conn, PreparedStatement statment, ResultSet result) throws SQLException {

        DbUtils.close(result);
        DbUtils.close(statment);
        DbUtils.close(conn);

    }


}
