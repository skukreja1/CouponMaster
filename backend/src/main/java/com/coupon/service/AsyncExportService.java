package com.coupon.service;

import com.coupon.dto.ExportJobDTO;
import com.coupon.entity.*;
import com.coupon.repository.CouponBatchRepository;
import com.coupon.repository.CouponRepository;
import com.coupon.repository.ExportJobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AsyncExportService {

    private final ExportJobRepository exportJobRepository;
    private final CouponRepository couponRepository;
    private final CouponBatchRepository batchRepository;

    private static final String EXPORT_DIR = "exports";
    private static final DateTimeFormatter FILE_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    @Transactional
    public ExportJobDTO submitBatchExport(Long batchId) {
        CouponBatch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new RuntimeException("Batch not found: " + batchId));

        ExportJob job = ExportJob.builder()
                .batchId(batchId)
                .campaignId(batch.getCampaign().getId())
                .exportType("BATCH")
                .status(ExportStatus.PENDING)
                .build();

        ExportJob saved = exportJobRepository.save(job);
        log.info("Created export job {} for batch {}", saved.getId(), batchId);

        processExportAsync(saved.getId());

        return toDTO(saved);
    }

    @Transactional
    public ExportJobDTO submitAllExport() {
        ExportJob job = ExportJob.builder()
                .exportType("ALL")
                .status(ExportStatus.PENDING)
                .build();

        ExportJob saved = exportJobRepository.save(job);
        log.info("Created export job {} for all coupons", saved.getId());

        processExportAsync(saved.getId());

        return toDTO(saved);
    }

    @Async
    public void processExportAsync(Long jobId) {
        try {
            Thread.sleep(100);
            processExport(jobId);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Export job {} interrupted", jobId);
        }
    }

    @Transactional
    public void processExport(Long jobId) {
        ExportJob job = exportJobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Export job not found: " + jobId));

        try {
            job.setStatus(ExportStatus.PROCESSING);
            exportJobRepository.save(job);

            File exportDir = new File(EXPORT_DIR);
            if (!exportDir.exists()) {
                exportDir.mkdirs();
            }

            String timestamp = LocalDateTime.now().format(FILE_DATE_FORMAT);
            String fileName;
            List<Coupon> coupons;

            if ("BATCH".equals(job.getExportType())) {
                fileName = "batch_" + job.getBatchId() + "_" + timestamp + ".csv";
                coupons = couponRepository.findAllByBatchId(job.getBatchId());
            } else {
                fileName = "all_coupons_" + timestamp + ".csv";
                coupons = couponRepository.findAll();
            }

            String filePath = EXPORT_DIR + "/" + fileName;
            writeCsvFile(filePath, coupons);

            job.setStatus(ExportStatus.COMPLETED);
            job.setFileName(fileName);
            job.setFilePath(filePath);
            job.setTotalRecords((long) coupons.size());
            job.setCompletedAt(LocalDateTime.now());
            exportJobRepository.save(job);

            log.info("Export job {} completed. File: {}, Records: {}", jobId, fileName, coupons.size());

        } catch (Exception e) {
            log.error("Export job {} failed: {}", jobId, e.getMessage(), e);
            job.setStatus(ExportStatus.FAILED);
            job.setErrorMessage(e.getMessage());
            job.setCompletedAt(LocalDateTime.now());
            exportJobRepository.save(job);
        }
    }

    private void writeCsvFile(String filePath, List<Coupon> coupons) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write("Coupon Code,Status,Usage Count,Max Usages,Campaign,Batch ID,Start Date,Expiry Date,Transaction Number,Loyalty ID,Source,Redeemed At");
            writer.newLine();

            for (Coupon coupon : coupons) {
                CouponBatch batch = coupon.getBatch();
                Campaign campaign = batch.getCampaign();

                StringBuilder line = new StringBuilder();
                line.append(coupon.getCode()).append(",");
                line.append(coupon.getStatus()).append(",");
                line.append(coupon.getUsageCount()).append(",");
                line.append(campaign.getMaxUsages()).append(",");
                line.append(escapeCSV(campaign.getName())).append(",");
                line.append(batch.getId()).append(",");
                line.append(campaign.getStartDate()).append(",");
                line.append(campaign.getExpiryDate()).append(",");
                line.append(coupon.getTransactionNumber() != null ? coupon.getTransactionNumber() : "").append(",");
                line.append(coupon.getLoyaltyId() != null ? coupon.getLoyaltyId() : "").append(",");
                line.append(coupon.getSource() != null ? coupon.getSource() : "").append(",");
                line.append(coupon.getRedeemedAt() != null ? coupon.getRedeemedAt().toString() : "");

                writer.write(line.toString());
                writer.newLine();
            }
        }
    }

    private String escapeCSV(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    @Transactional(readOnly = true)
    public ExportJobDTO getJobStatus(Long jobId) {
        ExportJob job = exportJobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Export job not found: " + jobId));
        return toDTO(job);
    }

    @Transactional(readOnly = true)
    public List<ExportJobDTO> getJobsForBatch(Long batchId) {
        return exportJobRepository.findByBatchIdOrderByCreatedAtDesc(batchId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ExportJobDTO> getAllJobs() {
        return exportJobRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public File getExportFile(Long jobId) {
        ExportJob job = exportJobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Export job not found: " + jobId));

        if (job.getStatus() != ExportStatus.COMPLETED) {
            throw new RuntimeException("Export is not ready for download. Status: " + job.getStatus());
        }

        File file = new File(job.getFilePath());
        if (!file.exists()) {
            throw new RuntimeException("Export file not found: " + job.getFilePath());
        }

        return file;
    }

    private ExportJobDTO toDTO(ExportJob job) {
        return ExportJobDTO.builder()
                .id(job.getId())
                .batchId(job.getBatchId())
                .campaignId(job.getCampaignId())
                .exportType(job.getExportType())
                .status(job.getStatus())
                .fileName(job.getFileName())
                .totalRecords(job.getTotalRecords())
                .errorMessage(job.getErrorMessage())
                .createdAt(job.getCreatedAt())
                .completedAt(job.getCompletedAt())
                .build();
    }
}
