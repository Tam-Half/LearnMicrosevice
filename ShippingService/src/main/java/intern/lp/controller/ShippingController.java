package intern.lp.controller;

import intern.lp.dto.request.ShippingRequest;
import intern.lp.dto.response.ShippingResponse;
import intern.lp.service.ShippingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/shippings")
@RequiredArgsConstructor
public class ShippingController {

    private final ShippingService shippingService;

    @PostMapping
    public ShippingResponse createShipping(@RequestBody ShippingRequest request) {
        return shippingService.createShipping(request);
    }
}
