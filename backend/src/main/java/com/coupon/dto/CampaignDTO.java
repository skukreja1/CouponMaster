package com.coupon.dto;

import jakarta.validation.constraints.*;
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

    @NotBlank(message = "User prefix is required")
    @Size(min = 4, max = 4, message = "User prefix must be exactly 4 characters")
    @Pattern(regexp = "^[A-Z0-9]{4}$", message = "User prefix must contain only uppercase letters and numbers")
    private String userPrefix;

    private String prefix;

    @NotNull(message = "Max usages is required")
    @Min(value = 1, message = "Max usages must be at least 1")
    private Integer maxUsages;

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
