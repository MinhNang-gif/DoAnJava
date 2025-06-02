package View.Customer;

import ConnectDB.ConnectionOracle;
import Process.UserToken;
import View.DangNhap;
import com.formdev.flatlaf.FlatLightLaf;
import com.toedter.calendar.JDateChooser;
import Process.RoleGroupConstants;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;
import java.awt.image.BufferedImage;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp; // Sử dụng java.sql.Timestamp
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


// Lớp tiện ích để hiển thị trong JComboBox cho Xe
class XeDisplay {
    private String bienSo;
    private String tenLoaiXe;

    public XeDisplay(String bienSo, String tenLoaiXe) {
        this.bienSo = bienSo;
        this.tenLoaiXe = tenLoaiXe;
    }

    public String getBienSo() {
        return bienSo;
    }

    @Override
    public String toString() {
        return bienSo + (tenLoaiXe != null && !tenLoaiXe.isEmpty() ? " (" + tenLoaiXe + ")" : "");
    }
}

// Lớp tiện ích để hiển thị trong JComboBox cho Dịch Vụ (Giữ nguyên)
class DichVuDisplay {
    private String maDV;
    private String tenDV;
    private double gia;

    public DichVuDisplay(String maDV, String tenDV, double gia) {
        this.maDV = maDV;
        this.tenDV = tenDV;
        this.gia = gia;
    }

    public String getMaDV() {
        return maDV;
    }

    public String getTenDV(){
        return tenDV;
    }

     public double getGia(){
        return gia;
    }

    @Override
    public String toString() {
        return tenDV + " - " + String.format("%,.0f VNĐ", gia);
    }
}

public class SuDungDichVuBaoDuong extends javax.swing.JFrame {
    // --- UI Styling Constants (Giữ nguyên) ---
    private final Color PRIMARY_COLOR = new Color(0, 123, 255);
    private final Color SIDEEBAR_BACKGROUND = new Color(45, 55, 70);
    private final Color MENU_ITEM_HOVER_BACKGROUND = new Color(70, 80, 95);
    private final Color WHITE_COLOR = Color.WHITE;
    private final Color LIGHT_GRAY_BACKGROUND = new Color(248, 249, 250);
    private final Color TEXT_COLOR_DARK = new Color(33, 37, 41);
    private final Color TEXT_COLOR_LIGHT = new Color(222, 226, 230);
    private final Color BORDER_COLOR = new Color(222, 226, 230);

    private final Font FONT_TITLE_PAGE = new Font("Segoe UI", Font.BOLD, 28);
    private final Font FONT_SUBTITLE = new Font("Segoe UI", Font.BOLD, 20);
    private final Font FONT_MENU_SERVICE = new Font("Segoe UI", Font.BOLD, 15);
    private final Font FONT_LABEL = new Font("Segoe UI", Font.PLAIN, 14);
    private final Font FONT_BUTTON = new Font("Segoe UI", Font.BOLD, 14);
    private final Font FONT_TEXT_FIELD = new Font("Segoe UI", Font.PLAIN, 14);

    // --- UI Components ---
    private JPanel mainContentPanel;
    private JPanel sidebarPanel;
    
    // Cho chức năng Đặt Lịch
    private JTextField txtBienSoDatLich; // THAY ĐỔI: Thêm làm biến thành viên
    private JComboBox<String> cbLoaiXeDatLich; // THAY ĐỔI: Thêm JComboBox chọn loại xe
    private JComboBox<DichVuDisplay> cbDichVuDatLich;
    private JDateChooser dateChooserDatLich; 
    private JComboBox<String> cbGioDatLich;

    // Cho chức năng Yêu Cầu Bảo Dưỡng
    // private JComboBox<XeDisplay> cbXeYeuCau; // THAY ĐỔI: Sẽ loại bỏ hoặc thay thế
    private JTextField txtBienSoYeuCau;      // THAY ĐỔI: Thay JComboBox bằng JTextField
    private JComboBox<String> cbLoaiXeYeuCau;  // THAY ĐỔI: Thêm JComboBox chọn loại xe
    private JTextArea txtMoTaYeuCau;

    private JTable lichSuTable;
    private DefaultTableModel lichSuTableModel;
    
    private JTextField txtTimKiemThietBi;
    private JTable thietBiTable;
    private DefaultTableModel thietBiTableModel;

    // --- Owner Frame Reference and User Info ---
    private CustomerHomePage ownerFrame; 
    private String currentMaKH;

    public SuDungDichVuBaoDuong(CustomerHomePage owner) { 
        this.ownerFrame = owner;
        if (this.ownerFrame != null && this.ownerFrame.getCurrentUser() != null) {
            this.currentMaKH = this.ownerFrame.getCurrentUser().getEntityId();
            setTitle("Dịch Vụ Bảo Dưỡng Xe - KH: " + this.ownerFrame.getCurrentUser().getEntityFullName());
        } else {
            JOptionPane.showMessageDialog(null, "Lỗi nghiêm trọng: Không thể xác định người dùng cho Dịch Vụ Bảo Dưỡng. Vui lòng đăng nhập lại.", "Lỗi Khởi Tạo", JOptionPane.ERROR_MESSAGE);
            SwingUtilities.invokeLater(() -> {
                if (this.ownerFrame != null) this.ownerFrame.dispose();
                this.dispose();
                new DangNhap().setVisible(true);
            });
            return; 
        }
        
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (UnsupportedLookAndFeelException e) {
            System.err.println("Không thể áp dụng FlatLaf LookAndFeel: " + e.getMessage());
        }

        setSize(1200, 700);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); 
        setLayout(new BorderLayout());
        setLocationRelativeTo(ownerFrame); 

        createServiceSidebarPanel();
        createMainContentArea();

        add(sidebarPanel, BorderLayout.WEST);
        add(mainContentPanel, BorderLayout.CENTER);

