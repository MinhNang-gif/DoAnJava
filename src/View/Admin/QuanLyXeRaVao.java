package View.Admin;

import ConnectDB.ConnectionOracle; // Assuming ConnectionOracle is in ConnectDB package
import com.formdev.flatlaf.FlatLightLaf; // For UI theme

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Vector;

public class QuanLyXeRaVao extends javax.swing.JFrame {
    // UI Components
    private JTextField txtBienSo, txtMaKH, txtTimKiemBienSo;
    private JComboBox<String> comboLoaiXe;
    private JButton btnXeVao, btnXeRa, btnTimKiem, btnLamMoiInput, btnHienThiDangGui, btnQuayLai; // Renamed btnHienThiTatCa
    private JTable tableXeTrongBai;
    private DefaultTableModel tableModel;

    // Database connection
    private Connection conn;

    // Constants for vehicle types
    private static final String XE_MAY = "Xe máy";
    private static final String O_TO = "Ô tô";

    public QuanLyXeRaVao() {
        // Initialize database connection
        try {
            conn = ConnectionOracle.getOracleConnection();
        } catch (SQLException | ClassNotFoundException e) {
            JOptionPane.showMessageDialog(this, "Lỗi kết nối cơ sở dữ liệu: " + e.getMessage(), "Lỗi Database", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }

        setTitle("Quản Lý Xe Ra Vào");
        setSize(1100, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
        ((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        initComponentsUI();
        loadVehiclesInLot(); // Load vehicles 'DANG GUI' initially

        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
            SwingUtilities.updateComponentTreeUI(this);
        } catch (UnsupportedLookAndFeelException e) {
            System.err.println("Failed to initialize LaF: " + e.getMessage());
        }
    }

    private void initComponentsUI() {
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JLabel lblTitle = new JLabel("Quản Lý Xe Ra Vào");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titlePanel.add(lblTitle);
        add(titlePanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBorder(BorderFactory.createTitledBorder("Thông tin xe vào"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        inputPanel.add(new JLabel("Biển số xe:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0;
        txtBienSo = new JTextField(15);
        inputPanel.add(txtBienSo, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        inputPanel.add(new JLabel("Loại xe:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1.0;
        comboLoaiXe = new JComboBox<>(new String[]{XE_MAY, O_TO});
        inputPanel.add(comboLoaiXe, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        inputPanel.add(new JLabel("Mã Khách Hàng:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; gbc.weightx = 1.0;
        txtMaKH = new JTextField(15);
        inputPanel.add(txtMaKH, gbc);

        JPanel inputButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnXeVao = new JButton("Ghi Xe Vào");
        btnLamMoiInput = new JButton("Làm Mới");
        inputButtonPanel.add(btnLamMoiInput);
        inputButtonPanel.add(btnXeVao);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.NONE; gbc.anchor = GridBagConstraints.EAST;
        inputPanel.add(inputButtonPanel, gbc);
        centerPanel.add(inputPanel, BorderLayout.NORTH);

        JPanel tableAndControlPanel = new JPanel(new BorderLayout(10,10));
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controlPanel.add(new JLabel("Tìm kiếm Biển số:"));
        txtTimKiemBienSo = new JTextField(15);
        controlPanel.add(txtTimKiemBienSo);
        btnTimKiem = new JButton("Tìm Kiếm (Tất cả)"); // Clarified button text
        controlPanel.add(btnTimKiem);
        btnXeRa = new JButton("Ghi Xe Ra");
        btnXeRa.setEnabled(false);
        controlPanel.add(btnXeRa);
        btnHienThiDangGui = new JButton("Hiển Thị Đang Gửi"); // Renamed
        controlPanel.add(btnHienThiDangGui);
        tableAndControlPanel.add(controlPanel, BorderLayout.NORTH);

        String[] columnNames = {"Mã QLRV", "Biển số", "Mã KH", "Thời gian vào", "Thời gian ra", "Loại xe", "Trạng thái"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tableXeTrongBai = new JTable(tableModel);
        tableXeTrongBai.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableXeTrongBai.getTableHeader().setReorderingAllowed(false);
        JScrollPane scrollPane = new JScrollPane(tableXeTrongBai);
        tableAndControlPanel.add(scrollPane, BorderLayout.CENTER);
        centerPanel.add(tableAndControlPanel, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnQuayLai = new JButton("Quay Lại Trang Chủ");
        southPanel.add(btnQuayLai);
        add(southPanel, BorderLayout.SOUTH);

        btnXeVao.addActionListener(e -> addVehicleEntry());
        btnLamMoiInput.addActionListener(e -> clearInputFields());
        btnTimKiem.addActionListener(e -> searchVehicle()); // Search will show all if field is empty
        btnXeRa.addActionListener(e -> updateVehicleExit());
        btnHienThiDangGui.addActionListener(e -> { // Renamed button action
            txtTimKiemBienSo.setText("");
            loadVehiclesInLot();
        });

        btnQuayLai.addActionListener(e -> {
            this.dispose();
            new AdminHomePage().setVisible(true);
        });

        tableXeTrongBai.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (tableXeTrongBai.getSelectedRow() != -1) {
                    String trangThai = (String) tableModel.getValueAt(tableXeTrongBai.getSelectedRow(), 6);
                    btnXeRa.setEnabled("DANG GUI".equalsIgnoreCase(trangThai));
                } else {
                    btnXeRa.setEnabled(false);
                }
            }
        });
    }

    private String generateMaQLRV() {
        Random random = new Random();
        return "QL" + String.format("%05d", random.nextInt(100000));
    }

    // Loads only vehicles with TRANGTHAI = 'DANG GUI'
    private void loadVehiclesInLot() {
        tableModel.setRowCount(0);
        btnXeRa.setEnabled(false);
        String sql = "SELECT MAQUANLYRAVAO, BIENSO, MAKH, THOIGIANVAO, THOIGIANRA, LOAIXE, TRANGTHAI FROM QUANLYRAVAO WHERE TRANGTHAI = 'DANG GUI' ORDER BY THOIGIANVAO DESC";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getString("MAQUANLYRAVAO"));
                row.add(rs.getString("BIENSO"));
                row.add(rs.getString("MAKH"));
                Timestamp thoiGianVao = rs.getTimestamp("THOIGIANVAO");
                row.add(thoiGianVao != null ? sdf.format(thoiGianVao) : "");
                Timestamp thoiGianRa = rs.getTimestamp("THOIGIANRA");
                row.add(thoiGianRa != null ? sdf.format(thoiGianRa) : "");
                row.add(rs.getString("LOAIXE"));
                row.add(rs.getString("TRANGTHAI"));
                tableModel.addRow(row);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi tải danh sách xe đang gửi: " + e.getMessage(), "Lỗi Database", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    private boolean checkVehicleExistsInXE(String bienSo) throws SQLException {
        String sql = "SELECT COUNT(*) FROM XE WHERE BIENSO = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, bienSo);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    private boolean checkCustomerExists(String maKH) throws SQLException {
        String sql = "SELECT COUNT(*) FROM KHACHHANG WHERE MAKH = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, maKH);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    private void addVehicleEntry() {
        String bienSo = txtBienSo.getText().trim().toUpperCase();
        String loaiXe = (String) comboLoaiXe.getSelectedItem();
        String maKH = txtMaKH.getText().trim().toUpperCase();

        if (bienSo.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Biển số xe không được để trống.", "Lỗi Nhập Liệu", JOptionPane.ERROR_MESSAGE);
            txtBienSo.requestFocus();
            return;
        }
        if (maKH.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Mã Khách Hàng không được để trống.", "Lỗi Nhập Liệu", JOptionPane.ERROR_MESSAGE);
            txtMaKH.requestFocus();
            return;
        }

        try {
            // Check if customer exists
            if (!checkCustomerExists(maKH)) {
                JOptionPane.showMessageDialog(this, "Mã khách hàng '" + maKH + "' không tồn tại trong hệ thống.\nVui lòng thêm khách hàng trước hoặc nhập đúng mã khách hàng.", "Lỗi Khách Hàng", JOptionPane.ERROR_MESSAGE);
                txtMaKH.requestFocus(); // Optionally focus on the customer ID field
                return; // Stop further processing
            }

            // Check if vehicle exists in XE table, prompt to add if not
            if (!checkVehicleExistsInXE(bienSo)) {
                int confirmAddXe = JOptionPane.showConfirmDialog(this,
                    "Biển số " + bienSo + " chưa tồn tại trong bảng XE. Bạn có muốn thêm mới xe này không?",
                    "Xác Nhận Thêm Xe Mới", JOptionPane.YES_NO_OPTION);
                if (confirmAddXe == JOptionPane.YES_OPTION) {
                    String insertXeSql = "INSERT INTO XE (BIENSO, TENLOAIXE) VALUES (?, ?)";
                    try (PreparedStatement pstmtXe = conn.prepareStatement(insertXeSql)) {
                        pstmtXe.setString(1, bienSo);
                        pstmtXe.setString(2, loaiXe); // Use selected vehicle type
                        pstmtXe.executeUpdate();
                        JOptionPane.showMessageDialog(this, "Đã thêm xe " + bienSo + " vào danh sách xe.", "Thông Báo", JOptionPane.INFORMATION_MESSAGE);
                    } catch (SQLException exXe) {
                         JOptionPane.showMessageDialog(this, "Lỗi khi thêm xe mới vào bảng XE: " + exXe.getMessage(), "Lỗi Database", JOptionPane.ERROR_MESSAGE);
                         return;
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Xe vào bãi bị hủy do xe chưa có trong hệ thống.", "Thông Báo", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }

            // Check if vehicle is already in the lot (DANG GUI)
            String checkSql = "SELECT COUNT(*) FROM QUANLYRAVAO WHERE BIENSO = ? AND TRANGTHAI = 'DANG GUI'";
            try (PreparedStatement pstmtCheck = conn.prepareStatement(checkSql)) {
                pstmtCheck.setString(1, bienSo);
                ResultSet rsCheck = pstmtCheck.executeQuery();
                if (rsCheck.next() && rsCheck.getInt(1) > 0) {
                    JOptionPane.showMessageDialog(this, "Xe có biển số " + bienSo + " đã có trong bãi.", "Thông Báo", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }

            // Proceed to add vehicle entry to QUANLYRAVAO
            String maQLRV = generateMaQLRV();
            String sqlInsertQLRV = "INSERT INTO QUANLYRAVAO (MAQUANLYRAVAO, BIENSO, MAKH, THOIGIANVAO, TRANGTHAI, LOAIXE) VALUES (?, ?, ?, CURRENT_TIMESTAMP, 'DANG GUI', ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sqlInsertQLRV)) {
                pstmt.setString(1, maQLRV);
                pstmt.setString(2, bienSo);
                pstmt.setString(3, maKH);
                pstmt.setString(4, loaiXe);
                int affectedRows = pstmt.executeUpdate();
                if (affectedRows > 0) {
                    JOptionPane.showMessageDialog(this, "Ghi nhận xe vào bãi thành công!", "Thành Công", JOptionPane.INFORMATION_MESSAGE);
                    loadVehiclesInLot(); // Show 'DANG GUI' after successful entry
                    clearInputFields();
                } else {
                    JOptionPane.showMessageDialog(this, "Ghi nhận xe vào bãi thất bại.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi thêm xe vào bãi: " + e.getMessage(), "Lỗi Database", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void updateVehicleExit() {
        int selectedRow = tableXeTrongBai.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một xe từ danh sách để ghi nhận ra.", "Chưa Chọn Xe", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String maQLRV = (String) tableModel.getValueAt(selectedRow, 0);
        String bienSo = (String) tableModel.getValueAt(selectedRow, 1);
        String trangThai = (String) tableModel.getValueAt(selectedRow, 6);

        if (!"DANG GUI".equalsIgnoreCase(trangThai)) {
             JOptionPane.showMessageDialog(this, "Xe này không ở trạng thái 'ĐANG GỬI'. Không thể ghi xe ra.", "Lỗi Trạng Thái", JOptionPane.ERROR_MESSAGE);
             return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Xác nhận ghi xe có biển số " + bienSo + " ra khỏi bãi?", "Xác Nhận Xe Ra", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        String sql = "UPDATE QUANLYRAVAO SET THOIGIANRA = CURRENT_TIMESTAMP, TRANGTHAI = 'DA LAY' WHERE MAQUANLYRAVAO = ? AND TRANGTHAI = 'DANG GUI'";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, maQLRV);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                JOptionPane.showMessageDialog(this, "Ghi nhận xe ra bãi thành công!", "Thành Công", JOptionPane.INFORMATION_MESSAGE);
                // After successful exit, clear search and call searchVehicle() to show all records,
                // including the one that just exited with its updated status.
                txtTimKiemBienSo.setText(""); 
                searchVehicle(); 
            } else {
                JOptionPane.showMessageDialog(this, "Ghi nhận xe ra bãi thất bại (có thể xe đã được xử lý).", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi cập nhật xe ra bãi: " + e.getMessage(), "Lỗi Database", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    // Search vehicle - if bienSoSearch is empty, it will show ALL records (DANG GUI and DA LAY)
    private void searchVehicle() {
        String bienSoSearch = txtTimKiemBienSo.getText().trim().toUpperCase();
        
        tableModel.setRowCount(0);
        btnXeRa.setEnabled(false);

        // The SQL query with LIKE '%%' (if bienSoSearch is empty) will fetch all records.
        String sql = "SELECT MAQUANLYRAVAO, BIENSO, MAKH, THOIGIANVAO, THOIGIANRA, LOAIXE, TRANGTHAI FROM QUANLYRAVAO WHERE BIENSO LIKE ? ORDER BY THOIGIANVAO DESC";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + bienSoSearch + "%");
            ResultSet rs = pstmt.executeQuery();

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            boolean found = false;
            while (rs.next()) {
                found = true;
                Vector<Object> row = new Vector<>();
                row.add(rs.getString("MAQUANLYRAVAO"));
                row.add(rs.getString("BIENSO"));
                row.add(rs.getString("MAKH"));
                Timestamp thoiGianVao = rs.getTimestamp("THOIGIANVAO");
                row.add(thoiGianVao != null ? sdf.format(thoiGianVao) : "");
                Timestamp thoiGianRa = rs.getTimestamp("THOIGIANRA");
                row.add(thoiGianRa != null ? sdf.format(thoiGianRa) : "");
                row.add(rs.getString("LOAIXE"));
                row.add(rs.getString("TRANGTHAI"));
                tableModel.addRow(row);
            }
            if (!found && !bienSoSearch.isEmpty()) { // Only show "not found" if a specific search term was entered
                JOptionPane.showMessageDialog(this, "Không tìm thấy xe nào với biển số: " + bienSoSearch, "Không Tìm Thấy", JOptionPane.INFORMATION_MESSAGE);
            }
            // No need for special handling if bienSoSearch is empty, as the table will show all matching records (i.e., all records).
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi tìm kiếm xe: " + e.getMessage(), "Lỗi Database", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void clearInputFields() {
        txtBienSo.setText("");
        txtMaKH.setText("");
        comboLoaiXe.setSelectedIndex(0);
        txtBienSo.requestFocus();
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
