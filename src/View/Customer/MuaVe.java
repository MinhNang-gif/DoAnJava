package View.Customer;

import Process.RoleGroupConstants;
import Process.UserToken;
import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Date;
import java.text.SimpleDateFormat;

public class MuaVe extends javax.swing.JFrame {
    // --- UI Styling Constants ---
    private final Color BLUE_COLOR = new Color(0, 123, 255); // Màu xanh dương chủ đạo
    private final Color WHITE_COLOR = Color.WHITE;
    private final Color ORANGE_COLOR = new Color(255, 153, 51); // Màu cam cho tổng tiền và nút chính
    private final Color LIGHT_GRAY_FORM_BG = new Color(248, 249, 250); // Màu nền form
    private final Color HEADER_BACKGROUND = new Color(70, 130, 180); // Steel Blue cho header

    private final Font TITLE_FONT = new Font("Arial", Font.BOLD, 24);
    private final Font REGULAR_FONT = new Font("Arial", Font.PLAIN, 14);
    private final Font BOLD_FONT = new Font("Arial", Font.BOLD, 16);
    private final Font PRICE_FONT = new Font("Arial", Font.BOLD, 15); // Font cho giá tiền

    // --- Ticket Prices (Giữ nguyên) ---
    private final double MOTORCYCLE_PRICE = 3000;
    private final double CAR_PRICE = 10000;
    private final double WEEKLY_PRICE = 25000;
    private final double MONTHLY_PRICE = 95000;
    private final double MAINTENANCE_PRICE = 50000;

    // --- Owner Frame Reference ---
    private CustomerHomePage ownerFrame;

    // --- UI Components ---
    private JComboBox<String> ticketTypeComboBox;
    private JSpinner quantitySpinner;
    private JLabel totalAmountDisplayLabel;
    private JLabel currentBalanceDisplayLabel;

    public MuaVe(CustomerHomePage owner) {
        this.ownerFrame = owner;
         if (this.ownerFrame == null || !this.ownerFrame.isUserCurrentlyLoggedIn()) {
             JOptionPane.showMessageDialog(null, "Lỗi: Không thể mở màn hình Mua Vé do không có thông tin người dùng.", "Lỗi Người Dùng", JOptionPane.ERROR_MESSAGE);
             SwingUtilities.invokeLater(() -> this.dispose());
             return;
        }
        // setTitle("Mua Vé Gửi Xe - " + owner.getUserFullName());
        // setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        // setSize(550, 480);
        // setLocationRelativeTo(owner);
        initComponentsCustom();
        updateCurrentBalanceDisplay();
        updateTotalAmount();
    }

