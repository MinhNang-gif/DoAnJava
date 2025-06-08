package View;

import View.Admin.AdminHomePage;
import Process.User;
import Process.UserToken;
import View.Customer.CustomerHomePage;
import View.Employee.EmployeeHomePage;
import Process.RoleGroupConstants;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import javax.swing.border.EmptyBorder;
import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;


public class DangNhap extends javax.swing.JFrame {
    // Định nghĩa màu sắc và font chữ
    private static final Color COLOR_BACKGROUND = new Color(240, 245, 250);
    private static final Color COLOR_PANEL_BACKGROUND = Color.WHITE;
    private static final Color COLOR_PRIMARY = new Color(0, 123, 255);
    private static final Color COLOR_PRIMARY_DARK = new Color(0, 105, 217);
    private static final Color COLOR_TEXT_FIELD_BORDER = new Color(200, 200, 200);
    private static final Color COLOR_TEXT_FIELD_BORDER_FOCUS = COLOR_PRIMARY;
    private static final Color COLOR_LABEL = new Color(100, 100, 100);
    private static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 30); // Font to hơn
    private static final Font FONT_LABEL = new Font("Segoe UI", Font.PLAIN, 16); // Font to hơn
    private static final Font FONT_TEXT_FIELD = new Font("Segoe UI", Font.PLAIN, 16); // Font to hơn
    private static final Font FONT_BUTTON = new Font("Segoe UI", Font.BOLD, 16); // Font to hơn

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField; // **THÊM TRƯỜNG NHẬP LẠI MẬT KHẨU**
    private JButton loginButton;
    private JButton registerButton;

    public static UserToken currentUserToken;

    public DangNhap() {
        setupLookAndFeel();
        initializeUI();
    }

    private void setupLookAndFeel() {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (UnsupportedLookAndFeelException ex) {
            System.err.println("Failed to initialize LaF. Using default.");
        }
    }

    private void initializeUI() {
        setTitle("Đăng Nhập Hệ Thống");
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(550, 650)); // **FORM TO HƠN**
        setLocationRelativeTo(null);
        getContentPane().setBackground(COLOR_BACKGROUND);
        setLayout(new GridBagLayout());

        JPanel loginPanel = new JPanel(new GridBagLayout());
        loginPanel.setBackground(COLOR_PANEL_BACKGROUND);
        loginPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 210, 210)),
                new EmptyBorder(50, 60, 50, 60) // **PADDING LỚN HƠN**
        ));
        // loginPanel.setPreferredSize(new Dimension(450, 580)); // Có thể set nếu muốn cố định panel con

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; // Tất cả các thành phần chính sẽ nằm ở cột 0
        gbc.gridwidth = 2; // Cho phép các thành phần chính chiếm 2 cột logic (nếu cần cho label + field)
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER; // Căn giữa các thành phần trong loginPanel
        int currentRow = 0;

        // Tiêu đề
        JLabel titleLabel = new JLabel("Đăng Nhập", SwingConstants.CENTER);
        titleLabel.setFont(FONT_TITLE);
        titleLabel.setForeground(COLOR_PRIMARY);
        gbc.gridy = currentRow++;
        gbc.insets = new Insets(0, 0, 35, 0); // Khoảng cách dưới tiêu đề
        loginPanel.add(titleLabel, gbc);

        // Tên đăng nhập
        gbc.gridwidth = 1; // Reset gridwidth cho label và field
        gbc.anchor = GridBagConstraints.WEST; // Căn label sang trái
        gbc.insets = new Insets(10, 0, 5, 5); // top, left, bottom, right
        gbc.gridy = currentRow;
        JLabel userLabel = new JLabel("Tên đăng nhập:");
        userLabel.setFont(FONT_LABEL);
        userLabel.setForeground(COLOR_LABEL);
        loginPanel.add(userLabel, gbc);

        gbc.gridx = 1; // Field ở cột 1
        gbc.insets = new Insets(10, 0, 5, 0);
        gbc.weightx = 1.0; // Cho phép field giãn ra
        usernameField = new JTextField(); // Độ dài ưu tiên sẽ được quyết định bởi layout và preferredSize
        styleTextField(usernameField);
        addFocusBorder(usernameField);
        loginPanel.add(usernameField, gbc);
        currentRow++;

        // Mật khẩu
        gbc.gridx = 0; // Label ở cột 0
        gbc.gridy = currentRow;
        gbc.weightx = 0; // Reset weightx cho label
        gbc.insets = new Insets(10, 0, 5, 5);
        JLabel passLabel = new JLabel("Mật khẩu:");
        passLabel.setFont(FONT_LABEL);
        passLabel.setForeground(COLOR_LABEL);
        loginPanel.add(passLabel, gbc);

        gbc.gridx = 1; // Field ở cột 1
        gbc.insets = new Insets(10, 0, 5, 0);
        gbc.weightx = 1.0;
        passwordField = new JPasswordField();
        styleTextField(passwordField);
        addFocusBorder(passwordField);
        loginPanel.add(passwordField, gbc);
        currentRow++;

        // **NHẬP LẠI MẬT KHẨU**
        gbc.gridx = 0; // Label ở cột 0
        gbc.gridy = currentRow;
        gbc.weightx = 0;
        gbc.insets = new Insets(10, 0, 5, 5);
        JLabel confirmPassLabel = new JLabel("Nhập lại mật khẩu:");
        confirmPassLabel.setFont(FONT_LABEL);
        confirmPassLabel.setForeground(COLOR_LABEL);
        loginPanel.add(confirmPassLabel, gbc);

        gbc.gridx = 1; // Field ở cột 1
        gbc.insets = new Insets(10, 0, 5, 0);
        gbc.weightx = 1.0;
        confirmPasswordField = new JPasswordField();
        styleTextField(confirmPasswordField);
        addFocusBorder(confirmPasswordField);
        loginPanel.add(confirmPasswordField, gbc);
        currentRow++;


        // Panel cho các nút ở dưới cùng
        JPanel bottomButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0)); // Căn giữa các nút
        bottomButtonPanel.setOpaque(false); // Để nền của loginPanel hiển thị

        loginButton = new JButton("Đăng Nhập");
        loginButton.setFont(FONT_BUTTON);
        loginButton.setBackground(COLOR_PRIMARY);
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.setBorder(new EmptyBorder(15, 60, 15, 60)); // **NÚT TO HƠN**
        loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) { loginButton.setBackground(COLOR_PRIMARY_DARK); }
            public void mouseExited(java.awt.event.MouseEvent evt) { loginButton.setBackground(COLOR_PRIMARY); }
        });
        loginButton.addActionListener(this::btnLoginActionPerformed);
        bottomButtonPanel.add(loginButton);

        registerButton = new JButton("Đăng Ký");
        registerButton.setFont(FONT_BUTTON);
        registerButton.setBackground(new Color(108, 117, 125)); // Màu xám (secondary)
        registerButton.setForeground(Color.WHITE);
        registerButton.setFocusPainted(false);
        registerButton.setBorder(new EmptyBorder(15, 40, 15, 40)); // **NÚT TO HƠN**
        registerButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        registerButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) { registerButton.setBackground(new Color(80, 88, 95)); }
            public void mouseExited(java.awt.event.MouseEvent evt) { registerButton.setBackground(new Color(108, 117, 125)); }
        });
        registerButton.addActionListener(e -> {
            new DangKyTaiKhoanKhachHang().setVisible(true);
            this.dispose();
        });
        bottomButtonPanel.add(registerButton);

        // Thêm panel nút vào loginPanel
        gbc.gridx = 0;
        gbc.gridy = currentRow; // Hàng tiếp theo sau các trường input
        gbc.gridwidth = 2; // Cho panel nút chiếm cả 2 cột
        gbc.fill = GridBagConstraints.HORIZONTAL; // Panel nút sẽ fill ngang
        gbc.anchor = GridBagConstraints.PAGE_END; // Đặt ở dưới cùng của không gian được cấp
        gbc.weighty = 1.0; // Cho phép panel nút "đẩy" xuống dưới nếu có không gian thừa dọc
        gbc.insets = new Insets(35, 0, 0, 0); // Khoảng cách lớn phía trên panel nút
        loginPanel.add(bottomButtonPanel, gbc);


        // Thêm loginPanel vào frame
        GridBagConstraints frameGbc = new GridBagConstraints();
        frameGbc.anchor = GridBagConstraints.CENTER; // Căn loginPanel giữa frame
        frameGbc.weightx = 1.0; // Cho phép loginPanel chiếm không gian khi frame resize (nếu cần)
        frameGbc.weighty = 1.0;
        add(loginPanel, frameGbc);

        pack();
        // Đảm bảo kích thước tối thiểu sau khi pack
        if (getWidth() < 550 || getHeight() < 650) {
            setSize(Math.max(getWidth(), 550), Math.max(getHeight(), 650));
            setLocationRelativeTo(null);
        }
    }

    private void styleTextField(JTextField component) {
        component.setFont(FONT_TEXT_FIELD);
        component.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_TEXT_FIELD_BORDER, 1),
            new EmptyBorder(12, 15, 12, 15) // **PADDING LỚN HƠN CHO TEXTFIELD**
        ));
        component.setPreferredSize(new Dimension(250, 48)); // **TEXTFIELD CAO HƠN VÀ CÓ ĐỘ RỘNG ƯU TIÊN**
                                                            // Điều chỉnh 250 nếu muốn rộng hơn nữa
    }

    private void addFocusBorder(JComponent component) {
        Border defaultBorder = component.getBorder();
        Border focusBorder = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_TEXT_FIELD_BORDER_FOCUS, 2),
            (defaultBorder instanceof CompoundBorder) ?
                ((CompoundBorder) defaultBorder).getInsideBorder() : new EmptyBorder(10,10,10,10)
        );
        component.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { component.setBorder(focusBorder); }
            @Override public void focusLost(FocusEvent e) { component.setBorder(defaultBorder); }
        });
    }

    private void btnLoginActionPerformed(java.awt.event.ActionEvent evt) {
        String user = usernameField.getText().trim();
        String pass = new String(passwordField.getPassword());
        String pass2 = new String(confirmPasswordField.getPassword()); // **LẤY GIÁ TRỊ TỪ TRƯỜNG MỚI**

        if (user.isEmpty() || pass.isEmpty() || pass2.isEmpty()) { // **KIỂM TRA CẢ 3 TRƯỜNG**
            JOptionPane.showMessageDialog(this,
                "Vui lòng nhập đầy đủ tên đăng nhập, mật khẩu và xác nhận mật khẩu.", // Cập nhật thông báo
                "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!pass.equals(pass2)) { // **KIỂM TRA MẬT KHẨU KHỚP NHAU**
            JOptionPane.showMessageDialog(this,
                "Mật khẩu và xác nhận mật khẩu không khớp.",
                "Lỗi", JOptionPane.ERROR_MESSAGE);
            passwordField.setText(""); // Xóa trường mật khẩu
            confirmPasswordField.setText(""); // Xóa trường xác nhận
            passwordField.requestFocus(); // Focus lại vào trường mật khẩu
            return;
        }

        String hash = hashPassword(pass);

        try {
            User loginUserProcess = new User();
            UserToken ut = loginUserProcess.loginUser(user, hash);

            if (ut != null && ut.isStatus()) { // Thêm kiểm tra ut != null
                currentUserToken = ut; // Vẫn có thể giữ lại nếu các phần khác của hệ thống cần
                String role = ut.getRole();

                if (RoleGroupConstants.ADMIN.equals(role)) {
                    // Giả sử AdminHomePage cũng có constructor nhận UserToken
                    new AdminHomePage().setVisible(true); 
                } else if (RoleGroupConstants.CUSTOMER.equals(role)) {
                    // **SỬA Ở ĐÂY: Truyền UserToken 'ut' vào constructor của CustomerHomePage**
                    CustomerHomePage customerHome = new CustomerHomePage(ut);
                    customerHome.setVisible(true);
                } else if (RoleGroupConstants.EMPLOYEE.equals(role)) {
                    // Giả sử EmployeeHomePage cũng có constructor nhận UserToken
                    new EmployeeHomePage().setVisible(true); 
                } else {
                    JOptionPane.showMessageDialog(this,
                        "Tài khoản không có quyền truy cập phù hợp hoặc vai trò không xác định.",
                        "Lỗi quyền", JOptionPane.ERROR_MESSAGE);
                    return; 
                }
                this.dispose(); 
            } else {
                JOptionPane.showMessageDialog(this,
                    "Tên đăng nhập hoặc mật khẩu không đúng, hoặc tài khoản không hoạt động/chưa có vai trò.",
                    "Đăng nhập thất bại", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Lỗi khi đăng nhập: " + ex.getMessage(),
                "Lỗi Hệ Thống", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String hashPassword(String password) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) { sb.append(String.format("%02x", b)); }
            return sb.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not supported", e);
        }
    }

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> new DangNhap().setVisible(true));
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
