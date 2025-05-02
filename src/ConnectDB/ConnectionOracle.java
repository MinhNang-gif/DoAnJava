// Tao ket noi den oracle db va tra ve doi tuong Connection

package ConnectDB;

import java.sql.*;


public class ConnectionOracle {
    public static Connection getOracleConnection() throws ClassNotFoundException, SQLException {
        // Load Driver Oracle de ket noi voi CSDL
        Class.forName("oracle.jdbc.driver.OracleDriver");

        // Tao thong so ket noi Oracle db
        String hostName = "localhost";
        String sid = "orcl";
        String username = "QUANLYXEVABAODUONGXE";
        String password = "Admin123";

        // Tao URL Connection
        String connectionURL = "jdbc:oracle:thin:@" + hostName + ":1521:" + sid;

        // Tao ket noi den csdl
        Connection conn = DriverManager.getConnection(connectionURL, username, password);

//        // Tao cau lenh SQL
//        String sql = "SELECT * FROM KHACHHANG";
//
//        // Thuc thi truy van
//        try (
//            Statement statement = conn.createStatement();
//            ResultSet resultSet = statement.executeQuery(sql);
//        ) {
//            while (resultSet.next()) {
//                String maKh = resultSet.getString("MAKH");
//                String tenKh = resultSet.getString("TENKH");
//                String sdt = resultSet.getString("SDT");
//                String diaChi = resultSet.getString("DIACHI");
//                String email = resultSet.getString("EMAIL");
//                String loaiKh = resultSet.getString("LOAIKH");
//
//                System.out.printf("MAKH: %s | TENKH: %s | SDT: %s | DIACHI: %s | EMAIL: %s | LOAIKH: %s%n", // %d cho so nguyen, %s cho chuoi 
//                            maKh, tenKh, sdt, diaChi, email, loaiKh
//                );
//            }
//        }

        return conn;
    }
}

