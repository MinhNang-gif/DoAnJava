package Process;

public class UserToken {
    private boolean status;
    private int accountId;
    private String role;

    public UserToken() {
    }

    public UserToken(boolean status, int accountId, String role) {
        this.status = status;
        this.accountId = accountId;
        this.role = role;
    }

    public boolean isStatus() {
        return status;
    }

    public int getAccountId() {
        return accountId;
    }

    public String getRole() {
        return role;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
