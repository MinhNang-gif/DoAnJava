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
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class NapTien extends javax.swing.JFrame {
    // --- UI Styling Constants ---
    private final Color BLUE_COLOR = new Color(0, 123, 255);
    private final Color YELLOW_COLOR = new Color(255, 180, 0); // Màu vàng cam đậm hơn
    private final Color LIGHT_GRAY_PANEL_BG = new Color(248, 249, 250);
    private final Color TEXT_FIELD_BORDER_COLOR = new Color(200, 200, 200);
    private final Color HEADER_BACKGROUND = new Color(70, 130, 180); // Thống nhất màu header

    private final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 22);
    private final Font REGULAR_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    private final Font BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 16);
    private final Font BALANCE_FONT = new Font("Segoe UI", Font.BOLD, 16); // Font cho số dư

    // --- UI Components ---
    private JTextField amountField;
    private JLabel currentBalanceDisplayLabel;
    private CustomerHomePage ownerFrame;

    public NapTien(CustomerHomePage owner) {
        this.ownerFrame = owner;
        if (this.ownerFrame == null || !this.ownerFrame.isUserCurrentlyLoggedIn()) {
             JOptionPane.showMessageDialog(null, "Lỗi: Không thể mở màn hình Nạp Tiền do không có thông tin người dùng.", "Lỗi Người Dùng", JOptionPane.ERROR_MESSAGE);
             SwingUtilities.invokeLater(() -> this.dispose());
             return;
        }
        // setTitle("Nạp Tiền Vào Tài Khoản - " + owner.getUserFullName());
        // setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        // setSize(480, 400); // Kích thước frame
        // setLocationRelativeTo(owner);
        initComponentsCustom();
        updateCurrentBalanceDisplay();
    }

    private void initComponentsCustom() {
        JPanel mainPanel = new JPanel(new BorderLayout(10,10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15,15,15,15));
        mainPanel.setBackground(Color.WHITE);

        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        headerPanel.setBackground(HEADER_BACKGROUND);
        headerPanel.setPreferredSize(new Dimension(0, 60));
        JLabel titleLabel = new JLabel("Nạp Tiền Vào Tài Khoản Ví");
        titleLabel.setFont(TITLE_FONT.deriveFont(20f)); // Giảm nhẹ kích thước
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(LIGHT_GRAY_PANEL_BG);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(TEXT_FIELD_BORDER_COLOR),
            BorderFactory.createEmptyBorder(25,25,25,25) // Tăng padding
        ));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12,8,12,8); // Tăng khoảng cách
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0;
        JLabel balanceTextLabel = new JLabel("Số dư hiện tại:");
        balanceTextLabel.setFont(REGULAR_FONT);
        formPanel.add(balanceTextLabel, gbc);

        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0; // Cho phép label số dư co giãn
        currentBalanceDisplayLabel = new JLabel();
        currentBalanceDisplayLabel.setFont(BALANCE_FONT);
        currentBalanceDisplayLabel.setForeground(BLUE_COLOR);
        formPanel.add(currentBalanceDisplayLabel, gbc);
        gbc.weightx = 0; // Reset weight

        gbc.gridx = 0; gbc.gridy = 1;
        JLabel amountLabelText = new JLabel("Số tiền cần nạp:");
        amountLabelText.setFont(REGULAR_FONT);
        formPanel.add(amountLabelText, gbc);

        gbc.gridx = 1; gbc.gridy = 1;
        JPanel amountInputPanel = new JPanel(new BorderLayout(5,0)); // Panel cho JTextField và VNĐ
        amountInputPanel.setOpaque(false);
        amountField = new JTextField(15);
        amountField.setFont(REGULAR_FONT.deriveFont(15f)); // Font to hơn cho input
        amountField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(TEXT_FIELD_BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(8, 8, 8, 8) // Tăng padding cho input
        ));
        amountField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) { validateInput(); }
            public void removeUpdate(DocumentEvent e) { validateInput(); }
            public void insertUpdate(DocumentEvent e) { validateInput(); }
            public void validateInput() {
                String text = amountField.getText();
                if (!text.matches("\\d*")) {
                    SwingUtilities.invokeLater(() -> 
                        amountField.setText(text.replaceAll("[^\\d]", "")));
                }
            }
        });
        amountInputPanel.add(amountField, BorderLayout.CENTER);
        JLabel vndLabel = new JLabel("VNĐ");
        vndLabel.setFont(REGULAR_FONT);
        vndLabel.setBorder(new EmptyBorder(0,5,0,0)); // Padding trái cho VNĐ
        amountInputPanel.add(vndLabel, BorderLayout.EAST);
        formPanel.add(amountInputPanel, gbc);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10,0,0,0));

        JButton topUpButton = new JButton("Nạp Tiền");
        topUpButton.setFont(BUTTON_FONT);
        topUpButton.setBackground(YELLOW_COLOR);
        topUpButton.setForeground(Color.BLACK);
        topUpButton.setIcon(loadIcon("top_up_wallet.png", 18,18));
        topUpButton.setPreferredSize(new Dimension(160, 45));
        topUpButton.addActionListener(e -> processTopUp());

        JButton backButton = new JButton("Quay Lại");
        backButton.setFont(BUTTON_FONT);
        backButton.setIcon(loadIcon("back_arrow.png", 16,16));
        backButton.setPreferredSize(new Dimension(150, 45));
        backButton.addActionListener(e -> {
             if (ownerFrame != null) {
                ownerFrame.showWelcomeScreen();
            }
        });
        
        buttonPanel.add(topUpButton);
        buttonPanel.add(backButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        this.setLayout(new BorderLayout());
        this.add(mainPanel, BorderLayout.CENTER);
    }
    
    private void updateCurrentBalanceDisplay() {
        if (ownerFrame != null && currentBalanceDisplayLabel != null) {
            currentBalanceDisplayLabel.setText(formatCurrency(ownerFrame.getWalletBalanceForCurrentUser())); // Dùng phương thức mới
        }
    }

    private void processTopUp() {
        if (ownerFrame == null) {
            JOptionPane.showMessageDialog(this.ownerFrame, "Lỗi hệ thống: Không tìm thấy thông tin khách hàng.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String amountText = amountField.getText().trim();
        if (amountText.isEmpty()) {
            ownerFrame.showGlobalNotification("Vui lòng nhập số tiền cần nạp.", "WARNING");
            return;
        }
        try {
            double amount = Double.parseDouble(amountText);
            if (amount <= 0) {
                ownerFrame.showGlobalNotification("Số tiền nạp phải lớn hơn 0.", "WARNING");
                return;
            }

            int confirmation = JOptionPane.showConfirmDialog(this.ownerFrame,
                    "Xác nhận nạp " + formatCurrency(amount) + " vào tài khoản?",
                    "Xác nhận nạp tiền",
                    JOptionPane.YES_NO_OPTION);

            if (confirmation == JOptionPane.YES_OPTION) {
                addToBalance(amount);
            }

        } catch (NumberFormatException ex) {
            ownerFrame.showGlobalNotification("Số tiền nhập không hợp lệ. Vui lòng chỉ nhập số.", "ERROR");
        }
    }

    public void addToBalance(double amount) {
        if (ownerFrame != null && amount > 0) {
            String transactionRef = "TOPUP_" + System.currentTimeMillis();
            boolean success = ownerFrame.updateWalletBalanceForCurrentUser(amount, "TOP_UP", transactionRef); // Dùng phương thức mới
            if (success) {
                // ownerFrame.showGlobalNotification("Nạp tiền thành công! Số tiền: " + formatCurrency(amount), "SUCCESS"); // Đã có trong CustomerHomePage
                updateCurrentBalanceDisplay();
                amountField.setText("");
                 if (ownerFrame != null) {
                    ownerFrame.showWelcomeScreen();
                }
            }
            // Không cần báo lỗi ở đây nữa
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
    
    public static void main(String args[]) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(new FlatLightLaf()); } catch (Exception e) { e.printStackTrace(); }
             UserToken testToken = new UserToken();
             testToken.setEntityId("TEST_USER_NT"); testToken.setEntityFullName("Test User NapTien");
             testToken.setAccountId(101); testToken.setRole(RoleGroupConstants.CUSTOMER);
            
            CustomerHomePage fakeOwner = new CustomerHomePage(testToken);
            
            JFrame testFrame = new JFrame("Test Nạp Tiền");
            testFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            testFrame.setSize(500, 450);
            NapTien napTienPanel = new NapTien(fakeOwner);
            testFrame.add(napTienPanel.getContentPane());
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
