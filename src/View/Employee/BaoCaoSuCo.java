package View.Employee;

import ConnectDB.ConnectionOracle; // Đảm bảo bạn có class này để kết nối CSDL
import com.formdev.flatlaf.FlatLightLaf;
import com.toedter.calendar.JDateChooser; // Thêm import cho JDateChooser

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Vector;

public class BaoCaoSuCo extends javax.swing.JFrame {
    private static class UIConstants {
        public static final Color BACKGROUND_COLOR = new Color(240, 245, 250);
        public static final Color HEADER_BACKGROUND = new Color(0, 120, 215); // Blue header
        public static final Color BUTTON_COLOR = new Color(0, 120, 215);
        public static final Color BUTTON_HOVER_COLOR = new Color(0, 100, 190);
        public static final Color FONT_COLOR_WHITE = Color.WHITE;
        public static final Color FONT_COLOR_DARK = new Color(50, 50, 50);
        public static final Font BOLD_FONT = new Font("Segoe UI", Font.BOLD, 14);
        public static final Font PLAIN_FONT = new Font("Segoe UI", Font.PLAIN, 14);
        public static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 20);
        public static final Font TABLE_HEADER_FONT = new Font("Segoe UI", Font.BOLD, 13);
        public static final String ICON_PATH_PREFIX = "/icons/";
        public static final String BACK_ICON = ICON_PATH_PREFIX + "back_arrow.png";
    }

    private JTextField txtMaBCSCCurrent;
    private JTextField txtMaNhanVien;
    private JDateChooser chooserNgayBaoCao;
    private JTextArea txtNoiDung;
    private JComboBox<String> cmbTinhTrang;
    private JButton btnThemForm; // Sẽ đổi tên thành "Làm Mới"
    private JButton btnLuu;       // Sẽ đổi tên thành "Tạo Báo Cáo" / "Cập Nhật Báo Cáo"
    private JButton btnBack;

    private JTextField txtSearchMaBCSC;
    private JComboBox<String> cmbSearchTinhTrang;
    private JButton btnTimKiem;
    private JButton btnTaiLaiDanhSach;
    private JTable tblSuCo;
    private DefaultTableModel tblModelSuCo;

    private String selectedMaBCSCForUpdate = null;
    private String currentEmployeeId; // Biến lưu mã nhân viên được truyền vào

    public BaoCaoSuCo(String employeeId) {
        this.currentEmployeeId = employeeId;

        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (UnsupportedLookAndFeelException ex) {
            System.err.println("Failed to initialize LaF: " + ex.getMessage());
        }

        setTitle("Báo Cáo Sự Cố");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1000, 720);
        setLocationRelativeTo(null);
        getContentPane().setBackground(UIConstants.BACKGROUND_COLOR);
        setLayout(new BorderLayout(10, 10));

        initComponentsUI();
        loadSuCoData();
        resetFormToNewMode(); // Đảm bảo form ở trạng thái "tạo mới" ban đầu
    }

    public BaoCaoSuCo() {
        this(null);
        if (this.currentEmployeeId == null) {
             JOptionPane.showMessageDialog(this,
                                           "Không thể khởi tạo form do thiếu thông tin nhân viên.",
                                           "Lỗi Khởi Tạo",
                                           JOptionPane.ERROR_MESSAGE);
            // SwingUtilities.invokeLater(this::dispose); // Consider closing if critical
        }
    }


    private void initComponentsUI() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UIConstants.HEADER_BACKGROUND);
        headerPanel.setBorder(new EmptyBorder(10, 15, 10, 15));

        JLabel lblTitle = new JLabel("Báo Cáo Sự Cố");
        lblTitle.setFont(UIConstants.TITLE_FONT);
        lblTitle.setForeground(UIConstants.FONT_COLOR_WHITE);
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
        headerPanel.add(lblTitle, BorderLayout.CENTER);

        btnBack = createStyledButton("", UIConstants.BACK_ICON, 24, 24);
        btnBack.setToolTipText("Quay lại trang chủ");
        btnBack.addActionListener(e -> {
             new EmployeeHomePage().setVisible(true); // Giả sử có EmployeeHomePage
            this.dispose();
        });
        JPanel backButtonContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        backButtonContainer.setBackground(UIConstants.HEADER_BACKGROUND);
        backButtonContainer.add(btnBack);
        headerPanel.add(backButtonContainer, BorderLayout.WEST);


        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setResizeWeight(0.45);
        splitPane.setBorder(null);
        splitPane.setBackground(UIConstants.BACKGROUND_COLOR);

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Thông Tin Sự Cố", 0, 0, UIConstants.BOLD_FONT, UIConstants.FONT_COLOR_DARK),
                new EmptyBorder(10, 10, 10, 10)
        ));
        formPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 5, 3, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Mã BCSC
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(createStyledLabel("Mã BCSC:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0;
        txtMaBCSCCurrent = new JTextField();
        txtMaBCSCCurrent.setEditable(false);
        txtMaBCSCCurrent.setFont(UIConstants.PLAIN_FONT);
        formPanel.add(txtMaBCSCCurrent, gbc);

        // Mã Nhân Viên
        gbc.gridx = 0; gbc.gridy = 1; gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(createStyledLabel("Mã Nhân Viên:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1;
        txtMaNhanVien = new JTextField();
        txtMaNhanVien.setFont(UIConstants.PLAIN_FONT);

        if (this.currentEmployeeId != null && !this.currentEmployeeId.isEmpty()) {
            txtMaNhanVien.setText(this.currentEmployeeId);
            txtMaNhanVien.setEditable(false);
            txtMaNhanVien.setBackground(new Color(230, 230, 230));
            txtMaNhanVien.setForeground(Color.DARK_GRAY);
        } else {
            txtMaNhanVien.setText("[Không có ID Nhân Viên]");
            txtMaNhanVien.setEditable(false);
            txtMaNhanVien.setBackground(new Color(240, 210, 210));
            txtMaNhanVien.setForeground(Color.RED);
        }
        formPanel.add(txtMaNhanVien, gbc);


        // Ngày Báo Cáo
        gbc.gridx = 0; gbc.gridy = 2; gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(createStyledLabel("Ngày Báo Cáo:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2;
        chooserNgayBaoCao = new JDateChooser();
        chooserNgayBaoCao.setDateFormatString("dd/MM/yyyy");
        chooserNgayBaoCao.setFont(UIConstants.PLAIN_FONT);
        formPanel.add(chooserNgayBaoCao, gbc);


        // Nội Dung
        gbc.gridx = 0; gbc.gridy = 3; gbc.anchor = GridBagConstraints.NORTHWEST;
        formPanel.add(createStyledLabel("Nội Dung:"), gbc);
        gbc.gridx = 1; gbc.gridy = 3; gbc.gridheight = 2; gbc.fill = GridBagConstraints.BOTH; gbc.weighty = 1.0;
        txtNoiDung = new JTextArea(4, 20);
        txtNoiDung.setFont(UIConstants.PLAIN_FONT);
        txtNoiDung.setLineWrap(true);
        txtNoiDung.setWrapStyleWord(true);
        JScrollPane scrollNoiDung = new JScrollPane(txtNoiDung);
        formPanel.add(scrollNoiDung, gbc);

        // Tình Trạng
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridheight = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.anchor = GridBagConstraints.WEST; gbc.weighty = 0;
        formPanel.add(createStyledLabel("Tình Trạng:"), gbc);
        gbc.gridx = 1; gbc.gridy = 5;
        cmbTinhTrang = new JComboBox<>(new String[]{"CHUA XU LY", "DANG XU LY", "DA XU LY"});
        cmbTinhTrang.setFont(UIConstants.PLAIN_FONT);
        formPanel.add(cmbTinhTrang, gbc);

        // Buttons Panel for Form
        JPanel formButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        formButtonPanel.setBackground(Color.WHITE);
        
        // Thay đổi tên nút "Thêm Mới" thành "Làm Mới"
        btnThemForm = createStyledButton("Làm Mới");
        btnThemForm.addActionListener(e -> resetFormToNewMode());
        
        // Tên nút "Lưu Báo Cáo" sẽ được đặt trong resetFormToNewMode hoặc populateForm
        btnLuu = createStyledButton("Tạo Báo Cáo"); // Tên mặc định khi khởi tạo
        btnLuu.addActionListener(e -> saveOrUpdateSuCo());

        formButtonPanel.add(btnThemForm);
        formButtonPanel.add(btnLuu);
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(formButtonPanel, gbc);

        splitPane.setTopComponent(formPanel);

        JPanel listPanel = new JPanel(new BorderLayout(0, 10));
        listPanel.setBorder(BorderFactory.createCompoundBorder(
             BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Danh Sách Sự Cố", 0, 0, UIConstants.BOLD_FONT, UIConstants.FONT_COLOR_DARK),
             new EmptyBorder(10, 10, 10, 10)
        ));
        listPanel.setBackground(Color.WHITE);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        filterPanel.setBackground(Color.WHITE);
        filterPanel.add(createStyledLabel("Tìm Mã BCSC:"));
        txtSearchMaBCSC = new JTextField(10);
        txtSearchMaBCSC.setFont(UIConstants.PLAIN_FONT);
        filterPanel.add(txtSearchMaBCSC);

        filterPanel.add(createStyledLabel("Tìm Tình Trạng:"));
        cmbSearchTinhTrang = new JComboBox<>(new String[]{"Tất cả", "CHUA XU LY", "DANG XU LY", "DA XU LY"});
        cmbSearchTinhTrang.setFont(UIConstants.PLAIN_FONT);
        filterPanel.add(cmbSearchTinhTrang);

        btnTimKiem = createStyledButton("Tìm Kiếm");
        btnTimKiem.addActionListener(e -> searchSuCo());
        filterPanel.add(btnTimKiem);

        btnTaiLaiDanhSach = createStyledButton("Tải Lại");
        btnTaiLaiDanhSach.addActionListener(e -> loadSuCoData());
        filterPanel.add(btnTaiLaiDanhSach);

        listPanel.add(filterPanel, BorderLayout.NORTH);

        tblModelSuCo = new DefaultTableModel(new String[]{"Mã BCSC", "Ngày Báo Cáo", "Mã NV", "Nội Dung", "Tình Trạng"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblSuCo = new JTable(tblModelSuCo);
        tblSuCo.setFont(UIConstants.PLAIN_FONT);
        tblSuCo.getTableHeader().setFont(UIConstants.TABLE_HEADER_FONT);
        tblSuCo.setRowHeight(25);
        tblSuCo.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblSuCo.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) {
                    int selectedRow = tblSuCo.getSelectedRow();
                    if (selectedRow != -1) {
                        populateFormFromSelectedTableRow(selectedRow);
                    }
                }
            }
        });
        JScrollPane scrollTable = new JScrollPane(tblSuCo);
        listPanel.add(scrollTable, BorderLayout.CENTER);

        splitPane.setBottomComponent(listPanel);

        add(headerPanel, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);
    }

    private JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(UIConstants.BOLD_FONT);
        label.setForeground(UIConstants.FONT_COLOR_DARK);
        return label;
    }
    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(UIConstants.BOLD_FONT);
        button.setBackground(UIConstants.BUTTON_COLOR);
        button.setForeground(UIConstants.FONT_COLOR_WHITE);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(8, 15, 8, 15));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                button.setBackground(UIConstants.BUTTON_HOVER_COLOR);
            }
            public void mouseExited(MouseEvent evt) {
                button.setBackground(UIConstants.BUTTON_COLOR);
            }
        });
        return button;
    }
     private JButton createStyledButton(String text, String iconPath, int iconWidth, int iconHeight) {
        JButton button = new JButton(text);
        if (iconPath != null && !iconPath.isEmpty()) {
            try {
                URL imgUrl = getClass().getResource(iconPath);
                if (imgUrl != null) {
                    ImageIcon originalIcon = new ImageIcon(imgUrl);
                    Image scaledImage = originalIcon.getImage().getScaledInstance(iconWidth, iconHeight, Image.SCALE_SMOOTH);
                    button.setIcon(new ImageIcon(scaledImage));
                } else {
                     System.err.println("Không tìm thấy icon: " + iconPath);
                }
            } catch (Exception e) {
                System.err.println("Lỗi tải icon " + iconPath + ": " + e.getMessage());
            }
        }
        button.setFont(UIConstants.BOLD_FONT);
        if(text == null || text.isEmpty()){
             button.setBackground(UIConstants.HEADER_BACKGROUND);
             button.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
             button.setContentAreaFilled(false);
             button.setOpaque(false);
        } else {
            button.setBackground(UIConstants.BUTTON_COLOR);
            button.setForeground(UIConstants.FONT_COLOR_WHITE);
            button.setBorder(new EmptyBorder(8, 15, 8, 15));
        }
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                if(text == null || text.isEmpty()) {
                    // button.setBackground(UIConstants.HEADER_BACKGROUND.darker());
                } else {
                    button.setBackground(UIConstants.BUTTON_HOVER_COLOR);
                }
            }
            public void mouseExited(MouseEvent evt) {
                 if(text == null || text.isEmpty()) {
                    // button.setBackground(UIConstants.HEADER_BACKGROUND);
                 } else {
                     button.setBackground(UIConstants.BUTTON_COLOR);
                 }
            }
        });
        return button;
    }
    private String generateNewMaBCSC() throws SQLException {
        String prefix = "SC";
        long nextSequenceNumber = 1;

        String sqlQueryMax = "SELECT MAX(TO_NUMBER(SUBSTR(MABCSUCO, " + (prefix.length() + 1) + "))) " +
                             "FROM BAOCAOSUCO " +
                             "WHERE MABCSUCO LIKE '" + prefix + "%' " +
                             "AND REGEXP_LIKE(SUBSTR(MABCSUCO, " + (prefix.length() + 1) + "), '^[0-9]+$')";

        try (Connection conn = ConnectionOracle.getOracleConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sqlQueryMax)) {

            if (rs.next()) {
                long currentMax = rs.getLong(1);
                if (!rs.wasNull()) {
                    nextSequenceNumber = currentMax + 1;
                }
            }
        } catch (ClassNotFoundException e) {
            JOptionPane.showMessageDialog(this, "Lỗi: Không tìm thấy Oracle JDBC Driver. " + e.getMessage(), "Lỗi Driver", JOptionPane.ERROR_MESSAGE);
            throw new SQLException("Oracle JDBC Driver not found", e);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Lỗi khi truy vấn mã BCSC lớn nhất: " + ex.getMessage(), "Lỗi SQL", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
            throw ex;
        }

        String newId;
        boolean idExists;
        int attempts = 0;
        final int MAX_ATTEMPTS = 100;

        do {
            newId = String.format("%s%03d", prefix, nextSequenceNumber + attempts);
            if (newId.length() > 10) {
                JOptionPane.showMessageDialog(this,
                        "Mã BCSC tạo ra (" + newId + ") quá dài (tối đa 10 ký tự). Vui lòng kiểm tra logic tạo mã hoặc giới hạn cột.",
                        "Lỗi Phát Sinh Mã", JOptionPane.ERROR_MESSAGE);
                throw new SQLException("Mã BCSC tạo ra quá dài: " + newId);
            }

            try (Connection conn = ConnectionOracle.getOracleConnection();
                 PreparedStatement checkStmt = conn.prepareStatement("SELECT 1 FROM BAOCAOSUCO WHERE MABCSUCO = ?")) {
                checkStmt.setString(1, newId);
                try (ResultSet checkRs = checkStmt.executeQuery()) {
                    idExists = checkRs.next();
                }
            } catch (ClassNotFoundException e) {
                 JOptionPane.showMessageDialog(this, "Lỗi: Không tìm thấy Oracle JDBC Driver khi kiểm tra mã. " + e.getMessage(), "Lỗi Driver", JOptionPane.ERROR_MESSAGE);
                 throw new SQLException("Oracle JDBC Driver not found during ID uniqueness check", e);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Lỗi khi kiểm tra tính duy nhất của mã BCSC: " + ex.getMessage(), "Lỗi SQL", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
                throw ex;
            }

            if (idExists) {
                attempts++;
            }

        } while (idExists && attempts < MAX_ATTEMPTS);

        if (idExists) {
            JOptionPane.showMessageDialog(this, "Không thể tạo mã BCSC duy nhất sau " + attempts + " lần thử. Có thể có lỗi logic hoặc dữ liệu.", "Lỗi Phát Sinh Mã", JOptionPane.ERROR_MESSAGE);
            throw new SQLException("Không thể tạo mã BCSC duy nhất.");
        }

        System.out.println("Mã BCSC được tạo: " + newId);
        return newId;
    }


    private void resetFormToNewMode() {
        selectedMaBCSCForUpdate = null;
        txtMaBCSCCurrent.setText("[Tạo Mới]");

        if (this.currentEmployeeId != null && !this.currentEmployeeId.isEmpty()) {
            txtMaNhanVien.setText(this.currentEmployeeId);
            txtMaNhanVien.setEditable(false);
            txtMaNhanVien.setBackground(new Color(230, 230, 230));
            txtMaNhanVien.setForeground(Color.DARK_GRAY);
        } else {
            txtMaNhanVien.setText("[Không có ID Nhân Viên]");
            txtMaNhanVien.setEditable(false);
            txtMaNhanVien.setBackground(new Color(240, 210, 210));
            txtMaNhanVien.setForeground(Color.RED);
        }

        chooserNgayBaoCao.setDate(new java.util.Date());
        txtNoiDung.setText("");
        
        // Thiết lập Tình Trạng cho chế độ thêm mới
        cmbTinhTrang.setSelectedItem("CHUA XU LY");
        cmbTinhTrang.setEnabled(false); // Vô hiệu hóa ComboBox
        
        // Đổi tên nút Lưu thành "Tạo Báo Cáo"
        btnLuu.setText("Tạo Báo Cáo");
        tblSuCo.clearSelection();
    }

    private void populateFormFromSelectedTableRow(int rowIndex) {
        selectedMaBCSCForUpdate = tblModelSuCo.getValueAt(rowIndex, 0).toString();
        txtMaBCSCCurrent.setText(selectedMaBCSCForUpdate);

        String ngayBaoCaoStr = tblModelSuCo.getValueAt(rowIndex, 1).toString();
        try {
            if (!"N/A".equals(ngayBaoCaoStr)) {
                java.util.Date ngayBC = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse(ngayBaoCaoStr);
                chooserNgayBaoCao.setDate(ngayBC);
            } else {
                chooserNgayBaoCao.setDate(null);
            }
        } catch (ParseException e) {
            System.err.println("Lỗi parse ngày từ bảng: " + e.getMessage());
            chooserNgayBaoCao.setDate(null);
        }

        txtNoiDung.setText(tblModelSuCo.getValueAt(rowIndex, 3).toString());
        
        // Thiết lập Tình Trạng cho chế độ cập nhật
        cmbTinhTrang.setSelectedItem(tblModelSuCo.getValueAt(rowIndex, 4).toString());
        cmbTinhTrang.setEnabled(true); // Kích hoạt ComboBox
        
        // Đổi tên nút Lưu thành "Cập Nhật Báo Cáo"
        btnLuu.setText("Cập Nhật Báo Cáo");
    }

    private void saveOrUpdateSuCo() {
        String maNV = this.currentEmployeeId;
        String noiDung = txtNoiDung.getText().trim();
        String tinhTrang = cmbTinhTrang.getSelectedItem().toString(); // Sẽ là "CHUA XU LY" nếu cmb bị disable
        java.util.Date ngayBaoCaoUtil = chooserNgayBaoCao.getDate();

        if (maNV == null || maNV.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Không xác định được mã nhân viên. Không thể lưu báo cáo.", "Lỗi Dữ Liệu", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (ngayBaoCaoUtil == null) {
            JOptionPane.showMessageDialog(this, "Ngày báo cáo không được để trống.", "Lỗi Nhập Liệu", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (noiDung.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nội dung sự cố không được để trống.", "Lỗi Nhập Liệu", JOptionPane.WARNING_MESSAGE);
            return;
        }

        java.sql.Timestamp ngayBaoCaoSql = new java.sql.Timestamp(ngayBaoCaoUtil.getTime());

        try (Connection conn = ConnectionOracle.getOracleConnection()) {
            if (selectedMaBCSCForUpdate == null) { // Chế độ thêm mới
                String newMaBCSC = generateNewMaBCSC();
                if (newMaBCSC.length() > 10) {
                     JOptionPane.showMessageDialog(this, "Mã BCSC tạo ra quá dài: " + newMaBCSC, "Lỗi Phát Sinh Mã", JOptionPane.ERROR_MESSAGE);
                     return;
                }
                // Tình trạng mặc định là "CHUA XU LY" do cmbTinhTrang đã được set và disable
                String sql = "INSERT INTO BAOCAOSUCO (MABCSUCO, NOIDUNG, TINHTRANGXULY, MANHANVIEN, NGAYBAOCAO) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, newMaBCSC);
                    pstmt.setString(2, noiDung);
                    pstmt.setString(3, "CHUA XU LY"); // Luôn là CHUA XU LY khi thêm mới
                    pstmt.setString(4, maNV);
                    pstmt.setTimestamp(5, ngayBaoCaoSql);

                    int affectedRows = pstmt.executeUpdate();
                    if (affectedRows > 0) {
                        JOptionPane.showMessageDialog(this, "Tạo báo cáo sự cố thành công!", "Thông Báo", JOptionPane.INFORMATION_MESSAGE);
                        loadSuCoData();
                        resetFormToNewMode();
                    }
                }
            } else { // Chế độ cập nhật
                String sql = "UPDATE BAOCAOSUCO SET NOIDUNG = ?, TINHTRANGXULY = ?, MANHANVIEN = ?, NGAYBAOCAO = ? WHERE MABCSUCO = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, noiDung);
                    pstmt.setString(2, tinhTrang); // Lấy từ cmbTinhTrang (đã enable và cho phép chọn)
                    pstmt.setString(3, maNV);
                    pstmt.setTimestamp(4, ngayBaoCaoSql);
                    pstmt.setString(5, selectedMaBCSCForUpdate);

                    int affectedRows = pstmt.executeUpdate();
                    if (affectedRows > 0) {
                        JOptionPane.showMessageDialog(this, "Cập nhật báo cáo sự cố thành công!", "Thông Báo", JOptionPane.INFORMATION_MESSAGE);
                        loadSuCoData();
                        resetFormToNewMode(); // Trở về trạng thái tạo mới sau khi cập nhật
                    } else {
                         JOptionPane.showMessageDialog(this, "Không tìm thấy báo cáo để cập nhật hoặc không có gì thay đổi.", "Lỗi Cập Nhật", JOptionPane.WARNING_MESSAGE);
                    }
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Lỗi cơ sở dữ liệu: " + ex.getMessage(), "Lỗi SQL", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        } catch (ClassNotFoundException e) {
            JOptionPane.showMessageDialog(this, "Lỗi: Không tìm thấy Oracle JDBC Driver. " + e.getMessage(), "Lỗi Driver", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadSuCoData() {
        searchSuCoInternal(null, "Tất cả");
    }

    private void searchSuCo() {
        String searchMa = txtSearchMaBCSC.getText().trim();
        String searchTinhTrang = cmbSearchTinhTrang.getSelectedItem().toString();
        searchSuCoInternal(searchMa, searchTinhTrang);
    }

    private void searchSuCoInternal(String searchMa, String searchTinhTrang) {
        tblModelSuCo.setRowCount(0);
        SimpleDateFormat sdfOutput = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

        StringBuilder sqlBuilder = new StringBuilder("SELECT MABCSUCO, NGAYBAOCAO, MANHANVIEN, NOIDUNG, TINHTRANGXULY FROM BAOCAOSUCO WHERE 1=1");
        if (searchMa != null && !searchMa.isEmpty()) {
            sqlBuilder.append(" AND UPPER(MABCSUCO) LIKE UPPER(?)");
        }
        if (searchTinhTrang != null && !searchTinhTrang.equals("Tất cả")) {
            sqlBuilder.append(" AND TINHTRANGXULY = ?");
        }
        sqlBuilder.append(" ORDER BY NGAYBAOCAO DESC, MABCSUCO DESC");

        try (Connection conn = ConnectionOracle.getOracleConnection();
             PreparedStatement pstmt = conn.prepareStatement(sqlBuilder.toString())) {

            int paramIndex = 1;
            if (searchMa != null && !searchMa.isEmpty()) {
                pstmt.setString(paramIndex++, "%" + searchMa + "%");
            }
            if (searchTinhTrang != null && !searchTinhTrang.equals("Tất cả")) {
                pstmt.setString(paramIndex++, searchTinhTrang);
            }

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Vector<String> row = new Vector<>();
                row.add(rs.getString("MABCSUCO"));
                Timestamp ngayBaoCaoTimestamp = rs.getTimestamp("NGAYBAOCAO");
                row.add(ngayBaoCaoTimestamp != null ? sdfOutput.format(ngayBaoCaoTimestamp) : "N/A");
                row.add(rs.getString("MANHANVIEN"));
                row.add(rs.getString("NOIDUNG"));
                row.add(rs.getString("TINHTRANGXULY"));
                tblModelSuCo.addRow(row);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Lỗi tải dữ liệu sự cố: " + ex.getMessage(), "Lỗi SQL", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        } catch (ClassNotFoundException e) {
            JOptionPane.showMessageDialog(this, "Lỗi: Không tìm thấy Oracle JDBC Driver. " + e.getMessage(), "Lỗi Driver", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        System.setProperty("flatlaf.useVisualPadding", "false");
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (UnsupportedLookAndFeelException ex) {
            System.err.println("Failed to initialize LaF: " + ex.getMessage());
        }

        SwingUtilities.invokeLater(() -> {
            new BaoCaoSuCo("NV_TEST_MAIN").setVisible(true);
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
