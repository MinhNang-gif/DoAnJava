package View.Admin; // Hoặc package phù hợp của bạn

import org.jfree.chart.renderer.category.BarRenderer;
import java.awt.Color;
import java.awt.Paint;

public class CustomBarRenderer extends BarRenderer {
    private Paint[] colors;

    public CustomBarRenderer() {
        this.colors = new Paint[] { /* ... mảng màu như đã định nghĩa ... */
            new Color(222, 78, 78),   // Đỏ
            new Color(80, 120, 200), // Xanh dương
            new Color(100, 180, 100), // Xanh lá
            new Color(240, 190, 75),  // Vàng
            new Color(150, 100, 210), // Tím
            new Color(70, 190, 190),  // Xanh ngọc
            new Color(255,140,0),    // Cam
            new Color(128,128,128),  // Xám
            new Color(238,130,238),  // Hồng tím
            new Color(0,128,0),      // Xanh lá đậm
            new Color(255, 105, 180), 
            new Color(0, 191, 255),   
            new Color(218, 165, 32),  
            new Color(72, 61, 139)    
        };
    }

    @Override
    public Paint getItemPaint(int row, int column) {
        return this.colors[column % this.colors.length];
    }
}