package com.coupon.dto;

import com.coupon.entity.ExportStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExportJobDTO {

    private Long id;
    private Long batchId;
    private Long campaignId;
    private String exportType;
    private ExportStatus status;
    private String fileName;
    private Long totalRecords;
    private String errorMessage;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
}
