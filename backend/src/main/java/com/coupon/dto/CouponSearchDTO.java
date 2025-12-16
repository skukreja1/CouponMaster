package com.coupon.dto;

import com.coupon.entity.CouponStatus;
import lombok.*;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponSearchDTO {

    private String code;
    private String prefix;
    private CouponStatus status;
    private Long campaignId;
    private Long batchId;
    private LocalDate createdFrom;
    private LocalDate createdTo;
    private Integer page;
    private Integer size;
}
