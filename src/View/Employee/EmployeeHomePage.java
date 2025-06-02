package View.Employee;


import Process.UserToken;
import View.DangNhap;
import Process.RoleGroupConstants;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.border.*;
import java.awt.image.BufferedImage;
import java.net.URL;
import com.formdev.flatlaf.FlatLightLaf;



public class EmployeeHomePage extends javax.swing.JFrame {
    private JPanel headerPanel;
    private JPanel menuPanel;
    private JLabel idValueLabel;
    private JLabel nameValueLabel;
    private JLabel roleValueLabel;
    
    private String hienThiMaDinhDanh; 
    private String hienThiHoTen;      
    private String hienThiChucVu;     

    private boolean initSuccess = false; 
    
    private static class UIConstants {
        public static final Color BACKGROUND_COLOR = new Color(235, 245, 251);
        public static final Color HEADER_BACKGROUND = Color.WHITE;
        public static final Color MENU_ITEM_BACKGROUND = Color.WHITE;
        public static final Color MENU_ITEM_HOVER_BACKGROUND = new Color(220, 235, 245);
        public static final Color FONT_COLOR_BLUE = new Color(0, 120, 215);
        public static final Font BOLD_FONT = new Font("Segoe UI", Font.BOLD, 14);
        public static final Font PLAIN_FONT = new Font("Segoe UI", Font.PLAIN, 14);
        public static final String ICON_PATH_PREFIX = "/icons/"; 
        public static final String AVATAR_ICON = ICON_PATH_PREFIX + "avatar_default.png";
        public static final String LOGOUT_ICON = ICON_PATH_PREFIX + "logout.png";
        public static final String VEHICLE_MAINTENANCE_ICON = ICON_PATH_PREFIX + "vehicle-management.png";
        public static final String PROBLEM_REPORT_ICON = ICON_PATH_PREFIX + "reporting.png";
        public static final String ACCOUNT_SETTING_ICON = ICON_PATH_PREFIX + "account_setting.png";
    }

    public EmployeeHomePage() {
        if (!isAuthenticatedAndAuthorized()) {
            SwingUtilities.invokeLater(this::dispose); 
            return; 
        }
        this.hienThiMaDinhDanh = DangNhap.currentUserToken.getEntityId();    
        this.hienThiHoTen = DangNhap.currentUserToken.getEntityFullName(); 
        this.hienThiChucVu = DangNhap.currentUserToken.getRole();
        
        System.setProperty("flatlaf.useVisualPadding", "false");
        try { UIManager.setLookAndFeel(new FlatLightLaf()); } 
        catch (UnsupportedLookAndFeelException ex) { System.err.println("Lỗi LaF: " + ex.getMessage()); }

        setSize(1350, 720);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setTitle("Trang Chủ Nhân Viên - " + this.hienThiHoTen); 
        setResizable(false);
        setLocationRelativeTo(null); 

        getContentPane().setBackground(UIConstants.BACKGROUND_COLOR);
        setLayout(new BorderLayout(10, 10));

        createHeaderPanel(); 
        add(headerPanel, BorderLayout.NORTH);

        createMenuPanel();
        JPanel menuContainer = new JPanel(new BorderLayout());
        menuContainer.setBackground(UIConstants.BACKGROUND_COLOR);
        menuContainer.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
        menuContainer.add(menuPanel, BorderLayout.CENTER);
        add(menuContainer, BorderLayout.CENTER);
        
    }

    private boolean isAuthenticatedAndAuthorized() {
        if (DangNhap.currentUserToken == null || !DangNhap.currentUserToken.isStatus()) {
            handleAuthFailure("Bạn cần đăng nhập để truy cập trang này.", "Yêu Cầu Đăng Nhập"); return false;
        }
        String role = DangNhap.currentUserToken.getRole();
        String entityId = DangNhap.currentUserToken.getEntityId(); 

        if (entityId == null || entityId.trim().isEmpty()) {
            handleAuthFailure("Thông tin định danh người dùng không hợp lệ. Vui lòng đăng nhập lại.", "Lỗi Token"); return false;
        }
        // Kiểm tra cả hienThiHoTen nếu nó quan trọng cho việc hiển thị/hoạt động
        if (DangNhap.currentUserToken.getEntityFullName() == null || DangNhap.currentUserToken.getEntityFullName().trim().isEmpty()){
             handleAuthFailure("Thông tin tên người dùng không hợp lệ (từ token). Vui lòng đăng nhập lại.", "Lỗi Token"); return false;
        }
        if (!RoleGroupConstants.EMPLOYEE.equals(role)) {
            handleAuthFailure("Bạn không có quyền truy cập Trang Chủ Nhân Viên. Vai trò: " + (role == null || role.isEmpty() ? "N/A" : role), "Không Được Phép"); return false;
        }
        initSuccess = true; 
        return true;
    }

