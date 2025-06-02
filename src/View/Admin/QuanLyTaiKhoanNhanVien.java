package View.Admin;

import ConnectDB.ConnectionOracle; // Đổi sang ConnectionOracle
import Process.RoleGroupConstants; // Thêm RoleGroupConstants
import View.DangKyTaiKhoanNhanVien;
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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

public class QuanLyTaiKhoanNhanVien extends javax.swing.JFrame {
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

    private JTable accountTable;
    private DefaultTableModel tableModel;

    private JTextField txtUsername;
    private JTextField txtFullName; // Cho phép sửa
    private JTextField txtEmail;    // Cho phép sửa
    private JTextField txtMaNhanVienDisplay;
    private JPasswordField txtNewPassword;
    // private JComboBox<String> cmbAccountStatus; // REMOVED
    private JTextField txtRoleDisplay;

    private JTextField txtSearch;

    private JButton btnNew;
    private JButton btnAddAccount;
    private JButton btnUpdate;
    private JButton btnDelete;
    private JButton btnBack;
    private JButton btnSearch;

    private int selectedAccountId = -1;
    private int selectedUserId = -1; // Lưu USER_ID của dòng được chọn để cập nhật USERS table

    private static final Pattern EMAIL_PATTERN = Pattern.compile( // Thêm pattern cho email
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$");


    public QuanLyTaiKhoanNhanVien() {
        System.setProperty("flatlaf.useVisualPadding", "false");
        applyLookAndFeel();
        initComponentsUI();
        loadAccountData("");
    }

    private void applyLookAndFeel() {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (UnsupportedLookAndFeelException ex) {
            System.err.println("Failed to initialize LaF: " + ex.getMessage());
        }
    }

    private void initComponentsUI() {
        setTitle("Quản Lý Tài Khoản Nhân Viên");
        setSize(1350, 750);
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

        JLabel lblTitle = new JLabel("QUẢN LÝ TÀI KHOẢN NHÂN VIÊN");
        lblTitle.setFont(UIStyleConstants.TITLE_FONT);
        lblTitle.setForeground(UIStyleConstants.PRIMARY_COLOR);
        lblTitle.setHorizontalAlignment(JLabel.CENTER);

        btnBack = new JButton("Quay lại");
        btnBack.setFont(UIStyleConstants.BOLD_FONT);
        btnBack.setIcon(loadIcon(UIStyleConstants.BACK_ICON, 20, 20));
        btnBack.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBack.addActionListener(e -> {
            dispose();
            // new AdminHomePage().setVisible(true); // Mở lại AdminHomePage
             SwingUtilities.invokeLater(() -> new AdminHomePage().setVisible(true));
        });

        titlePanel.add(lblTitle, BorderLayout.CENTER);
        titlePanel.add(btnBack, BorderLayout.WEST);
    }

    private void createInputFormPanel() {
        inputFormPanel = new JPanel(new GridBagLayout());
        inputFormPanel.setBackground(UIStyleConstants.PANEL_BACKGROUND_COLOR);
        inputFormPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Thông tin tài khoản nhân viên",
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION,
                UIStyleConstants.BOLD_FONT, UIStyleConstants.PRIMARY_COLOR));
        inputFormPanel.setPreferredSize(new Dimension(0, 220)); // You might want to adjust height after removing a row

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 12, 8, 12);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0;
        inputFormPanel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        txtUsername = new JTextField(20);
        txtUsername.setFont(UIStyleConstants.PLAIN_FONT);
        txtUsername.setEditable(false);
        inputFormPanel.add(txtUsername, gbc);

        gbc.gridx = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0;
        inputFormPanel.add(new JLabel("Họ và tên:"), gbc);
        gbc.gridx = 3; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        txtFullName = new JTextField(20);
        txtFullName.setFont(UIStyleConstants.PLAIN_FONT);
        txtFullName.setEditable(true); // Cho phép sửa họ tên
        inputFormPanel.add(txtFullName, gbc);

        gbc.gridy++;
        gbc.gridx = 0; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0;
        inputFormPanel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        txtEmail = new JTextField(20);
        txtEmail.setFont(UIStyleConstants.PLAIN_FONT);
        txtEmail.setEditable(true); // Cho phép sửa email
        inputFormPanel.add(txtEmail, gbc);

        gbc.gridx = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0;
        inputFormPanel.add(new JLabel("Mã nhân viên:"), gbc);
        gbc.gridx = 3; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        txtMaNhanVienDisplay = new JTextField(20);
        txtMaNhanVienDisplay.setFont(UIStyleConstants.PLAIN_FONT);
        txtMaNhanVienDisplay.setEditable(false);
        inputFormPanel.add(txtMaNhanVienDisplay, gbc);

        gbc.gridy++;
        gbc.gridx = 0; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0;
        inputFormPanel.add(new JLabel("Mật khẩu mới:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        txtNewPassword = new JPasswordField(20);
        txtNewPassword.setFont(UIStyleConstants.PLAIN_FONT);
        inputFormPanel.add(txtNewPassword, gbc);

        // REMOVED "Trạng thái TK" Label and ComboBox
        // gbc.gridx = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0;
        // inputFormPanel.add(new JLabel("Trạng thái TK:"), gbc);
        // gbc.gridx = 3; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        // cmbAccountStatus = new JComboBox<>(new String[]{"ACTIVE", "INACTIVE"});
        // cmbAccountStatus.setFont(UIStyleConstants.PLAIN_FONT);
        // inputFormPanel.add(cmbAccountStatus, gbc);

        gbc.gridy++; // This gbc.gridy++ might need adjustment if the above row was fully cleared and you want to use that row
                     // However, since "Mật khẩu mới" is still on the previous gbc.gridy, this new gbc.gridy++ is for "Vai trò" on a new line.
                     // If "Mật khẩu mới" was alone on its line and took full width, then this gbc.gridy could be the same as "Mật khẩu mới" gbc.gridy
                     // For now, keeping "Vai trò" on its own line below "Mật khẩu mới" as per original structure.
        gbc.gridx = 0; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0;
        inputFormPanel.add(new JLabel("Vai trò:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        txtRoleDisplay = new JTextField(20);
        txtRoleDisplay.setFont(UIStyleConstants.PLAIN_FONT);
        txtRoleDisplay.setEditable(false);
        inputFormPanel.add(txtRoleDisplay, gbc);
        
        // Optionally, to make "Mật khẩu mới" take up the space previously used by "Trạng thái TK"
        // You would need to adjust the gbc for txtNewPassword after it's added:
        // GridBagConstraints gbcForPass = ((GridBagLayout)inputFormPanel.getLayout()).getConstraints(txtNewPassword);
        // gbcForPass.gridwidth = 3; // To span across the label and field of "Trạng thái TK"
        // ((GridBagLayout)inputFormPanel.getLayout()).setConstraints(txtNewPassword, gbcForPass);
        // For now, I'm keeping it simple by just removing the components.
    }

    private void createButtonPanel() {
        buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10)); // Sử dụng FlowLayout.LEFT và thêm khoảng cách
        buttonPanel.setBackground(UIStyleConstants.PANEL_BACKGROUND_COLOR);
        buttonPanel.setBorder(new EmptyBorder(5, 0, 5, 0)); // Điều chỉnh padding nếu cần

        JLabel searchLabel = new JLabel("Tìm kiếm:"); // Label ngắn gọn hơn
        searchLabel.setFont(UIStyleConstants.PLAIN_FONT);
        buttonPanel.add(searchLabel);

        txtSearch = new JTextField(18); // **THU NGẮN Ô TÌM KIẾM** (ví dụ: 18 columns)
        txtSearch.setFont(UIStyleConstants.PLAIN_FONT);
        // Đặt kích thước tối đa để nó không giãn ra quá nhiều nếu FlowLayout có không gian
        txtSearch.setMaximumSize(new Dimension(250, txtSearch.getPreferredSize().height));
        buttonPanel.add(txtSearch);

        btnSearch = createStyledButton("Tìm", UIStyleConstants.SEARCH_ICON);
        btnSearch.addActionListener(e -> searchAccountData());
        txtSearch.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    searchAccountData();
                }
            }
        });
        buttonPanel.add(btnSearch);

        // Thêm một khoảng đệm giữa nút tìm kiếm và các nút chức năng khác
        buttonPanel.add(Box.createHorizontalStrut(20)); // Điều chỉnh độ rộng của khoảng đệm

        btnNew = createStyledButton("Làm mới Form", UIStyleConstants.CLEAR_ICON);
        btnAddAccount = createStyledButton("Thêm Tài Khoản NV", UIStyleConstants.ADD_ICON);
        btnUpdate = createStyledButton("Cập nhật TK", UIStyleConstants.EDIT_ICON);
        btnDelete = createStyledButton("Vô hiệu hóa TK", UIStyleConstants.DELETE_ICON);

        buttonPanel.add(btnNew);
        buttonPanel.add(btnAddAccount);
        buttonPanel.add(btnUpdate);
        buttonPanel.add(btnDelete);

        // Gán sự kiện cho các nút (đã có ở code trước, giữ nguyên)
        btnNew.addActionListener(e -> clearForm());
        btnAddAccount.addActionListener(e -> openDangKyTaiKhoanNhanVien());
        btnUpdate.addActionListener(e -> updateAccount());
        btnDelete.addActionListener(e -> deleteAccount());
    }

    private void openDangKyTaiKhoanNhanVien() {
        new DangKyTaiKhoanNhanVien(this).setVisible(true);
        this.setVisible(false);
    }

    private JButton createStyledButton(String text, String iconPath) {
        JButton button = new JButton(text);
        button.setFont(UIStyleConstants.BOLD_FONT);
        if (iconPath != null && !iconPath.isEmpty()) {
            button.setIcon(loadIcon(iconPath, 18, 18));
        }
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setFocusPainted(false);
        button.setMargin(new Insets(5, 10, 5, 10));
        return button;
    }

    private void createTablePanel() {
        tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(UIStyleConstants.PANEL_BACKGROUND_COLOR);
        tablePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Danh sách tài khoản",
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION,
                UIStyleConstants.BOLD_FONT, UIStyleConstants.PRIMARY_COLOR));
        tablePanel.setPreferredSize(new Dimension(this.getWidth() - 40, 350));

        String[] columns = {"STT", "Account ID", "User ID", "Username", "Họ Tên", "Email", "Vai trò", "Trạng thái TK", "Mã Nhân Viên"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        accountTable = new JTable(tableModel);
        accountTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        accountTable.setRowHeight(28);
        accountTable.setFont(UIStyleConstants.PLAIN_FONT);
        accountTable.setGridColor(Color.LIGHT_GRAY);

        JTableHeader header = accountTable.getTableHeader();
        header.setFont(UIStyleConstants.TABLE_HEADER_FONT);
        header.setBackground(UIStyleConstants.PRIMARY_COLOR);
        header.setForeground(Color.WHITE);
        header.setReorderingAllowed(false);

        accountTable.getColumnModel().getColumn(0).setPreferredWidth(40);  // STT
        accountTable.getColumnModel().getColumn(1).setPreferredWidth(80);  // Account ID
        accountTable.getColumnModel().getColumn(2).setPreferredWidth(80);  // User ID (thêm cột này)
        accountTable.getColumnModel().getColumn(3).setPreferredWidth(120); // Username
        accountTable.getColumnModel().getColumn(4).setPreferredWidth(180); // Họ Tên
        accountTable.getColumnModel().getColumn(5).setPreferredWidth(200); // Email
        accountTable.getColumnModel().getColumn(6).setPreferredWidth(100); // Vai trò
        accountTable.getColumnModel().getColumn(7).setPreferredWidth(100); // Trạng thái
        accountTable.getColumnModel().getColumn(8).setPreferredWidth(100); // Mã NV

        accountTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && accountTable.getSelectedRow() != -1) {
                displaySelectedAccount();
            }
        });
        JScrollPane scrollPane = new JScrollPane(accountTable);
        tablePanel.add(scrollPane, BorderLayout.CENTER);
    }

    private void executeDataLoad(String searchTerm) {
        tableModel.setRowCount(0);
        SwingWorker<List<Object[]>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Object[]> doInBackground() throws Exception {
                List<Object[]> rows = new ArrayList<>();
                // Thêm A.USER_ID vào câu SELECT
                String baseQuery = "SELECT A.ACCOUNT_ID, A.USER_ID, A.USERNAME, U.FULL_NAME, U.EMAIL, A.STATUS, RG.NAME_ROLE_GROUP, NV.MANHANVIEN " +
                        "FROM ACCOUNT A " +
                        "JOIN USERS U ON A.USER_ID = U.USER_ID " +
                        "LEFT JOIN ACCOUNT_ASSIGN_ROLE_GROUP AARG ON A.ACCOUNT_ID = AARG.ACCOUNT_ID " +
                        "LEFT JOIN ROLE_GROUP RG ON AARG.ROLE_GROUP_ID = RG.ROLE_GROUP_ID " +
                        "LEFT JOIN NHANVIEN NV ON A.ACCOUNT_ID = NV.ACCOUNT_ID ";

                String query;
                boolean hasSearchTerm = searchTerm != null && !searchTerm.isEmpty();

                if (hasSearchTerm) {
                    query = baseQuery + "WHERE (UPPER(A.USERNAME) LIKE UPPER(?) OR UPPER(U.FULL_NAME) LIKE UPPER(?) OR UPPER(U.EMAIL) LIKE UPPER(?) OR UPPER(NV.MANHANVIEN) LIKE UPPER(?)) AND A.IS_DELETED = 0 " +
                            "ORDER BY A.USERNAME";
                } else {
                    query = baseQuery + "WHERE A.IS_DELETED = 0 ORDER BY A.USERNAME";
                }

                try (Connection conn = ConnectionOracle.getOracleConnection();
                     PreparedStatement pstmt = conn.prepareStatement(query)) {

                    if (hasSearchTerm) {
                        String likePattern = "%" + searchTerm + "%";
                        pstmt.setString(1, likePattern);
                        pstmt.setString(2, likePattern);
                        pstmt.setString(3, likePattern);
                        pstmt.setString(4, likePattern);
                    }

                    try (ResultSet rs = pstmt.executeQuery()) {
                        int stt = 1;
                        while (rs.next()) {
                            rows.add(new Object[]{
                                    stt++,
                                    rs.getInt("ACCOUNT_ID"),
                                    rs.getInt("USER_ID"), // Lấy USER_ID
                                    rs.getString("USERNAME"),
                                    rs.getString("FULL_NAME"),
                                    rs.getString("EMAIL"),
                                    rs.getString("NAME_ROLE_GROUP") != null ? rs.getString("NAME_ROLE_GROUP") : "Chưa gán",
                                    rs.getString("STATUS"),
                                    rs.getString("MANHANVIEN") != null ? rs.getString("MANHANVIEN") : "N/A"
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
                    JOptionPane.showMessageDialog(QuanLyTaiKhoanNhanVien.this,
                            "Lỗi khi tải dữ liệu tài khoản: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    public void loadAccountData(String searchTerm) {
        executeDataLoad(searchTerm);
    }

    private void searchAccountData() {
        String key = txtSearch.getText().trim();
        loadAccountData(key);
    }

    private void clearForm() {
        selectedAccountId = -1;
        selectedUserId = -1; // Reset USER_ID
        txtUsername.setText("");
        txtFullName.setText("");
        txtEmail.setText("");
        txtMaNhanVienDisplay.setText("");
        txtRoleDisplay.setText("");
        txtNewPassword.setText("");
        // cmbAccountStatus.setSelectedIndex(0); // REMOVED
        accountTable.clearSelection();
        txtFullName.requestFocus(); // Focus vào trường Họ tên vì Username không sửa được
    }


    private void updateAccount() {
        if (selectedAccountId == -1 || selectedUserId == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một tài khoản để cập nhật!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String newFullName = txtFullName.getText().trim();
        String newEmail = txtEmail.getText().trim();
        String newPassword = new String(txtNewPassword.getPassword()).trim();
        // String accountStatus = (String) cmbAccountStatus.getSelectedItem(); // REMOVED

        if (newFullName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Họ và tên không được để trống!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            txtFullName.requestFocus();
            return;
        }
        if (newEmail.isEmpty() || !EMAIL_PATTERN.matcher(newEmail).matches()) {
            JOptionPane.showMessageDialog(this, "Email không hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            txtEmail.requestFocus();
            return;
        }
        // REMOVED accountStatus validation
        // if (accountStatus == null || accountStatus.isEmpty()) {
        //     JOptionPane.showMessageDialog(this, "Trạng thái tài khoản không hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        //     return;
        // }

        Connection conn = null;
        try {
            conn = ConnectionOracle.getOracleConnection();
            conn.setAutoCommit(false); // Bắt đầu transaction

            // 1. Cập nhật bảng USERS
            String sqlUpdateUsers = "UPDATE USERS SET FULL_NAME = ?, EMAIL = ?, UPDATED_AT = SYSDATE WHERE USER_ID = ?";
            try (PreparedStatement pstmtUsers = conn.prepareStatement(sqlUpdateUsers)) {
                pstmtUsers.setString(1, newFullName);
                pstmtUsers.setString(2, newEmail);
                pstmtUsers.setInt(3, selectedUserId);
                pstmtUsers.executeUpdate();
            }

            // 2. Cập nhật bảng ACCOUNT (password nếu có, và UPDATED_AT. STATUS is NOT updated here anymore)
            String sqlUpdateAccount;
            boolean updatePassword = !newPassword.isEmpty();

            if (updatePassword) {
                // Update password and UPDATED_AT
                sqlUpdateAccount = "UPDATE ACCOUNT SET PASSWORD_HASH = ?, UPDATED_AT = SYSDATE WHERE ACCOUNT_ID = ? AND IS_DELETED = 0";
            } else {
                // Only update UPDATED_AT as other user details (FULL_NAME, EMAIL) have changed in USERS table.
                // No change to password or status from this form.
                sqlUpdateAccount = "UPDATE ACCOUNT SET UPDATED_AT = SYSDATE WHERE ACCOUNT_ID = ? AND IS_DELETED = 0";
            }

            try (PreparedStatement pstmtAccount = conn.prepareStatement(sqlUpdateAccount)) {
                if (updatePassword) {
                    // pstmtAccount.setString(1, accountStatus); // REMOVED status update
                    pstmtAccount.setString(1, hashPassword(newPassword)); // Parameter index shifted
                    pstmtAccount.setInt(2, selectedAccountId);       // Parameter index shifted
                } else {
                    // pstmtAccount.setString(1, accountStatus); // REMOVED status update
                    pstmtAccount.setInt(1, selectedAccountId);       // Parameter index shifted (only account_id for where clause)
                }
                pstmtAccount.executeUpdate();
            }

            conn.commit(); // Commit transaction
            JOptionPane.showMessageDialog(this, "Cập nhật tài khoản thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            loadAccountData(txtSearch.getText().trim());
            clearForm();

        } catch (SQLException | ClassNotFoundException | NoSuchAlgorithmException e) {
            if (conn != null) {
                try {
                    conn.rollback(); // Rollback nếu có lỗi
                } catch (SQLException exRollback) {
                    exRollback.printStackTrace();
                }
            }
            // Xử lý lỗi unique constraint cho email trong bảng USERS
            if (e instanceof SQLException && ((SQLException) e).getErrorCode() == 1) { // ORA-00001: unique constraint violated
                 String msg = ((SQLException) e).getMessage().toUpperCase();
                 if (msg.contains("EMAIL_UK_USERS") || (msg.contains("EMAIL") && msg.contains("USERS"))) { // Giả sử tên constraint là EMAIL_UK_USERS
                    JOptionPane.showMessageDialog(this, "Email '" + newEmail + "' đã được sử dụng. Vui lòng chọn email khác.", "Lỗi Trùng Email", JOptionPane.ERROR_MESSAGE);
                    txtEmail.requestFocus();
                    return;
                }
            }
            JOptionPane.showMessageDialog(this, "Lỗi khi cập nhật tài khoản: " + e.getMessage(), "Lỗi Hệ Thống", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // Trả lại autoCommit
                    conn.close();
                } catch (SQLException exClose) {
                    exClose.printStackTrace();
                }
            }
        }
    }

    private String hashPassword(String password) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hashedBytes = md.digest(password.getBytes());
        StringBuilder sb = new StringBuilder();
        for (byte b : hashedBytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private void deleteAccount() {
        if (selectedAccountId == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một tài khoản để vô hiệu hóa!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int selectedRow = accountTable.getSelectedRow();
        if (selectedRow == -1) { // Should not happen if selectedAccountId is valid, but good practice
            JOptionPane.showMessageDialog(this, "Không có tài khoản nào được chọn trong bảng.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String currentRole = txtRoleDisplay.getText(); // Role from the form (populated by displaySelectedAccount)
        String currentStatusInTable = (String) tableModel.getValueAt(selectedRow, 7); // Get status from table model


        if (RoleGroupConstants.ADMIN.equals(currentRole)) {
            int activeAdminCount = 0;
            try (Connection conn = ConnectionOracle.getOracleConnection();
                 PreparedStatement pstmtCount = conn.prepareStatement(
                         "SELECT COUNT(*) FROM ACCOUNT A " +
                                 "JOIN ACCOUNT_ASSIGN_ROLE_GROUP AARG ON A.ACCOUNT_ID = AARG.ACCOUNT_ID " +
                                 "JOIN ROLE_GROUP RG ON AARG.ROLE_GROUP_ID = RG.ROLE_GROUP_ID " +
                                 "WHERE RG.NAME_ROLE_GROUP = ? AND A.STATUS = 'ACTIVE' AND A.IS_DELETED = 0"
                 )) {
                pstmtCount.setString(1, RoleGroupConstants.ADMIN);
                ResultSet rsCount = pstmtCount.executeQuery();
                if (rsCount.next()) {
                    activeAdminCount = rsCount.getInt(1);
                }
            } catch (SQLException | ClassNotFoundException ex) {
                JOptionPane.showMessageDialog(this, "Lỗi khi kiểm tra số lượng Admin: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            // Check if the admin being deleted is currently ACTIVE
            if (activeAdminCount <= 1 && "ACTIVE".equals(currentStatusInTable)) { // MODIFIED: Use status from table
                JOptionPane.showMessageDialog(this, "Không thể vô hiệu hóa tài khoản ADMIN cuối cùng đang hoạt động!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn vô hiệu hóa tài khoản này? (Tài khoản sẽ được đặt thành INACTIVE và IS_DELETED = 1)",
                "Xác nhận vô hiệu hóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            String sql = "UPDATE ACCOUNT SET STATUS = 'INACTIVE', IS_DELETED = 1, UPDATED_AT = SYSDATE WHERE ACCOUNT_ID = ?";
            try (Connection conn = ConnectionOracle.getOracleConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, selectedAccountId);
                int affectedRows = pstmt.executeUpdate();
                if (affectedRows > 0) {
                    JOptionPane.showMessageDialog(this, "Vô hiệu hóa tài khoản thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                    loadAccountData(txtSearch.getText().trim());
                    clearForm();
                } else {
                    JOptionPane.showMessageDialog(this, "Vô hiệu hóa tài khoản thất bại. Tài khoản có thể đã bị xóa hoặc không tồn tại.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException | ClassNotFoundException e) {
                JOptionPane.showMessageDialog(this, "Lỗi khi vô hiệu hóa tài khoản: " + e.getMessage(), "Lỗi SQL", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }

    private void displaySelectedAccount() {
        int selectedRow = accountTable.getSelectedRow();
        if (selectedRow != -1) {
            selectedAccountId = (Integer) tableModel.getValueAt(selectedRow, 1);
            selectedUserId = (Integer) tableModel.getValueAt(selectedRow, 2); // Lấy USER_ID

            txtUsername.setText((String) tableModel.getValueAt(selectedRow, 3));
            txtFullName.setText((String) tableModel.getValueAt(selectedRow, 4));
            txtEmail.setText((String) tableModel.getValueAt(selectedRow, 5));
            txtRoleDisplay.setText((String) tableModel.getValueAt(selectedRow, 6));
            // cmbAccountStatus.setSelectedItem((String) tableModel.getValueAt(selectedRow, 7)); // REMOVED
            txtMaNhanVienDisplay.setText((String) tableModel.getValueAt(selectedRow, 8));
            txtNewPassword.setText("");

            // Cho phép sửa Họ tên và Email
            txtFullName.setEditable(true);
            txtEmail.setEditable(true);
        } else {
            // Nếu không có dòng nào được chọn, có thể muốn vô hiệu hóa các trường
            txtFullName.setEditable(false);
            txtEmail.setEditable(false);
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
            new QuanLyTaiKhoanNhanVien().setVisible(true);
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
            .addGap(0, 734, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 361, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}



