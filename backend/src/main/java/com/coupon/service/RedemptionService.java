package com.coupon.service;

import com.coupon.dto.RedemptionResponseDTO;
import com.coupon.entity.Campaign;
import com.coupon.entity.Coupon;
import com.coupon.entity.CouponBatch;
import com.coupon.entity.CouponStatus;
import com.coupon.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedemptionService {

    private final CouponRepository couponRepository;

    @Transactional
    public RedemptionResponseDTO redeemCoupon(String code) {
        Optional<Coupon> couponOpt = couponRepository.findByCodeWithBatchAndCampaign(code.toUpperCase());

        if (couponOpt.isEmpty()) {
            log.warn("Redemption attempt for non-existent coupon: {}", code);
            return RedemptionResponseDTO.builder()
                    .success(false)
                    .message("Coupon not found")
                    .code(code)
                    .build();
        }

        Coupon coupon = couponOpt.get();
        CouponBatch batch = coupon.getBatch();
        Campaign campaign = batch.getCampaign();

        if (!batch.getActive()) {
            log.warn("Redemption attempt for inactive batch coupon: {}", code);
            return RedemptionResponseDTO.builder()
                    .success(false)
                    .message("This coupon batch has been deactivated")
                    .code(code)
                    .build();
        }

        if (!campaign.getActive()) {
            log.warn("Redemption attempt for inactive campaign coupon: {}", code);
            return RedemptionResponseDTO.builder()
                    .success(false)
                    .message("This campaign has been deactivated")
                    .code(code)
                    .build();
        }

        if (coupon.getStatus() == CouponStatus.INACTIVE) {
            log.warn("Redemption attempt for inactive coupon: {}", code);
            return RedemptionResponseDTO.builder()
                    .success(false)
                    .message("Coupon is inactive")
                    .code(code)
                    .build();
        }

        if (coupon.getStatus() == CouponStatus.EXPIRED) {
            log.warn("Redemption attempt for expired coupon: {}", code);
            return RedemptionResponseDTO.builder()
                    .success(false)
                    .message("Coupon has expired")
                    .code(code)
                    .build();
        }

        if (coupon.getStatus() == CouponStatus.MAX_USED) {
            log.warn("Redemption attempt for max-used coupon: {}", code);
            return RedemptionResponseDTO.builder()
                    .success(false)
                    .message("Coupon has reached maximum usage limit")
                    .code(code)
                    .usageCount(coupon.getUsageCount())
                    .maxUsages(campaign.getMaxUsages())
                    .remainingUsages(0)
                    .build();
        }

        LocalDate today = LocalDate.now();
        if (today.isBefore(campaign.getStartDate())) {
            log.warn("Redemption attempt for coupon not yet valid: {}", code);
            return RedemptionResponseDTO.builder()
                    .success(false)
                    .message("Coupon is not yet valid. Valid from: " + campaign.getStartDate())
                    .code(code)
                    .build();
        }

        if (today.isAfter(campaign.getExpiryDate())) {
            coupon.setStatus(CouponStatus.EXPIRED);
            couponRepository.save(coupon);
            log.warn("Redemption attempt for date-expired coupon: {}", code);
            return RedemptionResponseDTO.builder()
                    .success(false)
                    .message("Coupon has expired")
                    .code(code)
                    .build();
        }

        int newUsageCount = coupon.getUsageCount() + 1;
        coupon.setUsageCount(newUsageCount);

        if (newUsageCount >= campaign.getMaxUsages()) {
            coupon.setStatus(CouponStatus.MAX_USED);
            log.info("Coupon {} has reached max usage ({}/{})", code, newUsageCount, campaign.getMaxUsages());
        }

        couponRepository.save(coupon);
        log.info("Successfully redeemed coupon: {} (usage {}/{})", code, newUsageCount, campaign.getMaxUsages());

        return RedemptionResponseDTO.builder()
                .success(true)
                .message("Coupon redeemed successfully")
                .code(code)
                .usageCount(newUsageCount)
                .maxUsages(campaign.getMaxUsages())
                .remainingUsages(campaign.getMaxUsages() - newUsageCount)
                .build();
    }
}
