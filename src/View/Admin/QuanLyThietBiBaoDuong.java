package View.Admin;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;
import com.toedter.calendar.JDateChooser;
import ConnectDB.ConnectionOracle;

public class QuanLyThietBiBaoDuong extends javax.swing.JFrame {
    private DefaultTableModel tableModel;
    private JTable equipmentTable;
    private JTextField txtSearch;
    private JButton btnAdd, btnEdit, btnDelete, btnSearch, btnRefresh, btnBack;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
    // Tham chiếu đến AdminHomePage để quay lại
    private AdminHomePage adminHomePageInstance;

    /**
     * Constructor chính, nhận một instance của AdminHomePage để có thể quay lại.
     * @param adminHomePage Instance của AdminHomePage, hoặc null nếu không cần quay lại.
     */
    public QuanLyThietBiBaoDuong(AdminHomePage adminHomePage) {
        this.adminHomePageInstance = adminHomePage;
        setTitle("Quản Lý Thiết Bị");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1270, 800);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(new BorderLayout(10, 10));

        // Định nghĩa màu sắc và font chữ (có thể lấy từ lớp UIConstants của AdminHomePage nếu có)
        Font boldFont = new Font("Segoe UI", Font.BOLD, 14);
        Font plainFont = new Font("Segoe UI", Font.PLAIN, 14);
        Color bgColor = new Color(235, 245, 251);
        Color fgColorHeader = new Color(0, 120, 215);

        getContentPane().setBackground(bgColor);

