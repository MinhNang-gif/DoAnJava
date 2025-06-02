package View.Customer;

import java.util.List;
import java.util.Map;

public class DataObjects {
    // Đối tượng cho Giỏ hàng
    class CartItem {
        String productId;
        String productName;
        int quantity;
        double price;
        double itemTotalPrice;
        Map<String, Object> options;

        public CartItem(String productId, String productName, int quantity, double price, Map<String, Object> options) {
            this.productId = productId;
            this.productName = productName;
            this.quantity = quantity;
            this.price = price;
            this.options = options;
            this.itemTotalPrice = quantity * price;
        }
        // Getters and setters
    }

    class CartObject {
        String cartId;
        String userId;
        List<CartItem> items;
        double subTotal;
        double total;

        public CartObject(String cartId, String userId, List<CartItem> items) {
            this.cartId = cartId;
            this.userId = userId;
            this.items = items;
            calculateTotals();
        }

        public void calculateTotals() {
            this.subTotal = items.stream().mapToDouble(item -> item.itemTotalPrice).sum();
            // Giả sử có thêm các chi phí khác (vận chuyển, thuế) có thể được thêm vào đây
            this.total = this.subTotal;
        }
        // Getters and setters, and methods to add/update/remove items if cart logic is here
    }

    // Đối tượng cho Hồ sơ người dùng
    class UserProfileObject {
        String userId;
        String name;
        String email;
        String address;
        String phoneNumber;

        public UserProfileObject(String userId, String name, String email, String address, String phoneNumber) {
            this.userId = userId;
            this.name = name;
            this.email = email;
            this.address = address;
            this.phoneNumber = phoneNumber;
        }
        // Getters and setters
    }

    // Đối tượng cho Phương thức thanh toán
    class PaymentMethodObject {
        String paymentMethodId;
        String type; // e.g., "CreditCard", "BankAccount", "EWallet"
        String displayName; // e.g., "Visa **** 1234"
        Map<String, Object> details; // Additional details if needed

        public PaymentMethodObject(String paymentMethodId, String type, String displayName, Map<String, Object> details) {
            this.paymentMethodId = paymentMethodId;
            this.type = type;
            this.displayName = displayName;
            this.details = details;
        }
        // Getters and setters
    }

    class PaymentMethodListObject {
        List<PaymentMethodObject> paymentMethods;

        public PaymentMethodListObject(List<PaymentMethodObject> paymentMethods) {
            this.paymentMethods = paymentMethods;
        }
        // Getters and setters
    }

    // Đối tượng cho Đặt vé
    class BookingInitiationResultObject {
        String tempBookingId;
        String message;
        long paymentDeadlineTimestamp; // Thời hạn thanh toán

        public BookingInitiationResultObject(String tempBookingId, String message, long paymentDeadlineTimestamp) {
            this.tempBookingId = tempBookingId;
            this.message = message;
            this.paymentDeadlineTimestamp = paymentDeadlineTimestamp;
        }
        // Getters and setters
    }

    class BookingConfirmationObject {
        String bookingId;
        String ticketCode;
        String status; // e.g., "CONFIRMED"
        Map<String, Object> bookingDetails;

        public BookingConfirmationObject(String bookingId, String ticketCode, String status, Map<String, Object> bookingDetails) {
            this.bookingId = bookingId;
            this.ticketCode = ticketCode;
            this.status = status;
            this.bookingDetails = bookingDetails;
        }
        // Getters and setters
    }

    class BookingHistoryObject {
        String bookingId;
        String ticketCode;
        String flightDetails;
        String bookingDate;
        String status;
        double totalPrice;

        public BookingHistoryObject(String bookingId, String ticketCode, String flightDetails, String bookingDate, String status, double totalPrice) {
            this.bookingId = bookingId;
            this.ticketCode = ticketCode;
            this.flightDetails = flightDetails;
            this.bookingDate = bookingDate;
            this.status = status;
            this.totalPrice = totalPrice;
        }
        // Getters and setters
    }

    class BookingHistoryListObject {
        List<BookingHistoryObject> bookings;

        public BookingHistoryListObject(List<BookingHistoryObject> bookings) {
            this.bookings = bookings;
        }
        // Getters and setters
    }

    // Đối tượng cho Số dư tài khoản và Nạp tiền
    class AccountBalanceObject {
        String userId;
        double balance;
        String currency;

        public AccountBalanceObject(String userId, double balance, String currency) {
            this.userId = userId;
            this.balance = balance;
            this.currency = currency;
        }
        // Getters and setters
    }

    class TopUpInitiationResultObject {
        String transactionId;
        String redirectUrl; // Nếu cần chuyển hướng đến cổng thanh toán
        String message;

        public TopUpInitiationResultObject(String transactionId, String redirectUrl, String message) {
            this.transactionId = transactionId;
            this.redirectUrl = redirectUrl;
            this.message = message;
        }
        // Getters and setters
    }

    class TopUpHistoryObject {
        String transactionId;
        double amount;
        String currency;
        String topUpDate;
        String status; // e.g., "SUCCESS", "PENDING", "FAILED"
        String paymentMethodUsed;

        public TopUpHistoryObject(String transactionId, double amount, String currency, String topUpDate, String status, String paymentMethodUsed) {
            this.transactionId = transactionId;
            this.amount = amount;
            this.currency = currency;
            this.topUpDate = topUpDate;
            this.status = status;
            this.paymentMethodUsed = paymentMethodUsed;
        }
        // Getters and setters
    }

    class TopUpHistoryListObject {
        List<TopUpHistoryObject> topUps;

        public TopUpHistoryListObject(List<TopUpHistoryObject> topUps) {
            this.topUps = topUps;
        }
        // Getters and setters
    }

    // Custom Exceptions (ví dụ)
    class UserNotFoundException extends RuntimeException {
        public UserNotFoundException(String message) { super(message); }
    }
    class ProductNotFoundException extends RuntimeException {
        public ProductNotFoundException(String message) { super(message); }
    }
    class OutOfStockException extends RuntimeException {
        public OutOfStockException(String message) { super(message); }
    }
    class InvalidQuantityException extends RuntimeException {
        public InvalidQuantityException(String message) { super(message); }
    }
    class CartItemNotFoundException extends RuntimeException {
        public CartItemNotFoundException(String message) { super(message); }
    }
}
