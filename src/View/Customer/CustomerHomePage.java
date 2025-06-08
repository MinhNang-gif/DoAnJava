package View.Customer;

import Process.RoleGroupConstants;
import Process.UserToken;
import View.DangNhap;

import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Properties;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.text.NumberFormat;
import java.util.Locale;
import java.awt.image.BufferedImage;
import java.util.Date;
import java.text.SimpleDateFormat;


// Lớp RoundedBorder
class RoundedBorder implements Border {
    private int radius;
    private Color color;
    private int thickness;

    public RoundedBorder(Color color, int radius, int thickness) {
        this.color = color;
        this.radius = radius;
        this.thickness = thickness;
    }

    public Insets getBorderInsets(Component c) {
        return new Insets(this.radius + this.thickness, this.radius + this.thickness, this.radius + this.thickness, this.radius + this.thickness);
    }

    public boolean isBorderOpaque() {
        return true;
    }

    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(this.color);
        g2d.setStroke(new BasicStroke(this.thickness));
        g2d.drawRoundRect(x + thickness / 2, y + thickness / 2, width - thickness, height - thickness, radius, radius);
        g2d.dispose();
    }
}

// Placeholder for TransactionRecord if not defined elsewhere
class TransactionRecord {
    String userID;
    String type;
    double amount;
    Date timestamp;
    String referenceID;
    String status;

    public TransactionRecord(String userID, String type, double amount, Date timestamp, String referenceID, String status) {
        this.userID = userID;
        this.type = type;
        this.amount = amount;
        this.timestamp = timestamp;
        this.referenceID = referenceID;
        this.status = status;
    }

    @Override
    public String toString() {
        return "TransactionRecord{" +
                "userID='" + userID + '\'' +
                ", type='" + type + '\'' +
                ", amount=" + amount +
                ", timestamp=" + timestamp +
                ", referenceID='" + referenceID + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}

public class CustomerHomePage extends javax.swing.JFrame {
    // --- Constants for UI Styling (Giữ nguyên) ---
    private final Color PRIMARY_COLOR = new Color(0, 123, 255);
    private final Color SIDEEBAR_BACKGROUND = new Color(45, 55, 70);
    private final Color MENU_ITEM_HOVER_BACKGROUND = new Color(70, 80, 95);
    private final Color WHITE_COLOR = Color.WHITE;
    private final Color LIGHT_GRAY_BACKGROUND = new Color(248, 249, 250);
    private final Color TEXT_COLOR_DARK = new Color(33, 37, 41);
    private final Color TEXT_COLOR_LIGHT = new Color(222, 226, 230);
    private final Color BORDER_COLOR = new Color(222, 226, 230);
    private final Font FONT_TITLE_APP = new Font("Segoe UI", Font.BOLD, 22);
    private final Font FONT_PROFILE_NAME = new Font("Segoe UI", Font.BOLD, 17);
    private final Font FONT_PROFILE_DETAIL = new Font("Segoe UI", Font.PLAIN, 14);
    private final Font FONT_WELCOME = new Font("Segoe UI", Font.BOLD, 20);
    private final Font FONT_BALANCE_TEXT = new Font("Segoe UI", Font.BOLD, 14);
    private final Font FONT_BALANCE_VALUE = new Font("Segoe UI", Font.BOLD, 22);
    private final Font FONT_CARD_TITLE = new Font("Segoe UI", Font.BOLD, 16);
    private final Font FONT_CARD_DESC = new Font("Segoe UI", Font.PLAIN, 12);
    private final Font FONT_BUTTON_SIDEBAR = new Font("Segoe UI", Font.BOLD, 14);

    // --- UI Components ---
    private JPanel sidebarPanel;
    private JPanel mainPanelContainer;
    private CardLayout cardLayoutManager;
    private JLabel balanceDisplayLabel;
    private JLabel nameDisplayLabel;
    private JLabel profileIconDisplayLabel;
    private JLabel maKhachHangDisplayLabel;
    private JLabel accountIdDisplayLabel;

    // --- User and Data Management (NON-STATIC) ---
    private UserToken currentUserToken;
    private double currentUserBalance = 0;
    private List<LichSuVeDaMua> currentUserPurchasedTicketsList = new ArrayList<>();

    private final String USER_DATA_FILE_PREFIX = "user_data_";
    private final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    private Properties userDataProperties;
    private File userSpecificDataFile;

    private static final String BALANCE_PROPERTY_KEY = "walletBalance";
    private static final String TICKET_COUNT_PROPERTY_KEY = "purchasedTickets.count";
    private static final String TICKET_PREFIX_PROPERTY_KEY = "purchasedTickets.ticket";

