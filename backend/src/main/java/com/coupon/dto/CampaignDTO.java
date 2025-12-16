package com.coupon.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CampaignDTO {

    private Long id;

    @NotBlank(message = "Campaign name is required")
    @Size(max = 255, message = "Campaign name must be less than 255 characters")
    private String name;

    @Size(max = 2000, message = "Description must be less than 2000 characters")
    private String description;

    @Size(max = 50, message = "POS code must be less than 50 characters")
    private String posCode;

    @Size(max = 50, message = "ATG code must be less than 50 characters")
    private String atgCode;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "Expiry date is required")
    private LocalDate expiryDate;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean active;
    private Integer batchCount;
    private Long totalCoupons;
}
