package com.coupon.controller;

import com.coupon.dto.CouponBatchDTO;
import com.coupon.service.CouponBatchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/batches")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class BatchController {

    private final CouponBatchService batchService;

    @GetMapping
    public ResponseEntity<List<CouponBatchDTO>> getAllBatches() {
        return ResponseEntity.ok(batchService.getAllBatches());
    }

    @GetMapping("/campaign/{campaignId}")
    public ResponseEntity<List<CouponBatchDTO>> getBatchesByCampaign(@PathVariable Long campaignId) {
        return ResponseEntity.ok(batchService.getBatchesByCampaign(campaignId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CouponBatchDTO> getBatchById(@PathVariable Long id) {
        return ResponseEntity.ok(batchService.getBatchById(id));
    }

    @PostMapping
    public ResponseEntity<CouponBatchDTO> createBatch(@Valid @RequestBody CouponBatchDTO dto) {
        CouponBatchDTO created = batchService.createBatch(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> softDeleteBatch(@PathVariable Long id) {
        batchService.softDeleteBatch(id);
        return ResponseEntity.ok(Map.of("message", "Batch soft deleted and all coupons set to INACTIVE"));
    }

    @PostMapping("/{id}/reactivate")
    public ResponseEntity<Map<String, String>> reactivateBatch(@PathVariable Long id) {
        batchService.reactivateBatch(id);
        return ResponseEntity.ok(Map.of("message", "Batch reactivated and all coupons set to ACTIVE"));
    }
}
