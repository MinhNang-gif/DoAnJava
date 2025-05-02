package Process;


import ConnectDB.ConnectionUtils;
import java.awt.List;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import java.sql.SQLException;


public class User {
    public int addUser(String username, String passwordHash, String fullname, String email) throws SQLException, ClassNotFoundException {     
        int result = 0;
        try (Connection con = ConnectionUtils.getMyConnection()) {
            con.setAutoCommit(false);

            // 1. Lấy ACCOUNT_ID từ sequence
            int accountId;
            try (PreparedStatement psSeq = con.prepareStatement("SELECT ACCOUNT_SEQ.NEXTVAL FROM DUAL");
                 ResultSet rs = psSeq.executeQuery()) {
                rs.next();
                accountId = rs.getInt(1);
            }

            // 2. Chèn bản ghi mới vào ACCOUNT
            String sql = ""
                + "INSERT INTO ACCOUNT "
                + "(ACCOUNT_ID, USERNAME, PASSWORD_HASH, FULL_NAME, EMAIL, STATUS) "
                + "VALUES (?, ?, ?, ?, ?, 'ACTIVE')";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, accountId);
                ps.setString(2, username);
                ps.setString(3, passwordHash);
                ps.setString(4, fullname);
                ps.setString(5, email);
                result = ps.executeUpdate();
            }

            con.commit();
        } catch (SQLException e) {
            // unique constraint violated => trùng USERNAME
            if (e.getErrorCode() == 1) {
                result = -2000;
            } else {
                throw e;
            }
        }
        return result;
    }
    
    /*
     * Đăng nhập – trả về UserToken có status, accountId và role (tên ROLE_GROUP)
    */
    public UserToken loginUser(String username, String passwordHash) throws SQLException, ClassNotFoundException {
        UserToken ut = new UserToken();
        String sql = ""
            + "SELECT ACCOUNT_ID "
            + "FROM ACCOUNT "
            + "WHERE USERNAME = ? "
            + "  AND PASSWORD_HASH = ? "
            + "  AND STATUS = 'ACTIVE'";
        try (Connection con = ConnectionUtils.getMyConnection();
            PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, username);
                ps.setString(2, passwordHash);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        int accountId = rs.getInt("ACCOUNT_ID");
                        ut.setStatus(true);
                        ut.setAccountId(accountId);
                        // Lấy role name từ DB và gán vào token
                        String roleName = getRoleName(accountId);
                        ut.setRole(roleName);
                    } else {
                        ut.setStatus(false);
                        ut.setAccountId(0);
                    }
                }
            }
        return ut;
    }
    
    /*
     * Trả về tên ROLE_GROUP đầu tiên mà accountId này được gán
    */
    public String getRoleName(int accountId) throws SQLException, ClassNotFoundException {
        String role = ""
            + "SELECT rg.NAME_ROLE_GROUP "
            + "FROM ROLE_GROUP rg "
            +   "JOIN ACCOUNT_ASSIGN_ROLE_GROUP ar "
            +       "ON rg.ROLE_GROUP_ID = ar.ROLE_GROUP_ID "
            + "WHERE ar.ACCOUNT_ID = ?";
        try (Connection con = ConnectionUtils.getMyConnection(); // Mo ket noi
            PreparedStatement ps = con.prepareStatement(role)) { // Chuan bi cau lenh sql
            ps.setInt(1, accountId); // Dien tham so ? thanh accountId truyen vao
            try (ResultSet rs = ps.executeQuery()) { // Thuc thi truy van
                if (rs.next()) {
                    return rs.getString("NAME_ROLE_GROUP");
                }
            }
        }
        return "";
    }
}
