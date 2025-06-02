package View.Employee;

import ConnectDB.ConnectionOracle;
import Process.RoleGroupConstants;
import Process.UserToken;
import View.DangNhap;
import com.formdev.flatlaf.FlatLightLaf;
import com.toedter.calendar.JDateChooser;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;


public class ThucHienBaoDuongXe extends javax.swing.JFrame {
    private String currentEmployeeId;
    private String currentEmployeeFullName;
    private boolean initSuccess = false;

    private JTable maintenanceTable;
    private DefaultTableModel tableModel;
    private JTextField searchBienSoField;
    private JComboBox<String> searchTrangThaiComboBox;
    private JDateChooser searchStartDateChooser, searchEndDateChooser;

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private static final Color BACKGROUND_COLOR = new Color(240, 245, 250);
    private static final Color PANEL_BACKGROUND_COLOR = Color.WHITE;
    private static final Color HEADER_COLOR = new Color(0, 123, 255);
    private static final Font BOLD_FONT = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font PLAIN_FONT = new Font("Segoe UI", Font.PLAIN, 14);

    private static final String TT_CHO_THUC_HIEN = "CHO THUC HIEN";
    private static final String TT_DANG_THUC_HIEN = "DANG THUC HIEN";
    private static final String TT_HOAN_THANH = "HOAN THANH";

    private static class EmployeeItem {
        String maNV;
        String tenNV;

        public EmployeeItem(String maNV, String tenNV) {
            this.maNV = maNV;
            this.tenNV = tenNV;
        }

        public String getMaNV() {
            return maNV;
        }

        @Override
        public String toString() {
            return maNV + " - " + tenNV;
        }
    }

    // Constructor cập nhật để nhận employeeId và employeeFullName
    public ThucHienBaoDuongXe(String employeeId, String employeeFullName) {
        this.currentEmployeeId = employeeId;
        this.currentEmployeeFullName = employeeFullName;

        if (!isAuthenticatedAndAuthorized()) { // Kiểm tra xác thực và ủy quyền
            SwingUtilities.invokeLater(this::dispose);
            return;
        }
        // Thông tin nhân viên đã được truyền vào, không cần lấy từ DangNhap.currentUserToken nữa ở đây.

        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (UnsupportedLookAndFeelException ex) {
            System.err.println("Lỗi khi khởi tạo LaF: " + ex.getMessage());
        }

        setTitle("Thực Hiện Bảo Dưỡng Xe - Nhân viên: " + (this.currentEmployeeFullName != null && !this.currentEmployeeFullName.isEmpty() ? this.currentEmployeeFullName : this.currentEmployeeId));
        setSize(1250, 780);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BACKGROUND_COLOR);
        setLayout(new BorderLayout(10, 10));

        initComponentsUI();

