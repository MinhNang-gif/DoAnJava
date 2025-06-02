package View.Admin;

import ConnectDB.ConnectionOracle; // Đảm bảo import này chính xác
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Vector;


public class QuanLySuCo extends javax.swing.JFrame {
    private JFrame parentFrame;

    private JTable incidentTable;
    private DefaultTableModel tableModel;

    private JTextField txtMaBCSuCo;
    private JTextArea txtNoiDung;
    private JTextField txtMaNhanVien;
    private JComboBox<String> comboTinhTrangXuLy;

    private JComboBox<Integer> comboNgay;
    private JComboBox<Integer> comboThang;
    private JComboBox<Integer> comboNam;
    private JPanel panelNgayBaoCao;

    private JTextField txtSearchMaSuCo;
    private JComboBox<String> comboSearchTinhTrang;

    // private JButton btnAdd; // Removed
    private JButton btnUpdate;
    private JButton btnSearch;
    private JButton btnClearSearch;
    private JButton btnClearForm;
    private JButton btnBack;

    private static class UIConstants {
        public static final Color BACKGROUND_COLOR = new Color(240, 245, 250); // Màu nền chung
        public static final Color PANEL_BACKGROUND = Color.WHITE; // Màu nền cho các panel
        public static final Color BUTTON_COLOR = new Color(0, 120, 215); // Màu nền nút
        public static final Color BUTTON_TEXT_COLOR = Color.WHITE; // Màu chữ nút
        public static final Color TABLE_HEADER_BG = new Color(0, 120, 215); // Màu nền tiêu đề bảng
        public static final Color TABLE_HEADER_FG = Color.WHITE; // Màu chữ tiêu đề bảng
        public static final Font BOLD_FONT = new Font("Segoe UI", Font.BOLD, 14); // Font chữ đậm
        public static final Font PLAIN_FONT = new Font("Segoe UI", Font.PLAIN, 13); // Font chữ thường
        public static final Font LABEL_FONT = new Font("Segoe UI", Font.BOLD, 13); // Font chữ cho nhãn
        public static final Font TEXT_FIELD_FONT = new Font("Segoe UI", Font.PLAIN, 13); // Font chữ cho trường nhập liệu
    }

    // Định dạng hiển thị Timestamp trong bảng
    private final SimpleDateFormat displayTimestampFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

    public QuanLySuCo(JFrame parent) {
        this.parentFrame = parent; // Lưu frame cha để quay lại
        setTitle("Quản Lý Sự Cố");
        setSize(1000, 720);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Đóng frame này không đóng toàn bộ ứng dụng
        setLocationRelativeTo(null); // Căn giữa màn hình
        getContentPane().setBackground(UIConstants.BACKGROUND_COLOR);
        setLayout(new BorderLayout(10, 10)); // Layout chính với khoảng cách
        ((JPanel) getContentPane()).setBorder(new EmptyBorder(10, 10, 10, 10)); // Thêm padding cho frame

        initComponentsUI(); // Khởi tạo các thành phần giao diện
        addListeners();     // Thêm các trình xử lý sự kiện
        loadIncidents(null, "Tất cả"); // Tải dữ liệu ban đầu, hiển thị tất cả sự cố
        clearForm();        // Xóa trắng form sau khi tải
    }

