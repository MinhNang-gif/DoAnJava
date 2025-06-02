package Process;

public class UserToken {
    private boolean status;
    private int accountId;         // ID từ bảng ACCOUNT
    private String loginUsername;  // Username dùng để đăng nhập (từ bảng ACCOUNT)
    private String role;
    private String entityId;       // Mã định danh thực tế (VD: MANHANVIEN, MAKH)
    private String entityFullName; // Tên đầy đủ thực tế (VD: TENNHANVIEN, TENKH)
    private String email;          // Email của người dùng (lấy từ ACCOUNT hoặc USERS)

    public UserToken() {
    }

    // Getters and Setters
    public boolean isStatus() { return status; }
    public void setStatus(boolean status) { this.status = status; }

    public int getAccountId() { return accountId; }
    public void setAccountId(int accountId) { this.accountId = accountId; }

    public String getLoginUsername() { return loginUsername; }
    public void setLoginUsername(String loginUsername) { this.loginUsername = loginUsername; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getEntityId() { return entityId; }
    public void setEntityId(String entityId) { this.entityId = entityId; }

    public String getEntityFullName() { return entityFullName; }
    public void setEntityFullName(String entityFullName) { this.entityFullName = entityFullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}