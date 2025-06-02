package View.Admin;

import ConnectDB.ConnectionOracle;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;
import java.util.Vector;
import java.util.regex.PatternSyntaxException;
import com.formdev.flatlaf.FlatLightLaf;
import java.util.regex.Pattern;


public class QuanLyDichVu extends javax.swing.JFrame {
    private AdminHomePage parentFrame; // Để quay lại màn hình chính

    // UI Components
    private JPanel mainPanel;
    private JTextField txtMaDV, txtTenDV, txtMoTa, txtGia, txtTimKiem;
    private JButton btnThem, btnSua, btnXoa, btnLuu, btnLamMoi, btnTimKiem, btnQuayLai;
    private JTable tblDichVu;
    private DefaultTableModel tblModelDichVu;
    private JScrollPane scrollPaneDichVu;

    // Database connection variables
    private Connection conn;
    private PreparedStatement pstmt;
    private ResultSet rs;

    // Constants for UI styling (tương tự AdminHomePage để đồng bộ)
    private static final Color BACKGROUND_COLOR = new Color(235, 245, 251);
    private static final Color HEADER_BACKGROUND = Color.WHITE;
    private static final Color BUTTON_COLOR = new Color(0, 120, 215);
    private static final Color BUTTON_TEXT_COLOR = Color.WHITE;
    private static final Font BOLD_FONT = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font PLAIN_FONT = new Font("Segoe UI", Font.PLAIN, 14);

