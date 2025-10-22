package intern.lp.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Entity
@Table(name = "shipping")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Shipping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long orderId;
    private Long customerId;

    private String customerName;
    private String customerPhone;
    private String customerEmail;

    private String address;
    private String orderItems;
    private BigDecimal totalAmount;
    private LocalDateTime orderDate;

    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
