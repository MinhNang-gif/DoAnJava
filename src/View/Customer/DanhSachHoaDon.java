package View.Customer;

import com.formdev.flatlaf.FlatLightLaf;
import ConnectDB.ConnectionOracle;
import View.DangNhap;
import Process.UserToken; // **** THÊM HOẶC ĐẢM BẢO CÓ IMPORT NÀY ****
import Process.RoleGroupConstants; // **** THÊM IMPORT NÀY ****

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Vector;
import java.util.Date;
import javax.swing.table.DefaultTableCellRenderer;


public class DanhSachHoaDon extends javax.swing.JFrame {
    // --- UI Styling Constants ---
    private final Color FRAME_PRIMARY_COLOR = new Color(70, 130, 180);
    private final Color BUTTON_PRIMARY_COLOR = new Color(70, 130, 180);
    private final Color BUTTON_SECONDARY_COLOR = new Color(108, 117, 125);
    
    private final Color TABLE_HEADER_BACKGROUND = new Color(220, 230, 240);
    private final Color TEXT_COLOR_ON_PRIMARY = Color.WHITE;
    private final Color TEXT_COLOR_DARK = new Color(44, 62, 80);    
    private final Color WHITE_COLOR = Color.WHITE;
    private final Color APP_BACKGROUND = new Color(245, 247, 249);
    private final Color BORDER_COLOR = new Color(190, 200, 210);   

    private final Font FONT_TITLE_FRAME = new Font("Segoe UI", Font.BOLD, 20);
    private final Font FONT_TABLE_HEADER = new Font("Segoe UI", Font.BOLD, 13);
    private final Font FONT_TABLE_CELL = new Font("Segoe UI", Font.PLAIN, 12);
    private final Font FONT_BUTTON = new Font("Segoe UI", Font.BOLD, 13);
    private final Font FONT_LABEL = new Font("Segoe UI", Font.PLAIN, 13);

    // --- UI Components ---
    private JTable tblHoaDon;
    private DefaultTableModel hoaDonTableModel;
    private JTextField txtSearch;
    private JButton btnSearch;
    private JButton btnRefresh;
    private JButton btnBack;

    // --- Formatters ---
    private final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    private final SimpleDateFormat dateTimeFormatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

    // --- Owner Frame Reference and User Info ---
    private CustomerHomePage ownerFrame;
    private String currentMaKH;      

    /**
     * Constructor của DanhSachHoaDon.
     * Nhận CustomerHomePage làm frame cha để lấy thông tin UserToken.
     * @param owner Instance của CustomerHomePage.
     */
    public DanhSachHoaDon(CustomerHomePage owner) {
        this.ownerFrame = owner;
        
        // Lấy MaKH từ UserToken của ownerFrame
        if (this.ownerFrame != null && this.ownerFrame.getCurrentUser() != null &&
            RoleGroupConstants.CUSTOMER.equals(this.ownerFrame.getCurrentUser().getRole())) {
            this.currentMaKH = this.ownerFrame.getCurrentUser().getEntityId(); // Lấy MaKH
            setTitle("Danh Sách Hóa Đơn - KH: " + this.ownerFrame.getCurrentUser().getEntityFullName());
        } else {
            // Xử lý trường hợp thông tin người dùng không hợp lệ
            JOptionPane.showMessageDialog(null, "Lỗi: Thông tin người dùng không hợp lệ hoặc không phải khách hàng.", "Lỗi Khởi Tạo", JOptionPane.ERROR_MESSAGE);
            SwingUtilities.invokeLater(() -> {
                 if (this.ownerFrame != null && this.ownerFrame.isDisplayable()) this.ownerFrame.setEnabled(true); // Enable lại owner nếu nó bị disable
                this.dispose(); // Đóng frame này nếu có lỗi
            });
            return; // Không tiếp tục nếu lỗi
        }
        
        try { UIManager.setLookAndFeel(new FlatLightLaf()); } catch (Exception e) { System.err.println("Failed to initialize LaF: " + e.getMessage()); }
        
        setSize(1050, 720); 
        setMinimumSize(new Dimension(900, 600)); 
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); 
        setLocationRelativeTo(ownerFrame); // Hiển thị frame này tương đối với frame cha
        getContentPane().setBackground(APP_BACKGROUND); 
        setLayout(new BorderLayout(10,10));
        
