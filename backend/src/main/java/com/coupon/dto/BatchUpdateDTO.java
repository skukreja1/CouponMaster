package com.coupon.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchUpdateDTO {

    @Min(value = 1, message = "Max usages must be at least 1")
    private Integer maxUsages;
}
