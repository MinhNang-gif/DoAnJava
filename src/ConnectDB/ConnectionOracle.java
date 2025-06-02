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

        return conn;
    }
}

