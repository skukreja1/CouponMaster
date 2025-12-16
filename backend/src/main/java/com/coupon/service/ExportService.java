package com.coupon.service;

import com.coupon.entity.Coupon;
import com.coupon.repository.CouponBatchRepository;
import com.coupon.repository.CouponRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExportService {

    private final CouponRepository couponRepository;
    private final CouponBatchRepository batchRepository;

    private static final String CSV_HEADER = "Coupon Code,Status,Usage Count,Max Usages,Start Date,Expiry Date,Campaign,Batch ID,POS Code,ATG Code,Created At";

    @Transactional(readOnly = true)
    public void exportBatchToCSV(Long batchId, HttpServletResponse response) throws IOException {
        if (!batchRepository.existsById(batchId)) {
            throw new RuntimeException("Batch not found with id: " + batchId);
        }

        log.info("Starting CSV export for batch {}", batchId);
        setupCSVResponse(response, "batch_" + batchId + "_coupons.csv");

        try (PrintWriter writer = response.getWriter();
             Stream<Coupon> couponStream = couponRepository.streamByBatchId(batchId)) {
            
            writer.println(CSV_HEADER);
            
            couponStream.forEach(coupon -> {
                writer.println(formatCouponCSV(coupon));
                writer.flush();
            });
        }

        log.info("Completed CSV export for batch {}", batchId);
    }

    @Transactional(readOnly = true)
    public void exportAllToCSV(HttpServletResponse response) throws IOException {
        log.info("Starting full database CSV export");
        setupCSVResponse(response, "all_coupons.csv");

        try (PrintWriter writer = response.getWriter();
             Stream<Coupon> couponStream = couponRepository.streamAll()) {
            
            writer.println(CSV_HEADER);
            
            couponStream.forEach(coupon -> {
                writer.println(formatCouponCSV(coupon));
                writer.flush();
            });
        }

        log.info("Completed full database CSV export");
    }

    private void setupCSVResponse(HttpServletResponse response, String filename) {
        response.setContentType("text/csv");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");
    }

    private String formatCouponCSV(Coupon coupon) {
        return String.join(",",
                escapeCSV(coupon.getCode()),
                escapeCSV(coupon.getStatus().name()),
                String.valueOf(coupon.getUsageCount()),
                String.valueOf(coupon.getBatch().getMaxUsages()),
                escapeCSV(coupon.getBatch().getStartDate().toString()),
                escapeCSV(coupon.getBatch().getExpiryDate().toString()),
                escapeCSV(coupon.getBatch().getCampaign().getName()),
                String.valueOf(coupon.getBatch().getId()),
                escapeCSV(coupon.getBatch().getPosCode() != null ? coupon.getBatch().getPosCode() : ""),
                escapeCSV(coupon.getBatch().getAtgCode() != null ? coupon.getBatch().getAtgCode() : ""),
                escapeCSV(coupon.getCreatedAt().toString())
        );
    }

    private String escapeCSV(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
