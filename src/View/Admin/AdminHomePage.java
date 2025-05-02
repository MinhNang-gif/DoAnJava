package View.Admin;

import View.DangNhap;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;


public class AdminHomePage extends javax.swing.JFrame {

    public AdminHomePage() {
        initComponents();
        initCustomComponents();
        
        /* Cac buoc set giao dien */
        // 1. Set kich thuoc giao dien
        setSize(1270, 700);
        // 2. Set nut chon tat mac dinh EXIT_ON_CLOSE
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        // 3. Tieu de
        setTitle("Admin Home Page");
        // 4. Khong cho phong to, thu nho
        setResizable(false);
        // 5. Vi tri trang
        setLocation(100, 40);
    }
    
    
    private void initCustomComponents() {
        // Sử dụng BorderLayout cho frame
        getContentPane().setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(200, 230, 255));

        // ===== HEADER =====
        JPanel header = new JPanel(new BorderLayout(20, 0));
        header.setBorder(new LineBorder(Color.RED, 2));
        header.setBackground(Color.WHITE);
        header.setPreferredSize(new Dimension(0, 120));

        // Ảnh avatar bên trái
        // load ảnh gốc
        ImageIcon origIcon = new ImageIcon(
            getClass().getResource("/View/Admin/AdminIcons/profile.png")
        );

        // resize về 80x80 với chất lượng mượt
        Image scaledImg = origIcon.getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH);

        // tạo ImageIcon mới từ ảnh đã scale
        ImageIcon smallIcon = new ImageIcon(scaledImg);

        // gán cho label
        JLabel lblAvatar = new JLabel();
        lblAvatar.setIcon(smallIcon);
        lblAvatar.setPreferredSize(new Dimension(80, 80));

        
        header.add(lblAvatar, BorderLayout.WEST);

        // Panel chứa thông tin ID, Họ tên, Chức vụ, Đăng xuất
        JPanel info = new JPanel(new GridLayout(2, 4, 15, 5));
        info.setOpaque(false);
        info.add(new JLabel("ID:"));
        info.add(new JLabel("Quản trị viên"));
        info.add(new JLabel("Chức vụ:"));
        info.add(new JLabel("Quản trị viên"));
        info.add(new JLabel("Họ tên:"));
        info.add(new JLabel("Quản trị viên"));
        info.add(new JLabel("Đăng xuất:"));
        JButton btnLogout = new JButton(new ImageIcon(getClass().getResource(
                "/View/Admin/AdminIcons/profile.png"
            )));
        btnLogout.addActionListener((ActionEvent e) -> {
            dispose();
            new DangNhap().setVisible(true);
        });
        info.add(btnLogout);
        header.add(info, BorderLayout.CENTER);

        getContentPane().add(header, BorderLayout.NORTH);

        
        
        // ===== LƯỚI CHỨC NĂNG =====
        JPanel grid = new JPanel(new GridLayout(2, 4, 20, 20));
        grid.setBackground(getContentPane().getBackground());
        grid.setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 20));

        grid.add(createCard("/View/Admin/AdminImages/customers.png",    "Quản lý khách hàng"));
        grid.add(createCard("/View/Admin/AdminImages/staff.png",        "Quản lý nhân viên"));
        grid.add(createCard("/View/Admin/AdminImages/ticket.png",       "Tra cứu vé"));
        grid.add(createCard("/View/Admin/AdminImages/visitor.png",      "Khách vãng lai"));
        grid.add(createCard("/View/Admin/AdminImages/invoice.png",      "Tra cứu hóa đơn"));
        grid.add(createCard("/View/Admin/AdminImages/entry_exit.png",   "Xe ra vào"));
        grid.add(createCard("/View/Admin/AdminImages/report.png",       "Báo cáo & Thống kê"));
        grid.add(createCard("/View/Admin/AdminImages/vehicle.png",      "Quản lý xe"));

        getContentPane().add(grid, BorderLayout.CENTER);
    }

    // Tao card chuc nang
    private JPanel createCard(String iconPath, String title) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(new LineBorder(Color.RED, 2));
        card.setBackground(Color.WHITE);

        JButton btn = new JButton(new ImageIcon(iconPath));
        btn.setContentAreaFilled(false);
        btn.setBorder(null);
        // TODO: Thêm ActionListener cho button khi cần
        card.add(btn, BorderLayout.CENTER);

        JLabel lbl = new JLabel(title, SwingConstants.CENTER);
        lbl.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        card.add(lbl, BorderLayout.SOUTH);

        return card;
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

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new AdminHomePage().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
