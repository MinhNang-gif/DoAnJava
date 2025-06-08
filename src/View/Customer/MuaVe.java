package View.Customer;

import Process.RoleGroupConstants;
import Process.UserToken;
import ConnectDB.ConnectionOracle;
import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;
import java.util.ArrayList;

// Lớp để lưu thông tin loại gửi xe từ DB
class LoaiGuiXeInfo {
    String maLoaiGuiXe;
    String tenLoaiGuiXe;
    double phiGuiXe;
    // Thêm các trường khác nếu cần, ví dụ: đơn vị tính hạn (NGAY, TUAN, THANG)
    // String donViHan; // Ví dụ: "DAY", "WEEK", "MONTH"
    // int giaTriHan;   // Ví dụ: 1 (cho vé ngày), 7 (cho vé tuần), 30 (cho vé tháng)

    public LoaiGuiXeInfo(String maLoaiGuiXe, String tenLoaiGuiXe, double phiGuiXe) {
        this.maLoaiGuiXe = maLoaiGuiXe;
        this.tenLoaiGuiXe = tenLoaiGuiXe;
        this.phiGuiXe = phiGuiXe;
    }

    @Override
    public String toString() {
        return tenLoaiGuiXe; // Hiển thị tên trong JComboBox
    }
}

public class MuaVe extends javax.swing.JFrame {
    // --- UI Styling Constants --- (giữ nguyên)
    private final Color HEADER_BACKGROUND = new Color(70, 130, 180);
    private final Color WHITE_COLOR = Color.WHITE;
    private final Color ORANGE_COLOR = new Color(255, 153, 51);
    private final Color LIGHT_GRAY_FORM_BG = new Color(248, 249, 250);
    private final Font TITLE_FONT = new Font("Arial", Font.BOLD, 24);
    private final Font REGULAR_FONT = new Font("Arial", Font.PLAIN, 14);
    private final Font BOLD_FONT = new Font("Arial", Font.BOLD, 16);
    private final Font PRICE_FONT = new Font("Arial", Font.BOLD, 15);

    // --- Owner Frame Reference ---
    private CustomerHomePage ownerFrame;

    // --- UI Components ---
    private JComboBox<LoaiGuiXeInfo> loaiGuiXeComboBox;
    private JTextField txtBienSoXe;
    private JLabel lblPhiGuiXeValue;
    private JLabel lblNgayHetHanValue;
    private JLabel currentBalanceDisplayLabel;

    private List<LoaiGuiXeInfo> danhSachLoaiGuiXe;

    public MuaVe(CustomerHomePage owner) {
        this.ownerFrame = owner;
        if (this.ownerFrame == null || !this.ownerFrame.isUserCurrentlyLoggedIn()) {
            JOptionPane.showMessageDialog(null, "Lỗi: Không thể mở màn hình Mua Vé do không có thông tin người dùng.", "Lỗi Người Dùng", JOptionPane.ERROR_MESSAGE);
            return;
        }

        danhSachLoaiGuiXe = new ArrayList<>();
        // Quan trọng: Khởi tạo JComboBox ở đây để nó tồn tại trước khi load dữ liệu
        // nếu loadDanhSachLoaiGuiXe muốn cập nhật nó ngay lập tức.
        // Tuy nhiên, cách tốt hơn là load dữ liệu vào danhSachLoaiGuiXe trước,
        // sau đó khởi tạo JComboBox với danh sách đó trong initComponentsCustom.
        // Cách hiện tại của bạn là load vào List trước, rồi mới tạo ComboBox, điều này ổn.

        loadDanhSachLoaiGuiXe(); // Tải dữ liệu

        initComponentsCustom(); // Khởi tạo UI
        updateCurrentBalanceDisplay();
    }

