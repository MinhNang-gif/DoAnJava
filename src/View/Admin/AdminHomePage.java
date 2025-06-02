package View.Admin;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.border.*;
import java.awt.image.BufferedImage;
import java.net.URL;
import com.formdev.flatlaf.FlatLightLaf;


public class AdminHomePage extends javax.swing.JFrame {
    private JPanel headerPanel;
    private JPanel menuPanel;

    private static class UIConstants {
        public static final Color BACKGROUND_COLOR = new Color(235, 245, 251);
        public static final Color HEADER_BACKGROUND = Color.WHITE;
        public static final Color MENU_ITEM_BACKGROUND = Color.WHITE;
        public static final Color MENU_ITEM_HOVER_BACKGROUND = new Color(220, 235, 245);
        public static final Color FONT_COLOR_BLUE = new Color(0, 120, 215);
        public static final Font BOLD_FONT = new Font("Segoe UI", Font.BOLD, 14);
        public static final Font PLAIN_FONT = new Font("Segoe UI", Font.PLAIN, 14);

        public static final String ICON_PATH_PREFIX = "/icons/"; // Đảm bảo đường dẫn này chính xác

        public static final String AVATAR_ICON = ICON_PATH_PREFIX + "avatar_default.png";
        public static final String LOGOUT_ICON = ICON_PATH_PREFIX + "logout.png";

        public static final String CUSTOMER_MANAGEMENT_ICON = ICON_PATH_PREFIX + "customer_management.png";
        public static final String EMPLOYEE_MANAGEMENT_ICON = ICON_PATH_PREFIX + "employee_management.png";
        public static final String TICKET_LOOKUP_ICON = ICON_PATH_PREFIX + "problem.png";
        public static final String MAINTENANCE_EQUIPMENT_ICON = ICON_PATH_PREFIX + "maintenance_equipment.png";
        public static final String INVOICE_LOOKUP_ICON = ICON_PATH_PREFIX + "digital-services.png";
        public static final String VEHICLE_ACCESS_ICON = ICON_PATH_PREFIX + "vehicle_access.png";
        public static final String REPORT_STATS_ICON = ICON_PATH_PREFIX + "report_stats.png";
        public static final String VEHICLE_MANAGEMENT_ICON = ICON_PATH_PREFIX + "vehicle_management.png";
        public static final String PERSONNEL_MANAGEMENT_ICON = ICON_PATH_PREFIX + "management.png";
    }