    public CustomerHomePage(UserToken userToken) {
        System.out.println("CustomerHomePage: Constructor(UserToken) - Bắt đầu.");
        this.currentUserToken = userToken;

        if (!isValidUserToken(this.currentUserToken)) {
            System.out.println("CustomerHomePage: UserToken không hợp lệ. Gọi handleInvalidUserSession.");
            handleInvalidUserSession();
            // Quan trọng: Dừng thực thi constructor nếu token không hợp lệ
            // để ngăn việc cố gắng khởi tạo UI với dữ liệu null.
            return;
        }
        System.out.println("CustomerHomePage: UserToken hợp lệ. EntityID: " + this.currentUserToken.getEntityId());

        // Các bước này phải được gọi SAU KHI UserToken đã được xác nhận là hợp lệ.
        initializeUserDataStorage();
        loadUserDataForCurrentUser();

        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (UnsupportedLookAndFeelException e) {
            System.err.println("Không thể áp dụng FlatLaf LookAndFeel: " + e.getMessage());
        }

        // **QUAN TRỌNG**: Gọi initComponents() để thiết lập JFrame
        // (bao gồm cả setDefaultCloseOperation và setLayout nếu cần).
        initComponents();
        System.out.println("CustomerHomePage: Sau initComponents(). Layout của JFrame: " + (this.getLayout() != null ? this.getLayout().getClass().getName() : "null"));


        // Thiết lập UI chính
        setupMainApplicationUI();
        System.out.println("CustomerHomePage: Sau setupMainApplicationUI().");

        // Các cài đặt cho JFrame
        setTitle("Trang Chủ Khách Hàng - " + this.currentUserToken.getEntityFullName());
        setSize(1366, 768);
        setMinimumSize(new Dimension(1024, 700));
        // setDefaultCloseOperation đã được gọi trong initComponents() hoặc ở đây nếu initComponents() trống
        if(this.getDefaultCloseOperation() != JFrame.DO_NOTHING_ON_CLOSE) { // Chỉ set nếu chưa được set đúng
             setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        }
        setLocationRelativeTo(null);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                confirmAndExitApplication();
            }
        });
        refreshBalanceDisplay();
        System.out.println("CustomerHomePage: Khởi tạo hoàn tất.");
        // Lớp DangNhap sẽ gọi setVisible(true)
    }

    public CustomerHomePage() {
        this(DangNhap.currentUserToken);
    }

    private boolean isValidUserToken(UserToken token) {
        // ... (Giữ nguyên như phiên bản trước)
        if (token == null) { System.err.println("isValidUserToken: Token is null."); return false; }
        if (token.getEntityId() == null || token.getEntityId().trim().isEmpty()) { System.err.println("isValidUserToken: EntityId is null or empty."); return false; }
        if (token.getEntityFullName() == null || token.getEntityFullName().trim().isEmpty()) { System.err.println("isValidUserToken: EntityFullName is null or empty."); return false; }
        if (RoleGroupConstants.CUSTOMER == null) { System.err.println("isValidUserToken: RoleGroupConstants.CUSTOMER is null."); return false; }
        if (!RoleGroupConstants.CUSTOMER.equals(token.getRole())) { System.err.println("isValidUserToken: Role is not CUSTOMER. Actual: " + token.getRole()); return false; }
        return true;
    }

    private void handleInvalidUserSession() {
        // ... (Giữ nguyên như phiên bản trước)
        JOptionPane.showMessageDialog(null, "Phiên đăng nhập không hợp lệ hoặc thông tin khách hàng không đầy đủ. Vui lòng đăng nhập lại.", "Lỗi Truy Cập", JOptionPane.ERROR_MESSAGE);
        SwingUtilities.invokeLater(() -> {
            if (this.isDisplayable()) { this.dispose(); }
            new DangNhap().setVisible(true);
        });
    }

    private void initializeUserDataStorage() {
        // ... (Giữ nguyên như phiên bản trước)
         if (this.currentUserToken == null || this.currentUserToken.getEntityId() == null) {
            System.err.println("Lỗi nghiêm trọng: Không thể tạo userSpecificDataFile do currentUserToken hoặc entityId là null trong initializeUserDataStorage.");
            return;
        }
        String safeEntityId = this.currentUserToken.getEntityId().replaceAll("[^a-zA-Z0-9.-]", "_");
        this.userSpecificDataFile = new File(USER_DATA_FILE_PREFIX + safeEntityId + ".properties");
        this.userDataProperties = new Properties();
        System.out.println("initializeUserDataStorage: User data file path: " + this.userSpecificDataFile.getAbsolutePath());
    }

    private void confirmAndExitApplication() {
        // ... (Giữ nguyên như phiên bản trước)
        int choice = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn thoát ứng dụng?", "Xác nhận thoát", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (choice == JOptionPane.YES_OPTION) { saveUserDataForCurrentUser(); System.exit(0); }
    }

    private void setupMainApplicationUI() {
        System.out.println("setupMainApplicationUI: Bắt đầu thiết lập UI chính.");
        // Đảm bảo Content Pane của JFrame sử dụng BorderLayout
        Container contentPane = this.getContentPane();
        if (!(contentPane.getLayout() instanceof BorderLayout)) {
            contentPane.setLayout(new BorderLayout());
            System.out.println("setupMainApplicationUI: Đã thiết lập BorderLayout cho Content Pane của JFrame.");
        }

        // --- Sidebar Panel ---
        sidebarPanel = new JPanel();
        // ... (Code tạo sidebarPanel giữ nguyên như phiên bản trước)
        sidebarPanel.setBackground(SIDEEBAR_BACKGROUND); sidebarPanel.setPreferredSize(new Dimension(290, getHeight())); sidebarPanel.setLayout(new BorderLayout(0, 0)); sidebarPanel.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));
        JPanel profileDisplayPanel = new JPanel(); profileDisplayPanel.setLayout(new BoxLayout(profileDisplayPanel, BoxLayout.Y_AXIS)); profileDisplayPanel.setOpaque(false); profileDisplayPanel.setBorder(new EmptyBorder(15, 15, 30, 15));
        JLabel appTitleLabel = new JLabel("SMART PARKING"); appTitleLabel.setFont(FONT_TITLE_APP); appTitleLabel.setForeground(WHITE_COLOR); appTitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT); profileDisplayPanel.add(appTitleLabel); profileDisplayPanel.add(Box.createVerticalStrut(30));
        profileIconDisplayLabel = new JLabel(loadIcon("profile.png", 90, 90)); profileIconDisplayLabel.setAlignmentX(Component.CENTER_ALIGNMENT); profileDisplayPanel.add(profileIconDisplayLabel); profileDisplayPanel.add(Box.createVerticalStrut(15));
        String fullName = (currentUserToken != null && currentUserToken.getEntityFullName() != null) ? currentUserToken.getEntityFullName() : "Khách"; String entityId = (currentUserToken != null && currentUserToken.getEntityId() != null) ? currentUserToken.getEntityId() : "N/A"; int accountId = (currentUserToken != null) ? currentUserToken.getAccountId() : 0;
        nameDisplayLabel = new JLabel(fullName); nameDisplayLabel.setFont(FONT_PROFILE_NAME); nameDisplayLabel.setForeground(WHITE_COLOR); nameDisplayLabel.setAlignmentX(Component.CENTER_ALIGNMENT); profileDisplayPanel.add(nameDisplayLabel); profileDisplayPanel.add(Box.createVerticalStrut(7));
        maKhachHangDisplayLabel = new JLabel("Mã KH: " + entityId); maKhachHangDisplayLabel.setFont(FONT_PROFILE_DETAIL); maKhachHangDisplayLabel.setForeground(TEXT_COLOR_LIGHT); maKhachHangDisplayLabel.setAlignmentX(Component.CENTER_ALIGNMENT); profileDisplayPanel.add(maKhachHangDisplayLabel); profileDisplayPanel.add(Box.createVerticalStrut(7));
        accountIdDisplayLabel = new JLabel("ID Tài Khoản: " + accountId); accountIdDisplayLabel.setFont(FONT_PROFILE_DETAIL); accountIdDisplayLabel.setForeground(TEXT_COLOR_LIGHT); accountIdDisplayLabel.setAlignmentX(Component.CENTER_ALIGNMENT); profileDisplayPanel.add(accountIdDisplayLabel);
        JPanel menuPanel = new JPanel(); menuPanel.setOpaque(false); menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS)); menuPanel.add(Box.createVerticalGlue());
        JPanel bottomPanel = new JPanel(new BorderLayout()); bottomPanel.setOpaque(false); bottomPanel.setBorder(new EmptyBorder(10, 15, 10, 15)); JButton logoutButton = createSidebarButton("Đăng xuất", "logout.png"); logoutButton.addActionListener(e -> handleLogout()); bottomPanel.add(logoutButton, BorderLayout.CENTER);
        sidebarPanel.add(profileDisplayPanel, BorderLayout.NORTH); sidebarPanel.add(menuPanel, BorderLayout.CENTER); sidebarPanel.add(bottomPanel, BorderLayout.SOUTH);
        System.out.println("setupMainApplicationUI: SidebarPanel đã được tạo.");

        // --- Main Content Panel (sử dụng CardLayout) ---
        cardLayoutManager = new CardLayout();
        mainPanelContainer = new JPanel(cardLayoutManager);
        mainPanelContainer.setBackground(LIGHT_GRAY_BACKGROUND);
        System.out.println("setupMainApplicationUI: mainPanelContainer và cardLayoutManager đã được khởi tạo.");

        JPanel welcomeScreenPanel = createWelcomeScreenPanel();
        if (welcomeScreenPanel != null) {
            mainPanelContainer.add(welcomeScreenPanel, "WelcomeScreen");
            System.out.println("setupMainApplicationUI: welcomeScreenPanel đã được thêm vào mainPanelContainer.");
        } else {
            System.err.println("LỖI: createWelcomeScreenPanel() trả về null!");
            JPanel errorPanel = new JPanel(new GridBagLayout()); errorPanel.setBackground(Color.PINK); JLabel errorLabel = new JLabel("Lỗi: Không thể tải màn hình chính."); errorLabel.setFont(FONT_WELCOME); errorPanel.add(errorLabel); mainPanelContainer.add(errorPanel, "ErrorScreen"); cardLayoutManager.show(mainPanelContainer, "ErrorScreen");
        }

        // Thêm các panel vào Content Pane của JFrame
        contentPane.add(sidebarPanel, BorderLayout.WEST);
        contentPane.add(mainPanelContainer, BorderLayout.CENTER);
        System.out.println("setupMainApplicationUI: sidebarPanel và mainPanelContainer đã được thêm vào Content Pane.");

        if (welcomeScreenPanel != null) {
            cardLayoutManager.show(mainPanelContainer, "WelcomeScreen");
            System.out.println("setupMainApplicationUI: Đã gọi cardLayoutManager.show(\"WelcomeScreen\").");
        }

        // Quan trọng: validate() để cập nhật layout
        // this.validate(); // Có thể không cần thiết nếu setVisible(true) được gọi sau bởi DangNhap
        // this.repaint(); // Ít khi cần gọi repaint() trực tiếp ở đây
        System.out.println("setupMainApplicationUI: Hoàn tất thiết lập UI chính.");
    }

    private void handleLogout() {
        // ... (Giữ nguyên)
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn đăng xuất?", "Xác nhận đăng xuất", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) { saveUserDataForCurrentUser(); DangNhap.currentUserToken = null; this.dispose(); new DangNhap().setVisible(true); }
    }

    private JPanel createWelcomeScreenPanel() {
        // ... (Giữ nguyên, đảm bảo sử dụng currentUserToken đã được kiểm tra)
        System.out.println("createWelcomeScreenPanel: Bắt đầu tạo Welcome Screen.");
        JPanel welcomePanel = new JPanel(new BorderLayout(20,20));
        welcomePanel.setOpaque(false);
        welcomePanel.setBorder(new EmptyBorder(20, 30, 20, 30));

        JPanel topBarPanel = new JPanel(new BorderLayout(20,0));
        topBarPanel.setOpaque(false);
        String welcomeMessage = "Chào mừng trở lại!";
        if (currentUserToken != null && currentUserToken.getEntityFullName() != null) {
            welcomeMessage = "Chào mừng trở lại, " + currentUserToken.getEntityFullName() + "!";
        }
        JLabel welcomeTextLabel = new JLabel(welcomeMessage);
        welcomeTextLabel.setFont(FONT_WELCOME);
        welcomeTextLabel.setForeground(TEXT_COLOR_DARK);

        JPanel balanceContainer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        balanceContainer.setOpaque(false);
        JLabel balanceLabelText = new JLabel("Số dư của bạn: ");
        balanceLabelText.setFont(FONT_BALANCE_TEXT);
        balanceLabelText.setForeground(TEXT_COLOR_DARK);
        balanceDisplayLabel = new JLabel(currencyFormatter.format(this.currentUserBalance));
        balanceDisplayLabel.setFont(FONT_BALANCE_VALUE);
        balanceDisplayLabel.setForeground(PRIMARY_COLOR);
        balanceContainer.add(balanceLabelText);
        balanceContainer.add(balanceDisplayLabel);

        topBarPanel.add(welcomeTextLabel, BorderLayout.WEST);
        topBarPanel.add(balanceContainer, BorderLayout.EAST);

        // Thay đổi GridLayout từ (2,4) thành (2,3)
        JPanel contentCardsPanel = new JPanel(new GridLayout(2, 3, 25, 25));
        contentCardsPanel.setOpaque(false);
        contentCardsPanel.setBorder(new EmptyBorder(25,0,0,0));
        contentCardsPanel.add(createActionCard("Mua vé", "buy_ticket.png", "Chọn và mua vé gửi xe", e -> navigateToScreen("Mua vé")));
        contentCardsPanel.add(createActionCard("Nạp tiền", "top_up.png", "Nạp thêm tiền vào tài khoản", e -> navigateToScreen("Nạp tiền")));
        contentCardsPanel.add(createActionCard("Lịch sử Vé", "history.png", "Xem lịch sử vé đã mua", e -> navigateToScreen("Lịch sử Vé")));
        contentCardsPanel.add(createActionCard("Danh sách hóa đơn", "bill.png", "Xem các hóa đơn để thanh toán", e -> navigateToScreen("Danh sách hóa đơn")));
        contentCardsPanel.add(createActionCard("Dịch vụ bảo dưỡng", "maintenance_service.png", "Đặt lịch, xem lịch sử bảo dưỡng", e -> navigateToScreen("Dịch vụ bảo dưỡng")));
        contentCardsPanel.add(createActionCard("Cài đặt tài khoản", "account_setting.png", "Cài đặt thông tin & mật khẩu", e -> navigateToScreen("Cài đặt tài khoản")));
        // Đã xóa 2 ActionCard: "Thông tin vé" và "Liên hệ hỗ trợ"

        welcomePanel.add(topBarPanel, BorderLayout.NORTH);
        welcomePanel.add(contentCardsPanel, BorderLayout.CENTER);
        System.out.println("createWelcomeScreenPanel: Welcome Screen đã được tạo.");
        return welcomePanel;
    }

    private JButton createSidebarButton(String text, String iconName) {
        // ... (Giữ nguyên)
        JButton button = new JButton(text); button.setIcon(loadIcon(iconName, 20, 20)); button.setFont(FONT_BUTTON_SIDEBAR); button.setForeground(TEXT_COLOR_LIGHT); button.setBackground(new Color(70, 80, 95)); button.setFocusPainted(false); button.setBorder(new EmptyBorder(12, 20, 12, 20)); button.setCursor(new Cursor(Cursor.HAND_CURSOR)); button.setHorizontalAlignment(JButton.CENTER); button.setIconTextGap(12);
        button.addMouseListener(new MouseAdapter() { @Override public void mouseEntered(MouseEvent e) { button.setBackground(MENU_ITEM_HOVER_BACKGROUND.brighter()); } @Override public void mouseExited(MouseEvent e) { button.setBackground(new Color(70, 80, 95));} });
        return button;
    }
    private JPanel createActionCard(String title, String iconName, String description, ActionListener action) {
        // ... (Giữ nguyên)
        JPanel card = new JPanel(new GridBagLayout()); card.setBackground(WHITE_COLOR); card.setBorder(BorderFactory.createCompoundBorder(new RoundedBorder(BORDER_COLOR, 15, 1), new EmptyBorder(15, 15, 15, 15))); card.setCursor(new Cursor(Cursor.HAND_CURSOR)); GridBagConstraints gbc = new GridBagConstraints(); gbc.gridwidth = GridBagConstraints.REMAINDER; gbc.anchor = GridBagConstraints.CENTER; gbc.insets = new Insets(5, 0, 5, 0);
        JLabel iconLabel = new JLabel(loadIcon(iconName, 48, 48)); card.add(iconLabel, gbc); JLabel titleLabel = new JLabel(title); titleLabel.setFont(FONT_CARD_TITLE); titleLabel.setForeground(TEXT_COLOR_DARK); card.add(titleLabel, gbc);
        JLabel descLabel = new JLabel("<html><body style='text-align: center; width: 150px;'>" + description + "</body></html>"); descLabel.setFont(FONT_CARD_DESC); descLabel.setForeground(new Color(108, 117, 125)); card.add(descLabel, gbc);
        card.addMouseListener(new MouseAdapter() { Border originalBorder = card.getBorder(); Border hoverBorder = BorderFactory.createCompoundBorder(new RoundedBorder(PRIMARY_COLOR, 15, 2), new EmptyBorder(15, 15, 15, 15) ); @Override public void mouseClicked(MouseEvent e) { if (action != null) action.actionPerformed(null); } @Override public void mouseEntered(MouseEvent e) { card.setBorder(hoverBorder); } @Override public void mouseExited(MouseEvent e) { card.setBorder(originalBorder); } });
        card.setPreferredSize(new Dimension(210, 190));
        return card;
    }
    private ImageIcon loadIcon(String iconName, int width, int height) {
        // ... (Giữ nguyên, đảm bảo trả về placeholder nếu lỗi)
        String resourcePath = "/icons/" + iconName; URL imgUrl = getClass().getResource(resourcePath);
        if (imgUrl != null) { try { ImageIcon originalIcon = new ImageIcon(imgUrl); Image scaledImage = originalIcon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH); return new ImageIcon(scaledImage); } catch (Exception e) { System.err.println("Lỗi Exception: " + e.getMessage()); return createPlaceholderIcon(width, height, "X"); }
        } else { System.err.println("LỖI: Không tìm thấy: " + resourcePath); return createPlaceholderIcon(width, height, "?"); }
    }
    private ImageIcon createPlaceholderIcon(int width, int height, String text) {
        // ... (Giữ nguyên)
        BufferedImage placeholder = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB); Graphics2D g2d = placeholder.createGraphics(); g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); g2d.setColor(Color.LIGHT_GRAY); g2d.fillRect(0, 0, width, height); g2d.setColor(Color.DARK_GRAY); g2d.setFont(new Font("Arial", Font.BOLD, Math.min(width, height) / 2 + 2)); FontMetrics fm = g2d.getFontMetrics(); int x = (width - fm.stringWidth(text)) / 2; int y = (fm.getAscent() + (height - (fm.getAscent() + fm.getDescent())) / 2); g2d.drawString(text, x, y); g2d.dispose(); return new ImageIcon(placeholder);
    }

    private void navigateToScreen(String screenIdentifier) {
        // ... (Giữ nguyên logic điều hướng đã sửa ở lượt trước)
        JComponent screenComponent = null; String panelCardName = screenIdentifier.replaceAll("\\s+", "") + "Screen"; this.setEnabled(true);
        try {
            switch (screenIdentifier) {
                case "Mua vé": MuaVe mvi = new MuaVe(this); screenComponent = (JComponent) mvi.getContentPane(); break;
                case "Nạp tiền": NapTien nti = new NapTien(this); screenComponent = (JComponent) nti.getContentPane(); break;
                case "Lịch sử Vé": GioHang gh = new GioHang(this); screenComponent = (JComponent) gh.getContentPane(); break;
                case "Danh sách hóa đơn": this.setEnabled(false); DanhSachHoaDon dshd = new DanhSachHoaDon(this); dshd.setVisible(true); return;
                case "Dịch vụ bảo dưỡng": this.setEnabled(false); SuDungDichVuBaoDuong sddv = new SuDungDichVuBaoDuong(this); sddv.setVisible(true); return;
                case "Cài đặt tài khoản":
                    if (currentUserToken != null && currentUserToken.getEntityId() != null) {
                        this.setEnabled(false); CaiDatTaiKhoanKhachHang cdtk = new CaiDatTaiKhoanKhachHang(currentUserToken.getEntityId());
                        cdtk.addWindowListener(new WindowAdapter() { @Override public void windowClosed(WindowEvent e) { CustomerHomePage.this.setEnabled(true); CustomerHomePage.this.requestFocus(); } @Override public void windowClosing(WindowEvent e) { CustomerHomePage.this.setEnabled(true); CustomerHomePage.this.requestFocus(); } });
                        cdtk.setVisible(true);
                    } else { showGlobalNotification("Không thể xác định Mã Khách Hàng.", "ERROR"); } return;
                // Đã xóa case "Thông tin vé" và case "Hỗ trợ"
                default: showGlobalNotification("Chức năng không xác định.", "WARNING"); return;
            }
            if (screenComponent != null && cardLayoutManager != null && mainPanelContainer != null) {
                for (Component comp : mainPanelContainer.getComponents()) { if (panelCardName.equals(comp.getName())) { mainPanelContainer.remove(comp); break; } }
                screenComponent.setName(panelCardName); mainPanelContainer.add(screenComponent, panelCardName); cardLayoutManager.show(mainPanelContainer, panelCardName);
                this.setTitle(screenIdentifier + " - " + (currentUserToken != null ? currentUserToken.getEntityFullName() : ""));
                System.out.println("NavigateToScreen: Đã hiển thị " + panelCardName);
            } else { System.err.println("Lỗi navigateToScreen: null component cho " + screenIdentifier); }
        } catch (Exception ex) { ex.printStackTrace(); showGlobalNotification("Lỗi mở màn hình: " + ex.getMessage(), "ERROR"); showWelcomeScreen(); }
    }

    public void showWelcomeScreen() {
        // ... (Giữ nguyên)
        if (cardLayoutManager != null && mainPanelContainer != null) { cardLayoutManager.show(mainPanelContainer, "WelcomeScreen"); this.setTitle("Trang Chủ Khách Hàng - " + (currentUserToken != null ? currentUserToken.getEntityFullName() : "Khách")); refreshBalanceDisplay(); this.setEnabled(true); this.requestFocus(); System.out.println("CustomerHomePage: Đã quay lại WelcomeScreen."); }
        else { System.err.println("Lỗi showWelcomeScreen: cardLayoutManager hoặc mainPanelContainer là null."); }
    }

    private void loadUserDataForCurrentUser() {
        if (!isValidUserToken(this.currentUserToken) || userSpecificDataFile == null) {
             System.err.println("Không thể tải dữ liệu: User token không hợp lệ hoặc file dữ liệu chưa được khởi tạo.");
             this.currentUserBalance = 0.0;
             this.currentUserPurchasedTicketsList.clear();
             return;
        }

        try {
            if (!userSpecificDataFile.exists()) {
                if (userSpecificDataFile.createNewFile()) {
                    System.out.println("Tạo file dữ liệu mới cho người dùng: " + userSpecificDataFile.getName());
                    this.currentUserBalance = 0.0;
                    this.currentUserPurchasedTicketsList.clear();
                    saveUserDataForCurrentUser();
                } else {
                    System.err.println("Không thể tạo file dữ liệu: " + userSpecificDataFile.getName());
                }
                return;
            }
        } catch (IOException e) {
            System.err.println("Lỗi IO khi kiểm tra/tạo file dữ liệu: " + e.getMessage());
            this.currentUserBalance = 0.0;
            this.currentUserPurchasedTicketsList.clear();
            return;
        }

        try (InputStream input = new FileInputStream(userSpecificDataFile)) {
            userDataProperties.load(input);
            this.currentUserBalance = Double.parseDouble(userDataProperties.getProperty(BALANCE_PROPERTY_KEY, "0.0"));

            this.currentUserPurchasedTicketsList.clear();
            int ticketCount = Integer.parseInt(userDataProperties.getProperty(TICKET_COUNT_PROPERTY_KEY, "0"));
            // SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss"); // Không thấy dùng

            for (int i = 0; i < ticketCount; i++) {
                String name = userDataProperties.getProperty(TICKET_PREFIX_PROPERTY_KEY + i + ".name");
                if (name == null) continue;

                // **THÊM LẤY MAVEXE TỪ PROPERTIES (NẾU CÓ)**
                String maVeXeFromFile = userDataProperties.getProperty(TICKET_PREFIX_PROPERTY_KEY + i + ".maVeXe", "UNKNOWN_MAVEXE_" + i); // Giá trị mặc định nếu không có

                int quantity = Integer.parseInt(userDataProperties.getProperty(TICKET_PREFIX_PROPERTY_KEY + i + ".quantity", "0"));
                double unitPrice = Double.parseDouble(userDataProperties.getProperty(TICKET_PREFIX_PROPERTY_KEY + i + ".unitPrice", "0.0"));
                double totalPrice = Double.parseDouble(userDataProperties.getProperty(TICKET_PREFIX_PROPERTY_KEY + i + ".totalPrice", "0.0"));
                String purchaseDateStr = userDataProperties.getProperty(TICKET_PREFIX_PROPERTY_KEY + i + ".purchaseDate");

                // **SỬA CONSTRUCTOR Ở ĐÂY**
                LichSuVeDaMua ticket = new LichSuVeDaMua(maVeXeFromFile, name, quantity, unitPrice, totalPrice);
                if (purchaseDateStr != null && !purchaseDateStr.isEmpty()) {
                    ticket.setPurchaseDate(purchaseDateStr);
                }
                this.currentUserPurchasedTicketsList.add(ticket);
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("Lỗi khi tải dữ liệu từ " + userSpecificDataFile.getName() + ": " + e.getMessage());
            this.currentUserBalance = 0.0;
            this.currentUserPurchasedTicketsList.clear();
        }
    }

    public void saveUserDataForCurrentUser() {
        if (!isValidUserToken(this.currentUserToken) || userSpecificDataFile == null) {
            System.err.println("Không thể lưu dữ liệu: User token không hợp lệ hoặc file chưa được khởi tạo.");
            return;
       }

       userDataProperties.setProperty(BALANCE_PROPERTY_KEY, String.valueOf(this.currentUserBalance));
       userDataProperties.setProperty(TICKET_COUNT_PROPERTY_KEY, String.valueOf(this.currentUserPurchasedTicketsList.size()));

       for (int i = 0; i < this.currentUserPurchasedTicketsList.size(); i++) {
           LichSuVeDaMua ticket = this.currentUserPurchasedTicketsList.get(i);
           String prefix = TICKET_PREFIX_PROPERTY_KEY + i + ".";

           // **THÊM LƯU MAVEXE VÀO PROPERTIES**
           if (ticket.getMaVeXe() != null) { // Kiểm tra null trước khi lưu
               userDataProperties.setProperty(prefix + "maVeXe", ticket.getMaVeXe());
           } else {
               userDataProperties.remove(prefix + "maVeXe"); // Xóa nếu nó là null để tránh lưu chuỗi "null"
           }

           userDataProperties.setProperty(prefix + "name", ticket.getName());
           userDataProperties.setProperty(prefix + "quantity", String.valueOf(ticket.getQuantity()));
           userDataProperties.setProperty(prefix + "unitPrice", String.valueOf(ticket.getUnitPrice()));
           userDataProperties.setProperty(prefix + "totalPrice", String.valueOf(ticket.getTotalPrice()));
           if (ticket.getPurchaseDate() != null) {
                userDataProperties.setProperty(prefix + "purchaseDate", ticket.getPurchaseDate());
           }
       }

       try (OutputStream output = new FileOutputStream(userSpecificDataFile)) {
           userDataProperties.store(output, "User Data for " + currentUserToken.getEntityId());
           System.out.println("Đã lưu dữ liệu người dùng vào: " + userSpecificDataFile.getAbsolutePath());
       } catch (IOException e) {
           System.err.println("Lỗi khi lưu dữ liệu vào " + userSpecificDataFile.getName() + ": " + e.getMessage());
       }
   }

    public void refreshBalanceDisplay() {
        if (balanceDisplayLabel != null) {
            balanceDisplayLabel.setText(currencyFormatter.format(this.currentUserBalance));
        }
    }

    public UserToken getCurrentUser() {
        return this.currentUserToken;
    }

    public boolean isUserCurrentlyLoggedIn() {
        return isValidUserToken(this.currentUserToken);
    }

    public String getCurrentUserID() {
        return isUserCurrentlyLoggedIn() ? this.currentUserToken.getEntityId() : null;
    }

    public String getUserFullName() {
        return isUserCurrentlyLoggedIn() ? this.currentUserToken.getEntityFullName() : "Khách";
    }

    public double getWalletBalanceForCurrentUser() {
        return this.currentUserBalance;
    }

    public synchronized boolean updateWalletBalanceForCurrentUser(double amount, String transactionType, String transactionReferenceID) {
        if (!isUserCurrentlyLoggedIn()) {
            showGlobalNotification("Người dùng chưa đăng nhập, không thể cập nhật số dư.", "ERROR");
            return false;
        }
        if (this.currentUserBalance + amount < 0) {
            showGlobalNotification("Số dư không đủ để thực hiện giao dịch.", "WARNING");
            return false;
        }
        this.currentUserBalance += amount;
        logTransaction(new TransactionRecord(getCurrentUserID(), transactionType, amount, new Date(), transactionReferenceID, "COMPLETED"));
        saveUserDataForCurrentUser();
        refreshBalanceDisplay();
        // Thêm thông báo thành công nếu cần
        if (amount > 0 && "TOP_UP".equals(transactionType)) {
             showGlobalNotification("Nạp tiền thành công! Số tiền: " + currencyFormatter.format(amount) + ". Số dư mới: " + currencyFormatter.format(this.currentUserBalance), "SUCCESS");
        } else if (amount < 0 && "PURCHASE_TICKET".equals(transactionType)) {
             // Thông báo mua vé thành công đã có ở MuaVe, không cần ở đây
        } else if (amount !=0) { // Các loại giao dịch khác
            showGlobalNotification("Giao dịch thành công. Số dư mới: " + currencyFormatter.format(this.currentUserBalance), "SUCCESS");
        }
        return true;
    }

    public List<LichSuVeDaMua> getPurchasedTicketsForCurrentUser() {
        return Collections.unmodifiableList(new ArrayList<>(this.currentUserPurchasedTicketsList));
    }

    public void addPurchasedTicketForCurrentUser(LichSuVeDaMua ticket) {
        if (!isUserCurrentlyLoggedIn()) {
            showGlobalNotification("Người dùng chưa đăng nhập, không thể thêm vé.", "ERROR");
            return;
        }
        if (ticket != null) {
            if (ticket.getPurchaseDate() == null || ticket.getPurchaseDate().isEmpty()) {
                ticket.setPurchaseDate(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()));
            }
            this.currentUserPurchasedTicketsList.add(ticket);
            saveUserDataForCurrentUser();
        }
    }

    public void clearPurchasedTicketsHistoryForCurrentUser() {
        if (!isUserCurrentlyLoggedIn()) {
            showGlobalNotification("Người dùng chưa đăng nhập, không thể xóa lịch sử.", "ERROR");
            return;
        }
        this.currentUserPurchasedTicketsList.clear();
        saveUserDataForCurrentUser();
        showGlobalNotification("Lịch sử mua vé đã được xóa.", "INFO");
    }

    private void logTransaction(TransactionRecord record) {
        System.out.println("LOG TRANSACTION: " + record.toString());
    }

    public void showGlobalNotification(String message, String type) {
        int messageType = JOptionPane.INFORMATION_MESSAGE;
        String title = type;
        if ("ERROR".equalsIgnoreCase(type)) {
            messageType = JOptionPane.ERROR_MESSAGE;
            title = "Lỗi";
        } else if ("WARNING".equalsIgnoreCase(type)) {
            messageType = JOptionPane.WARNING_MESSAGE;
            title = "Cảnh báo";
        } else if ("SUCCESS".equalsIgnoreCase(type)) {
             messageType = JOptionPane.INFORMATION_MESSAGE;
             title = "Thành công";
        } else if ("INFO".equalsIgnoreCase(type)){
            title = "Thông báo";
        }
        JOptionPane.showMessageDialog(this, message, title, messageType);
    }

    public static void main(String args[]) {
        SwingUtilities.invokeLater(() -> {
             UserToken testToken = new UserToken();
             testToken.setEntityId("KH_MAIN_RUN");
             testToken.setEntityFullName("Main Run Test User");
             testToken.setAccountId(1001);
             if (RoleGroupConstants.CUSTOMER == null) {
                 System.err.println("Lỗi trong main: RoleGroupConstants.CUSTOMER chưa được định nghĩa!");
                 return;
             }
             testToken.setRole(RoleGroupConstants.CUSTOMER);

             CustomerHomePage homePage = new CustomerHomePage(testToken);
             // Chỉ setVisible nếu việc khởi tạo CustomerHomePage không bị return sớm do lỗi token
             if (homePage.currentUserToken != null && homePage.isValidUserToken(homePage.currentUserToken)) {
                 homePage.setVisible(true); // Quan trọng: Phải gọi setVisible để frame hiển thị
             } else {
                 System.err.println("Không thể hiển thị CustomerHomePage do lỗi khởi tạo hoặc UserToken không hợp lệ sau constructor.");
             }
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
