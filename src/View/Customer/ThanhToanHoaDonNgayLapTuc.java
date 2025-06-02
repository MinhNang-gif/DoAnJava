package View.Customer;

import com.formdev.flatlaf.FlatLightLaf;
import ConnectDB.ConnectionOracle;
import Process.RoleGroupConstants;
import Process.UserToken;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.Locale;

public class ThanhToanHoaDonNgayLapTuc extends javax.swing.JFrame {
    public ThanhToanHoaDonNgayLapTuc() {
        initComponents();
    }

    // --- UI Styling Constants ---
    private final Color FRAME_PRIMARY_COLOR = new Color(0, 123, 255); // Blue
    private final Color BUTTON_SUCCESS_COLOR = new Color(40, 167, 69); // Green
    private final Color BUTTON_CANCEL_COLOR = new Color(108, 117, 125); // Gray
    private final Color TEXT_COLOR_ON_PRIMARY = Color.WHITE;
    private final Color TEXT_COLOR_DARK = new Color(33, 37, 41);
    private final Color APP_BACKGROUND = new Color(248, 249, 250);
    private final Color INFO_PANEL_BACKGROUND = Color.WHITE;
    private final Color BORDER_COLOR = new Color(222, 226, 230);

    private final Font FONT_TITLE_FRAME = new Font("Segoe UI", Font.BOLD, 22);
    private final Font FONT_LABEL_HEADER = new Font("Segoe UI", Font.BOLD, 16);
    private final Font FONT_LABEL_INFO = new Font("Segoe UI", Font.BOLD, 14);
    private final Font FONT_VALUE_INFO = new Font("Segoe UI", Font.PLAIN, 14);
    private final Font FONT_BALANCE_HIGHLIGHT = new Font("Segoe UI", Font.BOLD, 16);
    private final Font FONT_BUTTON = new Font("Segoe UI", Font.BOLD, 14);

    // --- UI Components ---
    private JLabel lblMaHoaDonValue;
    private JLabel lblTenKhachHangValue;
    private JLabel lblTongTienValue;
    private JLabel lblSoDuHienTaiValue;
    private JButton btnXacNhanThanhToan;
    private JButton btnHuyBo;

    // --- Data and References ---
    private String currentMaHoaDon;
    private CustomerHomePage ownerCustomerHomePage;
    private ThanhToanHoaDon previousScreenThanhToanHoaDon; // Để gọi refresh
    private double tongTienHoaDon;
    private double soDuKhachHang;

    private final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

    public ThanhToanHoaDonNgayLapTuc(String maHoaDon, CustomerHomePage owner, ThanhToanHoaDon previousScreen) {
        this.currentMaHoaDon = maHoaDon;
        this.ownerCustomerHomePage = owner;
        this.previousScreenThanhToanHoaDon = previousScreen;

        if (ownerCustomerHomePage == null || !ownerCustomerHomePage.isUserCurrentlyLoggedIn()) {
            JOptionPane.showMessageDialog(null, "Lỗi: Thông tin khách hàng không hợp lệ hoặc chưa đăng nhập.", "Lỗi Người Dùng", JOptionPane.ERROR_MESSAGE);
            SwingUtilities.invokeLater(this::closeAndReEnablePrevious);
            return;
        }

        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception e) {
            System.err.println("Failed to initialize LaF: " + e.getMessage());
        }

        setTitle("Thanh Toán Hóa Đơn Ngay");
        setSize(550, 450);
        setMinimumSize(new Dimension(500, 400));
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // Xử lý đóng qua window listener
        setLocationRelativeTo(previousScreen); // Hiển thị gần cửa sổ cha
        getContentPane().setBackground(APP_BACKGROUND);
        setLayout(new BorderLayout(10, 10));

