package View.Customer;

import Process.RoleGroupConstants;
import Process.UserToken;
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
    private CustomerHomePage ownerFrame; // Tham chiếu đến CustomerHomePage

    public GioHang(CustomerHomePage owner) {
        this.ownerFrame = owner;
        if (this.ownerFrame == null || !this.ownerFrame.isUserCurrentlyLoggedIn()) {
             JOptionPane.showMessageDialog(null, "Lỗi: Không thể hiển thị lịch sử vé do không có thông tin người dùng.", "Lỗi Người Dùng", JOptionPane.ERROR_MESSAGE);
             SwingUtilities.invokeLater(() -> this.dispose()); // Đóng nếu không có owner hợp lệ
             return;
        }
        // setTitle("Lịch Sử Vé Đã Mua - " + owner.getUserFullName()); // Đặt title ở CardLayout
        // setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Sẽ được quản lý bởi CardLayout
        // setSize(700, 500);
        // setLocationRelativeTo(owner);
        initComponentsCustom();
        loadPurchasedTicketsHistory();
    }

    private void initComponentsCustom() {
        // Main Panel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.setBackground(WHITE_COLOR);

        // Header Panel
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        headerPanel.setBackground(HEADER_BACKGROUND);
        headerPanel.setPreferredSize(new Dimension(0, 50)); // Chiều cao cố định cho header
        JLabel titleLabel = new JLabel("Lịch Sử Vé Đã Mua");
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(WHITE_COLOR);
        headerPanel.add(titleLabel);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Table Panel
        String[] columnNames = {"Tên Vé", "Số Lượng", "Đơn Giá", "Tổng Tiền", "Ngày Mua"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        historyTable = new JTable(tableModel);
        historyTable.setFont(REGULAR_FONT);
        historyTable.setRowHeight(28); // Tăng chiều cao hàng
        historyTable.getTableHeader().setFont(BOLD_FONT);
        historyTable.getTableHeader().setBackground(LIGHT_TABLE_HEADER);
        historyTable.getTableHeader().setForeground(Color.DARK_GRAY);
        historyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        historyTable.setFillsViewportHeight(true);
        historyTable.setShowGrid(true); // Hiển thị grid
        historyTable.setGridColor(BORDER_COLOR.brighter());

        // Căn chỉnh và định dạng cột
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(JLabel.RIGHT);

        TableColumnModel columnModel = historyTable.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(200); // Tên Vé
        columnModel.getColumn(1).setCellRenderer(centerRenderer); // Số lượng
        columnModel.getColumn(1).setPreferredWidth(80);
        columnModel.getColumn(2).setCellRenderer(rightRenderer); // Đơn giá
        columnModel.getColumn(2).setPreferredWidth(120);
        columnModel.getColumn(3).setCellRenderer(rightRenderer); // Tổng Tiền
        columnModel.getColumn(3).setPreferredWidth(120);
        columnModel.getColumn(4).setCellRenderer(centerRenderer); // Ngày Mua
        columnModel.getColumn(4).setPreferredWidth(150);

        DefaultTableCellRenderer currencyRenderer = new DefaultTableCellRenderer() {
            final NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                if (value instanceof Number) {
                    value = formatter.format(((Number) value).doubleValue());
                }
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(JLabel.RIGHT); // Căn phải cho tiền
                return this;
            }
        };
        columnModel.getColumn(2).setCellRenderer(currencyRenderer); // Đơn giá
        columnModel.getColumn(3).setCellRenderer(currencyRenderer); // Tổng tiền

        JScrollPane scrollPane = new JScrollPane(historyTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBackground(WHITE_COLOR);

        //JButton clearHistoryButton = new JButton("Xóa Lịch Sử"); // Removed this button
        //clearHistoryButton.setFont(BOLD_FONT.deriveFont(13f));
        //clearHistoryButton.setBackground(new Color(220, 53, 69));
        //clearHistoryButton.setForeground(WHITE_COLOR);
        //clearHistoryButton.setIcon(loadIcon("delete_bin.png", 16, 16)); // Thêm icon
        //clearHistoryButton.addActionListener(e -> clearPurchasedTicketsHistory()); // Removed ActionListener

        JButton backButton = new JButton("Quay Lại Trang Chủ");
        backButton.setFont(BOLD_FONT.deriveFont(13f));
        backButton.setIcon(loadIcon("back_arrow.png", 16, 16)); // Thêm icon
        backButton.addActionListener(e -> {
            if (ownerFrame != null) {
                ownerFrame.showWelcomeScreen();
            }
        });

        //buttonPanel.add(clearHistoryButton); // Removed adding button
        buttonPanel.add(backButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Thay vì this.setContentPane(mainPanel);
        // JFrame này sẽ được lấy contentPane và add vào CardLayout của CustomerHomePage
        // Nên cấu trúc của nó phải trả về một Container (thường là mainPanel)
        this.setLayout(new BorderLayout()); // Đảm bảo JFrame này có LayoutManager
        this.add(mainPanel, BorderLayout.CENTER); // Thêm mainPanel vào JFrame này
    }

    public void loadPurchasedTicketsHistory() {
        if (ownerFrame == null) return;

        List<LichSuVeDaMua> history = ownerFrame.getPurchasedTicketsForCurrentUser(); // Sử dụng phương thức mới
        tableModel.setRowCount(0);
        if (history != null) {
            for (LichSuVeDaMua ticket : history) {
                tableModel.addRow(new Object[]{
                        ticket.getName(),
                        ticket.getQuantity(),
                        ticket.getUnitPrice(), // Sẽ được format bởi renderer
                        ticket.getTotalPrice(), // Sẽ được format bởi renderer
                        ticket.getPurchaseDate() != null ? ticket.getPurchaseDate() : "N/A"
                });
            }
        }
    }

    // Removed clearPurchasedTicketsHistory() method
    // public void clearPurchasedTicketsHistory() {
    //     if (ownerFrame == null) return;
    //
    //     int confirmation = JOptionPane.showConfirmDialog(this.ownerFrame, // Hiển thị dialog trên ownerFrame
    //             "Bạn có chắc chắn muốn xóa toàn bộ lịch sử mua vé không?",
    //             "Xác nhận xóa lịch sử",
    //             JOptionPane.YES_NO_OPTION,
    //             JOptionPane.WARNING_MESSAGE);
    //
    //     if (confirmation == JOptionPane.YES_OPTION) {
    //         ownerFrame.clearPurchasedTicketsHistoryForCurrentUser(); // Sử dụng phương thức mới
    //         loadPurchasedTicketsHistory();
    //         // ownerFrame.showGlobalNotification("Lịch sử mua vé đã được xóa.", "INFO"); // CustomerHomePage đã có thông báo
    //     }
    // }

    private ImageIcon loadIcon(String iconName, int width, int height) {
        // Copy hàm loadIcon từ CustomerHomePage hoặc tạo một lớp tiện ích chung
        String resourcePath = "/icons/" + iconName;
        URL imgUrl = getClass().getResource(resourcePath);
        if (imgUrl != null) {
            try {
                ImageIcon originalIcon = new ImageIcon(imgUrl);
                Image scaledImage = originalIcon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
                return new ImageIcon(scaledImage);
            } catch (Exception e) { /* Xử lý lỗi */ }
        }
        return null; // Hoặc placeholder icon
    }

    // Main method để test độc lập (nếu cần)
    public static void main(String args[]) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(new FlatLightLaf()); } catch (Exception e) { e.printStackTrace(); }
            UserToken testToken = new UserToken();
            testToken.setEntityId("TEST_USER_GH"); testToken.setEntityFullName("Test User GioHang");
            testToken.setAccountId(102); testToken.setRole(RoleGroupConstants.CUSTOMER);

            CustomerHomePage fakeOwner = new CustomerHomePage(testToken);
            fakeOwner.addPurchasedTicketForCurrentUser(new LichSuVeDaMua("Vé Ngày Xe Máy", 2, 3000, 6000));
            fakeOwner.addPurchasedTicketForCurrentUser(new LichSuVeDaMua("Vé Tuần Ô Tô", 1, 25000, 25000));

            // Thay vì new GioHang(...).setVisible(true) nếu dùng CardLayout
            // Thì sẽ add contentPane của GioHang vào CardLayout của CustomerHomePage
            // Test hiển thị độc lập:
             JFrame testFrame = new JFrame("Test Lịch Sử Vé");
             testFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
             testFrame.setSize(800, 600);
             GioHang gioHangPanel = new GioHang(fakeOwner);
             testFrame.add(gioHangPanel.getContentPane()); // Lấy contentPane nếu GioHang là JFrame
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