    private void initComponentsCustom() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(WHITE_COLOR);

        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        headerPanel.setBackground(HEADER_BACKGROUND);
        headerPanel.setPreferredSize(new Dimension(0, 60));
        JLabel titleLabel = new JLabel("Mua Vé Gửi Xe Trực Tuyến");
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(WHITE_COLOR);
        headerPanel.add(titleLabel);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(LIGHT_GRAY_FORM_BG);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200,200,200), 1),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10); // Khoảng cách giữa các component
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Current Balance
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel balanceTextLabel = new JLabel("Số dư hiện tại:");
        balanceTextLabel.setFont(REGULAR_FONT);
        formPanel.add(balanceTextLabel, gbc);

        gbc.gridx = 1; gbc.gridy = 0;
        currentBalanceDisplayLabel = new JLabel();
        currentBalanceDisplayLabel.setFont(PRICE_FONT);
        currentBalanceDisplayLabel.setForeground(new Color(0, 100, 0)); // Màu xanh lá cho số dư
        formPanel.add(currentBalanceDisplayLabel, gbc);
        
        // Ticket Type
        gbc.gridx = 0; gbc.gridy = 1;
        JLabel typeLabel = new JLabel("Loại vé:");
        typeLabel.setFont(REGULAR_FONT);
        formPanel.add(typeLabel, gbc);

        gbc.gridx = 1; gbc.gridy = 1;
        String[] ticketTypes = {"Vé Xe Máy (Ngày)", "Vé Ô Tô (Ngày)", "Vé Tuần", "Vé Tháng", "Vé Bảo Trì"};
        ticketTypeComboBox = new JComboBox<>(ticketTypes);
        ticketTypeComboBox.setFont(REGULAR_FONT);
        ticketTypeComboBox.addActionListener(e -> updateTotalAmount());
        formPanel.add(ticketTypeComboBox, gbc);

        // Quantity
        gbc.gridx = 0; gbc.gridy = 2;
        JLabel quantityLabelText = new JLabel("Số lượng:");
        quantityLabelText.setFont(REGULAR_FONT);
        formPanel.add(quantityLabelText, gbc);

        gbc.gridx = 1; gbc.gridy = 2;
        SpinnerModel spinnerModel = new SpinnerNumberModel(1, 1, 100, 1);
        quantitySpinner = new JSpinner(spinnerModel);
        quantitySpinner.setFont(REGULAR_FONT);
        quantitySpinner.addChangeListener(e -> updateTotalAmount());
        formPanel.add(quantitySpinner, gbc);

        // Total Amount
        gbc.gridx = 0; gbc.gridy = 3;
        JLabel totalTextLabel = new JLabel("Tổng tiền thanh toán:");
        totalTextLabel.setFont(BOLD_FONT);
        formPanel.add(totalTextLabel, gbc);

        gbc.gridx = 1; gbc.gridy = 3;
        totalAmountDisplayLabel = new JLabel();
        totalAmountDisplayLabel.setFont(PRICE_FONT.deriveFont(Font.BOLD, 18f)); // To hơn
        totalAmountDisplayLabel.setForeground(ORANGE_COLOR);
        formPanel.add(totalAmountDisplayLabel, gbc);
        
        mainPanel.add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setBackground(WHITE_COLOR);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10,0,0,0));

        JButton purchaseButton = new JButton("Thanh Toán");
        purchaseButton.setFont(BOLD_FONT);
        purchaseButton.setBackground(ORANGE_COLOR);
        purchaseButton.setForeground(WHITE_COLOR);
        purchaseButton.setIcon(loadIcon("payment.png", 18, 18));
        purchaseButton.setPreferredSize(new Dimension(180, 45));
        purchaseButton.addActionListener(e -> processPurchase());
        
        JButton backButton = new JButton("Quay Lại");
        backButton.setFont(BOLD_FONT);
        backButton.setIcon(loadIcon("back_arrow.png", 16, 16));
        backButton.setPreferredSize(new Dimension(150, 45));
        backButton.addActionListener(e -> {
            if (ownerFrame != null) {
                ownerFrame.showWelcomeScreen();
            }
        });

        buttonPanel.add(purchaseButton);
        buttonPanel.add(backButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        this.setLayout(new BorderLayout());
        this.add(mainPanel, BorderLayout.CENTER);
    }

    private void updateTotalAmount() {
        String selectedType = (String) ticketTypeComboBox.getSelectedItem();
        int quantity = (int) quantitySpinner.getValue();
        double unitPrice = 0;

        if (selectedType == null) return;

        switch (selectedType) {
            case "Vé Xe Máy (Ngày)": unitPrice = MOTORCYCLE_PRICE; break;
            case "Vé Ô Tô (Ngày)": unitPrice = CAR_PRICE; break;
            case "Vé Tuần": unitPrice = WEEKLY_PRICE; break;
            case "Vé Tháng": unitPrice = MONTHLY_PRICE; break;
            case "Vé Bảo Trì": unitPrice = MAINTENANCE_PRICE; break;
        }
        double total = unitPrice * quantity;
        totalAmountDisplayLabel.setText(formatCurrency(total));
    }
    
    private void updateCurrentBalanceDisplay() {
        if (ownerFrame != null && currentBalanceDisplayLabel != null) {
            currentBalanceDisplayLabel.setText(formatCurrency(ownerFrame.getWalletBalanceForCurrentUser())); // Dùng phương thức mới
        }
    }

    private void processPurchase() {
        if (ownerFrame == null) {
            JOptionPane.showMessageDialog(this.ownerFrame, "Lỗi hệ thống: Không tìm thấy thông tin khách hàng.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String selectedType = (String) ticketTypeComboBox.getSelectedItem();
        int quantity = (int) quantitySpinner.getValue();
        double unitPrice = 0;
        String ticketName = selectedType != null ? selectedType : "N/A";

        if (selectedType == null) {
             ownerFrame.showGlobalNotification("Vui lòng chọn loại vé.", "WARNING");
            return;
        }

        switch (selectedType) {
            case "Vé Xe Máy (Ngày)": unitPrice = MOTORCYCLE_PRICE; break;
            case "Vé Ô Tô (Ngày)": unitPrice = CAR_PRICE; break;
            case "Vé Tuần": unitPrice = WEEKLY_PRICE; break;
            case "Vé Tháng": unitPrice = MONTHLY_PRICE; break;
            case "Vé Bảo Trì": unitPrice = MAINTENANCE_PRICE; break;
            default:
                ownerFrame.showGlobalNotification("Loại vé không hợp lệ.", "ERROR");
                return;
        }
        
        double totalCost = unitPrice * quantity;

        if (ownerFrame.getWalletBalanceForCurrentUser() < totalCost) { // Dùng phương thức mới
            ownerFrame.showGlobalNotification("Số dư không đủ. Vui lòng nạp thêm tiền.", "WARNING");
            return;
        }

        int confirmation = JOptionPane.showConfirmDialog(this.ownerFrame,
                "Xác nhận mua " + quantity + " vé '" + ticketName + "' với tổng tiền là " + formatCurrency(totalCost) + "?",
                "Xác nhận mua vé",
                JOptionPane.YES_NO_OPTION);

        if (confirmation == JOptionPane.YES_OPTION) {
            boolean paymentSuccess = ownerFrame.updateWalletBalanceForCurrentUser(-totalCost, "PURCHASE_TICKET", "TICKET_ORDER_" + System.currentTimeMillis()); // Dùng phương thức mới
            
            if (paymentSuccess) {
                LichSuVeDaMua purchasedTicket = new LichSuVeDaMua(ticketName, quantity, unitPrice, totalCost);
                // Ngày mua sẽ được tự động gán trong addPurchasedTicketForCurrentUser nếu chưa có
                addPurchasedTicket(purchasedTicket);

                ownerFrame.showGlobalNotification("Mua vé thành công! Tổng tiền: " + formatCurrency(totalCost), "SUCCESS");
                updateCurrentBalanceDisplay();
                if (ownerFrame != null) {
                    ownerFrame.showWelcomeScreen();
                }
            }
            // Không cần thông báo lỗi ở đây vì updateWalletBalanceForCurrentUser đã làm
        }
    }
    
    // Hàm này gọi đến CustomerHomePage để thêm vé vào lịch sử
    public void addPurchasedTicket(LichSuVeDaMua ticket) {
        if (ownerFrame != null && ticket != null) {
            ownerFrame.addPurchasedTicketForCurrentUser(ticket); // Dùng phương thức mới
        }
    }

    // Hàm này ít dùng cho MuaVe, trừ khi là hoàn tiền.
    public void addToBalance(double amount) {
        if (ownerFrame != null && amount > 0) {
            String transactionRef = "MV_CREDIT_" + System.currentTimeMillis();
            boolean success = ownerFrame.updateWalletBalanceForCurrentUser(amount, "CREDIT_FROM_MUAVE", transactionRef); // Dùng phương thức mới
            if (success) {
                ownerFrame.showGlobalNotification("Đã cộng " + formatCurrency(amount) + " vào số dư của bạn.", "SUCCESS");
                updateCurrentBalanceDisplay();
            }
        } else if (amount <= 0) {
            ownerFrame.showGlobalNotification("Số tiền cộng vào phải lớn hơn 0.", "WARNING");
        }
    }

    private String formatCurrency(double amount) {
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        return currencyFormatter.format(amount);
    }

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
            testToken.setEntityId("TEST_USER_MV"); testToken.setEntityFullName("Test User MuaVe");
            testToken.setAccountId(100); testToken.setRole(RoleGroupConstants.CUSTOMER);
            
            CustomerHomePage fakeOwner = new CustomerHomePage(testToken);
            fakeOwner.updateWalletBalanceForCurrentUser(100000, "INITIAL_TOPUP_TEST", "INIT_REF");
            
             JFrame testFrame = new JFrame("Test Mua Vé");
             testFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
             testFrame.setSize(600, 500);
             MuaVe muaVePanel = new MuaVe(fakeOwner);
             testFrame.add(muaVePanel.getContentPane());
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
