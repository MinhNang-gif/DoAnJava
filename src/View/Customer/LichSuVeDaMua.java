package View.Customer;

import java.util.ArrayList;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LichSuVeDaMua {
    private String maVeXe; // << THÊM TRƯỜNG NÀY
    private String name;
    private int quantity;
    private double unitPrice;
    private double totalPrice;
    private String purchaseDate; // Nên là Date thay vì String để dễ sort, nhưng giữ nguyên theo code cũ

    // Cập nhật constructor
    public LichSuVeDaMua(String maVeXe, String name, int quantity, double unitPrice, double totalPrice) {
        this.maVeXe = maVeXe; // << GÁN MAVEXE
        this.name = name;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalPrice = totalPrice;
        // Tự động gán ngày mua hiện tại nếu không được cung cấp
        this.purchaseDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date());
    }

    // Getter cho maVeXe
    public String getMaVeXe() { // << THÊM GETTER
        return maVeXe;
    }

    public String getName() { return name; }
    public int getQuantity() { return quantity; }
    public double getUnitPrice() { return unitPrice; }
    public double getTotalPrice() { return totalPrice; }
    public String getPurchaseDate() { return purchaseDate; }

    public void setPurchaseDate(String purchaseDate) {
        this.purchaseDate = purchaseDate;
    }
     public void setPurchaseDate(Date date) {
        if (date != null) {
            this.purchaseDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(date);
        }
    }
}