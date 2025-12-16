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

    @NotBlank(message = "User prefix is required")
    @Size(min = 4, max = 4, message = "User prefix must be exactly 4 characters")
    @Pattern(regexp = "^[A-Z0-9]{4}$", message = "User prefix must contain only uppercase letters and numbers")
    private String userPrefix;

    private String prefix;

    @NotNull(message = "Coupon count is required")
    @Min(value = 1, message = "Coupon count must be at least 1")
    @Max(value = 3000000, message = "Coupon count cannot exceed 3,000,000")
    private Integer couponCount;

    @NotNull(message = "Max usages is required")
    @Min(value = 1, message = "Max usages must be at least 1")
    private Integer maxUsages;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean active;

    private Long activeCoupons;
    private Long usedCoupons;
    private Long expiredCoupons;
}
