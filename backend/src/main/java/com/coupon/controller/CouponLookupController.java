package com.coupon.controller;

import com.coupon.dto.CouponLookupResponseDTO;
import com.coupon.entity.Campaign;
import com.coupon.entity.Coupon;
import com.coupon.entity.CouponBatch;
import com.coupon.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public/coupon")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CouponLookupController {

    private final CouponRepository couponRepository;

    @GetMapping("/{code}")
    public ResponseEntity<CouponLookupResponseDTO> lookupCoupon(@PathVariable String code) {
        Coupon coupon = couponRepository.findByCodeWithBatchAndCampaign(code.toUpperCase())
                .orElseThrow(() -> new RuntimeException("Coupon not found: " + code));

        CouponBatch batch = coupon.getBatch();
        Campaign campaign = batch.getCampaign();

        CouponLookupResponseDTO response = CouponLookupResponseDTO.builder()
                .couponCode(coupon.getCode())
                .posCode(campaign.getPosCode())
                .atgCode(campaign.getAtgCode())
                .status(coupon.getStatus().name())
                .usageCount(coupon.getUsageCount())
                .maxUsages(campaign.getMaxUsages())
                .campaignName(campaign.getName())
                .startDate(campaign.getStartDate() != null ? campaign.getStartDate().toString() : null)
                .expiryDate(campaign.getExpiryDate() != null ? campaign.getExpiryDate().toString() : null)
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/lookup")
    public ResponseEntity<CouponLookupResponseDTO> lookupCouponPost(@RequestBody CouponLookupRequestDTO request) {
        return lookupCoupon(request.getCode());
    }

    @lombok.Getter
    @lombok.Setter
    @lombok.NoArgsConstructor
    public static class CouponLookupRequestDTO {
        private String code;
    }
}
