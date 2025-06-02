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
    private String currentEmployeeId; //
    private String currentEmployeeFullName; //
    private boolean initSuccess = false; //

    private JTable maintenanceTable; //
    private DefaultTableModel tableModel; //
    private JTextField searchBienSoField; //
    private JComboBox<String> searchTrangThaiComboBox; //
    private JDateChooser searchStartDateChooser, searchEndDateChooser; //

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd"); //
    private static final Color BACKGROUND_COLOR = new Color(240, 245, 250); //
    private static final Color PANEL_BACKGROUND_COLOR = Color.WHITE; //
    private static final Color HEADER_COLOR = new Color(0, 123, 255); //
    private static final Font BOLD_FONT = new Font("Segoe UI", Font.BOLD, 14); //
    private static final Font PLAIN_FONT = new Font("Segoe UI", Font.PLAIN, 14); //

    private static final String TT_CHO_THUC_HIEN = "CHO THUC HIEN"; //
    private static final String TT_DANG_THUC_HIEN = "DANG THUC HIEN"; //
    private static final String TT_HOAN_THANH = "HOAN THANH"; //

    // Lớp EmployeeItem không còn cần thiết vì JComboBox nhân viên đã bị loại bỏ hoặc thay thế.

    public ThucHienBaoDuongXe(String employeeId, String employeeFullName) { //
        this.currentEmployeeId = employeeId; //
        this.currentEmployeeFullName = employeeFullName; //

        if (!isAuthenticatedAndAuthorized()) { //
            SwingUtilities.invokeLater(this::dispose); //
            return; //
        }

        try {
            UIManager.setLookAndFeel(new FlatLightLaf()); //
        } catch (UnsupportedLookAndFeelException ex) {
            System.err.println("Lỗi khi khởi tạo LaF: " + ex.getMessage()); //
        }

        setTitle("Thực Hiện Bảo Dưỡng Xe - Nhân viên: " + (this.currentEmployeeFullName != null && !this.currentEmployeeFullName.isEmpty() ? this.currentEmployeeFullName : this.currentEmployeeId)); //
        setSize(1250, 780); //
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); //
        setLocationRelativeTo(null); //
        getContentPane().setBackground(BACKGROUND_COLOR); //
        setLayout(new BorderLayout(10, 10)); //

        initComponentsUI(); //

        if (initSuccess) { //
            loadMaintenanceData(); //
        }
    }
    
    public ThucHienBaoDuongXe() { //
        this(null, null); //
    }


    private boolean isAuthenticatedAndAuthorized() { //
        if (DangNhap.currentUserToken == null || !DangNhap.currentUserToken.isStatus()) { //
            handleAuthFailure("Bạn cần đăng nhập để truy cập chức năng này.", "Yêu Cầu Đăng Nhập"); //
            return false; //
        }
        String role = DangNhap.currentUserToken.getRole(); //

        if (!RoleGroupConstants.EMPLOYEE.equals(role)) { //
            handleAuthFailure("Chỉ nhân viên mới có quyền truy cập chức năng này.\nVai trò của bạn: " + //
                              (role == null || role.isEmpty() ? "Không xác định" : role), "Không Được Phép"); //
            return false; //
        }

        if (this.currentEmployeeId == null || this.currentEmployeeId.trim().isEmpty()) { //
            handleAuthFailure("Thông tin Mã Nhân Viên không được cung cấp hoặc không hợp lệ.", "Lỗi Dữ Liệu Nhân Viên"); //
            return false; //
        }
        if (this.currentEmployeeFullName == null || this.currentEmployeeFullName.trim().isEmpty()){ //
             handleAuthFailure("Thông tin Tên Nhân Viên không được cung cấp hoặc không hợp lệ.", "Lỗi Dữ Liệu Nhân Viên"); //
            return false; //
        }
        
        initSuccess = true; //
        return true; //
    }

    private void handleAuthFailure(String message, String title) { //
        EventQueue.invokeLater(() -> { //
            JOptionPane.showMessageDialog(null, message, title, JOptionPane.ERROR_MESSAGE); //
            Window window = SwingUtilities.getWindowAncestor(this); //
            if (window != null && window.isDisplayable()) { //
                 new DangNhap().setVisible(true); //
                 window.dispose(); //
            } else if (this.isDisplayable()){ //
                 new DangNhap().setVisible(true); //
                 this.dispose(); //
            }
        });
    }

    private void initComponentsUI() { //
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10)); //
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15)); //
        mainPanel.setBackground(BACKGROUND_COLOR); //

        JPanel searchPanel = new JPanel(new GridBagLayout()); //
        searchPanel.setBackground(PANEL_BACKGROUND_COLOR); //
        searchPanel.setBorder(BorderFactory.createTitledBorder( //
                BorderFactory.createEtchedBorder(), "Tìm Kiếm Lịch Bảo Dưỡng", //
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, //
                javax.swing.border.TitledBorder.DEFAULT_POSITION, //
                BOLD_FONT, HEADER_COLOR)); //
        GridBagConstraints gbcSearch = new GridBagConstraints(); //
        gbcSearch.insets = new Insets(5, 5, 5, 5);  //
        gbcSearch.anchor = GridBagConstraints.WEST; //

        gbcSearch.gridy = 0; //
        gbcSearch.gridx = 0; gbcSearch.weightx = 0.0; gbcSearch.fill = GridBagConstraints.NONE; searchPanel.add(new JLabel("Biển số xe:"), gbcSearch); //
        gbcSearch.gridx = 1; gbcSearch.weightx = 0.4; gbcSearch.fill = GridBagConstraints.HORIZONTAL; searchBienSoField = new JTextField(); searchBienSoField.setFont(PLAIN_FONT); searchPanel.add(searchBienSoField, gbcSearch); //

        gbcSearch.gridx = 2; gbcSearch.weightx = 0.0; gbcSearch.fill = GridBagConstraints.NONE; gbcSearch.insets = new Insets(5, 15, 5, 5); searchPanel.add(new JLabel("Trạng thái:"), gbcSearch); gbcSearch.insets = new Insets(5, 5, 5, 5);  //
        gbcSearch.gridx = 3; gbcSearch.weightx = 0.6; gbcSearch.fill = GridBagConstraints.HORIZONTAL; searchTrangThaiComboBox = new JComboBox<>(new String[]{"Tất cả", TT_CHO_THUC_HIEN, TT_DANG_THUC_HIEN, TT_HOAN_THANH}); searchTrangThaiComboBox.setFont(PLAIN_FONT); searchPanel.add(searchTrangThaiComboBox, gbcSearch); //

        gbcSearch.gridy = 1; //
        gbcSearch.gridx = 0; gbcSearch.weightx = 0.0; gbcSearch.fill = GridBagConstraints.NONE; searchPanel.add(new JLabel("Từ ngày:"), gbcSearch); //
        gbcSearch.gridx = 1; gbcSearch.weightx = 0.4; gbcSearch.fill = GridBagConstraints.HORIZONTAL; searchStartDateChooser = new JDateChooser(); searchStartDateChooser.setDateFormatString("yyyy-MM-dd"); searchStartDateChooser.setFont(PLAIN_FONT); searchPanel.add(searchStartDateChooser, gbcSearch); //

        gbcSearch.gridx = 2; gbcSearch.weightx = 0.0; gbcSearch.fill = GridBagConstraints.NONE; gbcSearch.insets = new Insets(5, 15, 5, 5); searchPanel.add(new JLabel("Đến ngày:"), gbcSearch); gbcSearch.insets = new Insets(5, 5, 5, 5);  //
        gbcSearch.gridx = 3; gbcSearch.weightx = 0.6; gbcSearch.fill = GridBagConstraints.HORIZONTAL; searchEndDateChooser = new JDateChooser(); searchEndDateChooser.setDateFormatString("yyyy-MM-dd"); searchEndDateChooser.setFont(PLAIN_FONT); searchPanel.add(searchEndDateChooser, gbcSearch); //

        JPanel searchButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0)); //
        searchButtonPanel.setBackground(PANEL_BACKGROUND_COLOR); //
        JButton btnSearch = new JButton("Tìm Kiếm"); styleButton(btnSearch, HEADER_COLOR, Color.WHITE); //
        btnSearch.addActionListener(e -> loadMaintenanceData()); //
        JButton btnClearSearch = new JButton("Xóa Filter"); btnClearSearch.setFont(BOLD_FONT);  //
        btnClearSearch.addActionListener(e -> clearSearchFilters()); //
        searchButtonPanel.add(btnSearch); //
        searchButtonPanel.add(btnClearSearch); //

        gbcSearch.gridy = 2; //
        gbcSearch.gridx = 0; //
        gbcSearch.gridwidth = 4;  //
        gbcSearch.anchor = GridBagConstraints.EAST;  //
        gbcSearch.fill = GridBagConstraints.NONE;  //
        gbcSearch.weightx = 1.0; //
        searchPanel.add(searchButtonPanel, gbcSearch); //
        mainPanel.add(searchPanel, BorderLayout.NORTH); //

        tableModel = new DefaultTableModel( //
                new Object[]{"Mã BD", "Biển Số", "Loại Xe", "Tên Dịch Vụ", "Mã NV Tạo", "Tên NV Tạo", "Trạng Thái", "Ngày BD"}, 0) { //
            @Override
            public boolean isCellEditable(int row, int column) { return false; } //
        };
        maintenanceTable = new JTable(tableModel); //
        maintenanceTable.setFont(PLAIN_FONT); //
        maintenanceTable.setRowHeight(28); //
        maintenanceTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); //
        maintenanceTable.getTableHeader().setFont(BOLD_FONT); //
        maintenanceTable.getTableHeader().setBackground(HEADER_COLOR); //
        maintenanceTable.getTableHeader().setForeground(Color.WHITE); //
        maintenanceTable.getTableHeader().setReorderingAllowed(false); //

        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel); //
        maintenanceTable.setRowSorter(sorter); //

        JScrollPane scrollPane = new JScrollPane(maintenanceTable); //
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(200,200,200))); //
        mainPanel.add(scrollPane, BorderLayout.CENTER); //

        JPanel bottomOuterPanel = new JPanel(new BorderLayout(0,0)); //
        bottomOuterPanel.setBackground(PANEL_BACKGROUND_COLOR); //
        bottomOuterPanel.setBorder(new EmptyBorder(8,10,8,10));  //

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 5));  //
        actionPanel.setBackground(PANEL_BACKGROUND_COLOR); //
        
        JButton btnEdit = new JButton("Sửa/Cập Nhật"); styleButton(btnEdit, new Color(23, 162, 184), Color.WHITE); btnEdit.addActionListener(e -> openEditMaintenanceDialog()); actionPanel.add(btnEdit); //
        JButton btnSearchEquipment = new JButton("Tra Cứu Thiết Bị"); styleButton(btnSearchEquipment, new Color(108, 117, 125), Color.WHITE); btnSearchEquipment.addActionListener(e -> openSearchEquipmentDialog()); actionPanel.add(btnSearchEquipment); //
        // Loại bỏ nút "Làm Mới DS"
        // JButton btnRefresh = new JButton("Làm Mới DS"); styleButton(btnRefresh, new Color(255, 193, 7), Color.BLACK); btnRefresh.addActionListener(e -> loadMaintenanceData()); actionPanel.add(btnRefresh);

        bottomOuterPanel.add(actionPanel, BorderLayout.CENTER); //

        JButton btnBack = new JButton("Quay Lại"); styleButton(btnBack, new Color(80,80,80), Color.WHITE); //
        btnBack.addActionListener(e -> { //
            new EmployeeHomePage().setVisible(true); //
            ThucHienBaoDuongXe.this.dispose(); //
        });
        JPanel backButtonWrapperPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 5));  //
        backButtonWrapperPanel.setBackground(PANEL_BACKGROUND_COLOR); //
        backButtonWrapperPanel.add(btnBack); //
        bottomOuterPanel.add(backButtonWrapperPanel, BorderLayout.EAST); //

        mainPanel.add(bottomOuterPanel, BorderLayout.SOUTH); //
        add(mainPanel); //
    }

    private void styleButton(JButton button, Color backgroundColor, Color foregroundColor) { //
        button.setFont(BOLD_FONT); //
        button.setBackground(backgroundColor); //
        button.setForeground(foregroundColor); //
        button.setFocusPainted(false); //
        button.setBorder(new EmptyBorder(10, 18, 10, 18));  //
        button.setCursor(new Cursor(Cursor.HAND_CURSOR)); //
    }

    private void clearSearchFilters() { //
        searchBienSoField.setText(""); //
        searchTrangThaiComboBox.setSelectedIndex(0); //
        searchStartDateChooser.setDate(null); //
        searchEndDateChooser.setDate(null); //
        loadMaintenanceData(); //
    }

    private void loadMaintenanceData() { //
        if (!initSuccess) return; //

        tableModel.setRowCount(0); //
        String sql = "SELECT b.MABAODUONG, b.BIENSO, x.TENLOAIXE, dv.TENDV, b.MANHANVIEN, nv.TENNHANVIEN, b.TRANGTHAI, b.NGAYBAODUONG " + //
                     "FROM BAODUONGXE b " + //
                     "JOIN XE x ON b.BIENSO = x.BIENSO " + //
                     "JOIN DICHVUBAODUONG dv ON b.MADVBAODUONG = dv.MADVBAODUONG " + //
                     "LEFT JOIN NHANVIEN nv ON b.MANHANVIEN = nv.MANHANVIEN"; //

        StringBuilder conditions = new StringBuilder(); //
        String bienSo = searchBienSoField.getText().trim(); //
        String trangThai = (String) searchTrangThaiComboBox.getSelectedItem(); //
        Date startDate = searchStartDateChooser.getDate(); //
        Date endDate = searchEndDateChooser.getDate(); //

        if (!bienSo.isEmpty()) { //
            conditions.append("UPPER(b.BIENSO) LIKE UPPER('%").append(bienSo.replace("'", "''")).append("%')"); //
        }
        if (trangThai != null && !"Tất cả".equals(trangThai)) { //
            if (conditions.length() > 0) conditions.append(" AND "); //
            conditions.append("b.TRANGTHAI = '").append(trangThai).append("'"); //
        }
        if (startDate != null) { //
            if (conditions.length() > 0) conditions.append(" AND "); //
            conditions.append("b.NGAYBAODUONG >= TO_DATE('").append(dateFormat.format(startDate)).append("', 'YYYY-MM-DD')"); //
        }
        if (endDate != null) { //
            if (conditions.length() > 0) conditions.append(" AND "); //
            Calendar c = Calendar.getInstance(); c.setTime(endDate); c.add(Calendar.DAY_OF_MONTH, 1); //
            conditions.append("b.NGAYBAODUONG < TO_DATE('").append(dateFormat.format(c.getTime())).append("', 'YYYY-MM-DD')"); //
        }

        if (conditions.length() > 0) { //
            sql += " WHERE " + conditions.toString(); //
        }
        sql += " ORDER BY b.NGAYBAODUONG DESC, b.MABAODUONG DESC"; //
        System.out.println("Executing SQL (loadMaintenanceData): " + sql); //

        try (Connection conn = ConnectionOracle.getOracleConnection(); //
             Statement stmt = conn.createStatement(); //
             ResultSet rs = stmt.executeQuery(sql)) { //
            while (rs.next()) { //
                Vector<Object> rowData = new Vector<>(); // Changed variable name for clarity
                rowData.add(rs.getString("MABAODUONG")); //
                rowData.add(rs.getString("BIENSO")); //
                rowData.add(rs.getString("TENLOAIXE")); //
                rowData.add(rs.getString("TENDV")); //
                rowData.add(rs.getString("MANHANVIEN")); //
                rowData.add(rs.getString("TENNHANVIEN") != null ? rs.getString("TENNHANVIEN") : "N/A"); //
                rowData.add(rs.getString("TRANGTHAI")); //
                Date ngayBD = rs.getDate("NGAYBAODUONG"); //
                rowData.add(ngayBD != null ? dateFormat.format(ngayBD) : "N/A"); //
                tableModel.addRow(rowData); //
            }
        } catch (SQLException | ClassNotFoundException e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi tải dữ liệu bảo dưỡng: " + e.getMessage(), "Lỗi Database", JOptionPane.ERROR_MESSAGE); //
            e.printStackTrace(); //
        }
    }

    private void openEditMaintenanceDialog() { //
        if (!initSuccess) return; //
        int selectedRow = maintenanceTable.getSelectedRow(); //
        if (selectedRow == -1) { //
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một mục bảo dưỡng để sửa.", "Chưa Chọn Mục", JOptionPane.WARNING_MESSAGE); //
            return; //
        }
        int modelRow = maintenanceTable.convertRowIndexToModel(selectedRow); //

        String maBd = (String) tableModel.getValueAt(modelRow, 0); //
        String currentBienSo = (String) tableModel.getValueAt(modelRow, 1); //
        String currentTenDv = (String) tableModel.getValueAt(modelRow, 3); //
        // String currentMaNvTao = (String) tableModel.getValueAt(modelRow, 4); // Không dùng nữa cho hiển thị ở field này
        // String currentTenNvTao = (String) tableModel.getValueAt(modelRow, 5); // Không dùng nữa cho hiển thị ở field này
        String currentTrangThai = (String) tableModel.getValueAt(modelRow, 6); //
        String currentNgayBdStr = (String) tableModel.getValueAt(modelRow, 7); //

        JDialog editDialog = new JDialog(this, "Sửa Thông Tin Bảo Dưỡng [Mã BD: " + maBd +"]", true); //
        editDialog.setLayout(new BorderLayout(10,10)); //

        JPanel formPanel = new JPanel(new GridBagLayout()); formPanel.setBorder(new EmptyBorder(20,20,20,20)); formPanel.setBackground(Color.WHITE); //
        GridBagConstraints gbc = new GridBagConstraints(); gbc.insets = new Insets(5,5,5,5); gbc.anchor = GridBagConstraints.WEST; //
        int yPos = 0; // Renamed variable y to yPos for clarity

        gbc.gridy = yPos; gbc.gridx = 0; gbc.weightx = 0.2; gbc.fill = GridBagConstraints.NONE; formPanel.add(new JLabel("Biển Số Xe:"), gbc); //
        gbc.gridx = 1; gbc.weightx = 0.8; gbc.fill = GridBagConstraints.HORIZONTAL; 
        JTextField bienSoField = new JTextField(currentBienSo); //
        bienSoField.setEditable(false); bienSoField.setFont(PLAIN_FONT); formPanel.add(bienSoField, gbc); //

        yPos++;
        gbc.gridy = yPos; gbc.gridx = 0; formPanel.add(new JLabel("Dịch Vụ:"), gbc); //
        gbc.gridx = 1; 
        JTextField serviceFieldEdit = new JTextField(currentTenDv); //
        serviceFieldEdit.setFont(PLAIN_FONT); //
        serviceFieldEdit.setEditable(false); 
        formPanel.add(serviceFieldEdit, gbc); //

        yPos++;
        gbc.gridy = yPos; gbc.gridx = 0; formPanel.add(new JLabel("NV Thực Hiện Sửa:"), gbc); // Đổi nhãn cho rõ ràng
        gbc.gridx = 1; 
        String nvThucHienDisplayText = this.currentEmployeeId + //
                                   (this.currentEmployeeFullName != null && !this.currentEmployeeFullName.trim().isEmpty() //
                                   ? " - " + this.currentEmployeeFullName : ""); //
        JTextField nvThucHienFieldEdit = new JTextField(nvThucHienDisplayText);
        nvThucHienFieldEdit.setFont(PLAIN_FONT); //
        nvThucHienFieldEdit.setEditable(false); 
        formPanel.add(nvThucHienFieldEdit, gbc);


        yPos++;
        gbc.gridy = yPos; gbc.gridx = 0; formPanel.add(new JLabel("Trạng Thái (*):"), gbc); //
        gbc.gridx = 1; JComboBox<String> trangThaiComboBoxEdit = new JComboBox<>(new String[]{TT_CHO_THUC_HIEN, TT_DANG_THUC_HIEN, TT_HOAN_THANH}); //
        trangThaiComboBoxEdit.setSelectedItem(currentTrangThai); trangThaiComboBoxEdit.setFont(PLAIN_FONT); formPanel.add(trangThaiComboBoxEdit, gbc); //

        yPos++;
        gbc.gridy = yPos; gbc.gridx = 0; formPanel.add(new JLabel("Ngày Bảo Dưỡng:"), gbc); //
        gbc.gridx = 1; JDateChooser ngayBdChooserEdit = new JDateChooser(); ngayBdChooserEdit.setDateFormatString("yyyy-MM-dd"); //
        try {
            if (currentNgayBdStr != null && !currentNgayBdStr.equals("N/A")) { //
                 ngayBdChooserEdit.setDate(dateFormat.parse(currentNgayBdStr)); //
            } else {
                ngayBdChooserEdit.setDate(new Date()); //
            }
        } catch (ParseException e) {
            ngayBdChooserEdit.setDate(new Date()); System.err.println("Lỗi parse ngày bảo dưỡng khi sửa: " + currentNgayBdStr); //
        }
        ngayBdChooserEdit.setFont(PLAIN_FONT); //
        ngayBdChooserEdit.setEnabled(false); 
        formPanel.add(ngayBdChooserEdit, gbc); //

        editDialog.add(formPanel, BorderLayout.CENTER); //
        JPanel buttonPanelDialog = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10,10)); buttonPanelDialog.setBorder(new EmptyBorder(0,0,10,10)); buttonPanelDialog.setBackground(BACKGROUND_COLOR); //
        JButton btnSaveEdit = new JButton("Lưu Thay Đổi"); styleButton(btnSaveEdit, HEADER_COLOR, Color.WHITE); //
        btnSaveEdit.addActionListener(e -> { //
            String newTrangThai = (String) trangThaiComboBoxEdit.getSelectedItem(); //

            if (newTrangThai == null) { //
                JOptionPane.showMessageDialog(editDialog, "Vui lòng chọn Trạng Thái.", "Thiếu Thông Tin", JOptionPane.WARNING_MESSAGE); //
                return; //
            }
            String sql = "UPDATE BAODUONGXE SET TRANGTHAI = ? WHERE MABAODUONG = ?"; //
            System.out.println("Executing SQL (openEditMaintenanceDialog - Save): " + sql); //
            System.out.println("Values: " + newTrangThai + ", " + maBd); //

            try (Connection conn = ConnectionOracle.getOracleConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) { //
                pstmt.setString(1, newTrangThai); //
                pstmt.setString(2, maBd); //

                int updatedRows = pstmt.executeUpdate(); //
                if (updatedRows > 0) { //
                    JOptionPane.showMessageDialog(editDialog, "Cập nhật trạng thái thành công!"); //
                    loadMaintenanceData(); //
                    editDialog.dispose(); //
                } else {
                    JOptionPane.showMessageDialog(editDialog, "Cập nhật thất bại. Không tìm thấy bản ghi hoặc không có thay đổi.", "Lỗi", JOptionPane.ERROR_MESSAGE); //
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(editDialog, "Lỗi CSDL khi cập nhật: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE); //
                ex.printStackTrace(); //
            }
        });
        buttonPanelDialog.add(btnSaveEdit); //
        JButton btnCancelEdit = new JButton("Hủy"); styleButton(btnCancelEdit, new Color(108,117,125), Color.WHITE); btnCancelEdit.addActionListener(e -> editDialog.dispose()); buttonPanelDialog.add(btnCancelEdit); //
        editDialog.add(buttonPanelDialog, BorderLayout.SOUTH); //

        editDialog.pack();  //
        editDialog.setLocationRelativeTo(this);  //
        editDialog.setVisible(true); //
    }
    
    private boolean loadEquipmentTableData(DefaultTableModel model, String searchTerm, JDialog parentDialog) { //
        model.setRowCount(0); //
        String sql; //
        boolean found = false; //

        if (searchTerm == null || searchTerm.trim().isEmpty()) { //
            sql = "SELECT MATHIETBI, TENTHIETBI, GIANHAP, NGAYNHAP, HANSUDUNG FROM THIETBI ORDER BY TENTHIETBI"; //
        } else {
            sql = "SELECT MATHIETBI, TENTHIETBI, GIANHAP, NGAYNHAP, HANSUDUNG FROM THIETBI " + //
                  "WHERE UPPER(MATHIETBI) LIKE UPPER(?) OR UPPER(TENTHIETBI) LIKE UPPER(?) " + //
                  "ORDER BY TENTHIETBI"; //
        }
        System.out.println("Executing SQL (loadEquipmentTableData): " + sql + (searchTerm != null && !searchTerm.isEmpty() ? " with term: " + searchTerm : " (loading all)")); //

        try (Connection conn = ConnectionOracle.getOracleConnection(); //
             PreparedStatement pstmt = conn.prepareStatement(sql)) { //

            if (searchTerm != null && !searchTerm.trim().isEmpty()) { //
                String likePattern = "%" + searchTerm.trim() + "%"; //
                pstmt.setString(1, likePattern); //
                pstmt.setString(2, likePattern); //
            }

            try (ResultSet rs = pstmt.executeQuery()) { //
                while (rs.next()) { //
                    found = true; //
                    Vector<Object> row = new Vector<>(); //
                    row.add(rs.getString("MATHIETBI")); //
                    row.add(rs.getString("TENTHIETBI")); //
                    row.add(rs.getBigDecimal("GIANHAP")); //
                    Date ngayNhapSQL = rs.getDate("NGAYNHAP"); // Renamed variable for clarity //
                    row.add(ngayNhapSQL != null ? dateFormat.format(ngayNhapSQL) : "N/A"); //
                    Date hanSdSQL = rs.getDate("HANSUDUNG"); // Renamed variable for clarity //
                    row.add(hanSdSQL != null ? dateFormat.format(hanSdSQL) : "N/A"); //
                    model.addRow(row); //
                }
            }
        } catch (SQLException | ClassNotFoundException ex) {
            JOptionPane.showMessageDialog(parentDialog, "Lỗi khi truy vấn dữ liệu thiết bị: " + ex.getMessage(), "Lỗi Database", JOptionPane.ERROR_MESSAGE); //
            ex.printStackTrace(); //
            return false; //
        }
        return found; //
    }


    private void openSearchEquipmentDialog() { //
        if (!initSuccess) return; //

        JDialog searchEquipDialog = new JDialog(this, "Tra Cứu Thông Tin Thiết Bị", true); //
        searchEquipDialog.setLayout(new BorderLayout(10, 10)); //

        JPanel topPanel = new JPanel(new GridBagLayout()); //
        topPanel.setBorder(new EmptyBorder(10, 10, 5, 10)); //
        topPanel.setBackground(PANEL_BACKGROUND_COLOR); //
        GridBagConstraints gbcTop = new GridBagConstraints(); //
        gbcTop.insets = new Insets(5,5,5,5); //
        gbcTop.anchor = GridBagConstraints.WEST; //

        gbcTop.gridx = 0; gbcTop.gridy = 0; gbcTop.weightx = 0.0; gbcTop.fill = GridBagConstraints.NONE; //
        topPanel.add(new JLabel("Nhập Tên hoặc Mã thiết bị:"), gbcTop); //

        gbcTop.gridx = 1; gbcTop.gridy = 0; gbcTop.weightx = 1.0; gbcTop.fill = GridBagConstraints.HORIZONTAL; //
        JTextField searchField = new JTextField(); //
        searchField.setFont(PLAIN_FONT); //
        topPanel.add(searchField, gbcTop); //

        gbcTop.gridx = 2; gbcTop.gridy = 0; gbcTop.weightx = 0.0; gbcTop.fill = GridBagConstraints.NONE; //
        JButton btnDoSearchEquip = new JButton("Tìm Kiếm"); //
        styleButton(btnDoSearchEquip, HEADER_COLOR, Color.WHITE); //
        topPanel.add(btnDoSearchEquip, gbcTop); //
        searchEquipDialog.add(topPanel, BorderLayout.NORTH); //

        DefaultTableModel equipTableModel = new DefaultTableModel( //
                new Object[]{"Mã TB", "Tên Thiết Bị", "Giá Nhập", "Ngày Nhập", "Hạn SD"}, 0) {  //
            @Override
            public boolean isCellEditable(int r, int c) { return false; } //
        };
        JTable equipTable = new JTable(equipTableModel); //
        equipTable.setFont(PLAIN_FONT); equipTable.setRowHeight(25); //
        equipTable.getTableHeader().setFont(BOLD_FONT); equipTable.getTableHeader().setBackground(HEADER_COLOR); equipTable.getTableHeader().setForeground(Color.WHITE); //

        JScrollPane equipScrollPane = new JScrollPane(equipTable); //
        equipScrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200))); //
        searchEquipDialog.add(equipScrollPane, BorderLayout.CENTER); //

        loadEquipmentTableData(equipTableModel, null, searchEquipDialog); //

        btnDoSearchEquip.addActionListener(e -> { //
            String searchTermLocal = searchField.getText().trim(); //
            boolean dataFound = loadEquipmentTableData(equipTableModel, searchTermLocal, searchEquipDialog); //

            if (!dataFound && !searchTermLocal.isEmpty()) { //
                JOptionPane.showMessageDialog(searchEquipDialog, "Không tìm thấy thiết bị nào phù hợp với từ khóa '" + searchTermLocal + "'.", "Thông Báo", JOptionPane.INFORMATION_MESSAGE); //
            } else if (!dataFound && searchTermLocal.isEmpty()){ //
                 JOptionPane.showMessageDialog(searchEquipDialog, "Không có thiết bị nào trong cơ sở dữ liệu.", "Thông Báo", JOptionPane.INFORMATION_MESSAGE); //
            }
        });

        JPanel bottomDialogPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT)); //
        bottomDialogPanel.setBackground(PANEL_BACKGROUND_COLOR); //
        JButton btnCloseDialog = new JButton("Đóng"); //
        styleButton(btnCloseDialog, new Color(108,117,125), Color.WHITE); //
        btnCloseDialog.addActionListener(e -> searchEquipDialog.dispose()); //
        bottomDialogPanel.add(btnCloseDialog); //
        searchEquipDialog.add(bottomDialogPanel, BorderLayout.SOUTH); //

        searchEquipDialog.pack();  //
        searchEquipDialog.setMinimumSize(new Dimension(700, 450));  //
        searchEquipDialog.setLocationRelativeTo(this);  //
        searchEquipDialog.setVisible(true); //
    }
    

    public static void main(String args[]) { //
        java.awt.EventQueue.invokeLater(() -> { //
            UserToken testTokenForMain = new UserToken(); //
            testTokenForMain.setStatus(true); //
            testTokenForMain.setRole(RoleGroupConstants.EMPLOYEE); //
            DangNhap.currentUserToken = testTokenForMain; //
            ThucHienBaoDuongXe frame = new ThucHienBaoDuongXe("NV_MAIN_TEST", "Nhân Viên Test Main"); //
            if (frame.isDisplayable() && frame.isVisible() == false && frame.initSuccess) { //
                 frame.setVisible(true); //
            } else if (!frame.initSuccess) { //
                 System.out.println("ThucHienBaoDuongXe.main: Khởi tạo thất bại, không hiển thị. (Lỗi xác thực hoặc ủy quyền)"); //
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
