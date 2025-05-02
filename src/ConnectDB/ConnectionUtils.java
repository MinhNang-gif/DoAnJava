// Lay ket noi csdl va kiem tra co thanh cong kh
package ConnectDB;

import java.sql.Connection;
import java.sql.SQLException;


public class ConnectionUtils {
    // Lay ket noi csdl
    public static Connection getMyConnection() throws SQLException, ClassNotFoundException {
        return ConnectionOracle.getOracleConnection();
    }
    // Kiem tra co thanh cong kh
    public static void main(String[] args) throws SQLException, ClassNotFoundException {
        System.out.println("Get connection ... ");
        
        // Lấy ra đối tượng Connection kết nối vào database.
        Connection conn = ConnectionUtils.getMyConnection();
        
        System.out.println("Get connection " + conn);
 
        System.out.println("Thanh cong!");
    }
}