        showDatLichBaoDuongPanel(); 
        
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                if (ownerFrame != null) {
                    ownerFrame.showWelcomeScreen(); 
                    ownerFrame.setEnabled(true);    
                    ownerFrame.requestFocus();      
                }
            }
        });
    }

    private ImageIcon loadIcon(String iconName, int width, int height) {
        String resourcePath = "/icons/" + iconName;
        URL imgUrl = getClass().getResource(resourcePath);
        if (imgUrl != null) {
            try {
                ImageIcon originalIcon = new ImageIcon(imgUrl);
                if (originalIcon.getImageLoadStatus() == MediaTracker.ERRORED) {
                     System.err.println("LỖI: ImageIcon báo lỗi khi tải icon: " + resourcePath);
                     return createPlaceholderIcon(width, height, "!");
                }
                Image scaledImage = originalIcon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
                return new ImageIcon(scaledImage);
            } catch (Exception e) {
                System.err.println("LỖI Exception khi xử lý icon: " + resourcePath);
                e.printStackTrace();
                return createPlaceholderIcon(width, height, "X");
            }
        } else {
            System.err.println("LỖI: Không thể tìm thấy resource icon: " + resourcePath);
            System.err.println("Hãy đảm bảo file tồn tại trong thư mục 'src/icons/' (trong thư mục build/classes hoặc jar) và đường dẫn là chính xác.");
            return createPlaceholderIcon(width, height, "?");
        }
    }

    private ImageIcon createPlaceholderIcon(int width, int height, String text) {
        BufferedImage placeholder = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = placeholder.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.fillRect(0, 0, width, height);
        g2d.setColor(Color.DARK_GRAY);
        g2d.setFont(new Font("Arial", Font.BOLD, Math.min(width, height) / 2 + 2));
        FontMetrics fm = g2d.getFontMetrics();
        int x = (width - fm.stringWidth(text)) / 2;
        int y = (fm.getAscent() + (height - (fm.getAscent() + fm.getDescent())) / 2);
        g2d.drawString(text, x, y);
        g2d.dispose();
        return new ImageIcon(placeholder);
    }

    private void createServiceSidebarPanel() {
        sidebarPanel = new JPanel();
        sidebarPanel.setBackground(SIDEEBAR_BACKGROUND);
        sidebarPanel.setPreferredSize(new Dimension(280, getHeight()));
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
        sidebarPanel.setBorder(new EmptyBorder(20, 15, 20, 15));

        JLabel titleLabel = new JLabel("<html><body style='text-align:center; width: 200px;'>Dịch Vụ Bảo Dưỡng</body></html>");
        titleLabel.setFont(FONT_SUBTITLE.deriveFont(20f));
        titleLabel.setForeground(WHITE_COLOR);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setBorder(new EmptyBorder(0,0,25,0));

        sidebarPanel.add(titleLabel);

        sidebarPanel.add(createServiceMenuItem("Đặt Lịch Bảo Dưỡng", "calendar_add.png", e -> showDatLichBaoDuongPanel()));
        sidebarPanel.add(Box.createVerticalStrut(8));
        sidebarPanel.add(createServiceMenuItem("Xem Lịch Sử Bảo Dưỡng", "history_maintenance.png", e -> showLichSuBaoDuongPanel()));
        sidebarPanel.add(Box.createVerticalStrut(8));
        sidebarPanel.add(createServiceMenuItem("Yêu Cầu Bảo Dưỡng Theo Chỉ Định", "request_service.png", e -> showYeuCauBaoDuongPanel()));
        sidebarPanel.add(Box.createVerticalStrut(8));
        sidebarPanel.add(createServiceMenuItem("Tìm Kiếm Thông Tin Thiết Bị", "search_device.png", e -> showTimKiemThietBiPanel()));
        
        sidebarPanel.add(Box.createVerticalGlue());

        JButton backButton = new JButton("<html><center>Quay Lại<br>Trang Chủ</center></html>");
        backButton.setFont(FONT_BUTTON.deriveFont(13f));
        backButton.setIcon(loadIcon("back_arrow.png", 18, 18));
        backButton.setBackground(new Color(108, 117, 125));
        backButton.setForeground(WHITE_COLOR);
        backButton.setFocusPainted(false);
        backButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        backButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backButton.setMaximumSize(new Dimension(230, 55));
        backButton.setIconTextGap(8);
        backButton.setHorizontalTextPosition(SwingConstants.RIGHT);
        backButton.addActionListener(e -> {
            if (ownerFrame != null) {
                ownerFrame.showWelcomeScreen(); 
            }
            this.dispose(); 
        });
        sidebarPanel.add(backButton);
    }

    private JPanel createServiceMenuItem(String text, String iconName, ActionListener action) {
        JPanel panel = new JPanel(new BorderLayout(10,0));
        panel.setBackground(SIDEEBAR_BACKGROUND);
        panel.setOpaque(true);
        panel.setMinimumSize(new Dimension(0, 50));
        panel.setPreferredSize(new Dimension(sidebarPanel.getPreferredSize().width - 30, 50));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 55));
        panel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        panel.setBorder(new EmptyBorder(5, 10, 5, 10));

        JLabel iconLabel = new JLabel(loadIcon(iconName, 22, 22));
        iconLabel.setVerticalAlignment(SwingConstants.CENTER);
        panel.add(iconLabel, BorderLayout.WEST);

        JLabel textLabel = new JLabel("<html><body style='width: 150px; vertical-align: middle;'>" + text + "</body></html>");
        textLabel.setFont(FONT_MENU_SERVICE);
        textLabel.setForeground(TEXT_COLOR_LIGHT);
        textLabel.setVerticalAlignment(SwingConstants.CENTER);
        panel.add(textLabel, BorderLayout.CENTER);

        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) { panel.setBackground(MENU_ITEM_HOVER_BACKGROUND); }
            @Override
            public void mouseExited(MouseEvent e) { panel.setBackground(SIDEEBAR_BACKGROUND); }
            @Override
            public void mouseClicked(MouseEvent e) { if (action != null) { action.actionPerformed(null); } }
        });
        return panel;
    }

    private void createMainContentArea() {
        mainContentPanel = new JPanel(new CardLayout());
        mainContentPanel.setBackground(LIGHT_GRAY_BACKGROUND);
        mainContentPanel.setBorder(new EmptyBorder(20, 30, 20, 30));
    }

    private void switchMainPanel(JPanel panelToShow, String panelName) {
        mainContentPanel.removeAll();
        mainContentPanel.add(panelToShow, panelName);
        CardLayout cl = (CardLayout)(mainContentPanel.getLayout());
        cl.show(mainContentPanel, panelName);
        mainContentPanel.revalidate();
        mainContentPanel.repaint();
    }
    
    // NEW HELPER METHOD TO VALIDATE BIENSO FOR THE CURRENT CUSTOMER
    private boolean kiemTraBienSoHopLeCuaKhach(String bienSo, String maKH) {
        if (bienSo == null || bienSo.trim().isEmpty() || maKH == null || maKH.trim().isEmpty()) {
            return false;
        }
        String sql = "SELECT COUNT(*) FROM XE WHERE BIENSO = ? AND MAKH = ?";
        try (Connection conn = ConnectionOracle.getOracleConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, bienSo.trim());
            pstmt.setString(2, maKH);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi kiểm tra biển số xe: " + e.getMessage());
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            System.err.println("Lỗi không tìm thấy Oracle driver: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    private List<XeDisplay> loadXeCuaKhachHang(String maKH) {
        List<XeDisplay> dsXe = new ArrayList<>();
        if (maKH == null || maKH.trim().isEmpty()) {
            System.err.println("loadXeCuaKhachHang: maKH không hợp lệ (null hoặc rỗng).");
            return dsXe; 
        }
        String sql = "SELECT x.BIENSO, x.TENLOAIXE " +
                     "FROM XE x " +
                     "WHERE x.MAKH = ? ORDER BY x.BIENSO";
        System.out.println("Executing SQL (loadXeCuaKhachHang cho MAKH: " + maKH + "): " + sql.replace("?", "'" + maKH + "'"));
        try (Connection conn = ConnectionOracle.getOracleConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, maKH);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    dsXe.add(new XeDisplay(rs.getString("BIENSO"), rs.getString("TENLOAIXE")));
                }
                if (dsXe.isEmpty()) {
                    System.out.println("loadXeCuaKhachHang: Không tìm thấy xe nào thuộc sở hữu của MAKH: " + maKH);
                } else {
                    System.out.println("loadXeCuaKhachHang: Tìm thấy " + dsXe.size() + " xe cho MAKH: " + maKH);
                }
            }
        } catch (SQLException e) {
            String errorMessage = "Lỗi SQL khi tải danh sách xe: " + e.getMessage() + " (SQLState: " + e.getSQLState() + ", ErrorCode: " + e.getErrorCode() + ")";
            System.err.println(errorMessage);
            e.printStackTrace(); 
            JOptionPane.showMessageDialog(this, errorMessage, "Lỗi Database", JOptionPane.ERROR_MESSAGE);
        } catch (ClassNotFoundException e) {
            String errorMessage = "Lỗi không tìm thấy Oracle driver: " + e.getMessage();
            System.err.println(errorMessage);
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, errorMessage, "Lỗi Driver", JOptionPane.ERROR_MESSAGE);
        }
        return dsXe;
    }

    private List<DichVuDisplay> loadDichVuBaoDuong() {
        List<DichVuDisplay> dsDichVu = new ArrayList<>();
        String sql = "SELECT MADVBAODUONG, TENDV, GIA FROM DICHVUBAODUONG ORDER BY TENDV";
        try (Connection conn = ConnectionOracle.getOracleConnection(); PreparedStatement pstmt = conn.prepareStatement(sql); ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) { dsDichVu.add(new DichVuDisplay(rs.getString("MADVBAODUONG"), rs.getString("TENDV"), rs.getDouble("GIA"))); }
        } catch (Exception e) { e.printStackTrace(); JOptionPane.showMessageDialog(this, "Lỗi tải dịch vụ: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE); }
        return dsDichVu;
    }

    private void showDatLichBaoDuongPanel() {
        JPanel datLichPanel = new JPanel(new BorderLayout(10,10));
        datLichPanel.setBackground(WHITE_COLOR); 
        datLichPanel.setBorder(BorderFactory.createCompoundBorder(new RoundedBorder(BORDER_COLOR, 15, 1), new EmptyBorder(20,20,20,20)));
        
        JLabel title = new JLabel("Đặt Lịch Bảo Dưỡng Xe", SwingConstants.CENTER); 
        title.setFont(FONT_SUBTITLE); 
        title.setForeground(TEXT_COLOR_DARK); 
        datLichPanel.add(title, BorderLayout.NORTH);
        
        JPanel formPanel = new JPanel(new GridBagLayout()); 
        formPanel.setOpaque(false); 
        GridBagConstraints gbc = new GridBagConstraints(); 
        gbc.insets = new Insets(8, 8, 8, 8); // Giảm khoảng cách để vừa thêm trường
        gbc.anchor = GridBagConstraints.WEST;
        
        // Hàng 0: Nhập biển số xe
        gbc.gridx = 0; gbc.gridy = 0; 
        formPanel.add(new JLabel("Nhập biển số xe:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; 
        gbc.fill = GridBagConstraints.HORIZONTAL; 
        gbc.weightx = 1.0; 
        txtBienSoDatLich = new JTextField(20); 
        txtBienSoDatLich.setFont(FONT_TEXT_FIELD);
        formPanel.add(txtBienSoDatLich, gbc); 
        gbc.fill = GridBagConstraints.NONE; 
        gbc.weightx = 0;

        // Hàng 1: Chọn loại xe
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Loại xe:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        cbLoaiXeDatLich = new JComboBox<>(new String[]{"Ô tô", "Xe máy"});
        cbLoaiXeDatLich.setFont(FONT_TEXT_FIELD);
        formPanel.add(cbLoaiXeDatLich, gbc);
        gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;

        // Hàng 2: Chọn dịch vụ
        gbc.gridx = 0; gbc.gridy = 2; 
        formPanel.add(new JLabel("Chọn dịch vụ:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0; 
        cbDichVuDatLich = new JComboBox<>(); 
        List<DichVuDisplay> dsDichVu = loadDichVuBaoDuong(); 
        if (dsDichVu.isEmpty()) { 
            cbDichVuDatLich.addItem(new DichVuDisplay("", "Không có dịch vụ",0)); 
            cbDichVuDatLich.setEnabled(false); 
        } else { 
            for (DichVuDisplay dv : dsDichVu) cbDichVuDatLich.addItem(dv); 
        } 
        cbDichVuDatLich.setFont(FONT_TEXT_FIELD); 
        formPanel.add(cbDichVuDatLich, gbc); 
        gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        
        // Hàng 3: Chọn ngày bảo dưỡng
        gbc.gridx = 0; gbc.gridy = 3; 
        formPanel.add(new JLabel("Chọn ngày bảo dưỡng:"), gbc);
        gbc.gridx = 1; gbc.gridy = 3; gbc.fill = GridBagConstraints.HORIZONTAL; 
        dateChooserDatLich = new JDateChooser(); 
        dateChooserDatLich.setDateFormatString("dd/MM/yyyy"); 
        dateChooserDatLich.setFont(FONT_TEXT_FIELD); 
        dateChooserDatLich.setMinSelectableDate(new Date()); 
        formPanel.add(dateChooserDatLich, gbc); gbc.fill = GridBagConstraints.NONE;
        
        // Hàng 4: Chọn giờ
        gbc.gridx = 0; gbc.gridy = 4; 
        formPanel.add(new JLabel("Chọn giờ:"), gbc);
        gbc.gridx = 1; gbc.gridy = 4; 
        cbGioDatLich = new JComboBox<>(new String[]{"08:00", "08:30", "09:00", "09:30", "10:00", "10:30", "11:00", "11:30", "13:00", "13:30", "14:00", "14:30", "15:00", "15:30", "16:00", "16:30"}); 
        cbGioDatLich.setFont(FONT_TEXT_FIELD); 
        formPanel.add(cbGioDatLich, gbc);
        
        datLichPanel.add(formPanel, BorderLayout.CENTER);
        
        JButton btnDatLich = new JButton("Đặt Lịch"); 
        btnDatLich.setFont(FONT_BUTTON); 
        btnDatLich.setBackground(PRIMARY_COLOR); 
        btnDatLich.setForeground(WHITE_COLOR); 
        btnDatLich.setPreferredSize(new Dimension(120, 40)); 
        btnDatLich.addActionListener(e -> {
            try {
                String bienSoXeNhap = txtBienSoDatLich.getText(); 
                String loaiXeChon = (String) cbLoaiXeDatLich.getSelectedItem();

                if (!handleXeRegistrationAndValidation(bienSoXeNhap, this.currentMaKH, loaiXeChon)) {
                    // Thông báo lỗi đã được hiển thị trong handleXeRegistrationAndValidation
                    if (txtBienSoDatLich != null) txtBienSoDatLich.requestFocus();
                    return;
                }
                // Nếu tới đây, biển số xe hợp lệ hoặc đã được đăng ký thành công.
                String validatedBienSo = bienSoXeNhap.trim().toUpperCase(); 

                DichVuDisplay selectedDichVu = (DichVuDisplay) cbDichVuDatLich.getSelectedItem();
                Date selectedDate = dateChooserDatLich.getDate(); 
                String gioChon = (String) cbGioDatLich.getSelectedItem();

                if (!cbDichVuDatLich.isEnabled() || selectedDichVu == null || selectedDichVu.getMaDV().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Vui lòng chọn dịch vụ hợp lệ.", "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                if (selectedDate == null) {
                    JOptionPane.showMessageDialog(this, "Vui lòng chọn ngày bảo dưỡng.", "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                Calendar calSelected = Calendar.getInstance();
                calSelected.setTime(selectedDate);
                Calendar calToday = Calendar.getInstance();
                calToday.set(Calendar.HOUR_OF_DAY, 0); calToday.set(Calendar.MINUTE, 0);
                calToday.set(Calendar.SECOND, 0); calToday.set(Calendar.MILLISECOND, 0);
                if (calSelected.before(calToday)) {
                     JOptionPane.showMessageDialog(this, "Ngày bảo dưỡng không được là ngày trong quá khứ.", "Ngày không hợp lệ", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                SimpleDateFormat dateFormatForDB = new SimpleDateFormat("yyyy-MM-dd");
                String ngayChonStr = dateFormatForDB.format(selectedDate);
                String ngayGioBaoDuongStr = ngayChonStr + " " + gioChon + ":00";

                String sqlInsert = "INSERT INTO BAODUONGXE (MABAODUONG, BIENSO, MADVBAODUONG, TRANGTHAI, NGAYBAODUONG) " +
                                   "VALUES (?, ?, ?, 'CHO THUC HIEN', TO_DATE(?, 'YYYY-MM-DD HH24:MI:SS'))";

                try (Connection conn = ConnectionOracle.getOracleConnection();
                    PreparedStatement pstmt = conn.prepareStatement(sqlInsert)) {
                    String maBaoDuong = generateNewMaBaoDuong(conn, "BD");
                    pstmt.setString(1, maBaoDuong);
                    pstmt.setString(2, validatedBienSo); 
                    pstmt.setString(3, selectedDichVu.getMaDV());
                    pstmt.setString(4, ngayGioBaoDuongStr);

                    int affectedRows = pstmt.executeUpdate();
                    if (affectedRows > 0) {
                        JOptionPane.showMessageDialog(this, "Đặt lịch bảo dưỡng thành công! Mã: " + maBaoDuong, "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                        if (txtBienSoDatLich != null) txtBienSoDatLich.setText(""); 
                        if (cbLoaiXeDatLich != null && cbLoaiXeDatLich.getItemCount() > 0) cbLoaiXeDatLich.setSelectedIndex(0);
                        if (dateChooserDatLich != null) dateChooserDatLich.setDate(null); 
                        if (cbDichVuDatLich != null && cbDichVuDatLich.getItemCount() > 0 && cbDichVuDatLich.isEnabled()) cbDichVuDatLich.setSelectedIndex(0);
                        if (cbGioDatLich != null && cbGioDatLich.getItemCount() > 0) cbGioDatLich.setSelectedIndex(0);
                    } else {
                        JOptionPane.showMessageDialog(this, "Đặt lịch thất bại. Không có bản ghi nào được thêm. Vui lòng thử lại.", "Lỗi Hệ Thống", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (SQLException sqle) {
                    sqle.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Lỗi khi đặt lịch vào database: " + sqle.getMessage() + "\nError Code: " + sqle.getErrorCode(), "Lỗi Database", JOptionPane.ERROR_MESSAGE);
                } // Không cần catch ClassNotFoundException ở đây vì đã có trong handleXeRegistrationAndValidation nếu nó được gọi từ đó
            } catch (Exception ex) { 
                 ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Có lỗi không mong muốn xảy ra: " + ex.getMessage(), "Lỗi Hệ Thống", JOptionPane.ERROR_MESSAGE);
            }
        });
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER)); 
        buttonPanel.setOpaque(false); 
        buttonPanel.setBorder(new EmptyBorder(10,0,0,0)); 
        buttonPanel.add(btnDatLich); 
        datLichPanel.add(buttonPanel, BorderLayout.SOUTH);
        switchMainPanel(datLichPanel, "DatLich");
    }
    
    /**
     * Kiểm tra biển số xe. Nếu xe đã tồn tại, xác minh quyền sở hữu.
     * Nếu xe chưa tồn tại, đăng ký xe mới cho khách hàng hiện tại.
     * @param bienSo Biển số xe cần kiểm tra/đăng ký.
     * @param currentMaKH Mã khách hàng hiện tại.
     * @param selectedTenLoaiXe Loại xe được chọn (dùng khi đăng ký xe mới).
     * @return true nếu xe hợp lệ hoặc đăng ký thành công, false nếu có lỗi.
     */
    private boolean handleXeRegistrationAndValidation(String bienSo, String currentMaKH, String selectedTenLoaiXe) {
        if (bienSo == null || bienSo.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập biển số xe.", "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        String bienSoTrimmed = bienSo.trim().toUpperCase(); // Chuẩn hóa biển số

        String checkXeSql = "SELECT MAKH, TENLOAIXE FROM XE WHERE BIENSO = ?";
        try (Connection conn = ConnectionOracle.getOracleConnection();
             PreparedStatement pstmtCheck = conn.prepareStatement(checkXeSql)) {

            pstmtCheck.setString(1, bienSoTrimmed);
            try (ResultSet rs = pstmtCheck.executeQuery()) {
                if (rs.next()) { // Xe đã tồn tại trong bảng XE
                    String existingMaKH = rs.getString("MAKH");
                    if (currentMaKH.equals(existingMaKH)) {
                        return true; // Xe tồn tại và thuộc sở hữu của khách hàng hiện tại
                    } else {
                        JOptionPane.showMessageDialog(this,
                                "Biển số xe " + bienSoTrimmed + " đã được đăng ký cho một khách hàng khác.\n" +
                                "Vui lòng kiểm tra lại hoặc liên hệ hỗ trợ.",
                                "Lỗi Biển Số", JOptionPane.ERROR_MESSAGE);
                        return false; // Xe tồn tại nhưng thuộc sở hữu của khách hàng khác
                    }
                } else { // Xe CHƯA tồn tại trong bảng XE, tiến hành đăng ký xe mới
                    if (selectedTenLoaiXe == null || selectedTenLoaiXe.trim().isEmpty() ||
                        !(selectedTenLoaiXe.equals("Xe máy") || selectedTenLoaiXe.equals("Ô tô"))) {
                        JOptionPane.showMessageDialog(this,
                                "Vui lòng chọn loại xe (Xe máy hoặc Ô tô) hợp lệ để đăng ký xe mới.",
                                "Thiếu thông tin loại xe", JOptionPane.WARNING_MESSAGE);
                        return false;
                    }

                    String insertXeSql = "INSERT INTO XE (BIENSO, TENLOAIXE, MAKH) VALUES (?, ?, ?)";
                    try (PreparedStatement pstmtInsert = conn.prepareStatement(insertXeSql)) {
                        pstmtInsert.setString(1, bienSoTrimmed);
                        pstmtInsert.setString(2, selectedTenLoaiXe);
                        pstmtInsert.setString(3, currentMaKH);
                        int affectedRows = pstmtInsert.executeUpdate();
                        if (affectedRows > 0) {
                            System.out.println("Xe mới " + bienSoTrimmed + " đã được đăng ký cho khách hàng " + currentMaKH + " với loại xe " + selectedTenLoaiXe);
                            return true; // Đăng ký xe mới thành công
                        } else {
                            JOptionPane.showMessageDialog(this, "Không thể đăng ký xe mới vào hệ thống. Vui lòng thử lại.", "Lỗi Đăng Ký Xe", JOptionPane.ERROR_MESSAGE);
                            return false;
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi cơ sở dữ liệu khi xử lý thông tin xe: " + e.getMessage(), "Lỗi Database", JOptionPane.ERROR_MESSAGE);
            return false;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi không tìm thấy driver cơ sở dữ liệu: " + e.getMessage(), "Lỗi Driver", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
    
    private void loadLichSuBaoDuongData(String maKH) {
        String[] columnNames = {"Ngày BD", "Giờ BD", "Biển Số", "Tên Dịch Vụ", "Nhân Viên TH", "Trạng Thái"};
        lichSuTableModel = new DefaultTableModel(columnNames, 0){
            @Override
            public boolean isCellEditable(int row, int column) {
               return false;
            }
        };
        if (lichSuTable != null) { 
            lichSuTable.setModel(lichSuTableModel);
        }

        if (maKH == null || maKH.trim().isEmpty()) {
            System.err.println("loadLichSuBaoDuongData: maKH không hợp lệ (null hoặc rỗng).");
            JOptionPane.showMessageDialog(this, "Mã khách hàng không hợp lệ để tải lịch sử bảo dưỡng.", "Lỗi Dữ Liệu", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Sửa câu SQL: Join BAODUONGXE với XE để lấy MAKH, sau đó lọc theo MAKH
        // Hoặc nếu BAODUONGXE đã có MAKH (nếu bạn quyết định thêm) thì sẽ đơn giản hơn.
        // Hiện tại, BIENSO trong BAODUONGXE là khóa ngoại tới XE(BIENSO), và XE có MAKH.
        String sql = "SELECT TO_CHAR(bd.NGAYBAODUONG, 'DD/MM/YYYY') AS NGAY_BD, " +
             "       TO_CHAR(bd.NGAYBAODUONG, 'HH24:MI') AS GIO_BD, " +
             "       bd.BIENSO, dv.TENDV, nv.TENNHANVIEN, bd.TRANGTHAI " +
             "FROM BAODUONGXE bd " +
             "JOIN DICHVUBAODUONG dv ON bd.MADVBAODUONG = dv.MADVBAODUONG " +
             "LEFT JOIN NHANVIEN nv ON bd.MANHANVIEN = nv.MANHANVIEN " +
             "JOIN XE x ON bd.BIENSO = x.BIENSO " + // Join với bảng XE
             "WHERE x.MAKH = ? " + // Lọc theo MAKH từ bảng XE
             "ORDER BY bd.NGAYBAODUONG DESC";
        
        System.out.println("Executing SQL (loadLichSuBaoDuongData cho MAKH: " + maKH + "): " + sql.replace("?", "'" + maKH + "'"));

        try (Connection conn = ConnectionOracle.getOracleConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, maKH);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    lichSuTableModel.addRow(new Object[]{
                            rs.getString("NGAY_BD"),
                            rs.getString("GIO_BD"),
                            rs.getString("BIENSO"),
                            rs.getString("TENDV"),
                            rs.getString("TENNHANVIEN") == null ? "Chưa có" : rs.getString("TENNHANVIEN"),
                            rs.getString("TRANGTHAI")
                    });
                }
                if (lichSuTableModel.getRowCount() == 0) {
                     System.out.println("loadLichSuBaoDuongData: Không có lịch sử bảo dưỡng nào cho các xe của MAKH: " + maKH);
                }
            }
        } catch (SQLException e) {
            String errorMessage = "Lỗi SQL khi tải lịch sử bảo dưỡng: " + e.getMessage() + " (SQLState: " + e.getSQLState() + ", ErrorCode: " + e.getErrorCode() + ")";
            System.err.println(errorMessage);
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, errorMessage, "Lỗi Database", JOptionPane.ERROR_MESSAGE);
        } catch (ClassNotFoundException e) {
             String errorMessage = "Lỗi không tìm thấy Oracle driver: " + e.getMessage();
            System.err.println(errorMessage);
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, errorMessage, "Lỗi Driver", JOptionPane.ERROR_MESSAGE);
        }
        if (lichSuTable != null) {
             lichSuTable.setModel(lichSuTableModel);
        }
    }
    
    private void showLichSuBaoDuongPanel() {
        JPanel lichSuPanel = new JPanel(new BorderLayout(10,10));
        lichSuPanel.setBackground(WHITE_COLOR); 
        lichSuPanel.setBorder(BorderFactory.createCompoundBorder(new RoundedBorder(BORDER_COLOR, 15, 1), new EmptyBorder(20,20,20,20)));
        JLabel title = new JLabel("Lịch Sử Bảo Dưỡng Xe", SwingConstants.CENTER); 
        title.setFont(FONT_SUBTITLE); 
        title.setForeground(TEXT_COLOR_DARK); 
        lichSuPanel.add(title, BorderLayout.NORTH);

        if (lichSuTable == null) { 
            lichSuTable = new JTable();
            lichSuTable.setFont(FONT_LABEL);
            lichSuTable.setRowHeight(28);
            lichSuTable.getTableHeader().setFont(FONT_BUTTON);
            lichSuTable.getTableHeader().setBackground(LIGHT_GRAY_BACKGROUND);
            lichSuTable.setFillsViewportHeight(true);
            lichSuTable.setAutoCreateRowSorter(true); 
        }
        loadLichSuBaoDuongData(this.currentMaKH); 
        JScrollPane scrollPane = new JScrollPane(lichSuTable);
        lichSuPanel.add(scrollPane, BorderLayout.CENTER);
        switchMainPanel(lichSuPanel, "LichSu");
    }
    
    private void showYeuCauBaoDuongPanel() {
        JPanel yeuCauPanel = new JPanel(new BorderLayout(10,10));
        yeuCauPanel.setBackground(WHITE_COLOR);
        yeuCauPanel.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(BORDER_COLOR, 15, 1),
            new EmptyBorder(20,20,20,20)
        ));

        JLabel title = new JLabel("Yêu Cầu Bảo Dưỡng Theo Chỉ Định", SwingConstants.CENTER);
        title.setFont(FONT_SUBTITLE);
        title.setForeground(TEXT_COLOR_DARK);
        yeuCauPanel.add(title, BorderLayout.NORTH);
        
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        // Hàng 0: Nhập biển số xe
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Nhập biển số xe:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        txtBienSoYeuCau = new JTextField(20); // THAY ĐỔI: Sử dụng JTextField
        txtBienSoYeuCau.setFont(FONT_TEXT_FIELD);
        formPanel.add(txtBienSoYeuCau, gbc);
        gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;

        // Hàng 1: Chọn loại xe
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Loại xe:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        cbLoaiXeYeuCau = new JComboBox<>(new String[]{"Ô tô", "Xe máy"}); // THAY ĐỔI: Thêm JComboBox
        cbLoaiXeYeuCau.setFont(FONT_TEXT_FIELD);
        formPanel.add(cbLoaiXeYeuCau, gbc);
        gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;

        // Hàng 2: Mô tả yêu cầu
        gbc.gridx = 0; gbc.gridy = 2; gbc.anchor = GridBagConstraints.NORTHWEST;
        formPanel.add(new JLabel("Mô tả yêu cầu chi tiết:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; gbc.gridheight = 3; gbc.fill = GridBagConstraints.BOTH; gbc.weightx = 1.0; gbc.weighty = 1.0;
        txtMoTaYeuCau = new JTextArea(5, 30);
        txtMoTaYeuCau.setFont(FONT_TEXT_FIELD);
        txtMoTaYeuCau.setLineWrap(true);
        txtMoTaYeuCau.setWrapStyleWord(true);
        JScrollPane scrollMoTa = new JScrollPane(txtMoTaYeuCau);
        formPanel.add(scrollMoTa, gbc);
        gbc.gridheight = 1; gbc.fill = GridBagConstraints.NONE; gbc.weighty = 0;

        yeuCauPanel.add(formPanel, BorderLayout.CENTER);

        JButton btnGuiYeuCau = new JButton("Gửi Yêu Cầu");
        btnGuiYeuCau.setFont(FONT_BUTTON);
        btnGuiYeuCau.setBackground(PRIMARY_COLOR);
        btnGuiYeuCau.setForeground(WHITE_COLOR);
        btnGuiYeuCau.setPreferredSize(new Dimension(150, 40));
        btnGuiYeuCau.addActionListener(e -> {
            try {
                String bienSoXeNhap = txtBienSoYeuCau.getText();
                String loaiXeChon = (String) cbLoaiXeYeuCau.getSelectedItem();
                String moTa = txtMoTaYeuCau.getText().trim();

                if (!handleXeRegistrationAndValidation(bienSoXeNhap, this.currentMaKH, loaiXeChon)) {
                    // Thông báo lỗi đã được hiển thị trong handleXeRegistrationAndValidation
                     if (txtBienSoYeuCau != null) txtBienSoYeuCau.requestFocus();
                    return;
                }
                // Nếu tới đây, biển số xe hợp lệ hoặc đã được đăng ký thành công.
                String validatedBienSo = bienSoXeNhap.trim().toUpperCase();

                if (moTa.isEmpty()){ 
                     JOptionPane.showMessageDialog(this, "Vui lòng mô tả yêu cầu bảo dưỡng của bạn.", "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
                     if (txtMoTaYeuCau != null) txtMoTaYeuCau.requestFocus();
                    return;
                }
                
                
                String sqlInsert = "INSERT INTO BAODUONGXE (MABAODUONG, BIENSO, MADVBAODUONG, TRANGTHAI, NGAYBAODUONG, MOTA) " +
                                   "VALUES (?, ?, ?, 'CHO THUC HIEN', ?, ?)"; 

                try (Connection conn = ConnectionOracle.getOracleConnection();
                     PreparedStatement pstmt = conn.prepareStatement(sqlInsert)) {
                    
                    String maBaoDuong = generateNewMaBaoDuong(conn, "BD"); // GỌI HÀM MỚI, sử dụng tiền tố "BD" theo yêu cầu chung
                    String maDVCustom = "DV_YEUCAU"; 
                    java.sql.Timestamp ngayYeuCauTimestamp = new java.sql.Timestamp(System.currentTimeMillis());
                    
                    pstmt.setString(1, maBaoDuong);
                    pstmt.setString(2, validatedBienSo); 
                    pstmt.setString(3, maDVCustom);
                    pstmt.setTimestamp(4, ngayYeuCauTimestamp);
                    pstmt.setString(5, moTa); 

                    int affectedRows = pstmt.executeUpdate();
                    if (affectedRows > 0) {
                        JOptionPane.showMessageDialog(this, "Đã gửi yêu cầu bảo dưỡng! Chúng tôi sẽ sớm liên hệ.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                        if (txtBienSoYeuCau != null) txtBienSoYeuCau.setText("");
                        if (cbLoaiXeYeuCau != null && cbLoaiXeYeuCau.getItemCount() > 0) cbLoaiXeYeuCau.setSelectedIndex(0);
                        if (txtMoTaYeuCau != null) txtMoTaYeuCau.setText("");
                    } else {
                        JOptionPane.showMessageDialog(this, "Gửi yêu cầu thất bại. Vui lòng thử lại.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    }
                } // Không cần catch ClassNotFoundException ở đây
            } catch (SQLException ex_sql) {
                ex_sql.printStackTrace();
                JOptionPane.showMessageDialog(this, "Lỗi khi gửi yêu cầu: " + ex_sql.getMessage(), "Lỗi Database", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex_gen) {
                 ex_gen.printStackTrace();
                JOptionPane.showMessageDialog(this, "Có lỗi xảy ra: " + ex_gen.getMessage(), "Lỗi Hệ Thống", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(new EmptyBorder(10,0,0,0));
        buttonPanel.add(btnGuiYeuCau);
        yeuCauPanel.add(buttonPanel, BorderLayout.SOUTH);

        switchMainPanel(yeuCauPanel, "YeuCau");
    }
    
    /**
     * Tạo mã bảo dưỡng mới tự động theo định dạng Prefix + 3 chữ số thứ tự.
     * Ví dụ: BD001, BD012, BD123.
     * @param conn Đối tượng Connection đến cơ sở dữ liệu.
     * @param prefix Tiền tố cho mã (ví dụ: "BD").
     * @return Một chuỗi mã bảo dưỡng mới.
     * @throws SQLException Nếu có lỗi khi truy vấn cơ sở dữ liệu.
     */
    private String generateNewMaBaoDuong(Connection conn, String prefix) throws SQLException {
        String newId;
        // Sử dụng LENGTH(?) trong SUBSTR và toán tử nối chuỗi || của Oracle cho LIKE
        String query = "SELECT MAX(TO_NUMBER(SUBSTR(MABAODUONG, LENGTH(?) + 1))) FROM BAODUONGXE WHERE MABAODUONG LIKE ? || '%'";

        PreparedStatement pstmt = conn.prepareStatement(query);
        pstmt.setString(1, prefix); // Tham số cho LENGTH(?)
        pstmt.setString(2, prefix); // Tham số cho LIKE ? || '%'

        ResultSet rs = pstmt.executeQuery();
        int nextNumber = 1; 
        if (rs.next()) {
            int maxNumber = rs.getInt(1); 
            if (!rs.wasNull()) { 
                 nextNumber = maxNumber + 1;
            }
        }
        rs.close();
        pstmt.close();

        newId = String.format("%s%03d", prefix, nextNumber); // Định dạng: BD001, BD011, BD123
        return newId;
    }
    
    private void loadThietBiData(String searchTerm) {
         String[] columnNames = {"Mã TB", "Tên Thiết Bị", "Giá Nhập", "Ngày Nhập", "Hạn Sử Dụng", "NV Nhập"};
        thietBiTableModel = new DefaultTableModel(columnNames, 0){
            @Override
            public boolean isCellEditable(int row, int column) {
               return false;
            }
        };

        String sql = "SELECT t.MATHIETBI, t.TENTHIETBI, t.GIANHAP, " +
                     "       TO_CHAR(t.NGAYNHAP, 'DD/MM/YYYY') AS NGAY_NHAP, " +
                     "       TO_CHAR(t.HANSUDUNG, 'DD/MM/YYYY') AS HAN_SD, " +
                     "       nv.TENNHANVIEN " +
                     "FROM THIETBI t " +
                     "LEFT JOIN NHANVIEN nv ON t.MANHANVIEN = nv.MANHANVIEN ";
        
        if (searchTerm != null && !searchTerm.isEmpty()) {
            sql += "WHERE (LOWER(t.TENTHIETBI) LIKE LOWER(?) OR LOWER(t.MATHIETBI) LIKE LOWER(?)) ";
        }
        sql += "ORDER BY t.TENTHIETBI";

        try (Connection conn = ConnectionOracle.getOracleConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            if (searchTerm != null && !searchTerm.isEmpty()) {
                pstmt.setString(1, "%" + searchTerm + "%");
                pstmt.setString(2, "%" + searchTerm + "%");
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                 NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
                while (rs.next()) {
                    thietBiTableModel.addRow(new Object[]{
                            rs.getString("MATHIETBI"),
                            rs.getString("TENTHIETBI"),
                            currencyFormat.format(rs.getDouble("GIANHAP")),
                            rs.getString("NGAY_NHAP"),
                            rs.getString("HAN_SD") == null ? "Không có" : rs.getString("HAN_SD"),
                            rs.getString("TENNHANVIEN") == null ? "N/A" : rs.getString("TENNHANVIEN")
                    });
                }
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi tải danh sách thiết bị: " + e.getMessage(), "Lỗi Database", JOptionPane.ERROR_MESSAGE);
        }

        if (thietBiTable != null) { 
            thietBiTable.setModel(thietBiTableModel);
            TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(thietBiTableModel);
            thietBiTable.setRowSorter(sorter);
        } else { 
            thietBiTable = new JTable(thietBiTableModel);
            thietBiTable.setFont(FONT_LABEL);
            thietBiTable.setRowHeight(28);
            thietBiTable.getTableHeader().setFont(FONT_BUTTON);
            thietBiTable.getTableHeader().setBackground(LIGHT_GRAY_BACKGROUND);
            thietBiTable.setFillsViewportHeight(true);
            TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(thietBiTableModel);
            thietBiTable.setRowSorter(sorter);
        }
    }
    private void showTimKiemThietBiPanel() {
        JPanel timKiemPanel = new JPanel(new BorderLayout(10,10));
        timKiemPanel.setBackground(WHITE_COLOR);
        timKiemPanel.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(BORDER_COLOR, 15, 1),
            new EmptyBorder(20,20,20,20)
        ));

        JPanel topPanel = new JPanel(new BorderLayout(10,10));
        topPanel.setOpaque(false);

        JLabel title = new JLabel("Tìm Kiếm Thông Tin Thiết Bị Bảo Dưỡng", SwingConstants.CENTER);
        title.setFont(FONT_SUBTITLE);
        title.setForeground(TEXT_COLOR_DARK);
        topPanel.add(title, BorderLayout.NORTH);

        JPanel searchBarPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        searchBarPanel.setOpaque(false);
        txtTimKiemThietBi = new JTextField(30);
        txtTimKiemThietBi.setFont(FONT_TEXT_FIELD);
        txtTimKiemThietBi.putClientProperty("JTextField.placeholderText", "Nhập tên hoặc mã thiết bị...");
        
        JButton btnTimKiemTB = new JButton("Tìm Kiếm");
        btnTimKiemTB.setFont(FONT_BUTTON);
        btnTimKiemTB.setIcon(loadIcon("search_device.png", 16,16)); 
        btnTimKiemTB.setPreferredSize(new Dimension(130,35));
        btnTimKiemTB.addActionListener(e -> loadThietBiData(txtTimKiemThietBi.getText()));
        
        searchBarPanel.add(txtTimKiemThietBi);
        searchBarPanel.add(btnTimKiemTB);
        topPanel.add(searchBarPanel, BorderLayout.CENTER);
        
        timKiemPanel.add(topPanel, BorderLayout.NORTH);

        if (thietBiTable == null) {
            thietBiTable = new JTable();
            thietBiTable.setFont(FONT_LABEL);
            thietBiTable.setRowHeight(28);
            thietBiTable.getTableHeader().setFont(FONT_BUTTON);
            thietBiTable.getTableHeader().setBackground(LIGHT_GRAY_BACKGROUND);
            thietBiTable.setFillsViewportHeight(true);
        }
        loadThietBiData(null); 
        // thietBiTable.setModel(thietBiTableModel); // Already set in loadThietBiData

        JScrollPane scrollPane = new JScrollPane(thietBiTable);
        timKiemPanel.add(scrollPane, BorderLayout.CENTER);
        switchMainPanel(timKiemPanel, "TimKiemThietBi");
    }
    
    public static void main(String args[]) {
        try { UIManager.setLookAndFeel(new FlatLightLaf()); } catch (Exception e) { e.printStackTrace(); }
        java.awt.EventQueue.invokeLater(() -> {
            UserToken dummyToken = new UserToken(); 
            dummyToken.setEntityId("DUMMY_KH_DV_MAIN"); 
            dummyToken.setEntityFullName("Dummy Customer For DV Main");
            dummyToken.setRole(RoleGroupConstants.CUSTOMER); 
            dummyToken.setAccountId(123); 
            
            // Create a dummy CustomerHomePage instance as the owner
            // This requires CustomerHomePage to have a constructor that can accept a UserToken, 
            // which it does.
            CustomerHomePage dummyOwner = new CustomerHomePage(dummyToken);
            
            // Check if dummyOwner was initialized correctly before trying to use it
             if (dummyOwner.isUserCurrentlyLoggedIn()) { // Or a more direct check if initialization succeeded
                // To prevent dummyOwner from actually showing, we don't call setVisible on it here.
                // We just need it as a valid owner for SuDungDichVuBaoDuong.
                new SuDungDichVuBaoDuong(dummyOwner).setVisible(true);
            } else {
                 System.err.println("Lỗi: Không thể khởi tạo CustomerHomePage (owner) cho SuDungDichVuBaoDuong trong main test.");
                JOptionPane.showMessageDialog(null, "Lỗi Test SuDungDichVuBaoDuong: Không thể khởi tạo frame chủ.", "Lỗi Khởi Tạo Test", JOptionPane.ERROR_MESSAGE);
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
