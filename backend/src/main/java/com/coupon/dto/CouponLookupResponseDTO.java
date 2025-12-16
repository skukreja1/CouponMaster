package com.coupon.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponLookupResponseDTO {
    private String couponCode;
    private String posCode;
    private String atgCode;
    private String status;
    private Integer usageCount;
    private Integer maxUsages;
    private String campaignName;
    private String startDate;
    private String expiryDate;
}