    private void handleAuthFailure(String message, String title) {
        EventQueue.invokeLater(() -> {
            JOptionPane.showMessageDialog(null, message, title, JOptionPane.ERROR_MESSAGE);
            // Kiểm tra trước khi tạo mới DangNhap và dispose để tránh lỗi nếu frame đã bị dispose
            Window window = SwingUtilities.getWindowAncestor(this);
            if (window != null && window.isDisplayable()) {
                 new DangNhap().setVisible(true);
                 window.dispose();
            } else if (this.isDisplayable()){ // Fallback nếu getWindowAncestor trả về null nhưng frame vẫn displayable
                 new DangNhap().setVisible(true);
                 this.dispose();
            }
        });
    }

    private ImageIcon loadIcon(String path, int width, int height) {
        URL imgUrl = getClass().getResource(path);
        if (imgUrl != null) {
            ImageIcon originalIcon = new ImageIcon(imgUrl);
            Image scaledImage = originalIcon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
            return new ImageIcon(scaledImage);
        } else {
            System.err.println("Không tìm thấy icon: " + path + ". Sử dụng placeholder.");
            BufferedImage placeholder = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = placeholder.createGraphics();
            g2d.setColor(Color.LIGHT_GRAY);
            g2d.fillRect(0, 0, width, height);
            g2d.setColor(Color.DARK_GRAY);
            g2d.drawString("?", width / 2 - 4, height / 2 + 4);
            g2d.dispose();
            return new ImageIcon(placeholder);
        }
    }

