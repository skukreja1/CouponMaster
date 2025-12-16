package com.coupon.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RedemptionRequestDTO {

    @NotBlank(message = "Coupon code is required")
    private String code;
    
    private String transactionNumber;
    
    private String loyaltyId;
    
    private String source;
}
