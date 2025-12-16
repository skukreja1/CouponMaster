package com.coupon.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RedemptionRequestDTO {

    @NotBlank(message = "Coupon code is required")
    @Size(min = 14, max = 14, message = "Coupon code must be exactly 14 characters")
    private String code;
}
