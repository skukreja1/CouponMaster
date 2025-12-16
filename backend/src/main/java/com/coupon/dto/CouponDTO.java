package com.coupon.dto;

import com.coupon.entity.CouponStatus;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponDTO {

    private Long id;
    private Long batchId;
    private String code;
    private CouponStatus status;
    private Integer usageCount;
    private Integer maxUsages;
    private LocalDate startDate;
    private LocalDate expiryDate;
    private String campaignName;
    private String posCode;
    private String atgCode;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