        initComponentsCustom(); // Khởi tạo các thành phần UI tùy chỉnh
        loadDanhSachHoaDonChoKhachHang(""); // Tải danh sách hóa đơn cho khách hàng hiện tại
        
        // Xử lý khi cửa sổ này đóng
        this.addWindowListener(new WindowAdapter() {
            @Override 
            public void windowClosed(WindowEvent e) { 
                if (ownerFrame != null) { 
                    ownerFrame.setEnabled(true); // Enable lại frame cha
                    ownerFrame.requestFocus();   // Focus lại frame cha
                } 
            }
            @Override 
            public void windowClosing(WindowEvent e) { 
                if (ownerFrame != null) { 
                    ownerFrame.setEnabled(true); 
                    ownerFrame.requestFocus(); 
                } 
                DanhSachHoaDon.this.dispose(); // Đảm bảo frame này được giải phóng
            }
        });
    }

    private void initComponentsCustom() {
        // --- Panel Tiêu đề ---
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        titlePanel.setBackground(FRAME_PRIMARY_COLOR);
        titlePanel.setBorder(new EmptyBorder(15,0,15,0));
        JLabel lblTitle = new JLabel("DANH SÁCH HÓA ĐƠN CỦA BẠN");
        lblTitle.setFont(FONT_TITLE_FRAME);
        lblTitle.setForeground(TEXT_COLOR_ON_PRIMARY);
        titlePanel.add(lblTitle);

        // --- Panel chứa các nút actions và tìm kiếm ---
        JPanel topActionsPanel = new JPanel(new BorderLayout(15, 10));
        topActionsPanel.setBackground(APP_BACKGROUND);
        topActionsPanel.setBorder(new EmptyBorder(15, 20, 10, 20));

        JPanel searchFieldsAndButtonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        searchFieldsAndButtonsPanel.setOpaque(false);

        JLabel lblSearch = new JLabel("Tìm kiếm HĐ:");
        lblSearch.setFont(FONT_LABEL.deriveFont(Font.BOLD));
        lblSearch.setForeground(TEXT_COLOR_DARK);
        txtSearch = new JTextField(25);
        txtSearch.setFont(FONT_LABEL);
        txtSearch.putClientProperty("JTextField.placeholderText", "Mã HĐ, Ngày lập...");

        btnSearch = new JButton("Tìm");
        styleButton(btnSearch, BUTTON_PRIMARY_COLOR, WHITE_COLOR);
        btnSearch.setIcon(loadIcon("search.png", 16, 16));
        btnSearch.addActionListener(e -> loadDanhSachHoaDonChoKhachHang(txtSearch.getText().trim()));

        btnRefresh = new JButton("Làm Mới");
        styleButton(btnRefresh, BUTTON_SECONDARY_COLOR, WHITE_COLOR);
        btnRefresh.setIcon(loadIcon("refresh.png", 16, 16));
        btnRefresh.addActionListener(e -> {
            txtSearch.setText("");
            loadDanhSachHoaDonChoKhachHang("");
        });
        
        searchFieldsAndButtonsPanel.add(lblSearch);
        searchFieldsAndButtonsPanel.add(txtSearch);
        searchFieldsAndButtonsPanel.add(btnSearch);
        searchFieldsAndButtonsPanel.add(btnRefresh);

        btnBack = new JButton("Trang Chủ");
        styleButton(btnBack, BUTTON_SECONDARY_COLOR, WHITE_COLOR);
        btnBack.setIcon(loadIcon("back_arrow.png", 16, 16));
        btnBack.addActionListener(e -> {
            if (ownerFrame != null) {
                ownerFrame.showWelcomeScreen(); // Quay lại màn hình welcome của CustomerHomePage
            }
            this.dispose(); // Đóng frame hiện tại
        });
        
        JPanel backButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0,0));
        backButtonPanel.setOpaque(false);
        backButtonPanel.add(btnBack);

        topActionsPanel.add(searchFieldsAndButtonsPanel, BorderLayout.WEST);
        topActionsPanel.add(backButtonPanel, BorderLayout.EAST);

        // --- Bảng hiển thị danh sách hóa đơn ---
        String[] columnNames = {"STT", "Mã Hóa Đơn", "Ngày Lập", "Tổng Tiền", "Trạng Thái"};
        hoaDonTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblHoaDon = new JTable(hoaDonTableModel);
        tblHoaDon.setFont(FONT_TABLE_CELL);
        tblHoaDon.setRowHeight(32);
        tblHoaDon.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblHoaDon.setGridColor(BORDER_COLOR);
        tblHoaDon.setShowGrid(true);
        tblHoaDon.setIntercellSpacing(new Dimension(0,0));

        JTableHeader tableHeader = tblHoaDon.getTableHeader();
        tableHeader.setFont(FONT_TABLE_HEADER);
        tableHeader.setBackground(TABLE_HEADER_BACKGROUND);
        tableHeader.setForeground(TEXT_COLOR_DARK);
        tableHeader.setReorderingAllowed(false);
        tableHeader.setPreferredSize(new Dimension(tableHeader.getWidth(), 40));

        TableColumnModel columnModel = tblHoaDon.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(50);  // STT
        columnModel.getColumn(1).setPreferredWidth(150); // Mã Hóa Đơn
        columnModel.getColumn(2).setPreferredWidth(180); // Ngày Lập
        columnModel.getColumn(3).setPreferredWidth(150); // Tổng Tiền
        columnModel.getColumn(4).setPreferredWidth(180); // Trạng Thái
        
        DefaultTableCellRenderer leftPaddingRenderer = new DefaultTableCellRenderer();
        leftPaddingRenderer.setHorizontalAlignment(SwingConstants.LEFT);
        leftPaddingRenderer.setBorder(new EmptyBorder(0, 10, 0, 10));

        for (int i = 0; i < tblHoaDon.getColumnCount(); i++) {
            tblHoaDon.getColumnModel().getColumn(i).setCellRenderer(leftPaddingRenderer);
        }
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        centerRenderer.setBorder(new EmptyBorder(0, 10, 0, 10));
        tblHoaDon.getColumnModel().getColumn(0).setCellRenderer(centerRenderer); // STT căn giữa
        
        DefaultTableCellRenderer rightPaddingRenderer = new DefaultTableCellRenderer();
        rightPaddingRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        rightPaddingRenderer.setBorder(new EmptyBorder(0, 10, 0, 10));
        tblHoaDon.getColumnModel().getColumn(3).setCellRenderer(rightPaddingRenderer); // Tổng tiền căn phải

        JScrollPane scrollPane = new JScrollPane(tblHoaDon);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            new EmptyBorder(1,1,1,1)
        ));
        scrollPane.getViewport().setBackground(WHITE_COLOR);
        
        JPanel tableContainerPanel = new JPanel(new BorderLayout());
        tableContainerPanel.setBackground(APP_BACKGROUND);
        tableContainerPanel.setBorder(new EmptyBorder(0,20,20,20));
        tableContainerPanel.add(scrollPane, BorderLayout.CENTER);

        tblHoaDon.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    int selectedRow = tblHoaDon.getSelectedRow();
                    if (selectedRow >= 0) {
                        String maHoaDon = (String) hoaDonTableModel.getValueAt(selectedRow, 1);
                        
                        // Tạo và hiển thị frame ThanhToanHoaDon
                        ThanhToanHoaDon chiTietHD = new ThanhToanHoaDon(maHoaDon, DanhSachHoaDon.this.ownerFrame);
                        chiTietHD.setVisible(true);
                        
                        if(ownerFrame != null) ownerFrame.setEnabled(false); // Disable CustomerHomePage
                        DanhSachHoaDon.this.setEnabled(false); // Disable frame hiện tại

                        chiTietHD.addWindowListener(new WindowAdapter() {
                             @Override
                             public void windowClosed(WindowEvent e) {
                                 if(ownerFrame != null) ownerFrame.setEnabled(true);
                                 DanhSachHoaDon.this.setEnabled(true);
                                 loadDanhSachHoaDonChoKhachHang(txtSearch.getText().trim()); // Tải lại danh sách
                                 DanhSachHoaDon.this.requestFocus();
                             }
                        });
                    }
                }
            }
        });

        add(titlePanel, BorderLayout.NORTH);
        
        JPanel mainContentPanel = new JPanel(new BorderLayout(0,15));
        mainContentPanel.setBackground(APP_BACKGROUND);
        mainContentPanel.add(topActionsPanel, BorderLayout.NORTH);
        mainContentPanel.add(tableContainerPanel, BorderLayout.CENTER);

        add(mainContentPanel, BorderLayout.CENTER);
    }
    
    private void styleButton(JButton button, Color bgColor, Color fgColor) {
        button.setFont(FONT_BUTTON);
        if (bgColor != null) button.setBackground(bgColor);
        if (fgColor != null) button.setForeground(fgColor);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(10, 18, 10, 18));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        Color hoverBgColor = (bgColor != null) ? bgColor.brighter() : FRAME_PRIMARY_COLOR.brighter();
        Color pressedBgColor = (bgColor != null) ? bgColor.darker() : FRAME_PRIMARY_COLOR.darker();

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) { button.setBackground(hoverBgColor); }
            @Override
            public void mouseExited(MouseEvent e) { button.setBackground(bgColor); }
            @Override
            public void mousePressed(MouseEvent e) { button.setBackground(pressedBgColor); }
            @Override
            public void mouseReleased(MouseEvent e) {
                if (button.getBounds().contains(e.getPoint())) { button.setBackground(hoverBgColor); }
                else { button.setBackground(bgColor); }
            }
        });
    }

    private ImageIcon loadIcon(String iconName, int width, int height) {
        String resourcePath = "/icons/" + iconName;
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
            System.err.println("LỖI: Không thể tìm thấy resource icon: " + resourcePath);
        }
        // Tạo placeholder icon nếu không tìm thấy hoặc lỗi
        BufferedImage placeholder = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = placeholder.createGraphics();
        g2d.setColor(new Color(220,220,220)); g2d.fillRect(0,0,width,height);
        g2d.setColor(new Color(150,150,150)); g2d.drawString("?", width/2-4, height/2+4);
        g2d.dispose();
        return new ImageIcon(placeholder);
    }

    /**
     * Tải danh sách hóa đơn cho khách hàng hiện tại (dựa trên currentMaKH).
     * @param searchTerm Từ khóa tìm kiếm. Nếu rỗng, tải tất cả hóa đơn của khách hàng.
     */
    private void loadDanhSachHoaDonChoKhachHang(String searchTerm) {
        System.out.println("Đang tải hóa đơn cho MAKH: " + this.currentMaKH + " với từ khóa: '" + searchTerm + "'");
        hoaDonTableModel.setRowCount(0); // Xóa dữ liệu cũ trên bảng
        
        // Kiểm tra MaKH trước khi truy vấn
        if (this.currentMaKH == null || this.currentMaKH.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Không thể tải hóa đơn: Mã khách hàng không hợp lệ.", "Lỗi Xác Thực Người Dùng", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        String sql = "SELECT hd.MAHOADON, hd.NGAYLAP, hd.TONGTIEN, hd.TRANGTHAI " +
                     "FROM HOADON hd " +
                     "WHERE hd.MAKH = ? "; // Lọc theo MAKH của người dùng hiện tại

        if (searchTerm != null && !searchTerm.isEmpty()) {
            // Thêm điều kiện tìm kiếm nếu searchTerm không rỗng
            sql += "AND (UPPER(hd.MAHOADON) LIKE UPPER(?) OR TO_CHAR(hd.NGAYLAP, 'DD/MM/YYYY') LIKE ? OR UPPER(hd.TRANGTHAI) LIKE UPPER(?)) ";
        }
        sql += "ORDER BY hd.NGAYLAP DESC"; // Sắp xếp theo ngày lập giảm dần

        try {
            conn = ConnectionOracle.getOracleConnection();
            pstmt = conn.prepareStatement(sql);
            int paramIndex = 1;
            pstmt.setString(paramIndex++, this.currentMaKH); // Gán giá trị cho MAKH

            if (searchTerm != null && !searchTerm.isEmpty()) {
                String searchPattern = "%" + searchTerm + "%";
                pstmt.setString(paramIndex++, searchPattern); // Cho MAHOADON
                pstmt.setString(paramIndex++, searchPattern); // Cho NGAYLAP (đã format thành chuỗi)
                pstmt.setString(paramIndex++, "%" + searchTerm.toUpperCase() + "%"); // Cho TRANGTHAI
            }

            rs = pstmt.executeQuery();
            int stt = 1; // Số thứ tự
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(String.valueOf(stt++));
                row.add(rs.getString("MAHOADON"));
                Date ngayLap = rs.getTimestamp("NGAYLAP"); // Lấy NGAYLAP kiểu Timestamp
                row.add(ngayLap != null ? dateTimeFormatter.format(ngayLap) : "N/A"); // Format ngày giờ
                row.add(currencyFormatter.format(rs.getDouble("TONGTIEN"))); // Format tiền tệ
                String trangThai = rs.getString("TRANGTHAI");
                row.add(trangThai != null ? trangThai.toUpperCase() : "N/A"); // Chuyển trạng thái sang chữ hoa
                hoaDonTableModel.addRow(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi tải danh sách hóa đơn: " + e.getMessage(), "Lỗi SQL", JOptionPane.ERROR_MESSAGE);
        } catch (ClassNotFoundException e) { // Bắt lỗi nếu ConnectionOracle.getOracleConnection() ném ClassNotFoundException
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi driver Oracle: " + e.getMessage(), "Lỗi Driver", JOptionPane.ERROR_MESSAGE);
        } finally {
            // Đóng các resource
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // Main method chỉ để test độc lập (nếu cần) - Cần CustomerHomePage giả lập
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            // Để chạy test độc lập, bạn cần một CustomerHomePage giả hoặc một UserToken giả.
            // Ví dụ:
            // UserToken testToken = new UserToken("KH001", "Tên KH Test", 1, RoleGroupConstants.CUSTOMER);
            // CustomerHomePage fakeOwner = new CustomerHomePage(testToken); // Giả sử constructor này tồn tại
            // new DanhSachHoaDon(fakeOwner).setVisible(true);
            
            // Nếu không có CustomerHomePage thực sự, việc khởi tạo có thể không đúng
            // và currentMaKH sẽ null, dẫn đến lỗi khi tải dữ liệu.
            System.out.println("Chạy DanhSachHoaDon.main() - Cần CustomerHomePage hợp lệ để hoạt động đúng.");
             JFrame errorFrame = new JFrame("Thông báo");
             JOptionPane.showMessageDialog(errorFrame, 
                "Phương thức main() trong DanhSachHoaDon chỉ dành cho mục đích thử nghiệm cơ bản.\n" +
                "Để hoạt động đầy đủ, cần được gọi từ CustomerHomePage với UserToken hợp lệ.", 
                "Thông báo chạy thử", JOptionPane.INFORMATION_MESSAGE);
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