    public QuanLyDichVu(AdminHomePage parent) {
        this.parentFrame = parent; // Lưu lại frame cha

        // Áp dụng FlatLaf Look and Feel (tùy chọn)
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (UnsupportedLookAndFeelException ex) {
            System.err.println("Failed to initialize LaF: " + ex.getMessage());
        }

        setTitle("Quản Lý Dịch Vụ Bảo Dưỡng");
        setSize(1000, 650); // Tăng kích thước để chứa đủ các thành phần
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Chỉ đóng cửa sổ này
        setLocationRelativeTo(null);
        setResizable(false);

        initComponentsUI();
        loadDataToTable(); // Tải dữ liệu khi khởi tạo

        // Xử lý sự kiện đóng cửa sổ
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                if (parentFrame != null) {
                    parentFrame.setVisible(true); // Hiển thị lại trang admin chính
                }
            }
        });
    }

    private void initComponentsUI() {
        mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(BACKGROUND_COLOR);
        setContentPane(mainPanel);

        // --- Input Panel ---
        JPanel inputPanelContainer = new JPanel(new BorderLayout(0, 10));
        inputPanelContainer.setBackground(HEADER_BACKGROUND);
        inputPanelContainer.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Thông tin dịch vụ",
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                javax.swing.border.TitledBorder.DEFAULT_POSITION,
                BOLD_FONT, BUTTON_COLOR));
        inputPanelContainer.setPreferredSize(new Dimension(300, 0)); // Chiều rộng cố định cho panel input

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new GridBagLayout());
        inputPanel.setBackground(HEADER_BACKGROUND);
        inputPanel.setBorder(new EmptyBorder(10,10,10,10));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Mã dịch vụ
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel lblMaDV = new JLabel("Mã dịch vụ:");
        lblMaDV.setFont(PLAIN_FONT);
        inputPanel.add(lblMaDV, gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0;
        txtMaDV = new JTextField(15);
        txtMaDV.setFont(PLAIN_FONT);
        inputPanel.add(txtMaDV, gbc);

        // Tên dịch vụ
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        JLabel lblTenDV = new JLabel("Tên dịch vụ:");
        lblTenDV.setFont(PLAIN_FONT);
        inputPanel.add(lblTenDV, gbc);
        gbc.gridx = 1; gbc.gridy = 1;
        txtTenDV = new JTextField(15);
        txtTenDV.setFont(PLAIN_FONT);
        inputPanel.add(txtTenDV, gbc);

        // Mô tả
        gbc.gridx = 0; gbc.gridy = 2;
        JLabel lblMoTa = new JLabel("Mô tả:");
        lblMoTa.setFont(PLAIN_FONT);
        inputPanel.add(lblMoTa, gbc);
        gbc.gridx = 1; gbc.gridy = 2;
        txtMoTa = new JTextField(15);
        txtMoTa.setFont(PLAIN_FONT);
        inputPanel.add(txtMoTa, gbc);

        // Giá
        gbc.gridx = 0; gbc.gridy = 3;
        JLabel lblGia = new JLabel("Giá (VNĐ):");
        lblGia.setFont(PLAIN_FONT);
        inputPanel.add(lblGia, gbc);
        gbc.gridx = 1; gbc.gridy = 3;
        txtGia = new JTextField(15);
        txtGia.setFont(PLAIN_FONT);
        inputPanel.add(txtGia, gbc);

        inputPanelContainer.add(inputPanel, BorderLayout.NORTH);

        // --- Button Panel for Input Form ---
        JPanel formButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        formButtonPanel.setBackground(HEADER_BACKGROUND);

        btnThem = createStyledButton("Thêm mới");
        btnLuu = createStyledButton("Lưu");
        btnSua = createStyledButton("Sửa"); // Nút Sửa để load dữ liệu lên form
        btnLamMoi = createStyledButton("Làm mới form"); // Nút làm mới form

        formButtonPanel.add(btnThem);
        formButtonPanel.add(btnLuu);
        formButtonPanel.add(btnSua);
        formButtonPanel.add(btnLamMoi);
        inputPanelContainer.add(formButtonPanel, BorderLayout.CENTER);

        mainPanel.add(inputPanelContainer, BorderLayout.WEST);

        // --- Table and Actions Panel (Center + South) ---
        JPanel tableAndActionsPanel = new JPanel(new BorderLayout(10, 10));
        tableAndActionsPanel.setBackground(BACKGROUND_COLOR);

        // --- Search Panel ---
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        searchPanel.setBackground(BACKGROUND_COLOR);
        JLabel lblTimKiem = new JLabel("Tìm kiếm dịch vụ:");
        lblTimKiem.setFont(BOLD_FONT);
        txtTimKiem = new JTextField(20);
        txtTimKiem.setFont(PLAIN_FONT);
        btnTimKiem = createStyledButton("Tìm kiếm");
        JButton btnClearSearch = createStyledButton("Xóa tìm");


        searchPanel.add(lblTimKiem);
        searchPanel.add(txtTimKiem);
        searchPanel.add(btnTimKiem);
        searchPanel.add(btnClearSearch);
        tableAndActionsPanel.add(searchPanel, BorderLayout.NORTH);

        // --- Table Panel ---
        tblModelDichVu = new DefaultTableModel(new String[]{"Mã DV", "Tên Dịch Vụ", "Mô Tả", "Giá"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Không cho sửa trực tiếp trên bảng
            }
        };
        tblDichVu = new JTable(tblModelDichVu);
        tblDichVu.setFont(PLAIN_FONT);
        tblDichVu.setRowHeight(25);
        tblDichVu.getTableHeader().setFont(BOLD_FONT);
        tblDichVu.getTableHeader().setBackground(BUTTON_COLOR);
        tblDichVu.getTableHeader().setForeground(Color.WHITE);
        tblDichVu.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // Chỉ cho chọn 1 dòng
        scrollPaneDichVu = new JScrollPane(tblDichVu);
        tableAndActionsPanel.add(scrollPaneDichVu, BorderLayout.CENTER);

        // --- Action Buttons Panel for Table ---
        JPanel actionButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        actionButtonPanel.setBackground(BACKGROUND_COLOR);
        btnXoa = createStyledButton("Xóa dịch vụ đã chọn");
        btnQuayLai = createStyledButton("Quay lại");

        actionButtonPanel.add(btnXoa);
        actionButtonPanel.add(btnQuayLai);
        tableAndActionsPanel.add(actionButtonPanel, BorderLayout.SOUTH);

        mainPanel.add(tableAndActionsPanel, BorderLayout.CENTER);

        // --- Event Listeners ---
        btnThem.addActionListener(this::themDichVuAction);
        btnLuu.addActionListener(this::luuDichVuAction);
        btnSua.addActionListener(this::suaDichVuAction); // Kích hoạt chế độ sửa
        btnXoa.addActionListener(this::xoaDichVuAction);
        btnLamMoi.addActionListener(e -> clearForm());
        btnTimKiem.addActionListener(this::timKiemDichVuAction);
        btnClearSearch.addActionListener(e -> {
            txtTimKiem.setText("");
            loadDataToTable(); // Tải lại toàn bộ dữ liệu
        });
        btnQuayLai.addActionListener(this::quayLaiAction);

        tblDichVu.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) { // Một click để chọn và load lên form nếu muốn sửa
                     // Không tự động load lên form khi click, chỉ khi nhấn nút "Sửa"
                }
            }
        });

        // Ban đầu, nút Lưu và Mã DV không cho sửa
        btnLuu.setEnabled(false);
        txtMaDV.setEditable(false);
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(BOLD_FONT);
        button.setBackground(BUTTON_COLOR);
        button.setForeground(BUTTON_TEXT_COLOR);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(Math.max(150, text.length()*10 + 20), 35));
        // Thêm hiệu ứng hover đơn giản
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                button.setBackground(BUTTON_COLOR.darker());
            }
            public void mouseExited(MouseEvent evt) {
                button.setBackground(BUTTON_COLOR);
            }
        });
        return button;
    }

    private void clearForm() {
        txtMaDV.setText("DV" + System.currentTimeMillis() % 10000); // Gợi ý mã DV
        txtTenDV.setText("");
        txtMoTa.setText("");
        txtGia.setText("");
        txtMaDV.setEditable(true); // Cho phép sửa mã khi thêm mới
        txtMaDV.requestFocus();
        btnThem.setEnabled(false); // Vô hiệu hóa nút Thêm khi đang ở chế độ nhập mới
        btnLuu.setEnabled(true);   // Kích hoạt nút Lưu
        btnSua.setEnabled(true);  // Kích hoạt lại nút Sửa
        tblDichVu.clearSelection();
    }
    
    private void resetFormToInitialState() {
        txtMaDV.setText("");
        txtTenDV.setText("");
        txtMoTa.setText("");
        txtGia.setText("");
        txtMaDV.setEditable(false);
        btnThem.setEnabled(true);
        btnLuu.setEnabled(false);
        btnSua.setEnabled(true);
        tblDichVu.clearSelection();
    }

    private void themDichVuAction(ActionEvent e) {
        clearForm(); // Chuẩn bị form cho việc thêm mới
        // txtMaDV tự động được set editable trong clearForm()
    }
    
    private void suaDichVuAction(ActionEvent e) {
        int selectedRow = tblDichVu.getSelectedRow();
        if (selectedRow >= 0) {
            txtMaDV.setText(tblModelDichVu.getValueAt(selectedRow, 0).toString());
            txtTenDV.setText(tblModelDichVu.getValueAt(selectedRow, 1).toString());
            txtMoTa.setText(tblModelDichVu.getValueAt(selectedRow, 2) != null ? tblModelDichVu.getValueAt(selectedRow, 2).toString() : "");
            txtGia.setText(tblModelDichVu.getValueAt(selectedRow, 3).toString());

            txtMaDV.setEditable(false); // Không cho sửa mã dịch vụ
            btnLuu.setEnabled(true);    // Cho phép lưu thay đổi
            btnThem.setEnabled(false);  // Không cho thêm mới khi đang sửa
        } else {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một dịch vụ từ bảng để sửa.", "Thông báo", JOptionPane.WARNING_MESSAGE);
        }
    }


    private boolean validateInput() {
        if (txtMaDV.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Mã dịch vụ không được để trống!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            txtMaDV.requestFocus();
            return false;
        }
        if (txtTenDV.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tên dịch vụ không được để trống!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            txtTenDV.requestFocus();
            return false;
        }
        try {
            double gia = Double.parseDouble(txtGia.getText().trim());
            if (gia < 0) {
                JOptionPane.showMessageDialog(this, "Giá dịch vụ không được là số âm!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                txtGia.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Giá dịch vụ phải là một số hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            txtGia.requestFocus();
            return false;
        }
        return true;
    }

    private boolean isMaDVExists(String maDV) throws SQLException, ClassNotFoundException {
        String query = "SELECT COUNT(*) FROM DICHVUBAODUONG WHERE MADVBAODUONG = ?";
        conn = ConnectionOracle.getOracleConnection(); //
        pstmt = conn.prepareStatement(query);
        pstmt.setString(1, maDV);
        rs = pstmt.executeQuery();
        if (rs.next()) {
            return rs.getInt(1) > 0;
        }
        return false;
    }

    private void luuDichVuAction(ActionEvent e) {
        if (!validateInput()) {
            return;
        }

        String maDV = txtMaDV.getText().trim();
        String tenDV = txtTenDV.getText().trim();
        String moTa = txtMoTa.getText().trim();
        double gia = Double.parseDouble(txtGia.getText().trim());

        try {
            conn = ConnectionOracle.getOracleConnection(); //
            // Kiểm tra xem đây là THÊM MỚI hay CẬP NHẬT
            // Nếu txtMaDV.isEditable() == true => Thêm mới
            // Ngược lại => Cập nhật
            if (txtMaDV.isEditable()) { // Chế độ thêm mới
                if (isMaDVExists(maDV)) {
                    JOptionPane.showMessageDialog(this, "Mã dịch vụ đã tồn tại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    txtMaDV.requestFocus();
                    return;
                }
                String sql = "INSERT INTO DICHVUBAODUONG (MADVBAODUONG, TENDV, MOTA, GIA) VALUES (?, ?, ?, ?)";
                pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, maDV);
                pstmt.setString(2, tenDV);
                pstmt.setString(3, moTa);
                pstmt.setDouble(4, gia);
                pstmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Thêm dịch vụ thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            } else { // Chế độ cập nhật (Sửa)
                String sql = "UPDATE DICHVUBAODUONG SET TENDV = ?, MOTA = ?, GIA = ? WHERE MADVBAODUONG = ?";
                pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, tenDV);
                pstmt.setString(2, moTa);
                pstmt.setDouble(3, gia);
                pstmt.setString(4, maDV); // maDV lấy từ txtMaDV (đã bị disable edit)
                pstmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Cập nhật dịch vụ thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            }
            loadDataToTable();
            resetFormToInitialState(); // Reset form sau khi lưu
        } catch (SQLException | ClassNotFoundException ex) {
            JOptionPane.showMessageDialog(this, "Lỗi khi thao tác với CSDL: " + ex.getMessage(), "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        } finally {
            closeResources();
        }
    }

    private void xoaDichVuAction(ActionEvent e) {
        int selectedRow = tblDichVu.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một dịch vụ để xóa.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String maDV = tblModelDichVu.getValueAt(selectedRow, 0).toString();
        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn xóa dịch vụ '" + maDV + "' không?",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                conn = ConnectionOracle.getOracleConnection(); //
                // Kiểm tra xem dịch vụ có đang được sử dụng không
                if (isServiceInUse(maDV)) {
                    JOptionPane.showMessageDialog(this,
                        "Không thể xóa dịch vụ này vì đang được tham chiếu trong Bảng Bảo Dưỡng hoặc Chi Tiết Hóa Đơn.",
                        "Lỗi ràng buộc khóa ngoại", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                String sql = "DELETE FROM DICHVUBAODUONG WHERE MADVBAODUONG = ?";
                pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, maDV);
                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this, "Xóa dịch vụ thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                    loadDataToTable();
                    resetFormToInitialState();
                } else {
                     JOptionPane.showMessageDialog(this, "Không tìm thấy dịch vụ để xóa.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException | ClassNotFoundException ex) {
                 if (ex.getMessage().toUpperCase().contains("ORA-02292")) { // Lỗi khóa ngoại cụ thể của Oracle
                    JOptionPane.showMessageDialog(this,
                        "Không thể xóa dịch vụ này. Dịch vụ đang được sử dụng trong các bản ghi khác (ví dụ: bảo dưỡng, hóa đơn).",
                        "Lỗi ràng buộc khóa ngoại", JOptionPane.ERROR_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Lỗi khi xóa dịch vụ: " + ex.getMessage(), "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
                }
                ex.printStackTrace();
            } finally {
                closeResources();
            }
        }
    }
    
    private boolean isServiceInUse(String maDV) throws SQLException, ClassNotFoundException {
        String[] checkQueries = {
            "SELECT 1 FROM BAODUONGXE WHERE MADVBAODUONG = ? AND ROWNUM = 1",
            "SELECT 1 FROM CHITIETHOADON WHERE MADVBAODUONG = ? AND ROWNUM = 1"
        };
        conn = ConnectionOracle.getOracleConnection(); //
        for (String query : checkQueries) {
            pstmt = conn.prepareStatement(query);
            pstmt.setString(1, maDV);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return true; // Tìm thấy tham chiếu
            }
            rs.close();
            pstmt.close();
        }
        // Không đóng connection ở đây vì có thể được gọi từ hàm khác đã mở connection
        return false;
    }


    private void timKiemDichVuAction(ActionEvent e) {
        String keyword = txtTimKiem.getText().trim().toLowerCase();
        if (keyword.isEmpty()) {
            loadDataToTable(); // Nếu ô tìm kiếm trống, tải lại tất cả
            return;
        }
        
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tblModelDichVu);
        tblDichVu.setRowSorter(sorter);
        
        // Tạo bộ lọc cho nhiều cột: Mã DV, Tên DV, Mô tả
        // Sử dụng (?i) để không phân biệt hoa thường trong regex
        try {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(keyword)));
        } catch (PatternSyntaxException pse) {
            System.err.println("Bad regex pattern: " + pse.getMessage());
            JOptionPane.showMessageDialog(this, "Lỗi cú pháp mẫu tìm kiếm. Vui lòng thử lại.", "Lỗi tìm kiếm", JOptionPane.ERROR_MESSAGE);
            sorter.setRowFilter(null); // Xóa bộ lọc nếu có lỗi
            loadDataToTable(); // Tải lại dữ liệu gốc
        }
    }


    private void loadDataToTable() {
        tblModelDichVu.setRowCount(0); // Xóa dữ liệu cũ
        try {
            conn = ConnectionOracle.getOracleConnection();
            String sql = "SELECT MADVBAODUONG, TENDV, MOTA, GIA FROM DICHVUBAODUONG ORDER BY MADVBAODUONG";
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getString("MADVBAODUONG"));
                row.add(rs.getString("TENDV"));
                row.add(rs.getString("MOTA"));
                row.add(rs.getDouble("GIA"));
                tblModelDichVu.addRow(row);
            }
             // Xóa bộ lọc nếu có sau khi tải lại dữ liệu
            if (tblDichVu.getRowSorter() != null) {
                ((TableRowSorter<?>) tblDichVu.getRowSorter()).setRowFilter(null);
            }
        } catch (SQLException | ClassNotFoundException ex) {
            JOptionPane.showMessageDialog(this, "Lỗi khi tải dữ liệu dịch vụ: " + ex.getMessage(), "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        } finally {
            closeResources();
        }
        resetFormToInitialState();
    }

    private void quayLaiAction(ActionEvent e) {
        if (parentFrame != null) {
            parentFrame.setVisible(true);
        }
        this.dispose(); // Đóng cửa sổ quản lý dịch vụ
    }

    private void closeResources() {
        try {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
            if (conn != null) conn.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    // Main method (Chỉ để test riêng form này, có thể xóa nếu không cần)
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            // Để test, tạo một AdminHomePage giả hoặc null nếu không cần tương tác parent
            // new QuanLyDichVu(null).setVisible(true); 
             try {
                UIManager.setLookAndFeel(new FlatLightLaf()); // Áp dụng theme
            } catch (UnsupportedLookAndFeelException ex) {
                System.err.println("Failed to initialize LaF: " + ex.getMessage());
            }
            AdminHomePage fakeParent = new AdminHomePage(); // Tạo một instance giả
            fakeParent.setVisible(false); // Không cần hiển thị nó
            new QuanLyDichVu(fakeParent).setVisible(true);
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
