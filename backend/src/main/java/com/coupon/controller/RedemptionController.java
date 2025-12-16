package com.coupon.controller;

import com.coupon.dto.RedemptionRequestDTO;
import com.coupon.dto.RedemptionResponseDTO;
import com.coupon.service.RedemptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public/redeem")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class RedemptionController {

    private final RedemptionService redemptionService;

    @PostMapping
    public ResponseEntity<RedemptionResponseDTO> redeemCoupon(@Valid @RequestBody RedemptionRequestDTO request) {
        RedemptionResponseDTO response = redemptionService.redeemCoupon(request.getCode());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{code}")
    public ResponseEntity<RedemptionResponseDTO> redeemCouponGet(@PathVariable String code) {
        RedemptionResponseDTO response = redemptionService.redeemCoupon(code);
        return ResponseEntity.ok(response);
    }
}
