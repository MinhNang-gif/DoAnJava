package View.Customer;

import java.util.ArrayList;
import java.util.List;

public class LichSuVeDaMua {
    String name;
    int quantity;
    double unitPrice;
    double totalPrice;
    String purchaseDate; // Tùy chọn: thêm ngày mua

    public LichSuVeDaMua(String name, int quantity, double unitPrice, double totalPrice) {
        this.name = name;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalPrice = totalPrice;
        // this.purchaseDate = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new java.util.Date()); // Có thể tự động gán ngày mua ở đây hoặc khi tạo vé
    }

    // Getters
    public String getName() { return name; }
    public int getQuantity() { return quantity; }
    public double getUnitPrice() { return unitPrice; }
    public double getTotalPrice() { return totalPrice; }
    public String getPurchaseDate() { return purchaseDate; }

    // Setter for purchaseDate (nếu cần thiết)
    public void setPurchaseDate(String purchaseDate) {
        this.purchaseDate = purchaseDate;
    }
}