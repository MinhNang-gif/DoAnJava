package View.Admin;

import ConnectDB.ConnectionOracle; // Giữ nguyên import này
import com.formdev.flatlaf.FlatLightLaf;
import com.toedter.calendar.JDateChooser;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.math.BigDecimal;
import java.sql.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap; // Giữ thứ tự chèn cho các ngày trên biểu đồ
import java.util.Map;
import java.util.Set;
import java.util.TreeSet; // Để sắp xếp các ngày nếu cần
import java.util.Vector;

// Import cho JFreeChart
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter; // Để màu cột đồng nhất
import org.jfree.data.category.DefaultCategoryDataset;

// Import for AdminHomePage to enable back button functionality
import View.Admin.AdminHomePage;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.ui.RectangleInsets;




public class QuanLyDoanhThuVaThongKe extends javax.swing.JFrame {
    private static final Font FONT_LABEL = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font FONT_TEXT_FIELD = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font FONT_BUTTON = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font FONT_TABLE_HEADER = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font FONT_TABLE_CELL = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Color COLOR_PRIMARY = new Color(0, 120, 215);
    private static final Color COLOR_BACKGROUND = new Color(240, 245, 250);
    private static final Color COLOR_BLUE_CUSTOM = new Color(80, 120, 200);
    private static final Color COLOR_RED_CUSTOM = new Color(222, 78, 78);


    private JDateChooser dcStartDateRevenue, dcEndDateRevenue;
    private JTable tblRevenue;
    private DefaultTableModel modelRevenue;
    private JLabel lblTotalRevenue;
    private ChartPanel chartPanelRevenue;

    private JDateChooser dcStartDateIncomeExpense, dcEndDateIncomeExpense;
    private JTable tblIncome, tblExpense;
    private DefaultTableModel modelIncome, modelExpense;
    private JLabel lblTotalIncome, lblTotalExpense, lblNetProfit;
    private ChartPanel chartPanelIncomeExpense;

    private JTextField txtSearchInvoice;
    private JDateChooser dcStartDateInvoice, dcEndDateInvoice;
    private JTable tblInvoices, tblInvoiceDetails;
    private DefaultTableModel modelInvoices, modelInvoiceDetails;

    private final SimpleDateFormat displayDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    private final SimpleDateFormat queryDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private final SimpleDateFormat chartDailyKeyFormat = new SimpleDateFormat("dd/MM/yy");
    private final DecimalFormat currencyFormat = new DecimalFormat("###,##0");
    private final DecimalFormat currencyFormatVND = new DecimalFormat("###,##0 VND");


    public QuanLyDoanhThuVaThongKe() {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (UnsupportedLookAndFeelException ex) {
            System.err.println("Không thể khởi tạo Look and Feel: " + ex.getMessage());
        }

        setTitle("Quản Lý Doanh Thu và Thống Kê");
        setSize(1300, 850);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(COLOR_BACKGROUND);
        setLayout(new BorderLayout(10, 10));

        JLabel lblTitle = new JLabel("QUẢN LÝ DOANH THU VÀ THỐNG KÊ", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(COLOR_PRIMARY);
        lblTitle.setBorder(new EmptyBorder(15, 0, 15, 0));
        add(lblTitle, BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 15));

        tabbedPane.addTab("Thống Kê Doanh Thu", createRevenuePanel());
        tabbedPane.addTab("Thống Kê Thu Chi", createIncomeExpensePanel());
        tabbedPane.addTab("Tra Cứu Hóa Đơn", createInvoiceLookupPanel());

        add(tabbedPane, BorderLayout.CENTER);

        setInitialDates();
        setVisible(true);
    }

    private void setInitialDates() {
        Date currentDate = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(currentDate);

        if (dcEndDateRevenue != null) dcEndDateRevenue.setDate(currentDate);
        if (dcEndDateIncomeExpense != null) dcEndDateIncomeExpense.setDate(currentDate);
        if (dcEndDateInvoice != null) dcEndDateInvoice.setDate(currentDate);

        cal.add(Calendar.DAY_OF_MONTH, -6);
        Date startDateDefault = cal.getTime();

        if (dcStartDateRevenue != null) dcStartDateRevenue.setDate(startDateDefault);
        if (dcStartDateIncomeExpense != null) dcStartDateIncomeExpense.setDate(startDateDefault);
        if (dcStartDateInvoice != null) dcStartDateInvoice.setDate(startDateDefault);
    }

    private JDateChooser createJDateChooser() {
        JDateChooser dateChooser = new JDateChooser();
        dateChooser.setDateFormatString("dd/MM/yyyy");
        dateChooser.setFont(FONT_TEXT_FIELD);
        dateChooser.setPreferredSize(new Dimension(140, 30));
        return dateChooser;
    }

    private JPanel createBackButtonPanel() {
        JPanel backButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        backButtonPanel.setBackground(COLOR_BACKGROUND);

        JButton btnBack = new JButton("Quay lại Trang Chủ");
        btnBack.setFont(FONT_BUTTON);
        btnBack.addActionListener(e -> {
            new AdminHomePage().setVisible(true);
            this.dispose();
        });
        backButtonPanel.add(btnBack);
        return backButtonPanel;
    }

