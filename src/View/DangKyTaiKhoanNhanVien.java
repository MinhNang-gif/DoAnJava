package View;

import ConnectDB.ConnectionOracle;
import Process.User;
import Process.RoleGroupConstants; // Import RoleGroupConstants
import View.Admin.QuanLyTaiKhoanNhanVien;
import View.DangNhap; // Import DangNhap để điều hướng

import com.formdev.flatlaf.FlatLightLaf;
import com.toedter.calendar.JDateChooser; // Sử dụng JDateChooser cho ngày sinh

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

public class DangKyTaiKhoanNhanVien extends javax.swing.JFrame {
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


    private JTextField txtEmployeeName;
    private JDateChooser dateChooserNgaySinh;
    private JRadioButton radNam, radNu, radKhac;
    private ButtonGroup groupGioiTinh;
    private JTextField txtEmployeeAddress;
    private JTextField txtEmployeePhone;
    private JTextField txtEmployeeEmail; // Email nhân viên (không còn dùng làm username trực tiếp)

    private JButton btnRegisterEmployee;
    private JButton btnBack;

    private JFrame parentFrame;
    private static final String DEFAULT_PASSWORD = "password123";

    public DangKyTaiKhoanNhanVien(JFrame parent) {
        this.parentFrame = parent;
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
        setTitle("Đăng Ký Hồ Sơ Nhân Viên Mới");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(800, 600)); // Kích thước có thể điều chỉnh lại
        setLocationRelativeTo(null);
        getContentPane().setBackground(COLOR_BACKGROUND);
        setLayout(new GridBagLayout());

