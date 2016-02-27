/**
 * Created by 景舜 on 2016/2/26.
 */

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class JDBCUtil {

    private static String  datebase = "item1";
    private static String  userName = "root";
    private static String  password = "";
    public static final String name = "com.mysql.jdbc.Driver";

    public static Connection getConnection()
    {
        Connection con = null;
        try {
            Class.forName(name); //加载mysq驱动
        } catch (ClassNotFoundException e) {
            System.out.println("驱动加载错误");
            e.printStackTrace();//打印出错详细信息
        }
        try {
            String url = "jdbc:mysql://localhost:3306/"+datebase;
            con = DriverManager.getConnection(url,userName,password);
        } catch (SQLException e) {
            System.out.println("数据库链接错误");
            e.printStackTrace();
        }
//        System.out.println("数据库连接成功");
        return con;
    }

    public static void close(Connection con)
    {
        if(con!=null)
            try {
                con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
//        System.out.println("数据库关闭");
    }
    public static void close(PreparedStatement ps)
    {
        if(ps!=null)
            try {
                ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
    }
    public static void close(ResultSet rs)
    {
        if(rs!=null)
            try {
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
    }

}
