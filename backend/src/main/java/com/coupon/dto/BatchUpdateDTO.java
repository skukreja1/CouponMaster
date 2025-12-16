package com.coupon.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchUpdateDTO {

    @Size(max = 50, message = "POS code must be less than 50 characters")
    private String posCode;

    @Size(max = 50, message = "ATG code must be less than 50 characters")
    private String atgCode;

    @FutureOrPresent(message = "Start date must be today or in the future")
    private LocalDate startDate;

    @FutureOrPresent(message = "Expiry date must be today or in the future")
    private LocalDate expiryDate;

    @Min(value = 1, message = "Max usages must be at least 1")
    private Integer maxUsages;
}
