package View.Customer;

import ConnectDB.ConnectionOracle;
import Process.RoleGroupConstants; // Thêm nếu cần kiểm tra role trong token
import Process.UserToken;       // Thêm để sử dụng UserToken
import Utils.PasswordUtil;
import View.DangNhap;           // Để lấy currentUserToken và điều hướng

import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.Pattern;

public class CaiDatTaiKhoanKhachHang extends javax.swing.JFrame {
    private JTextField txtTenKhachHang;
    private JTextField txtSdt;
    private JTextField txtDiaChi;
    private JTextField txtEmail;
    private JLabel lblLoaiKhachHang;
    private JLabel lblMaKHDisplay;

    private JPasswordField txtMatKhauHienTai;
    private JPasswordField txtMatKhauMoi;
    private JPasswordField txtXacNhanMatKhauMoi;

    private JButton btnLuuThongTin;
    private JButton btnDoiMatKhau;
    private JButton btnQuayLai;

    private String currentMaKhachHang;
    private int currentAccountId = -1;
    private int currentUserId = -1;

    private final Color PRIMARY_COLOR = new Color(0, 123, 255);
    private final Color LABEL_TEXT_COLOR = new Color(50, 50, 50);
    private final Font LABEL_FONT = new Font("Segoe UI", Font.BOLD, 14);
    private final Font TEXT_FIELD_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\d{10,11}$");

