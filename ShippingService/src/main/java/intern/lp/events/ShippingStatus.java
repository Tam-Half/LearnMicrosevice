package intern.lp.events;

public enum ShippingStatus {
    PENDING,        // Đang chờ xử lý
    PROCESSING,     // Đang đóng gói
    SHIPPED,        // Đã gửi đi
    DELIVERY,       // Đang giao hàng (như bạn yêu cầu)
    COMPLETED,      // Giao hàng thành công
    CANCELLED       // Đã hủy
}