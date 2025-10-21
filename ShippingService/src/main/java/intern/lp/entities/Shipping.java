package intern.lp.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;


@Entity
@Table(name = "shippings")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@NonNull
public class Shipping {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "address", nullable = false)
    private String address;

    @Column(nullable = false)
    private String status; // CREATED, SHIPPED, DELIVERED

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;


}