    private void createHeaderPanel() {
        headerPanel = new JPanel(new BorderLayout(20, 0));
        headerPanel.setBackground(UIConstants.HEADER_BACKGROUND);
        headerPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
        headerPanel.setPreferredSize(new Dimension(getWidth(), 70)); 
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JPanel avatarPanel = new JPanel(new BorderLayout());
        avatarPanel.setBackground(UIConstants.HEADER_BACKGROUND);
        avatarPanel.setPreferredSize(new Dimension(80, 60)); 
        JLabel avatarLabel = new JLabel(loadIcon(UIConstants.AVATAR_ICON, 50, 50));
        avatarLabel.setHorizontalAlignment(JLabel.CENTER);
        avatarPanel.add(avatarLabel, BorderLayout.CENTER);

        JPanel infoPanel = new JPanel(new GridLayout(2,1,0,5));
        infoPanel.setBackground(UIConstants.HEADER_BACKGROUND);

        JPanel idPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0,0));
        idPanel.setBackground(UIConstants.HEADER_BACKGROUND);
        JLabel idLabelText = new JLabel("Mã Định Danh:"); 
        idLabelText.setFont(UIConstants.BOLD_FONT);
        this.idValueLabel = new JLabel(this.hienThiMaDinhDanh); 
        this.idValueLabel.setFont(UIConstants.PLAIN_FONT);
        this.idValueLabel.setForeground(UIConstants.FONT_COLOR_BLUE);
        idPanel.add(idLabelText);
        idPanel.add(Box.createHorizontalStrut(5));
        idPanel.add(this.idValueLabel);

        JPanel namePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        namePanel.setBackground(UIConstants.HEADER_BACKGROUND);
        JLabel nameLabelText = new JLabel("Họ tên:");
        nameLabelText.setFont(UIConstants.BOLD_FONT);
        this.nameValueLabel = new JLabel(this.hienThiHoTen); 
        this.nameValueLabel.setFont(UIConstants.PLAIN_FONT);
        this.nameValueLabel.setForeground(UIConstants.FONT_COLOR_BLUE);
        namePanel.add(nameLabelText);
        namePanel.add(Box.createHorizontalStrut(5));
        namePanel.add(this.nameValueLabel);
        
        infoPanel.add(idPanel);
        infoPanel.add(namePanel);


        JPanel rightPanel = new JPanel(new GridLayout(2,1,0,5));
        rightPanel.setBackground(UIConstants.HEADER_BACKGROUND);
        JPanel rolePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        rolePanel.setBackground(UIConstants.HEADER_BACKGROUND);
        JLabel roleLabelText = new JLabel("Chức vụ:");
        roleLabelText.setFont(UIConstants.BOLD_FONT);
        this.roleValueLabel = new JLabel(this.hienThiChucVu); 
        this.roleValueLabel.setFont(UIConstants.PLAIN_FONT);
        this.roleValueLabel.setForeground(UIConstants.FONT_COLOR_BLUE);
        rolePanel.add(roleLabelText); rolePanel.add(Box.createHorizontalStrut(5)); rolePanel.add(this.roleValueLabel);

        JButton logoutButton = new JButton("Đăng xuất", loadIcon(UIConstants.LOGOUT_ICON, 18, 18));
        logoutButton.setFont(UIConstants.BOLD_FONT);
        logoutButton.setFocusPainted(false);
        logoutButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutButton.addActionListener(e -> { 
            DangNhap.currentUserToken = null; new DangNhap().setVisible(true); this.dispose();});
        JPanel logoutPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0)); 
        logoutPanel.setBackground(UIConstants.HEADER_BACKGROUND);
        logoutPanel.add(logoutButton);
        
        rightPanel.add(rolePanel);
        rightPanel.add(logoutPanel);

        headerPanel.add(avatarPanel, BorderLayout.WEST);
        headerPanel.add(infoPanel, BorderLayout.CENTER);
        headerPanel.add(rightPanel, BorderLayout.EAST);
    }

    private void createMenuPanel() {
        menuPanel = new JPanel();
        menuPanel.setLayout(new GridLayout(1, 3, 15, 15)); 
        menuPanel.setBackground(UIConstants.BACKGROUND_COLOR);

        String[] menuTitles = {
            "Thực hiện bảo dưỡng xe", "Báo cáo sự cố", "Cài đặt tài khoản"
        };

        String[] menuIcons = {
                UIConstants.VEHICLE_MAINTENANCE_ICON,
                UIConstants.PROBLEM_REPORT_ICON,
                UIConstants.ACCOUNT_SETTING_ICON
        };

        for (int i = 0; i < menuTitles.length; i++) {
            if (menuTitles[i] != null && !menuTitles[i].isEmpty()) {
                JPanel menuItemPanel = createMenuItem(menuTitles[i], menuIcons[i]);
                menuPanel.add(menuItemPanel);
            } else {
                JPanel emptyPanel = new JPanel();
                emptyPanel.setOpaque(false); 
                menuPanel.add(emptyPanel);
            }
        }
    }

    private JPanel createMenuItem(String title, String iconPath) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(0, 10));
        panel.setBackground(UIConstants.MENU_ITEM_BACKGROUND);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JLabel iconLabel = new JLabel();
        iconLabel.setHorizontalAlignment(JLabel.CENTER);
        iconLabel.setIcon(loadIcon(iconPath, 70, 70));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setHorizontalAlignment(JLabel.CENTER);
        titleLabel.setFont(UIConstants.BOLD_FONT);
        titleLabel.setForeground(UIConstants.FONT_COLOR_BLUE);

        panel.add(iconLabel, BorderLayout.CENTER);
        panel.add(titleLabel, BorderLayout.SOUTH);

        panel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                panel.setBackground(UIConstants.MENU_ITEM_HOVER_BACKGROUND);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                panel.setBackground(UIConstants.MENU_ITEM_BACKGROUND);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (DangNhap.currentUserToken == null || !DangNhap.currentUserToken.isStatus()) {
                     JOptionPane.showMessageDialog(EmployeeHomePage.this,
                        "Phiên đăng nhập không hợp lệ. Vui lòng đăng nhập lại.",
                        "Lỗi Phiên", JOptionPane.ERROR_MESSAGE);
                     new DangNhap().setVisible(true);
                     EmployeeHomePage.this.dispose();
                     return;
                }
                
                String currentTitle = title; 
                System.out.println("Nhân viên " + hienThiHoTen + " (ID: " + hienThiMaDinhDanh + ") click: " + currentTitle); 

                switch (currentTitle) {
                    case "Thực hiện bảo dưỡng xe":
                        if (EmployeeHomePage.this.hienThiMaDinhDanh != null && !EmployeeHomePage.this.hienThiMaDinhDanh.isEmpty() &&
                            EmployeeHomePage.this.hienThiHoTen != null && !EmployeeHomePage.this.hienThiHoTen.isEmpty()) {
                            // Truyền Mã Định Danh và Họ Tên vào ThucHienBaoDuongXe
                            new ThucHienBaoDuongXe(EmployeeHomePage.this.hienThiMaDinhDanh, EmployeeHomePage.this.hienThiHoTen).setVisible(true);
                        } else {
                            JOptionPane.showMessageDialog(EmployeeHomePage.this,
                                "Không thể lấy thông tin nhân viên đầy đủ (ID hoặc Tên). Vui lòng đăng nhập lại.",
                                "Lỗi Người Dùng",
                                JOptionPane.ERROR_MESSAGE);
                        }
                        EmployeeHomePage.this.dispose(); 
                        break;
                    case "Báo cáo sự cố":
                        if (EmployeeHomePage.this.hienThiMaDinhDanh != null && !EmployeeHomePage.this.hienThiMaDinhDanh.isEmpty()) {
                            // Truyền Mã Định Danh (ID Nhân Viên) vào BaoCaoSuCo
                            new BaoCaoSuCo(EmployeeHomePage.this.hienThiMaDinhDanh).setVisible(true); 
                        } else {
                            JOptionPane.showMessageDialog(EmployeeHomePage.this,
                                "Không thể lấy mã nhân viên. Vui lòng đăng nhập lại.",
                                "Lỗi Người Dùng",
                                JOptionPane.ERROR_MESSAGE);
                        }
                        EmployeeHomePage.this.dispose(); 
                        break;
                    case "Cài đặt tài khoản":
                        if (DangNhap.currentUserToken != null &&
                            DangNhap.currentUserToken.isStatus() &&
                            DangNhap.currentUserToken.getEntityId() != null && 
                            !DangNhap.currentUserToken.getEntityId().isEmpty() && 
                            DangNhap.currentUserToken.getLoginUsername() != null &&
                            !DangNhap.currentUserToken.getLoginUsername().isEmpty()) {

                            String maNhanVienDeTruyen = DangNhap.currentUserToken.getEntityId(); 
                            String tenDangNhapDeTruyen = DangNhap.currentUserToken.getLoginUsername();
                            new CaiDatTaiKhoanNhanVien(maNhanVienDeTruyen, tenDangNhapDeTruyen).setVisible(true);
                            EmployeeHomePage.this.dispose();

                        } else {
                            JOptionPane.showMessageDialog(EmployeeHomePage.this,
                                "Không thể lấy thông tin người dùng hợp lệ. Vui lòng đăng nhập lại.",
                                "Lỗi Người Dùng",
                                JOptionPane.ERROR_MESSAGE);
                            new DangNhap().setVisible(true); 
                            EmployeeHomePage.this.dispose();
                        }
                        break;
                    default:
                        JOptionPane.showMessageDialog(EmployeeHomePage.this, "Chức năng '" + currentTitle + "' không xác định.");
                }
            }
        });
        return panel;
    }
    
    @Override
    public void setVisible(boolean b) {
        if (b && !initSuccess && (DangNhap.currentUserToken == null || !DangNhap.currentUserToken.isStatus())) {
            System.out.println("EmployeeHomePage: Ngăn không cho hiển thị do khởi tạo thất bại (chưa xác thực).");
            return;
        }
        super.setVisible(b);
    }

    public static void main(String args[]) {
        System.setProperty("flatlaf.useVisualPadding", "false");
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (UnsupportedLookAndFeelException ex) {
            System.err.println("Failed to initialize LaF: " + ex.getMessage());
        }
        
        java.awt.EventQueue.invokeLater(() -> {
            /*
            UserToken testToken = new UserToken();
            testToken.setStatus(true);
            testToken.setEntityId("EMP001"); 
            testToken.setEntityFullName("Nhân Viên Test UI");
            testToken.setLoginUsername("nv_test_ui");
            testToken.setRole(RoleGroupConstants.EMPLOYEE);
            DangNhap.currentUserToken = testToken;
            */
            
            EmployeeHomePage homePage = new EmployeeHomePage();
            // initSuccess được set trong isAuthenticatedAndAuthorized
            // Nếu false, frame đã được dispose hoặc không bao giờ được tạo/hiển thị đúng cách.
            if (homePage.isDisplayable() && homePage.initSuccess) { // Thêm kiểm tra isDisplayable
                 homePage.setVisible(true);
            } else if (!homePage.initSuccess) {
                System.out.println("EmployeeHomePage.main: Khởi tạo thất bại, không hiển thị. (Lỗi xác thực hoặc ủy quyền)");
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
