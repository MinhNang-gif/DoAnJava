package View.Admin;

import ConnectDB.ConnectionOracle;
import com.formdev.flatlaf.FlatLightLaf;
import com.toedter.calendar.JDateChooser;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class QuanLyNhanSuBaiXe extends javax.swing.JFrame {
    private AdminHomePage adminHomePage;

    private JTabbedPane tabbedPane;
    private JPanel shiftManagementPanel;
    private JPanel personnelReportPanel;

    private JTextField txtMaCa, txtSearchMaNV, txtSearchTenNV;
    private JComboBox<NhanVienDTO> cmbNhanVien;
    private JDateChooser dateNgayLam, dateSearchNgayLam;
    private JSpinner timeGioBatDau, timeGioKetThuc;
    private JButton btnThemCa, btnSuaCa, btnXoaCa, btnLamMoiForm, btnTimKiemCa, btnResetTimKiem, btnGenerateMaCa; // Added btnGenerateMaCa here
    private JTable tblCaLamViec;
    private DefaultTableModel modelCaLamViec;

    private JComboBox<String> cmbReportType;
    private JDateChooser dateReportNgay;
    private JButton btnXemBaoCao;
    private DefaultTableModel modelBaoCao;
    private JTable tblBaoCao;
    private JButton btnQuayLai; // Declare here for styling

    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
    private SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

    // Định nghĩa màu sắc cho nút
    private static final Color COLOR_PRIMARY_ACTION = new Color(0, 120, 215);
    private static final Color COLOR_SECONDARY_ACTION = new Color(108, 117, 125); // Bootstrap's secondary color (gray)
    // Hoặc một màu xanh nhạt hơn: private static final Color COLOR_SECONDARY_ACTION = new Color(70, 130, 180); // SteelBlue
    private static final Color COLOR_BUTTON_TEXT = Color.WHITE;


    private static class NhanVienDTO {
        String maNV;
        String tenNV;

        public NhanVienDTO(String maNV, String tenNV) {
            this.maNV = maNV;
            this.tenNV = tenNV;
        }

        @Override
        public String toString() {
            return maNV + " - " + tenNV;
        }
    }

    public QuanLyNhanSuBaiXe(AdminHomePage parent) {
        this.adminHomePage = parent;
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (UnsupportedLookAndFeelException ex) {
            System.err.println("Failed to initialize LaF: " + ex.getMessage());
        }

        setTitle("Quản Lý Nhân Sự Bãi Xe");
        setSize(1100, 750);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                closeFrame();
            }
        });

        tabbedPane = new JTabbedPane();

        createShiftManagementPanel();
        tabbedPane.addTab("Quản Lý Ca Làm Việc", null, shiftManagementPanel, "Quản lý thêm, sửa, xóa, tìm kiếm ca làm việc");

        createPersonnelReportPanel();
        tabbedPane.addTab("Báo Cáo Tình Trạng Nhân Sự", null, personnelReportPanel, "Xem báo cáo tình trạng nhân sự");

        add(tabbedPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setBorder(new EmptyBorder(5, 0, 5, 10));
        btnQuayLai = new JButton("Quay Lại Trang Chủ"); // Khởi tạo ở đây
        styleButton(btnQuayLai, COLOR_SECONDARY_ACTION, COLOR_BUTTON_TEXT); // Áp dụng style
        btnQuayLai.addActionListener(e -> closeFrame());
        bottomPanel.add(btnQuayLai);
        add(bottomPanel, BorderLayout.SOUTH);

        loadNhanVienComboBox();
        loadCaLamViecTable(null, null, null);
    }

    private void styleButton(JButton button, Color backgroundColor, Color foregroundColor) {
        button.setBackground(backgroundColor);
        button.setForeground(foregroundColor);
        button.setFocusPainted(false); // Tắt viền focus để trông gọn hơn
        // button.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15)); // Thêm padding nếu cần
        // UIManager.put("Button.arc", 6); // For rounded corners with FlatLaf, set globally or per component if possible
    }
    
    private void closeFrame() {
        if (adminHomePage != null) {
            adminHomePage.setVisible(true);
        }
        dispose();
    }

    private void createShiftManagementPanel() {
        shiftManagementPanel = new JPanel(new BorderLayout(10, 10));
        shiftManagementPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(new TitledBorder("Thông Tin Ca Làm Việc"));
        
        JPanel inputFieldsContainerPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbcInput = new GridBagConstraints();
        gbcInput.insets = new Insets(5, 5, 5, 5);
        gbcInput.anchor = GridBagConstraints.WEST;

        int yRow = 0;

        gbcInput.gridx = 0; gbcInput.gridy = yRow; gbcInput.anchor = GridBagConstraints.EAST; 
        inputFieldsContainerPanel.add(new JLabel("Mã Ca:"), gbcInput);
        
        gbcInput.gridx = 1; gbcInput.gridy = yRow; gbcInput.anchor = GridBagConstraints.WEST; gbcInput.fill = GridBagConstraints.NONE;
        txtMaCa = new JTextField(); txtMaCa.setEditable(false);
        txtMaCa.setPreferredSize(new Dimension(180, txtMaCa.getPreferredSize().height));
        inputFieldsContainerPanel.add(txtMaCa, gbcInput);
        
        gbcInput.gridx = 2; gbcInput.gridy = yRow; gbcInput.anchor = GridBagConstraints.WEST;
        btnGenerateMaCa = new JButton("Tạo Mã"); // Khởi tạo ở đây
        styleButton(btnGenerateMaCa, COLOR_SECONDARY_ACTION, COLOR_BUTTON_TEXT); // Áp dụng style
        btnGenerateMaCa.setToolTipText("Tự động tạo mã ca làm việc");
        btnGenerateMaCa.addActionListener(e -> txtMaCa.setText("CA" + UUID.randomUUID().toString().substring(0, 8).toUpperCase()));
        inputFieldsContainerPanel.add(btnGenerateMaCa, gbcInput);
        yRow++;

        gbcInput.gridx = 0; gbcInput.gridy = yRow; gbcInput.anchor = GridBagConstraints.EAST;
        inputFieldsContainerPanel.add(new JLabel("Nhân Viên:"), gbcInput);
        gbcInput.gridx = 1; gbcInput.gridy = yRow; gbcInput.anchor = GridBagConstraints.WEST; gbcInput.gridwidth = 2;
        cmbNhanVien = new JComboBox<>();
        cmbNhanVien.setPreferredSize(new Dimension(270, cmbNhanVien.getPreferredSize().height));
        inputFieldsContainerPanel.add(cmbNhanVien, gbcInput);
        gbcInput.gridwidth = 1;
        yRow++;

        gbcInput.gridx = 0; gbcInput.gridy = yRow; gbcInput.anchor = GridBagConstraints.EAST;
        inputFieldsContainerPanel.add(new JLabel("Ngày Làm:"), gbcInput);
        gbcInput.gridx = 1; gbcInput.gridy = yRow; gbcInput.anchor = GridBagConstraints.WEST; gbcInput.gridwidth = 2;
        dateNgayLam = new JDateChooser(); dateNgayLam.setDateFormatString("dd/MM/yyyy");
        dateNgayLam.setPreferredSize(new Dimension(270, dateNgayLam.getPreferredSize().height));
        inputFieldsContainerPanel.add(dateNgayLam, gbcInput);
        gbcInput.gridwidth = 1;
        yRow++;

        gbcInput.gridx = 0; gbcInput.gridy = yRow; gbcInput.anchor = GridBagConstraints.EAST;
        inputFieldsContainerPanel.add(new JLabel("Giờ Bắt Đầu:"), gbcInput);
        gbcInput.gridx = 1; gbcInput.gridy = yRow; gbcInput.anchor = GridBagConstraints.WEST; gbcInput.gridwidth = 2;
        timeGioBatDau = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor deBatDau = new JSpinner.DateEditor(timeGioBatDau, "HH:mm:ss");
        timeGioBatDau.setEditor(deBatDau);
        timeGioBatDau.setValue(new Date());
        timeGioBatDau.setPreferredSize(new Dimension(270, timeGioBatDau.getPreferredSize().height));
        inputFieldsContainerPanel.add(timeGioBatDau, gbcInput);
        gbcInput.gridwidth = 1;
        yRow++;
        
        gbcInput.gridx = 0; gbcInput.gridy = yRow; gbcInput.anchor = GridBagConstraints.EAST;
        inputFieldsContainerPanel.add(new JLabel("Giờ Kết Thúc:"), gbcInput);
        gbcInput.gridx = 1; gbcInput.gridy = yRow; gbcInput.anchor = GridBagConstraints.WEST; gbcInput.gridwidth = 2;
        timeGioKetThuc = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor deKetThuc = new JSpinner.DateEditor(timeGioKetThuc, "HH:mm:ss");
        timeGioKetThuc.setEditor(deKetThuc);
        timeGioKetThuc.setValue(new Date());
        timeGioKetThuc.setPreferredSize(new Dimension(270, timeGioKetThuc.getPreferredSize().height));
        inputFieldsContainerPanel.add(timeGioKetThuc, gbcInput);
        gbcInput.gridwidth = 1;

        GridBagConstraints gbcForm = new GridBagConstraints();
        gbcForm.gridx = 0; gbcForm.gridy = 0;
        gbcForm.anchor = GridBagConstraints.CENTER;
        formPanel.add(inputFieldsContainerPanel, gbcForm);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        btnThemCa = new JButton("Thêm Ca"); styleButton(btnThemCa, COLOR_PRIMARY_ACTION, COLOR_BUTTON_TEXT);
        btnSuaCa = new JButton("Sửa Ca"); styleButton(btnSuaCa, COLOR_PRIMARY_ACTION, COLOR_BUTTON_TEXT);
        btnXoaCa = new JButton("Xóa Ca"); styleButton(btnXoaCa, COLOR_PRIMARY_ACTION, COLOR_BUTTON_TEXT); // Hoặc màu đỏ nếu muốn
        btnLamMoiForm = new JButton("Làm Mới Form"); styleButton(btnLamMoiForm, COLOR_SECONDARY_ACTION, COLOR_BUTTON_TEXT);

        buttonPanel.add(btnThemCa);
        buttonPanel.add(btnSuaCa);
        buttonPanel.add(btnXoaCa);
        buttonPanel.add(btnLamMoiForm);

        JPanel searchOuterPanel = new JPanel(new GridBagLayout()); 
        searchOuterPanel.setBorder(new TitledBorder("Tìm Kiếm Ca Làm Việc"));
        GridBagConstraints gbcOuterSearch = new GridBagConstraints();
        gbcOuterSearch.gridy = 0;
        gbcOuterSearch.weightx = 1; 

        JPanel searchFieldsPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbcSearch = new GridBagConstraints();
        gbcSearch.insets = new Insets(5, 5, 5, 5);
        gbcSearch.anchor = GridBagConstraints.WEST; 

        gbcSearch.gridx = 0; gbcSearch.gridy = 0; searchFieldsPanel.add(new JLabel("Mã NV:"), gbcSearch);
        gbcSearch.gridx = 1; gbcSearch.gridy = 0; gbcSearch.fill = GridBagConstraints.HORIZONTAL; txtSearchMaNV = new JTextField(8); searchFieldsPanel.add(txtSearchMaNV, gbcSearch);
        gbcSearch.gridx = 2; gbcSearch.gridy = 0; gbcSearch.insets = new Insets(5, 15, 5, 5); 
        searchFieldsPanel.add(new JLabel("Tên NV:"), gbcSearch);
        gbcSearch.insets = new Insets(5, 5, 5, 5); 
        gbcSearch.gridx = 3; gbcSearch.gridy = 0; gbcSearch.fill = GridBagConstraints.HORIZONTAL; txtSearchTenNV = new JTextField(12); searchFieldsPanel.add(txtSearchTenNV, gbcSearch);
        gbcSearch.gridx = 4; gbcSearch.gridy = 0; gbcSearch.insets = new Insets(5, 15, 5, 5); 
        searchFieldsPanel.add(new JLabel("Ngày Làm:"), gbcSearch);
        gbcSearch.insets = new Insets(5, 5, 5, 5); 
        gbcSearch.gridx = 5; gbcSearch.gridy = 0; gbcSearch.fill = GridBagConstraints.HORIZONTAL; 
        dateSearchNgayLam = new JDateChooser(); 
        dateSearchNgayLam.setDateFormatString("dd/MM/yyyy");
        dateSearchNgayLam.setPreferredSize(new Dimension(120, dateSearchNgayLam.getPreferredSize().height)); 
        searchFieldsPanel.add(dateSearchNgayLam, gbcSearch);
        
        gbcOuterSearch.gridy = 0;
        searchOuterPanel.add(searchFieldsPanel, gbcOuterSearch);

        JPanel searchButtonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        btnTimKiemCa = new JButton("Tìm Kiếm"); styleButton(btnTimKiemCa, COLOR_PRIMARY_ACTION, COLOR_BUTTON_TEXT);
        btnResetTimKiem = new JButton("Hiện Tất Cả"); styleButton(btnResetTimKiem, COLOR_SECONDARY_ACTION, COLOR_BUTTON_TEXT);
        searchButtonsPanel.add(btnTimKiemCa);
        searchButtonsPanel.add(btnResetTimKiem);
        
        gbcOuterSearch.gridy = 1;
        gbcOuterSearch.fill = GridBagConstraints.HORIZONTAL; 
        searchOuterPanel.add(searchButtonsPanel, gbcOuterSearch);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(formPanel, BorderLayout.NORTH);
        topPanel.add(buttonPanel, BorderLayout.CENTER);
        topPanel.add(searchOuterPanel, BorderLayout.SOUTH); 

        modelCaLamViec = new DefaultTableModel(new String[]{"Mã Ca", "Mã NV", "Tên Nhân Viên", "Ngày Làm", "Giờ Bắt Đầu", "Giờ Kết Thúc"}, 0){
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblCaLamViec = new JTable(modelCaLamViec);
        tblCaLamViec.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblCaLamViec.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int selectedRow = tblCaLamViec.getSelectedRow();
                if (selectedRow >= 0) {
                    txtMaCa.setText(modelCaLamViec.getValueAt(selectedRow, 0).toString());
                    String maNV = modelCaLamViec.getValueAt(selectedRow, 1).toString();
                    for (int i = 0; i < cmbNhanVien.getItemCount(); i++) {
                        if (cmbNhanVien.getItemAt(i).maNV.equals(maNV)) {
                            cmbNhanVien.setSelectedIndex(i);
                            break;
                        }
                    }
                    try {
                        Date ngayLam = dateFormat.parse(modelCaLamViec.getValueAt(selectedRow, 3).toString());
                        dateNgayLam.setDate(ngayLam);
                        Date gioBatDauDateTime = dateTimeFormat.parse(modelCaLamViec.getValueAt(selectedRow, 3).toString() + " " + modelCaLamViec.getValueAt(selectedRow, 4).toString());
                        timeGioBatDau.setValue(gioBatDauDateTime);
                        Date gioKetThucDateTime = dateTimeFormat.parse(modelCaLamViec.getValueAt(selectedRow, 3).toString() + " " + modelCaLamViec.getValueAt(selectedRow, 5).toString());
                        timeGioKetThuc.setValue(gioKetThucDateTime);
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(QuanLyNhanSuBaiXe.this, "Lỗi định dạng ngày/giờ khi chọn từ bảng: "+ e.getMessage(), "Lỗi", JOptionPane.WARNING_MESSAGE);
                    }
                }
            }
        });

        shiftManagementPanel.add(topPanel, BorderLayout.NORTH);
        shiftManagementPanel.add(new JScrollPane(tblCaLamViec), BorderLayout.CENTER);

        btnThemCa.addActionListener(this::addCaLamViecAction);
        btnSuaCa.addActionListener(this::updateCaLamViecAction);
        btnXoaCa.addActionListener(this::deleteCaLamViecAction);
        btnLamMoiForm.addActionListener(e -> clearForm());
        btnTimKiemCa.addActionListener(this::searchCaLamViecAction);
        btnResetTimKiem.addActionListener(e -> {
            txtSearchMaNV.setText("");
            txtSearchTenNV.setText("");
            dateSearchNgayLam.setDate(null);
            loadCaLamViecTable(null, null, null);
        });
    }

    private void createPersonnelReportPanel() {
        personnelReportPanel = new JPanel(new BorderLayout(10, 10));
        personnelReportPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel reportControlsPanel = new JPanel(new GridBagLayout()); 
        reportControlsPanel.setBorder(new TitledBorder("Tùy Chọn Báo Cáo"));
        GridBagConstraints gbcReport = new GridBagConstraints();
        gbcReport.insets = new Insets(5, 5, 5, 10); 
        gbcReport.anchor = GridBagConstraints.WEST;

        gbcReport.gridx = 0; gbcReport.gridy = 0;
        reportControlsPanel.add(new JLabel("Loại Báo Cáo:"), gbcReport);
        gbcReport.gridx = 1; gbcReport.gridy = 0;
        cmbReportType = new JComboBox<>(new String[]{"Nhân sự trực trong ngày", "Lịch trực theo ngày"});
        cmbReportType.setPreferredSize(new Dimension(200, cmbReportType.getPreferredSize().height));
        reportControlsPanel.add(cmbReportType, gbcReport);
        gbcReport.gridx = 2; gbcReport.gridy = 0; gbcReport.insets = new Insets(5, 20, 5, 10); 
        reportControlsPanel.add(new JLabel("Chọn Ngày:"), gbcReport);
        gbcReport.insets = new Insets(5, 5, 5, 10); 
        gbcReport.gridx = 3; gbcReport.gridy = 0;
        dateReportNgay = new JDateChooser();
        dateReportNgay.setDateFormatString("dd/MM/yyyy");
        dateReportNgay.setDate(new Date()); 
        dateReportNgay.setPreferredSize(new Dimension(130, dateReportNgay.getPreferredSize().height));
        reportControlsPanel.add(dateReportNgay, gbcReport);
        gbcReport.gridx = 4; gbcReport.gridy = 0; gbcReport.insets = new Insets(5, 20, 5, 5); 
        btnXemBaoCao = new JButton("Xem Báo Cáo"); styleButton(btnXemBaoCao, COLOR_PRIMARY_ACTION, COLOR_BUTTON_TEXT);
        reportControlsPanel.add(btnXemBaoCao, gbcReport);
        gbcReport.gridx = 5; gbcReport.gridy = 0; gbcReport.weightx = 1.0;
        reportControlsPanel.add(Box.createHorizontalGlue(), gbcReport);

        modelBaoCao = new DefaultTableModel(new String[]{"Thông Tin"}, 0) {
             @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblBaoCao = new JTable(modelBaoCao);
        personnelReportPanel.add(reportControlsPanel, BorderLayout.NORTH);
        personnelReportPanel.add(new JScrollPane(tblBaoCao), BorderLayout.CENTER);
        btnXemBaoCao.addActionListener(this::generatePersonnelReportAction);
    }

    // ... (Các phương thức xử lý CSDL: loadNhanVienComboBox, loadCaLamViecTable, addCaLamViecAction, etc. giữ nguyên) ...
    // Bạn hãy copy các phương thức này từ phiên bản trước vào đây.
    // Để cho ngắn gọn, tôi sẽ không lặp lại chúng ở đây.
    private void loadNhanVienComboBox() {
        cmbNhanVien.removeAllItems();
        String sql = "SELECT MANHANVIEN, TENNHANVIEN FROM NHANVIEN ORDER BY TENNHANVIEN";
        try (Connection conn = ConnectionOracle.getOracleConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                cmbNhanVien.addItem(new NhanVienDTO(rs.getString("MANHANVIEN"), rs.getString("TENNHANVIEN")));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi tải danh sách nhân viên: " + e.getMessage(), "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadCaLamViecTable(String searchMaNV, String searchTenNV, Date searchNgay) {
        modelCaLamViec.setRowCount(0);
        StringBuilder sqlBuilder = new StringBuilder(
            "SELECT c.MACA, c.MANHANVIEN, n.TENNHANVIEN, c.NGAYLAM, c.GIOBATDAU, c.GIOKETTHUC " +
            "FROM CALAMVIEC c " +
            "JOIN NHANVIEN n ON c.MANHANVIEN = n.MANHANVIEN "
        );
        List<Object> params = new ArrayList<>();
        boolean hasWhere = false;

        if (searchMaNV != null && !searchMaNV.trim().isEmpty()) {
            sqlBuilder.append(hasWhere ? " AND " : " WHERE ");
            sqlBuilder.append("UPPER(c.MANHANVIEN) LIKE UPPER(?)"); 
            params.add("%" + searchMaNV.trim() + "%");
            hasWhere = true;
        }
        if (searchTenNV != null && !searchTenNV.trim().isEmpty()) {
            sqlBuilder.append(hasWhere ? " AND " : " WHERE ");
            sqlBuilder.append("UPPER(n.TENNHANVIEN) LIKE UPPER(?)");
            params.add("%" + searchTenNV.trim() + "%");
            hasWhere = true;
        }
        if (searchNgay != null) {
            sqlBuilder.append(hasWhere ? " AND " : " WHERE ");
            sqlBuilder.append("TRUNC(c.NGAYLAM) = ?"); 
            params.add(new java.sql.Date(searchNgay.getTime()));
            hasWhere = true;
        }
        sqlBuilder.append(" ORDER BY c.NGAYLAM DESC, c.GIOBATDAU ASC");

        try (Connection conn = ConnectionOracle.getOracleConnection();
             PreparedStatement pstmt = conn.prepareStatement(sqlBuilder.toString())) {
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                modelCaLamViec.addRow(new Object[]{
                        rs.getString("MACA"),
                        rs.getString("MANHANVIEN"),
                        rs.getString("TENNHANVIEN"),
                        dateFormat.format(rs.getDate("NGAYLAM")),
                        timeFormat.format(rs.getTimestamp("GIOBATDAU")),
                        timeFormat.format(rs.getTimestamp("GIOKETTHUC"))
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi tải dữ liệu ca làm việc: " + e.getMessage(), "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void searchCaLamViecAction(ActionEvent e) {
        String maNV = txtSearchMaNV.getText();
        String tenNV = txtSearchTenNV.getText();
        Date ngayLam = dateSearchNgayLam.getDate();
        loadCaLamViecTable(maNV, tenNV, ngayLam);
    }

    private void addCaLamViecAction(ActionEvent e) {
        String maCa = txtMaCa.getText().trim();
        if (maCa.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Mã ca không được để trống. Hãy tạo mã.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (cmbNhanVien.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn nhân viên.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        NhanVienDTO selectedNhanVien = (NhanVienDTO) cmbNhanVien.getSelectedItem();
        Date ngayLam = dateNgayLam.getDate();
        if (ngayLam == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn ngày làm.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Date gioBatDauUtil = (Date) timeGioBatDau.getValue();
        Date gioKetThucUtil = (Date) timeGioKetThuc.getValue();

        Calendar cal = Calendar.getInstance();
        cal.setTime(ngayLam);

        Calendar timeCalStart = Calendar.getInstance();
        timeCalStart.setTime(gioBatDauUtil);
        cal.set(Calendar.HOUR_OF_DAY, timeCalStart.get(Calendar.HOUR_OF_DAY));
        cal.set(Calendar.MINUTE, timeCalStart.get(Calendar.MINUTE));
        cal.set(Calendar.SECOND, timeCalStart.get(Calendar.SECOND));
        Timestamp gioBatDau = new Timestamp(cal.getTimeInMillis());

        Calendar timeCalEnd = Calendar.getInstance();
        timeCalEnd.setTime(gioKetThucUtil);
        cal.set(Calendar.HOUR_OF_DAY, timeCalEnd.get(Calendar.HOUR_OF_DAY));
        cal.set(Calendar.MINUTE, timeCalEnd.get(Calendar.MINUTE));
        cal.set(Calendar.SECOND, timeCalEnd.get(Calendar.SECOND));
        Timestamp gioKetThuc = new Timestamp(cal.getTimeInMillis());


        if (gioKetThuc.before(gioBatDau) || gioKetThuc.equals(gioBatDau)) {
             JOptionPane.showMessageDialog(this, "Giờ kết thúc phải sau giờ bắt đầu.", "Lỗi", JOptionPane.ERROR_MESSAGE);
             return;
        }

        String sql = "INSERT INTO CALAMVIEC (MACA, MANHANVIEN, NGAYLAM, GIOBATDAU, GIOKETTHUC) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = ConnectionOracle.getOracleConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, maCa);
            pstmt.setString(2, selectedNhanVien.maNV);
            pstmt.setDate(3, new java.sql.Date(ngayLam.getTime()));
            pstmt.setTimestamp(4, gioBatDau);
            pstmt.setTimestamp(5, gioKetThuc);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                JOptionPane.showMessageDialog(this, "Thêm ca làm việc thành công!", "Thông Báo", JOptionPane.INFORMATION_MESSAGE);
                loadCaLamViecTable(null, null, null);
                clearForm();
            }
        } catch (SQLIntegrityConstraintViolationException dupkey) {
             JOptionPane.showMessageDialog(this, "Lỗi: Mã ca '" + maCa + "' đã tồn tại.", "Lỗi Trùng Mã", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi khi thêm ca làm việc: " + ex.getMessage(), "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateCaLamViecAction(ActionEvent e) {
        int selectedRow = tblCaLamViec.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một ca để sửa.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String maCa = txtMaCa.getText().trim(); 
         if (cmbNhanVien.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn nhân viên.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        NhanVienDTO selectedNhanVien = (NhanVienDTO) cmbNhanVien.getSelectedItem();
        Date ngayLam = dateNgayLam.getDate();
        if (ngayLam == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn ngày làm.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Date gioBatDauUtil = (Date) timeGioBatDau.getValue();
        Date gioKetThucUtil = (Date) timeGioKetThuc.getValue();

        Calendar cal = Calendar.getInstance();
        cal.setTime(ngayLam);
        Calendar timeCalStart = Calendar.getInstance();
        timeCalStart.setTime(gioBatDauUtil);
        cal.set(Calendar.HOUR_OF_DAY, timeCalStart.get(Calendar.HOUR_OF_DAY));
        cal.set(Calendar.MINUTE, timeCalStart.get(Calendar.MINUTE));
        cal.set(Calendar.SECOND, timeCalStart.get(Calendar.SECOND));
        Timestamp gioBatDau = new Timestamp(cal.getTimeInMillis());

        Calendar timeCalEnd = Calendar.getInstance();
        timeCalEnd.setTime(gioKetThucUtil);
        cal.set(Calendar.HOUR_OF_DAY, timeCalEnd.get(Calendar.HOUR_OF_DAY));
        cal.set(Calendar.MINUTE, timeCalEnd.get(Calendar.MINUTE));
        cal.set(Calendar.SECOND, timeCalEnd.get(Calendar.SECOND));
        Timestamp gioKetThuc = new Timestamp(cal.getTimeInMillis());

        if (gioKetThuc.before(gioBatDau) || gioKetThuc.equals(gioBatDau)) {
             JOptionPane.showMessageDialog(this, "Giờ kết thúc phải sau giờ bắt đầu.", "Lỗi", JOptionPane.ERROR_MESSAGE);
             return;
        }

        String sql = "UPDATE CALAMVIEC SET MANHANVIEN = ?, NGAYLAM = ?, GIOBATDAU = ?, GIOKETTHUC = ? WHERE MACA = ?";
        try (Connection conn = ConnectionOracle.getOracleConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, selectedNhanVien.maNV);
            pstmt.setDate(2, new java.sql.Date(ngayLam.getTime()));
            pstmt.setTimestamp(3, gioBatDau);
            pstmt.setTimestamp(4, gioKetThuc);
            pstmt.setString(5, maCa);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                JOptionPane.showMessageDialog(this, "Cập nhật ca làm việc thành công!", "Thông Báo", JOptionPane.INFORMATION_MESSAGE);
                loadCaLamViecTable(null, null, null);
                clearForm();
            } else {
                JOptionPane.showMessageDialog(this, "Không tìm thấy ca làm việc để cập nhật (Mã: "+maCa+"). Có thể đã bị xóa.", "Lỗi", JOptionPane.WARNING_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi khi cập nhật ca làm việc: " + ex.getMessage(), "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteCaLamViecAction(ActionEvent e) {
        int selectedRow = tblCaLamViec.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một ca để xóa.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String maCa = modelCaLamViec.getValueAt(selectedRow, 0).toString();
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa ca làm việc '" + maCa + "'?", "Xác Nhận Xóa", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            String sql = "DELETE FROM CALAMVIEC WHERE MACA = ?";
            try (Connection conn = ConnectionOracle.getOracleConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, maCa);
                int affectedRows = pstmt.executeUpdate();
                if (affectedRows > 0) {
                    JOptionPane.showMessageDialog(this, "Xóa ca làm việc thành công!", "Thông Báo", JOptionPane.INFORMATION_MESSAGE);
                    loadCaLamViecTable(null, null, null);
                    clearForm();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi khi xóa ca làm việc: " + ex.getMessage(), "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void clearForm() {
        txtMaCa.setText("");
        if (cmbNhanVien.getItemCount() > 0) { 
           cmbNhanVien.setSelectedIndex(0); 
        } else {
            cmbNhanVien.setSelectedItem(null);
        }
        dateNgayLam.setDate(null);
        Date now = new Date();
        timeGioBatDau.setValue(now);
        Calendar cal = Calendar.getInstance();
        cal.setTime(now);
        cal.add(Calendar.HOUR_OF_DAY, 8); 
        timeGioKetThuc.setValue(cal.getTime());
        tblCaLamViec.clearSelection();
    }

    private void generatePersonnelReportAction(ActionEvent e) {
        modelBaoCao.setRowCount(0); 
        String reportType = (String) cmbReportType.getSelectedItem();
        Date selectedDate = dateReportNgay.getDate();

        if (selectedDate == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn ngày cho báo cáo.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String sql = "";
        List<String> columnNames = new ArrayList<>();

        if ("Nhân sự trực trong ngày".equals(reportType)) {
             sql = "SELECT n.TENNHANVIEN, c.GIOBATDAU, c.GIOKETTHUC, " +
                  "CASE " + 
                  "  WHEN TRUNC(c.NGAYLAM) = TRUNC(?) AND CURRENT_TIMESTAMP BETWEEN c.GIOBATDAU AND c.GIOKETTHUC THEN 'Đang trực' " +
                  "  ELSE 'Theo lịch' " +
                  "END AS TRANGTHAI_TRUC " +
                  "FROM CALAMVIEC c " +
                  "JOIN NHANVIEN n ON c.MANHANVIEN = n.MANHANVIEN " +
                  "WHERE TRUNC(c.NGAYLAM) = TRUNC(?) " + 
                  "ORDER BY c.GIOBATDAU";
            columnNames.addAll(List.of("Tên Nhân Viên", "Giờ Bắt Đầu", "Giờ Kết Thúc", "Trạng Thái"));
        } else if ("Lịch trực theo ngày".equals(reportType)) {
            sql = "SELECT n.TENNHANVIEN, c.GIOBATDAU, c.GIOKETTHUC " +
                  "FROM CALAMVIEC c " +
                  "JOIN NHANVIEN n ON c.MANHANVIEN = n.MANHANVIEN " +
                  "WHERE TRUNC(c.NGAYLAM) = TRUNC(?) " +
                  "ORDER BY n.TENNHANVIEN, c.GIOBATDAU";
            columnNames.addAll(List.of("Tên Nhân Viên", "Giờ Bắt Đầu", "Giờ Kết Thúc"));
        }
        modelBaoCao.setColumnIdentifiers(columnNames.toArray());

        try (Connection conn = ConnectionOracle.getOracleConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDate(1, new java.sql.Date(selectedDate.getTime()));
             if ("Nhân sự trực trong ngày".equals(reportType)) { 
                pstmt.setDate(2, new java.sql.Date(selectedDate.getTime())); 
            }

            ResultSet rs = pstmt.executeQuery();
            boolean found = false;
            while (rs.next()) {
                found = true;
                if ("Nhân sự trực trong ngày".equals(reportType)) {
                     modelBaoCao.addRow(new Object[]{
                        rs.getString("TENNHANVIEN"),
                        timeFormat.format(rs.getTimestamp("GIOBATDAU")),
                        timeFormat.format(rs.getTimestamp("GIOKETTHUC")),
                        rs.getString("TRANGTHAI_TRUC")
                    });
                } else { 
                    modelBaoCao.addRow(new Object[]{
                        rs.getString("TENNHANVIEN"),
                        timeFormat.format(rs.getTimestamp("GIOBATDAU")),
                        timeFormat.format(rs.getTimestamp("GIOKETTHUC"))
                    });
                }
            }
            if (!found) {
                modelBaoCao.setColumnIdentifiers(new String[]{"Thông báo"});
                modelBaoCao.addRow(new Object[]{"Không có dữ liệu ca làm việc cho ngày đã chọn."});
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi khi tạo báo cáo: " + ex.getMessage(), "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
            modelBaoCao.setColumnIdentifiers(new String[]{"Lỗi"}); 
            modelBaoCao.addRow(new Object[]{"Lỗi khi tạo báo cáo: " + ex.getMessage()});
        }
    }


    public static void main(String args[]) {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (UnsupportedLookAndFeelException ex) {
            System.err.println("Failed to initialize LaF: " + ex.getMessage());
        }
        java.awt.EventQueue.invokeLater(() -> {
             AdminHomePage fakeParent = null; 
            new QuanLyNhanSuBaiXe(fakeParent).setVisible(true);
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
