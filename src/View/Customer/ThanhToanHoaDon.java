package View.Customer;

import com.formdev.flatlaf.FlatLightLaf;
import ConnectDB.ConnectionOracle;
import Process.RoleGroupConstants;
import Process.UserToken;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.Timestamp;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Optional;
import java.util.Vector;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;

// Import cho PDFBox
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;


public class ThanhToanHoaDon extends javax.swing.JFrame {
    // --- UI Styling Constants ---
    private final Color FRAME_PRIMARY_COLOR = new Color(60, 75, 100);
    private final Color BUTTON_EXPORT_COLOR = new Color(0, 123, 255);
    private final Color BUTTON_CLOSE_COLOR = new Color(108, 117, 125);
    private final Color TABLE_HEADER_BACKGROUND = new Color(230, 235, 240);
    private final Color TEXT_COLOR_ON_PRIMARY = Color.WHITE;
    private final Color TEXT_COLOR_DARK = new Color(33, 37, 41);
    private final Color WHITE_COLOR = Color.WHITE;
    private final Color APP_BACKGROUND = new Color(248, 249, 250);
    private final Color BORDER_COLOR = new Color(200, 205, 210);

    private final Font FONT_TITLE_FRAME = new Font("Segoe UI", Font.BOLD, 20);
    private final Font FONT_SUB_TITLE = new Font("Segoe UI", Font.BOLD, 16);
    private final Font FONT_LABEL_INFO = new Font("Segoe UI", Font.BOLD, 13);
    private final Font FONT_VALUE_INFO = new Font("Segoe UI", Font.PLAIN, 13);
    private final Font FONT_TABLE_HEADER = new Font("Segoe UI", Font.BOLD, 13);
    private final Font FONT_TABLE_CELL = new Font("Segoe UI", Font.PLAIN, 12);
    private final Font FONT_BUTTON = new Font("Segoe UI", Font.BOLD, 13);

    // --- UI Components ---
    private JLabel lblMaHoaDon, lblTenKhachHang, lblMaKhachHang, lblDiaChiKH, lblSdtKH;
    private JLabel lblNgayLap, lblTongTienHD, lblTrangThaiHD;
    private JTable tblChiTietHoaDon;
    private DefaultTableModel chiTietTableModel;
    private JButton btnXuatHoaDon, btnDong;
    private JLabel lblThoiGianXuat;

    // --- Formatters ---
    private final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    private final SimpleDateFormat dateTimeFormatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

    // --- Data and Owner ---
    private final String maHoaDon = null;
    private String currentMaHoaDon;
    private CustomerHomePage ownerFrame;
    private String currentInvoiceStatus;
    private String currentLoaiHoaDon; // << THÊM BIẾN NÀY
    private String currentMaKhachHangHD; // Lưu mã khách hàng của hóa đơn để truy vấn vé

    public ThanhToanHoaDon(String maHoaDon, CustomerHomePage owner) {
        this.currentMaHoaDon = maHoaDon;
        this.ownerFrame = owner;

        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception e) {
            System.err.println("Failed to initialize LaF: " + e.getMessage());
        }

        setTitle("Chi Tiết Hóa Đơn - " + this.currentMaHoaDon);
        setSize(850, 730);
        setMinimumSize(new Dimension(700, 650));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(ownerFrame);
        getContentPane().setBackground(APP_BACKGROUND);
        setLayout(new BorderLayout(10, 10));

