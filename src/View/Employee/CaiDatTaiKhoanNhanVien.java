package View.Employee;

import ConnectDB.ConnectionOracle; // File kết nối CSDL của bạn
import Utils.PasswordUtil;     // Lớp tiện ích mật khẩu
import View.DangNhap;            // Để quay lại trang đăng nhập hoặc trang chủ
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class CaiDatTaiKhoanNhanVien extends javax.swing.JFrame {
    private JTextField txtTenNhanVien;
    private JTextField txtNgaySinh; // Định dạng yyyy-MM-dd
    private JComboBox<String> cmbGioiTinh;
    private JTextField txtDiaChi;
    private JTextField txtEmail;
    private JTextField txtSdt;
    private JPasswordField txtMatKhauHienTai;
    private JPasswordField txtMatKhauMoi;
    private JPasswordField txtXacNhanMatKhauMoi;

    private JButton btnLuuThongTin;
    private JButton btnDoiMatKhau;
    private JButton btnQuayLai;

    // Đổi tên biến thành viên để rõ ràng hơn ý nghĩa thực sự của chúng
    private String nhanVienMaSo;      // Sẽ giữ MANHANVIEN (ví dụ: "NV010") được truyền từ EmployeeHomePage
    private String taiKhoanUsername;  // Sẽ giữ ACCOUNT.USERNAME (ví dụ: "nvminhnang01") được truyền từ EmployeeHomePage

    private final Color PRIMARY_COLOR = new Color(0, 123, 255);
    private final Color LABEL_TEXT_COLOR = new Color(50, 50, 50);
    private final Font LABEL_FONT = new Font("Segoe UI", Font.BOLD, 14);
    private final Font TEXT_FIELD_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    // Constructor được cập nhật để phản ánh tên tham số rõ ràng hơn
    public CaiDatTaiKhoanNhanVien(String maNhanVienThucTe, String usernameDangNhap) {
        this.nhanVienMaSo = maNhanVienThucTe; // Lưu MANHANVIEN
        this.taiKhoanUsername = usernameDangNhap; // Lưu ACCOUNT.USERNAME
        dateFormat.setLenient(false);

        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (UnsupportedLookAndFeelException e) {
            System.err.println("Không thể áp dụng FlatLaf LookAndFeel: " + e.getMessage());
        }

        setTitle("Cài Đặt Tài Khoản Nhân Viên - " + this.nhanVienMaSo); // Hiển thị MANHANVIEN trên tiêu đề
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(750, 700);
        setLocationRelativeTo(null);
        setResizable(false);

        initComponentsCustom();
        loadEmployeeData();

        btnLuuThongTin.addActionListener(e -> saveEmployeeDetails());
        btnDoiMatKhau.addActionListener(e -> changePassword());
        btnQuayLai.addActionListener(e -> {
            new EmployeeHomePage().setVisible(true);
            this.dispose();
        });
        
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                new EmployeeHomePage().setVisible(true);
            }
        });
    }

    private void initComponentsCustom() {
        JPanel mainPanel = new JPanel(new BorderLayout(20, 20));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);

        JPanel thongTinPanel = new JPanel(new GridBagLayout());
        thongTinPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Thông tin cá nhân",
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                javax.swing.border.TitledBorder.DEFAULT_POSITION,
                new Font("Segoe UI", Font.BOLD, 16), PRIMARY_COLOR));
        thongTinPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        txtTenNhanVien = createStyledTextField();
        txtNgaySinh = createStyledTextField();
        txtNgaySinh.setToolTipText("Nhập ngày theo định dạng yyyy-MM-dd");
        cmbGioiTinh = new JComboBox<>(new String[]{"NAM", "NU", "KHAC"});
        cmbGioiTinh.setFont(TEXT_FIELD_FONT);
        cmbGioiTinh.setBackground(Color.WHITE);
        txtDiaChi = createStyledTextField();
        txtEmail = createStyledTextField();
        txtSdt = createStyledTextField();

        // SỬA Ở ĐÂY: Hiển thị MANHANVIEN (this.nhanVienMaSo)
        addField(thongTinPanel, gbc, 0, "Mã nhân viên:", new JLabel(this.nhanVienMaSo));
        addField(thongTinPanel, gbc, 1, "Tên nhân viên:", txtTenNhanVien);
        addField(thongTinPanel, gbc, 2, "Ngày sinh (YYYY-MM-DD):", txtNgaySinh);
        addField(thongTinPanel, gbc, 3, "Giới tính:", cmbGioiTinh);
        addField(thongTinPanel, gbc, 4, "Địa chỉ:", txtDiaChi);
        addField(thongTinPanel, gbc, 5, "Email:", txtEmail);
        addField(thongTinPanel, gbc, 6, "Số điện thoại:", txtSdt);

        JPanel matKhauPanel = new JPanel(new GridBagLayout());
        matKhauPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Đổi mật khẩu",
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                javax.swing.border.TitledBorder.DEFAULT_POSITION,
                new Font("Segoe UI", Font.BOLD, 16), PRIMARY_COLOR));
        matKhauPanel.setBackground(Color.WHITE);
        GridBagConstraints gbcPass = new GridBagConstraints();
        gbcPass.fill = GridBagConstraints.HORIZONTAL;
        gbcPass.insets = new Insets(8, 8, 8, 8);
        gbcPass.anchor = GridBagConstraints.WEST;
        
        txtMatKhauHienTai = createStyledPasswordField();
        txtMatKhauMoi = createStyledPasswordField();
        txtXacNhanMatKhauMoi = createStyledPasswordField();

        addField(matKhauPanel, gbcPass, 0, "Mật khẩu hiện tại:", txtMatKhauHienTai);
        addField(matKhauPanel, gbcPass, 1, "Mật khẩu mới:", txtMatKhauMoi);
        addField(matKhauPanel, gbcPass, 2, "Xác nhận mật khẩu mới:", txtXacNhanMatKhauMoi);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setBackground(new Color(240, 240, 240));
        btnLuuThongTin = createStyledButton("Lưu Thông Tin");
        btnDoiMatKhau = createStyledButton("Đổi Mật Khẩu");
        btnQuayLai = createStyledButton("Quay Lại");
        
        buttonPanel.add(btnLuuThongTin);
        buttonPanel.add(btnDoiMatKhau);
        buttonPanel.add(btnQuayLai);

        JPanel centerPanel = new JPanel(new BorderLayout(0, 20));
        centerPanel.setBackground(Color.WHITE);
        centerPanel.add(thongTinPanel, BorderLayout.NORTH);
        centerPanel.add(matKhauPanel, BorderLayout.CENTER);
        
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }

    private JTextField createStyledTextField() {
        JTextField textField = new JTextField(25);
        textField.setFont(TEXT_FIELD_FONT);
        textField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY),
            new EmptyBorder(5, 5, 5, 5)
        ));
        return textField;
    }
    
    private JPasswordField createStyledPasswordField() {
        JPasswordField pwField = new JPasswordField(25);
        pwField.setFont(TEXT_FIELD_FONT);
        pwField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY),
            new EmptyBorder(5, 5, 5, 5)
        ));
        return pwField;
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBackground(PRIMARY_COLOR);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(10, 20, 10, 20));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private void addField(JPanel panel, GridBagConstraints gbc, int y, String labelText, Component component) {
        gbc.gridx = 0;
        gbc.gridy = y;
        gbc.weightx = 0.1; // Điều chỉnh tỉ lệ nếu cần
        JLabel label = new JLabel(labelText);
        label.setFont(LABEL_FONT);
        label.setForeground(LABEL_TEXT_COLOR);
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.9; // Điều chỉnh tỉ lệ nếu cần
        panel.add(component, gbc);
    }

    private void loadEmployeeData() {
        // SỬA Ở ĐÂY: Truy vấn bằng MANHANVIEN (this.nhanVienMaSo)
        String sql = "SELECT TENNHANVIEN, NGAYSINH, GIOITINH, DIACHI, EMAIL, SDT FROM NHANVIEN WHERE MANHANVIEN = ?";
        try (Connection conn = ConnectionOracle.getOracleConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, this.nhanVienMaSo); // Sử dụng MANHANVIEN để truy vấn
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                txtTenNhanVien.setText(rs.getString("TENNHANVIEN"));
                Date ngaySinh = rs.getDate("NGAYSINH");
                if (ngaySinh != null) {
                    txtNgaySinh.setText(dateFormat.format(ngaySinh));
                } else {
                    txtNgaySinh.setText("");
                }
                cmbGioiTinh.setSelectedItem(rs.getString("GIOITINH"));
                txtDiaChi.setText(rs.getString("DIACHI"));
                txtEmail.setText(rs.getString("EMAIL"));
                txtSdt.setText(rs.getString("SDT"));
            } else {
                JOptionPane.showMessageDialog(this, "Không tìm thấy thông tin nhân viên cho Mã NV: " + this.nhanVienMaSo, "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException | ClassNotFoundException e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi tải dữ liệu nhân viên: " + e.getMessage(), "Lỗi SQL", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void saveEmployeeDetails() {
        if (txtTenNhanVien.getText().trim().isEmpty() || 
            txtEmail.getText().trim().isEmpty() ||
            txtSdt.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tên, Email, SĐT không được để trống.", "Lỗi Nhập Liệu", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        java.sql.Date ngaySinhSql = null;
        if (!txtNgaySinh.getText().trim().isEmpty()) {
            try {
                java.util.Date parsedDate = dateFormat.parse(txtNgaySinh.getText().trim());
                ngaySinhSql = new java.sql.Date(parsedDate.getTime());
            } catch (ParseException e) {
                JOptionPane.showMessageDialog(this, "Định dạng ngày sinh không hợp lệ. Vui lòng sử dụng yyyy-MM-dd.", "Lỗi Định Dạng Ngày", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        // SỬA Ở ĐÂY: Cập nhật bằng MANHANVIEN (this.nhanVienMaSo)
        String sql = "UPDATE NHANVIEN SET TENNHANVIEN = ?, NGAYSINH = ?, GIOITINH = ?, DIACHI = ?, EMAIL = ?, SDT = ? WHERE MANHANVIEN = ?";
        try (Connection conn = ConnectionOracle.getOracleConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, txtTenNhanVien.getText().trim());
            if (ngaySinhSql != null) {
                pstmt.setDate(2, ngaySinhSql);
            } else {
                pstmt.setNull(2, Types.DATE);
            }
            pstmt.setString(3, (String) cmbGioiTinh.getSelectedItem());
            pstmt.setString(4, txtDiaChi.getText().trim());
            pstmt.setString(5, txtEmail.getText().trim());
            pstmt.setString(6, txtSdt.getText().trim());
            pstmt.setString(7, this.nhanVienMaSo); // Sử dụng MANHANVIEN để cập nhật

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                JOptionPane.showMessageDialog(this, "Cập nhật thông tin thành công!", "Thông Báo", JOptionPane.INFORMATION_MESSAGE);
                if (DangNhap.currentUserToken != null) {
                    DangNhap.currentUserToken.setEntityFullName(txtTenNhanVien.getText().trim());
                }
            } else {
                JOptionPane.showMessageDialog(this, "Cập nhật thông tin thất bại.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException | ClassNotFoundException e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi cập nhật thông tin: " + e.getMessage(), "Lỗi SQL", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void changePassword() {
        String currentPassword = new String(txtMatKhauHienTai.getPassword());
        String newPassword = new String(txtMatKhauMoi.getPassword());
        String confirmNewPassword = new String(txtXacNhanMatKhauMoi.getPassword());

        if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmNewPassword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin mật khẩu.", "Lỗi Nhập Liệu", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!newPassword.equals(confirmNewPassword)) {
            JOptionPane.showMessageDialog(this, "Mật khẩu mới và xác nhận mật khẩu không khớp.", "Lỗi Nhập Liệu", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // SỬA Ở ĐÂY: Lấy ACCOUNT_ID và PASSWORD_HASH từ bảng ACCOUNT dựa trên ACCOUNT.USERNAME (this.taiKhoanUsername)
        String sqlGetAccount = "SELECT ACCOUNT_ID, PASSWORD_HASH FROM ACCOUNT WHERE USERNAME = ?";
        long accountId = -1;
        String dbPasswordHash = null;

        try (Connection conn = ConnectionOracle.getOracleConnection();
             PreparedStatement pstmtGet = conn.prepareStatement(sqlGetAccount)) {
            
            pstmtGet.setString(1, this.taiKhoanUsername); // Sử dụng ACCOUNT.USERNAME để truy vấn
            ResultSet rs = pstmtGet.executeQuery();

            if (rs.next()) {
                accountId = rs.getLong("ACCOUNT_ID");
                dbPasswordHash = rs.getString("PASSWORD_HASH");
            } else {
                JOptionPane.showMessageDialog(this, "Không tìm thấy tài khoản để đổi mật khẩu (Username: " + this.taiKhoanUsername + ").", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (SQLException | ClassNotFoundException e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi truy vấn thông tin tài khoản: " + e.getMessage(), "Lỗi SQL", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            return;
        }
        
        if (!PasswordUtil.checkPassword(currentPassword, dbPasswordHash)) {
            JOptionPane.showMessageDialog(this, "Mật khẩu hiện tại không đúng.", "Lỗi Xác Thực", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String newHashedPassword = PasswordUtil.hashPassword(newPassword);
        String sqlUpdatePass = "UPDATE ACCOUNT SET PASSWORD_HASH = ?, UPDATED_AT = SYSDATE WHERE ACCOUNT_ID = ?";
        try (Connection conn = ConnectionOracle.getOracleConnection();
             PreparedStatement pstmtUpdate = conn.prepareStatement(sqlUpdatePass)) {
            pstmtUpdate.setString(1, newHashedPassword);
            pstmtUpdate.setLong(2, accountId);

            int affectedRows = pstmtUpdate.executeUpdate();
            if (affectedRows > 0) {
                JOptionPane.showMessageDialog(this, "Đổi mật khẩu thành công!", "Thông Báo", JOptionPane.INFORMATION_MESSAGE);
                txtMatKhauHienTai.setText("");
                txtMatKhauMoi.setText("");
                txtXacNhanMatKhauMoi.setText("");
            } else {
                JOptionPane.showMessageDialog(this, "Đổi mật khẩu thất bại.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException | ClassNotFoundException e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi đổi mật khẩu: " + e.getMessage(), "Lỗi SQL", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
             // Để test, bạn cần đảm bảo DangNhap.currentUserToken được thiết lập đúng
             // Ví dụ:
             // UserToken testToken = new UserToken();
             // testToken.setEntityId("NV001"); // MANHANVIEN
             // testToken.setLoginUsername("nvtest01"); // ACCOUNT.USERNAME
             // testToken.setStatus(true);
             // DangNhap.currentUserToken = testToken;

             if (DangNhap.currentUserToken != null && DangNhap.currentUserToken.isStatus()) {
                // Truyền MANHANVIEN và ACCOUNT.USERNAME
                new CaiDatTaiKhoanNhanVien(DangNhap.currentUserToken.getEntityId(), DangNhap.currentUserToken.getLoginUsername()).setVisible(true);
             } else {
                 System.out.println("Không có token hoặc token không hợp lệ để test CaiDatTaiKhoanNhanVien");
                 // new DangNhap().setVisible(true); 
             }
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
