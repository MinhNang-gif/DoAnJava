package View.Customer;

import Process.RoleGroupConstants;
import Process.UserToken;
import ConnectDB.ConnectionOracle; // Cần thiết để truy vấn VEGUIXE
import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.sql.Connection; // Cần thiết
import java.sql.PreparedStatement; // Cần thiết
import java.sql.ResultSet; // Cần thiết
import java.sql.SQLException; // Cần thiết
import java.sql.Timestamp; // Cần thiết
import java.text.SimpleDateFormat; // Cần thiết
import java.util.Date; // Cần thiết

public class GioHang extends javax.swing.JFrame {
    // --- UI Styling Constants ---
    private final Color HEADER_BACKGROUND = new Color(70, 130, 180);
    private final Color WHITE_COLOR = Color.WHITE;
    private final Color LIGHT_TABLE_HEADER = new Color(235, 240, 245);
    private final Color BORDER_COLOR = new Color(200, 200, 200);

    private final Font TITLE_FONT = new Font("Arial", Font.BOLD, 22);
    private final Font REGULAR_FONT = new Font("Arial", Font.PLAIN, 14);
    private final Font BOLD_FONT = new Font("Arial", Font.BOLD, 15);

    // --- UI Components ---
    private JTable historyTable;
    private DefaultTableModel tableModel;
    private CustomerHomePage ownerFrame;
    private final SimpleDateFormat dateTimeFormatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");


    public GioHang(CustomerHomePage owner) {
        this.ownerFrame = owner;
        if (this.ownerFrame == null || !this.ownerFrame.isUserCurrentlyLoggedIn()) {
             JOptionPane.showMessageDialog(null, "Lỗi: Không thể hiển thị lịch sử vé do không có thông tin người dùng.", "Lỗi Người Dùng", JOptionPane.ERROR_MESSAGE);
             SwingUtilities.invokeLater(() -> this.dispose());
             return;
        }
        initComponentsCustom();
        loadPurchasedTicketsHistory();
    }

    private void initComponentsCustom() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.setBackground(WHITE_COLOR);

        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        headerPanel.setBackground(HEADER_BACKGROUND);
        headerPanel.setPreferredSize(new Dimension(0, 50));
        JLabel titleLabel = new JLabel("Lịch Sử Vé Đã Mua");
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(WHITE_COLOR);
        headerPanel.add(titleLabel);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // *** KHÔI PHỤC CỘT "Mã Vé" ***
        String[] columnNames = {"Mã Vé", "Tên Vé Hiển Thị", "Số Lượng", "Đơn Giá", "Tổng Tiền", "Ngày Mua"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        historyTable = new JTable(tableModel);
        historyTable.setFont(REGULAR_FONT);
        historyTable.setRowHeight(28);
        historyTable.getTableHeader().setFont(BOLD_FONT);
        historyTable.getTableHeader().setBackground(LIGHT_TABLE_HEADER);
        historyTable.getTableHeader().setForeground(Color.DARK_GRAY);
        historyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        historyTable.setFillsViewportHeight(true);
        historyTable.setShowGrid(true);
        historyTable.setGridColor(BORDER_COLOR.brighter());

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
        DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
        leftRenderer.setHorizontalAlignment(JLabel.LEFT);


        TableColumnModel columnModelTable = historyTable.getColumnModel(); // Đổi tên biến để tránh trùng
        columnModelTable.getColumn(0).setPreferredWidth(120); // Mã Vé
        columnModelTable.getColumn(0).setCellRenderer(leftRenderer);
        columnModelTable.getColumn(1).setPreferredWidth(280); // Tên Vé Hiển Thị
        columnModelTable.getColumn(1).setCellRenderer(leftRenderer);
        columnModelTable.getColumn(2).setCellRenderer(centerRenderer); // Số lượng
        columnModelTable.getColumn(2).setPreferredWidth(80);
        columnModelTable.getColumn(3).setCellRenderer(rightRenderer); // Đơn giá
        columnModelTable.getColumn(3).setPreferredWidth(120);
        columnModelTable.getColumn(4).setCellRenderer(rightRenderer); // Tổng Tiền
        columnModelTable.getColumn(4).setPreferredWidth(120);
        columnModelTable.getColumn(5).setCellRenderer(centerRenderer); // Ngày Mua
        columnModelTable.getColumn(5).setPreferredWidth(150);

        DefaultTableCellRenderer currencyRenderer = new DefaultTableCellRenderer() {
            final NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                if (value instanceof Number) {
                    value = formatter.format(((Number) value).doubleValue());
                }
                // Gọi super sau khi đã format value
                Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(JLabel.RIGHT);
                return cell;
            }
        };
        columnModelTable.getColumn(3).setCellRenderer(currencyRenderer); // Đơn giá
        columnModelTable.getColumn(4).setCellRenderer(currencyRenderer); // Tổng tiền