        initComponentsCustom();
        loadHoaDonInfo();
        loadChiTietHoaDon();

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                handleWindowClose();
            }
        });
    }

    private void handleWindowClose() {
        if (ownerFrame != null) {
            ownerFrame.setEnabled(true);
            ownerFrame.requestFocus();
        }
        // this.dispose(); // JFrame.DISPOSE_ON_CLOSE sẽ tự xử lý việc này
    }
    
    private void initComponentsCustom() {
        // ... (Nội dung của initComponentsCustom không thay đổi) ...
        // Panel Tiêu đề Frame
        JPanel titleFramePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        titleFramePanel.setBackground(FRAME_PRIMARY_COLOR);
        titleFramePanel.setBorder(new EmptyBorder(10, 0, 10, 0));
        JLabel lblTitleFrame = new JLabel("CHI TIẾT HÓA ĐƠN");
        lblTitleFrame.setFont(FONT_TITLE_FRAME);
        lblTitleFrame.setForeground(TEXT_COLOR_ON_PRIMARY);
        titleFramePanel.add(lblTitleFrame);

        // Panel Thông tin chung
        JPanel infoPanel = new JPanel(new GridBagLayout());
        infoPanel.setBackground(WHITE_COLOR);
        infoPanel.setBorder(BorderFactory.createCompoundBorder(
                new EmptyBorder(15, 20, 15, 20),
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(BORDER_COLOR), "Thông Tin Chung",
                        javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                        javax.swing.border.TitledBorder.DEFAULT_POSITION, FONT_SUB_TITLE, TEXT_COLOR_DARK)));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0; infoPanel.add(createLabel("Mã Hóa Đơn:", FONT_LABEL_INFO), gbc);
        gbc.gridx = 1; lblMaHoaDon = createValueLabel("", FONT_VALUE_INFO); infoPanel.add(lblMaHoaDon, gbc);
        gbc.gridx = 0; gbc.gridy = 1; infoPanel.add(createLabel("Ngày Lập:", FONT_LABEL_INFO), gbc);
        gbc.gridx = 1; lblNgayLap = createValueLabel("", FONT_VALUE_INFO); infoPanel.add(lblNgayLap, gbc);
        gbc.gridx = 0; gbc.gridy = 2; infoPanel.add(createLabel("Trạng Thái:", FONT_LABEL_INFO), gbc);
        gbc.gridx = 1; lblTrangThaiHD = createValueLabel("", FONT_VALUE_INFO); infoPanel.add(lblTrangThaiHD, gbc);

        gbc.gridx = 2; gbc.gridy = 0; gbc.insets = new Insets(5, 25, 5, 10); infoPanel.add(createLabel("Khách Hàng:", FONT_LABEL_INFO), gbc);
        gbc.gridx = 3; gbc.insets = new Insets(5, 10, 5, 10); lblTenKhachHang = createValueLabel("", FONT_VALUE_INFO); infoPanel.add(lblTenKhachHang, gbc);
        gbc.gridx = 2; gbc.gridy = 1; gbc.insets = new Insets(5, 25, 5, 10); infoPanel.add(createLabel("Mã KH:", FONT_LABEL_INFO), gbc);
        gbc.gridx = 3; gbc.insets = new Insets(5, 10, 5, 10); lblMaKhachHang = createValueLabel("", FONT_VALUE_INFO); infoPanel.add(lblMaKhachHang, gbc);
        gbc.gridx = 2; gbc.gridy = 2; gbc.insets = new Insets(5, 25, 5, 10); infoPanel.add(createLabel("Địa Chỉ:", FONT_LABEL_INFO), gbc);
        gbc.gridx = 3; gbc.insets = new Insets(5, 10, 5, 10); lblDiaChiKH = createValueLabel("", FONT_VALUE_INFO); infoPanel.add(lblDiaChiKH, gbc);
        gbc.gridx = 2; gbc.gridy = 3; gbc.insets = new Insets(5, 25, 5, 10); infoPanel.add(createLabel("Điện Thoại:", FONT_LABEL_INFO), gbc);
        gbc.gridx = 3; gbc.insets = new Insets(5, 10, 5, 10); lblSdtKH = createValueLabel("", FONT_VALUE_INFO); infoPanel.add(lblSdtKH, gbc);

        // Panel Chi Tiết Hóa Đơn
        JPanel detailsPanel = new JPanel(new BorderLayout());
        detailsPanel.setBackground(WHITE_COLOR);
        detailsPanel.setBorder(BorderFactory.createCompoundBorder(
                new EmptyBorder(0, 20, 10, 20),
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(BORDER_COLOR), "Chi Tiết Dịch Vụ",
                        javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                        javax.swing.border.TitledBorder.DEFAULT_POSITION, FONT_SUB_TITLE, TEXT_COLOR_DARK)));
        String[] columnNames = {"STT", "Tên Dịch Vụ", "Số Lượng", "Đơn Giá", "Thành Tiền"};
        chiTietTableModel = new DefaultTableModel(columnNames, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        tblChiTietHoaDon = new JTable(chiTietTableModel);
        setupTableStyle(tblChiTietHoaDon);
        JScrollPane scrollPane = new JScrollPane(tblChiTietHoaDon);
        scrollPane.getViewport().setBackground(WHITE_COLOR);
        detailsPanel.add(scrollPane, BorderLayout.CENTER);

        // Panel Tổng tiền, Nút bấm và Thời gian xuất
        JPanel bottomOuterPanel = new JPanel(new BorderLayout(10, 5));
        bottomOuterPanel.setBackground(APP_BACKGROUND);
        bottomOuterPanel.setBorder(new EmptyBorder(10, 20, 15, 20));

        JPanel topOfBottomPanel = new JPanel(new BorderLayout(10, 0));
        topOfBottomPanel.setOpaque(false);

        JPanel totalPanel = new JPanel(new FlowLayout(FlowLayout.LEFT)); 
        totalPanel.setOpaque(false);
        totalPanel.add(createLabel("Tổng Cộng Hóa Đơn:", FONT_LABEL_INFO.deriveFont(14f)));
        lblTongTienHD = createValueLabel("0 đ", FONT_SUB_TITLE.deriveFont(Font.BOLD, 16f));
        lblTongTienHD.setForeground(FRAME_PRIMARY_COLOR);
        totalPanel.add(lblTongTienHD);
        
        JPanel buttonActionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonActionsPanel.setOpaque(false);
        btnXuatHoaDon = new JButton("Xuất Hóa Đơn");
        styleButton(btnXuatHoaDon, BUTTON_EXPORT_COLOR, TEXT_COLOR_ON_PRIMARY);
        btnXuatHoaDon.setIcon(loadIcon("receipt.png", 16,16)); 
        btnXuatHoaDon.addActionListener(e -> xuatHoaDonAction());

        btnDong = new JButton("Đóng");
        styleButton(btnDong, BUTTON_CLOSE_COLOR, TEXT_COLOR_ON_PRIMARY);
        btnDong.setIcon(loadIcon("delete.png", 16,16)); 
        btnDong.addActionListener(e -> {this.dispose(); handleWindowClose();}); 
        
        buttonActionsPanel.add(btnXuatHoaDon);
        buttonActionsPanel.add(btnDong);

        topOfBottomPanel.add(totalPanel, BorderLayout.WEST);
        topOfBottomPanel.add(buttonActionsPanel, BorderLayout.EAST);

        JPanel exportTimePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        exportTimePanel.setOpaque(false);
        lblThoiGianXuat = new JLabel("");
        lblThoiGianXuat.setFont(FONT_VALUE_INFO.deriveFont(Font.ITALIC));
        lblThoiGianXuat.setForeground(TEXT_COLOR_DARK);
        exportTimePanel.add(lblThoiGianXuat);

        bottomOuterPanel.add(topOfBottomPanel, BorderLayout.NORTH);
        bottomOuterPanel.add(exportTimePanel, BorderLayout.CENTER); 

        // Gộp các Panel chính
        JPanel mainContentPanel = new JPanel(new BorderLayout(0,0));
        mainContentPanel.setBackground(APP_BACKGROUND);
        mainContentPanel.add(infoPanel, BorderLayout.NORTH);
        mainContentPanel.add(detailsPanel, BorderLayout.CENTER);

        add(titleFramePanel, BorderLayout.NORTH);
        add(mainContentPanel, BorderLayout.CENTER);
        add(bottomOuterPanel, BorderLayout.SOUTH);
    }
    
    private JLabel createLabel(String text, Font font) { /* ... Giữ nguyên ... */ 
        JLabel label = new JLabel(text);
        label.setFont(font);
        label.setForeground(TEXT_COLOR_DARK);
        return label;
    }
    private JLabel createValueLabel(String text, Font font) { /* ... Giữ nguyên ... */ 
        JLabel label = new JLabel(text);
        label.setFont(font);
        label.setForeground(TEXT_COLOR_DARK.darker());
        return label;
    }
    private void setupTableStyle(JTable table) { /* ... Giữ nguyên ... */ 
        table.setFont(FONT_TABLE_CELL);
        table.setRowHeight(30);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setGridColor(BORDER_COLOR);
        table.setShowGrid(true);
        table.setIntercellSpacing(new Dimension(0, 0));

        JTableHeader tableHeader = table.getTableHeader();
        tableHeader.setFont(FONT_TABLE_HEADER);
        tableHeader.setBackground(TABLE_HEADER_BACKGROUND);
        tableHeader.setForeground(TEXT_COLOR_DARK);
        tableHeader.setReorderingAllowed(false);
        tableHeader.setPreferredSize(new Dimension(tableHeader.getWidth(), 35));

        TableColumnModel columnModel = table.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(40);
        columnModel.getColumn(1).setPreferredWidth(250);
        columnModel.getColumn(2).setPreferredWidth(80);
        columnModel.getColumn(3).setPreferredWidth(120);
        columnModel.getColumn(4).setPreferredWidth(120);
        
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);

        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        table.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);
        table.getColumnModel().getColumn(4).setCellRenderer(rightRenderer);
    }
    private void loadHoaDonInfo() {
    // 1) Chuẩn style/renderer cho bảng chi tiết
    setupTableStyle(tblChiTietHoaDon);

    // 2) Load chung từ HOADON JOIN KHACHHANG
    String sql = ""
        + "SELECT hd.MAKH    AS MAKH, "
        + "       kh.TENKH   AS TENKH, "
        + "       kh.DIACHI AS DIACHI, "
        + "       kh.SDT    AS SDT, "
        + "       hd.NGAYLAP, "
        + "       hd.TONGTIEN, "
        + "       hd.TRANGTHAI, "
        + "       hd.LOAIHOADON "
        + "  FROM HOADON hd "
        + "  JOIN KHACHHANG kh ON hd.MAKH = kh.MAKH "
        + " WHERE hd.MAHOADON = ?";

    try (
        Connection conn = ConnectionOracle.getOracleConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)
    ) {
        pstmt.setString(1, this.currentMaHoaDon);
        try (ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                // Thông tin chung
                String makh = rs.getString("MAKH");
                lblMaHoaDon.setText(this.currentMaHoaDon);
                lblMaKhachHang.setText(makh);
                lblTenKhachHang.setText(rs.getString("TENKH"));
                lblDiaChiKH.setText(Optional.ofNullable(rs.getString("DIACHI")).orElse("N/A"));
                lblSdtKH.setText(Optional.ofNullable(rs.getString("SDT")).orElse("N/A"));

                java.sql.Timestamp ts = rs.getTimestamp("NGAYLAP");
                lblNgayLap.setText(ts != null
                    ? dateTimeFormatter.format(ts)
                    : "N/A"
                );

                // sau khi đã đọc được rs.next()
                String loaiHD = rs.getString("LOAIHOADON");

                // nếu là vé gửi xe thì tính lại bằng computeInvoiceTotal(), 
                // còn bình thường thì lấy luôn từ CSDL
                double tongHD;
                if ("VE_XE".equalsIgnoreCase(loaiHD)) {
                    tongHD = computeInvoiceTotal(conn, this.currentMaHoaDon); // phòng trường hợp lỗi, vẫn fallback về giá trị cũ
                } else {
                    tongHD = rs.getDouble("TONGTIEN");
                }

                lblTongTienHD.setText(currencyFormatter.format(tongHD));

                String trangThai = rs.getString("TRANGTHAI");
                lblTrangThaiHD.setText(trangThai != null
                    ? trangThai.toUpperCase()
                    : "N/A"
                );

                // Lưu lại để load chi tiết
                this.currentLoaiHoaDon = rs.getString("LOAIHOADON");
                this.currentMaKhachHangHD = makh;
                this.currentInvoiceStatus = trangThai;
            }
        }

    } catch (SQLException | ClassNotFoundException ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(this,
            "Lỗi khi tải thông tin chung: " + ex.getMessage(),
            "Lỗi", JOptionPane.ERROR_MESSAGE
        );
    }

    // 3) Sau khi đã có currentLoaiHoaDon, load chi tiết
    loadChiTietHoaDon();
}

    private double computeInvoiceTotal(Connection conn, String maHoaDon) throws SQLException {
    String sql =
      "SELECT SUM( " +
      "  CASE " +
      "    WHEN ct.MAVEXE             IS NOT NULL THEN vx.PHIGUIXE * ct.SOLUONG " +
      "    ELSE dv.GIA                * ct.SOLUONG " +   // ← cột GIA đúng trong DICHVUBAODUONG
      "  END" +
      ") AS TONG " +
      "FROM CHITIETHOADON ct " +
      "LEFT JOIN VEGUIXE vx            ON ct.MAVEXE       = vx.MAVEXE " +
      "LEFT JOIN DICHVUBAODUONG dv     ON ct.MADVBAODUONG = dv.MADVBAODUONG " +
      "WHERE ct.MAHOADON = ?";
    try (PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setString(1, maHoaDon);
        try (ResultSet rs = ps.executeQuery()) {
            return (rs.next() ? rs.getDouble("TONG") : 0.0);
        }
    }
}

    
    // Hàm helper để kiểm tra xem hóa đơn có chi tiết dịch vụ không
    private boolean hasChiTietDichVu(Connection conn, String maHoaDon) throws SQLException {
        String sqlCheck = "SELECT 1 FROM CHITIETHOADON WHERE MAHOADON = ?";
        try (PreparedStatement pstmtCheck = conn.prepareStatement(sqlCheck)) {
            pstmtCheck.setString(1, maHoaDon);
            try (ResultSet rsCheck = pstmtCheck.executeQuery()) {
                return rsCheck.next(); // Trả về true nếu có ít nhất 1 dòng
            }
        }
    }
    
    /**
 * Thay thế hoàn toàn cái hàm “phỏng đoán” VE_XE cũ
 * Giờ chỉ dựa vào CHITIETHOADON → DICHVUBAODUONG hoặc VEGUIXE + LOAIGUIXE
 */