    private void loadDanhSachLoaiGuiXe() {
        String sql = "SELECT MALOAIGUIXE, TENLOAIGUIXE, PHIGUIXE FROM LOAIGUIXE ORDER BY MALOAIGUIXE";
        danhSachLoaiGuiXe.clear();
        System.out.println("Đang tải danh sách loại gửi xe...");
        try (Connection conn = ConnectionOracle.getOracleConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                LoaiGuiXeInfo info = new LoaiGuiXeInfo(
                        rs.getString("MALOAIGUIXE"),
                        rs.getString("TENLOAIGUIXE"),
                        rs.getDouble("PHIGUIXE")
                );
                danhSachLoaiGuiXe.add(info);
                System.out.println("Đã tải: " + info.tenLoaiGuiXe);
            }
            System.out.println("Tổng số loại vé đã tải: " + danhSachLoaiGuiXe.size());
            if (danhSachLoaiGuiXe.isEmpty()) {
                System.err.println("Không có loại gửi xe nào được tải từ bảng LOAIGUIXE. Kiểm tra dữ liệu trong bảng.");
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            String errorMessage = "Lỗi nghiêm trọng khi tải danh sách loại gửi xe.\nChi tiết: " + e.getMessage();
            JOptionPane.showMessageDialog(this, errorMessage, "Lỗi Cơ Sở Dữ Liệu", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void initComponentsCustom() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(WHITE_COLOR);

        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        headerPanel.setBackground(HEADER_BACKGROUND);
        headerPanel.setPreferredSize(new Dimension(0, 60));
        JLabel titleLabel = new JLabel("Mua Vé Gửi Xe Trực Tuyến");
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(WHITE_COLOR);
        headerPanel.add(titleLabel);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(LIGHT_GRAY_FORM_BG);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Số dư hiện tại:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        currentBalanceDisplayLabel = new JLabel("0 đ");
        currentBalanceDisplayLabel.setFont(PRICE_FONT.deriveFont(Font.BOLD));
        currentBalanceDisplayLabel.setForeground(new Color(0,100,0));
        formPanel.add(currentBalanceDisplayLabel, gbc);
        gbc.weightx = 0;

        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Loại vé:"), gbc);
        gbc.gridx = 1;
        // Khởi tạo JComboBox và điền dữ liệu từ danhSachLoaiGuiXe
        loaiGuiXeComboBox = new JComboBox<>(); // Khởi tạo trước
        if (danhSachLoaiGuiXe != null && !danhSachLoaiGuiXe.isEmpty()) {
            for (LoaiGuiXeInfo info : danhSachLoaiGuiXe) {
                loaiGuiXeComboBox.addItem(info);
            }
            loaiGuiXeComboBox.setSelectedIndex(0);
        } else {
            // Nếu danh sách vẫn rỗng sau khi load, hiển thị thông báo
            // (Có thể không cần thiết nếu loadDanhSachLoaiGuiXe đã thông báo lỗi nghiêm trọng)
            // Để tránh lỗi NullPointerException nếu không có item nào, thêm một item mặc định hoặc disable combobox
            if(loaiGuiXeComboBox.getItemCount() == 0){ // Kiểm tra lại một lần nữa
                 System.out.println("initComponentsCustom: danhSachLoaiGuiXe rỗng hoặc null. JComboBox sẽ không có item.");
                 if(SwingUtilities.getWindowAncestor(this) != null && SwingUtilities.getWindowAncestor(this).isVisible()){
                    JOptionPane.showMessageDialog(this, "Hiện tại chưa có loại vé nào được cấu hình trong hệ thống.", "Thông báo", JOptionPane.WARNING_MESSAGE);
                 }
                 // loaiGuiXeComboBox.addItem(new LoaiGuiXeInfo("NULL", "Không có loại vé", 0)); // Item placeholder
                 // loaiGuiXeComboBox.setEnabled(false); // Vô hiệu hóa nếu không có vé
            }
        }
        loaiGuiXeComboBox.setFont(REGULAR_FONT);
        loaiGuiXeComboBox.addActionListener(e -> updatePhiVaNgayHetHan());
        formPanel.add(loaiGuiXeComboBox, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Biển số xe:"), gbc);
        gbc.gridx = 1;
        txtBienSoXe = new JTextField(15);
        txtBienSoXe.setFont(REGULAR_FONT);
        formPanel.add(txtBienSoXe, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Phí gửi xe:"), gbc);
        gbc.gridx = 1;
        lblPhiGuiXeValue = new JLabel("0 đ");
        lblPhiGuiXeValue.setFont(PRICE_FONT);
        lblPhiGuiXeValue.setForeground(ORANGE_COLOR);
        formPanel.add(lblPhiGuiXeValue, gbc);

        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(new JLabel("Ngày lập vé:"), gbc);
        gbc.gridx = 1;
        JLabel lblNgayLapVeValue = new JLabel(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()));
        lblNgayLapVeValue.setFont(REGULAR_FONT);
        formPanel.add(lblNgayLapVeValue, gbc);

        gbc.gridx = 0; gbc.gridy = 5;
        formPanel.add(new JLabel("Ngày hết hạn:"), gbc);
        gbc.gridx = 1;
        lblNgayHetHanValue = new JLabel("N/A");
        lblNgayHetHanValue.setFont(REGULAR_FONT);
        formPanel.add(lblNgayHetHanValue, gbc);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setBackground(WHITE_COLOR);

        JButton purchaseButton = new JButton("Thanh Toán");
        purchaseButton.setFont(BOLD_FONT);
        purchaseButton.setBackground(ORANGE_COLOR);
        purchaseButton.setForeground(WHITE_COLOR);
        purchaseButton.setIcon(loadIcon("payment.png", 18, 18));
        purchaseButton.setPreferredSize(new Dimension(180, 45));
        purchaseButton.addActionListener(e -> processPurchase());
        buttonPanel.add(purchaseButton);

        JButton backButton = new JButton("Quay Lại");
        backButton.setFont(BOLD_FONT);
        backButton.setIcon(loadIcon("back_arrow.png", 16, 16));
        backButton.setPreferredSize(new Dimension(150, 45));
        backButton.addActionListener(e -> {
            if (ownerFrame != null) {
                ownerFrame.setEnabled(true);
                ownerFrame.showWelcomeScreen();
            }
             if (SwingUtilities.getWindowAncestor(this) instanceof JFrame) {
                 this.dispose();
             }
        });
        buttonPanel.add(backButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        this.setLayout(new BorderLayout());
        this.add(mainPanel, BorderLayout.CENTER);

        // Gọi updatePhiVaNgayHetHan sau khi tất cả UI components đã được khởi tạo và JComboBox có thể đã có item
        SwingUtilities.invokeLater(this::updatePhiVaNgayHetHan);
    }

    private void updateCurrentBalanceDisplay() {
        // ... (giữ nguyên)
        if (ownerFrame != null && currentBalanceDisplayLabel != null) {
            currentBalanceDisplayLabel.setText(formatCurrency(ownerFrame.getWalletBalanceForCurrentUser()));
        } else if (currentBalanceDisplayLabel != null) {
             currentBalanceDisplayLabel.setText("N/A");
        }
    }

    private void updatePhiVaNgayHetHan() {
        if (loaiGuiXeComboBox == null || lblPhiGuiXeValue == null || lblNgayHetHanValue == null) {
            return;
        }
        // Phải đảm bảo JComboBox đã sẵn sàng và có item
        if (loaiGuiXeComboBox.getItemCount() == 0) {
            lblPhiGuiXeValue.setText("0 đ");
            lblNgayHetHanValue.setText("N/A");
            System.out.println("updatePhiVaNgayHetHan: ComboBox không có item nào.");
            return;
        }

        LoaiGuiXeInfo selectedLoai = (LoaiGuiXeInfo) loaiGuiXeComboBox.getSelectedItem();
        if (selectedLoai != null) { // Kiểm tra selectedLoai không null
            lblPhiGuiXeValue.setText(formatCurrency(selectedLoai.phiGuiXe));
            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date());
            String tenLoaiNormalized = selectedLoai.tenLoaiGuiXe.toLowerCase();
            if (tenLoaiNormalized.contains("ngày")) {
                cal.add(Calendar.DAY_OF_MONTH, 1);
            } else if (tenLoaiNormalized.contains("tuần")) {
                cal.add(Calendar.WEEK_OF_YEAR, 1);
            } else if (tenLoaiNormalized.contains("tháng")) {
                cal.add(Calendar.MONTH, 1);
            } else {
                cal.add(Calendar.DAY_OF_MONTH, 1);
            }
            lblNgayHetHanValue.setText(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(cal.getTime()));
        } else {
            lblPhiGuiXeValue.setText("0 đ"); // Xử lý trường hợp không có item nào được chọn
            lblNgayHetHanValue.setText("N/A");
             System.out.println("updatePhiVaNgayHetHan: selectedLoai là null.");
        }
    }

    private String generateNextMa(String prefix, String tenBang, String tenCotMa) throws SQLException, ClassNotFoundException {
        // ... (giữ nguyên)
         String newId = prefix + "001";
        String query = "SELECT MAX(TO_NUMBER(SUBSTR(" + tenCotMa + ", " + (prefix.length() + 1) + "))) FROM " + tenBang +
                       " WHERE " + tenCotMa + " LIKE '" + prefix + "%'";
        int nextNum = 1;
        try (Connection conn = ConnectionOracle.getOracleConnection();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                nextNum = rs.getInt(1) + 1;
                if (rs.wasNull()) {
                    nextNum = 1;
                }
            }
        }

        if (nextNum < 10) {
            newId = prefix + "00" + nextNum;
        } else if (nextNum < 100) {
            newId = prefix + "0" + nextNum;
        } else {
            newId = prefix + nextNum;
        }
        System.out.println("Generated ID for " + tenBang + "."+ tenCotMa +": " + newId);
        return newId;
    }

    private void processPurchase() {
    // 1) Kiểm tra user
    if (ownerFrame == null || !ownerFrame.isUserCurrentlyLoggedIn()) {
        JOptionPane.showMessageDialog(this,
            "Lỗi: Phiên đăng nhập không hợp lệ.",
            "Lỗi Người Dùng",
            JOptionPane.ERROR_MESSAGE);
        return;
    }
    // 2) Kiểm tra đã có loại vé
    if (loaiGuiXeComboBox.getSelectedIndex() == -1 || loaiGuiXeComboBox.getItemCount() == 0) {
        ownerFrame.showGlobalNotification(
            "Vui lòng chọn loại vé. Hệ thống chưa có loại vé nào được cấu hình.",
            "WARNING"
        );
        return;
    }

    LoaiGuiXeInfo selectedLoai = (LoaiGuiXeInfo) loaiGuiXeComboBox.getSelectedItem();
    String bienSoXe = txtBienSoXe.getText().trim().toUpperCase();
    if (selectedLoai == null) {
        ownerFrame.showGlobalNotification("Vui lòng chọn loại vé.", "WARNING");
        return;
    }
    if (bienSoXe.isEmpty()) {
        ownerFrame.showGlobalNotification("Vui lòng nhập biển số xe.", "WARNING");
        txtBienSoXe.requestFocus();
        return;
    }

    // 3) Xác nhận phí
    double phiGuiXe = selectedLoai.phiGuiXe;
    String maKH = ownerFrame.getCurrentUserID();
    if (maKH == null) {
        ownerFrame.showGlobalNotification("Lỗi: Không xác định được mã khách hàng.", "ERROR");
        return;
    }
    String confirmMessage = String.format(
        "Xác nhận mua vé '%s' cho biển số %s với phí %s?",
        selectedLoai.tenLoaiGuiXe,
        bienSoXe,
        formatCurrency(phiGuiXe)
    );
    int confirmation = JOptionPane.showConfirmDialog(
        this, confirmMessage, "Xác nhận mua vé", JOptionPane.YES_NO_OPTION
    );
    if (confirmation != JOptionPane.YES_OPTION) {
        return;
    }

    // 4) Thanh toán trừ ví
    String transactionRef = "TICKET_" +
        selectedLoai.tenLoaiGuiXe.replaceAll("[^A-Za-z0-9]", "") +
        "_" + System.currentTimeMillis();
    boolean paymentSuccess = ownerFrame.updateWalletBalanceForCurrentUser(
        -phiGuiXe, "PURCHASE_PARKING_TICKET", transactionRef
    );
    if (!paymentSuccess) {
        // ownerFrame sẽ hiển thị lỗi đủ số dư
        return;
    }

    // 5) Thực thi transaction DB
    Connection conn = null;
    try {
        conn = ConnectionOracle.getOracleConnection();
        conn.setAutoCommit(false);

        // --- Bước 1: Thêm xe nếu cần ---
        kiemTraVaThemXeNeuCan(conn, bienSoXe, selectedLoai.tenLoaiGuiXe, maKH);

        // --- Bước 2: Tạo vé gửi xe ---
        String maVeXeMoi = generateNextMa("VX", "VEGUIXE", "MAVEXE");
        Timestamp ngayLapVeSql = new Timestamp(new Date().getTime());
        Calendar calHetHan = Calendar.getInstance();
        calHetHan.setTime(ngayLapVeSql);
        String norm = selectedLoai.tenLoaiGuiXe.toLowerCase();
        if (norm.contains("ngày"))      calHetHan.add(Calendar.DAY_OF_MONTH, 1);
        else if (norm.contains("tuần"))  calHetHan.add(Calendar.WEEK_OF_YEAR, 1);
        else if (norm.contains("tháng")) calHetHan.add(Calendar.MONTH, 1);
        else                             calHetHan.add(Calendar.DAY_OF_MONTH, 1);
        Timestamp ngayHetHanSql = new Timestamp(calHetHan.getTimeInMillis());

        String sqlVe =
            "INSERT INTO VEGUIXE (MAVEXE, BIENSO, MAKH, MALOAIGUIXE, PHIGUIXE, NGAYLAPVE, NGAYHETHAN) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmtVe = conn.prepareStatement(sqlVe)) {
            pstmtVe.setString(1, maVeXeMoi);
            pstmtVe.setString(2, bienSoXe);
            pstmtVe.setString(3, maKH);
            pstmtVe.setString(4, selectedLoai.maLoaiGuiXe);
            pstmtVe.setDouble(5, phiGuiXe);
            pstmtVe.setTimestamp(6, ngayLapVeSql);
            pstmtVe.setTimestamp(7, ngayHetHanSql);
            pstmtVe.executeUpdate();
            System.out.println("Đã lưu vé: " + maVeXeMoi);
        }

        // --- Bước 3: Tạo hóa đơn ---
        String maHoaDonMoi = generateNextMa("HD", "HOADON", "MAHOADON");
        String sqlHD =
            "INSERT INTO HOADON (MAHOADON, MAKH, NGAYLAP, TONGTIEN, TRANGTHAI, LOAIHOADON) " +
            "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmtHD = conn.prepareStatement(sqlHD)) {
            pstmtHD.setString(1, maHoaDonMoi);
            pstmtHD.setString(2, maKH);
            pstmtHD.setTimestamp(3, ngayLapVeSql);
            pstmtHD.setDouble(4, phiGuiXe);
            pstmtHD.setString(5, "DA THANH TOAN");
            pstmtHD.setString(6, "VE_XE");
            pstmtHD.executeUpdate();
            System.out.println("Đã tạo hóa đơn: " + maHoaDonMoi);
        }

        // --- Bước 4: Tạo chi tiết hóa đơn nếu chưa có ---
        String sqlCheckCT =
            "SELECT COUNT(*) FROM CHITIETHOADON WHERE MAHOADON = ?";
        try (PreparedStatement pstmtChk = conn.prepareStatement(sqlCheckCT)) {
            pstmtChk.setString(1, maHoaDonMoi);
            try (ResultSet rsChk = pstmtChk.executeQuery()) {
                if (rsChk.next() && rsChk.getInt(1) == 0) {
                    String maCTHD = generateNextMa("CTHD", "CHITIETHOADON", "MACTHD");
                    String sqlCT =
                        "INSERT INTO CHITIETHOADON (MACTHD, MAHOADON, MAVEXE, SOLUONG) " +
                        "VALUES (?, ?, ?, ?)";
                    try (PreparedStatement pstmtCT = conn.prepareStatement(sqlCT)) {
                        pstmtCT.setString(1, maCTHD);
                        pstmtCT.setString(2, maHoaDonMoi);
                        pstmtCT.setString(3, maVeXeMoi);
                        pstmtCT.setInt(4, 1);
                        pstmtCT.executeUpdate();
                        System.out.println("Đã tạo chi tiết hóa đơn vé gửi xe: " + maCTHD);
                    }
                }
            }
        }

        // --- Commit cả transaction ---
        conn.commit();
        ownerFrame.showGlobalNotification(
            "Mua vé thành công và tạo hóa đơn " + maHoaDonMoi + "!",
            "SUCCESS"
        );

        // Cập nhật lịch sử mua vé trên UI
        LichSuVeDaMua purchasedTicketLocal = new LichSuVeDaMua(
            maVeXeMoi,
            selectedLoai.tenLoaiGuiXe + (bienSoXe.isEmpty() ? "" : " (BSX: " + bienSoXe + ")"),
            1,
            phiGuiXe,
            phiGuiXe
        );
        ownerFrame.addPurchasedTicketForCurrentUser(purchasedTicketLocal);

        updateCurrentBalanceDisplay();
        txtBienSoXe.setText("");

    } catch (SQLException | ClassNotFoundException ex) {
        // Rollback nếu có lỗi
        if (conn != null) {
            try {
                conn.rollback();
                System.out.println("Đã rollback transaction.");
            } catch (SQLException e2) {
                e2.printStackTrace();
            }
        }
        ex.printStackTrace();
        ownerFrame.showGlobalNotification(
            "Lỗi khi mua vé: " + ex.getMessage(),
            "ERROR"
        );
        // Hoàn tiền
        String refundRef = "REFUND_ERR_PURCHASE_" + transactionRef;
        ownerFrame.updateWalletBalanceForCurrentUser(
            phiGuiXe, "REFUND_TICKET_PURCHASE_ERROR", refundRef
        );
        updateCurrentBalanceDisplay();
    } finally {
        if (conn != null) {
            try {
                conn.setAutoCommit(true);
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
} 


    // *** PHƯƠNG THỨC MỚI ĐỂ KIỂM TRA VÀ THÊM XE ***
    private void kiemTraVaThemXeNeuCan(Connection conn, String bienSo, String tenLoaiGuiXeUI, String maKH) throws SQLException {
        String sqlSelectXe = "SELECT COUNT(*) FROM XE WHERE BIENSO = ?";
        boolean xeDaTonTai = false;
        try (PreparedStatement pstmtSelect = conn.prepareStatement(sqlSelectXe)) {
            pstmtSelect.setString(1, bienSo);
            try (ResultSet rs = pstmtSelect.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    xeDaTonTai = true;
                    System.out.println("Biển số " + bienSo + " đã tồn tại trong bảng XE.");
                    // Optional: Kiểm tra xem có đúng MAKH không, nếu không thì báo lỗi/xử lý
                    // String sqlCheckOwner = "SELECT MAKH FROM XE WHERE BIENSO = ?";
                    // try (PreparedStatement pstmtCheckOwner = conn.prepareStatement(sqlCheckOwner)) {
                    //     pstmtCheckOwner.setString(1, bienSo);
                    //     try (ResultSet rsOwner = pstmtCheckOwner.executeQuery()) {
                    //         if (rsOwner.next()) {
                    //             String chuXeHienTai = rsOwner.getString("MAKH");
                    //             if (!maKH.equals(chuXeHienTai)) {
                    //                 // Nghiệp vụ: Biển số đã được đăng ký bởi KH khác.
                    //                 // throw new SQLException("Biển số " + bienSo + " đã được đăng ký bởi một khách hàng khác.");
                    //                 System.out.println("Cảnh báo: Biển số " + bienSo + " đã được đăng ký bởi KH " + chuXeHienTai + ", nhưng KH " + maKH + " đang mua vé.");
                    //             }
                    //         }
                    //     }
                    // }
                }
            }
        }

        if (!xeDaTonTai) {
            String tenLoaiXeDB = null; // 'Xe máy' hoặc 'Ô tô'
            String tenLoaiGuiXeNormalized = tenLoaiGuiXeUI.toLowerCase();

            if (tenLoaiGuiXeNormalized.contains("xe máy") || tenLoaiGuiXeNormalized.contains("motorcycle")) {
                tenLoaiXeDB = "Xe máy";
            } else if (tenLoaiGuiXeNormalized.contains("ô tô") || tenLoaiGuiXeNormalized.contains("car") || tenLoaiGuiXeNormalized.contains("oto")) {
                tenLoaiXeDB = "Ô tô";
            } else {
                // Nếu không suy ra được, có thể mặc định hoặc báo lỗi
                System.err.println("Không thể suy ra loại xe (Xe máy/Ô tô) từ tên loại gửi xe: " + tenLoaiGuiXeUI + " cho biển số " + bienSo);
                // Hoặc để tenLoaiXeDB là null và cột TENLOAIXE trong bảng XE cho phép null (không khuyến khích)
                // Hoặc throw new SQLException("Không thể xác định loại xe (Xe máy/Ô tô) để thêm vào bảng XE.");
                // Tạm thời để là null nếu cột cho phép, hoặc bạn cần logic chuẩn hơn
                // tenLoaiXeDB = null; // Nếu cột TENLOAIXE trong XE cho phép NULL
                // Hoặc gán một giá trị mặc định nếu nghiệp vụ cho phép:
                tenLoaiXeDB = "Xe máy"; // Mặc định là xe máy nếu không rõ
                System.out.println("Cảnh báo: Không rõ loại xe cho " + tenLoaiGuiXeUI + ", mặc định là 'Xe máy' cho BSX " + bienSo);
            }

            if (tenLoaiXeDB != null) { // Chỉ insert nếu xác định được loại xe
                String sqlInsertXe = "INSERT INTO XE (BIENSO, TENLOAIXE, MAKH) VALUES (?, ?, ?)";
                try (PreparedStatement pstmtInsert = conn.prepareStatement(sqlInsertXe)) {
                    pstmtInsert.setString(1, bienSo);
                    pstmtInsert.setString(2, tenLoaiXeDB);
                    pstmtInsert.setString(3, maKH);
                    pstmtInsert.executeUpdate();
                    System.out.println("Đã tự động thêm xe mới vào bảng XE: BSX=" + bienSo + ", Loại=" + tenLoaiXeDB + ", MAKH=" + maKH);
                }
            } else {
                 // Nếu không thể xác định tenLoaiXeDB và cột TENLOAIXE trong bảng XE là NOT NULL,
                 // thì không nên cố gắng insert mà nên throw lỗi hoặc thông báo.
                 // Hiện tại, code đã có một fallback mặc định là "Xe máy".
                 System.err.println("Không thể thêm xe " + bienSo + " vào bảng XE do không xác định được loại xe (Xe máy/Ô tô).");
                 // Nếu muốn dừng hẳn quá trình mua vé:
                 // throw new SQLException("Không thể tự động thêm xe " + bienSo + " vào hệ thống do không rõ loại xe.");
            }
        }
    }

    private String formatCurrency(double amount) {
        // ... (giữ nguyên)
        return NumberFormat.getCurrencyInstance(new Locale("vi", "VN")).format(amount);
    }

    private ImageIcon loadIcon(String iconName, int width, int height) {
        // ... (giữ nguyên)
        String resourcePath = "/icons/" + iconName;
        URL imgUrl = getClass().getResource(resourcePath);
        if (imgUrl != null) {
            try {
                ImageIcon originalIcon = new ImageIcon(imgUrl);
                Image scaledImage = originalIcon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
                return new ImageIcon(scaledImage);
            } catch (Exception e) { /* Xử lý lỗi */ }
        }
        BufferedImage placeholder = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = placeholder.createGraphics();
        g2d.setColor(Color.LIGHT_GRAY); g2d.fillRect(0,0,width,height);
        g2d.setColor(Color.DARK_GRAY); g2d.drawString("?", width/2-4, height/2+4);
        g2d.dispose();
        return new ImageIcon(placeholder);
    }

    public static void main(String args[]) {
        // ... (giữ nguyên hoặc cập nhật nếu cần)
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(new FlatLightLaf()); } catch (Exception e) { e.printStackTrace(); }
            UserToken testToken = new UserToken();
            testToken.setEntityId("KH_MV_TEST");
            testToken.setEntityFullName("Người Dùng Test Mua Vé");
            testToken.setAccountId(12345);
            if (RoleGroupConstants.CUSTOMER == null) {
                 System.err.println("FATAL: RoleGroupConstants.CUSTOMER is null. Cannot run test for MuaVe.");
                 JOptionPane.showMessageDialog(null, "Lỗi cấu hình: RoleGroupConstants.CUSTOMER không được định nghĩa.", "Lỗi Test", JOptionPane.ERROR_MESSAGE);
                return;
            }
            testToken.setRole(RoleGroupConstants.CUSTOMER);

            final CustomerHomePage testOwner = new CustomerHomePage(testToken) {
                private double mockBalance = 0;
                { this.updateWalletBalanceForCurrentUser(500000, "INITIAL_MOCK_BALANCE", "INIT_MOCK_MV"); }
                @Override public void showGlobalNotification(String message, String type) { System.out.println("Test Owner Notification [" + type + "]: " + message); JOptionPane.showMessageDialog(null, message, "Test Notification: " + type, JOptionPane.INFORMATION_MESSAGE); }
                @Override public void showWelcomeScreen() { System.out.println("Test Owner: showWelcomeScreen() called."); }
                @Override public synchronized boolean updateWalletBalanceForCurrentUser(double amount, String transactionType, String transactionReferenceID) { System.out.println("Test Owner (mock): updateWalletBalance. Amount: " + amount + ", Type: " + transactionType); if (this.mockBalance + amount < 0) { if (!transactionType.startsWith("REFUND")) { showGlobalNotification("Số dư không đủ (test mock).", "WARNING"); return false; } } this.mockBalance += amount; System.out.println("Test Owner (mock): New mock balance: " + formatCurrency(this.mockBalance)); return true; }
                @Override public double getWalletBalanceForCurrentUser() { return this.mockBalance; }
                @Override public void addPurchasedTicketForCurrentUser(LichSuVeDaMua ticket) { System.out.println("Test Owner (mock): addPurchasedTicketForCurrentUser called for ticket: " + ticket.getName()); }
                @Override public String getCurrentUserID() { UserToken currentUser = this.getCurrentUser(); return currentUser != null ? currentUser.getEntityId() : "KH_MV_TEST_DEFAULT_IN_MOCK"; }
                private String formatCurrency(double amount) { return NumberFormat.getCurrencyInstance(new Locale("vi", "VN")).format(amount); }
            };

            MuaVe muaVeFrame = new MuaVe(testOwner);
             if (muaVeFrame.ownerFrame == null || !muaVeFrame.ownerFrame.isUserCurrentlyLoggedIn()){
                 System.err.println("Không thể khởi tạo MuaVe do lỗi user token trong test main.");
             } else {
                muaVeFrame.setTitle("Test Mua Vé");
                muaVeFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                muaVeFrame.setSize(650, 600);
                muaVeFrame.setLocationRelativeTo(null);
                muaVeFrame.setVisible(true);
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