        initComponentsCustom();
        loadInvoiceAndBalanceDetails();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                closeAndReEnablePrevious();
            }
        });
    }

    private void initComponentsCustom() {
        // --- Panel Tiêu đề ---
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        titlePanel.setBackground(FRAME_PRIMARY_COLOR);
        titlePanel.setBorder(new EmptyBorder(15, 0, 15, 0));
        JLabel lblTitle = new JLabel("XÁC NHẬN THANH TOÁN HÓA ĐƠN");
        lblTitle.setFont(FONT_TITLE_FRAME);
        lblTitle.setForeground(TEXT_COLOR_ON_PRIMARY);
        titlePanel.add(lblTitle);

        // --- Panel Thông tin ---
        JPanel infoContainerPanel = new JPanel(new BorderLayout());
        infoContainerPanel.setBorder(new EmptyBorder(20, 25, 20, 25));
        infoContainerPanel.setOpaque(false);

        JPanel infoDetailsPanel = new JPanel(new GridBagLayout());
        infoDetailsPanel.setBackground(INFO_PANEL_BACKGROUND);
        infoDetailsPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            new EmptyBorder(20, 20, 20, 20)
        ));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 5, 8, 15); // top, left, bottom, right
        gbc.anchor = GridBagConstraints.WEST;

        // Mã Hóa Đơn
        gbc.gridx = 0; gbc.gridy = 0;
        infoDetailsPanel.add(createLabel("Mã Hóa Đơn:", FONT_LABEL_INFO), gbc);
        gbc.gridx = 1; lblMaHoaDonValue = createValueLabel("", FONT_VALUE_INFO);
        infoDetailsPanel.add(lblMaHoaDonValue, gbc);

        // Tên Khách Hàng
        gbc.gridx = 0; gbc.gridy = 1;
        infoDetailsPanel.add(createLabel("Khách Hàng:", FONT_LABEL_INFO), gbc);
        gbc.gridx = 1; lblTenKhachHangValue = createValueLabel("", FONT_VALUE_INFO);
        infoDetailsPanel.add(lblTenKhachHangValue, gbc);

        // Tổng Tiền Hóa Đơn
        gbc.gridx = 0; gbc.gridy = 2;
        infoDetailsPanel.add(createLabel("Tổng Tiền Thanh Toán:", FONT_LABEL_INFO), gbc);
        gbc.gridx = 1; lblTongTienValue = createValueLabel("", FONT_BALANCE_HIGHLIGHT);
        lblTongTienValue.setForeground(Color.RED.darker());
        infoDetailsPanel.add(lblTongTienValue, gbc);
        
        // Đường kẻ ngang
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(15, 0, 15, 0);
        infoDetailsPanel.add(new JSeparator(), gbc);
        gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE; // Reset
        gbc.insets = new Insets(8, 5, 8, 15); // Reset

        // Số Dư Hiện Tại
        gbc.gridx = 0; gbc.gridy = 4;
        infoDetailsPanel.add(createLabel("Số Dư Ví Hiện Tại:", FONT_LABEL_INFO), gbc);
        gbc.gridx = 1; lblSoDuHienTaiValue = createValueLabel("", FONT_BALANCE_HIGHLIGHT);
        lblSoDuHienTaiValue.setForeground(FRAME_PRIMARY_COLOR);
        infoDetailsPanel.add(lblSoDuHienTaiValue, gbc);
        
        infoContainerPanel.add(infoDetailsPanel, BorderLayout.CENTER);

        // --- Panel Nút Bấm ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        buttonPanel.setBackground(APP_BACKGROUND);
        buttonPanel.setBorder(new EmptyBorder(0, 20, 20, 20));

        btnXacNhanThanhToan = new JButton("Xác Nhận Thanh Toán");
        styleButton(btnXacNhanThanhToan, BUTTON_SUCCESS_COLOR, TEXT_COLOR_ON_PRIMARY, FONT_BUTTON);
        btnXacNhanThanhToan.setIcon(loadIcon("receipt.png", 18,18));
        btnXacNhanThanhToan.addActionListener(e -> performPayment());

        btnHuyBo = new JButton("Hủy Bỏ");
        styleButton(btnHuyBo, BUTTON_CANCEL_COLOR, TEXT_COLOR_ON_PRIMARY, FONT_BUTTON);
        btnHuyBo.setIcon(loadIcon("delete.png", 18,18));
        btnHuyBo.addActionListener(e -> closeAndReEnablePrevious());

        buttonPanel.add(btnHuyBo);
        buttonPanel.add(btnXacNhanThanhToan);

        add(titlePanel, BorderLayout.NORTH);
        add(infoContainerPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JLabel createLabel(String text, Font font) {
        JLabel label = new JLabel(text);
        label.setFont(font);
        label.setForeground(TEXT_COLOR_DARK);
        return label;
    }

    private JLabel createValueLabel(String text, Font font) {
        JLabel label = new JLabel(text);
        label.setFont(font);
        label.setForeground(TEXT_COLOR_DARK.darker());
        return label;
    }
    
    private void styleButton(JButton button, Color bgColor, Color fgColor, Font font) {
        button.setFont(font);
        if (bgColor != null) button.setBackground(bgColor);
        if (fgColor != null) button.setForeground(fgColor);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(10, 20, 10, 20));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setIconTextGap(10);
    }
    
    private ImageIcon loadIcon(String iconName, int width, int height) {
        String resourcePath = "/icons/" + iconName; // Đảm bảo có thư mục icons trong resources
        java.net.URL imgUrl = getClass().getResource(resourcePath);
        if (imgUrl != null) {
            try {
                ImageIcon originalIcon = new ImageIcon(imgUrl);
                Image scaledImage = originalIcon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
                return new ImageIcon(scaledImage);
            } catch (Exception e) {
                 System.err.println("Lỗi khi tạo ImageIcon từ URL: " + resourcePath + " - " + e.getMessage());
            }
        } else {
            System.err.println("LỖI: Không thể tìm thấy resource icon: " + resourcePath + ". Tạo placeholder.");
        }
        // Tạo placeholder icon nếu không tìm thấy hoặc lỗi
        java.awt.image.BufferedImage placeholder = new java.awt.image.BufferedImage(width, height, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = placeholder.createGraphics();
        g2d.setColor(new Color(220,220,220)); g2d.fillRect(0,0,width,height);
        g2d.setColor(new Color(150,150,150)); g2d.drawString("?", width/2-4, height/2+4);
        g2d.dispose();
        return new ImageIcon(placeholder);
    }


    private void loadInvoiceAndBalanceDetails() {
        if (currentMaHoaDon == null || currentMaHoaDon.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Mã hóa đơn không hợp lệ.", "Lỗi Dữ Liệu", JOptionPane.ERROR_MESSAGE);
            closeAndReEnablePrevious();
            return;
        }

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String sql = "SELECT hd.TONGTIEN, kh.TENKH " +
                     "FROM HOADON hd " +
                     "JOIN KHACHHANG kh ON hd.MAKH = kh.MAKH " +
                     "WHERE hd.MAHOADON = ?";
        try {
            conn = ConnectionOracle.getOracleConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, currentMaHoaDon);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                this.tongTienHoaDon = rs.getDouble("TONGTIEN");
                String tenKhachHang = rs.getString("TENKH");

                lblMaHoaDonValue.setText(currentMaHoaDon);
                lblTenKhachHangValue.setText(tenKhachHang);
                lblTongTienValue.setText(currencyFormatter.format(this.tongTienHoaDon));

                this.soDuKhachHang = ownerCustomerHomePage.getWalletBalanceForCurrentUser();
                lblSoDuHienTaiValue.setText(currencyFormatter.format(this.soDuKhachHang));

                if (this.soDuKhachHang < this.tongTienHoaDon) {
                    lblSoDuHienTaiValue.setForeground(Color.RED.darker());
                    btnXacNhanThanhToan.setEnabled(false);
                     JOptionPane.showMessageDialog(this,
                        "Số dư trong ví không đủ để thanh toán hóa đơn này.\n" +
                        "Vui lòng nạp thêm tiền vào tài khoản.",
                        "Số Dư Không Đủ", JOptionPane.WARNING_MESSAGE);
                } else {
                    lblSoDuHienTaiValue.setForeground(new Color(0,128,0)); // Green for sufficient balance
                    btnXacNhanThanhToan.setEnabled(true);
                }

            } else {
                JOptionPane.showMessageDialog(this, "Không tìm thấy thông tin hóa đơn: " + currentMaHoaDon, "Lỗi Dữ Liệu", JOptionPane.ERROR_MESSAGE);
                closeAndReEnablePrevious();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi SQL khi tải thông tin hóa đơn: " + e.getMessage(), "Lỗi SQL", JOptionPane.ERROR_MESSAGE);
            closeAndReEnablePrevious();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi driver Oracle: " + e.getMessage(), "Lỗi Driver", JOptionPane.ERROR_MESSAGE);
            closeAndReEnablePrevious();
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void performPayment() {
        if (soDuKhachHang < tongTienHoaDon) {
            JOptionPane.showMessageDialog(this, "Số dư không đủ để thực hiện thanh toán.", "Thanh Toán Thất Bại", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Gọi phương thức cập nhật số dư từ CustomerHomePage
        // Số tiền là âm vì đây là thanh toán (trừ tiền)
        boolean balanceUpdated = ownerCustomerHomePage.updateWalletBalanceForCurrentUser(
            -tongTienHoaDon, 
            "INVOICE_PAYMENT", // Loại giao dịch
            currentMaHoaDon      // ID tham chiếu (Mã hóa đơn)
        );

        if (balanceUpdated) {
            // Cập nhật trạng thái hóa đơn trong CSDL
            boolean invoiceStatusUpdated = updateInvoiceStatusInDB(currentMaHoaDon, "DA THANH TOAN");
            if (invoiceStatusUpdated) {
                JOptionPane.showMessageDialog(this, 
                    "Thanh toán hóa đơn " + currentMaHoaDon + " thành công!\n" +
                    "Số tiền: " + currencyFormatter.format(tongTienHoaDon), 
                    "Thanh Toán Thành Công", JOptionPane.INFORMATION_MESSAGE);
                
                closeAndReEnablePrevious(true); // true để báo hiệu cần refresh
            } else {
                // Lỗi cập nhật CSDL (hiếm khi xảy ra nếu trừ tiền thành công, nhưng cần xử lý)
                // Cân nhắc rollback hoặc thông báo cho quản trị viên
                 JOptionPane.showMessageDialog(this, 
                    "Đã trừ tiền thành công nhưng có lỗi khi cập nhật trạng thái hóa đơn trong cơ sở dữ liệu.\n" +
                    "Vui lòng liên hệ quản trị viên để kiểm tra hóa đơn: " + currentMaHoaDon, 
                    "Lỗi Cập Nhật Hóa Đơn", JOptionPane.ERROR_MESSAGE);
                ownerCustomerHomePage.updateWalletBalanceForCurrentUser(tongTienHoaDon, "PAYMENT_REFUND_SYSTEM_ERROR", currentMaHoaDon); // Hoàn tiền nếu có lỗi
                closeAndReEnablePrevious();
            }
        } else {
            // Lỗi khi updateWalletBalanceForCurrentUser (có thể do ghi file lỗi, hoặc validation khác trong đó)
            // CustomerHomePage đã show message lỗi rồi, không cần show thêm
            // Chỉ cần đảm bảo không đóng form và cho user thử lại hoặc hủy.
             System.err.println("ThanhToanHoaDonNgayLapTuc: updateWalletBalanceForCurrentUser trả về false.");
             // Không đóng cửa sổ, để người dùng có thể hủy hoặc thử lại sau khi khắc phục
        }
    }

    private boolean updateInvoiceStatusInDB(String invoiceId, String newStatus) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        String sql = "UPDATE HOADON SET TRANGTHAI = ? WHERE MAHOADON = ?";
        try {
            conn = ConnectionOracle.getOracleConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, newStatus);
            pstmt.setString(2, invoiceId);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi SQL khi cập nhật trạng thái hóa đơn: " + e.getMessage(), "Lỗi SQL", JOptionPane.ERROR_MESSAGE);
            return false;
        } catch (ClassNotFoundException e) {
             e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi driver Oracle: " + e.getMessage(), "Lỗi Driver", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        finally {
            try {
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    private void closeAndReEnablePrevious() {
        closeAndReEnablePrevious(false);
    }

    private void closeAndReEnablePrevious(boolean refreshNeeded) {
        if (previousScreenThanhToanHoaDon != null) {
            previousScreenThanhToanHoaDon.setEnabled(true);
            if (refreshNeeded) {
                previousScreenThanhToanHoaDon.refreshInvoiceDetails();
            }
            previousScreenThanhToanHoaDon.requestFocus();
        } else if (ownerCustomerHomePage != null) { // Fallback nếu previousScreenThanhToanHoaDon là null
             ownerCustomerHomePage.setEnabled(true);
             ownerCustomerHomePage.requestFocus();
             ownerCustomerHomePage.showWelcomeScreen(); // Hoặc refresh màn hình thích hợp
        }
        this.dispose();
    }
    

    // Main method for testing (optional, but requires a valid CustomerHomePage instance)
    public static void main(String args[]) {
        SwingUtilities.invokeLater(() -> {
            // --- Setup Test UserToken and CustomerHomePage (Owner) ---
            UserToken testToken = new UserToken();
            testToken.setEntityId("KH001"); // Mã KH có hóa đơn "CHUA THANH TOAN"
            testToken.setEntityFullName("Khách Hàng Test TT");
            testToken.setAccountId(1);
            testToken.setRole(RoleGroupConstants.CUSTOMER);

            CustomerHomePage fakeOwner = null;
            try {
                 // Giả sử KH001 có số dư ban đầu là 500,000 VND
                fakeOwner = new CustomerHomePage(testToken);
                // Nạp thử tiền cho user test nếu cần để đảm bảo có đủ số dư
                // fakeOwner.updateWalletBalanceForCurrentUser(500000, "INITIAL_BALANCE_TEST", "SYS_INIT");
                // System.out.println("Số dư ban đầu của KH001 (test): " + fakeOwner.getWalletBalanceForCurrentUser());

            } catch (Exception e) {
                System.err.println("LỖI: Không thể khởi tạo CustomerHomePage giả lập: " + e.getMessage());
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Không thể tạo CustomerHomePage giả lập. Test không thể chạy.", "Lỗi Test", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // --- Setup a fake ThanhToanHoaDon (Previous Screen) ---
            // Giả sử HD003 là hóa đơn "CHUA THANH TOAN" của KH001
            String maHoaDonTest = "HD003"; // << THAY THẾ BẰNG MÃ HÓA ĐƠN "CHUA THANH TOAN" CÓ TRONG DB
            
            // Tạo một ThanhToanHoaDon giả (không cần hiển thị, chỉ để làm tham số)
            // Trong thực tế, ThanhToanHoaDon sẽ được tạo từ DanhSachHoaDon
            ThanhToanHoaDon fakePreviousScreen = new ThanhToanHoaDon(maHoaDonTest, fakeOwner);
            // fakePreviousScreen.setVisible(false); // Không cần hiển thị frame này

            if (fakeOwner != null) {
                ThanhToanHoaDonNgayLapTuc frame = new ThanhToanHoaDonNgayLapTuc(maHoaDonTest, fakeOwner, fakePreviousScreen);
                // Chỉ setVisible nếu việc khởi tạo không bị return sớm
                if (frame.isDisplayable() || frame.isVisible()) { // Kiểm tra nếu frame vẫn còn "sống"
                    frame.setVisible(true);
                } else {
                     System.err.println("Không thể hiển thị ThanhToanHoaDonNgayLapTuc do lỗi khởi tạo hoặc UserToken/Owner không hợp lệ.");
                }
            } else {
                System.err.println("LỖI: Không thể mở ThanhToanHoaDonNgayLapTuc do CustomerHomePage (fakeOwner) là null.");
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