    public AdminHomePage() {
        // Yêu cầu FlatLaf không sử dụng visual padding liên quan đến MigLayout
        System.setProperty("flatlaf.useVisualPadding", "false");
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (UnsupportedLookAndFeelException ex) {
            System.err.println("Failed to initialize LaF: " + ex.getMessage());
        }

        setSize(1350, 720);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setTitle("Admin Home Page - Quản Lý Xe và Bảo Dưỡng Xe");
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

        // setVisible(true); // Sẽ được gọi trong main hoặc khi cần hiển thị
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
        headerPanel = new JPanel();
        headerPanel.setLayout(new BorderLayout(20, 0));
        headerPanel.setBackground(UIConstants.HEADER_BACKGROUND);
        headerPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
        headerPanel.setPreferredSize(new Dimension(getWidth(), 90)); // Sử dụng getWidth() động có thể không tốt nếu frame chưa được packed/shown
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JPanel avatarPanel = new JPanel(new BorderLayout());
        avatarPanel.setBackground(UIConstants.HEADER_BACKGROUND);
        avatarPanel.setPreferredSize(new Dimension(80, 70));
        JLabel avatarLabel = new JLabel();
        avatarLabel.setIcon(loadIcon(UIConstants.AVATAR_ICON, 60, 60));
        avatarLabel.setHorizontalAlignment(JLabel.CENTER);
        avatarPanel.add(avatarLabel, BorderLayout.CENTER);

        JPanel infoPanel = new JPanel();
        infoPanel.setBackground(UIConstants.HEADER_BACKGROUND);
        infoPanel.setLayout(new GridLayout(2, 1, 0, 5));
        JPanel idPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        idPanel.setBackground(UIConstants.HEADER_BACKGROUND);
        JLabel idLabel = new JLabel("ID:");
        idLabel.setFont(UIConstants.BOLD_FONT);
        JLabel idValueLabel = new JLabel("Quản trị viên");
        idValueLabel.setFont(UIConstants.PLAIN_FONT);
        idValueLabel.setForeground(UIConstants.FONT_COLOR_BLUE);
        idPanel.add(idLabel);
        idPanel.add(Box.createHorizontalStrut(10));
        idPanel.add(idValueLabel);
        JPanel namePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        namePanel.setBackground(UIConstants.HEADER_BACKGROUND);
        JLabel nameLabel = new JLabel("Họ tên:");
        nameLabel.setFont(UIConstants.BOLD_FONT);
        JLabel nameValueLabel = new JLabel("Quản trị viên");
        nameValueLabel.setFont(UIConstants.PLAIN_FONT);
        nameValueLabel.setForeground(UIConstants.FONT_COLOR_BLUE);
        namePanel.add(nameLabel);
        namePanel.add(Box.createHorizontalStrut(10));
        namePanel.add(nameValueLabel);
        infoPanel.add(idPanel);
        infoPanel.add(namePanel);

        JPanel rightPanel = new JPanel();
        rightPanel.setBackground(UIConstants.HEADER_BACKGROUND);
        rightPanel.setLayout(new GridLayout(2, 1, 0, 5));
        JPanel rolePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        rolePanel.setBackground(UIConstants.HEADER_BACKGROUND);
        JLabel roleLabel = new JLabel("Chức vụ:");
        roleLabel.setFont(UIConstants.BOLD_FONT);
        JLabel roleValueLabel = new JLabel("Quản trị viên");
        roleValueLabel.setFont(UIConstants.PLAIN_FONT);
        roleValueLabel.setForeground(UIConstants.FONT_COLOR_BLUE);
        rolePanel.add(roleLabel);
        rolePanel.add(Box.createHorizontalStrut(10));
        rolePanel.add(roleValueLabel);
        JButton logoutButton = new JButton("Đăng xuất");
        logoutButton.setFont(UIConstants.BOLD_FONT);
        logoutButton.setIcon(loadIcon(UIConstants.LOGOUT_ICON, 18, 18));
        logoutButton.setFocusPainted(false);
        logoutButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutButton.setHorizontalAlignment(SwingConstants.LEFT);
        logoutButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn đăng xuất?", "Xác nhận đăng xuất", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                 // Giả sử bạn có một màn hình đăng nhập là LoginForm
                 // new LoginForm().setVisible(true);
                 // dispose();
                System.out.println("Đã đăng xuất, quay lại màn hình Login (cần LoginForm.class)");
                System.exit(0); // Hoặc cách khác để quay lại login
            }
        });
        JPanel logoutPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
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
        menuPanel.setLayout(new GridLayout(2, 5, 15, 15));
        menuPanel.setBackground(UIConstants.BACKGROUND_COLOR);

        String[] menuTitles = {
                "Quản lý khách hàng", "Quản lý tài khoản nhân viên", "Quản lý sự cố", "Quản lý thiết bị bảo dưỡng", "Quản lý dịch vụ",
                "Quản lý xe ra vào", "Quản lý doanh thu & Thống kê", "Quản lý xe", "Quản lý nhân sự bãi xe", ""
        };

        String[] menuIcons = {
                UIConstants.CUSTOMER_MANAGEMENT_ICON, UIConstants.EMPLOYEE_MANAGEMENT_ICON,
                UIConstants.TICKET_LOOKUP_ICON, UIConstants.MAINTENANCE_EQUIPMENT_ICON,
                UIConstants.INVOICE_LOOKUP_ICON, UIConstants.VEHICLE_ACCESS_ICON,
                UIConstants.REPORT_STATS_ICON, UIConstants.VEHICLE_MANAGEMENT_ICON,
                UIConstants.PERSONNEL_MANAGEMENT_ICON, ""
        };

        for (int i = 0; i < menuTitles.length; i++) {
            if (menuTitles[i].isEmpty()) {
                menuPanel.add(new JPanel(){{setOpaque(false);}});
            } else {
                JPanel menuItemPanel = createMenuItem(menuTitles[i], menuIcons[i]);
                menuPanel.add(menuItemPanel);
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
                // setVisible(false); // Cân nhắc việc ẩn/dispose ở đây
                String currentTitle = title; // Sử dụng biến local để tránh thay đổi title trong lambda/inner class
                System.out.println("Clicked on: " + currentTitle); // Debug
                
                // Tạm thời đóng cửa sổ hiện tại trước khi mở cửa sổ mới
                // Điều này giúp tránh việc có nhiều cửa sổ AdminHomePage mở cùng lúc nếu không được quản lý cẩn thận
                // AdminHomePage.this.dispose(); // hoặc setVisible(false) nếu bạn muốn quay lại sau

                switch (currentTitle) {
                    case "Quản lý tài khoản nhân viên":
                        new QuanLyTaiKhoanNhanVien().setVisible(true);
                        AdminHomePage.this.dispose();
                        break;
                    case "Quản lý khách hàng":
                        new QuanLyKhachHang().setVisible(true);
                        AdminHomePage.this.dispose();
                        break;
                    case "Quản lý xe ra vào":
                         new QuanLyXeRaVao().setVisible(true);
                        AdminHomePage.this.dispose();
                        break;
                    case "Quản lý xe":
                         new QuanLyXe().setVisible(true);
                        AdminHomePage.this.dispose();
                        break;
                    case "Quản lý thiết bị bảo dưỡng":
                         new QuanLyThietBiBaoDuong().setVisible(true);
                        AdminHomePage.this.dispose();
                        break;
                    case "Quản lý doanh thu & Thống kê":
                         new QuanLyDoanhThuVaThongKe().setVisible(true);
                        AdminHomePage.this.dispose();
                        break;
                    case "Quản lý sự cố":
                         new QuanLySuCo(AdminHomePage.this).setVisible(true);
                         AdminHomePage.this.setVisible(false); // Ẩn thay vì dispose nếu cần quay lại
                        break;
                    case "Quản lý dịch vụ":
                         new QuanLyDichVu(AdminHomePage.this).setVisible(true);
                         AdminHomePage.this.setVisible(false);
                        break;
                    case "Quản lý nhân sự bãi xe":
                         new QuanLyNhanSuBaiXe(AdminHomePage.this).setVisible(true);
                         AdminHomePage.this.setVisible(false);
                        break;
                    default:
                        JOptionPane.showMessageDialog(AdminHomePage.this, "Bạn đã nhấp vào: " + currentTitle);
                         AdminHomePage.this.setVisible(true); // Hiển thị lại nếu không có điều hướng
                }
            }
        });
        return panel;
    }
    
    public static void main(String args[]) {
        // Yêu cầu FlatLaf không sử dụng visual padding liên quan đến MigLayout
        System.setProperty("flatlaf.useVisualPadding", "false");
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (UnsupportedLookAndFeelException ex) {
            System.err.println("Failed to initialize LaF: " + ex.getMessage());
        }

        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new AdminHomePage().setVisible(true); // Hiển thị frame ở đây
            }
        });
    }
    
    
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponentsUI
    private void initComponentsUI() {

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
    }// </editor-fold>//GEN-END:initComponentsUI

    

    
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
