package com.coupon.service;

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
        Campaign campaign = campaignRepository.findById(dto.getCampaignId())
                .orElseThrow(() -> new RuntimeException("Campaign not found with id: " + dto.getCampaignId()));

        CouponBatch batch = CouponBatch.builder()
                .campaign(campaign)
                .couponCount(dto.getCouponCount())
                .active(true)
                .build();

        CouponBatch savedBatch = batchRepository.save(batch);
        entityManager.flush();
        log.info("Created batch {} for campaign {} with prefix {}", savedBatch.getId(), campaign.getName(), campaign.getPrefix());

        int generated = couponGeneratorService.generateCoupons(savedBatch, dto.getCouponCount());
        log.info("Generated {} coupons for batch {}", generated, savedBatch.getId());

        return toDTO(savedBatch);
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

    private CouponBatchDTO toDTO(CouponBatch batch) {
        Long activeCoupons = batchRepository.countActiveCouponsByBatchId(batch.getId());
        Long usedCoupons = batchRepository.countUsedCouponsByBatchId(batch.getId());
        Long expiredCoupons = batchRepository.countExpiredCouponsByBatchId(batch.getId());

        Campaign campaign = batch.getCampaign();
        String prefix = campaign.getPrefix() != null ? campaign.getPrefix() : "FFTEST";
        String userPrefix = prefix.length() > 2 ? prefix.substring(2) : "";

        return CouponBatchDTO.builder()
                .id(batch.getId())
                .campaignId(campaign.getId())
                .campaignName(campaign.getName())
                .prefix(campaign.getPrefix())
                .userPrefix(userPrefix)
                .couponCount(batch.getCouponCount())
                .maxUsages(campaign.getMaxUsages())
                .createdAt(batch.getCreatedAt())
                .updatedAt(batch.getUpdatedAt())
                .active(batch.getActive())
                .activeCoupons(activeCoupons)
                .usedCoupons(usedCoupons)
                .expiredCoupons(expiredCoupons)
                .build();
    }
}