        JScrollPane scrollPane = new JScrollPane(historyTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        historyTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int selectedRow = historyTable.getSelectedRow();
                    if (selectedRow >= 0) {
                        // Lấy MAVEXE từ model của table (cột 0)
                        String maVeXe = (String) tableModel.getValueAt(historyTable.convertRowIndexToModel(selectedRow), 0);
                        if (maVeXe != null && !maVeXe.isEmpty()) {
                            // Kiểm tra nếu là mã vé cũ không có trong DB
                            if (maVeXe.startsWith("UNKNOWN_MAVEXE_")) {
                                JOptionPane.showMessageDialog(ownerFrame,
                                        "Đây là dữ liệu vé cũ, không có thông tin chi tiết trong cơ sở dữ liệu.",
                                        "Thông Báo", JOptionPane.INFORMATION_MESSAGE);
                            } else {
                                showChiTietVeDialog(maVeXe);
                            }
                        } else {
                             System.err.println("Không lấy được Mã Vé từ dòng đã chọn hoặc mã vé rỗng.");
                             JOptionPane.showMessageDialog(ownerFrame, "Không thể lấy thông tin mã vé cho dòng này.", "Lỗi", JOptionPane.WARNING_MESSAGE);
                        }
                    }
                }
            }
        });

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBackground(WHITE_COLOR);
        JButton backButton = new JButton("Quay Lại Trang Chủ");
        backButton.setFont(BOLD_FONT.deriveFont(13f));
        backButton.setIcon(loadIcon("back_arrow.png", 16, 16));
        backButton.addActionListener(e -> {
            if (ownerFrame != null) {
                ownerFrame.setEnabled(true); // Enable lại owner frame
                ownerFrame.showWelcomeScreen();
            }
            // Nếu GioHang là JFrame riêng, nó nên tự đóng
             if (SwingUtilities.getWindowAncestor(this) instanceof JFrame && this.isDisplayable()) {
                this.dispose();
            }
        });
        buttonPanel.add(backButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        this.setLayout(new BorderLayout());
        this.add(mainPanel, BorderLayout.CENTER);
    }

    public void loadPurchasedTicketsHistory() {
        if (ownerFrame == null) return;

        List<LichSuVeDaMua> history = ownerFrame.getPurchasedTicketsForCurrentUser();
        tableModel.setRowCount(0); // Xóa dữ liệu cũ
        if (history != null) {
            for (LichSuVeDaMua ticket : history) {
                tableModel.addRow(new Object[]{
                        ticket.getMaVeXe() != null ? ticket.getMaVeXe() : "N/A", // *** ĐẢM BẢO TRUYỀN MAVEXE ***
                        ticket.getName(),
                        ticket.getQuantity(),
                        ticket.getUnitPrice(),
                        ticket.getTotalPrice(),
                        ticket.getPurchaseDate() != null ? ticket.getPurchaseDate() : "N/A"
                });
            }
        }
    }

    private void showChiTietVeDialog(String maVeXe) {
        // ... (Phương thức showChiTietVeDialog giữ nguyên như đã cung cấp ở lần trước) ...
        System.out.println("Requesting details for MAVEXE: " + maVeXe);
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String sql = "SELECT v.MAVEXE, v.BIENSO, v.MAKH, lgx.TENLOAIGUIXE, v.PHIGUIXE, v.NGAYLAPVE, v.NGAYHETHAN " +
                     "FROM VEGUIXE v " +
                     "JOIN LOAIGUIXE lgx ON v.MALOAIGUIXE = lgx.MALOAIGUIXE " +
                     "WHERE v.MAVEXE = ?";

        try {
            conn = ConnectionOracle.getOracleConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, maVeXe);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                JDialog detailDialog = new JDialog(this.ownerFrame, "Chi Tiết Vé: " + maVeXe, true);
                detailDialog.setSize(450, 380); // Tăng chiều cao một chút
                detailDialog.setLocationRelativeTo(this.ownerFrame);
                detailDialog.setLayout(new BorderLayout(10,10));
                detailDialog.getContentPane().setBackground(Color.WHITE);

                JPanel infoPanel = new JPanel(new GridBagLayout());
                infoPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
                infoPanel.setBackground(new Color(245, 245, 245));
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.gridx = 0; gbc.gridy = GridBagConstraints.RELATIVE;
                gbc.anchor = GridBagConstraints.WEST;
                gbc.insets = new Insets(6, 5, 6, 5); // Điều chỉnh insets

                Font labelFont = new Font("Arial", Font.BOLD, 14);
                Font valueFont = new Font("Arial", Font.PLAIN, 14);

                addDetailRow(infoPanel, gbc, "Mã Vé:", rs.getString("MAVEXE"), labelFont, valueFont);
                addDetailRow(infoPanel, gbc, "Biển Số Xe:", rs.getString("BIENSO") != null ? rs.getString("BIENSO") : "Không có", labelFont, valueFont);
                addDetailRow(infoPanel, gbc, "Mã Khách Hàng:", rs.getString("MAKH"), labelFont, valueFont);
                addDetailRow(infoPanel, gbc, "Loại Vé:", rs.getString("TENLOAIGUIXE"), labelFont, valueFont);
                addDetailRow(infoPanel, gbc, "Phí Gửi Xe:", NumberFormat.getCurrencyInstance(new Locale("vi", "VN")).format(rs.getDouble("PHIGUIXE")), labelFont, valueFont);

                Timestamp ngayLap = rs.getTimestamp("NGAYLAPVE");
                addDetailRow(infoPanel, gbc, "Ngày Lập Vé:", ngayLap != null ? dateTimeFormatter.format(ngayLap) : "N/A", labelFont, valueFont);

                Timestamp ngayHetHan = rs.getTimestamp("NGAYHETHAN");
                addDetailRow(infoPanel, gbc, "Ngày Hết Hạn:", ngayHetHan != null ? dateTimeFormatter.format(ngayHetHan) : "N/A", labelFont, valueFont);

                JScrollPane scrollPaneDialog = new JScrollPane(infoPanel);
                scrollPaneDialog.setBorder(BorderFactory.createEtchedBorder()); // Thêm border cho scrollpane

                detailDialog.add(scrollPaneDialog, BorderLayout.CENTER);

                JButton closeButton = new JButton("Đóng");
                closeButton.setFont(new Font("Arial", Font.BOLD, 13));
                closeButton.addActionListener(e -> detailDialog.dispose());
                JPanel buttonPanelDialog = new JPanel(new FlowLayout(FlowLayout.CENTER));
                buttonPanelDialog.setBorder(BorderFactory.createEmptyBorder(5,0,10,0)); // Điều chỉnh padding
                buttonPanelDialog.add(closeButton);
                detailDialog.add(buttonPanelDialog, BorderLayout.SOUTH);

                detailDialog.setVisible(true);

            } else {
                JOptionPane.showMessageDialog(this.ownerFrame, "Không tìm thấy thông tin chi tiết cho mã vé: " + maVeXe, "Thông Tin Vé", JOptionPane.WARNING_MESSAGE);
            }

        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this.ownerFrame, "Lỗi khi tải chi tiết vé: " + e.getMessage(), "Lỗi Cơ Sở Dữ Liệu", JOptionPane.ERROR_MESSAGE);
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void addDetailRow(JPanel panel, GridBagConstraints gbcParent, String labelText, String valueText, Font labelFont, Font valueFont) {
        // ... (Phương thức này giữ nguyên)
        GridBagConstraints gbc = (GridBagConstraints) gbcParent.clone();
        JLabel label = new JLabel(labelText);
        label.setFont(labelFont);
        gbc.gridx = 0;
        gbc.weightx = 0.3;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(label, gbc);

        JLabel value = new JLabel(valueText);
        value.setFont(valueFont);
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(value, gbc);
    }

    private ImageIcon loadIcon(String iconName, int width, int height) {
        // ... (Phương thức này giữ nguyên)
        String resourcePath = "/icons/" + iconName;
        URL imgUrl = getClass().getResource(resourcePath);
        if (imgUrl != null) {
            try {
                ImageIcon originalIcon = new ImageIcon(imgUrl);
                Image scaledImage = originalIcon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
                return new ImageIcon(scaledImage);
            } catch (Exception e) { /* Xử lý lỗi */ }
        }
        return null;
    }

    // Phương thức main (giữ nguyên như bạn đã cung cấp, đã sửa constructor LichSuVeDaMua)
    public static void main(String args[]) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(new FlatLightLaf()); } catch (Exception e) { e.printStackTrace(); }
            UserToken testToken = new UserToken();
            testToken.setEntityId("TEST_USER_GH"); testToken.setEntityFullName("Test User GioHang");
            testToken.setAccountId(102);
             if (RoleGroupConstants.CUSTOMER == null) { System.err.println("Lỗi test GioHang: RoleGroupConstants.CUSTOMER là null"); return; }
            testToken.setRole(RoleGroupConstants.CUSTOMER);

            CustomerHomePage fakeOwner = new CustomerHomePage(testToken);
            fakeOwner.addPurchasedTicketForCurrentUser(new LichSuVeDaMua("VX_TEST_001_MAIN", "Vé Ngày Xe Máy (Test Main)", 2, 3000, 6000));
            fakeOwner.addPurchasedTicketForCurrentUser(new LichSuVeDaMua("VX_TEST_002_MAIN", "Vé Tuần Ô Tô (Test Main)", 1, 25000, 25000));
            // Thêm một vé không có mã (để test trường hợp UNKNOWN)
            LichSuVeDaMua oldTicket = new LichSuVeDaMua(null, "Vé Cũ Không Mã", 1, 1000, 1000);
            // Giả sử maVeXe được set thành UNKNOWN khi load từ file cũ
            // Để test, ta có thể trực tiếp set giá trị này sau khi tạo
            // Hoặc, nếu constructor của LichSuVeDaMua trong CustomerHomePage.java đã xử lý, thì không cần
            // Ví dụ, nếu CustomerHomePage gán "UNKNOWN..." nếu maVeXe là null khi tải
            // (Tuy nhiên, constructor LichSuVeDaMua.java hiện tại sẽ không tự làm điều đó)
            // Để mô phỏng dữ liệu cũ từ file, bạn cần đảm bảo khi load, nó được gán "UNKNOWN..."
            // Hiện tại, constructor LichSuVeDaMua sẽ nhận null và lưu null.
            // Ta sẽ sửa ở `loadPurchasedTicketsHistory` để xử lý `ticket.getMaVeXe()` là null.
            // Hoặc, sửa constructor của `LichSuVeDaMua` để nếu `maVeXe` là null thì gán "UNKNOWN"
            // Nhưng tốt hơn là xử lý lúc hiển thị hoặc lúc lấy từ model.
            // Trong `main` này, ta cứ truyền mã test cụ thể.

             JFrame testFrame = new JFrame("Test Lịch Sử Vé");
             testFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
             testFrame.setSize(800, 600);
             GioHang gioHangPanel = new GioHang(fakeOwner);
             // Giả sử GioHang là JFrame, ta lấy contentPane của nó
             // Nếu GioHang là JPanel, chỉ cần add gioHangPanel
             testFrame.add(gioHangPanel.getContentPane());
             testFrame.setLocationRelativeTo(null);
             testFrame.setVisible(true);
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
