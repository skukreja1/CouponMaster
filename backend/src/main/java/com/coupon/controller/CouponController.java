package com.coupon.controller;

import com.coupon.dto.CouponDTO;
import com.coupon.dto.CouponSearchDTO;
import com.coupon.dto.PagedResponseDTO;
import com.coupon.entity.CouponStatus;
import com.coupon.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CouponController {

    private final CouponService couponService;

    @GetMapping("/search")
    public ResponseEntity<PagedResponseDTO<CouponDTO>> searchCoupons(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String prefix,
            @RequestParam(required = false) CouponStatus status,
            @RequestParam(required = false) Long campaignId,
            @RequestParam(required = false) Long batchId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createdFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createdTo,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {

        CouponSearchDTO searchDTO = CouponSearchDTO.builder()
                .code(code)
                .prefix(prefix)
                .status(status)
                .campaignId(campaignId)
                .batchId(batchId)
                .createdFrom(createdFrom)
                .createdTo(createdTo)
                .page(page)
                .size(size)
                .build();

        return ResponseEntity.ok(couponService.searchCoupons(searchDTO));
    }

    @GetMapping("/{code}")
    public ResponseEntity<CouponDTO> getCouponByCode(@PathVariable String code) {
        return ResponseEntity.ok(couponService.getCouponByCode(code));
    }
}