        if (initSuccess) { // initSuccess được đặt trong isAuthenticatedAndAuthorized
            loadMaintenanceData();
        }
    }
    
    // Constructor mặc định có thể dùng để cảnh báo nếu không có thông tin nhân viên
    public ThucHienBaoDuongXe() {
        this(null, null); // Gọi constructor chính với giá trị null
    }


    private boolean isAuthenticatedAndAuthorized() {
        // 1. Kiểm tra token chung
        if (DangNhap.currentUserToken == null || !DangNhap.currentUserToken.isStatus()) {
            handleAuthFailure("Bạn cần đăng nhập để truy cập chức năng này.", "Yêu Cầu Đăng Nhập");
            return false;
        }
        String role = DangNhap.currentUserToken.getRole(); // Lấy role từ token để kiểm tra quyền

        // 2. Kiểm tra vai trò
        if (!RoleGroupConstants.EMPLOYEE.equals(role)) {
            handleAuthFailure("Chỉ nhân viên mới có quyền truy cập chức năng này.\nVai trò của bạn: " +
                              (role == null || role.isEmpty() ? "Không xác định" : role), "Không Được Phép");
            return false;
        }

        // 3. Kiểm tra thông tin nhân viên đã được truyền vào (this.currentEmployeeId và this.currentEmployeeFullName)
        if (this.currentEmployeeId == null || this.currentEmployeeId.trim().isEmpty()) {
            handleAuthFailure("Thông tin Mã Nhân Viên không được cung cấp hoặc không hợp lệ.", "Lỗi Dữ Liệu Nhân Viên");
            return false;
        }
        if (this.currentEmployeeFullName == null || this.currentEmployeeFullName.trim().isEmpty()){
             handleAuthFailure("Thông tin Tên Nhân Viên không được cung cấp hoặc không hợp lệ.", "Lỗi Dữ Liệu Nhân Viên");
            return false;
        }
        
        initSuccess = true; // Tất cả kiểm tra thành công
        return true;
    }

    private void handleAuthFailure(String message, String title) {
        EventQueue.invokeLater(() -> {
            JOptionPane.showMessageDialog(null, message, title, JOptionPane.ERROR_MESSAGE);
            // Đảm bảo không cố gắng dispose nếu frame chưa được hiển thị hoặc đã bị dispose
            Window window = SwingUtilities.getWindowAncestor(this);
            if (window != null && window.isDisplayable()) {
                 new DangNhap().setVisible(true);
                 window.dispose(); // Sử dụng window.dispose() thay vì this.dispose() trực tiếp trong lambda
            } else if (this.isDisplayable()){
                 new DangNhap().setVisible(true);
                 this.dispose();
            }
        });
    }

    private void initComponentsUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(BACKGROUND_COLOR);

        // ======== TOP: Search Panel ========
        JPanel searchPanel = new JPanel(new GridBagLayout());
        searchPanel.setBackground(PANEL_BACKGROUND_COLOR);
        searchPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Tìm Kiếm Lịch Bảo Dưỡng",
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                javax.swing.border.TitledBorder.DEFAULT_POSITION,
                BOLD_FONT, HEADER_COLOR));
        GridBagConstraints gbcSearch = new GridBagConstraints();
        gbcSearch.insets = new Insets(5, 5, 5, 5); 
        gbcSearch.anchor = GridBagConstraints.WEST;

        // Row 0
        gbcSearch.gridy = 0;
        gbcSearch.gridx = 0; gbcSearch.weightx = 0.0; gbcSearch.fill = GridBagConstraints.NONE; searchPanel.add(new JLabel("Biển số xe:"), gbcSearch);
        gbcSearch.gridx = 1; gbcSearch.weightx = 0.4; gbcSearch.fill = GridBagConstraints.HORIZONTAL; searchBienSoField = new JTextField(); searchBienSoField.setFont(PLAIN_FONT); searchPanel.add(searchBienSoField, gbcSearch);

        gbcSearch.gridx = 2; gbcSearch.weightx = 0.0; gbcSearch.fill = GridBagConstraints.NONE; gbcSearch.insets = new Insets(5, 15, 5, 5); searchPanel.add(new JLabel("Trạng thái:"), gbcSearch); gbcSearch.insets = new Insets(5, 5, 5, 5); 
        gbcSearch.gridx = 3; gbcSearch.weightx = 0.6; gbcSearch.fill = GridBagConstraints.HORIZONTAL; searchTrangThaiComboBox = new JComboBox<>(new String[]{"Tất cả", TT_CHO_THUC_HIEN, TT_DANG_THUC_HIEN, TT_HOAN_THANH}); searchTrangThaiComboBox.setFont(PLAIN_FONT); searchPanel.add(searchTrangThaiComboBox, gbcSearch);

        // Row 1
        gbcSearch.gridy = 1;
        gbcSearch.gridx = 0; gbcSearch.weightx = 0.0; gbcSearch.fill = GridBagConstraints.NONE; searchPanel.add(new JLabel("Từ ngày:"), gbcSearch);
        gbcSearch.gridx = 1; gbcSearch.weightx = 0.4; gbcSearch.fill = GridBagConstraints.HORIZONTAL; searchStartDateChooser = new JDateChooser(); searchStartDateChooser.setDateFormatString("yyyy-MM-dd"); searchStartDateChooser.setFont(PLAIN_FONT); searchPanel.add(searchStartDateChooser, gbcSearch);

        gbcSearch.gridx = 2; gbcSearch.weightx = 0.0; gbcSearch.fill = GridBagConstraints.NONE; gbcSearch.insets = new Insets(5, 15, 5, 5); searchPanel.add(new JLabel("Đến ngày:"), gbcSearch); gbcSearch.insets = new Insets(5, 5, 5, 5); 
        gbcSearch.gridx = 3; gbcSearch.weightx = 0.6; gbcSearch.fill = GridBagConstraints.HORIZONTAL; searchEndDateChooser = new JDateChooser(); searchEndDateChooser.setDateFormatString("yyyy-MM-dd"); searchEndDateChooser.setFont(PLAIN_FONT); searchPanel.add(searchEndDateChooser, gbcSearch);

        JPanel searchButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        searchButtonPanel.setBackground(PANEL_BACKGROUND_COLOR);
        JButton btnSearch = new JButton("Tìm Kiếm"); styleButton(btnSearch, HEADER_COLOR, Color.WHITE);
        btnSearch.addActionListener(e -> loadMaintenanceData());
        JButton btnClearSearch = new JButton("Xóa Filter"); btnClearSearch.setFont(BOLD_FONT); 
        btnClearSearch.addActionListener(e -> clearSearchFilters());
        searchButtonPanel.add(btnSearch);
        searchButtonPanel.add(btnClearSearch);

        gbcSearch.gridy = 2;
        gbcSearch.gridx = 0;
        gbcSearch.gridwidth = 4; 
        gbcSearch.anchor = GridBagConstraints.EAST; 
        gbcSearch.fill = GridBagConstraints.NONE; 
        gbcSearch.weightx = 1.0;
        searchPanel.add(searchButtonPanel, gbcSearch);
        mainPanel.add(searchPanel, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(
                new Object[]{"Mã BD", "Biển Số", "Loại Xe", "Tên Dịch Vụ", "Mã NV Tạo", "Tên NV Tạo", "Trạng Thái", "Ngày BD"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        maintenanceTable = new JTable(tableModel);
        maintenanceTable.setFont(PLAIN_FONT);
        maintenanceTable.setRowHeight(28);
        maintenanceTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        maintenanceTable.getTableHeader().setFont(BOLD_FONT);
        maintenanceTable.getTableHeader().setBackground(HEADER_COLOR);
        maintenanceTable.getTableHeader().setForeground(Color.WHITE);
        maintenanceTable.getTableHeader().setReorderingAllowed(false);

        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        maintenanceTable.setRowSorter(sorter);

        JScrollPane scrollPane = new JScrollPane(maintenanceTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(200,200,200)));
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel bottomOuterPanel = new JPanel(new BorderLayout(0,0));
        bottomOuterPanel.setBackground(PANEL_BACKGROUND_COLOR);
        bottomOuterPanel.setBorder(new EmptyBorder(8,10,8,10)); 

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 5)); 
        actionPanel.setBackground(PANEL_BACKGROUND_COLOR);

        JButton btnAddNew = new JButton("Thêm Mới"); styleButton(btnAddNew, HEADER_COLOR, Color.WHITE); btnAddNew.addActionListener(e -> openAddMaintenanceDialog()); actionPanel.add(btnAddNew);
        JButton btnEdit = new JButton("Sửa/Cập Nhật"); styleButton(btnEdit, new Color(23, 162, 184), Color.WHITE); btnEdit.addActionListener(e -> openEditMaintenanceDialog()); actionPanel.add(btnEdit);
        JButton btnSearchEquipment = new JButton("Tra Cứu Thiết Bị"); styleButton(btnSearchEquipment, new Color(108, 117, 125), Color.WHITE); btnSearchEquipment.addActionListener(e -> openSearchEquipmentDialog()); actionPanel.add(btnSearchEquipment);
        JButton btnRefresh = new JButton("Làm Mới DS"); styleButton(btnRefresh, new Color(255, 193, 7), Color.BLACK); btnRefresh.addActionListener(e -> loadMaintenanceData()); actionPanel.add(btnRefresh);

        bottomOuterPanel.add(actionPanel, BorderLayout.CENTER);

        JButton btnBack = new JButton("Quay Lại"); styleButton(btnBack, new Color(80,80,80), Color.WHITE);
        btnBack.addActionListener(e -> {
            new EmployeeHomePage().setVisible(true);
            ThucHienBaoDuongXe.this.dispose();
        });
        JPanel backButtonWrapperPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 5)); 
        backButtonWrapperPanel.setBackground(PANEL_BACKGROUND_COLOR);
        backButtonWrapperPanel.add(btnBack);
        bottomOuterPanel.add(backButtonWrapperPanel, BorderLayout.EAST);

        mainPanel.add(bottomOuterPanel, BorderLayout.SOUTH);
        add(mainPanel);
    }

    private void styleButton(JButton button, Color backgroundColor, Color foregroundColor) {
        button.setFont(BOLD_FONT);
        button.setBackground(backgroundColor);
        button.setForeground(foregroundColor);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(10, 18, 10, 18)); 
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void clearSearchFilters() {
        searchBienSoField.setText("");
        searchTrangThaiComboBox.setSelectedIndex(0);
        searchStartDateChooser.setDate(null);
        searchEndDateChooser.setDate(null);
        loadMaintenanceData();
    }

    private void loadMaintenanceData() {
        if (!initSuccess) return;

        tableModel.setRowCount(0);
        String sql = "SELECT b.MABAODUONG, b.BIENSO, x.TENLOAIXE, dv.TENDV, b.MANHANVIEN, nv.TENNHANVIEN, b.TRANGTHAI, b.NGAYBAODUONG " +
                     "FROM BAODUONGXE b " +
                     "JOIN XE x ON b.BIENSO = x.BIENSO " +
                     "JOIN DICHVUBAODUONG dv ON b.MADVBAODUONG = dv.MADVBAODUONG " +
                     "LEFT JOIN NHANVIEN nv ON b.MANHANVIEN = nv.MANHANVIEN";

        StringBuilder conditions = new StringBuilder();
        String bienSo = searchBienSoField.getText().trim();
        String trangThai = (String) searchTrangThaiComboBox.getSelectedItem();
        Date startDate = searchStartDateChooser.getDate();
        Date endDate = searchEndDateChooser.getDate();

        if (!bienSo.isEmpty()) {
            conditions.append("UPPER(b.BIENSO) LIKE UPPER('%").append(bienSo.replace("'", "''")).append("%')");
        }
        if (trangThai != null && !"Tất cả".equals(trangThai)) {
            if (conditions.length() > 0) conditions.append(" AND ");
            conditions.append("b.TRANGTHAI = '").append(trangThai).append("'");
        }
        if (startDate != null) {
            if (conditions.length() > 0) conditions.append(" AND ");
            conditions.append("b.NGAYBAODUONG >= TO_DATE('").append(dateFormat.format(startDate)).append("', 'YYYY-MM-DD')");
        }
        if (endDate != null) {
            if (conditions.length() > 0) conditions.append(" AND ");
            Calendar c = Calendar.getInstance(); c.setTime(endDate); c.add(Calendar.DAY_OF_MONTH, 1);
            conditions.append("b.NGAYBAODUONG < TO_DATE('").append(dateFormat.format(c.getTime())).append("', 'YYYY-MM-DD')");
        }

        if (conditions.length() > 0) {
            sql += " WHERE " + conditions.toString();
        }
        sql += " ORDER BY b.NGAYBAODUONG DESC, b.MABAODUONG DESC";
        System.out.println("Executing SQL (loadMaintenanceData): " + sql);

        try (Connection conn = ConnectionOracle.getOracleConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getString("MABAODUONG"));
                row.add(rs.getString("BIENSO"));
                row.add(rs.getString("TENLOAIXE"));
                row.add(rs.getString("TENDV"));
                row.add(rs.getString("MANHANVIEN"));
                row.add(rs.getString("TENNHANVIEN") != null ? rs.getString("TENNHANVIEN") : "N/A");
                row.add(rs.getString("TRANGTHAI"));
                Date ngayBD = rs.getDate("NGAYBAODUONG");
                row.add(ngayBD != null ? dateFormat.format(ngayBD) : "N/A");
                tableModel.addRow(row);
            }
        } catch (SQLException | ClassNotFoundException e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi tải dữ liệu bảo dưỡng: " + e.getMessage(), "Lỗi Database", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private String generateMaintenanceId() {
        String newIdPrefix = "BD";
        int nextNumericValue = 1;

        String query = "SELECT MABAODUONG FROM BAODUONGXE " +
                       "WHERE REGEXP_LIKE(MABAODUONG, '^" + newIdPrefix + "[0-9]{3}$') " + 
                       "ORDER BY MABAODUONG DESC FETCH FIRST 1 ROW ONLY";
        System.out.println("Executing SQL (generateMaintenanceId - get last ID): " + query);

        try (Connection conn = ConnectionOracle.getOracleConnection();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) {
                String lastId = rs.getString("MABAODUONG");
                System.out.println("Last ID found: " + lastId);
                String numericPartStr = lastId.substring(newIdPrefix.length());
                try {
                    nextNumericValue = Integer.parseInt(numericPartStr) + 1;
                } catch (NumberFormatException e) {
                    System.err.println("Lỗi parse số từ MABAODUONG: " + numericPartStr + ". Sẽ thử đếm số lượng.");
                    try (Statement stmtCount = conn.createStatement();
                         ResultSet rsCount = stmtCount.executeQuery("SELECT COUNT(*) FROM BAODUONGXE WHERE REGEXP_LIKE(MABAODUONG, '^" + newIdPrefix + "[0-9]{3}$')")) {
                        if (rsCount.next()) {
                            nextNumericValue = rsCount.getInt(1) + 1;
                            System.out.println("Count of BDXXX IDs + 1: " + nextNumericValue);
                        }
                    } catch (SQLException countEx) {
                         System.err.println("Lỗi đếm số lượng BDXXX IDs: " + countEx.getMessage());
                         return newIdPrefix + String.format("%03d", (int)(System.currentTimeMillis() % 1000));
                    }
                }
            } else {
                 System.out.println("No BDXXX IDs found, starting with 1.");
            }
        } catch (SQLException | ClassNotFoundException e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi tạo Mã Bảo Dưỡng: " + e.getMessage(), "Lỗi Database", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            return newIdPrefix + String.format("%03d", (int)(System.currentTimeMillis() % 1000));
        }
        String generatedId = newIdPrefix + String.format("%03d", nextNumericValue);
        System.out.println("Generated Maintenance ID: " + generatedId);
        return generatedId;
    }


    private void openAddMaintenanceDialog() {
        if (!initSuccess) return;

        JDialog addDialog = new JDialog(this, "Thêm Lịch Bảo Dưỡng Mới", true);
        addDialog.setLayout(new BorderLayout(10,10));

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(new EmptyBorder(20,20,20,20));
        formPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,5,5,5); 
        gbc.anchor = GridBagConstraints.WEST;

        int y = 0; 

        gbc.gridy = y; gbc.gridx = 0; gbc.weightx = 0.2; gbc.fill = GridBagConstraints.NONE; formPanel.add(new JLabel("Mã Bảo Dưỡng:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.8; gbc.fill = GridBagConstraints.HORIZONTAL; JTextField maBdField = new JTextField(generateMaintenanceId()); maBdField.setFont(PLAIN_FONT); maBdField.setEditable(false); formPanel.add(maBdField, gbc);

        y++;
        gbc.gridy = y; gbc.gridx = 0; formPanel.add(new JLabel("Biển Số Xe (*):"), gbc);
        gbc.gridx = 1; JComboBox<String> localVehicleComboBox = new JComboBox<>(); localVehicleComboBox.setFont(PLAIN_FONT);
        try (Connection conn = ConnectionOracle.getOracleConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT BIENSO FROM XE ORDER BY BIENSO")) {
            while (rs.next()) { localVehicleComboBox.addItem(rs.getString("BIENSO")); }
        } catch (Exception ex) { JOptionPane.showMessageDialog(addDialog, "Lỗi tải DS xe: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE); }
        formPanel.add(localVehicleComboBox, gbc);

        y++;
        gbc.gridy = y; gbc.gridx = 0; formPanel.add(new JLabel("Dịch Vụ (*):"), gbc);
        gbc.gridx = 1; JComboBox<String> localServiceComboBox = new JComboBox<>(); localServiceComboBox.setFont(PLAIN_FONT);
        try (Connection conn = ConnectionOracle.getOracleConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT MADVBAODUONG, TENDV FROM DICHVUBAODUONG ORDER BY TENDV")) {
            while (rs.next()) { localServiceComboBox.addItem(rs.getString("MADVBAODUONG") + " - " + rs.getString("TENDV")); }
        } catch (Exception ex) { JOptionPane.showMessageDialog(addDialog, "Lỗi tải DS dịch vụ: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE); }
        formPanel.add(localServiceComboBox, gbc);

        y++;
        gbc.gridy = y; gbc.gridx = 0; formPanel.add(new JLabel("Mã Nhân Viên Tạo (*):"), gbc);
        gbc.gridx = 1; JComboBox<EmployeeItem> maNvComboBox = new JComboBox<>(); maNvComboBox.setFont(PLAIN_FONT);
        EmployeeItem currentLoginEmployeeItem = null; // Mục để chọn nhân viên đang đăng nhập
        try (Connection conn = ConnectionOracle.getOracleConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT MANHANVIEN, TENNHANVIEN FROM NHANVIEN ORDER BY MANHANVIEN")) { 
            while (rs.next()) { 
                EmployeeItem item = new EmployeeItem(rs.getString("MANHANVIEN"), rs.getString("TENNHANVIEN"));
                maNvComboBox.addItem(item);
                // Tự động chọn nhân viên đang đăng nhập (this.currentEmployeeId)
                if (this.currentEmployeeId != null && this.currentEmployeeId.equals(item.getMaNV())) {
                    currentLoginEmployeeItem = item;
                }
            }
            if (currentLoginEmployeeItem != null) {
                maNvComboBox.setSelectedItem(currentLoginEmployeeItem);
            } else if (maNvComboBox.getItemCount() > 0) {
                // Nếu không tìm thấy nhân viên đang đăng nhập trong danh sách (ví dụ, ID không khớp)
                // thì chọn mục đầu tiên để tránh null, hoặc bạn có thể xử lý khác.
                // maNvComboBox.setSelectedIndex(0); 
                // Hoặc tốt hơn là không cho phép lưu nếu không có NV hợp lệ.
                // Tạm thời cứ để ComboBox tự quản lý, nếu không có currentLoginEmployeeId thì người dùng phải chọn.
            }

        } catch (Exception ex) { JOptionPane.showMessageDialog(addDialog, "Lỗi tải DS Nhân viên: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE); }
        formPanel.add(maNvComboBox, gbc);

        y++;
        gbc.gridy = y; gbc.gridx = 0; formPanel.add(new JLabel("Trạng Thái Ban Đầu (*):"), gbc);
        gbc.gridx = 1; JComboBox<String> trangThaiComboBox = new JComboBox<>(new String[]{TT_CHO_THUC_HIEN, TT_DANG_THUC_HIEN}); trangThaiComboBox.setFont(PLAIN_FONT);
        formPanel.add(trangThaiComboBox, gbc);

        y++;
        gbc.gridy = y; gbc.gridx = 0; formPanel.add(new JLabel("Ngày Bảo Dưỡng (*):"), gbc);
        gbc.gridx = 1; JDateChooser ngayBdChooser = new JDateChooser(); ngayBdChooser.setDateFormatString("yyyy-MM-dd"); ngayBdChooser.setDate(new Date()); ngayBdChooser.setFont(PLAIN_FONT);
        formPanel.add(ngayBdChooser, gbc);

        addDialog.add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanelDialog = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanelDialog.setBorder(new EmptyBorder(0,0,10,10));
        buttonPanelDialog.setBackground(BACKGROUND_COLOR);
        JButton btnSave = new JButton("Lưu"); styleButton(btnSave, HEADER_COLOR, Color.WHITE);
        btnSave.addActionListener(saveEvent -> {
            String maBd = maBdField.getText();
            String bienSo = (String) localVehicleComboBox.getSelectedItem();
            String serviceItem = (String) localServiceComboBox.getSelectedItem();
            String maDv = serviceItem != null ? serviceItem.split(" - ")[0] : null;

            EmployeeItem selectedEmployee = (EmployeeItem) maNvComboBox.getSelectedItem();
            String maNvTao = null;
            if (selectedEmployee != null) {
                maNvTao = selectedEmployee.getMaNV();
            } else { // Kiểm tra thêm nếu không có nhân viên nào được chọn (dù đã cố gắng auto-select)
                JOptionPane.showMessageDialog(addDialog, "Vui lòng chọn một Nhân Viên Tạo.", "Thiếu Thông Tin", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String trangThai = (String) trangThaiComboBox.getSelectedItem();
            Date ngayBd = ngayBdChooser.getDate();

            if (bienSo == null || maDv == null || trangThai == null || ngayBd == null) {
                JOptionPane.showMessageDialog(addDialog, "Vui lòng điền đủ thông tin (*).", "Thiếu Thông Tin", JOptionPane.WARNING_MESSAGE);
                return;
            }
             // Mã NV Tạo đã được kiểm tra ở trên
            if (maBd == null || maBd.trim().isEmpty() || !maBd.startsWith("BD")){ 
                JOptionPane.showMessageDialog(addDialog, "Mã bảo dưỡng không hợp lệ. Vui lòng thử lại.", "Lỗi Mã Bảo Dưỡng", JOptionPane.ERROR_MESSAGE);
                maBdField.setText(generateMaintenanceId()); 
                return;
            }

            String sql = "INSERT INTO BAODUONGXE (MABAODUONG, BIENSO, MADVBAODUONG, MANHANVIEN, TRANGTHAI, NGAYBAODUONG) VALUES (?, ?, ?, ?, ?, ?)";
            System.out.println("Executing SQL (openAddMaintenanceDialog - Save): " + sql);
            System.out.println("Values: " + maBd + ", " + bienSo + ", " + maDv + ", " + maNvTao + ", " + trangThai + ", " + (ngayBd != null ? new java.sql.Date(ngayBd.getTime()) : null ));

            try (Connection conn = ConnectionOracle.getOracleConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, maBd);
                pstmt.setString(2, bienSo);
                pstmt.setString(3, maDv);
                pstmt.setString(4, maNvTao);
                pstmt.setString(5, trangThai);
                pstmt.setDate(6, new java.sql.Date(ngayBd.getTime()));

                pstmt.executeUpdate();
                JOptionPane.showMessageDialog(addDialog, "Thêm lịch bảo dưỡng thành công!");
                loadMaintenanceData();
                addDialog.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(addDialog, "Lỗi khi thêm: " + ex.getMessage(), "Lỗi Database", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });
        buttonPanelDialog.add(btnSave);
        JButton btnCancel = new JButton("Hủy"); styleButton(btnCancel, new Color(108,117,125), Color.WHITE);
        btnCancel.addActionListener(e -> addDialog.dispose());
        buttonPanelDialog.add(btnCancel);
        addDialog.add(buttonPanelDialog, BorderLayout.SOUTH);

        addDialog.pack(); 
        addDialog.setLocationRelativeTo(this); 
        addDialog.setVisible(true);
    }

    private void openEditMaintenanceDialog() {
        if (!initSuccess) return;
        int selectedRow = maintenanceTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một mục bảo dưỡng để sửa.", "Chưa Chọn Mục", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = maintenanceTable.convertRowIndexToModel(selectedRow);

        String maBd = (String) tableModel.getValueAt(modelRow, 0);
        String currentBienSo = (String) tableModel.getValueAt(modelRow, 1);
        String currentTenDv = (String) tableModel.getValueAt(modelRow, 3);
        String currentMaNvTaoGoc = (String) tableModel.getValueAt(modelRow, 4);
        String currentTrangThai = (String) tableModel.getValueAt(modelRow, 6);
        String currentNgayBdStr = (String) tableModel.getValueAt(modelRow, 7);

        JDialog editDialog = new JDialog(this, "Sửa Thông Tin Bảo Dưỡng [Mã BD: " + maBd +"]", true);
        editDialog.setLayout(new BorderLayout(10,10));

        JPanel formPanel = new JPanel(new GridBagLayout()); formPanel.setBorder(new EmptyBorder(20,20,20,20)); formPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints(); gbc.insets = new Insets(5,5,5,5); gbc.anchor = GridBagConstraints.WEST;
        int y = 0;

        gbc.gridy = y; gbc.gridx = 0; gbc.weightx = 0.2; gbc.fill = GridBagConstraints.NONE; formPanel.add(new JLabel("Biển Số Xe:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.8; gbc.fill = GridBagConstraints.HORIZONTAL; JTextField bienSoField = new JTextField(currentBienSo); bienSoField.setEditable(false); bienSoField.setFont(PLAIN_FONT); formPanel.add(bienSoField, gbc);

        y++;
        gbc.gridy = y; gbc.gridx = 0; formPanel.add(new JLabel("Dịch Vụ (*):"), gbc);
        gbc.gridx = 1; JComboBox<String> serviceComboBoxEdit = new JComboBox<>(); serviceComboBoxEdit.setFont(PLAIN_FONT);
        String selectedServiceItem = null;
        try (Connection conn = ConnectionOracle.getOracleConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT MADVBAODUONG, TENDV FROM DICHVUBAODUONG ORDER BY TENDV")) {
            while (rs.next()) { String item = rs.getString("MADVBAODUONG") + " - " + rs.getString("TENDV"); serviceComboBoxEdit.addItem(item); if (rs.getString("TENDV").equals(currentTenDv)) selectedServiceItem = item; }
            if (selectedServiceItem != null) serviceComboBoxEdit.setSelectedItem(selectedServiceItem);
        } catch (Exception ex) { JOptionPane.showMessageDialog(editDialog, "Lỗi tải DS dịch vụ.", "Lỗi", JOptionPane.ERROR_MESSAGE); }
        formPanel.add(serviceComboBoxEdit, gbc);

        y++;
        gbc.gridy = y; gbc.gridx = 0; formPanel.add(new JLabel("Mã Nhân Viên Tạo (*):"), gbc);
        gbc.gridx = 1; JComboBox<EmployeeItem> maNvComboBoxEdit = new JComboBox<>(); maNvComboBoxEdit.setFont(PLAIN_FONT);
        EmployeeItem selectedEmployeeToEdit = null;
        try (Connection conn = ConnectionOracle.getOracleConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT MANHANVIEN, TENNHANVIEN FROM NHANVIEN ORDER BY MANHANVIEN")) {
            while (rs.next()) {
                EmployeeItem item = new EmployeeItem(rs.getString("MANHANVIEN"), rs.getString("TENNHANVIEN"));
                maNvComboBoxEdit.addItem(item);
                if (item.getMaNV().equals(currentMaNvTaoGoc)) {
                    selectedEmployeeToEdit = item;
                }
            }
            if (selectedEmployeeToEdit != null) {
                maNvComboBoxEdit.setSelectedItem(selectedEmployeeToEdit);
            }
        } catch (Exception ex) { JOptionPane.showMessageDialog(editDialog, "Lỗi tải DS Nhân viên: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE); }
        formPanel.add(maNvComboBoxEdit, gbc);

        y++;
        gbc.gridy = y; gbc.gridx = 0; formPanel.add(new JLabel("Trạng Thái (*):"), gbc);
        gbc.gridx = 1; JComboBox<String> trangThaiComboBoxEdit = new JComboBox<>(new String[]{TT_CHO_THUC_HIEN, TT_DANG_THUC_HIEN, TT_HOAN_THANH});
        trangThaiComboBoxEdit.setSelectedItem(currentTrangThai); trangThaiComboBoxEdit.setFont(PLAIN_FONT); formPanel.add(trangThaiComboBoxEdit, gbc);

        y++;
        gbc.gridy = y; gbc.gridx = 0; formPanel.add(new JLabel("Ngày Bảo Dưỡng (*):"), gbc);
        gbc.gridx = 1; JDateChooser ngayBdChooserEdit = new JDateChooser(); ngayBdChooserEdit.setDateFormatString("yyyy-MM-dd");
        try {
            if (currentNgayBdStr != null && !currentNgayBdStr.equals("N/A")) {
                 ngayBdChooserEdit.setDate(dateFormat.parse(currentNgayBdStr));
            } else {
                ngayBdChooserEdit.setDate(new Date());
            }
        } catch (ParseException e) {
            ngayBdChooserEdit.setDate(new Date()); System.err.println("Lỗi parse ngày bảo dưỡng khi sửa: " + currentNgayBdStr);
        }
        ngayBdChooserEdit.setFont(PLAIN_FONT); formPanel.add(ngayBdChooserEdit, gbc);

        editDialog.add(formPanel, BorderLayout.CENTER);
        JPanel buttonPanelDialog = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10,10)); buttonPanelDialog.setBorder(new EmptyBorder(0,0,10,10)); buttonPanelDialog.setBackground(BACKGROUND_COLOR);
        JButton btnSaveEdit = new JButton("Lưu Thay Đổi"); styleButton(btnSaveEdit, HEADER_COLOR, Color.WHITE);
        btnSaveEdit.addActionListener(e -> {
            String newServiceItem = (String) serviceComboBoxEdit.getSelectedItem();
            String newMaDv = newServiceItem != null ? newServiceItem.split(" - ")[0] : null;

            EmployeeItem selectedEmployeeNew = (EmployeeItem) maNvComboBoxEdit.getSelectedItem();
            String newMaNv = null;
            if (selectedEmployeeNew != null) {
                newMaNv = selectedEmployeeNew.getMaNV();
            }

            String newTrangThai = (String) trangThaiComboBoxEdit.getSelectedItem();
            Date newNgayBd = ngayBdChooserEdit.getDate();

            if (newMaDv == null || newMaNv == null || newTrangThai == null || newNgayBd == null) {
                JOptionPane.showMessageDialog(editDialog, "Vui lòng chọn đủ thông tin (*).", "Thiếu Thông Tin", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String sql = "UPDATE BAODUONGXE SET MADVBAODUONG = ?, MANHANVIEN = ?, TRANGTHAI = ?, NGAYBAODUONG = ? WHERE MABAODUONG = ?";
            System.out.println("Executing SQL (openEditMaintenanceDialog - Save): " + sql);
            System.out.println("Values: " + newMaDv + ", " + newMaNv + ", " + newTrangThai + ", " + (newNgayBd != null ? new java.sql.Date(newNgayBd.getTime()) : null) + ", " + maBd);

            try (Connection conn = ConnectionOracle.getOracleConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, newMaDv);
                pstmt.setString(2, newMaNv);
                pstmt.setString(3, newTrangThai);
                pstmt.setDate(4, new java.sql.Date(newNgayBd.getTime()));
                pstmt.setString(5, maBd);

                int updatedRows = pstmt.executeUpdate();
                if (updatedRows > 0) {
                    JOptionPane.showMessageDialog(editDialog, "Cập nhật thành công!");
                    loadMaintenanceData();
                    editDialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(editDialog, "Cập nhật thất bại. Không tìm thấy bản ghi hoặc không có thay đổi.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(editDialog, "Lỗi CSDL khi cập nhật: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });
        buttonPanelDialog.add(btnSaveEdit);
        JButton btnCancelEdit = new JButton("Hủy"); styleButton(btnCancelEdit, new Color(108,117,125), Color.WHITE); btnCancelEdit.addActionListener(e -> editDialog.dispose()); buttonPanelDialog.add(btnCancelEdit);
        editDialog.add(buttonPanelDialog, BorderLayout.SOUTH);

        editDialog.pack(); 
        editDialog.setLocationRelativeTo(this); 
        editDialog.setVisible(true);
    }

    private void openSearchEquipmentDialog() {
        if (!initSuccess) return;

        JDialog searchEquipDialog = new JDialog(this, "Tra Cứu Thông Tin Thiết Bị", true);
        searchEquipDialog.setLayout(new BorderLayout(10, 10));

        JPanel topPanel = new JPanel(new GridBagLayout());
        topPanel.setBorder(new EmptyBorder(10, 10, 5, 10));
        topPanel.setBackground(PANEL_BACKGROUND_COLOR);
        GridBagConstraints gbcTop = new GridBagConstraints();
        gbcTop.insets = new Insets(5,5,5,5);
        gbcTop.anchor = GridBagConstraints.WEST;

        gbcTop.gridx = 0; gbcTop.gridy = 0; gbcTop.weightx = 0.0; gbcTop.fill = GridBagConstraints.NONE;
        topPanel.add(new JLabel("Nhập Tên hoặc Mã thiết bị:"), gbcTop);

        gbcTop.gridx = 1; gbcTop.gridy = 0; gbcTop.weightx = 1.0; gbcTop.fill = GridBagConstraints.HORIZONTAL;
        JTextField searchField = new JTextField();
        searchField.setFont(PLAIN_FONT);
        topPanel.add(searchField, gbcTop);

        gbcTop.gridx = 2; gbcTop.gridy = 0; gbcTop.weightx = 0.0; gbcTop.fill = GridBagConstraints.NONE;
        JButton btnDoSearchEquip = new JButton("Tìm Kiếm");
        styleButton(btnDoSearchEquip, HEADER_COLOR, Color.WHITE);
        topPanel.add(btnDoSearchEquip, gbcTop);
        searchEquipDialog.add(topPanel, BorderLayout.NORTH);

        DefaultTableModel equipTableModel = new DefaultTableModel(
                new Object[]{"Mã TB", "Tên Thiết Bị", "Giá Nhập", "Ngày Nhập", "Hạn SD", "Mã NV Nhập"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable equipTable = new JTable(equipTableModel);
        equipTable.setFont(PLAIN_FONT); equipTable.setRowHeight(25);
        equipTable.getTableHeader().setFont(BOLD_FONT); equipTable.getTableHeader().setBackground(HEADER_COLOR); equipTable.getTableHeader().setForeground(Color.WHITE);

        JScrollPane equipScrollPane = new JScrollPane(equipTable);
        equipScrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        searchEquipDialog.add(equipScrollPane, BorderLayout.CENTER);

        btnDoSearchEquip.addActionListener(e -> {
            String searchTerm = searchField.getText().trim();
            equipTableModel.setRowCount(0);
            if (searchTerm.isEmpty()) {
                JOptionPane.showMessageDialog(searchEquipDialog, "Vui lòng nhập từ khóa tìm kiếm.", "Thông Tin", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            String sql = "SELECT MATHIETBI, TENTHIETBI, GIANHAP, NGAYNHAP, HANSUDUNG, MANHANVIEN " +
                         "FROM THIETBI " +
                         "WHERE UPPER(MATHIETBI) LIKE UPPER(?) OR UPPER(TENTHIETBI) LIKE UPPER(?) " +
                         "ORDER BY TENTHIETBI";
            System.out.println("Executing SQL (openSearchEquipmentDialog): " + sql);
            try (Connection conn = ConnectionOracle.getOracleConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                String likePattern = "%" + searchTerm + "%";
                pstmt.setString(1, likePattern); pstmt.setString(2, likePattern);
                try (ResultSet rs = pstmt.executeQuery()) {
                    boolean found = false;
                    while (rs.next()) {
                        found = true; Vector<Object> row = new Vector<>();
                        row.add(rs.getString("MATHIETBI")); row.add(rs.getString("TENTHIETBI"));
                        row.add(rs.getBigDecimal("GIANHAP"));
                        Date ngayNhap = rs.getDate("NGAYNHAP"); row.add(ngayNhap != null ? dateFormat.format(ngayNhap) : "N/A");
                        Date hanSd = rs.getDate("HANSUDUNG"); row.add(hanSd != null ? dateFormat.format(hanSd) : "N/A");
                        row.add(rs.getString("MANHANVIEN") != null ? rs.getString("MANHANVIEN") : "N/A");
                        equipTableModel.addRow(row);
                    }
                    if (!found) { JOptionPane.showMessageDialog(searchEquipDialog, "Không tìm thấy thiết bị nào phù hợp.", "Thông Báo", JOptionPane.INFORMATION_MESSAGE); }
                }
            } catch (SQLException | ClassNotFoundException ex) {
                JOptionPane.showMessageDialog(searchEquipDialog, "Lỗi khi tìm kiếm thiết bị: " + ex.getMessage(), "Lỗi Database", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });

        JPanel bottomDialogPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomDialogPanel.setBackground(PANEL_BACKGROUND_COLOR);
        JButton btnCloseDialog = new JButton("Đóng");
        styleButton(btnCloseDialog, new Color(108,117,125), Color.WHITE);
        btnCloseDialog.addActionListener(e -> searchEquipDialog.dispose());
        bottomDialogPanel.add(btnCloseDialog);
        searchEquipDialog.add(bottomDialogPanel, BorderLayout.SOUTH);

        searchEquipDialog.pack(); 
        searchEquipDialog.setMinimumSize(new Dimension(700, 450)); 
        searchEquipDialog.setLocationRelativeTo(this); 
        searchEquipDialog.setVisible(true);
    }
    

    public static void main(String args[]) {
        // UserToken testToken = new UserToken(); // Không dùng token giả trực tiếp ở đây nữa
        // testToken.setStatus(true);
        // testToken.setEntityId("NV001"); 
        // testToken.setEntityFullName("Nhân Viên Test Chính");
        // testToken.setLoginUsername("nvtestmain_login");
        // testToken.setRole(RoleGroupConstants.EMPLOYEE);
        // DangNhap.currentUserToken = testToken; // Không nên set token giả nếu constructor đã thay đổi

        java.awt.EventQueue.invokeLater(() -> {
            // Để test, bạn cần tạo UserToken giả và đặt nó vào DangNhap.currentUserToken *TRƯỚC KHI*
            // gọi constructor của ThucHienBaoDuongXe, bởi vì isAuthenticatedAndAuthorized sẽ đọc nó.
            // Hoặc, bạn phải truyền trực tiếp ID và Name vào constructor khi test.
            UserToken testTokenForMain = new UserToken();
            testTokenForMain.setStatus(true);
            testTokenForMain.setRole(RoleGroupConstants.EMPLOYEE);
            // Đặt các giá trị khác nếu cần cho logic trong isAuthenticatedAndAuthorized
            DangNhap.currentUserToken = testTokenForMain; // Token này dùng cho isAuthenticatedAndAuthorized

            // Truyền ID và Tên giả vào constructor để test
            ThucHienBaoDuongXe frame = new ThucHienBaoDuongXe("NV_MAIN_TEST", "Nhân Viên Test Main");
            
            // initSuccess giờ được kiểm tra bên trong constructor thông qua isAuthenticatedAndAuthorized
            // Không cần kiểm tra frame.initSuccess ở đây nữa, vì nếu false, frame sẽ tự dispose.
            if (frame.isDisplayable() && frame.isVisible() == false && frame.initSuccess) { // Kiểm tra lại điều kiện hiển thị
                 frame.setVisible(true);
            } else if (!frame.initSuccess) {
                 System.out.println("ThucHienBaoDuongXe.main: Khởi tạo thất bại, không hiển thị. (Lỗi xác thực hoặc ủy quyền)");
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
