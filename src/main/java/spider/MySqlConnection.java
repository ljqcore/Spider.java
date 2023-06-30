package spider;

import javax.swing.*;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.sql.*;

public class MySqlConnection {
    public void insert(String url_,String crawl_time,String sensWord,int freq,String level) throws Exception{
        Connection conn = null;
        try {
            // 加载 JDBC 驱动
            Class.forName("com.mysql.cj.jdbc.Driver");

            // 建立连接
            String url = "jdbc:mysql://localhost:3306/sensword?useSSL=false&serverTimezone=UTC&useUnicode=true&characterEncoding=utf-8";
            String user = "root";
            String password = "123456";
            conn = DriverManager.getConnection(url, user, password);

            // 创建 PreparedStatement
            PreparedStatement ps = conn.prepareStatement("INSERT INTO website_detect_info (url, crawl_time,sensWord,freq,level) VALUES (?, ?,?,?,?)");

            // 设置参数
            ps.setString(1, url_);
            ps.setString(2, crawl_time);
            ps.setString(3, sensWord);
            ps.setInt(4, freq);
            ps.setString(5, level);

            // 执行更新
            ps.executeUpdate();

            // 关闭 PreparedStatement
            ps.close();
        }catch(SQLIntegrityConstraintViolationException e){//主键重复时，异常处理内update记录
            String sql = "update website_detect_info set crawl_time=?,freq=?,level=? where url=? and sensWord=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, crawl_time);
            ps.setInt(2, freq);
            ps.setString(3, level);
            ps.setString(4, url_);
            ps.setString(5, sensWord);

            // 执行更新
            ps.executeUpdate();

            // 关闭 PreparedStatement
            ps.close();
        }catch (DataTruncation e) {//若插入的数据超过varchar长度，发生截断异常
            System.out.println("截断！！！");
            JOptionPane.showMessageDialog(null, "信息过长，存储失败！", "警告", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            //关闭连接
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    // 从数据库中读取数据存入到csv文件中
    public void ExportToCSV() throws Exception{
        // 加载 JDBC 驱动
        Class.forName("com.mysql.cj.jdbc.Driver");
        // 建立连接
        String url = "jdbc:mysql://localhost:3306/sensword?useSSL=false&serverTimezone=UTC&useUnicode=true&characterEncoding=utf-8";
        String user = "root";
        String password = "123456";
        Connection conn = DriverManager.getConnection(url, user, password);

        // 查询语句
        String query = "select * from website_detect_info;";

        //创建CSV文件并写入数据
        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream("output.csv"), StandardCharsets.UTF_8);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query))

        {
            // 写入表头
            for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                writer.append(rs.getMetaData().getColumnName(i));
                if (i < rs.getMetaData().getColumnCount()) {
                    writer.append(",");
                }
                else {
                    writer.append("\n");
                }
            }

            // 写入数据
            while (rs.next()) {
                for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                    writer.append(rs.getString(i));
                    if (i < rs.getMetaData().getColumnCount()) {
                        writer.append(",");
                    }
                    else {
                        writer.append("\n");
                    }
                }
            }
            writer.flush();
        }
        conn.close();
    }


//    public static void main(String[] args) throws Exception {
////        new MySqlConnection().insert("https://blog.csdn.net/pan_junbiao/article/details/89404620","23:10:20","文章",90);
//        new MySqlConnection().insert("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Alit laciniac turpis elit v","2023:08:12 23:45","我",23,"高");
//    }
}