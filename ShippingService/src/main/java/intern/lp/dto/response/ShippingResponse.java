package intern.lp.dto.response;

import lombok.*;

@Data
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ShippingResponse {
   Long shippingId;
   String status;
}
