package View;

// Sau khi đăng ký, ngoài việc tạo bản ghi USERS và ACCOUNT, sẽ tự động gán vai trò 'CUSTOMER', sinh MAKH, và tạo bản ghi KHACHHANG liên kết với ACCOUNT_ID vừa tạo.

import ConnectDB.ConnectionOracle;
import Process.RoleGroupConstants;
import Process.User;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.Pattern;
import javax.swing.border.Border;


public class DangKyTaiKhoanKhachHang extends javax.swing.JFrame {
    private static final Color COLOR_BACKGROUND = new Color(240, 245, 250);
    private static final Color COLOR_PANEL_BACKGROUND = Color.WHITE;
    private static final Color COLOR_PRIMARY = new Color(0, 123, 255);
    private static final Color COLOR_PRIMARY_DARK = new Color(0, 105, 217);
    private static final Color COLOR_TEXT_FIELD_BORDER = new Color(200, 200, 200);
    private static final Color COLOR_LABEL = new Color(100, 100, 100);
    private static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 28);
    private static final Font FONT_LABEL = new Font("Segoe UI", Font.PLAIN, 15);
    private static final Font FONT_TEXT_FIELD = new Font("Segoe UI", Font.PLAIN, 15);
    private static final Font FONT_BUTTON = new Font("Segoe UI", Font.BOLD, 15);

    // Thông tin tài khoản
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JPasswordField txtConfirmPassword;
    private JTextField txtFullName; // TENKH và USERS.FULL_NAME
    private JTextField txtEmail;    // EMAIL Khách hàng và USERS.EMAIL

    // Thông tin khách hàng bổ sung
    private JTextField txtSdt;
    private JTextField txtDiaChi;
    // LOAIKH sẽ mặc định là "Vãng lai"

    private JButton btnRegister;
    private JButton btnBackToLogin;

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\d{10,11}$");


    public DangKyTaiKhoanKhachHang() {
        setupLookAndFeel();
        initializeUI();
    }

    private void setupLookAndFeel() {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (UnsupportedLookAndFeelException ex) {
            System.err.println("Lỗi FlatLaf: " + ex.getMessage());
        }
    }

    private void initializeUI() {
        setTitle("Đăng Ký Tài Khoản Khách Hàng");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(650, 750)); // Tăng chiều cao cho thêm trường
        setLocationRelativeTo(null);
        getContentPane().setBackground(COLOR_BACKGROUND);
        setLayout(new GridBagLayout());

        JPanel registrationPanel = new JPanel(new GridBagLayout());
        registrationPanel.setBackground(COLOR_PANEL_BACKGROUND);
        registrationPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 210, 210)),
                new EmptyBorder(40, 50, 40, 50)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        int currentRow = 0;

        JLabel titleLabel = new JLabel("Đăng Ký Khách Hàng Mới", SwingConstants.CENTER);
        titleLabel.setFont(FONT_TITLE);
        titleLabel.setForeground(COLOR_PRIMARY);
        gbc.gridy = currentRow++;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(0, 0, 25, 0);
        registrationPanel.add(titleLabel, gbc);

        gbc.insets = new Insets(0, 0, 18, 0); // Khoảng cách giữa các cặp label-field

        txtUsername = new JTextField(25);
        gbc.gridy = currentRow++;
        registrationPanel.add(createLabelFieldPair("Tên đăng nhập (*):", txtUsername), gbc);

        txtPassword = new JPasswordField(25);
        gbc.gridy = currentRow++;
        registrationPanel.add(createLabelFieldPair("Mật khẩu (*):", txtPassword), gbc);

        txtConfirmPassword = new JPasswordField(25);
        gbc.gridy = currentRow++;
        registrationPanel.add(createLabelFieldPair("Xác nhận mật khẩu (*):", txtConfirmPassword), gbc);

        txtFullName = new JTextField(25);
        gbc.gridy = currentRow++;
        registrationPanel.add(createLabelFieldPair("Họ và tên (*):", txtFullName), gbc);

        txtEmail = new JTextField(25);
        gbc.gridy = currentRow++;
        registrationPanel.add(createLabelFieldPair("Email (*):", txtEmail), gbc);

        txtSdt = new JTextField(25);
        gbc.gridy = currentRow++;
        registrationPanel.add(createLabelFieldPair("Số điện thoại (*):", txtSdt), gbc);

        txtDiaChi = new JTextField(25);
        gbc.gridy = currentRow++;
        gbc.insets = new Insets(0, 0, 30, 0); // Khoảng cách lớn hơn trước nút
        registrationPanel.add(createLabelFieldPair("Địa chỉ:", txtDiaChi), gbc);


        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setOpaque(false);

        btnRegister = new JButton("Đăng Ký");
        styleButton(btnRegister, COLOR_PRIMARY, COLOR_PRIMARY_DARK, new EmptyBorder(12, 50, 12, 50));
        btnRegister.addActionListener(this::registerBtnActionPerformed);
        buttonPanel.add(btnRegister);

        btnBackToLogin = new JButton("Quay Lại Đăng Nhập");
        styleButton(btnBackToLogin, new Color(108, 117, 125), new Color(80, 88, 95), new EmptyBorder(12, 25, 12, 25));
        btnBackToLogin.addActionListener(e -> {
            new DangNhap().setVisible(true);
            this.dispose();
        });
        buttonPanel.add(btnBackToLogin);

        gbc.gridy = currentRow++;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(0, 0, 0, 0);
        registrationPanel.add(buttonPanel, gbc);

        GridBagConstraints frameGbc = new GridBagConstraints();
        frameGbc.anchor = GridBagConstraints.CENTER;
        frameGbc.weightx = 1;
        frameGbc.weighty = 1;
        add(registrationPanel, frameGbc);

        pack();
        // Đảm bảo kích thước tối thiểu sau khi pack
        Dimension packedSize = getSize();
        Dimension minSize = getMinimumSize();
        setSize(Math.max(packedSize.width, minSize.width), Math.max(packedSize.height, minSize.height));
        setLocationRelativeTo(null);
    }

    private JPanel createLabelFieldPair(String labelText, JComponent field) {
        JPanel pairPanel = new JPanel(new BorderLayout(12, 0));
        pairPanel.setOpaque(false);

        JLabel label = new JLabel(labelText);
        label.setFont(FONT_LABEL);
        label.setForeground(COLOR_LABEL);
        label.setPreferredSize(new Dimension(180, label.getPreferredSize().height)); // Tăng độ rộng label

        if (field instanceof JTextField || field instanceof JPasswordField) {
            styleTextField((JTextField) field);
        }
        pairPanel.add(label, BorderLayout.WEST);
        pairPanel.add(field, BorderLayout.CENTER);
        return pairPanel;
    }

    private void styleTextField(JTextField component) {
        component.setFont(FONT_TEXT_FIELD);
        component.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_TEXT_FIELD_BORDER, 1),
                new EmptyBorder(10, 12, 10, 12) // Tăng padding cho textfield
        ));
        component.setPreferredSize(new Dimension(component.getPreferredSize().width, 42)); // Tăng chiều cao textfield
    }

    private void styleButton(JButton button, Color bg, Color hoverBg, Border border) {
        button.setFont(FONT_BUTTON);
        button.setBackground(bg);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(border);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(hoverBg);
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(bg);
            }
        });
    }

    private String hashPassword(String password) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hashedBytes = md.digest(password.getBytes());
        StringBuilder sb = new StringBuilder();
        for (byte b : hashedBytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
     private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Lỗi Nhập Liệu", JOptionPane.ERROR_MESSAGE);
    }

    private boolean validateInputs() {
        if (txtUsername.getText().trim().isEmpty()) { showError("Tên đăng nhập không được để trống."); txtUsername.requestFocus(); return false; }
        if (new String(txtPassword.getPassword()).isEmpty()) { showError("Mật khẩu không được để trống."); txtPassword.requestFocus(); return false; }
        if (!new String(txtPassword.getPassword()).equals(new String(txtConfirmPassword.getPassword()))) {
            showError("Mật khẩu và xác nhận mật khẩu không khớp."); txtConfirmPassword.requestFocus(); return false;
        }
        if (txtFullName.getText().trim().isEmpty()) { showError("Họ và tên không được để trống."); txtFullName.requestFocus(); return false; }
        if (txtEmail.getText().trim().isEmpty() || !EMAIL_PATTERN.matcher(txtEmail.getText().trim()).matches()) {
            showError("Email không hợp lệ."); txtEmail.requestFocus(); return false;
        }
        if (txtSdt.getText().trim().isEmpty() || !PHONE_PATTERN.matcher(txtSdt.getText().trim()).matches()) {
            showError("Số điện thoại không hợp lệ (10-11 chữ số)."); txtSdt.requestFocus(); return false;
        }
        // Địa chỉ có thể không bắt buộc
        return true;
    }


    private void registerBtnActionPerformed(ActionEvent evt) {
        if (!validateInputs()) {
            return;
        }

        Connection con = null;
        try {
            con = ConnectionOracle.getOracleConnection();
            con.setAutoCommit(false);

            // Thông tin từ form
            String username = txtUsername.getText().trim();
            String password = new String(txtPassword.getPassword());
            String fullName = txtFullName.getText().trim(); // Sẽ dùng cho TENKH và USERS.FULL_NAME
            String email = txtEmail.getText().trim();       // Sẽ dùng cho KHACHHANG.EMAIL và USERS.EMAIL
            String sdt = txtSdt.getText().trim();
            String diaChi = txtDiaChi.getText().trim();
            String loaiKh = "Vãng lai"; // Mặc định

            String hashedPassword = hashPassword(password);

            // 1. Tạo User và Account
            User userProcess = new User();
            int addUserResult = userProcess.addUser(username, hashedPassword, fullName, email);

            if (addUserResult <= 0) {
                String msg = (addUserResult == -2000) ? "Tên đăng nhập '" + username + "' đã tồn tại." :
                             (addUserResult == -2001) ? "Email '" + email + "' đã được sử dụng." :
                             "Đăng ký tài khoản thất bại. Mã lỗi không xác định: " + addUserResult;
                showError(msg);
                if (con != null) con.rollback();
                return;
            }

            // 2. Lấy ACCOUNT_ID vừa tạo
            int newAccountId = getAccountIdByUsername(username, con);
            if (newAccountId == -1) {
                showError("Không thể lấy Account ID sau khi tạo tài khoản.");
                if (con != null) con.rollback();
                return;
            }

            // 3. Gán vai trò 'CUSTOMER'
            int customerRoleId = getRoleId(con, RoleGroupConstants.CUSTOMER);
            if (customerRoleId == -1) {
                showError("Không tìm thấy vai trò 'CUSTOMER' trong hệ thống.");
                if (con != null) con.rollback();
                return;
            }
            assignRoleToAccount(newAccountId, customerRoleId, con);

            // 4. Sinh MAKH
            String newMaKh = generateNewMaKh(con);

            // 5. Insert vào KHACHHANG
            String sqlInsertKhachHang = "INSERT INTO KHACHHANG (MAKH, TENKH, SDT, DIACHI, EMAIL, LOAIKH, ACCOUNT_ID) " +
                                        "VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement psKhachHang = con.prepareStatement(sqlInsertKhachHang)) {
                psKhachHang.setString(1, newMaKh);
                psKhachHang.setString(2, fullName); // TENKH
                psKhachHang.setString(3, sdt);
                psKhachHang.setString(4, diaChi);
                psKhachHang.setString(5, email);    // EMAIL trong KHACHHANG
                psKhachHang.setString(6, loaiKh);
                psKhachHang.setInt(7, newAccountId); // Liên kết với ACCOUNT
                psKhachHang.executeUpdate();
            }

            con.commit();
            JOptionPane.showMessageDialog(this,
                    "Đăng ký tài khoản khách hàng thành công!\nMã khách hàng: " + newMaKh + "\nTên đăng nhập: " + username +
                    "\nVui lòng đăng nhập để sử dụng dịch vụ.",
                    "Thành Công", JOptionPane.INFORMATION_MESSAGE);

            new DangNhap().setVisible(true);
            this.dispose();

        } catch (SQLException | ClassNotFoundException | NoSuchAlgorithmException e) {
            if (con != null) { try { con.rollback(); } catch (SQLException exRollback) { exRollback.printStackTrace(); } }
            e.printStackTrace();
            showError("Lỗi hệ thống khi đăng ký: " + e.getMessage());
        } finally {
            if (con != null) { try { con.setAutoCommit(true); con.close(); } catch (SQLException exClose) { exClose.printStackTrace(); } }
        }
    }

    private int getAccountIdByUsername(String username, Connection con) throws SQLException {
        String sql = "SELECT ACCOUNT_ID FROM ACCOUNT WHERE USERNAME = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("ACCOUNT_ID");
            }
        }
        return -1;
    }

    private int getRoleId(Connection con, String roleName) throws SQLException {
        String sql = "SELECT ROLE_GROUP_ID FROM ROLE_GROUP WHERE NAME_ROLE_GROUP = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, roleName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("ROLE_GROUP_ID");
            }
        }
        return -1;
    }

    private void assignRoleToAccount(int accountId, int roleId, Connection con) throws SQLException {
        String checkSql = "SELECT COUNT(*) FROM ACCOUNT_ASSIGN_ROLE_GROUP WHERE ACCOUNT_ID = ? AND ROLE_GROUP_ID = ?";
        try (PreparedStatement psCheck = con.prepareStatement(checkSql)) {
            psCheck.setInt(1, accountId);
            psCheck.setInt(2, roleId);
            try (ResultSet rsCheck = psCheck.executeQuery()) {
                if (rsCheck.next() && rsCheck.getInt(1) > 0) { return; } // Đã gán
            }
        }
        String sql = "INSERT INTO ACCOUNT_ASSIGN_ROLE_GROUP (ACCOUNT_ID, ROLE_GROUP_ID, CREATED_AT, UPDATED_AT, IS_DELETED) " +
                     "VALUES (?, ?, SYSDATE, SYSDATE, 0)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            ps.setInt(2, roleId);
            ps.executeUpdate();
        }
    }

    private String generateNewMaKh(Connection con) throws SQLException {
        String sqlMaxMaKH = "SELECT MAX(TO_NUMBER(SUBSTR(MAKH, 3))) FROM KHACHHANG WHERE MAKH LIKE 'KH%'";
        int nextId = 1;
        try (PreparedStatement ps = con.prepareStatement(sqlMaxMaKH);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                nextId = rs.getInt(1) + 1;
                if (rs.wasNull() || nextId <= 1) { nextId = 1; }
            }
        }
        return String.format("KH%03d", nextId);
    }

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> new DangKyTaiKhoanKhachHang().setVisible(true));
    }


    
    
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents



    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
