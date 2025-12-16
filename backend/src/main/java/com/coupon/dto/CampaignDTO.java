package com.coupon.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
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

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean active;
    private Integer batchCount;
    private Long totalCoupons;
}