        // Gọi phương thức khởi tạo các thành phần giao diện
        initComponentsUI(boldFont, plainFont, bgColor, fgColorHeader);
        loadEquipmentData();
    }

    /**
     * Constructor mặc định.
     * Được sử dụng khi chạy trực tiếp file này để test hoặc khi không cần truyền AdminHomePage.
     * Gọi constructor chính với tham số adminHomePageInstance là null.
     */
    public QuanLyThietBiBaoDuong() {
        this(null); // Quan trọng: Gọi constructor ThietBiBaoDuong(AdminHomePage adminHomePage)
    }

    /**
     * Khởi tạo các thành phần giao diện người dùng (UI components).
     * @param boldFont Font chữ đậm
     * @param plainFont Font chữ thường
     * @param bgColor Màu nền
     * @param fgColorHeader Màu chữ cho header của bảng
     */
    private void initComponentsUI(Font boldFont, Font plainFont, Color bgColor, Color fgColorHeader) {
        JPanel topPanel = new JPanel(new BorderLayout(10,0));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        topPanel.setBackground(bgColor);

        btnBack = new JButton("Quay Lại");
        btnBack.setFont(boldFont);
        btnBack.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBack.addActionListener(e -> {
            this.dispose();
            if (adminHomePageInstance != null) {
                adminHomePageInstance.setVisible(true);
            } else {
                 new AdminHomePage().setVisible(true);
            }
        });
        topPanel.add(btnBack, BorderLayout.WEST);

        JLabel titleLabel = new JLabel("DANH SÁCH THIẾT BỊ BẢO DƯỠNG", JLabel.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(fgColorHeader);
        topPanel.add(titleLabel, BorderLayout.CENTER);

        JPanel topRightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10,0));
        topRightPanel.setBackground(bgColor);

        txtSearch = new JTextField(20);
        txtSearch.setFont(plainFont);
        topRightPanel.add(new JLabel("Tìm kiếm:"));
        topRightPanel.add(txtSearch);

        btnSearch = new JButton("Tìm");
        btnSearch.setFont(boldFont);
        btnSearch.addActionListener(e -> searchEquipment());
        topRightPanel.add(btnSearch);

        btnAdd = new JButton("Thêm Mới");
        btnAdd.setFont(boldFont);
        btnAdd.addActionListener(e -> openAddEditDialog(null));
        topRightPanel.add(btnAdd);

        topPanel.add(topRightPanel, BorderLayout.EAST);

        tableModel = new DefaultTableModel(new String[]{"Mã TB", "Tên Thiết Bị", "Giá Nhập", "Ngày Nhập", "Hạn Sử Dụng"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        equipmentTable = new JTable(tableModel);
        equipmentTable.setFont(plainFont);
        equipmentTable.setRowHeight(25);
        equipmentTable.getTableHeader().setFont(boldFont);
        equipmentTable.getTableHeader().setBackground(fgColorHeader);
        equipmentTable.getTableHeader().setForeground(Color.WHITE);
        equipmentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        equipmentTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int selectedRow = equipmentTable.getSelectedRow();
                    if (selectedRow != -1) {
                        String maTB = (String) tableModel.getValueAt(equipmentTable.convertRowIndexToModel(selectedRow), 0);
                        openAddEditDialog(maTB);
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(equipmentTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(0,10,0,10));

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(0,10,10,10));
        bottomPanel.setBackground(bgColor);

        btnEdit = new JButton("Sửa");
        btnEdit.setFont(boldFont);
        btnEdit.addActionListener(e -> {
            int selectedRow = equipmentTable.getSelectedRow();
            if (selectedRow != -1) {
                String maTB = (String) tableModel.getValueAt(equipmentTable.convertRowIndexToModel(selectedRow), 0);
                openAddEditDialog(maTB);
            } else {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn một thiết bị để sửa.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            }
        });

        btnDelete = new JButton("Xóa");
        btnDelete.setFont(boldFont);
        btnDelete.addActionListener(e -> deleteEquipment());

        btnRefresh = new JButton("Làm Mới");
        btnRefresh.setFont(boldFont);
        btnRefresh.addActionListener(e -> loadEquipmentData());

        bottomPanel.add(btnEdit);
        bottomPanel.add(btnDelete);
        bottomPanel.add(btnRefresh);

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void loadEquipmentData() {
        tableModel.setRowCount(0);
        String sql = "SELECT t.MATHIETBI, t.TENTHIETBI, t.GIANHAP, " +
                     "t.NGAYNHAP, t.HANSUDUNG " +
                     "FROM THIETBI t " +
                     "ORDER BY t.MATHIETBI ASC";
        try (Connection conn = ConnectionOracle.getOracleConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getString("MATHIETBI"));
                row.add(rs.getString("TENTHIETBI"));
                row.add(rs.getDouble("GIANHAP"));

                Date ngayNhap = rs.getDate("NGAYNHAP");
                row.add(ngayNhap != null ? dateFormat.format(ngayNhap) : null);
                Date hanSD = rs.getDate("HANSUDUNG");
                row.add(hanSD != null ? dateFormat.format(hanSD) : null);

                tableModel.addRow(row);
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi tải dữ liệu thiết bị: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void searchEquipment() {
        String searchText = txtSearch.getText().trim();
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        equipmentTable.setRowSorter(sorter);
        if (searchText.length() == 0) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + searchText));
        }
    }

    private void openAddEditDialog(String maThietBi) {
        EquipmentDialogVerySimplified dialog = new EquipmentDialogVerySimplified(this, maThietBi);
        dialog.setVisible(true);
        if (dialog.isDataChanged()) {
            loadEquipmentData();
        }
    }

    private void deleteEquipment() {
        int selectedRow = equipmentTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một thiết bị để xóa.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String maTB = (String) tableModel.getValueAt(equipmentTable.convertRowIndexToModel(selectedRow), 0);
        String tenTB = (String) tableModel.getValueAt(equipmentTable.convertRowIndexToModel(selectedRow), 1);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn xóa thiết bị '" + tenTB + "' (Mã: " + maTB + ") không?",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            String sql = "DELETE FROM THIETBI WHERE MATHIETBI = ?";
            try (Connection conn = ConnectionOracle.getOracleConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, maTB);
                int affectedRows = pstmt.executeUpdate();
                if (affectedRows > 0) {
                    JOptionPane.showMessageDialog(this, "Xóa thiết bị thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    loadEquipmentData();
                } else {
                    JOptionPane.showMessageDialog(this, "Xóa thiết bị thất bại. Không tìm thấy thiết bị.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException | ClassNotFoundException e) {
                e.printStackTrace();
                String userMessage = "Lỗi khi xóa thiết bị: " + e.getMessage();
                // The specific integrity constraint for child record might relate to other tables if THIETBI is a parent.
                // This generic message is kept. If MANHANVIEN was the only FK, then it would be less likely.
                if (e.getMessage().toLowerCase().contains("integrity constraint") && e.getMessage().toLowerCase().contains("child record found")) {
                    userMessage = "Không thể xóa thiết bị này vì nó đang được tham chiếu trong các bản ghi khác.";
                }
                JOptionPane.showMessageDialog(this, userMessage, "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    
    
    
    // Lớp Dialog để Thêm/Sửa Thiết Bị (Rất đơn giản hóa theo CSDL mới nhất)
class EquipmentDialogVerySimplified extends JDialog {
    private JTextField txtMaTB, txtTenTB, txtGiaNhap;
    // private JTextField txtMaNhanVienPhuTrach; // Removed
    private JDateChooser dateNgayNhap, dateHSD;
    private JButton btnSave, btnCancel;

    private String currentMaTB;
    private boolean dataChanged = false;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

    public EquipmentDialogVerySimplified(Frame owner, String maThietBi) {
        super(owner, true);
        this.currentMaTB = maThietBi;
        setTitle(maThietBi == null ? "Thêm Thiết Bị Mới" : "Cập Nhật Thông Tin Thiết Bị");
        setSize(450, 270); // Adjusted size slightly as one field is removed
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10,10));
        ((JPanel)getContentPane()).setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        initComponentsDialogUI();

        if (maThietBi != null) {
            loadEquipmentDetails(maThietBi);
            txtMaTB.setEditable(false);
        } else {
            txtMaTB.setText(generateNewMaTB());
        }
    }

    public boolean isDataChanged() {
        return dataChanged;
    }

    private String generateNewMaTB() {
        String newId = "TB001";
        String sql = "SELECT MAX(SUBSTR(MATHIETBI, 3)) FROM THIETBI WHERE MATHIETBI LIKE 'TB%'";
        try (Connection conn = ConnectionOracle.getOracleConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                String lastNumStr = rs.getString(1);
                if (lastNumStr != null) {
                    int num = Integer.parseInt(lastNumStr);
                    newId = String.format("TB%03d", num + 1);
                }
            }
        } catch (SQLException | NumberFormatException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return newId;
    }

    private void initComponentsDialogUI() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        int y_gbc = 0;

        gbc.gridx = 0; gbc.gridy = y_gbc; formPanel.add(new JLabel("Mã Thiết Bị (*):"), gbc);
        gbc.gridx = 1; gbc.gridy = y_gbc++; txtMaTB = new JTextField(15); formPanel.add(txtMaTB, gbc);

        gbc.gridx = 0; gbc.gridy = y_gbc; formPanel.add(new JLabel("Tên Thiết Bị (*):"), gbc);
        gbc.gridx = 1; gbc.gridy = y_gbc++; txtTenTB = new JTextField(15); formPanel.add(txtTenTB, gbc);

        gbc.gridx = 0; gbc.gridy = y_gbc; formPanel.add(new JLabel("Giá Nhập:"), gbc);
        gbc.gridx = 1; gbc.gridy = y_gbc++; txtGiaNhap = new JTextField(15); formPanel.add(txtGiaNhap, gbc);

        gbc.gridx = 0; gbc.gridy = y_gbc; formPanel.add(new JLabel("Ngày Nhập (*):"), gbc);
        gbc.gridx = 1; gbc.gridy = y_gbc++; dateNgayNhap = new JDateChooser(); dateNgayNhap.setDateFormatString("dd/MM/yyyy"); formPanel.add(dateNgayNhap, gbc);

        gbc.gridx = 0; gbc.gridy = y_gbc; formPanel.add(new JLabel("Hạn Sử Dụng:"), gbc);
        gbc.gridx = 1; gbc.gridy = y_gbc++; dateHSD = new JDateChooser(); dateHSD.setDateFormatString("dd/MM/yyyy"); formPanel.add(dateHSD, gbc);

        // Removed Mã NV Phụ Trách field

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnSave = new JButton("Lưu");
        btnSave.addActionListener(e -> saveEquipment());
        btnCancel = new JButton("Hủy");
        btnCancel.addActionListener(e -> dispose());
        buttonPanel.add(btnSave);
        buttonPanel.add(btnCancel);

        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }


    private void loadEquipmentDetails(String maThietBi) {
        String sql = "SELECT MATHIETBI, TENTHIETBI, GIANHAP, " +
                     "NGAYNHAP, HANSUDUNG " + // Removed MANHANVIEN
                     "FROM THIETBI WHERE MATHIETBI = ?";
        try (Connection conn = ConnectionOracle.getOracleConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, maThietBi);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                txtMaTB.setText(rs.getString("MATHIETBI"));
                txtTenTB.setText(rs.getString("TENTHIETBI"));
                txtGiaNhap.setText(String.valueOf(rs.getDouble("GIANHAP")));
                dateNgayNhap.setDate(rs.getDate("NGAYNHAP"));
                dateHSD.setDate(rs.getDate("HANSUDUNG"));
                // txtMaNhanVienPhuTrach.setText(rs.getString("MANHANVIEN")); // Removed
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi tải chi tiết thiết bị: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveEquipment() {
        String maTB = txtMaTB.getText().trim();
        String tenTB = txtTenTB.getText().trim();
        java.util.Date ngayNhapUtil = dateNgayNhap.getDate();

        if (maTB.isEmpty() || tenTB.isEmpty() || ngayNhapUtil == null) {
            JOptionPane.showMessageDialog(this, "Mã thiết bị, Tên thiết bị và Ngày nhập là bắt buộc.", "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double giaNhap;
        try {
            giaNhap = txtGiaNhap.getText().trim().isEmpty() ? 0.0 : Double.parseDouble(txtGiaNhap.getText().trim());
            if (giaNhap < 0) {
                JOptionPane.showMessageDialog(this, "Giá nhập không thể âm.", "Lỗi dữ liệu", JOptionPane.WARNING_MESSAGE);
                return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Giá nhập không hợp lệ.", "Lỗi dữ liệu", JOptionPane.WARNING_MESSAGE);
            return;
        }

        java.util.Date hsdUtil = dateHSD.getDate();
        if (hsdUtil != null && hsdUtil.before(ngayNhapUtil)) {
            JOptionPane.showMessageDialog(this, "Hạn sử dụng không thể trước Ngày nhập.", "Lỗi dữ liệu", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // String maNhanVienPT = txtMaNhanVienPhuTrach.getText().trim(); // Removed
        // if (maNhanVienPT.isEmpty()) { // Removed
        // maNhanVienPT = null; // Removed
        // } // Removed


        String sql;
        boolean isUpdate = (currentMaTB != null);
        if (isUpdate) {
            sql = "UPDATE THIETBI SET TENTHIETBI=?, GIANHAP=?, NGAYNHAP=?, HANSUDUNG=? " + // Removed MANHANVIEN=?
                  "WHERE MATHIETBI=?";
        } else {
            sql = "INSERT INTO THIETBI (MATHIETBI, TENTHIETBI, GIANHAP, NGAYNHAP, HANSUDUNG) " + // Removed MANHANVIEN
                  "VALUES (?, ?, ?, ?, ?)"; // Removed one ?
        }

        try (Connection conn = ConnectionOracle.getOracleConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            int paramIndex = 1;
            if (isUpdate) {
                pstmt.setString(paramIndex++, tenTB);
            } else {
                pstmt.setString(paramIndex++, maTB);
                pstmt.setString(paramIndex++, tenTB);
            }

            pstmt.setDouble(paramIndex++, giaNhap);
            pstmt.setDate(paramIndex++, new java.sql.Date(ngayNhapUtil.getTime()));
            pstmt.setDate(paramIndex++, hsdUtil != null ? new java.sql.Date(hsdUtil.getTime()) : null);

            // if (maNhanVienPT == null) { // Removed
            // pstmt.setNull(paramIndex++, Types.VARCHAR); // Removed
            // } else { // Removed
            // pstmt.setString(paramIndex++, maNhanVienPT); // Removed
            // } // Removed


            if (isUpdate) {
                pstmt.setString(paramIndex, currentMaTB);
            }

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                JOptionPane.showMessageDialog(this, (isUpdate ? "Cập nhật" : "Thêm mới") + " thiết bị thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                dataChanged = true;
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, (isUpdate ? "Cập nhật" : "Thêm mới") + " thiết bị thất bại.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }

        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            String errorMessage = "Lỗi khi lưu thiết bị: " + e.getMessage();
            if (e.getMessage().contains("ORA-00001")) {
                 errorMessage = "Lỗi: Mã thiết bị đã tồn tại.";
            }
            // Removed ORA-02291 check for MANHANVIEN
            JOptionPane.showMessageDialog(this, errorMessage, "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
        }
    }
}

    class ComboBoxItem {
    private String id;
    private String value;

    public ComboBoxItem(String id, String value) {
        this.id = id;
        this.value = value;
    }

    public String getId() { return id; }
    public String getValue() { return value; }

    @Override
    public String toString() { return value; }
}
    
    
    
    
    
    public static void main(String[] args) {
        try {
            // Giả sử bạn dùng FlatLaf, nếu không thì có thể bỏ qua phần này
            UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatLightLaf());
        } catch (UnsupportedLookAndFeelException ex) {
            System.err.println("Failed to initialize LaF: " + ex.getMessage());
        }
        SwingUtilities.invokeLater(() -> new QuanLyThietBiBaoDuong().setVisible(true));
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