    private boolean areAllValuesZero(DefaultCategoryDataset dataset) {
        if (dataset == null || dataset.getRowCount() == 0 || dataset.getColumnCount() == 0) {
            return true;
        }
        for (int r = 0; r < dataset.getRowCount(); r++) {
            for (int c = 0; c < dataset.getColumnCount(); c++) {
                Number value = dataset.getValue(r, c);
                if (value != null && value.doubleValue() != 0.0) {
                    return false;
                }
            }
        }
        return true;
    }


    private JFreeChart createBarChart(DefaultCategoryDataset dataset, String title, String categoryAxisLabel, String valueAxisLabel, boolean isRevenueChart) {
        JFreeChart barChart = ChartFactory.createBarChart(
                title, categoryAxisLabel, valueAxisLabel, dataset,
                PlotOrientation.VERTICAL,
                !isRevenueChart, // Hiển thị legend cho biểu đồ Thu Chi nếu isRevenueChart là false
                true, false);

        barChart.setBackgroundPaint(Color.white);
        CategoryPlot plot = barChart.getCategoryPlot();

        if (plot == null) {
            System.err.println("Lỗi: CategoryPlot là null sau khi tạo biểu đồ.");
            return barChart; // Trả về biểu đồ cơ bản hoặc ném ngoại lệ
        }

        plot.setBackgroundPaint(COLOR_BACKGROUND);
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
        plot.setInsets(new RectangleInsets(5.0, 5.0, 25.0, 5.0)); // Giữ nguyên bottom inset để legend gần trục X

        CategoryItemRenderer genericRenderer = plot.getRenderer();

        if (genericRenderer instanceof BarRenderer) {
            BarRenderer renderer = (BarRenderer) genericRenderer;

            // Các cài đặt chung cho BarRenderer
            renderer.setDrawBarOutline(false); // Dòng này đã từng gây lỗi
            renderer.setDefaultItemLabelGenerator(new StandardCategoryItemLabelGenerator("{2}", currencyFormat));
            renderer.setDefaultItemLabelsVisible(true);
            renderer.setDefaultPositiveItemLabelPosition(new org.jfree.chart.labels.ItemLabelPosition(org.jfree.chart.labels.ItemLabelAnchor.OUTSIDE12, org.jfree.chart.ui.TextAnchor.BOTTOM_CENTER));
            renderer.setBarPainter(new StandardBarPainter()); // Áp dụng StandardBarPainter chung

            // Cài đặt riêng dựa theo loại biểu đồ
            if (isRevenueChart) {
                renderer.setSeriesPaint(0, COLOR_BLUE_CUSTOM);
                if (barChart.getLegend() != null) {
                    barChart.getLegend().setVisible(false); // Ẩn legend cho biểu đồ doanh thu
                }
                renderer.setMaximumBarWidth(0.25);
                renderer.setItemMargin(0.05);
            } else { // Biểu đồ Thu Chi
                // Cài đặt màu cho series Thu/Chi
                 if (dataset != null && dataset.getRowCount() > 0) {
                    if (dataset.getRowCount() >= 2 && dataset.getRowKeys().size() >= 2) {
                        int thuIndex = dataset.getRowIndex("Tổng Thu");
                        int chiIndex = dataset.getRowIndex("Tổng Chi");

                        if (thuIndex != -1) renderer.setSeriesPaint(thuIndex, COLOR_BLUE_CUSTOM);
                        if (chiIndex != -1) renderer.setSeriesPaint(chiIndex, COLOR_RED_CUSTOM);
                        
                        if (thuIndex == -1 && chiIndex == -1 && dataset.getRowCount() == 2) { // Fallback
                            renderer.setSeriesPaint(0, COLOR_BLUE_CUSTOM);
                            renderer.setSeriesPaint(1, COLOR_RED_CUSTOM);
                        }
                    } else if (dataset.getRowCount() == 1 && dataset.getRowKeys().size() == 1) {
                        Comparable<?> rowKey = dataset.getRowKey(0);
                        if (rowKey.equals("Tổng Thu")) {
                            renderer.setSeriesPaint(0, COLOR_BLUE_CUSTOM);
                        } else if (rowKey.equals("Tổng Chi")) {
                            renderer.setSeriesPaint(0, COLOR_RED_CUSTOM);
                        } else { 
                            renderer.setSeriesPaint(0, COLOR_PRIMARY);
                        }
                    }
                }
                renderer.setItemMargin(0.1);
                // Legend cho biểu đồ Thu Chi đã được kích hoạt bởi ChartFactory
            }
        } else {
            System.err.println("Lỗi: Renderer không phải là BarRenderer hoặc là null. Không thể áp dụng cài đặt cho BarRenderer.");
            if (genericRenderer == null) {
                System.err.println("Chi tiết: plot.getRenderer() trả về null.");
            } else {
                System.err.println("Chi tiết: plot.getRenderer() trả về kiểu: " + genericRenderer.getClass().getName());
            }
        }

        // Cấu hình trục Domain (X)
        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);
        domainAxis.setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 11));
        domainAxis.setCategoryMargin(0.20);
        domainAxis.setLowerMargin(0.02);
        domainAxis.setUpperMargin(0.02);

        // Cấu hình trục Range (Y)
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setNumberFormatOverride(currencyFormatVND);
        rangeAxis.setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 11));
        rangeAxis.setLowerBound(0.0);

        if (areAllValuesZero(dataset)) {
            double defaultUpperBound = 100000.0;
            double defaultTickUnit = 20000.0;
            if (defaultUpperBound <= 0.0) defaultUpperBound = 1.0;
            if (defaultTickUnit <= 0.0 || defaultTickUnit > defaultUpperBound) defaultTickUnit = defaultUpperBound / 5;
            if (defaultTickUnit == 0 && defaultUpperBound > 0) defaultTickUnit = defaultUpperBound / 5;
            else if (defaultTickUnit == 0 && defaultUpperBound == 0) defaultTickUnit = 1.0;

            rangeAxis.setRange(0.0, defaultUpperBound);
            if (defaultTickUnit > 0) {
                rangeAxis.setTickUnit(new NumberTickUnit(defaultTickUnit));
            } else {
                rangeAxis.setAutoTickUnitSelection(true);
            }
        } else {
            rangeAxis.setAutoRangeIncludesZero(true);
            rangeAxis.setUpperMargin(0.15);
            rangeAxis.setAutoTickUnitSelection(true);
        }

        // Cấu hình tiêu đề và Legend
        barChart.getTitle().setFont(new Font("Segoe UI", Font.BOLD, 18));
        LegendTitle legend = barChart.getLegend();
        if (legend != null) {
            legend.setItemFont(new Font("Segoe UI", Font.PLAIN, 12));
            // Việc hiển thị legend đã được quyết định khi gọi ChartFactory.createBarChart
            // !isRevenueChart (true cho Thu Chi, false cho Doanh Thu)
            // Nếu isRevenueChart=true (Doanh thu), legend sẽ không được tạo/hiển thị
            // Nếu isRevenueChart=false (Thu Chi), legend sẽ được tạo/hiển thị
        }
        return barChart;
    }

    // ... (các phương thức khác không đổi) ...
    // ... (createRevenuePanel, createIncomeExpensePanel, createInvoiceLookupPanel, etc.)
    // ... (Đảm bảo setPreferredSize cho chartPanelIncomeExpense là (1200, 500) trong createIncomeExpensePanel)

    private JScrollPane createRevenuePanel() {
        JPanel mainTabContentPanel = new JPanel(new BorderLayout(10, 10));
        mainTabContentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        mainTabContentPanel.setBackground(COLOR_BACKGROUND);

        JPanel pnlTopArea = new JPanel(new BorderLayout());
        pnlTopArea.setBackground(COLOR_BACKGROUND);

        JPanel pnlControls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        pnlControls.setBackground(COLOR_BACKGROUND);
        dcStartDateRevenue = createJDateChooser();
        dcEndDateRevenue = createJDateChooser();
        JButton btnViewRevenue = new JButton("Xem Doanh Thu");
        btnViewRevenue.setFont(FONT_BUTTON);
        btnViewRevenue.addActionListener(this::viewRevenueAction);

        pnlControls.add(new JLabel("Từ ngày:"));
        pnlControls.add(dcStartDateRevenue);
        pnlControls.add(new JLabel("Đến ngày:"));
        pnlControls.add(dcEndDateRevenue);
        pnlControls.add(btnViewRevenue);
        pnlTopArea.add(pnlControls, BorderLayout.WEST);

        pnlTopArea.add(createBackButtonPanel(), BorderLayout.EAST); 
        mainTabContentPanel.add(pnlTopArea, BorderLayout.NORTH);

        modelRevenue = new DefaultTableModel(new String[]{"Nguồn Doanh Thu", "Ngày Ghi Nhận", "Số Tiền (VND)"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        tblRevenue = new JTable(modelRevenue);
        styleTable(tblRevenue);
        JScrollPane tableScrollPane = new JScrollPane(tblRevenue);
        tableScrollPane.setPreferredSize(new Dimension(700, 200)); 

        lblTotalRevenue = new JLabel("Tổng Doanh Thu: 0 VND");
        lblTotalRevenue.setFont(FONT_LABEL);
        lblTotalRevenue.setHorizontalAlignment(SwingConstants.RIGHT);
        lblTotalRevenue.setBorder(new EmptyBorder(5,0,5,0));

        JPanel pnlTableAndTotal = new JPanel(new BorderLayout(5, 5));
        pnlTableAndTotal.setBackground(COLOR_BACKGROUND);
        pnlTableAndTotal.add(tableScrollPane, BorderLayout.CENTER);
        pnlTableAndTotal.add(lblTotalRevenue, BorderLayout.SOUTH);

        JPanel pnlResults = new JPanel(new BorderLayout(5, 10));
        pnlResults.setBackground(COLOR_BACKGROUND);
        pnlResults.add(pnlTableAndTotal, BorderLayout.NORTH);

        DefaultCategoryDataset initialRevenueDataset = new DefaultCategoryDataset();
        JFreeChart revenueChartObject = createBarChart(initialRevenueDataset, "Biểu đồ Doanh Thu Theo Ngày", "Ngày", "Số Tiền", true);
        chartPanelRevenue = new ChartPanel(revenueChartObject);
        chartPanelRevenue.setPreferredSize(new Dimension(1200, 500)); 

        pnlResults.add(chartPanelRevenue, BorderLayout.CENTER);
        mainTabContentPanel.add(pnlResults, BorderLayout.CENTER);

        JScrollPane tabScrollPane = new JScrollPane(mainTabContentPanel);
        tabScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        tabScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        return tabScrollPane;
    }

    private JScrollPane createIncomeExpensePanel() {
        JPanel mainTabContentPanel = new JPanel(new BorderLayout(10, 10));
        mainTabContentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        mainTabContentPanel.setBackground(COLOR_BACKGROUND);

        JPanel pnlTopArea = new JPanel(new BorderLayout());
        pnlTopArea.setBackground(COLOR_BACKGROUND);

        JPanel pnlControls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        pnlControls.setBackground(COLOR_BACKGROUND);
        dcStartDateIncomeExpense = createJDateChooser();
        dcEndDateIncomeExpense = createJDateChooser();
        JButton btnViewIncomeExpense = new JButton("Xem Thu Chi");
        btnViewIncomeExpense.setFont(FONT_BUTTON);
        btnViewIncomeExpense.addActionListener(this::viewIncomeExpenseAction);

        pnlControls.add(new JLabel("Từ ngày:"));
        pnlControls.add(dcStartDateIncomeExpense);
        pnlControls.add(new JLabel("Đến ngày:"));
        pnlControls.add(dcEndDateIncomeExpense);
        pnlControls.add(btnViewIncomeExpense);
        pnlTopArea.add(pnlControls, BorderLayout.WEST);

        pnlTopArea.add(createBackButtonPanel(), BorderLayout.EAST); 
        mainTabContentPanel.add(pnlTopArea, BorderLayout.NORTH);

        modelIncome = new DefaultTableModel(new String[]{"Ngày Thu", "Nguồn Thu", "Số Tiền (VND)"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        tblIncome = new JTable(modelIncome);
        styleTable(tblIncome);
        JScrollPane incomeTableScrollPane = new JScrollPane(tblIncome);
        incomeTableScrollPane.setPreferredSize(new Dimension(400, 180)); 

        JPanel pnlIncome = new JPanel(new BorderLayout(5, 5));
        pnlIncome.setBorder(BorderFactory.createTitledBorder("Khoản Thu"));
        pnlIncome.add(incomeTableScrollPane, BorderLayout.CENTER);
        lblTotalIncome = new JLabel("Tổng Thu: 0 VND");
        lblTotalIncome.setFont(FONT_LABEL);
        pnlIncome.add(lblTotalIncome, BorderLayout.SOUTH);

        modelExpense = new DefaultTableModel(new String[]{"Ngày Chi", "Lý Do Chi", "Số Tiền (VND)"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        tblExpense = new JTable(modelExpense);
        styleTable(tblExpense);
        JScrollPane expenseTableScrollPane = new JScrollPane(tblExpense);
        expenseTableScrollPane.setPreferredSize(new Dimension(400,180)); 

        JPanel pnlExpense = new JPanel(new BorderLayout(5, 5));
        pnlExpense.setBorder(BorderFactory.createTitledBorder("Khoản Chi"));
        pnlExpense.add(expenseTableScrollPane, BorderLayout.CENTER);
        lblTotalExpense = new JLabel("Tổng Chi: 0 VND");
        lblTotalExpense.setFont(FONT_LABEL);
        pnlExpense.add(lblTotalExpense, BorderLayout.SOUTH);

        JPanel pnlTables = new JPanel(new GridLayout(1, 2, 10, 0));
        pnlTables.setBackground(COLOR_BACKGROUND);
        pnlTables.add(pnlIncome);
        pnlTables.add(pnlExpense);

        lblNetProfit = new JLabel("Lợi Nhuận (Thu - Chi): 0 VND");
        lblNetProfit.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblNetProfit.setForeground(Color.BLUE);
        lblNetProfit.setHorizontalAlignment(SwingConstants.CENTER);
        lblNetProfit.setBorder(new EmptyBorder(8,0,8,0));

        JPanel pnlTablesAndProfit = new JPanel(new BorderLayout(5,10));
        pnlTablesAndProfit.setBackground(COLOR_BACKGROUND);
        pnlTablesAndProfit.add(pnlTables, BorderLayout.CENTER);
        pnlTablesAndProfit.add(lblNetProfit, BorderLayout.SOUTH);

        JPanel pnlResults = new JPanel(new BorderLayout(5, 10));
        pnlResults.setBackground(COLOR_BACKGROUND);
        pnlResults.add(pnlTablesAndProfit, BorderLayout.NORTH);

        DefaultCategoryDataset initialIEdataSet = new DefaultCategoryDataset();
        JFreeChart incomeExpenseChartObject = createBarChart(initialIEdataSet, "Biểu đồ Thu Chi Theo Ngày", "Ngày", "Số Tiền", false);
        chartPanelIncomeExpense = new ChartPanel(incomeExpenseChartObject);
        chartPanelIncomeExpense.setPreferredSize(new Dimension(1200, 500)); // Đã tăng chiều cao

        pnlResults.add(chartPanelIncomeExpense, BorderLayout.CENTER);
        mainTabContentPanel.add(pnlResults, BorderLayout.CENTER);

        JScrollPane tabScrollPane = new JScrollPane(mainTabContentPanel);
        tabScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        tabScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        return tabScrollPane;
    }

    // ... (phần còn lại không thay đổi nhiều)
    private JScrollPane createInvoiceLookupPanel() {
        JPanel mainTabContentPanel = new JPanel(new BorderLayout(10, 10));
        mainTabContentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        mainTabContentPanel.setBackground(COLOR_BACKGROUND);

        JPanel pnlTopArea = new JPanel(new BorderLayout());
        pnlTopArea.setBackground(COLOR_BACKGROUND);

        JPanel pnlControls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        pnlControls.setBackground(COLOR_BACKGROUND);
        txtSearchInvoice = new JTextField(18);
        txtSearchInvoice.setFont(FONT_TEXT_FIELD);
        dcStartDateInvoice = createJDateChooser();
        dcEndDateInvoice = createJDateChooser();
        JButton btnSearchInvoice = new JButton("Tìm Hóa Đơn");
        btnSearchInvoice.setFont(FONT_BUTTON);
        btnSearchInvoice.addActionListener(this::searchInvoicesAction);

        pnlControls.add(new JLabel("Tìm (Mã HĐ/KH, Tên KH):"));
        pnlControls.add(txtSearchInvoice);
        pnlControls.add(new JLabel("Từ ngày:"));
        pnlControls.add(dcStartDateInvoice);
        pnlControls.add(new JLabel("Đến ngày:"));
        pnlControls.add(dcEndDateInvoice);
        pnlControls.add(btnSearchInvoice);
        pnlTopArea.add(pnlControls, BorderLayout.WEST);

        pnlTopArea.add(createBackButtonPanel(), BorderLayout.EAST); 
        mainTabContentPanel.add(pnlTopArea, BorderLayout.NORTH);

        modelInvoices = new DefaultTableModel(new String[]{"Mã HĐ", "Mã KH", "Tên KH", "Ngày Lập", "Tổng Tiền (VND)", "Trạng Thái"}, 0){
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        tblInvoices = new JTable(modelInvoices);
        styleTable(tblInvoices);
        tblInvoices.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tblInvoices.getSelectedRow() != -1) {
                String maHoaDon = modelInvoices.getValueAt(tblInvoices.getSelectedRow(), 0).toString();
                loadInvoiceDetails(maHoaDon);
            }
        });

        modelInvoiceDetails = new DefaultTableModel(new String[]{"Mã DV", "Tên Dịch Vụ", "Đơn Giá", "Số Lượng", "Thành Tiền"}, 0){
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        tblInvoiceDetails = new JTable(modelInvoiceDetails);
        styleTable(tblInvoiceDetails);
        JPanel pnlDetails = new JPanel(new BorderLayout());
        pnlDetails.setBorder(BorderFactory.createTitledBorder("Chi Tiết Hóa Đơn"));
        pnlDetails.add(new JScrollPane(tblInvoiceDetails), BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(tblInvoices), pnlDetails);
        splitPane.setResizeWeight(0.55);
        splitPane.setOneTouchExpandable(true);

        mainTabContentPanel.add(splitPane, BorderLayout.CENTER);

        JScrollPane tabScrollPane = new JScrollPane(mainTabContentPanel);
        tabScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        tabScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        return tabScrollPane;
    }

    private void styleTable(JTable table) {
        table.getTableHeader().setFont(FONT_TABLE_HEADER);
        table.getTableHeader().setBackground(COLOR_PRIMARY);
        table.getTableHeader().setForeground(Color.WHITE);
        table.setFont(FONT_TABLE_CELL);
        table.setRowHeight(28);
        table.setGridColor(Color.LIGHT_GRAY);
        table.setSelectionBackground(COLOR_PRIMARY.brighter());
        table.setSelectionForeground(Color.WHITE);
        table.setAutoCreateRowSorter(true);
    }

    private void viewRevenueAction(ActionEvent e) {
        Date startDate = dcStartDateRevenue.getDate();
        Date endDate = dcEndDateRevenue.getDate();

        if (startDate == null || endDate == null || endDate.before(startDate)) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn ngày hợp lệ.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Timestamp startTimestamp = new Timestamp(startDate.getTime());
        Calendar endCal = Calendar.getInstance();
        endCal.setTime(endDate);
        endCal.set(Calendar.HOUR_OF_DAY, 23);
        endCal.set(Calendar.MINUTE, 59);
        endCal.set(Calendar.SECOND, 59);
        endCal.set(Calendar.MILLISECOND, 999);
        Timestamp endOfDayTimestamp = new Timestamp(endCal.getTimeInMillis());


        modelRevenue.setRowCount(0);
        BigDecimal totalRevenueValue = BigDecimal.ZERO;
        Map<String, BigDecimal> dailyRevenueData = new LinkedHashMap<>();

        try (Connection conn = ConnectionOracle.getOracleConnection()) {
            String sqlHoadon = "SELECT NGAYLAP, TONGTIEN FROM HOADON WHERE TRANGTHAI = 'DA THANH TOAN' AND NGAYLAP BETWEEN ? AND ? ORDER BY NGAYLAP ASC";
            try (PreparedStatement pstmt = conn.prepareStatement(sqlHoadon)) {
                pstmt.setTimestamp(1, startTimestamp);
                pstmt.setTimestamp(2, endOfDayTimestamp);
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    Timestamp ngayLap = rs.getTimestamp("NGAYLAP");
                    BigDecimal amount = rs.getBigDecimal("TONGTIEN");

                    Vector<Object> row = new Vector<>();
                    row.add("Hóa đơn dịch vụ");
                    row.add(displayDateFormat.format(ngayLap));
                    row.add(currencyFormat.format(amount));
                    modelRevenue.addRow(row);
                    totalRevenueValue = totalRevenueValue.add(amount);

                    String dayKey = chartDailyKeyFormat.format(ngayLap);
                    dailyRevenueData.merge(dayKey, amount, BigDecimal::add);
                }
            }

            String sqlLichSuGuiXe = "SELECT THOIGIANRA, TONGTIEN FROM LICHSUGUIXE WHERE THOIGIANRA BETWEEN ? AND ? ORDER BY THOIGIANRA ASC";
            try (PreparedStatement pstmt = conn.prepareStatement(sqlLichSuGuiXe)) {
                pstmt.setTimestamp(1, startTimestamp);
                pstmt.setTimestamp(2, endOfDayTimestamp);
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                     Timestamp thoiGianRa = rs.getTimestamp("THOIGIANRA");
                    BigDecimal amount = rs.getBigDecimal("TONGTIEN");

                    Vector<Object> row = new Vector<>();
                    row.add("Phí gửi xe (Lịch sử)");
                    row.add(displayDateFormat.format(thoiGianRa));
                    row.add(currencyFormat.format(amount));
                    modelRevenue.addRow(row);
                    totalRevenueValue = totalRevenueValue.add(amount);

                    String dayKey = chartDailyKeyFormat.format(thoiGianRa);
                    dailyRevenueData.merge(dayKey, amount, BigDecimal::add);
                }
            }
            lblTotalRevenue.setText(String.format("Tổng Doanh Thu: %s VND", currencyFormat.format(totalRevenueValue)));

            DefaultCategoryDataset revenueDataset = new DefaultCategoryDataset();
            if (!dailyRevenueData.isEmpty()) {
                for (Map.Entry<String, BigDecimal> entry : dailyRevenueData.entrySet()) {
                    revenueDataset.addValue(entry.getValue(), "Doanh thu", entry.getKey());
                }
            }
             if (modelRevenue.getRowCount() == 0 && dailyRevenueData.isEmpty()) { 
                 JOptionPane.showMessageDialog(this, "Không có dữ liệu doanh thu trong khoảng đã chọn.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            }


            String chartTitle = "Biểu đồ Doanh Thu";
            if (startDate != null && endDate != null) {
                chartTitle = "Biểu đồ Doanh Thu: " + queryDateFormat.format(startDate) + " - " + queryDateFormat.format(endDate);
            }
            JFreeChart chart = createBarChart(revenueDataset, chartTitle , "Ngày", "Số Tiền", true);
            chartPanelRevenue.setChart(chart);


            int numberOfCategories = revenueDataset.getColumnCount();
            int newChartWidth = Math.max(1100, numberOfCategories * 70);
            chartPanelRevenue.setPreferredSize(new Dimension(newChartWidth, chartPanelRevenue.getPreferredSize().height));
            chartPanelRevenue.revalidate();
            chartPanelRevenue.repaint();


        } catch (SQLException | ClassNotFoundException ex) {
            JOptionPane.showMessageDialog(this, "Lỗi CSDL (Doanh thu): " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void viewIncomeExpenseAction(ActionEvent e) {
        Date startDateVal = dcStartDateIncomeExpense.getDate();
        Date endDateVal = dcEndDateIncomeExpense.getDate();

        if (startDateVal == null || endDateVal == null || endDateVal.before(startDateVal)) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn ngày hợp lệ.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Timestamp startTimestamp = new Timestamp(startDateVal.getTime());
        Calendar endCal = Calendar.getInstance();
        endCal.setTime(endDateVal);
        endCal.set(Calendar.HOUR_OF_DAY, 23);
        endCal.set(Calendar.MINUTE, 59);
        endCal.set(Calendar.SECOND, 59);
        endCal.set(Calendar.MILLISECOND, 999);
        Timestamp endOfDayTimestamp = new Timestamp(endCal.getTimeInMillis());

        java.sql.Date sqlStartDate = new java.sql.Date(startDateVal.getTime());
        java.sql.Date sqlEndDate = new java.sql.Date(endCal.getTimeInMillis());


        modelIncome.setRowCount(0);
        modelExpense.setRowCount(0);
        BigDecimal totalIncomeValue = BigDecimal.ZERO;
        BigDecimal totalExpenseValue = BigDecimal.ZERO;

        Map<String, BigDecimal> dailyIncomeData = new LinkedHashMap<>();
        Map<String, BigDecimal> dailyExpenseData = new LinkedHashMap<>();

        try (Connection conn = ConnectionOracle.getOracleConnection()) {
            String sqlHoadonIncome = "SELECT NGAYLAP, TONGTIEN FROM HOADON WHERE TRANGTHAI = 'DA THANH TOAN' AND NGAYLAP BETWEEN ? AND ? ORDER BY NGAYLAP ASC";
            try (PreparedStatement pstmt = conn.prepareStatement(sqlHoadonIncome)) {
                pstmt.setTimestamp(1, startTimestamp);
                pstmt.setTimestamp(2, endOfDayTimestamp);
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    Timestamp ngayLap = rs.getTimestamp("NGAYLAP");
                    BigDecimal amount = rs.getBigDecimal("TONGTIEN");
                    modelIncome.addRow(new Object[]{displayDateFormat.format(ngayLap), "Hóa đơn dịch vụ", currencyFormat.format(amount)});
                    totalIncomeValue = totalIncomeValue.add(amount);
                    dailyIncomeData.merge(chartDailyKeyFormat.format(ngayLap), amount, BigDecimal::add);
                }
            }
            String sqlLichSuGuiXeIncome = "SELECT THOIGIANRA, TONGTIEN FROM LICHSUGUIXE WHERE THOIGIANRA BETWEEN ? AND ? ORDER BY THOIGIANRA ASC";
            try (PreparedStatement pstmt = conn.prepareStatement(sqlLichSuGuiXeIncome)) {
                pstmt.setTimestamp(1, startTimestamp);
                pstmt.setTimestamp(2, endOfDayTimestamp);
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    Timestamp thoiGianRa = rs.getTimestamp("THOIGIANRA");
                    BigDecimal amount = rs.getBigDecimal("TONGTIEN");
                    modelIncome.addRow(new Object[]{displayDateFormat.format(thoiGianRa), "Phí gửi xe (Lịch sử)", currencyFormat.format(amount)});
                    totalIncomeValue = totalIncomeValue.add(amount);
                    dailyIncomeData.merge(chartDailyKeyFormat.format(thoiGianRa), amount, BigDecimal::add);
                }
            }
            lblTotalIncome.setText(String.format("Tổng Thu: %s VND", currencyFormat.format(totalIncomeValue)));

            String sqlThietBiExpense = "SELECT NGAYNHAP, TENTHIETBI, GIANHAP FROM THIETBI WHERE NGAYNHAP BETWEEN ? AND ? ORDER BY NGAYNHAP ASC";
            try (PreparedStatement pstmt = conn.prepareStatement(sqlThietBiExpense)) {
                pstmt.setDate(1, sqlStartDate);
                pstmt.setDate(2, sqlEndDate);
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    java.sql.Date ngayNhapSQL = rs.getDate("NGAYNHAP");
                    java.util.Date ngayNhapUtil = new java.util.Date(ngayNhapSQL.getTime());

                    BigDecimal amount = rs.getBigDecimal("GIANHAP");
                    modelExpense.addRow(new Object[]{queryDateFormat.format(ngayNhapUtil), "Mua TB: " + rs.getString("TENTHIETBI"), currencyFormat.format(amount)});
                    totalExpenseValue = totalExpenseValue.add(amount);
                    dailyExpenseData.merge(chartDailyKeyFormat.format(ngayNhapUtil), amount, BigDecimal::add);
                }
            }
            lblTotalExpense.setText(String.format("Tổng Chi: %s VND", currencyFormat.format(totalExpenseValue)));

            BigDecimal netProfitValue = totalIncomeValue.subtract(totalExpenseValue);
            lblNetProfit.setText(String.format("Lợi Nhuận (Thu - Chi): %s VND", currencyFormat.format(netProfitValue)));
            lblNetProfit.setForeground(netProfitValue.compareTo(BigDecimal.ZERO) < 0 ? COLOR_RED_CUSTOM.darker() : new Color(0,100,0));

            DefaultCategoryDataset ieDataset = new DefaultCategoryDataset();
            Set<String> allActiveDays = new TreeSet<>(dailyIncomeData.keySet());
            allActiveDays.addAll(dailyExpenseData.keySet());

            if (!allActiveDays.isEmpty()) {
                for (String dayKey : allActiveDays) {
                    BigDecimal incomeForDay = dailyIncomeData.getOrDefault(dayKey, BigDecimal.ZERO);
                    BigDecimal expenseForDay = dailyExpenseData.getOrDefault(dayKey, BigDecimal.ZERO);
                    ieDataset.addValue(incomeForDay, "Tổng Thu", dayKey);
                    ieDataset.addValue(expenseForDay, "Tổng Chi", dayKey);
                }
            }

            if (modelIncome.getRowCount() == 0 && modelExpense.getRowCount() == 0 && allActiveDays.isEmpty()) { 
                JOptionPane.showMessageDialog(this, "Không có dữ liệu thu chi trong khoảng đã chọn.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            }


            String chartTitle = "Biểu đồ Thu Chi";
            if (startDateVal != null && endDateVal != null) {
                chartTitle = "Biểu đồ Thu Chi: " + queryDateFormat.format(startDateVal) + " - " + queryDateFormat.format(endDateVal);
            }
            JFreeChart chart = createBarChart(ieDataset, chartTitle, "Ngày", "Số Tiền", false);
            chartPanelIncomeExpense.setChart(chart);

            int numberOfCategoriesIE = ieDataset.getColumnCount();
            int newChartWidthIE = Math.max(1100, numberOfCategoriesIE * 90);
            chartPanelIncomeExpense.setPreferredSize(new Dimension(newChartWidthIE, chartPanelIncomeExpense.getPreferredSize().height));
            chartPanelIncomeExpense.revalidate();
            chartPanelIncomeExpense.repaint();

        } catch (SQLException | ClassNotFoundException ex) {
            JOptionPane.showMessageDialog(this, "Lỗi CSDL (Thu Chi): " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void searchInvoicesAction(ActionEvent e) {
        String searchTerm = txtSearchInvoice.getText().trim();
        Date startDate = dcStartDateInvoice.getDate();
        Date endDate = dcEndDateInvoice.getDate();

        if (startDate == null || endDate == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn ngày bắt đầu và ngày kết thúc cho hóa đơn.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (endDate.before(startDate)) {
            JOptionPane.showMessageDialog(this, "Ngày kết thúc không được trước ngày bắt đầu.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Timestamp startTimestamp = new Timestamp(startDate.getTime());
        Calendar endCal = Calendar.getInstance();
        endCal.setTime(endDate);
        endCal.set(Calendar.HOUR_OF_DAY, 23);
        endCal.set(Calendar.MINUTE, 59);
        endCal.set(Calendar.SECOND, 59);
        endCal.set(Calendar.MILLISECOND, 999);
        Timestamp endOfDayTimestamp = new Timestamp(endCal.getTimeInMillis());


        modelInvoices.setRowCount(0);
        modelInvoiceDetails.setRowCount(0);

        StringBuilder sqlBuilder = new StringBuilder(
            "SELECT H.MAHOADON, H.MAKH, K.TENKH, H.NGAYLAP, H.TONGTIEN, H.TRANGTHAI " +
            "FROM HOADON H " +
            "LEFT JOIN KHACHHANG K ON H.MAKH = K.MAKH " +
            "WHERE (H.NGAYLAP BETWEEN ? AND ?) "
        );

        if (!searchTerm.isEmpty()) {
            sqlBuilder.append("AND (UPPER(H.MAHOADON) LIKE UPPER(?) OR UPPER(H.MAKH) LIKE UPPER(?) OR (K.TENKH IS NOT NULL AND UPPER(K.TENKH) LIKE UPPER(?))) ");
        }
        sqlBuilder.append("ORDER BY H.NGAYLAP DESC");

        try (Connection conn = ConnectionOracle.getOracleConnection();
             PreparedStatement pstmt = conn.prepareStatement(sqlBuilder.toString())) {

            int paramIndex = 1;
            pstmt.setTimestamp(paramIndex++, startTimestamp);
            pstmt.setTimestamp(paramIndex++, endOfDayTimestamp);

            if (!searchTerm.isEmpty()) {
                String likeTerm = "%" + searchTerm.toUpperCase() + "%";
                pstmt.setString(paramIndex++, likeTerm);
                pstmt.setString(paramIndex++, likeTerm);
                pstmt.setString(paramIndex++, likeTerm);
            }

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getString("MAHOADON"));
                row.add(rs.getString("MAKH"));
                row.add(rs.getString("TENKH"));
                row.add(displayDateFormat.format(rs.getTimestamp("NGAYLAP")));
                row.add(currencyFormat.format(rs.getBigDecimal("TONGTIEN")));
                row.add(rs.getString("TRANGTHAI"));
                modelInvoices.addRow(row);
            }
            if (modelInvoices.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this, "Không tìm thấy hóa đơn nào khớp với tiêu chí.", "Thông Báo", JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (SQLException | ClassNotFoundException ex) {
            JOptionPane.showMessageDialog(this, "Lỗi kết nối hoặc truy vấn CSDL: " + ex.getMessage(), "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void loadInvoiceDetails(String maHoaDon) {
        modelInvoiceDetails.setRowCount(0);
        String sql = "SELECT CT.MADVBAODUONG, DVB.TENDV, DVB.GIA, CT.SOLUONG, (DVB.GIA * CT.SOLUONG) AS THANHTIEN " +
                     "FROM CHITIETHOADON CT " +
                     "JOIN DICHVUBAODUONG DVB ON CT.MADVBAODUONG = DVB.MADVBAODUONG " +
                     "WHERE CT.MAHOADON = ?";

        try (Connection conn = ConnectionOracle.getOracleConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, maHoaDon);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getString("MADVBAODUONG"));
                row.add(rs.getString("TENDV"));
                row.add(currencyFormat.format(rs.getBigDecimal("GIA")));
                row.add(rs.getInt("SOLUONG"));
                row.add(currencyFormat.format(rs.getBigDecimal("THANHTIEN")));
                modelInvoiceDetails.addRow(row);
            }
        } catch (SQLException | ClassNotFoundException ex) {
            JOptionPane.showMessageDialog(this, "Lỗi tải chi tiết hóa đơn: " + ex.getMessage(), "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (UnsupportedLookAndFeelException ex) {
            System.err.println("Không thể khởi tạo LaF: " + ex.getMessage());
        }
        SwingUtilities.invokeLater(() -> new QuanLyDoanhThuVaThongKe().setVisible(true));
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
