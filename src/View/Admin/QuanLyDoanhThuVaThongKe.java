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
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.ui.RectangleInsets;

// Import cho PDFBox để xuất báo cáo
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ListSelectionEvent;



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
    private static final Color COLOR_EXPORT_BUTTON = new Color(0, 123, 255);

    private JDateChooser dcStartDateRevenue, dcEndDateRevenue;
    private JTable tblRevenue;
    private DefaultTableModel modelRevenue;
    private JLabel lblTotalRevenue;
    private ChartPanel chartPanelRevenue;
    private JButton btnExportRevenueReport; // Nút xuất báo cáo doanh thu

    private JDateChooser dcStartDateIncomeExpense, dcEndDateIncomeExpense;
    private JTable tblIncome, tblExpense;
    private DefaultTableModel modelIncome, modelExpense;
    private JLabel lblTotalIncome, lblTotalExpense, lblNetProfit;
    private ChartPanel chartPanelIncomeExpense;
    private JButton btnExportIncomeExpenseReport; // Nút xuất báo cáo thu chi

    private JTextField txtSearchInvoice;
    private JDateChooser dcStartDateInvoice, dcEndDateInvoice;
    private JTable tblInvoices, tblInvoiceDetails;
    private DefaultTableModel modelInvoices, modelInvoiceDetails;
    private JButton       btnSearchInvoice;

    private final SimpleDateFormat displayDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    private final SimpleDateFormat queryDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private final SimpleDateFormat chartDailyKeyFormat = new SimpleDateFormat("dd/MM/yy");
    private final SimpleDateFormat reportDateFormat = new SimpleDateFormat("dd/MM/yyyy");
    private final SimpleDateFormat fileNameDateFormat = new SimpleDateFormat("ddMMyyyy");
    private final DecimalFormat currencyFormat = new DecimalFormat("###,##0");
    private final DecimalFormat currencyFormatVND = new DecimalFormat("###,##0 VND");

    public QuanLyDoanhThuVaThongKe() throws ClassNotFoundException {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (UnsupportedLookAndFeelException ex) {
            System.err.println("Không thể khởi tạo Look and Feel: " + ex.getMessage());
        }

        setTitle("Quản Lý Doanh Thu và Thống Kê");
        setSize(1300, 850);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Changed from EXIT_ON_CLOSE
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

JScrollPane invoicePanel = createInvoiceLookupPanel();

// Nếu đã có >= 3 tab, thì “thay thế” tab thứ 3,
// ngược lại thì “thêm” mới tab thứ 3.
if (tabbedPane.getTabCount() > 2) {
    tabbedPane.setComponentAt(2, invoicePanel);
    tabbedPane.setTitleAt(2, "Tra Cứu Hóa Đơn");
} else {
    tabbedPane.addTab("Tra Cứu Hóa Đơn", invoicePanel);
}

// --- phần đặt ngày mặc định, load data vẫn như cũ ---
Calendar cal = Calendar.getInstance();
Date end = cal.getTime();
cal.add(Calendar.MONTH, -1);
Date start = cal.getTime();
dcStartDateInvoice.setDate(start);
dcEndDateInvoice.setDate(end);
performInvoiceSearch("", start, end);

        add(tabbedPane, BorderLayout.CENTER);

        setInitialDates(); // This ensures date pickers have default values
        setVisible(true);
    }

    private void setInitialDates() {
        Date currentDate = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(currentDate);

        if (dcEndDateRevenue != null) dcEndDateRevenue.setDate(currentDate);
        if (dcEndDateIncomeExpense != null) dcEndDateIncomeExpense.setDate(currentDate);
        if (dcEndDateInvoice != null) dcEndDateInvoice.setDate(currentDate);

        cal.add(Calendar.DAY_OF_MONTH, -6); // Default to last 7 days
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
                !isRevenueChart, 
                true, false);

        barChart.setBackgroundPaint(Color.white);
        CategoryPlot plot = barChart.getCategoryPlot();

        if (plot == null) {
            System.err.println("Lỗi: CategoryPlot là null sau khi tạo biểu đồ.");
            return barChart; 
        }

        plot.setBackgroundPaint(COLOR_BACKGROUND);
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
        plot.setInsets(new RectangleInsets(5.0, 5.0, 25.0, 5.0)); 

        CategoryItemRenderer genericRenderer = plot.getRenderer();

        if (genericRenderer instanceof BarRenderer) {
            BarRenderer renderer = (BarRenderer) genericRenderer;

            renderer.setDrawBarOutline(false); 
            renderer.setDefaultItemLabelGenerator(new StandardCategoryItemLabelGenerator("{2}", currencyFormat));
            renderer.setDefaultItemLabelsVisible(true);
            renderer.setDefaultPositiveItemLabelPosition(new org.jfree.chart.labels.ItemLabelPosition(org.jfree.chart.labels.ItemLabelAnchor.OUTSIDE12, org.jfree.chart.ui.TextAnchor.BOTTOM_CENTER));
            renderer.setBarPainter(new StandardBarPainter()); 

            if (isRevenueChart) {
                renderer.setSeriesPaint(0, COLOR_BLUE_CUSTOM);
                if (barChart.getLegend() != null) {
                    barChart.getLegend().setVisible(false); 
                }
                renderer.setMaximumBarWidth(0.25);
                renderer.setItemMargin(0.05);
            } else { 
                 if (dataset != null && dataset.getRowCount() > 0) {
                    if (dataset.getRowCount() >= 2 && dataset.getRowKeys().size() >= 2) {
                        int thuIndex = dataset.getRowIndex("Tổng Thu");
                        int chiIndex = dataset.getRowIndex("Tổng Chi");

                        if (thuIndex != -1) renderer.setSeriesPaint(thuIndex, COLOR_BLUE_CUSTOM);
                        if (chiIndex != -1) renderer.setSeriesPaint(chiIndex, COLOR_RED_CUSTOM);
                        
                        if (thuIndex == -1 && chiIndex == -1 && dataset.getRowCount() == 2) { 
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
            }
        } else {
            System.err.println("Lỗi: Renderer không phải là BarRenderer hoặc là null. Không thể áp dụng cài đặt cho BarRenderer.");
            if (genericRenderer == null) {
                System.err.println("Chi tiết: plot.getRenderer() trả về null.");
            } else {
                System.err.println("Chi tiết: plot.getRenderer() trả về kiểu: " + genericRenderer.getClass().getName());
            }
        }

        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);
        domainAxis.setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 11));
        domainAxis.setCategoryMargin(0.20);
        domainAxis.setLowerMargin(0.02);
        domainAxis.setUpperMargin(0.02);

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

        barChart.getTitle().setFont(new Font("Segoe UI", Font.BOLD, 18));
        LegendTitle legend = barChart.getLegend();
        if (legend != null) {
            legend.setItemFont(new Font("Segoe UI", Font.PLAIN, 12));
        }
        return barChart;
    }

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

        // Thêm nút xuất báo cáo doanh thu
        btnExportRevenueReport = new JButton("Xuất Báo Cáo");
        btnExportRevenueReport.setFont(FONT_BUTTON);
        btnExportRevenueReport.setBackground(COLOR_EXPORT_BUTTON);
        btnExportRevenueReport.setForeground(Color.WHITE);
        btnExportRevenueReport.setFocusPainted(false);
        btnExportRevenueReport.addActionListener(this::exportRevenueReportAction);

        pnlControls.add(new JLabel("Từ ngày:"));
        pnlControls.add(dcStartDateRevenue);
        pnlControls.add(new JLabel("Đến ngày:"));
        pnlControls.add(dcEndDateRevenue);
        pnlControls.add(btnViewRevenue);
        pnlControls.add(btnExportRevenueReport);
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

        // Thêm nút xuất báo cáo thu chi
        btnExportIncomeExpenseReport = new JButton("Xuất Báo Cáo");
        btnExportIncomeExpenseReport.setFont(FONT_BUTTON);
        btnExportIncomeExpenseReport.setBackground(COLOR_EXPORT_BUTTON);
        btnExportIncomeExpenseReport.setForeground(Color.WHITE);
        btnExportIncomeExpenseReport.setFocusPainted(false);
        btnExportIncomeExpenseReport.addActionListener(this::exportIncomeExpenseReportAction);

        pnlControls.add(new JLabel("Từ ngày:"));
        pnlControls.add(dcStartDateIncomeExpense);
        pnlControls.add(new JLabel("Đến ngày:"));
        pnlControls.add(dcEndDateIncomeExpense);
        pnlControls.add(btnViewIncomeExpense);
        pnlControls.add(btnExportIncomeExpenseReport);
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
        chartPanelIncomeExpense.setPreferredSize(new Dimension(1200, 500)); 

        pnlResults.add(chartPanelIncomeExpense, BorderLayout.CENTER);
        mainTabContentPanel.add(pnlResults, BorderLayout.CENTER);

        JScrollPane tabScrollPane = new JScrollPane(mainTabContentPanel);
        tabScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        tabScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        return tabScrollPane;
    }

    // === 2) Xây dựng panel Tra cứu Hóa Đơn ===
    private JScrollPane createInvoiceLookupPanel() {
        JPanel main = new JPanel(new BorderLayout(10,10));
        main.setBorder(new EmptyBorder(10,10,10,10));
        main.setBackground(Color.WHITE);

        // --- Top controls ---
        JPanel pnlControls = new JPanel(new FlowLayout(FlowLayout.LEFT, 8,5));
        pnlControls.setBackground(Color.WHITE);
        pnlControls.add(new JLabel("Tìm (Mã HĐ / Tên KH):"));
        txtSearchInvoice = new JTextField(16);
        pnlControls.add(txtSearchInvoice);

        pnlControls.add(new JLabel("Từ ngày:"));
        dcStartDateInvoice = new JDateChooser();
        pnlControls.add(dcStartDateInvoice);

        pnlControls.add(new JLabel("Đến ngày:"));
        dcEndDateInvoice = new JDateChooser();
        pnlControls.add(dcEndDateInvoice);

        btnSearchInvoice = new JButton("Tìm Hóa Đơn");
        btnSearchInvoice.setFont(new Font("Tahoma",Font.PLAIN,14));
        btnSearchInvoice.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String kw = txtSearchInvoice.getText().trim();
                Date   s  = dcStartDateInvoice.getDate();
                Date   t  = dcEndDateInvoice.getDate();
                try {
                    performInvoiceSearch(kw,s,t);
                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(QuanLyDoanhThuVaThongKe.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        pnlControls.add(btnSearchInvoice);

        main.add(pnlControls, BorderLayout.NORTH);

        // --- Bảng danh sách hóa đơn ---
        modelInvoices = new DefaultTableModel(
            new String[]{"Mã HĐ","Mã KH","Tên KH","Ngày Lập","Tổng Tiền","Trạng Thái"},0
        ) {
            @Override public boolean isCellEditable(int r,int c){ return false; }
        };
        tblInvoices = new JTable(modelInvoices);
        tblInvoices.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblInvoices.getSelectionModel().addListSelectionListener((ListSelectionEvent ev)->{
            if (!ev.getValueIsAdjusting() && tblInvoices.getSelectedRow()>=0) {
                String maHD = modelInvoices.getValueAt(tblInvoices.getSelectedRow(),0).toString();
                try {
                    loadInvoiceDetails(maHD);
                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(QuanLyDoanhThuVaThongKe.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        // Thêm mouse click backup
        tblInvoices.addMouseListener(new MouseAdapter(){
            public void mouseClicked(MouseEvent e){
                int row = tblInvoices.rowAtPoint(e.getPoint());
                if(row>=0) {
                    String maHD = modelInvoices.getValueAt(row,0).toString();
                    try {
                        loadInvoiceDetails(maHD);
                    } catch (ClassNotFoundException ex) {
                        Logger.getLogger(QuanLyDoanhThuVaThongKe.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        });

        // --- Bảng chi tiết ---
        modelInvoiceDetails = new DefaultTableModel(
            new String[]{"Mã CTHĐ","Mã Vé","Mã DV","Tên DV/Vé","Đơn Giá","Số Lượng","Thành Tiền"},0
        ) {
            @Override public boolean isCellEditable(int r,int c){ return false; }
        };
        tblInvoiceDetails = new JTable(modelInvoiceDetails);

        JSplitPane split = new JSplitPane(
            JSplitPane.VERTICAL_SPLIT,
            new JScrollPane(tblInvoices),
            new JScrollPane(tblInvoiceDetails)
        );
        split.setResizeWeight(0.6);
        split.setOneTouchExpandable(true);

        main.add(split, BorderLayout.CENTER);
        return new JScrollPane(main);
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

    // ==== 4) Sự kiện nút tìm kiếm ====
private void searchInvoicesAction(ActionEvent e) throws ClassNotFoundException {
    String searchTerm = txtSearchInvoice.getText().trim();
    Date   startDate  = dcStartDateInvoice.getDate();
    Date   endDate    = dcEndDateInvoice.getDate();
    performInvoiceSearch(searchTerm, startDate, endDate);
}

    // === 3) Tải danh sách hóa đơn, đồng thời tự tính LẠI tổng tiền từ chi tiết nếu HOADON.TONGTIEN = 0 ===
    private void performInvoiceSearch(String searchTerm, Date startDate, Date endDate) throws ClassNotFoundException {
        if (startDate==null || endDate==null) {
            JOptionPane.showMessageDialog(this,
                "Chọn đủ khoảng thời gian.","Lỗi",JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (endDate.before(startDate)) {
            JOptionPane.showMessageDialog(this,
                "Ngày kết thúc trước ngày bắt đầu.","Lỗi",JOptionPane.ERROR_MESSAGE);
            return;
        }

        // chuẩn hóa đến 23:59:59 của endDate
        Calendar c = Calendar.getInstance();
        c.setTime(endDate);
        c.set(Calendar.HOUR_OF_DAY,23);
        c.set(Calendar.MINUTE,59);
        c.set(Calendar.SECOND,59);
        c.set(Calendar.MILLISECOND,999);
        Timestamp tsStart = new Timestamp(startDate.getTime());
        Timestamp tsEnd   = new Timestamp(c.getTimeInMillis());

        modelInvoices.setRowCount(0);
        modelInvoiceDetails.setRowCount(0);

        StringBuilder sql = new StringBuilder()
            .append("SELECT H.MAHOADON,H.MAKH,K.TENKH,H.NGAYLAP, ")
            // lấy luôn HOADON.TONGTIEN gốc
            .append(" NVL(H.TONGTIEN,0) AS TONG_HOADON, H.TRANGTHAI ")
            .append("FROM HOADON H ")
            .append("LEFT JOIN KHACHHANG K ON H.MAKH=K.MAKH ")
            .append("WHERE H.LOAIHOADON IN('VE_XE','VE_BAO_DUONG') ")
            .append("  AND H.NGAYLAP BETWEEN ? AND ? ");
        if (!searchTerm.isEmpty()) {
            sql.append(" AND (UPPER(H.MAHOADON) LIKE UPPER(?)")
               .append(" OR UPPER(K.TENKH) LIKE UPPER(?))");
        }
        sql.append(" ORDER BY H.NGAYLAP DESC");

        try (
            Connection conn = ConnectionOracle.getOracleConnection();
            PreparedStatement p = conn.prepareStatement(sql.toString())
        ) {
            int idx=1;
            p.setTimestamp(idx++, tsStart);
            p.setTimestamp(idx++, tsEnd);
            if (!searchTerm.isEmpty()) {
                String kw = "%"+searchTerm+"%";
                p.setString(idx++, kw);
                p.setString(idx++, kw);
            }
            ResultSet rs = p.executeQuery();
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            DecimalFormat fmt   = new DecimalFormat("#,###");

            while(rs.next()) {
                String maHD     = rs.getString("MAHOADON");
                BigDecimal tong = rs.getBigDecimal("TONG_HOADON");
                // nếu HOADON.TONGTIEN == 0 thì tính lại bằng subtotal của CHITIETHOADON
                if (tong.compareTo(BigDecimal.ZERO)==0) {
                    tong = recalcTotalFromDetails(conn, maHD);
                }
                modelInvoices.addRow(new Object[]{
                    maHD,
                    rs.getString("MAKH"),
                    rs.getString("TENKH"),
                    df.format(rs.getTimestamp("NGAYLAP")),
                    fmt.format(tong),
                    rs.getString("TRANGTHAI")
                });
            }
            // tự chọn dòng đầu và nạp chi tiết
            if (modelInvoices.getRowCount()>0) {
                tblInvoices.setRowSelectionInterval(0,0);
                loadInvoiceDetails(modelInvoices.getValueAt(0,0).toString());
            }
        } catch(SQLException ex){
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Lỗi tải hóa đơn:\n"+ex.getMessage(),
                "Lỗi",JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // helper: tính lại tổng từ CHITIETHOADON
    private BigDecimal recalcTotalFromDetails(Connection conn, String maHD) throws SQLException {
        String sql =
          "SELECT SUM(CASE WHEN CT.MADVBAODUONG IS NOT NULL THEN DVB.GIA*CT.SOLUONG ELSE VX.PHIGUIXE*CT.SOLUONG END) AS SUBTOTAL " +
          "FROM CHITIETHOADON CT " +
          "LEFT JOIN DICHVUBAODUONG DVB ON CT.MADVBAODUONG=DVB.MADVBAODUONG " +
          "LEFT JOIN VEGUIXE VX ON CT.MAVEXE=VX.MAVEXE " +
          "WHERE CT.MAHOADON = ?";
        try (PreparedStatement p = conn.prepareStatement(sql)) {
            p.setString(1, maHD);
            ResultSet r = p.executeQuery();
            if (r.next()) {
                return r.getBigDecimal("SUBTOTAL")==null
                   ? BigDecimal.ZERO
                   : r.getBigDecimal("SUBTOTAL");
            }
        }
        return BigDecimal.ZERO;
    }

    // === 4) Tải chi tiết khi user click hoặc chọn row ===
    private void loadInvoiceDetails(String maHoaDon) throws ClassNotFoundException {
        modelInvoiceDetails.setRowCount(0);

        String sql =
          "SELECT CT.MACTHD, CT.MAVEXE, CT.MADVBAODUONG, DVB.TENDV, DVB.GIA AS GIA_DV, " +
          "       VX.PHIGUIXE AS GIA_VE, CT.SOLUONG, " +
          "       CASE WHEN CT.MADVBAODUONG IS NOT NULL THEN DVB.GIA*CT.SOLUONG " +
          "            ELSE VX.PHIGUIXE*CT.SOLUONG END AS THANHTIEN " +
          "FROM CHITIETHOADON CT " +
          "LEFT JOIN DICHVUBAODUONG DVB ON CT.MADVBAODUONG=DVB.MADVBAODUONG " +
          "LEFT JOIN VEGUIXE VX ON CT.MAVEXE=VX.MAVEXE " +
          "WHERE CT.MAHOADON = ?";
        try (
            Connection conn = ConnectionOracle.getOracleConnection();
            PreparedStatement p = conn.prepareStatement(sql)
        ) {
            p.setString(1, maHoaDon);
            ResultSet rs = p.executeQuery();
            DecimalFormat fmt = new DecimalFormat("#,###");
            while(rs.next()) {
                String mav    = rs.getString("MAVEXE");
                String madv   = rs.getString("MADVBAODUONG");
                String name   = rs.getString("TENDV");
                BigDecimal ug = rs.getBigDecimal("GIA_DV");
                if (ug==null) ug = rs.getBigDecimal("GIA_VE");
                modelInvoiceDetails.addRow(new Object[]{
                    rs.getString("MACTHD"),
                    mav==null?"":mav,
                    madv==null?"":madv,
                    name==null?"Phí gửi xe":name,
                    fmt.format(ug),
                    rs.getInt("SOLUONG"),
                    fmt.format(rs.getBigDecimal("THANHTIEN"))
                });
            }
        } catch(SQLException ex){
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Lỗi tải chi tiết hóa đơn:\n"+ex.getMessage(),
                "Lỗi",JOptionPane.ERROR_MESSAGE);
        }
    }


    // Phương thức xuất báo cáo doanh thu
    private void exportRevenueReportAction(ActionEvent e) {
        Date startDate = dcStartDateRevenue.getDate();
        Date endDate = dcEndDateRevenue.getDate();

        if (startDate == null || endDate == null || endDate.before(startDate)) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn khoảng thời gian hợp lệ để xuất báo cáo.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (modelRevenue.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Không có dữ liệu doanh thu để xuất báo cáo.\nVui lòng nhấn 'Xem Doanh Thu' trước.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Chọn nơi lưu báo cáo doanh thu");
        String defaultFileName = "ThongKeDoanhThu_" + fileNameDateFormat.format(startDate) + "_" + fileNameDateFormat.format(endDate) + ".pdf";
        fileChooser.setSelectedFile(new File(defaultFileName));
        FileNameExtensionFilter filter = new FileNameExtensionFilter("PDF Documents (*.pdf)", "pdf");
        fileChooser.setFileFilter(filter);

        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            String filePath = fileToSave.getAbsolutePath();
            if (!filePath.toLowerCase().endsWith(".pdf")) {
                filePath += ".pdf";
            }

            try {
                generateRevenueReportPDF(filePath, startDate, endDate);
                JOptionPane.showMessageDialog(this,
                    "Báo cáo doanh thu đã được xuất thành công!\nĐường dẫn: " + filePath,
                    "Xuất Báo Cáo Thành Công",
                    JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this,
                    "Có lỗi xảy ra khi tạo báo cáo: " + ex.getMessage(),
                    "Lỗi Xuất Báo Cáo",
                    JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }

    // Phương thức xuất báo cáo thu chi
    private void exportIncomeExpenseReportAction(ActionEvent e) {
        Date startDate = dcStartDateIncomeExpense.getDate();
        Date endDate = dcEndDateIncomeExpense.getDate();

        if (startDate == null || endDate == null || endDate.before(startDate)) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn khoảng thời gian hợp lệ để xuất báo cáo.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (modelIncome.getRowCount() == 0 && modelExpense.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Không có dữ liệu thu chi để xuất báo cáo.\nVui lòng nhấn 'Xem Thu Chi' trước.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Chọn nơi lưu báo cáo thu chi");
        String defaultFileName = "ThongKeThuChi_" + fileNameDateFormat.format(startDate) + "_" + fileNameDateFormat.format(endDate) + ".pdf";
        fileChooser.setSelectedFile(new File(defaultFileName));
        FileNameExtensionFilter filter = new FileNameExtensionFilter("PDF Documents (*.pdf)", "pdf");
        fileChooser.setFileFilter(filter);

        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            String filePath = fileToSave.getAbsolutePath();

            // Đảm bảo tệp có đuôi .pdf
            if (!filePath.toLowerCase().endsWith(".pdf")) {
                filePath += ".pdf";
            }

            try {
                generateIncomeExpenseReportPDF(filePath, startDate, endDate);
                JOptionPane.showMessageDialog(this,
                    "Báo cáo thu chi đã được xuất thành công!\nĐường dẫn: " + filePath,
                    "Xuất Báo Cáo Thành Công",
                    JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this,
                    "Có lỗi xảy ra khi tạo báo cáo: " + ex.getMessage(),
                    "Lỗi Xuất Báo Cáo",
                    JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }

    // Phương thức tạo PDF báo cáo doanh thu - ĐÃ XÓA PHẦN CHỮ KÝ
    private void generateRevenueReportPDF(String filePath, Date startDate, Date endDate) throws IOException {
        PDFont localFontRegular = null;
        PDFont localFontBold = null;

        try (PDDocument document = new PDDocument();
             InputStream fontStreamReg = getClass().getResourceAsStream("/fonts/arial.ttf");
             InputStream fontStreamBld = getClass().getResourceAsStream("/fonts/arialbd.ttf")) {

            if (fontStreamReg != null) {
                localFontRegular = PDType0Font.load(document, fontStreamReg);
            } else {
                throw new IOException("Không tìm thấy file font regular: /fonts/arial.ttf");
            }

            if (fontStreamBld != null) {
                localFontBold = PDType0Font.load(document, fontStreamBld);
            } else {
                System.err.println("Không tìm thấy file font bold: /fonts/arialbd.ttf. Sử dụng font regular cho bold.");
                localFontBold = localFontRegular;
            }

            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            float margin = 40;
            float yStart = page.getMediaBox().getHeight() - margin;
            float yPosition = yStart;
            float pageWidth = page.getMediaBox().getWidth();
            float contentWidth = pageWidth - 2 * margin;

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                // Thêm logo nếu có
                try {
                    URL logoUrl = getClass().getResource("/icons/logoF4ThuDuc.jpg");
                    if (logoUrl != null) {
                        PDImageXObject logoImage = PDImageXObject.createFromByteArray(document, 
                            logoUrl.openStream().readAllBytes(), "logo");
                        float logoWidth = 80;
                        float logoHeight = 60;
                        contentStream.drawImage(logoImage, margin, yPosition - logoHeight, logoWidth, logoHeight);
                        yPosition -= logoHeight + 10;
                    }
                } catch (Exception e) {
                    System.err.println("Không thể tải logo: " + e.getMessage());
                    yPosition -= 20; // Giảm khoảng cách nếu không có logo
                }

                // Header công ty
                drawTextCentered(contentStream, "HỆ THỐNG QUẢN LÝ XE VÀ BẢO DƯỠNG XE", localFontBold, 16, yPosition, pageWidth);
                yPosition -= 30;

                // Ngày báo cáo
                String currentDate = new SimpleDateFormat("EEEE dd MMMM yyyy", new java.util.Locale("vi", "VN")).format(new Date());
                drawTextRight(contentStream, currentDate, localFontRegular, 11, pageWidth - margin, yPosition);
                yPosition -= 40;

                // Tiêu đề báo cáo
                String reportTitle = "BÁO CÁO DOANH THU TỪ " + reportDateFormat.format(startDate) + " ĐẾN " + reportDateFormat.format(endDate);
                drawTextCentered(contentStream, reportTitle, localFontBold, 16, yPosition, pageWidth);
                yPosition -= 40;

                // Bảng dữ liệu
                String[] headers = {"STT", "NGUỒN DOANH THU", "NGÀY GHI NHẬN", "SỐ TIỀN"};
                float[] colWidths = {contentWidth * 0.08f, contentWidth * 0.35f, contentWidth * 0.27f, contentWidth * 0.30f};
                float tableRowHeight = 25f;

                // Vẽ header bảng
                yPosition -= tableRowHeight;
                float currentX = margin;
                for (int i = 0; i < headers.length; i++) {
                    drawTableCell(contentStream, headers[i], localFontBold, 11f, currentX, yPosition, colWidths[i], tableRowHeight, "CENTER", true);
                    currentX += colWidths[i];
                }
                drawTableBorder(contentStream, margin, yPosition, contentWidth, tableRowHeight);

                // Vẽ dữ liệu
                DefaultTableModel model = modelRevenue;
                for (int i = 0; i < model.getRowCount(); i++) {
                    yPosition -= tableRowHeight;
                    currentX = margin;
                    
                    drawTableCell(contentStream, String.valueOf(i + 1), localFontRegular, 10f, currentX, yPosition, colWidths[0], tableRowHeight, "CENTER", false);
                    currentX += colWidths[0];
                    drawTableCell(contentStream, model.getValueAt(i, 0).toString(), localFontRegular, 10f, currentX, yPosition, colWidths[1], tableRowHeight, "CENTER", false);
                    currentX += colWidths[1];
                    drawTableCell(contentStream, model.getValueAt(i, 1).toString(), localFontRegular, 10f, currentX, yPosition, colWidths[2], tableRowHeight, "CENTER", false);
                    currentX += colWidths[2];
                    drawTableCell(contentStream, model.getValueAt(i, 2).toString(), localFontRegular, 10f, currentX, yPosition, colWidths[3], tableRowHeight, "CENTER", false);
                    
                    drawTableBorder(contentStream, margin, yPosition, contentWidth, tableRowHeight);
                }

                // Tổng doanh thu
                yPosition -= 30;
                String totalText = "TỔNG DOANH THU: " + lblTotalRevenue.getText().replace("Tổng Doanh Thu: ", "");
                drawTextCentered(contentStream, totalText, localFontBold, 14, yPosition, pageWidth);
                
                // ĐÃ XÓA PHẦN CHỮ KÝ - KẾT THÚC TẠI ĐÂY
            }

            document.save(filePath);
        }
    }

    // Phương thức tạo PDF báo cáo thu chi - ĐÃ XÓA PHẦN CHỮ KÝ
    private void generateIncomeExpenseReportPDF(String filePath, Date startDate, Date endDate) throws IOException {
        PDFont localFontRegular = null;
        PDFont localFontBold = null;

        try (PDDocument document = new PDDocument();
             InputStream fontStreamReg = getClass().getResourceAsStream("/fonts/arial.ttf");
             InputStream fontStreamBld = getClass().getResourceAsStream("/fonts/arialbd.ttf")) {

            if (fontStreamReg != null) {
                localFontRegular = PDType0Font.load(document, fontStreamReg);
            } else {
                throw new IOException("Không tìm thấy file font regular: /fonts/arial.ttf");
            }

            if (fontStreamBld != null) {
                localFontBold = PDType0Font.load(document, fontStreamBld);
            } else {
                System.err.println("Không tìm thấy file font bold: /fonts/arialbd.ttf. Sử dụng font regular cho bold.");
                localFontBold = localFontRegular;
            }

            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            float margin = 40;
            float yStart = page.getMediaBox().getHeight() - margin;
            float yPosition = yStart;
            float pageWidth = page.getMediaBox().getWidth();
            float contentWidth = pageWidth - 2 * margin;

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                // Thêm logo nếu có
                try {
                    URL logoUrl = getClass().getResource("/icons/logoF4ThuDuc.jpg");
                    if (logoUrl != null) {
                        PDImageXObject logoImage = PDImageXObject.createFromByteArray(document, 
                            logoUrl.openStream().readAllBytes(), "logo");
                        float logoWidth = 80;
                        float logoHeight = 60;
                        contentStream.drawImage(logoImage, margin, yPosition - logoHeight, logoWidth, logoHeight);
                        yPosition -= logoHeight + 10;
                    }
                } catch (Exception e) {
                    System.err.println("Không thể tải logo: " + e.getMessage());
                    yPosition -= 20;
                }

                // Header công ty
                drawTextCentered(contentStream, "HỆ THỐNG QUẢN LÝ XE VÀ BẢO DƯỠNG XE", localFontBold, 16, yPosition, pageWidth);
                yPosition -= 30;

                // Ngày báo cáo
                String currentDate = new SimpleDateFormat("EEEE dd MMMM yyyy", new java.util.Locale("vi", "VN")).format(new Date());
                drawTextRight(contentStream, currentDate, localFontRegular, 11, pageWidth - margin, yPosition);
                yPosition -= 40;

                // Tiêu đề báo cáo
                String reportTitle = "BÁO CÁO THU CHI TỪ " + reportDateFormat.format(startDate) + " ĐẾN " + reportDateFormat.format(endDate);
                drawTextCentered(contentStream, reportTitle, localFontBold, 16, yPosition, pageWidth);
                yPosition -= 40;

                // Tính toán layout cân bằng cho thu chi
                int incomeRows = modelIncome.getRowCount();
                int expenseRows = modelExpense.getRowCount();
                
                // Nếu có dữ liệu thu chi, hiển thị theo layout cải thiện
                if (incomeRows > 0 || expenseRows > 0) {
                    // Bảng khoản thu
                    if (incomeRows > 0) {
                        drawTextLeft(contentStream, "KHOẢN THU:", localFontBold, 12, margin, yPosition);
                        yPosition -= 25;

                        String[] incomeHeaders = {"STT", "NGÀY THU", "NGUỒN THU", "SỐ TIỀN"};
                        float[] incomeColWidths = {contentWidth * 0.08f, contentWidth * 0.25f, contentWidth * 0.37f, contentWidth * 0.30f};
                        float tableRowHeight = 20f;

                        // Header bảng thu
                        yPosition -= tableRowHeight;
                        float currentX = margin;
                        for (int i = 0; i < incomeHeaders.length; i++) {
                            drawTableCell(contentStream, incomeHeaders[i], localFontBold, 10f, currentX, yPosition, incomeColWidths[i], tableRowHeight, "CENTER", true);
                            currentX += incomeColWidths[i];
                        }
                        drawTableBorder(contentStream, margin, yPosition, contentWidth, tableRowHeight);

                        // Dữ liệu thu - GIỚI HẠN SỐ DÒNG ĐỂ KHÔNG CHIẾM HẾT TRANG
                        int maxIncomeRowsToShow = Math.min(incomeRows, 15); // Giới hạn tối đa 15 dòng
                        for (int i = 0; i < maxIncomeRowsToShow; i++) {
                            yPosition -= tableRowHeight;
                            currentX = margin;
                            
                            drawTableCell(contentStream, String.valueOf(i + 1), localFontRegular, 9f, currentX, yPosition, incomeColWidths[0], tableRowHeight, "CENTER", false);
                            currentX += incomeColWidths[0];
                            drawTableCell(contentStream, modelIncome.getValueAt(i, 0).toString(), localFontRegular, 9f, currentX, yPosition, incomeColWidths[1], tableRowHeight, "CENTER", false);
                            currentX += incomeColWidths[1];
                            drawTableCell(contentStream, modelIncome.getValueAt(i, 1).toString(), localFontRegular, 9f, currentX, yPosition, incomeColWidths[2], tableRowHeight, "CENTER", false);
                            currentX += incomeColWidths[2];
                            drawTableCell(contentStream, modelIncome.getValueAt(i, 2).toString(), localFontRegular, 9f, currentX, yPosition, incomeColWidths[3], tableRowHeight, "CENTER", false);
                            
                            drawTableBorder(contentStream, margin, yPosition, contentWidth, tableRowHeight);
                        }

                        // Hiển thị thông báo nếu có nhiều dòng hơn
                        if (incomeRows > maxIncomeRowsToShow) {
                            yPosition -= 15;
                            drawTextLeft(contentStream, "... và " + (incomeRows - maxIncomeRowsToShow) + " khoản thu khác", localFontRegular, 9f, margin, yPosition);
                        }

                        yPosition -= 20;
                        drawTextRight(contentStream, lblTotalIncome.getText(), localFontBold, 11, pageWidth - margin, yPosition);
                        yPosition -= 30;
                    }

                    // Bảng khoản chi
                    if (expenseRows > 0) {
                        drawTextLeft(contentStream, "KHOẢN CHI:", localFontBold, 12, margin, yPosition);
                        yPosition -= 25;

                        String[] expenseHeaders = {"STT", "NGÀY CHI", "LÝ DO CHI", "SỐ TIỀN"};
                        float[] expenseColWidths = {contentWidth * 0.08f, contentWidth * 0.25f, contentWidth * 0.37f, contentWidth * 0.30f};
                        float tableRowHeight = 20f;

                        // Header bảng chi
                        yPosition -= tableRowHeight;
                        float currentX = margin;
                        for (int i = 0; i < expenseHeaders.length; i++) {
                            drawTableCell(contentStream, expenseHeaders[i], localFontBold, 10f, currentX, yPosition, expenseColWidths[i], tableRowHeight, "CENTER", true);
                            currentX += expenseColWidths[i];
                        }
                        drawTableBorder(contentStream, margin, yPosition, contentWidth, tableRowHeight);

                        // Dữ liệu chi - GIỚI HẠN SỐ DÒNG
                        int maxExpenseRowsToShow = Math.min(expenseRows, 10); // Giới hạn tối đa 10 dòng cho chi
                        for (int i = 0; i < maxExpenseRowsToShow; i++) {
                            yPosition -= tableRowHeight;
                            currentX = margin;
                            
                            drawTableCell(contentStream, String.valueOf(i + 1), localFontRegular, 9f, currentX, yPosition, expenseColWidths[0], tableRowHeight, "CENTER", false);
                            currentX += expenseColWidths[0];
                            drawTableCell(contentStream, modelExpense.getValueAt(i, 0).toString(), localFontRegular, 9f, currentX, yPosition, expenseColWidths[1], tableRowHeight, "CENTER", false);
                            currentX += expenseColWidths[1];
                            drawTableCell(contentStream, modelExpense.getValueAt(i, 1).toString(), localFontRegular, 9f, currentX, yPosition, expenseColWidths[2], tableRowHeight, "CENTER", false);
                            currentX += expenseColWidths[2];
                            drawTableCell(contentStream, modelExpense.getValueAt(i, 2).toString(), localFontRegular, 9f, currentX, yPosition, expenseColWidths[3], tableRowHeight, "CENTER", false);
                            
                            drawTableBorder(contentStream, margin, yPosition, contentWidth, tableRowHeight);
                        }

                        // Hiển thị thông báo nếu có nhiều dòng hơn
                        if (expenseRows > maxExpenseRowsToShow) {
                            yPosition -= 15;
                            drawTextLeft(contentStream, "... và " + (expenseRows - maxExpenseRowsToShow) + " khoản chi khác", localFontRegular, 9f, margin, yPosition);
                        }

                        yPosition -= 20;
                        drawTextRight(contentStream, lblTotalExpense.getText(), localFontBold, 11, pageWidth - margin, yPosition);
                        yPosition -= 30;
                    }
                }

                // Lợi nhuận
                drawTextCentered(contentStream, lblNetProfit.getText(), localFontBold, 14, yPosition, pageWidth);
                
                // ĐÃ XÓA PHẦN CHỮ KÝ - KẾT THÚC TẠI ĐÂY
            }

            document.save(filePath);
        }
    }

    // Helper methods for PDF generation
    private void drawText(PDPageContentStream contentStream, String text, PDFont font, float fontSize, float x, float y) throws IOException {
        if (font == null) {
            System.err.println("drawText: Font is null. Text: " + text);
            return;
        }
        contentStream.beginText();
        contentStream.setFont(font, fontSize);
        contentStream.newLineAtOffset(x, y);
        contentStream.showText(text != null ? text : "");
        contentStream.endText();
    }

    private void drawTextCentered(PDPageContentStream contentStream, String text, PDFont font, float fontSize, float y, float pageWidth) throws IOException {
        if (font == null) {
            System.err.println("drawTextCentered: Font is null. Text: " + text);
            return;
        }
        text = text != null ? text : "";
        float textWidth = font.getStringWidth(text) / 1000f * fontSize;
        float x = (pageWidth - textWidth) / 2f;
        drawText(contentStream, text, font, fontSize, x, y);
    }

    private void drawTextRight(PDPageContentStream contentStream, String text, PDFont font, float fontSize, float rightX, float y) throws IOException {
        if (font == null) {
            System.err.println("drawTextRight: Font is null. Text: " + text);
            return;
        }
        text = text != null ? text : "";
        float textWidth = font.getStringWidth(text) / 1000f * fontSize;
        float x = rightX - textWidth;
        drawText(contentStream, text, font, fontSize, x, y);
    }

    private void drawTextLeft(PDPageContentStream contentStream, String text, PDFont font, float fontSize, float x, float y) throws IOException {
        drawText(contentStream, text, font, fontSize, x, y);
    }

    private void drawTableCell(PDPageContentStream contentStream, String text, PDFont font, float fontSize, float x, float y, float cellWidth, float cellHeight, String align, boolean isHeader) throws IOException {
        if (font == null) {
            System.err.println("drawTableCell: Font is null. Text: " + text);
            return;
        }
        text = text != null ? text : "";
        float textWidth = font.getStringWidth(text) / 1000f * fontSize;
        float actualX = x;
        float padding = 4f;

        // Truncate text if too long
        if (textWidth > cellWidth - 2 * padding) {
            String originalText = text;
            text = "";
            for (int i = 0; i < originalText.length(); i++) {
                String testText = originalText.substring(0, i + 1);
                float tempWidth = font.getStringWidth(testText + "...") / 1000f * fontSize;
                if (tempWidth < cellWidth - 2 * padding) {
                    text = testText;
                } else {
                    break;
                }
            }
            if (!text.equals(originalText) && !text.isEmpty()) {
                text += "...";
            }
            textWidth = font.getStringWidth(text) / 1000f * fontSize;
        }

        // Align text - CẢI THIỆN CĂNG GIỮA CHÍNH XÁC
        if ("CENTER".equalsIgnoreCase(align)) {
            actualX = x + (cellWidth - textWidth) / 2f;
        } else if ("RIGHT".equalsIgnoreCase(align)) {
            actualX = x + cellWidth - textWidth - padding;
        } else { // LEFT
            actualX = x + padding;
        }

        // Đảm bảo text không bị tràn ra ngoài cell
        if (actualX < x + padding) actualX = x + padding;
        if (actualX + textWidth > x + cellWidth - padding) {
            actualX = x + cellWidth - textWidth - padding;
        }

        float textY = y + (cellHeight - fontSize) / 2f - (fontSize * 0.15f);
        if (isHeader) textY = y + (cellHeight - fontSize) / 2f - (fontSize * 0.05f);

        contentStream.beginText();
        contentStream.setFont(font, fontSize);
        contentStream.newLineAtOffset(actualX, textY);
        contentStream.showText(text);
        contentStream.endText();
    }

    private void drawTableBorder(PDPageContentStream contentStream, float x, float y, float width, float height) throws IOException {
        contentStream.setLineWidth(0.5f);
        contentStream.addRect(x, y, width, height);
        contentStream.stroke();
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (UnsupportedLookAndFeelException ex) {
            System.err.println("Không thể khởi tạo LaF: " + ex.getMessage());
        }
        SwingUtilities.invokeLater(() -> {
            try {
                new QuanLyDoanhThuVaThongKe().setVisible(true);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(QuanLyDoanhThuVaThongKe.class.getName()).log(Level.SEVERE, null, ex);
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
