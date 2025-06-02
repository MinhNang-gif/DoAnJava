package Process;

import ConnectDB.ConnectionOracle;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class User {
    public int addUser(String username, String passwordHash, String fullname, String email)
                       throws SQLException, ClassNotFoundException {
        // ... (Giữ nguyên code addUser đã hoạt động tốt của bạn)
        int result = 0;
        Connection con = null;
        int newUserId = -1; 

        try {
            con = ConnectionOracle.getOracleConnection();
            con.setAutoCommit(false); 

            String userSqlSeq = "SELECT USER_SEQ.NEXTVAL FROM DUAL";
            try (PreparedStatement psUserSeq = con.prepareStatement(userSqlSeq);
                 ResultSet rsUserSeq = psUserSeq.executeQuery()) {
                if (rsUserSeq.next()) {
                    newUserId = rsUserSeq.getInt(1);
                } else {
                    con.rollback();
                    System.err.println("Lỗi: Không thể tạo USER_ID từ sequence USER_SEQ.");
                    throw new SQLException("Không thể tạo USER_ID từ sequence USER_SEQ.");
                }
            }

            String userSql = "INSERT INTO USERS (USER_ID, FULL_NAME, EMAIL, CREATED_AT, UPDATED_AT, IS_DELETED) " +
                             "VALUES (?, ?, ?, SYSDATE, SYSDATE, 0)";
            try (PreparedStatement psUser = con.prepareStatement(userSql)) {
                psUser.setInt(1, newUserId);
                psUser.setString(2, fullname);
                psUser.setString(3, email);
                psUser.executeUpdate();
            }

            int accountIdToInsert;
            String accountSqlSeq = "SELECT ACCOUNT_SEQ.NEXTVAL FROM DUAL";
            try (PreparedStatement psAccountSeq = con.prepareStatement(accountSqlSeq);
                 ResultSet rsAccountSeq = psAccountSeq.executeQuery()) {
                if (rsAccountSeq.next()) {
                    accountIdToInsert = rsAccountSeq.getInt(1);
                } else {
                    con.rollback();
                    System.err.println("Lỗi: Không thể tạo ACCOUNT_ID từ sequence ACCOUNT_SEQ.");
                    throw new SQLException("Không thể tạo ACCOUNT_ID từ sequence ACCOUNT_SEQ.");
                }
            }

            String accountSql = "INSERT INTO ACCOUNT (ACCOUNT_ID, USER_ID, USERNAME, PASSWORD_HASH, STATUS, CREATED_AT, UPDATED_AT, IS_DELETED) " +
                                "VALUES (?, ?, ?, ?, 'ACTIVE', SYSDATE, SYSDATE, 0)";
            try (PreparedStatement psAccount = con.prepareStatement(accountSql)) {
                psAccount.setInt(1, accountIdToInsert);
                psAccount.setInt(2, newUserId); 
                psAccount.setString(3, username);
                psAccount.setString(4, passwordHash);
                result = psAccount.executeUpdate(); 
            }

            con.commit(); 

        } catch (SQLException e) {
            if (con != null) { try { con.rollback(); } catch (SQLException exRollback) { System.err.println("Lỗi khi rollback: " + exRollback.getMessage()); }}
            if (e.getErrorCode() == 1) { 
                String msg = e.getMessage().toUpperCase();
                // Kiểm tra các tên constraint UNIQUE có thể có cho USERNAME và EMAIL
                if (msg.contains("USERNAME") && (msg.contains("ACCOUNT_USERNAME_UK") || msg.contains("UQ_ACCOUNT_USERNAME") || msg.contains("USERNAME_UNIQUE_CONSTRAINT"))) { 
                    result = -2000; 
                } else if (msg.contains("EMAIL") && (msg.contains("USERS_EMAIL_UK") || msg.contains("UQ_USERS_EMAIL") || msg.contains("EMAIL_UNIQUE_CONSTRAINT") || msg.contains("UNIQUE_USERS_EMAIL"))) { 
                    result = -2001; 
                } else {
                    result = -1; 
                }
                System.err.println("Lỗi unique constraint khi thêm user: " + e.getMessage() + ". Mã trả về: " + result);
            } else {
                System.err.println("SQLException trong addUser: " + e.getMessage());
                e.printStackTrace(); throw e; 
            }
        } catch (ClassNotFoundException cnfe) {
             System.err.println("Không tìm thấy Driver JDBC: " + cnfe.getMessage());
             cnfe.printStackTrace(); throw cnfe;
        } finally {
            if (con != null) { try { con.setAutoCommit(true); con.close(); } catch (SQLException exClose) { System.err.println("Lỗi khi đóng kết nối: " + exClose.getMessage()); }}
        }
        return result; 
    }

    public UserToken loginUser(String loginUsername, String passwordHash) throws SQLException, ClassNotFoundException {
        UserToken ut = new UserToken();
        ut.setStatus(false); 

        String accountSql = "SELECT A.ACCOUNT_ID, A.USER_ID, U.FULL_NAME, U.EMAIL " +
                            "FROM ACCOUNT A JOIN USERS U ON A.USER_ID = U.USER_ID " +
                            "WHERE A.USERNAME = ? AND A.PASSWORD_HASH = ? AND A.STATUS = 'ACTIVE' AND A.IS_DELETED = 0";
        
        Connection con = null;
        try {
            con = ConnectionOracle.getOracleConnection();
            
            try (PreparedStatement psAccount = con.prepareStatement(accountSql)) {
                psAccount.setString(1, loginUsername);
                psAccount.setString(2, passwordHash);
                
                try (ResultSet rsAccount = psAccount.executeQuery()) {
                    if (rsAccount.next()) {
                        int accountId = rsAccount.getInt("ACCOUNT_ID");
                        int userId = rsAccount.getInt("USER_ID");
                        String userFullNameFromUsers = rsAccount.getString("FULL_NAME");
                        String userEmailFromUsers = rsAccount.getString("EMAIL");

                        ut.setAccountId(accountId);
                        ut.setLoginUsername(loginUsername);
                        ut.setEmail(userEmailFromUsers);

                        String roleName = getRoleName(accountId, con);
                        ut.setRole(roleName);

                        if (roleName == null || roleName.isEmpty()){
                             System.err.println("Tài khoản " + loginUsername + " đã xác thực nhưng chưa được gán vai trò hoặc vai trò không hợp lệ.");
                             return ut; 
                        }
                        
                        ut.setStatus(true);

                        if (RoleGroupConstants.CUSTOMER.equals(roleName)) {
                            String khachHangSql = "SELECT MAKH, TENKH FROM KHACHHANG WHERE ACCOUNT_ID = ?";
                            try (PreparedStatement psKH = con.prepareStatement(khachHangSql)) {
                                psKH.setInt(1, accountId);
                                try (ResultSet rsKH = psKH.executeQuery()) {
                                    if (rsKH.next()) {
                                        ut.setEntityId(rsKH.getString("MAKH"));
                                        ut.setEntityFullName(rsKH.getString("TENKH"));
                                    } else {
                                        System.err.println("Cảnh báo: TK CUSTOMER (AccountID: " + accountId + ") không có KHACHHANG tương ứng. Sử dụng thông tin từ USERS.");
                                        ut.setEntityId("KH_TMP_" + accountId); // ID tạm
                                        ut.setEntityFullName(userFullNameFromUsers);
                                    }
                                }
                            }
                        } else if (RoleGroupConstants.EMPLOYEE.equals(roleName)) {
                            String nhanVienSql = "SELECT MANHANVIEN, TENNHANVIEN FROM NHANVIEN WHERE ACCOUNT_ID = ?";
                            try (PreparedStatement psNV = con.prepareStatement(nhanVienSql)) {
                                psNV.setInt(1, accountId);
                                try (ResultSet rsNV = psNV.executeQuery()) {
                                    if (rsNV.next()) {
                                        ut.setEntityId(rsNV.getString("MANHANVIEN"));
                                        ut.setEntityFullName(rsNV.getString("TENNHANVIEN"));
                                    } else {
                                        System.err.println("Cảnh báo: TK EMPLOYEE (AccountID: " + accountId + ") không có NHANVIEN tương ứng. Sử dụng thông tin từ USERS.");
                                        ut.setEntityId("NV_TMP_" + accountId); // ID tạm
                                        ut.setEntityFullName(userFullNameFromUsers);
                                    }
                                }
                            }
                        } else if (RoleGroupConstants.ADMIN.equals(roleName)) {
                            ut.setEntityFullName(userFullNameFromUsers);
                            ut.setEntityId("ADMIN"); // Hoặc một định danh khác
                        } else {
                            // Vai trò không xác định rõ ràng, dùng thông tin chung
                            ut.setEntityFullName(userFullNameFromUsers);
                            ut.setEntityId("USER_" + userId); // Hoặc để trống nếu không có ý nghĩa
                        }
                    }
                }
            }
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException exClose) { System.err.println("Lỗi đóng kết nối loginUser: " + exClose.getMessage()); }}
        }
        return ut;
    }

    public String getRoleName(int accountId, Connection con) throws SQLException {
        String roleSql = "SELECT rg.NAME_ROLE_GROUP " +
                         "FROM ROLE_GROUP rg " +
                         "JOIN ACCOUNT_ASSIGN_ROLE_GROUP ar ON rg.ROLE_GROUP_ID = ar.ROLE_GROUP_ID " +
                         "WHERE ar.ACCOUNT_ID = ? AND ar.IS_DELETED = 0";
        try (PreparedStatement ps = con.prepareStatement(roleSql)) {
            ps.setInt(1, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("NAME_ROLE_GROUP");
                }
            }
        }
        return null;
    }
}