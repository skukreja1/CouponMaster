package com.coupon.service;

import com.coupon.dto.BatchUpdateDTO;
import com.coupon.dto.CouponBatchDTO;
import com.coupon.entity.Campaign;
import com.coupon.entity.CouponBatch;
import com.coupon.entity.CouponStatus;
import com.coupon.repository.CampaignRepository;
import com.coupon.repository.CouponBatchRepository;
import com.coupon.repository.CouponRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CouponBatchService {

    private final CouponBatchRepository batchRepository;
    private final CampaignRepository campaignRepository;
    private final CouponRepository couponRepository;
    private final CouponGeneratorService couponGeneratorService;
    private final EntityManager entityManager;

    private static final String PREFIX_START = "FF";

    @Transactional(readOnly = true)
    public List<CouponBatchDTO> getAllBatches() {
        return batchRepository.findAllWithCampaign()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CouponBatchDTO> getBatchesByCampaign(Long campaignId) {
        return batchRepository.findByCampaignIdWithCampaign(campaignId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CouponBatchDTO getBatchById(Long id) {
        CouponBatch batch = batchRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Batch not found with id: " + id));
        return toDTO(batch);
    }

    @Transactional
    public CouponBatchDTO createBatch(CouponBatchDTO dto) {
        validateDates(dto.getStartDate(), dto.getExpiryDate());

        Campaign campaign = campaignRepository.findById(dto.getCampaignId())
                .orElseThrow(() -> new RuntimeException("Campaign not found with id: " + dto.getCampaignId()));

        String prefix = PREFIX_START + dto.getUserPrefix().toUpperCase();
        if (prefix.length() != 6) {
            throw new RuntimeException("Prefix must be exactly 6 characters (FF + 4 user characters)");
        }

        CouponBatch batch = CouponBatch.builder()
                .campaign(campaign)
                .prefix(prefix)
                .couponCount(dto.getCouponCount())
                .posCode(dto.getPosCode())
                .atgCode(dto.getAtgCode())
                .startDate(dto.getStartDate())
                .expiryDate(dto.getExpiryDate())
                .maxUsages(dto.getMaxUsages())
                .active(true)
                .build();

        CouponBatch savedBatch = batchRepository.save(batch);
        entityManager.flush();
        log.info("Created batch {} with prefix {} for campaign {}", savedBatch.getId(), prefix, campaign.getName());

        int generated = couponGeneratorService.generateCoupons(savedBatch, dto.getCouponCount());
        log.info("Generated {} coupons for batch {}", generated, savedBatch.getId());

        return toDTO(savedBatch);
    }

    @Transactional
    public CouponBatchDTO updateBatch(Long id, BatchUpdateDTO dto) {
        CouponBatch batch = batchRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Batch not found with id: " + id));

        if (dto.getStartDate() != null && dto.getExpiryDate() != null) {
            validateDates(dto.getStartDate(), dto.getExpiryDate());
        } else if (dto.getStartDate() != null) {
            validateDates(dto.getStartDate(), batch.getExpiryDate());
        } else if (dto.getExpiryDate() != null) {
            validateDates(batch.getStartDate(), dto.getExpiryDate());
        }

        if (dto.getPosCode() != null) {
            batch.setPosCode(dto.getPosCode());
        }
        if (dto.getAtgCode() != null) {
            batch.setAtgCode(dto.getAtgCode());
        }
        if (dto.getStartDate() != null) {
            batch.setStartDate(dto.getStartDate());
        }
        if (dto.getExpiryDate() != null) {
            batch.setExpiryDate(dto.getExpiryDate());
        }
        if (dto.getMaxUsages() != null) {
            batch.setMaxUsages(dto.getMaxUsages());
        }

        CouponBatch saved = batchRepository.save(batch);
        log.info("Updated batch {}", id);
        return toDTO(saved);
    }

    @Transactional
    public void softDeleteBatch(Long id) {
        CouponBatch batch = batchRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Batch not found with id: " + id));
        batch.setActive(false);
        batchRepository.save(batch);
        
        couponRepository.updateStatusByBatchId(id, CouponStatus.INACTIVE);
        log.info("Soft deleted batch {} and set all coupons to INACTIVE", id);
    }

    @Transactional
    public void reactivateBatch(Long id) {
        CouponBatch batch = batchRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Batch not found with id: " + id));
        batch.setActive(true);
        batchRepository.save(batch);
        
        couponRepository.updateStatusByBatchId(id, CouponStatus.ACTIVE);
        log.info("Reactivated batch {} and set all coupons to ACTIVE", id);
    }

    private void validateDates(LocalDate startDate, LocalDate expiryDate) {
        LocalDate today = LocalDate.now();
        if (startDate.isBefore(today)) {
            throw new RuntimeException("Start date must be today or in the future");
        }
        if (expiryDate.isBefore(today)) {
            throw new RuntimeException("Expiry date must be today or in the future");
        }
        if (expiryDate.isBefore(startDate)) {
            throw new RuntimeException("Expiry date must be on or after start date");
        }
    }

    private CouponBatchDTO toDTO(CouponBatch batch) {
        Long activeCoupons = batchRepository.countActiveCouponsByBatchId(batch.getId());
        Long usedCoupons = batchRepository.countUsedCouponsByBatchId(batch.getId());
        Long expiredCoupons = batchRepository.countExpiredCouponsByBatchId(batch.getId());

        String userPrefix = batch.getPrefix().substring(2);

        return CouponBatchDTO.builder()
                .id(batch.getId())
                .campaignId(batch.getCampaign().getId())
                .campaignName(batch.getCampaign().getName())
                .prefix(batch.getPrefix())
                .userPrefix(userPrefix)
                .couponCount(batch.getCouponCount())
                .posCode(batch.getPosCode())
                .atgCode(batch.getAtgCode())
                .startDate(batch.getStartDate())
                .expiryDate(batch.getExpiryDate())
                .maxUsages(batch.getMaxUsages())
                .createdAt(batch.getCreatedAt())
                .updatedAt(batch.getUpdatedAt())
                .active(batch.getActive())
                .activeCoupons(activeCoupons)
                .usedCoupons(usedCoupons)
                .expiredCoupons(expiredCoupons)
                .build();
    }
}