    // Khởi tạo và bố trí các thành phần giao diện
    private void initComponentsUI() {
        // Panel tìm kiếm
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        searchPanel.setBackground(UIConstants.PANEL_BACKGROUND);
        searchPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), "Tìm Kiếm Sự Cố",
            TitledBorder.LEFT, TitledBorder.TOP, UIConstants.BOLD_FONT, UIConstants.BUTTON_COLOR));
        searchPanel.add(new JLabel("Mã Sự Cố:")).setFont(UIConstants.LABEL_FONT);
        txtSearchMaSuCo = new JTextField(15);
        txtSearchMaSuCo.setFont(UIConstants.TEXT_FIELD_FONT);
        searchPanel.add(txtSearchMaSuCo);
        searchPanel.add(new JLabel("Tình Trạng:")).setFont(UIConstants.LABEL_FONT);
        String[] searchStatuses = {"Tất cả", "CHUA XU LY", "DANG XU LY", "DA XU LY"};
        comboSearchTinhTrang = new JComboBox<>(searchStatuses);
        comboSearchTinhTrang.setFont(UIConstants.TEXT_FIELD_FONT);
        searchPanel.add(comboSearchTinhTrang);
        btnSearch = createStyledButton("Tìm Kiếm");
        btnClearSearch = createStyledButton("Làm Mới TK");
        searchPanel.add(btnSearch);
        searchPanel.add(btnClearSearch);
        add(searchPanel, BorderLayout.NORTH); // Thêm panel tìm kiếm vào phía Bắc

        // Bảng hiển thị sự cố
        tableModel = new DefaultTableModel(new String[]{"Mã BC Sự Cố", "Ngày Báo Cáo", "Nội Dung", "Tình Trạng Xử Lý", "Mã Nhân Viên"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Không cho phép sửa trực tiếp trên bảng
            }
        };
        incidentTable = new JTable(tableModel);
        incidentTable.setFont(UIConstants.PLAIN_FONT);
        incidentTable.setRowHeight(25);
        incidentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // Chỉ cho chọn 1 dòng
        JTableHeader tableHeader = incidentTable.getTableHeader();
        tableHeader.setFont(UIConstants.BOLD_FONT);
        tableHeader.setBackground(UIConstants.TABLE_HEADER_BG);
        tableHeader.setForeground(UIConstants.TABLE_HEADER_FG);
        tableHeader.setReorderingAllowed(false); // Không cho kéo thả cột
        JScrollPane scrollPane = new JScrollPane(incidentTable);
        scrollPane.getViewport().setBackground(UIConstants.PANEL_BACKGROUND);
        add(scrollPane, BorderLayout.CENTER); // Thêm bảng vào giữa

        // Panel form nhập liệu
        JPanel formPanelOuter = new JPanel(new BorderLayout(0, 10));
        formPanelOuter.setBackground(UIConstants.PANEL_BACKGROUND);
        formPanelOuter.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), "Thông Tin Sự Cố",
            TitledBorder.LEFT, TitledBorder.TOP, UIConstants.BOLD_FONT, UIConstants.BUTTON_COLOR));

        JPanel formFieldsPanel = new JPanel(new GridBagLayout()); // Sử dụng GridBagLayout để căn chỉnh linh hoạt
        formFieldsPanel.setBackground(UIConstants.PANEL_BACKGROUND);
        formFieldsPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); // Khoảng cách giữa các thành phần

        // Mã BC Sự Cố
        JLabel lblMaBCSuCo = new JLabel("Mã BC Sự Cố:");
        lblMaBCSuCo.setFont(UIConstants.LABEL_FONT);
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST; 
        gbc.weightx = 0.0; gbc.fill = GridBagConstraints.NONE; 
        formFieldsPanel.add(lblMaBCSuCo, gbc);
        txtMaBCSuCo = new JTextField();
        txtMaBCSuCo.setFont(UIConstants.TEXT_FIELD_FONT);
        txtMaBCSuCo.setEditable(false); // << MODIFIED: Not editable
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0; 
        gbc.fill = GridBagConstraints.HORIZONTAL; 
        formFieldsPanel.add(txtMaBCSuCo, gbc);

        // Ngày Báo Cáo
        JLabel lblNgayBaoCao = new JLabel("Ngày Báo Cáo:");
        lblNgayBaoCao.setFont(UIConstants.LABEL_FONT);
        gbc.gridx = 0; gbc.gridy = 1; gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 0.0; gbc.fill = GridBagConstraints.NONE;
        formFieldsPanel.add(lblNgayBaoCao, gbc);
        panelNgayBaoCao = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 0)); 
        panelNgayBaoCao.setBackground(UIConstants.PANEL_BACKGROUND);
        comboNgay = new JComboBox<>();
        comboThang = new JComboBox<>();
        comboNam = new JComboBox<>();
        comboNgay.setFont(UIConstants.TEXT_FIELD_FONT);
        comboThang.setFont(UIConstants.TEXT_FIELD_FONT);
        comboNam.setFont(UIConstants.TEXT_FIELD_FONT);
        Dimension dateComboDim = new Dimension(65, comboNgay.getPreferredSize().height); 
        comboNgay.setPreferredSize(dateComboDim);
        comboThang.setPreferredSize(dateComboDim);
        comboNam.setPreferredSize(new Dimension(85, comboNam.getPreferredSize().height)); 
        panelNgayBaoCao.add(new JLabel("Ng:")).setFont(UIConstants.PLAIN_FONT); panelNgayBaoCao.add(comboNgay);
        panelNgayBaoCao.add(new JLabel("Th:")).setFont(UIConstants.PLAIN_FONT); panelNgayBaoCao.add(comboThang);
        panelNgayBaoCao.add(new JLabel("Nm:")).setFont(UIConstants.PLAIN_FONT); panelNgayBaoCao.add(comboNam);
        comboNgay.setEnabled(false); // << MODIFIED: Disabled
        comboThang.setEnabled(false); // << MODIFIED: Disabled
        comboNam.setEnabled(false); // << MODIFIED: Disabled
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formFieldsPanel.add(panelNgayBaoCao, gbc);
        populateDateComboBoxes(); 

        // Mã Nhân Viên
        JLabel lblMaNhanVien = new JLabel("Mã Nhân Viên:");
        lblMaNhanVien.setFont(UIConstants.LABEL_FONT);
        gbc.gridx = 0; gbc.gridy = 2; gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 0.0; gbc.fill = GridBagConstraints.NONE;
        formFieldsPanel.add(lblMaNhanVien, gbc);
        txtMaNhanVien = new JTextField();
        txtMaNhanVien.setFont(UIConstants.TEXT_FIELD_FONT);
        txtMaNhanVien.setEditable(false); // << MODIFIED: Not editable
        gbc.gridx = 1; gbc.gridy = 2; gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formFieldsPanel.add(txtMaNhanVien, gbc);

        // Tình Trạng Xử Lý
        JLabel lblTinhTrang = new JLabel("Tình Trạng Xử Lý:");
        lblTinhTrang.setFont(UIConstants.LABEL_FONT);
        gbc.gridx = 0; gbc.gridy = 3; gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 0.0; gbc.fill = GridBagConstraints.NONE;
        formFieldsPanel.add(lblTinhTrang, gbc);
        String[] statuses = {"CHUA XU LY", "DANG XU LY", "DA XU LY"};
        comboTinhTrangXuLy = new JComboBox<>(statuses);
        comboTinhTrangXuLy.setFont(UIConstants.TEXT_FIELD_FONT);
        // comboTinhTrangXuLy remains enabled
        gbc.gridx = 1; gbc.gridy = 3; gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formFieldsPanel.add(comboTinhTrangXuLy, gbc);

        // Nội Dung
        JLabel lblNoiDung = new JLabel("Nội Dung:");
        lblNoiDung.setFont(UIConstants.LABEL_FONT);
        gbc.gridx = 0; gbc.gridy = 4; gbc.anchor = GridBagConstraints.NORTHWEST; 
        gbc.weightx = 0.0; gbc.fill = GridBagConstraints.NONE;
        formFieldsPanel.add(lblNoiDung, gbc);
        txtNoiDung = new JTextArea(4, 20); 
        txtNoiDung.setFont(UIConstants.TEXT_FIELD_FONT);
        txtNoiDung.setLineWrap(true); 
        txtNoiDung.setWrapStyleWord(true); 
        txtNoiDung.setEditable(false); // << MODIFIED: Not editable
        JScrollPane noiDungScrollPane = new JScrollPane(txtNoiDung); 
        gbc.gridx = 1; gbc.gridy = 4; gbc.weightx = 1.0; gbc.weighty = 1.0; 
        gbc.fill = GridBagConstraints.BOTH; 
        formFieldsPanel.add(noiDungScrollPane, gbc);

        formPanelOuter.add(formFieldsPanel, BorderLayout.CENTER);

        // Panel chứa các nút chức năng
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10)); 
        buttonsPanel.setBackground(UIConstants.PANEL_BACKGROUND);
        // btnAdd = createStyledButton("Thêm Mới"); // Removed
        btnUpdate = createStyledButton("Cập Nhật");
        btnClearForm = createStyledButton("Làm Mới Form");
        btnBack = createStyledButton("Quay Lại");
        // buttonsPanel.add(btnAdd); // Removed
        buttonsPanel.add(btnUpdate);
        buttonsPanel.add(btnClearForm);
        buttonsPanel.add(btnBack);
        formPanelOuter.add(buttonsPanel, BorderLayout.SOUTH); 
        
        add(formPanelOuter, BorderLayout.SOUTH); 
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(UIConstants.BOLD_FONT);
        button.setBackground(UIConstants.BUTTON_COLOR);
        button.setForeground(UIConstants.BUTTON_TEXT_COLOR);
        button.setFocusPainted(false); 
        button.setCursor(new Cursor(Cursor.HAND_CURSOR)); 
        return button;
    }

    private void populateDateComboBoxes() {
        Calendar cal = Calendar.getInstance(); 
        int currentYear = cal.get(Calendar.YEAR);
        int currentMonth = cal.get(Calendar.MONTH) + 1; 

        for (int i = currentYear - 10; i <= currentYear + 10; i++) {
            comboNam.addItem(i);
        }
        comboNam.setSelectedItem(currentYear); 

        for (int i = 1; i <= 12; i++) {
            comboThang.addItem(i);
        }
        comboThang.setSelectedItem(currentMonth); 

        updateDaysComboBox(); 

        ActionListener dateChangeListener = e -> updateDaysComboBox();
        comboThang.addActionListener(dateChangeListener);
        comboNam.addActionListener(dateChangeListener);
    }

    private void updateDaysComboBox() {
        Object yearObj = comboNam.getSelectedItem();
        Object monthObj = comboThang.getSelectedItem();
        if (yearObj == null || monthObj == null) return; 

        int year = (Integer) yearObj;
        int month = (Integer) monthObj;
        Object selectedDayObj = comboNgay.getSelectedItem(); 
        int selectedDay = (selectedDayObj != null) ? (Integer) selectedDayObj : 1;

        Calendar cal = Calendar.getInstance();
        cal.set(year, month - 1, 1); 
        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH); 

        ActionListener[] dayActionListeners = comboNgay.getActionListeners();
        for(ActionListener al : dayActionListeners) comboNgay.removeActionListener(al);

        comboNgay.removeAllItems(); 
        for (int i = 1; i <= daysInMonth; i++) {
            comboNgay.addItem(i); 
        }

        if (selectedDay <= daysInMonth) {
            comboNgay.setSelectedItem(selectedDay);
        } else {
            comboNgay.setSelectedItem(daysInMonth);
        }
        for(ActionListener al : dayActionListeners) comboNgay.addActionListener(al);
    }

    private void addListeners() {
        btnBack.addActionListener(e -> {
            if (parentFrame != null) parentFrame.setVisible(true); 
            this.dispose(); 
        });

        btnClearForm.addActionListener(e -> clearForm());

        btnSearch.addActionListener(e -> {
            String maSuCo = txtSearchMaSuCo.getText().trim();
            String tinhTrang = comboSearchTinhTrang.getSelectedItem() != null ? comboSearchTinhTrang.getSelectedItem().toString() : "Tất cả";
            loadIncidents(maSuCo.isEmpty() ? null : maSuCo, tinhTrang); 
        });

        btnClearSearch.addActionListener(e -> {
            txtSearchMaSuCo.setText("");
            comboSearchTinhTrang.setSelectedIndex(0); 
            loadIncidents(null, "Tất cả"); 
        });

        // btnAdd.addActionListener(e -> addIncident()); // Removed

        btnUpdate.addActionListener(e -> updateIncident());

        incidentTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) { 
                    int selectedRow = incidentTable.getSelectedRow();
                    if (selectedRow != -1) { 
                        txtMaBCSuCo.setText(tableModel.getValueAt(selectedRow, 0).toString());
                        // txtMaBCSuCo is already non-editable

                        Object ngayBaoCaoObj = tableModel.getValueAt(selectedRow, 1);
                        if (ngayBaoCaoObj != null && !ngayBaoCaoObj.toString().isEmpty()) {
                            try {
                                java.util.Date date = displayTimestampFormat.parse(ngayBaoCaoObj.toString());
                                Calendar cal = Calendar.getInstance();
                                cal.setTime(date);
                                comboNam.setSelectedItem(cal.get(Calendar.YEAR));
                                comboThang.setSelectedItem(cal.get(Calendar.MONTH) + 1);
                                updateDaysComboBox(); 
                                comboNgay.setSelectedItem(cal.get(Calendar.DAY_OF_MONTH));
                            } catch (java.text.ParseException ex) {
                                System.err.println("[QuanLySuCo] Lỗi parse ngày từ bảng: " + ex.getMessage());
                                Calendar cal = Calendar.getInstance();
                                comboNam.setSelectedItem(cal.get(Calendar.YEAR));
                                comboThang.setSelectedItem(cal.get(Calendar.MONTH) + 1);
                                updateDaysComboBox();
                                comboNgay.setSelectedItem(cal.get(Calendar.DAY_OF_MONTH));
                            }
                        }

                        txtNoiDung.setText(tableModel.getValueAt(selectedRow, 2).toString());
                        comboTinhTrangXuLy.setSelectedItem(tableModel.getValueAt(selectedRow, 3).toString());
                        Object maNVObj = tableModel.getValueAt(selectedRow, 4);
                        txtMaNhanVien.setText(maNVObj != null ? maNVObj.toString() : "");

                        // btnAdd.setEnabled(false); // Removed
                        btnUpdate.setEnabled(true); 
                    }
                }
            }
        });
    }

    private void clearForm() {
        txtMaBCSuCo.setText("");
        // txtMaBCSuCo.setEditable(true); // Removed - always non-editable
        txtNoiDung.setText("");
        txtMaNhanVien.setText("");

        Calendar cal = Calendar.getInstance();
        comboNam.setSelectedItem(cal.get(Calendar.YEAR));
        comboThang.setSelectedItem(cal.get(Calendar.MONTH) + 1);
        updateDaysComboBox(); 
        comboNgay.setSelectedItem(cal.get(Calendar.DAY_OF_MONTH));

        comboTinhTrangXuLy.setSelectedIndex(0); 
        incidentTable.clearSelection(); 

        // btnAdd.setEnabled(true); // Removed
        btnUpdate.setEnabled(false); 
    }

    private void refreshTable() {
        String maSuCo = txtSearchMaSuCo.getText().trim();
        String tinhTrang = comboSearchTinhTrang.getSelectedItem() != null ? comboSearchTinhTrang.getSelectedItem().toString() : "Tất cả";
        loadIncidents(maSuCo.isEmpty() ? null : maSuCo, tinhTrang);
    }

    private void loadIncidents(String searchMaSuCo, String searchTinhTrang) {
        System.out.println("[QuanLySuCo] Bắt đầu loadIncidents. Tìm kiếm Mã: '" + searchMaSuCo + "', Tình trạng: '" + searchTinhTrang + "'");
        tableModel.setRowCount(0); 

        String sql = "SELECT MABCSUCO, NGAYBAOCAO, NOIDUNG, TINHTRANGXULY, MANHANVIEN FROM BAOCAOSUCO";
        boolean hasWhere = false;
        if (searchMaSuCo != null && !searchMaSuCo.isEmpty()) {
            sql += " WHERE LOWER(MABCSUCO) LIKE LOWER(?)"; 
            hasWhere = true;
        }
        if (searchTinhTrang != null && !searchTinhTrang.equalsIgnoreCase("Tất cả")) {
            sql += (hasWhere ? " AND" : " WHERE") + " TINHTRANGXULY = ?";
        }
        sql += " ORDER BY NGAYBAOCAO DESC"; 
        System.out.println("[QuanLySuCo] Câu lệnh SQL: " + sql);

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = ConnectionOracle.getOracleConnection(); 
            System.out.println("[QuanLySuCo] Đã kết nối CSDL thành công.");
            pstmt = conn.prepareStatement(sql);

            int paramIndex = 1;
            if (searchMaSuCo != null && !searchMaSuCo.isEmpty()) {
                pstmt.setString(paramIndex++, "%" + searchMaSuCo + "%");
                System.out.println("[QuanLySuCo] Đặt tham số tìm kiếm MABCSUCO: %" + searchMaSuCo + "%");
            }
            if (searchTinhTrang != null && !searchTinhTrang.equalsIgnoreCase("Tất cả")) {
                pstmt.setString(paramIndex++, searchTinhTrang);
                System.out.println("[QuanLySuCo] Đặt tham số tìm kiếm TINHTRANGXULY: " + searchTinhTrang);
            }

            System.out.println("[QuanLySuCo] Đang thực thi truy vấn...");
            rs = pstmt.executeQuery();
            System.out.println("[QuanLySuCo] Đã thực thi truy vấn.");

            int rowCount = 0;
            while (rs.next()) { 
                rowCount++;
                Vector<Object> row = new Vector<>();
                row.add(rs.getString("MABCSUCO"));
                Timestamp ngayBaoCaoTs = rs.getTimestamp("NGAYBAOCAO");
                row.add(ngayBaoCaoTs != null ? displayTimestampFormat.format(ngayBaoCaoTs) : ""); 
                row.add(rs.getString("NOIDUNG"));
                row.add(rs.getString("TINHTRANGXULY"));
                row.add(rs.getString("MANHANVIEN"));
                tableModel.addRow(row); 
            }
            System.out.println("[QuanLySuCo] Đã tải " + rowCount + " dòng vào bảng.");

            if (rowCount == 0) {
                if ((searchMaSuCo == null || searchMaSuCo.isEmpty()) && (searchTinhTrang == null || searchTinhTrang.equalsIgnoreCase("Tất cả"))) {
                    System.out.println("[QuanLySuCo] Không có dữ liệu sự cố nào trong cơ sở dữ liệu hoặc phù hợp với điều kiện mặc định.");
                } else {
                     System.out.println("[QuanLySuCo] Tìm kiếm không trả về kết quả nào.");
                }
            }

        } catch (ClassNotFoundException e) {
            System.err.println("[QuanLySuCo] LỖI: Không tìm thấy Oracle JDBC driver. Vui lòng kiểm tra thư viện (classpath).");
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Lỗi nghiêm trọng: Không tìm thấy Oracle JDBC driver.\n" +
                    "Vui lòng đảm bảo file driver (.jar) đã được thêm vào dự án.\nChi tiết: " + e.getMessage(),
                    "Lỗi Driver", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            System.err.println("[QuanLySuCo] LỖI SQL: " + e.getMessage() + " (SQLState: " + e.getSQLState() + ", ErrorCode: " + e.getErrorCode() + ")");
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Lỗi khi truy cập cơ sở dữ liệu hoặc thực thi câu lệnh SQL.\n" +
                    "Chi tiết: " + e.getMessage() + "\n" +
                    "Vui lòng kiểm tra thông tin kết nối, quyền truy cập và cú pháp SQL.\n" +
                    "Xem Console để biết thêm chi tiết.",
                    "Lỗi Cơ Sở Dữ Liệu", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) { 
            System.err.println("[QuanLySuCo] LỖI KHÔNG MONG MUỐN: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Đã xảy ra lỗi không mong muốn trong quá trình tải dữ liệu.\n" +
                    "Chi tiết: " + e.getMessage() + "\n" +
                    "Xem Console để biết thêm chi tiết.",
                    "Lỗi Không Xác Định", JOptionPane.ERROR_MESSAGE);
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
                System.out.println("[QuanLySuCo] Đã đóng kết nối CSDL (nếu có).");
            } catch (SQLException ex) {
                System.err.println("[QuanLySuCo] Lỗi khi đóng tài nguyên CSDL: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
        clearForm(); 
    }

    // Method addIncident() removed

    private void updateIncident() {
        String maBCSuCo = txtMaBCSuCo.getText().trim();
        String tinhTrang = comboTinhTrangXuLy.getSelectedItem() != null ? comboTinhTrangXuLy.getSelectedItem().toString() : "CHUA XU LY";

        if (maBCSuCo.isEmpty()){
             JOptionPane.showMessageDialog(this, "Vui lòng chọn một sự cố từ bảng để cập nhật.", "Cảnh Báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        // No need to validate other fields as they are not being updated directly by user input here.

        // String sql = "UPDATE BAOCAOSUCO SET NGAYBAOCAO = ?, NOIDUNG = ?, TINHTRANGXULY = ?, MANHANVIEN = ? WHERE MABCSUCO = ?"; // Old SQL
        String sql = "UPDATE BAOCAOSUCO SET TINHTRANGXULY = ? WHERE MABCSUCO = ?"; // << MODIFIED SQL
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = ConnectionOracle.getOracleConnection();
            pstmt = conn.prepareStatement(sql);
            // pstmt.setTimestamp(1, ngayBaoCaoTimestamp); // Removed
            // pstmt.setString(2, noiDung); // Removed
            pstmt.setString(1, tinhTrang); // << MODIFIED: Parameter index 1
            // if (maNhanVien.isEmpty()) { // Removed
            //     pstmt.setNull(4, java.sql.Types.VARCHAR);
            // } else {
            //     pstmt.setString(4, maNhanVien);
            // }
            pstmt.setString(2, maBCSuCo); // << MODIFIED: Parameter index 2 (for WHERE clause)

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                JOptionPane.showMessageDialog(this, "Cập nhật tình trạng sự cố thành công!", "Thông Báo", JOptionPane.INFORMATION_MESSAGE);
                refreshTable(); 
            } else {
                JOptionPane.showMessageDialog(this, "Cập nhật sự cố thất bại. Không tìm thấy sự cố hoặc không có gì thay đổi.", "Thông Báo", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException e) {
            // ORA-02291 (Foreign Key violation) check for MaNhanVien is no longer directly relevant here
            // as MaNhanVien is not being updated by this method.
            // However, general SQL error handling is still important.
            JOptionPane.showMessageDialog(this, "Lỗi khi cập nhật sự cố: " + e.getMessage(), "Lỗi SQL", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
             JOptionPane.showMessageDialog(this, "Lỗi Driver JDBC: " + e.getMessage(), "Lỗi Driver", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        } finally {
            try {
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (UnsupportedLookAndFeelException ex) {
            System.err.println("Failed to initialize LaF: " + ex.getMessage());
        }
        SwingUtilities.invokeLater(() -> {
            QuanLySuCo qlsFrame = new QuanLySuCo(null); 
            qlsFrame.setVisible(true);
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
