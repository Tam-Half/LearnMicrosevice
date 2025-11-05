package intern.lp.service;

import intern.lp.dto.request.ShippingRequest;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class NotifycationService {

    @Autowired
    private JavaMailSender mailSender;

    @RabbitListener(queues = "notification.queue")
    public void sendEmail(ShippingRequest shipping) {
        System.out.println("✅ Received Shipping Event: " + shipping);

        // Build danh sách sản phẩm
        StringBuilder productDetails = new StringBuilder();
        if (shipping.getOrderItems() != null) {
            for (ShippingRequest.OrderItem item : shipping.getOrderItems()) {
                productDetails.append("- ")
                        .append(item.getProductName())
                        .append(" (SL: ").append(item.getQuantity()).append(")")
                        .append(" - ").append(item.getPrice()).append(" VND\n");
            }
        }

        // Nội dung email
        String emailText = "Có đơn hàng mới từ khách: " + shipping.getCustomerName() +
                "\nEmail: " + shipping.getCustomerEmail() +
                "\nSố điện thoại: " + shipping.getCustomerPhone() +
                "\nĐịa chỉ giao: " + shipping.getShippingAddress() +
                "\n\nSản phẩm:\n" + productDetails +
                "\nTổng tiền: " + shipping.getTotalAmount() + " VND";

        // Gửi email
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(" tam.half28@gmail.com");
        message.setSubject("New Order Notification - Order #" + shipping.getOrderId());
        message.setText(emailText);

        mailSender.send(message);
    }
}