private void loadChiTietHoaDon() {
    chiTietTableModel.setRowCount(0);
    if (this.currentMaHoaDon == null || this.currentMaHoaDon.isEmpty()) return;

    String sqlDetail;
    if ("VE_BAO_DUONG".equalsIgnoreCase(this.currentLoaiHoaDon)) {
        sqlDetail =
          "SELECT c.MACTHD, d.TENDV AS TEN, c.SOLUONG, d.GIA, c.SOLUONG * d.GIA AS THANHTIEN " +
          "  FROM CHITIETHOADON c " +
          "  JOIN DICHVUBAODUONG d ON c.MADVBAODUONG = d.MADVBAODUONG " +
          " WHERE c.MAHOADON = ?";
    } else {  // VE_XE
        sqlDetail =
          "SELECT c.MACTHD, lg.TENLOAIGUIXE AS TEN, 1 AS SOLUONG, vg.PHIGUIXE AS GIA, vg.PHIGUIXE AS THANHTIEN " +
          "  FROM CHITIETHOADON c " +
          "  JOIN VEGUIXE vg ON c.MAVEXE = vg.MAVEXE " +
          "  JOIN LOAIGUIXE lg ON vg.MALOAIGUIXE = lg.MALOAIGUIXE " +
          " WHERE c.MAHOADON = ?";
    }

    try (Connection conn = ConnectionOracle.getOracleConnection();
         PreparedStatement p = conn.prepareStatement(sqlDetail)) {

        p.setString(1, this.currentMaHoaDon);
        try (ResultSet rs = p.executeQuery()) {
            int stt = 1;
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(stt++);                              // STT
                row.add(rs.getString("TEN"));                // Tên dịch vụ / loại gửi xe
                row.add(rs.getInt("SOLUONG"));               // Số lượng
                row.add(currencyFormatter.format(rs.getDouble("GIA")));       // Đơn giá
                row.add(currencyFormatter.format(rs.getDouble("THANHTIEN"))); // Thành tiền
                chiTietTableModel.addRow(row);
            }
        }
    } catch (SQLException | ClassNotFoundException ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(this,
            "Lỗi khi tải chi tiết hóa đơn: " + ex.getMessage(),
            "Lỗi", JOptionPane.ERROR_MESSAGE
        );
    }
}

    
    public void refreshInvoiceDetails() { /* ... Giữ nguyên ... */ 
        System.out.println("ThanhToanHoaDon: Refreshing invoice details for " + currentMaHoaDon);
        loadHoaDonInfo();
        loadChiTietHoaDon();
        if ("DA THANH TOAN".equalsIgnoreCase(this.currentInvoiceStatus)) {
            lblThoiGianXuat.setText("");
        }
    }

    private void xuatHoaDonAction() {
        if (this.currentInvoiceStatus == null || this.currentInvoiceStatus.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Không thể xác định trạng thái hóa đơn để xuất.", "Lỗi Trạng Thái", JOptionPane.ERROR_MESSAGE);
            lblThoiGianXuat.setText("");
            return;
        }

        if ("CHUA THANH TOAN".equalsIgnoreCase(this.currentInvoiceStatus)) {
            int choice = JOptionPane.showConfirmDialog(this,
                "Hóa đơn này CHƯA THANH TOÁN.\nBạn có muốn thanh toán ngay bây giờ không?",
                "Xác Nhận Thanh Toán",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

            if (choice == JOptionPane.YES_OPTION) {
                ThanhToanHoaDonNgayLapTuc paymentScreen = new ThanhToanHoaDonNgayLapTuc(currentMaHoaDon, ownerFrame, this);
                paymentScreen.setVisible(true);
                this.setEnabled(false);
            } else {
                JOptionPane.showMessageDialog(this,
                    "Vui lòng thanh toán hóa đơn trước khi xuất.",
                    "Yêu Cầu Thanh Toán",
                    JOptionPane.WARNING_MESSAGE);
                lblThoiGianXuat.setText("");
            }
        } else if ("DA THANH TOAN".equalsIgnoreCase(this.currentInvoiceStatus)) {
            String thoiGianHienTaiStr = dateTimeFormatter.format(new Date());
            String thoiGianXuatDayDu = "Thời gian xuất hóa đơn: " + thoiGianHienTaiStr;

            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Chọn nơi lưu file PDF hóa đơn");

            String maKhachHangPDF = lblMaKhachHang.getText();
            if (maKhachHangPDF == null || maKhachHangPDF.trim().isEmpty() || "N/A".equalsIgnoreCase(maKhachHangPDF)) {
                maKhachHangPDF = "UnknownKH";
            }
            String defaultFileName = "HoaDon_" + currentMaHoaDon + "_" + maKhachHangPDF + ".pdf";

            fileChooser.setSelectedFile(new File(defaultFileName));
            FileNameExtensionFilter filter = new FileNameExtensionFilter("PDF Documents (*.pdf)", "pdf");
            fileChooser.setFileFilter(filter);

            int userSelection = fileChooser.showSaveDialog(this);

            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File fileToSave = fileChooser.getSelectedFile();
                String filePath = fileToSave.getAbsolutePath();
                if (!filePath.toLowerCase().endsWith(".pdf")) {
                    filePath += ".pdf";
                }

                try {
                    generateInvoiceToPDF(filePath, thoiGianXuatDayDu);
                    JOptionPane.showMessageDialog(this,
                        "Hóa đơn đã được xuất ra file PDF thành công!\nĐường dẫn: " + filePath,
                        "Xuất PDF Thành Công",
                        JOptionPane.INFORMATION_MESSAGE);
                    lblThoiGianXuat.setText(thoiGianXuatDayDu);
                    lblThoiGianXuat.setVisible(true);
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(this,
                        "Có lỗi xảy ra khi tạo file PDF: " + e.getMessage(),
                        "Lỗi Xuất PDF",
                        JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace(); 
                    lblThoiGianXuat.setText("Lỗi khi xuất PDF: " + e.getMessage().split("\n")[0]); 
                    lblThoiGianXuat.setVisible(true);
                } catch (Exception e) { 
                     JOptionPane.showMessageDialog(this,
                        "Có lỗi không xác định xảy ra khi tạo file PDF: " + e.getMessage(),
                        "Lỗi Không Xác Định",
                        JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                    lblThoiGianXuat.setText("Lỗi không xác định khi xuất PDF.");
                    lblThoiGianXuat.setVisible(true);
                }
            } else {
                lblThoiGianXuat.setText("Xuất PDF đã được hủy bởi người dùng.");
                lblThoiGianXuat.setVisible(true);
            }
        } else {
            JOptionPane.showMessageDialog(this,
                "Trạng thái hóa đơn không xác định (" + this.currentInvoiceStatus + "), không thể xuất.",
                "Lỗi Trạng Thái",
                JOptionPane.ERROR_MESSAGE);
            lblThoiGianXuat.setText("");
        }
    }

    private void generateInvoiceToPDF(String filePath, String thoiGianXuatFull) throws IOException {
        // Load fonts on demand for this PDF generation, associated with the new PDDocument
        PDFont localFontRegular = null;
        PDFont localFontBold = null;

        try (PDDocument document = new PDDocument(); // PDDocument này sẽ được dùng để tạo PDF thực sự
             InputStream fontStreamReg = getClass().getResourceAsStream("/fonts/arial.ttf");
             InputStream fontStreamBld = getClass().getResourceAsStream("/fonts/arialbd.ttf")) {

            if (fontStreamReg != null) {
                localFontRegular = PDType0Font.load(document, fontStreamReg);
            } else {
                throw new IOException("Không tìm thấy file font regular: /fonts/arial.ttf");
            }

            if (fontStreamBld != null) {
                localFontBold = PDType0Font.load(document, fontStreamBld);
            } else {
                System.err.println("Không tìm thấy file font bold: /fonts/arialbd.ttf. Sử dụng font regular cho bold.");
                localFontBold = localFontRegular; // Fallback
            }
            
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            float margin = 40;
            float yStart = page.getMediaBox().getHeight() - margin;
            float yPosition = yStart;
            float pageWidth = page.getMediaBox().getWidth();
            float contentWidth = pageWidth - 2 * margin;

            float leadingSmall = 13f;
            float leadingMedium = 16f;
            float leadingLarge = 20f;
            float sectionSpacing = leadingLarge * 0.75f;
            float lineSpacingAfterTitle = leadingMedium * 0.4f;
            float lineSpacingAfterLine = leadingMedium * 0.6f;

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                // --- 1. Tiêu đề Frame ---
                yPosition -= leadingLarge * 0.5f;
                drawTextCentered(contentStream, "CHI TIẾT HÓA ĐƠN", localFontBold, 18, yPosition, pageWidth);
                yPosition -= sectionSpacing * 1.5f;

                // --- 2. Thông Tin Chung ---
                drawText(contentStream, "Thông Tin Chung", localFontBold, 14, margin, yPosition);
                yPosition -= lineSpacingAfterTitle;
                contentStream.setLineWidth(0.75f);
                contentStream.moveTo(margin, yPosition);
                contentStream.lineTo(margin + contentWidth, yPosition);
                contentStream.stroke();
                yPosition -= lineSpacingAfterLine;

                float col1X = margin;
                float col2X = margin + contentWidth / 2f + 10f;
                float labelWidth1 = 90f;
                float labelWidth2 = 80f;
                float initialYInfo = yPosition;

                yPosition = drawKeyValueLine(contentStream, "Mã Hóa Đơn:", lblMaHoaDon.getText(), localFontBold, localFontRegular, 11, col1X, yPosition, labelWidth1, leadingSmall);
                yPosition = drawKeyValueLine(contentStream, "Ngày Lập:", lblNgayLap.getText(), localFontBold, localFontRegular, 11, col1X, yPosition, labelWidth1, leadingSmall);
                yPosition = drawKeyValueLine(contentStream, "Trạng Thái:", lblTrangThaiHD.getText(), localFontBold, localFontRegular, 11, col1X, yPosition, labelWidth1, leadingSmall);
                float yPosCol1End = yPosition;

                yPosition = initialYInfo;
                yPosition = drawKeyValueLine(contentStream, "Khách Hàng:", lblTenKhachHang.getText(), localFontBold, localFontRegular, 11, col2X, yPosition, labelWidth2, leadingSmall);
                yPosition = drawKeyValueLine(contentStream, "Mã KH:", lblMaKhachHang.getText(), localFontBold, localFontRegular, 11, col2X, yPosition, labelWidth2, leadingSmall);
                yPosition = drawKeyValueLine(contentStream, "Địa Chỉ:", lblDiaChiKH.getText(), localFontBold, localFontRegular, 11, col2X, yPosition, labelWidth2, leadingSmall);
                yPosition = drawKeyValueLine(contentStream, "Điện Thoại:", lblSdtKH.getText(), localFontBold, localFontRegular, 11, col2X, yPosition, labelWidth2, leadingSmall);
                float yPosCol2End = yPosition;

                yPosition = Math.min(yPosCol1End, yPosCol2End) - sectionSpacing;

                // --- 3. Chi Tiết Dịch Vụ (Bảng) ---
                drawText(contentStream, "Chi Tiết Dịch Vụ", localFontBold, 14, margin, yPosition);
                yPosition -= lineSpacingAfterTitle;
                contentStream.moveTo(margin, yPosition);
                contentStream.lineTo(margin + contentWidth, yPosition);
                contentStream.stroke();
                yPosition -= lineSpacingAfterLine * 0.5f;

                String[] headers = {"STT", "Tên Dịch Vụ", "SL", "Đơn Giá", "Thành Tiền"};
                float sttColWidth = contentWidth * 0.08f;
                float tenDVColWidth = contentWidth * 0.39f;
                float slColWidth = contentWidth * 0.10f;
                float donGiaColWidth = contentWidth * 0.21f;
                float thanhTienColWidth = contentWidth * 0.22f;
                float[] colWidths = {sttColWidth, tenDVColWidth, slColWidth, donGiaColWidth, thanhTienColWidth};
                float tableRowHeight = leadingMedium * 1.15f;

                yPosition -= tableRowHeight;
                float currentX = margin;
                for (int i = 0; i < headers.length; i++) {
                    drawTableCell(contentStream, headers[i], localFontBold, 10.5f, currentX, yPosition, colWidths[i], tableRowHeight, "CENTER", true);
                    currentX += colWidths[i];
                }
                contentStream.moveTo(margin, yPosition);
                contentStream.lineTo(margin + contentWidth, yPosition);
                contentStream.stroke();

                DefaultTableModel model = (DefaultTableModel) tblChiTietHoaDon.getModel();
                for (int i = 0; i < model.getRowCount(); i++) {
                    yPosition -= tableRowHeight;
                    currentX = margin;
                    drawTableCell(contentStream, model.getValueAt(i, 0).toString(), localFontRegular, 10f, currentX, yPosition, colWidths[0], tableRowHeight, "CENTER", false); currentX += colWidths[0];
                    drawTableCell(contentStream, model.getValueAt(i, 1).toString(), localFontRegular, 10f, currentX, yPosition, colWidths[1], tableRowHeight, "LEFT", false);   currentX += colWidths[1];
                    drawTableCell(contentStream, model.getValueAt(i, 2).toString(), localFontRegular, 10f, currentX, yPosition, colWidths[2], tableRowHeight, "CENTER", false); currentX += colWidths[2];
                    drawTableCell(contentStream, model.getValueAt(i, 3).toString(), localFontRegular, 10f, currentX, yPosition, colWidths[3], tableRowHeight, "RIGHT", false);  currentX += colWidths[3];
                    drawTableCell(contentStream, model.getValueAt(i, 4).toString(), localFontRegular, 10f, currentX, yPosition, colWidths[4], tableRowHeight, "RIGHT", false);

                    contentStream.moveTo(margin, yPosition);
                    contentStream.lineTo(margin + contentWidth, yPosition);
                    contentStream.stroke();
                }

                float tableTopYLine = yPosition + (model.getRowCount() +1) * tableRowHeight;
                currentX = margin;
                for(int i=0; i<=headers.length; i++){
                    contentStream.moveTo(currentX, tableTopYLine);
                    contentStream.lineTo(currentX, yPosition);
                    contentStream.stroke();
                    if(i < headers.length) currentX += colWidths[i];
                }
                yPosition -= sectionSpacing;

                // --- 4. Tổng Cộng Hóa Đơn ---
                String tongCongLabel = "Tổng Cộng Hóa Đơn:";
                String tongCongValue = lblTongTienHD.getText();
                float tongCongLabelWidth = localFontBold.getStringWidth(tongCongLabel) / 1000f * 12f;
                float tongCongValueWidth = localFontBold.getStringWidth(tongCongValue) / 1000f * 13f;

                float totalTongCongWidth = tongCongLabelWidth + tongCongValueWidth + 5f;
                float startXTongCong = margin + contentWidth - totalTongCongWidth;

                drawText(contentStream, tongCongLabel, localFontBold, 12f, startXTongCong, yPosition);
                drawText(contentStream, tongCongValue, localFontBold, 13f, startXTongCong + tongCongLabelWidth + 5f, yPosition);
                yPosition -= leadingMedium * 1.5f;

                // --- 5. Thời Gian Xuất Hóa Đơn ---
                if (thoiGianXuatFull != null && !thoiGianXuatFull.isEmpty()) {
                     drawText(contentStream, thoiGianXuatFull, localFontRegular, 9.5f, margin, yPosition);
                }
            } // PDPageContentStream tự đóng
            document.save(filePath); // Lưu file
        } // PDDocument chính và InputStreams của font được đóng tự động
    }
    
    // --- Helper methods for drawing PDF content ---
    private void drawText(PDPageContentStream contentStream, String text, PDFont font, float fontSize, float x, float y) throws IOException {
        if (font == null) {
            System.err.println("drawText: Font is null (không thể vẽ). Text: " + text);
            return; 
        }
        contentStream.beginText();
        contentStream.setFont(font, fontSize);
        contentStream.newLineAtOffset(x, y);
        contentStream.showText(text != null ? text : "");
        contentStream.endText();
    }

    private void drawTextCentered(PDPageContentStream contentStream, String text, PDFont font, float fontSize, float y, float pageWidth) throws IOException {
        if (font == null) {
             System.err.println("drawTextCentered: Font is null (không thể vẽ). Text: " + text);
            return;
        }
        text = text != null ? text : "";
        float textWidth = font.getStringWidth(text) / 1000f * fontSize;
        float x = (pageWidth - textWidth) / 2f;
        drawText(contentStream, text, font, fontSize, x, y);
    }

    private float drawKeyValueLine(PDPageContentStream contentStream, String key, String value, PDFont keyFont, PDFont valueFont, float fontSize, float x, float y, float keyWidth, float leading) throws IOException {
         if (keyFont == null || valueFont == null) {
            System.err.println("drawKeyValueLine: KeyFont hoặc ValueFont là null (không thể vẽ).");
            return y - leading; 
        }
        key = key != null ? key : "";
        value = value != null ? value : "";
        drawText(contentStream, key, keyFont, fontSize, x, y);
        drawText(contentStream, value, valueFont, fontSize, x + keyWidth + 5f, y);
        return y - leading;
    }

    private void drawTableCell(PDPageContentStream contentStream, String text, PDFont font, float fontSize, float x, float y, float cellWidth, float cellHeight, String align, boolean isHeader) throws IOException {
        if (font == null) {
            System.err.println("drawTableCell: Font is null (không thể vẽ). Text: " + text);
            return;
        }
        text = text != null ? text : "";
        float textWidth = font.getStringWidth(text) / 1000f * fontSize;
        float actualX = x;
        float padding = 4f;

        if (textWidth > cellWidth - 2 * padding) {
            String originalText = text;
            text = ""; 
            float tempWidth;
            for(int i=0; i < originalText.length(); i++){
                String testText = originalText.substring(0, i+1);
                tempWidth = font.getStringWidth(testText + "...") / 1000f * fontSize;
                if(tempWidth < cellWidth - 2 * padding){
                    text = testText;
                } else {
                    break; 
                }
            }
            if (!text.equals(originalText) && !text.isEmpty()) {
                 text += "...";
            } else if (text.isEmpty() && originalText.length() > 0){ 
                text = originalText.substring(0, Math.min(originalText.length(), (int)((cellWidth-2*padding)/(fontSize/1000f * (font.getStringWidth("W") / 1000f))) -1 )) + ".."; 
            }
             textWidth = font.getStringWidth(text) / 1000f * fontSize;
        }


        if ("CENTER".equalsIgnoreCase(align)) {
            actualX = x + (cellWidth - textWidth) / 2f;
        } else if ("RIGHT".equalsIgnoreCase(align)) {
            actualX = x + cellWidth - textWidth - padding;
        } else { // LEFT
            actualX = x + padding;
        }

        if (actualX < x + padding/2f) actualX = x + padding/2f;
        if (actualX + textWidth > x + cellWidth - padding/2f && !"LEFT".equalsIgnoreCase(align)) {
             actualX = x + cellWidth - textWidth - padding/2f;
        }

        float textY = y + (cellHeight - fontSize) / 2f - (fontSize*0.15f);
        if(isHeader) textY = y + (cellHeight - fontSize) / 2f - (fontSize*0.05f);

        contentStream.beginText();
        contentStream.setFont(font, fontSize);
        contentStream.newLineAtOffset(actualX, textY);
        contentStream.showText(text);
        contentStream.endText();
    }

    private void styleButton(JButton button, Color bgColor, Color fgColor) { /* ... Giữ nguyên ... */ 
        button.setFont(FONT_BUTTON);
        if (bgColor != null) button.setBackground(bgColor);
        if (fgColor != null) button.setForeground(fgColor);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(8, 15, 8, 15));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
    private ImageIcon loadIcon(String iconName, int width, int height) { /* ... Giữ nguyên ... */ 
        String resourcePath = "/icons/" + iconName;
        URL imgUrl = getClass().getResource(resourcePath);
        if (imgUrl != null) {
            try {
                ImageIcon originalIcon = new ImageIcon(imgUrl);
                Image scaledImage = originalIcon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
                return new ImageIcon(scaledImage);
            } catch (Exception e) {
                 System.err.println("Lỗi khi tạo ImageIcon từ URL: " + resourcePath + " - " + e.getMessage());
            }
        } else {
            System.err.println("LỖI: Không tìm thấy resource icon: " + resourcePath);
        }
        BufferedImage placeholder = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = placeholder.createGraphics();
        g2d.setColor(new Color(220,220,220)); g2d.fillRect(0,0,width,height);
        g2d.setColor(new Color(150,150,150)); g2d.drawString("?", width/2-4, height/2+4);
        g2d.dispose();
        return new ImageIcon(placeholder);
    }

    public static void main(String args[]) {
        SwingUtilities.invokeLater(() -> {
            String maHoaDonDeTest = "HD011"; 
            ThanhToanHoaDon frameTest = new ThanhToanHoaDon(maHoaDonDeTest, null);
            frameTest.setVisible(true);
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
