package intern.lp.dto;


import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductRequest  implements Serializable {
    private String name;
    private String description;
    private Double price;
}
