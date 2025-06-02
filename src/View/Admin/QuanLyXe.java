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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class QuanLyXe extends javax.swing.JFrame {
    private static class UIStyleConstants {
        public static final Color PRIMARY_COLOR = new Color(0, 120, 215);
        public static final Color BACKGROUND_COLOR = new Color(240, 245, 250);
        public static final Color PANEL_BACKGROUND_COLOR = Color.WHITE;
        public static final Font BOLD_FONT = new Font("Segoe UI", Font.BOLD, 14);
        public static final Font PLAIN_FONT = new Font("Segoe UI", Font.PLAIN, 14);
        public static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 28);
        public static final Font TABLE_HEADER_FONT = new Font("Segoe UI", Font.BOLD, 14);

        public static final String ICON_PATH_PREFIX = "/icons/";
        public static final String BACK_ICON = ICON_PATH_PREFIX + "back_arrow.png";
        public static final String ADD_ICON = ICON_PATH_PREFIX + "add.png";
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

    private JTable vehicleTable;
    private DefaultTableModel tableModel;

    // Form components
    private JTextField txtBienSo;
    private JComboBox<String> cboTenLoaiXe;
    private JTextField txtSearch;

    // Buttons
    private JButton btnNew;
    private JButton btnAdd;
    private JButton btnUpdate;
    private JButton btnDelete;
    private JButton btnBack;
    private JButton btnSearch;

    public QuanLyXe() {
        System.setProperty("flatlaf.useVisualPadding", "false");
        applyLookAndFeel();
        initComponentsUI();
        loadVehicleData();
    }

    private void applyLookAndFeel() {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
            // SwingUtilities.updateComponentTreeUI(this); // Có thể cần nếu L&F thay đổi sau khi UI đã vẽ
        } catch (UnsupportedLookAndFeelException ex) {
            System.err.println("Failed to initialize LaF: " + ex.getMessage());
        }
    }

    private void initComponentsUI() {
        setTitle("Quản Lý Xe");
        setSize(1200, 700); // Kích thước có thể điều chỉnh cho phù hợp
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

        JPanel topSectionPanel = new JPanel(new BorderLayout(10, 10));
        topSectionPanel.setBackground(UIStyleConstants.BACKGROUND_COLOR);
        // inputFormPanel cần ít không gian hơn các form khác
        // topSectionPanel.setPreferredSize(new Dimension(getWidth(), 150)); // Điều chỉnh chiều cao cho form và nút
        topSectionPanel.add(inputFormPanel, BorderLayout.CENTER); // Hoặc NORTH nếu muốn table chiếm nhiều hơn
        topSectionPanel.add(buttonPanel, BorderLayout.SOUTH);


        mainPanel.add(titlePanel, BorderLayout.NORTH);
        mainPanel.add(topSectionPanel, BorderLayout.CENTER);
        mainPanel.add(tablePanel, BorderLayout.SOUTH); // Sẽ chiếm không gian còn lại

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

        JLabel lblTitle = new JLabel("QUẢN LÝ XE");
        lblTitle.setFont(UIStyleConstants.TITLE_FONT);
        lblTitle.setForeground(UIStyleConstants.PRIMARY_COLOR);
        lblTitle.setHorizontalAlignment(JLabel.CENTER);

        btnBack = new JButton("Quay lại");
        btnBack.setFont(UIStyleConstants.BOLD_FONT);
        btnBack.setIcon(loadIcon(UIStyleConstants.BACK_ICON, 20, 20));
        btnBack.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBack.addActionListener(e -> {
            dispose();
            new AdminHomePage().setVisible(true);
        });

        titlePanel.add(lblTitle, BorderLayout.CENTER);
        titlePanel.add(btnBack, BorderLayout.WEST);
    }

    private void createInputFormPanel() {
        inputFormPanel = new JPanel(new GridBagLayout());
        inputFormPanel.setBackground(UIStyleConstants.PANEL_BACKGROUND_COLOR);
        inputFormPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Thông tin xe",
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION,
                UIStyleConstants.BOLD_FONT, UIStyleConstants.PRIMARY_COLOR));
        // inputFormPanel.setPreferredSize(new Dimension(this.getWidth() - 40, 120)); // Giảm chiều cao form


        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 15, 10, 15); // Tăng khoảng đệm
        gbc.anchor = GridBagConstraints.WEST;

        // Hàng 1: Biển số xe
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel lblBienSo = new JLabel("Biển số xe:");
        lblBienSo.setFont(UIStyleConstants.PLAIN_FONT);
        inputFormPanel.add(lblBienSo, gbc);

        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 0.3; // Phân bổ weight
        txtBienSo = new JTextField(20); // Kích thước gợi ý
        txtBienSo.setFont(UIStyleConstants.PLAIN_FONT);
        inputFormPanel.add(txtBienSo, gbc);

        // Hàng 1: Tên loại xe (cùng hàng với Biển số)
        gbc.gridx = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0; // Reset weight cho label
        JLabel lblTenLoaiXe = new JLabel("Tên loại xe:");
        lblTenLoaiXe.setFont(UIStyleConstants.PLAIN_FONT);
        inputFormPanel.add(lblTenLoaiXe, gbc);
        
        gbc.gridx = 3; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 0.7; // Phân bổ weight
        cboTenLoaiXe = new JComboBox<>(new String[]{"Xe máy", "Ô tô"}); // Giá trị mẫu
        cboTenLoaiXe.setFont(UIStyleConstants.PLAIN_FONT);
        inputFormPanel.add(cboTenLoaiXe, gbc);
        
        // Thêm một dummy component để đẩy các field lên trên nếu panel quá rộng
        gbc.gridy = 1;
        gbc.gridx = 0;
        gbc.weighty = 1.0; // Đẩy lên trên
        inputFormPanel.add(new JLabel(), gbc);
    }

    private void createButtonPanel() {
        buttonPanel = new JPanel(new BorderLayout(10, 10));
        buttonPanel.setBackground(UIStyleConstants.PANEL_BACKGROUND_COLOR);
        buttonPanel.setBorder(new EmptyBorder(10, 15, 15, 15));

        JPanel searchComponentsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        searchComponentsPanel.setBackground(UIStyleConstants.PANEL_BACKGROUND_COLOR);
        JLabel lblSearch = new JLabel("Tìm kiếm (Biển số):");
        lblSearch.setFont(UIStyleConstants.PLAIN_FONT);
        searchComponentsPanel.add(lblSearch);
        txtSearch = new JTextField(20);
        txtSearch.setFont(UIStyleConstants.PLAIN_FONT);
        searchComponentsPanel.add(txtSearch);
        btnSearch = createStyledButton("Tìm", UIStyleConstants.SEARCH_ICON);
        searchComponentsPanel.add(btnSearch);
        btnSearch.addActionListener(e -> searchVehicleData());
        txtSearch.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) { if (e.getKeyCode() == KeyEvent.VK_ENTER) searchVehicleData(); }
        });

        JPanel actionButtonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionButtonsPanel.setBackground(UIStyleConstants.PANEL_BACKGROUND_COLOR);
        btnNew = createStyledButton("Nhập mới", UIStyleConstants.CLEAR_ICON);
        btnAdd = createStyledButton("Thêm", UIStyleConstants.ADD_ICON);
        btnUpdate = createStyledButton("Cập nhật", UIStyleConstants.EDIT_ICON);
        btnDelete = createStyledButton("Xóa", UIStyleConstants.DELETE_ICON);
        actionButtonsPanel.add(btnNew); actionButtonsPanel.add(btnAdd);
        actionButtonsPanel.add(btnUpdate); actionButtonsPanel.add(btnDelete);
        btnNew.addActionListener(e -> clearForm());
        btnAdd.addActionListener(e -> addVehicle());
        btnUpdate.addActionListener(e -> updateVehicle());
        btnDelete.addActionListener(e -> deleteVehicle());

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
        tablePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Danh sách xe",
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION,
                UIStyleConstants.BOLD_FONT, UIStyleConstants.PRIMARY_COLOR));
        tablePanel.setPreferredSize(new Dimension(this.getWidth() - 40, 400)); // Điều chỉnh chiều cao bảng

        String[] columns = {"STT", "Biển số xe", "Tên loại xe"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        vehicleTable = new JTable(tableModel);
        vehicleTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        vehicleTable.setRowHeight(28);
        vehicleTable.setFont(UIStyleConstants.PLAIN_FONT);
        vehicleTable.setGridColor(Color.LIGHT_GRAY);
        JTableHeader header = vehicleTable.getTableHeader();
        header.setFont(UIStyleConstants.TABLE_HEADER_FONT);
        header.setBackground(UIStyleConstants.PRIMARY_COLOR);
        header.setForeground(Color.WHITE);
        header.setReorderingAllowed(false);
        vehicleTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && vehicleTable.getSelectedRow() != -1) displaySelectedVehicle();
        });
        JScrollPane scrollPane = new JScrollPane(vehicleTable);
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
                            rows.add(new Object[]{
                                    stt++, rs.getString("BIENSO"), rs.getString("TENLOAIXE")
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
                    for (Object[] row : resultRows) tableModel.addRow(row);
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(QuanLyXe.this, "Lỗi khi tải dữ liệu xe: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void loadVehicleData() {
        executeDataLoad("SELECT * FROM XE ORDER BY BIENSO", null);
    }

    private void searchVehicleData() {
        String key = txtSearch.getText().trim();
        if (key.isEmpty()) loadVehicleData();
        else executeDataLoad("SELECT * FROM XE WHERE BIENSO LIKE ? ORDER BY BIENSO", key);
    }

    private void clearForm() {
        txtBienSo.setText("");
        txtBienSo.setEditable(true);
        if (cboTenLoaiXe.getItemCount() > 0) {
            cboTenLoaiXe.setSelectedIndex(0);
        }
        txtBienSo.requestFocus();
        vehicleTable.clearSelection();
    }

    private boolean validateForm(boolean isUpdate) {
        String bienSo = txtBienSo.getText().trim();
        if (bienSo.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập biển số xe!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            txtBienSo.requestFocus();
            return false;
        }

        if (!isUpdate && bienSoExists(bienSo)) {
            JOptionPane.showMessageDialog(this, "Biển số xe đã tồn tại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            txtBienSo.requestFocus();
            return false;
        }
        
        // Ví dụ validate định dạng biển số (có thể tùy chỉnh cho phù hợp với Việt Nam)
        // String licensePlateRegex = "^[0-9]{2}[A-Z]-\\d{3,5}$"; // Vd: 51A-12345 hoặc 29H-123.45 (cần regex chính xác hơn)
        // if (!bienSo.matches(licensePlateRegex)) {
        // JOptionPane.showMessageDialog(this, "Biển số xe không đúng định dạng (VD: 51A-12345).", "Lỗi", JOptionPane.ERROR_MESSAGE);
        // txtBienSo.requestFocus();
        // return false;
        // }
        return true;
    }

    private boolean bienSoExists(String bienSo) {
        try (Connection conn = ConnectionUtils.getMyConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT COUNT(*) FROM XE WHERE BIENSO = ?")) {
            pstmt.setString(1, bienSo);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi kiểm tra Biển số: " + e.getMessage(), "Lỗi Cơ sở dữ liệu", JOptionPane.ERROR_MESSAGE);
        }
        return false;
    }

    private void addVehicle() {
        if (!validateForm(false)) return;
        String bienSo = txtBienSo.getText().trim();
        String tenLoaiXe = (String) cboTenLoaiXe.getSelectedItem();
        String sql = "INSERT INTO XE (BIENSO, TENLOAIXE) VALUES (?, ?)";
        try (Connection conn = ConnectionUtils.getMyConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, bienSo);
            pstmt.setString(2, tenLoaiXe);
            if (pstmt.executeUpdate() > 0) {
                JOptionPane.showMessageDialog(this, "Thêm xe thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                clearForm(); loadVehicleData();
            } else JOptionPane.showMessageDialog(this, "Thêm xe thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException | ClassNotFoundException e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi thêm xe: " + e.getMessage(), "Lỗi SQL", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void updateVehicle() {
        if (vehicleTable.getSelectedRow() == -1) { JOptionPane.showMessageDialog(this, "Vui lòng chọn xe để cập nhật!", "Cảnh báo", JOptionPane.WARNING_MESSAGE); return; }
        
        String originalBienSo = (String) tableModel.getValueAt(vehicleTable.getSelectedRow(), 1);
        String currentBienSoInForm = txtBienSo.getText().trim();

        // Chỉ validate nếu biển số trong form khác với biển số gốc VÀ khác với biển số đang được chọn (để cho phép chỉ sửa loại xe)
        if (!currentBienSoInForm.equals(originalBienSo)) {
            if (!validateForm(true)) return; // Validate form, bao gồm kiểm tra trùng nếu biển số thay đổi
        } else { // Nếu biển số không thay đổi, không cần check trùng biển số, chỉ cần validate các trường khác (nếu có)
            if (currentBienSoInForm.isEmpty()) {
                 JOptionPane.showMessageDialog(this, "Vui lòng nhập biển số xe!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                 txtBienSo.requestFocus();
                 return;
            }
        }
        
        String bienSo = currentBienSoInForm;
        String tenLoaiXe = (String) cboTenLoaiXe.getSelectedItem();

        String sql = "UPDATE XE SET BIENSO = ?, TENLOAIXE = ? WHERE BIENSO = ?";
        try (Connection conn = ConnectionUtils.getMyConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, bienSo);
            pstmt.setString(2, tenLoaiXe);
            pstmt.setString(3, originalBienSo);
            if (pstmt.executeUpdate() > 0) {
                JOptionPane.showMessageDialog(this, "Cập nhật xe thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                clearForm(); loadVehicleData();
            } else JOptionPane.showMessageDialog(this, "Cập nhật xe thất bại hoặc không tìm thấy xe.", "Lỗi", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException | ClassNotFoundException e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi cập nhật xe: " + e.getMessage(), "Lỗi SQL", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void deleteVehicle() {
        if (vehicleTable.getSelectedRow() == -1) { JOptionPane.showMessageDialog(this, "Vui lòng chọn xe để xóa!", "Cảnh báo", JOptionPane.WARNING_MESSAGE); return; }
        if (JOptionPane.showConfirmDialog(this, "Bạn có chắc muốn xóa xe này?", "Xác nhận xóa", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            String bienSo = (String) tableModel.getValueAt(vehicleTable.getSelectedRow(), 1);
            try (Connection conn = ConnectionUtils.getMyConnection(); PreparedStatement pstmt = conn.prepareStatement("DELETE FROM XE WHERE BIENSO = ?")) {
                pstmt.setString(1, bienSo);
                if (pstmt.executeUpdate() > 0) {
                    JOptionPane.showMessageDialog(this, "Xóa xe thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                    clearForm(); loadVehicleData();
                } else JOptionPane.showMessageDialog(this, "Xóa xe thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            } catch (SQLException | ClassNotFoundException e) {
                JOptionPane.showMessageDialog(this, "Lỗi khi xóa xe: " + e.getMessage(), "Lỗi SQL", JOptionPane.ERROR_MESSAGE);
                // Kiểm tra ràng buộc khóa ngoại nếu có lỗi liên quan
                if (e.getMessage().toLowerCase().contains("integrity constraint") || e.getMessage().toLowerCase().contains("foreign key")) {
                     JOptionPane.showMessageDialog(this, "Không thể xóa xe này do có dữ liệu liên quan (ví dụ: xe đang ra vào, lịch sử bảo dưỡng,...).", "Lỗi ràng buộc", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private void displaySelectedVehicle() {
        int selectedRow = vehicleTable.getSelectedRow();
        if (selectedRow != -1) {
            txtBienSo.setText((String) tableModel.getValueAt(selectedRow, 1));
            txtBienSo.setEditable(false); // Không cho sửa biển số trực tiếp khi chọn, việc thay đổi biển số sẽ qua logic update
            String tenLoaiXe = (String) tableModel.getValueAt(selectedRow, 2);
            cboTenLoaiXe.setSelectedItem(tenLoaiXe);
        }
    }

    public static void main(String args[]) {
        System.setProperty("flatlaf.useVisualPadding", "false");
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (UnsupportedLookAndFeelException ex) {
            System.err.println("Failed to initialize LaF: " + ex.getMessage());
        }
        java.awt.EventQueue.invokeLater(() -> {
            new QuanLyXe().setVisible(true);
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