        JPanel mainPanel = new JPanel(new BorderLayout(20, 20));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(COLOR_PANEL_BACKGROUND);

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        titlePanel.setBackground(COLOR_PANEL_BACKGROUND);
        JLabel titleLabel = new JLabel("Đăng Ký Thông Tin Nhân Viên");
        titleLabel.setFont(FONT_TITLE);
        titleLabel.setForeground(COLOR_PRIMARY);
        titlePanel.add(titleLabel);
        mainPanel.add(titlePanel, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(COLOR_PANEL_BACKGROUND);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Thông tin hồ sơ nhân viên",
                        javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION,
                        new Font("Segoe UI", Font.BOLD, 18), COLOR_PRIMARY),
                new EmptyBorder(15, 25, 15, 25)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);
        int currentRow = 0;

        txtEmployeeName = new JTextField(25);
        currentRow = addLabelAndField(formPanel, gbc, currentRow, "Tên nhân viên (*):", txtEmployeeName);

        txtEmployeeEmail = new JTextField(25);
        // Bỏ "(dùng làm username)" khỏi label
        currentRow = addLabelAndField(formPanel, gbc, currentRow, "Email nhân viên (*):", txtEmployeeEmail);

        dateChooserNgaySinh = new JDateChooser();
        dateChooserNgaySinh.setDateFormatString("dd-MM-yyyy");
        dateChooserNgaySinh.setFont(FONT_TEXT_FIELD);
        currentRow = addLabelAndField(formPanel, gbc, currentRow, "Ngày sinh (*):", dateChooserNgaySinh);

        JPanel genderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        genderPanel.setBackground(COLOR_PANEL_BACKGROUND);
        radNam = new JRadioButton("Nam"); radNam.setFont(FONT_TEXT_FIELD); radNam.setBackground(COLOR_PANEL_BACKGROUND); radNam.setSelected(true);
        radNu = new JRadioButton("Nữ"); radNu.setFont(FONT_TEXT_FIELD); radNu.setBackground(COLOR_PANEL_BACKGROUND);
        radKhac = new JRadioButton("Khác"); radKhac.setFont(FONT_TEXT_FIELD); radKhac.setBackground(COLOR_PANEL_BACKGROUND);
        groupGioiTinh = new ButtonGroup();
        groupGioiTinh.add(radNam); groupGioiTinh.add(radNu); groupGioiTinh.add(radKhac);
        genderPanel.add(radNam); genderPanel.add(radNu); genderPanel.add(radKhac);
        currentRow = addLabelAndField(formPanel, gbc, currentRow, "Giới tính (*):", genderPanel);

        txtEmployeeAddress = new JTextField(25);
        currentRow = addLabelAndField(formPanel, gbc, currentRow, "Địa chỉ:", txtEmployeeAddress);

        txtEmployeePhone = new JTextField(25);
        currentRow = addLabelAndField(formPanel, gbc, currentRow, "Số điện thoại NV:", txtEmployeePhone);

        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setBackground(COLOR_PANEL_BACKGROUND);
        buttonPanel.setBorder(new EmptyBorder(15, 0, 15, 0));

        btnRegisterEmployee = new JButton("Đăng Ký Nhân Viên");
        styleButton(btnRegisterEmployee, COLOR_PRIMARY, COLOR_PRIMARY_DARK);
        btnRegisterEmployee.addActionListener(this::btnRegisterEmployeeActionPerformed);

        btnBack = new JButton("Quay Lại");
        styleButton(btnBack, new Color(108, 117, 125), new Color(80, 88, 95));
        btnBack.addActionListener(e -> {
            if (parentFrame != null) {
                parentFrame.setVisible(true);
                if (parentFrame instanceof QuanLyTaiKhoanNhanVien) {
                    ((QuanLyTaiKhoanNhanVien) parentFrame).loadAccountData("");
                }
            }
            this.dispose();
        });

        buttonPanel.add(btnRegisterEmployee);
        buttonPanel.add(btnBack);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        GridBagConstraints frameGbc = new GridBagConstraints();
        frameGbc.anchor = GridBagConstraints.CENTER;
        frameGbc.weightx = 1;
        frameGbc.weighty = 1;
        frameGbc.fill = GridBagConstraints.BOTH;
        add(mainPanel, frameGbc);

        pack();
        Dimension packedSize = getSize();
        Dimension minSize = getMinimumSize();
        setSize(Math.max(packedSize.width, minSize.width), Math.max(packedSize.height, minSize.height));
        setLocationRelativeTo(null);
    }

    private int addLabelAndField(JPanel panel, GridBagConstraints gbc, int currentRow, String labelText, JComponent field) {
        JLabel label = new JLabel(labelText);
        label.setFont(FONT_LABEL);
        label.setForeground(COLOR_LABEL);
        gbc.gridx = 0;
        gbc.gridy = currentRow;
        gbc.anchor = GridBagConstraints.LINE_END;
        gbc.weightx = 0.35;
        gbc.insets = new Insets(5, 5, 5, 10);
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.weightx = 0.65;
        gbc.insets = new Insets(5, 0, 5, 5);
        styleField(field);
        panel.add(field, gbc);
        return currentRow + 1;
    }

    private void styleField(JComponent component) {
        component.setFont(FONT_TEXT_FIELD);
        Dimension preferredSize = new Dimension(280, 38);

        if (component instanceof JTextField || component instanceof JPasswordField) {
            component.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(COLOR_TEXT_FIELD_BORDER, 1),
                    new EmptyBorder(8, 10, 8, 10)));
            component.setPreferredSize(preferredSize);
        } else if (component instanceof JDateChooser) {
            component.setPreferredSize(new Dimension(200, 38)); // Giữ chiều rộng đã đặt hoặc điều chỉnh
        }
    }

    private void styleButton(JButton button, Color bg, Color bgHover) {
        button.setFont(FONT_BUTTON);
        button.setBackground(bg);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(12, 30, 12, 30));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(bgHover);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(bg);
            }
        });
    }

    // --- Các hàm xử lý Username ---
    private String removeDiacritics(String str) {
        String nfdNormalizedString = Normalizer.normalize(str, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(nfdNormalizedString).replaceAll("");
    }

    private String convertFullNameToUsernameBase(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return "user"; // Default base if name is empty
        }
        String noDiacritics = removeDiacritics(fullName.toLowerCase());
        String[] nameParts = noDiacritics.split("\\s+");
        if (nameParts.length == 0) {
            return "user";
        }
        StringBuilder usernameBase = new StringBuilder();
        // Lấy tên + các chữ cái đầu của họ và tên đệm
        usernameBase.append(nameParts[nameParts.length - 1]); // Tên
        for (int i = 0; i < nameParts.length - 1; i++) {
            if (!nameParts[i].isEmpty()) {
                usernameBase.append(nameParts[i].charAt(0)); // Chữ cái đầu
            }
        }
        // Hoặc đơn giản hơn: nối tất cả các phần không dấu lại
        // for (String part : nameParts) {
        //     usernameBase.append(part);
        // }
        if(usernameBase.length() == 0) return "user"; // Fallback
        return usernameBase.toString().replaceAll("[^a-zA-Z0-9]", ""); // Chỉ giữ lại chữ và số
    }

    private String generateUniqueUsername(String baseUsername, Connection con) throws SQLException {
        String currentUsername = baseUsername;
        int count = 0;
        String sql = "SELECT COUNT(*) FROM ACCOUNT WHERE USERNAME = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            while (true) {
                ps.setString(1, currentUsername);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) == 0) {
                        return currentUsername; // Username này là duy nhất
                    }
                }
                count++;
                currentUsername = baseUsername + count;
            }
        }
    }
    // --- Kết thúc hàm xử lý Username ---


    private String hashPassword(String password) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hashedBytes = md.digest(password.getBytes());
        StringBuilder sb = new StringBuilder();
        for (byte b : hashedBytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\d{10,11}$");

    private boolean validateInputs() {
        if (txtEmployeeName.getText().trim().isEmpty()) { showError("Tên nhân viên không được để trống."); txtEmployeeName.requestFocus(); return false; }
        if (txtEmployeeEmail.getText().trim().isEmpty() || !EMAIL_PATTERN.matcher(txtEmployeeEmail.getText().trim()).matches()) {
            showError("Email nhân viên không hợp lệ."); txtEmployeeEmail.requestFocus(); return false;
        }
        if (dateChooserNgaySinh.getDate() == null) { showError("Ngày sinh không được để trống."); return false; }
        if (dateChooserNgaySinh.getDate().after(new Date())) { showError("Ngày sinh không thể ở tương lai."); return false; }
        if (groupGioiTinh.getSelection() == null) { showError("Vui lòng chọn giới tính."); return false; }
        String empPhone = txtEmployeePhone.getText().trim();
        if (!empPhone.isEmpty() && !PHONE_PATTERN.matcher(empPhone).matches()) { showError("Số điện thoại nhân viên không hợp lệ (phải là 10-11 chữ số)."); txtEmployeePhone.requestFocus(); return false; }
        return true;
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Lỗi Nhập Liệu", JOptionPane.ERROR_MESSAGE);
    }

    private void btnRegisterEmployeeActionPerformed(ActionEvent evt) {
        if (!validateInputs()) {
            return;
        }
        Connection con = null;
        try {
            con = ConnectionOracle.getOracleConnection();
            con.setAutoCommit(false);

            String tenNhanVien = txtEmployeeName.getText().trim();
            String emailNhanVien = txtEmployeeEmail.getText().trim();
            java.util.Date ngaySinhUtil = dateChooserNgaySinh.getDate();
            java.sql.Date ngaySinhSql = new java.sql.Date(ngaySinhUtil.getTime());
            String gioiTinh = radNam.isSelected() ? "NAM" : (radNu.isSelected() ? "NU" : "KHAC");
            String diaChi = txtEmployeeAddress.getText().trim();
            String sdtNhanVien = txtEmployeePhone.getText().trim();

            // Tạo Username từ Tên nhân viên
            String usernameBase = convertFullNameToUsernameBase(tenNhanVien);
            String derivedUsername = generateUniqueUsername(usernameBase, con); // Đảm bảo username duy nhất

            String derivedUserFullName = tenNhanVien;
            String derivedUserEmail = emailNhanVien; // Email cho bảng USERS vẫn là email nhân viên
            String passwordHash = hashPassword(DEFAULT_PASSWORD);

            User userProcess = new User();
            int addUserResult = userProcess.addUser(derivedUsername, passwordHash, derivedUserFullName, derivedUserEmail);

            if (addUserResult <= 0) {
                handleUserAddError(addUserResult, derivedUsername, derivedUserEmail);
                if (con != null) con.rollback();
                return;
            }

            int newAccountId = getAccountIdByUsername(derivedUsername, con);
            if (newAccountId == -1) {
                showError("Không thể lấy Account ID sau khi tạo tài khoản.");
                if (con != null) con.rollback();
                return;
            }

            int employeeRoleId = getEmployeeRoleId(con);
            if (employeeRoleId == -1) {
                showError("Không tìm thấy vai trò 'EMPLOYEE' trong hệ thống.");
                if (con != null) con.rollback();
                return;
            }
            assignRoleToAccount(newAccountId, employeeRoleId, con);

            String newMaNhanVien = generateNewMaNhanVien(con);

            String sqlInsertNhanVien = "INSERT INTO NHANVIEN (MANHANVIEN, TENNHANVIEN, NGAYSINH, GIOITINH, DIACHI, EMAIL, SDT, ACCOUNT_ID) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement psNhanVien = con.prepareStatement(sqlInsertNhanVien)) {
                psNhanVien.setString(1, newMaNhanVien);
                psNhanVien.setString(2, tenNhanVien);
                psNhanVien.setDate(3, ngaySinhSql);
                psNhanVien.setString(4, gioiTinh);
                psNhanVien.setString(5, diaChi);
                psNhanVien.setString(6, emailNhanVien);
                psNhanVien.setString(7, sdtNhanVien);
                psNhanVien.setInt(8, newAccountId);
                psNhanVien.executeUpdate();
            }

            con.commit();
            JOptionPane.showMessageDialog(this,
                    "Đăng ký nhân viên và tài khoản thành công!\nUsername: " + derivedUsername + "\nMật khẩu mặc định: " + DEFAULT_PASSWORD + "\nMã nhân viên: " + newMaNhanVien +
                            "\nVui lòng đổi mật khẩu sau khi đăng nhập lần đầu.",
                    "Thành Công", JOptionPane.INFORMATION_MESSAGE);

            new DangNhap().setVisible(true);
            if (parentFrame != null) parentFrame.dispose();
            this.dispose();

        } catch (SQLException | ClassNotFoundException | NoSuchAlgorithmException e) {
            if (con != null) { try { con.rollback(); } catch (SQLException exRollback) { exRollback.printStackTrace(); } }
            e.printStackTrace();
            showError("Lỗi hệ thống khi đăng ký: " + e.getMessage());
        } finally {
            if (con != null) { try { con.setAutoCommit(true); con.close(); } catch (SQLException exClose) { exClose.printStackTrace(); } }
        }
    }

    private void handleUserAddError(int errorCode, String username, String email) {
        String message;
        switch (errorCode) {
            case -2000: message = "Username '" + username + "' (tạo từ tên nhân viên) đã tồn tại."; break;
            case -2001: message = "Email nhân viên '" + email + "' đã được sử dụng cho một tài khoản khác."; break;
            default: message = "Không thể tạo tài khoản người dùng. Mã lỗi: " + errorCode; break;
        }
        showError(message);
    }

    private int getAccountIdByUsername(String username, Connection con) throws SQLException { /* Giữ nguyên */
        String sql = "SELECT ACCOUNT_ID FROM ACCOUNT WHERE USERNAME = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("ACCOUNT_ID");
            }
        }
        return -1;
    }
    private int getEmployeeRoleId(Connection con) throws SQLException { /* Giữ nguyên */
        String sql = "SELECT ROLE_GROUP_ID FROM ROLE_GROUP WHERE NAME_ROLE_GROUP = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, RoleGroupConstants.EMPLOYEE);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("ROLE_GROUP_ID");
            }
        }
        return -1;
    }
    private void assignRoleToAccount(int accountId, int roleId, Connection con) throws SQLException { /* Giữ nguyên */
        String checkSql = "SELECT COUNT(*) FROM ACCOUNT_ASSIGN_ROLE_GROUP WHERE ACCOUNT_ID = ? AND ROLE_GROUP_ID = ?";
        try (PreparedStatement psCheck = con.prepareStatement(checkSql)) {
            psCheck.setInt(1, accountId);
            psCheck.setInt(2, roleId);
            try (ResultSet rsCheck = psCheck.executeQuery()) {
                if (rsCheck.next() && rsCheck.getInt(1) > 0) {
                    System.out.println("Vai trò " + roleId + " đã được gán cho tài khoản " + accountId + ". Bỏ qua gán lại.");
                    return;
                }
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
    private String generateNewMaNhanVien(Connection con) throws SQLException { /* Giữ nguyên */
        String sqlMaxMaNV = "SELECT MAX(TO_NUMBER(SUBSTR(MANHANVIEN, 3))) FROM NHANVIEN WHERE MANHANVIEN LIKE 'NV%'";
        int nextId = 1;
        try (PreparedStatement ps = con.prepareStatement(sqlMaxMaNV);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                nextId = rs.getInt(1) + 1;
                if (rs.wasNull() || nextId <= 1) {
                    nextId = 1;
                }
            }
        }
        return String.format("NV%03d", nextId);
    }

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            new DangKyTaiKhoanNhanVien(null).setVisible(true);
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
