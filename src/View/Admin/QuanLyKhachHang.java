package View.Admin;

import ConnectDB.ConnectionUtils;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.table.TableColumn;

public class QuanLyKhachHang extends javax.swing.JFrame {
    private static class UIStyleConstants {
        public static final Color PRIMARY_COLOR = new Color(0, 120, 215);
        public static final Color BACKGROUND_COLOR = new Color(240, 245, 250);
        public static final Color PANEL_BACKGROUND_COLOR = Color.WHITE;
        // public static final Color BUTTON_HOVER_COLOR = new Color(220, 235, 245); // Không thấy dùng
        public static final Font BOLD_FONT = new Font("Segoe UI", Font.BOLD, 14);
        public static final Font PLAIN_FONT = new Font("Segoe UI", Font.PLAIN, 14);
        public static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 28);
        public static final Font TABLE_HEADER_FONT = new Font("Segoe UI", Font.BOLD, 14);

        public static final String ICON_PATH_PREFIX = "/icons/";
        public static final String BACK_ICON = ICON_PATH_PREFIX + "back_arrow.png";
        // public static final String ADD_ICON = ICON_PATH_PREFIX + "add.png"; // Removed
        public static final String EDIT_ICON = ICON_PATH_PREFIX + "update.png";
        public static final String DELETE_ICON = ICON_PATH_PREFIX + "delete.png";
        public static final String CLEAR_ICON = ICON_PATH_PREFIX + "refresh.png";
        public static final String SEARCH_ICON = ICON_PATH_PREFIX + "search.png";
    }

    private JPanel mainPanel;
    private JPanel titlePanel;
    private JPanel inputFormPanel;
    private JPanel buttonPanel;
    private JPanel tablePanel;

    private JTable customerTable;
    private DefaultTableModel tableModel;

    private JTextField txtMaKH;
    private JTextField txtTenKH;
    private JTextField txtSDT;
    private JTextField txtDiaChi;
    private JTextField txtEmail;
    private JTextField txtLoaiKH;
    private JTextField txtSearch;

    private JButton btnNew;
    // private JButton btnAdd; // Removed
    private JButton btnUpdate;
    private JButton btnDelete;
    private JButton btnBack;
    private JButton btnSearch;

    public QuanLyKhachHang() {
        // Yêu cầu FlatLaf không sử dụng visual padding liên quan đến MigLayout
        System.setProperty("flatlaf.useVisualPadding", "false");
        applyLookAndFeel();
        initComponentsUI();
        loadCustomerData();
    }

    private void applyLookAndFeel() {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (UnsupportedLookAndFeelException ex) {
            System.err.println("Failed to initialize LaF: " + ex.getMessage());
        }
    }

    private void initComponentsUI() {
        setTitle("Quản Lý Khách Hàng");
        setSize(1300, 750);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.setBackground(UIStyleConstants.BACKGROUND_COLOR);

        createTitlePanel();
        createInputFormPanel();
        createButtonPanel();
        createTablePanel();

        JPanel topSectionPanel = new JPanel(new BorderLayout(10,10));
        topSectionPanel.setBackground(UIStyleConstants.BACKGROUND_COLOR);
        topSectionPanel.add(inputFormPanel, BorderLayout.CENTER);
        topSectionPanel.add(buttonPanel, BorderLayout.SOUTH);

        mainPanel.add(titlePanel, BorderLayout.NORTH);
        mainPanel.add(topSectionPanel, BorderLayout.CENTER);
        mainPanel.add(tablePanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private ImageIcon loadIcon(String path, int width, int height) {
        URL imgUrl = getClass().getResource(path);
        if (imgUrl != null) {
            ImageIcon originalIcon = new ImageIcon(imgUrl);
            Image scaledImage = originalIcon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
            return new ImageIcon(scaledImage);
        } else {
            System.err.println("Không tìm thấy icon: " + path + ". Using default.");
            BufferedImage placeholder = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = placeholder.createGraphics();
            g2d.setColor(Color.LIGHT_GRAY);
            g2d.fillRect(0, 0, width, height);
            g2d.setColor(Color.DARK_GRAY);
            g2d.drawString("?", width / 2 - 4, height / 2 + 4);
            g2d.dispose();
            return new ImageIcon(placeholder);
        }
    }

    private void createTitlePanel() {
        titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(UIStyleConstants.PANEL_BACKGROUND_COLOR);
        titlePanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel lblTitle = new JLabel("QUẢN LÝ KHÁCH HÀNG");
        lblTitle.setFont(UIStyleConstants.TITLE_FONT);
        lblTitle.setForeground(UIStyleConstants.PRIMARY_COLOR);
        lblTitle.setHorizontalAlignment(JLabel.CENTER);

        btnBack = new JButton("Quay lại");
        btnBack.setFont(UIStyleConstants.BOLD_FONT);
        btnBack.setIcon(loadIcon(UIStyleConstants.BACK_ICON, 20, 20));
        btnBack.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBack.addActionListener(e -> {
            dispose();
            new AdminHomePage().setVisible(true); // Mở lại AdminHomePage
        });

        titlePanel.add(lblTitle, BorderLayout.CENTER);
        titlePanel.add(btnBack, BorderLayout.WEST);
    }

    private void createInputFormPanel() {
    inputFormPanel = new JPanel(new GridBagLayout());
    inputFormPanel.setBackground(UIStyleConstants.PANEL_BACKGROUND_COLOR);
    inputFormPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), "Thông tin khách hàng",
            javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
            javax.swing.border.TitledBorder.DEFAULT_POSITION,
            UIStyleConstants.BOLD_FONT, UIStyleConstants.PRIMARY_COLOR));

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(5, 10, 5, 10);
    gbc.anchor = GridBagConstraints.WEST;

    // ====== Hàng 0: Mã khách hàng, Tên khách hàng ======
    gbc.gridx = 0; 
    gbc.gridy = 0;
    JLabel lblMaKH = new JLabel("Mã khách hàng:");
    lblMaKH.setFont(UIStyleConstants.PLAIN_FONT);
    inputFormPanel.add(lblMaKH, gbc);

    gbc.gridx = 1;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.weightx = 1.0;
    txtMaKH = new JTextField(15);
    txtMaKH.setFont(UIStyleConstants.PLAIN_FONT);
    inputFormPanel.add(txtMaKH, gbc);

    gbc.gridx = 2;
    gbc.fill = GridBagConstraints.NONE;
    gbc.weightx = 0.0;
    JLabel lblTenKH = new JLabel("Tên khách hàng:");
    lblTenKH.setFont(UIStyleConstants.PLAIN_FONT);
    inputFormPanel.add(lblTenKH, gbc);

    gbc.gridx = 3;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.weightx = 1.0;
    txtTenKH = new JTextField(15);
    txtTenKH.setFont(UIStyleConstants.PLAIN_FONT);
    inputFormPanel.add(txtTenKH, gbc);

    // ====== Hàng 1: Số điện thoại, Email ======
    gbc.gridy = 1;
    gbc.gridx = 0;
    gbc.fill = GridBagConstraints.NONE;
    gbc.weightx = 0.0;
    JLabel lblSDT = new JLabel("Số điện thoại:");
    lblSDT.setFont(UIStyleConstants.PLAIN_FONT);
    inputFormPanel.add(lblSDT, gbc);

    gbc.gridx = 1;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.weightx = 1.0;
    txtSDT = new JTextField(15);
    txtSDT.setFont(UIStyleConstants.PLAIN_FONT);
    inputFormPanel.add(txtSDT, gbc);

    gbc.gridx = 2;
    gbc.fill = GridBagConstraints.NONE;
    gbc.weightx = 0.0;
    JLabel lblEmail = new JLabel("Email:");
    lblEmail.setFont(UIStyleConstants.PLAIN_FONT);
    inputFormPanel.add(lblEmail, gbc);

    gbc.gridx = 3;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.weightx = 1.0;
    txtEmail = new JTextField(15);
    txtEmail.setFont(UIStyleConstants.PLAIN_FONT);
    inputFormPanel.add(txtEmail, gbc);

    // ====== Hàng 2: Địa chỉ (rút ngắn lại chỉ 1 cell) ======
    gbc.gridy = 2;
    gbc.gridx = 0;
    gbc.fill = GridBagConstraints.NONE;
    gbc.weightx = 0.0;
    JLabel lblDiaChi = new JLabel("Địa chỉ:");
    lblDiaChi.setFont(UIStyleConstants.PLAIN_FONT);
    inputFormPanel.add(lblDiaChi, gbc);

    gbc.gridx = 1;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.weightx = 1.0;
    // Chỉ chiếm 1 cột (gridwidth = 1), không phải 3 cột như trước
    gbc.gridwidth = 1;
    txtDiaChi = new JTextField(15);
    txtDiaChi.setFont(UIStyleConstants.PLAIN_FONT);
    inputFormPanel.add(txtDiaChi, gbc);
    gbc.gridwidth = 1;  // Reset về mặc định (nếu có dòng khác sau này)

    // ====== Hàng 3: Loại khách hàng (vẫn thêm vào layout nhưng GIẤU đi) ======
    gbc.gridy = 3;
    gbc.gridx = 2;
    gbc.fill = GridBagConstraints.NONE;
    gbc.weightx = 0.0;
    JLabel lblLoaiKH = new JLabel("Loại khách hàng:");
    lblLoaiKH.setFont(UIStyleConstants.PLAIN_FONT);
    inputFormPanel.add(lblLoaiKH, gbc);
    // GIẤU luôn label và ô nhập
    lblLoaiKH.setVisible(false);

    gbc.gridx = 3;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.weightx = 1.0;
    txtLoaiKH = new JTextField(15);
    txtLoaiKH.setFont(UIStyleConstants.PLAIN_FONT);
    inputFormPanel.add(txtLoaiKH, gbc);
    // GIẤU luôn ô nhập
    txtLoaiKH.setVisible(false);

    // Kết thúc createInputFormPanel()
}



    private void createButtonPanel() {
        buttonPanel = new JPanel(new BorderLayout(10,10));
        buttonPanel.setBackground(UIStyleConstants.PANEL_BACKGROUND_COLOR);
        buttonPanel.setBorder(new EmptyBorder(10,15,15,15));

        JPanel searchComponentsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        searchComponentsPanel.setBackground(UIStyleConstants.PANEL_BACKGROUND_COLOR);
        JLabel lblSearch = new JLabel("Tìm kiếm (Mã KH):");
        lblSearch.setFont(UIStyleConstants.PLAIN_FONT);
        txtSearch = new JTextField(20);
        txtSearch.setFont(UIStyleConstants.PLAIN_FONT);
        btnSearch = new JButton("Tìm");
        btnSearch.setFont(UIStyleConstants.BOLD_FONT);
        btnSearch.setIcon(loadIcon(UIStyleConstants.SEARCH_ICON, 18, 18));
        btnSearch.setCursor(new Cursor(Cursor.HAND_CURSOR));
        searchComponentsPanel.add(lblSearch);
        searchComponentsPanel.add(txtSearch);
        searchComponentsPanel.add(btnSearch);
        btnSearch.addActionListener(e -> searchCustomerData());
        txtSearch.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) { if (e.getKeyCode() == KeyEvent.VK_ENTER) searchCustomerData(); }
        });

        JPanel actionButtonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionButtonsPanel.setBackground(UIStyleConstants.PANEL_BACKGROUND_COLOR);
        btnNew = createStyledButton("Nhập mới", UIStyleConstants.CLEAR_ICON);
        // btnAdd = createStyledButton("Thêm", UIStyleConstants.ADD_ICON); // Removed
        btnUpdate = createStyledButton("Cập nhật", UIStyleConstants.EDIT_ICON);
        btnDelete = createStyledButton("Xóa", UIStyleConstants.DELETE_ICON);
        actionButtonsPanel.add(btnNew);
        // actionButtonsPanel.add(btnAdd); // Removed
        actionButtonsPanel.add(btnUpdate); actionButtonsPanel.add(btnDelete);
        btnNew.addActionListener(e -> clearForm());
        // btnAdd.addActionListener(e -> addCustomer()); // Removed
        btnUpdate.addActionListener(e -> updateCustomer());
        btnDelete.addActionListener(e -> deleteCustomer());

        buttonPanel.add(searchComponentsPanel, BorderLayout.WEST);
        buttonPanel.add(actionButtonsPanel, BorderLayout.EAST);
    }
    
    private JButton createStyledButton(String text, String iconPath) {
        JButton button = new JButton(text);
        button.setFont(UIStyleConstants.BOLD_FONT);
        button.setIcon(loadIcon(iconPath, 18, 18));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setFocusPainted(false);
        return button;
    }

    private void createTablePanel() {
    tablePanel = new JPanel(new BorderLayout());
    tablePanel.setBackground(UIStyleConstants.PANEL_BACKGROUND_COLOR);
    tablePanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), "Danh sách khách hàng",
            javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
            javax.swing.border.TitledBorder.DEFAULT_POSITION,
            UIStyleConstants.BOLD_FONT, UIStyleConstants.PRIMARY_COLOR));
    tablePanel.setPreferredSize(new Dimension(this.getWidth() - 40, 350));

    // Tạo model với 7 cột (giữ cột "Loại KH" index 6 để tránh chỉnh SQL quá nhiều)
    String[] columns = {"STT", "Mã KH", "Tên KH", "SĐT", "Email", "Địa Chỉ", "Loại KH"};
    tableModel = new DefaultTableModel(columns, 0) {
        @Override 
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    customerTable = new JTable(tableModel);
    customerTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    customerTable.setRowHeight(28);
    customerTable.setFont(UIStyleConstants.PLAIN_FONT);
    customerTable.setGridColor(Color.LIGHT_GRAY);

    JTableHeader header = customerTable.getTableHeader();
    header.setFont(UIStyleConstants.TABLE_HEADER_FONT);
    header.setBackground(UIStyleConstants.PRIMARY_COLOR);
    header.setForeground(Color.WHITE);
    header.setReorderingAllowed(false);

    // ====== Ẩn cột “Loại KH” index = 6 ======
    // Khi count > 6, tức có cột thứ 7 (index 6) tồn tại, ta cho width = 0
    if (customerTable.getColumnModel().getColumnCount() > 6) {
        TableColumn colLoaiKH = customerTable.getColumnModel().getColumn(6);
        colLoaiKH.setMinWidth(0);
        colLoaiKH.setMaxWidth(0);
        colLoaiKH.setPreferredWidth(0);
        // Nếu muốn xóa hẳn header column, có thể làm:
        // customerTable.getColumnModel().removeColumn(colLoaiKH);
    }

    // Khi user chọn 1 row, gọi displaySelectedCustomer()
    customerTable.getSelectionModel().addListSelectionListener(e -> {
        if (!e.getValueIsAdjusting() && customerTable.getSelectedRow() != -1) {
            displaySelectedCustomer();
        }
    });

    JScrollPane scrollPane = new JScrollPane(customerTable);
    tablePanel.add(scrollPane, BorderLayout.CENTER);
}



    private void executeDataLoad(String query, String searchTerm) {
    tableModel.setRowCount(0);

    SwingWorker<List<Object[]>, Void> worker = new SwingWorker<>() {
        @Override
        protected List<Object[]> doInBackground() throws Exception {
            List<Object[]> rows = new ArrayList<>();
            try (Connection conn = ConnectionUtils.getMyConnection();
                 PreparedStatement pstmt = conn.prepareStatement(query)) {
                if (searchTerm != null && !searchTerm.isEmpty()) {
                    pstmt.setString(1, "%" + searchTerm + "%");
                }
                try (ResultSet rs = pstmt.executeQuery()) {
                    int stt = 1;
                    while (rs.next()) {
                        // ----- Chỉ lấy 5 cột thực sự tồn tại trong DB + 1 giá trị rỗng cho LOAIKH -----
                        rows.add(new Object[]{
                            stt++,
                            rs.getString("MAKH"),
                            rs.getString("TENKH"),
                            rs.getString("SDT"),
                            rs.getString("EMAIL"),
                            rs.getString("DIACHI"),
                            ""  // Thay vì rs.getString("LOAIKH"), vì cột LOAIKH đã bị xóa ở DB
                        });
                    }
                }
            }
            return rows;
        }

        @Override
        protected void done() {
            try {
                List<Object[]> resultRows = get();
                for (Object[] row : resultRows) {
                    tableModel.addRow(row);
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(QuanLyKhachHang.this,
                        "Lỗi khi tải dữ liệu: " + e.getMessage(),
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    };
    worker.execute();
}


    private void loadCustomerData() {
    // Vẫn để SELECT * vì executeDataLoad() chỉ truy xuất đúng 5 cột + bỏ qua LOAIKH
    executeDataLoad("SELECT * FROM KHACHHANG ORDER BY MAKH", null);
}

    private void searchCustomerData() {
    String key = txtSearch.getText().trim();
    if (key.isEmpty()) {
        loadCustomerData();
    } else {
        executeDataLoad(
            "SELECT * FROM KHACHHANG WHERE MAKH LIKE ? ORDER BY MAKH",
            key
        );
    }
}

    private void clearForm() {
        txtMaKH.setText(""); txtMaKH.setEditable(true);
        txtTenKH.setText(""); txtSDT.setText(""); txtEmail.setText("");
        txtDiaChi.setText(""); txtLoaiKH.setText("");
        txtMaKH.requestFocus(); customerTable.clearSelection();
    }

    private boolean validateForm() { // Removed isUpdate parameter
        String maKH = txtMaKH.getText().trim();
        String tenKH = txtTenKH.getText().trim();
        String loaiKH = txtLoaiKH.getText().trim();
        String email = txtEmail.getText().trim();

        if (maKH.isEmpty()) { JOptionPane.showMessageDialog(this, "Vui lòng nhập mã khách hàng!", "Lỗi", JOptionPane.ERROR_MESSAGE); txtMaKH.requestFocus(); return false; }
        // Removed: if (!isUpdate && maKHExists(maKH)) { JOptionPane.showMessageDialog(this, "Mã khách hàng đã tồn tại!", "Lỗi", JOptionPane.ERROR_MESSAGE); txtMaKH.requestFocus(); return false; }
        if (tenKH.isEmpty()) { JOptionPane.showMessageDialog(this, "Vui lòng nhập tên khách hàng!", "Lỗi", JOptionPane.ERROR_MESSAGE); txtTenKH.requestFocus(); return false; }
        if (loaiKH.isEmpty()) { JOptionPane.showMessageDialog(this, "Vui lòng nhập loại khách hàng!", "Lỗi", JOptionPane.ERROR_MESSAGE); txtLoaiKH.requestFocus(); return false; }
        if (!email.isEmpty() && !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) { JOptionPane.showMessageDialog(this, "Email không hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE); txtEmail.requestFocus(); return false; }
        return true;
    }
    
    private boolean maKHExists(String maKH) {
        try (Connection conn = ConnectionUtils.getMyConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT COUNT(*) FROM KHACHHANG WHERE MAKH = ?")) {
            pstmt.setString(1, maKH);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi kiểm tra Mã KH: " + e.getMessage(), "Lỗi Cơ sở dữ liệu", JOptionPane.ERROR_MESSAGE);
        }
        return false; 
    }

    // Removed addCustomer() method

    private void updateCustomer() {
        if (customerTable.getSelectedRow() == -1) { JOptionPane.showMessageDialog(this, "Vui lòng chọn khách hàng cần cập nhật!", "Thông báo", JOptionPane.WARNING_MESSAGE); return; }
        if (!validateForm()) return; // Updated call
        String originalMaKH = (String) tableModel.getValueAt(customerTable.getSelectedRow(), 1);
        String maKH = txtMaKH.getText().trim(); String tenKH = txtTenKH.getText().trim();
        String sdt = txtSDT.getText().trim(); String email = txtEmail.getText().trim();
        String diaChi = txtDiaChi.getText().trim(); String loaiKH = txtLoaiKH.getText().trim();
        if (!maKH.equals(originalMaKH) && maKHExists(maKH)) { JOptionPane.showMessageDialog(this, "Mã khách hàng mới '" + maKH + "' đã tồn tại cho một khách hàng khác.", "Lỗi", JOptionPane.ERROR_MESSAGE); txtMaKH.requestFocus(); return; }
        String sql = "UPDATE KHACHHANG SET MAKH = ?, TENKH = ?, SDT = ?, DIACHI = ?, EMAIL = ?, LOAIKH = ? WHERE MAKH = ?";
        try (Connection conn = ConnectionUtils.getMyConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, maKH); pstmt.setString(2, tenKH);
            pstmt.setString(3, sdt.isEmpty() ? null : sdt); pstmt.setString(4, diaChi.isEmpty() ? null : diaChi);
            pstmt.setString(5, email.isEmpty() ? null : email); pstmt.setString(6, loaiKH);
            pstmt.setString(7, originalMaKH);
            if (pstmt.executeUpdate() > 0) {
                JOptionPane.showMessageDialog(this, "Cập nhật khách hàng thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                clearForm(); loadCustomerData();
            } else JOptionPane.showMessageDialog(this, "Cập nhật khách hàng thất bại hoặc không tìm thấy khách hàng với mã gốc.", "Lỗi", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException | ClassNotFoundException e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi cập nhật khách hàng: " + e.getMessage(), "Lỗi SQL", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void deleteCustomer() {
        if (customerTable.getSelectedRow() == -1) { JOptionPane.showMessageDialog(this, "Vui lòng chọn khách hàng cần xóa!", "Thông báo", JOptionPane.WARNING_MESSAGE); return; }
        if (JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa khách hàng này?", "Xác nhận xóa", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            String maKH = (String) tableModel.getValueAt(customerTable.getSelectedRow(), 1);
            String sql = "DELETE FROM KHACHHANG WHERE MAKH = ?";
            try (Connection conn = ConnectionUtils.getMyConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, maKH);
                if (pstmt.executeUpdate() > 0) {
                    JOptionPane.showMessageDialog(this, "Xóa khách hàng thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                    clearForm(); loadCustomerData();
                } else JOptionPane.showMessageDialog(this, "Xóa khách hàng thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            } catch (SQLException | ClassNotFoundException e) {
                JOptionPane.showMessageDialog(this, "Lỗi khi xóa khách hàng: " + e.getMessage(), "Lỗi SQL", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }

    private void displaySelectedCustomer() {
        int selectedRow = customerTable.getSelectedRow();
        if (selectedRow != -1) {
            txtMaKH.setText((String) tableModel.getValueAt(selectedRow, 1));
            txtMaKH.setEditable(false); 
            txtTenKH.setText((String) tableModel.getValueAt(selectedRow, 2));
            txtSDT.setText(tableModel.getValueAt(selectedRow, 3) != null ? (String) tableModel.getValueAt(selectedRow, 3) : "");
            txtEmail.setText(tableModel.getValueAt(selectedRow, 4) != null ? (String) tableModel.getValueAt(selectedRow, 4) : "");
            txtDiaChi.setText(tableModel.getValueAt(selectedRow, 5) != null ? (String) tableModel.getValueAt(selectedRow, 5) : "");
            txtLoaiKH.setText(tableModel.getValueAt(selectedRow, 6) != null ? (String) tableModel.getValueAt(selectedRow, 6) : "");
        }
    }

    public static void main(String args[]) {
        // Yêu cầu FlatLaf không sử dụng visual padding liên quan đến MigLayout
        System.setProperty("flatlaf.useVisualPadding", "false");
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (UnsupportedLookAndFeelException ex) {
            System.err.println("Failed to initialize LaF: " + ex.getMessage());
        }

        java.awt.EventQueue.invokeLater(() -> {
            new QuanLyKhachHang().setVisible(true);
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