    public CaiDatTaiKhoanKhachHang(String maKhachHang) {
        this.currentMaKhachHang = maKhachHang;

        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (UnsupportedLookAndFeelException e) {
            System.err.println("Không thể áp dụng FlatLaf LookAndFeel: " + e.getMessage());
        }

        setTitle("Cài Đặt Tài Khoản - " + (this.currentMaKhachHang != null ? this.currentMaKhachHang : ""));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(780, 720);
        setLocationRelativeTo(null);
        setResizable(false);

        initComponentsCustom();
        
        if (this.currentMaKhachHang != null && !this.currentMaKhachHang.trim().isEmpty()) {
            if(lblMaKHDisplay != null) lblMaKHDisplay.setText(this.currentMaKhachHang);
            loadCustomerDataAndAccountId();
        } else {
            JOptionPane.showMessageDialog(this, "Mã khách hàng không hợp lệ.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            disableFormControls();
        }

        btnLuuThongTin.addActionListener(e -> saveCustomerDetails());
        btnDoiMatKhau.addActionListener(e -> changePassword());
        btnQuayLai.addActionListener(e -> navigateBackToCustomerHome());

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                navigateBackToCustomerHome();
            }
        });
    }
    
    private void disableFormControls(){
        if(btnLuuThongTin != null) btnLuuThongTin.setEnabled(false);
        if(btnDoiMatKhau != null) btnDoiMatKhau.setEnabled(false);
        if(txtTenKhachHang != null) txtTenKhachHang.setEnabled(false);
        if(txtSdt != null) txtSdt.setEnabled(false);
        if(txtDiaChi != null) txtDiaChi.setEnabled(false);
        if(txtEmail != null) txtEmail.setEnabled(false);
        if(txtMatKhauHienTai != null) txtMatKhauHienTai.setEnabled(false);
        if(txtMatKhauMoi != null) txtMatKhauMoi.setEnabled(false);
        if(txtXacNhanMatKhauMoi != null) txtXacNhanMatKhauMoi.setEnabled(false);
    }

    private void navigateBackToCustomerHome() {
        UserToken token = DangNhap.currentUserToken;
        if (token != null && RoleGroupConstants.CUSTOMER.equals(token.getRole())) {
             new CustomerHomePage(token).setVisible(true);
        } else {
            System.out.println("Không tìm thấy UserToken hợp lệ hoặc vai trò không đúng, quay về Đăng Nhập.");
            new DangNhap().setVisible(true);
        }
        this.dispose();
    }

    private void initComponentsCustom() {
        JPanel mainPanel = new JPanel(new BorderLayout(20, 20));
        mainPanel.setBorder(new EmptyBorder(20, 25, 20, 25));
        mainPanel.setBackground(new Color(248,249,250));

        JPanel thongTinPanel = new JPanel(new GridBagLayout());
        thongTinPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Thông tin cá nhân",
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                javax.swing.border.TitledBorder.DEFAULT_POSITION,
                new Font("Segoe UI", Font.BOLD, 17), PRIMARY_COLOR));
        thongTinPanel.setBackground(Color.WHITE);
        GridBagConstraints gbcInfo = new GridBagConstraints();
        gbcInfo.fill = GridBagConstraints.HORIZONTAL;
        gbcInfo.insets = new Insets(10, 10, 10, 10);
        gbcInfo.anchor = GridBagConstraints.WEST;
        int infoRow = 0;

        txtTenKhachHang = createStyledTextField();
        txtSdt = createStyledTextField();
        txtDiaChi = createStyledTextField();
        txtEmail = createStyledTextField();
        lblLoaiKhachHang = new JLabel("Đang tải...");
        lblLoaiKhachHang.setFont(TEXT_FIELD_FONT);
        lblLoaiKhachHang.setForeground(Color.DARK_GRAY);
        
        lblMaKHDisplay = new JLabel(this.currentMaKhachHang != null ? this.currentMaKhachHang : "N/A");
        lblMaKHDisplay.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblMaKHDisplay.setForeground(new Color(33,37,41));

        infoRow = addField(thongTinPanel, gbcInfo, infoRow, "Mã khách hàng:", lblMaKHDisplay);
        infoRow = addField(thongTinPanel, gbcInfo, infoRow, "Tên khách hàng (*):", txtTenKhachHang);
        infoRow = addField(thongTinPanel, gbcInfo, infoRow, "Số điện thoại (*):", txtSdt);
        infoRow = addField(thongTinPanel, gbcInfo, infoRow, "Địa chỉ:", txtDiaChi);
        infoRow = addField(thongTinPanel, gbcInfo, infoRow, "Email (*):", txtEmail);
        infoRow = addField(thongTinPanel, gbcInfo, infoRow, "Loại khách hàng:", lblLoaiKhachHang);

        JPanel matKhauPanel = new JPanel(new GridBagLayout());
        matKhauPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Đổi mật khẩu",
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                javax.swing.border.TitledBorder.DEFAULT_POSITION,
                new Font("Segoe UI", Font.BOLD, 17), PRIMARY_COLOR));
        matKhauPanel.setBackground(Color.WHITE);
        GridBagConstraints gbcPass = new GridBagConstraints();
        gbcPass.fill = GridBagConstraints.HORIZONTAL;
        gbcPass.insets = new Insets(10, 10, 10, 10);
        gbcPass.anchor = GridBagConstraints.WEST;
        int passRow = 0;
        
        txtMatKhauHienTai = createStyledPasswordField();
        txtMatKhauMoi = createStyledPasswordField();
        txtXacNhanMatKhauMoi = createStyledPasswordField();

        passRow = addField(matKhauPanel, gbcPass, passRow, "Mật khẩu hiện tại:", txtMatKhauHienTai);
        passRow = addField(matKhauPanel, gbcPass, passRow, "Mật khẩu mới:", txtMatKhauMoi);
        passRow = addField(matKhauPanel, gbcPass, passRow, "Xác nhận mật khẩu mới:", txtXacNhanMatKhauMoi);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15));
        buttonPanel.setBackground(new Color(240, 240, 240));
        btnLuuThongTin = createStyledButton("Lưu Thông Tin");
        btnDoiMatKhau = createStyledButton("Đổi Mật Khẩu");
        btnQuayLai = createStyledButton("Quay Lại Trang Chủ");
        
        buttonPanel.add(btnLuuThongTin);
        buttonPanel.add(btnDoiMatKhau);
        buttonPanel.add(btnQuayLai);

        JPanel centerPanel = new JPanel(new BorderLayout(0, 25));
        centerPanel.setBackground(new Color(248,249,250));
        centerPanel.add(thongTinPanel, BorderLayout.NORTH);
        centerPanel.add(matKhauPanel, BorderLayout.CENTER);
        
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    private JTextField createStyledTextField() { /* Giữ nguyên */
        JTextField textField = new JTextField(30);
        textField.setFont(TEXT_FIELD_FONT);
        textField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200,200,200), 1),
            new EmptyBorder(8, 10, 8, 10)
        ));
        textField.setMinimumSize(new Dimension(200, 38));
        return textField;
    }
    
    private JPasswordField createStyledPasswordField() { /* Giữ nguyên */
        JPasswordField pwField = new JPasswordField(30);
        pwField.setFont(TEXT_FIELD_FONT);
        pwField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200,200,200), 1),
            new EmptyBorder(8, 10, 8, 10)
        ));
        pwField.setMinimumSize(new Dimension(200, 38));
        return pwField;
    }

    private JButton createStyledButton(String text) { /* Giữ nguyên */
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 15));
        button.setBackground(PRIMARY_COLOR);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(12, 25, 12, 25));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }
    
    private int addField(JPanel panel, GridBagConstraints gbc, int currentRow, String labelText, Component component) {
        // ... (Giữ nguyên như phiên bản trước, đã sửa)
        gbc.gridx = 0;
        gbc.gridy = currentRow;
        gbc.weightx = 0.3;
        gbc.anchor = GridBagConstraints.LINE_END;
        JLabel label = new JLabel(labelText);
        label.setFont(LABEL_FONT);
        label.setForeground(LABEL_TEXT_COLOR);
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        gbc.anchor = GridBagConstraints.LINE_START;
        panel.add(component, gbc);
        return currentRow + 1;
    }

    private void loadCustomerDataAndAccountId() { /* Giữ nguyên logic */
        // ... (như code trước)
        if (this.currentMaKhachHang == null || this.currentMaKhachHang.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Mã khách hàng không được cung cấp.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            disableFormControls(); return;
        }
        if(lblMaKHDisplay != null) lblMaKHDisplay.setText(this.currentMaKhachHang);

        String sql = "SELECT kh.TENKH, kh.SDT, kh.DIACHI, kh.EMAIL AS KHACHHANG_EMAIL, kh.LOAIKH, kh.ACCOUNT_ID, acc.USER_ID " +
                     "FROM KHACHHANG kh " +
                     "LEFT JOIN ACCOUNT acc ON kh.ACCOUNT_ID = acc.ACCOUNT_ID " +
                     "WHERE kh.MAKH = ?";
        try (Connection conn = ConnectionOracle.getOracleConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, this.currentMaKhachHang);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                txtTenKhachHang.setText(rs.getString("TENKH"));
                txtSdt.setText(rs.getString("SDT"));
                txtDiaChi.setText(rs.getString("DIACHI"));
                txtEmail.setText(rs.getString("KHACHHANG_EMAIL"));
                lblLoaiKhachHang.setText(rs.getString("LOAIKH") != null ? rs.getString("LOAIKH") : "Chưa xác định");
                this.currentAccountId = rs.getInt("ACCOUNT_ID");
                if (rs.wasNull()) this.currentAccountId = -1;
                this.currentUserId = rs.getInt("USER_ID");
                if (rs.wasNull()) this.currentUserId = -1;
                btnLuuThongTin.setEnabled(true);
                btnDoiMatKhau.setEnabled(this.currentAccountId != -1);
                if (this.currentAccountId == -1) System.out.println("Khách hàng " + this.currentMaKhachHang + " chưa liên kết với tài khoản đăng nhập.");
            } else {
                JOptionPane.showMessageDialog(this, "Không tìm thấy thông tin cho Mã Khách Hàng: " + this.currentMaKhachHang, "Lỗi", JOptionPane.ERROR_MESSAGE);
                disableFormControls();
            }
        } catch (SQLException | ClassNotFoundException e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi tải dữ liệu khách hàng: " + e.getMessage(), "Lỗi SQL", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace(); disableFormControls();
        }
    }
    
    private void saveCustomerDetails() { /* Giữ nguyên logic */
        // ... (như code trước)
        String tenKH = txtTenKhachHang.getText().trim();
        String sdt = txtSdt.getText().trim();
        String diaChi = txtDiaChi.getText().trim();
        String email = txtEmail.getText().trim();

        if (tenKH.isEmpty() || email.isEmpty() || sdt.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tên, Email, SĐT không được để trống.", "Lỗi Nhập Liệu", JOptionPane.WARNING_MESSAGE); return;
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            JOptionPane.showMessageDialog(this, "Email không hợp lệ.", "Lỗi Nhập Liệu", JOptionPane.WARNING_MESSAGE); txtEmail.requestFocus(); return;
        }
        if (!PHONE_PATTERN.matcher(sdt).matches()) {
            JOptionPane.showMessageDialog(this, "Số điện thoại không hợp lệ (10-11 chữ số).", "Lỗi Nhập Liệu", JOptionPane.WARNING_MESSAGE); txtSdt.requestFocus(); return;
        }

        Connection conn = null;
        try {
            conn = ConnectionOracle.getOracleConnection();
            conn.setAutoCommit(false);
            String sqlUpdateKH = "UPDATE KHACHHANG SET TENKH = ?, SDT = ?, DIACHI = ?, EMAIL = ? WHERE MAKH = ?";
            try (PreparedStatement pstmtKH = conn.prepareStatement(sqlUpdateKH)) {
                pstmtKH.setString(1, tenKH); pstmtKH.setString(2, sdt); pstmtKH.setString(3, diaChi);
                pstmtKH.setString(4, email); pstmtKH.setString(5, this.currentMaKhachHang);
                pstmtKH.executeUpdate();
            }
            if (this.currentUserId != -1) {
                String sqlUpdateUsers = "UPDATE USERS SET FULL_NAME = ?, EMAIL = ?, UPDATED_AT = SYSDATE WHERE USER_ID = ?";
                try (PreparedStatement pstmtUsers = conn.prepareStatement(sqlUpdateUsers)) {
                    pstmtUsers.setString(1, tenKH); pstmtUsers.setString(2, email); pstmtUsers.setInt(3, this.currentUserId);
                    pstmtUsers.executeUpdate();
                }
                 // Cập nhật UserToken nếu tên hoặc email chính của USERS thay đổi
                if (DangNhap.currentUserToken != null && DangNhap.currentUserToken.getAccountId() == this.currentAccountId) {
                    DangNhap.currentUserToken.setEntityFullName(tenKH); // Nếu TENKH là tên chính
                    // DangNhap.currentUserToken.setEmail(email); // Chỉ cập nhật nếu email này là email login chính
                }

            } else System.out.println("Thông tin: Khách hàng " + this.currentMaKhachHang + " không có USER_ID liên kết, không đồng bộ bảng USERS.");
            conn.commit();
            JOptionPane.showMessageDialog(this, "Cập nhật thông tin thành công!", "Thông Báo", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException | ClassNotFoundException e) {
            if (conn != null) { try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); } }
             if (e instanceof SQLException && ((SQLException) e).getErrorCode() == 1) {
                 String msg = ((SQLException) e).getMessage().toUpperCase();
                 if ((msg.contains("USERS") && (msg.contains("EMAIL_UK") || msg.contains("UQ_USERS_EMAIL") || msg.contains(".ACCOUNT_EMAIL_UK") || msg.contains("UNIQUE_USERS_EMAIL") || msg.contains("SYS_C"))) || 
                     (msg.contains("KHACHHANG") && (msg.contains("EMAIL_UNIQUE") || msg.contains("UQ_KHACHHANG_EMAIL") || msg.contains("SYS_C") || msg.contains("UNIQUE_KH_EMAIL")))) {
                     JOptionPane.showMessageDialog(this, "Email '" + email + "' đã được sử dụng.", "Lỗi Trùng Email", JOptionPane.ERROR_MESSAGE);
                 } else if (msg.contains("KHACHHANG") && (msg.contains("SDT_UNIQUE") || msg.contains("UQ_KHACHHANG_SDT") || msg.contains("SYS_C") || msg.contains("UNIQUE_KH_SDT"))) {
                     JOptionPane.showMessageDialog(this, "Số điện thoại '" + sdt + "' đã được sử dụng.", "Lỗi Trùng SĐT", JOptionPane.ERROR_MESSAGE);
                 } else { JOptionPane.showMessageDialog(this, "Lỗi khi cập nhật thông tin (DB constraint): " + e.getMessage(), "Lỗi SQL", JOptionPane.ERROR_MESSAGE); }
            } else { JOptionPane.showMessageDialog(this, "Lỗi khi cập nhật thông tin: " + e.getMessage(), "Lỗi Hệ Thống", JOptionPane.ERROR_MESSAGE); }
            e.printStackTrace();
        } finally {
            if (conn != null) { try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ex) { ex.printStackTrace(); } }
        }
    }

    private void changePassword() {
        // ... (Giữ nguyên logic, đảm bảo PasswordUtil.java đã được thống nhất)
        if (this.currentAccountId == -1) {
            JOptionPane.showMessageDialog(this, "Không thể đổi mật khẩu: Khách hàng chưa được liên kết với tài khoản hệ thống.", "Lỗi", JOptionPane.ERROR_MESSAGE); return;
        }
        String currentPasswordInput = new String(txtMatKhauHienTai.getPassword());
        String newPassword = new String(txtMatKhauMoi.getPassword());
        String confirmNewPassword = new String(txtXacNhanMatKhauMoi.getPassword());

        if (currentPasswordInput.isEmpty() || newPassword.isEmpty() || confirmNewPassword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin mật khẩu.", "Lỗi Nhập Liệu", JOptionPane.WARNING_MESSAGE); return;
        }
        if (!newPassword.equals(confirmNewPassword)) {
            JOptionPane.showMessageDialog(this, "Mật khẩu mới và xác nhận mật khẩu không khớp.", "Lỗi Nhập Liệu", JOptionPane.WARNING_MESSAGE); return;
        }
        if (newPassword.length() < 6) {
            JOptionPane.showMessageDialog(this, "Mật khẩu mới phải có ít nhất 6 ký tự.", "Lỗi Nhập Liệu", JOptionPane.WARNING_MESSAGE); return;
        }

        String dbPasswordHash = null;
        String sqlGetPass = "SELECT PASSWORD_HASH FROM ACCOUNT WHERE ACCOUNT_ID = ?";
        try (Connection conn = ConnectionOracle.getOracleConnection(); PreparedStatement pstmtGet = conn.prepareStatement(sqlGetPass)) {
            pstmtGet.setInt(1, this.currentAccountId);
            ResultSet rs = pstmtGet.executeQuery();
            if (rs.next()) dbPasswordHash = rs.getString("PASSWORD_HASH");
            else { JOptionPane.showMessageDialog(this, "Không tìm thấy tài khoản để xác thực.", "Lỗi", JOptionPane.ERROR_MESSAGE); return; }
        } catch (SQLException | ClassNotFoundException e) {
            JOptionPane.showMessageDialog(this, "Lỗi truy vấn mật khẩu: " + e.getMessage(), "Lỗi SQL", JOptionPane.ERROR_MESSAGE); e.printStackTrace(); return;
        }

        if (dbPasswordHash == null || !PasswordUtil.checkPassword(currentPasswordInput, dbPasswordHash)) {
            JOptionPane.showMessageDialog(this, "Mật khẩu hiện tại không đúng.", "Lỗi Xác Thực", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String newHashedPassword = PasswordUtil.hashPassword(newPassword);
        String sqlUpdatePass = "UPDATE ACCOUNT SET PASSWORD_HASH = ?, UPDATED_AT = SYSDATE WHERE ACCOUNT_ID = ?";
        try (Connection conn = ConnectionOracle.getOracleConnection(); PreparedStatement pstmtUpdate = conn.prepareStatement(sqlUpdatePass)) {
            pstmtUpdate.setString(1, newHashedPassword);
            pstmtUpdate.setInt(2, this.currentAccountId);
            if (pstmtUpdate.executeUpdate() > 0) {
                JOptionPane.showMessageDialog(this, "Đổi mật khẩu thành công!", "Thông Báo", JOptionPane.INFORMATION_MESSAGE);
                txtMatKhauHienTai.setText(""); txtMatKhauMoi.setText(""); txtXacNhanMatKhauMoi.setText("");
            } else { JOptionPane.showMessageDialog(this, "Đổi mật khẩu thất bại (không có dòng nào được cập nhật).", "Lỗi", JOptionPane.ERROR_MESSAGE); }
        } catch (SQLException | ClassNotFoundException e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi đổi mật khẩu trong CSDL: " + e.getMessage(), "Lỗi SQL", JOptionPane.ERROR_MESSAGE); e.printStackTrace();
        }
    }

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            UserToken testToken = new UserToken();
            testToken.setRole(RoleGroupConstants.CUSTOMER);
            // ... có thể set các giá trị khác cho testToken để test
            DangNhap.currentUserToken = testToken; 
            new CaiDatTaiKhoanKhachHang("KH008").setVisible(true); // Test với MAKH cụ thể
        });
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
