package com.coupon.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RedemptionResponseDTO {

    private boolean success;
    private String message;
    private String code;
    private Integer usageCount;
    private Integer maxUsages;
    private Integer remainingUsages;
    private String transactionNumber;
    private String loyaltyId;
    private String source;
    private String redeemedAt;
}
