package com.coupon.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponBatchDTO {

    private Long id;

    @NotNull(message = "Campaign ID is required")
    private Long campaignId;

    private String campaignName;

    private String prefix;

    private String userPrefix;

    @NotNull(message = "Coupon count is required")
    @Min(value = 1, message = "Coupon count must be at least 1")
    @Max(value = 3000000, message = "Coupon count cannot exceed 3,000,000")
    private Integer couponCount;

    private Integer maxUsages;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean active;

    private Long activeCoupons;
    private Long usedCoupons;
    private Long expiredCoupons;
}
