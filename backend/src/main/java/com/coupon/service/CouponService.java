package com.coupon.service;

import com.coupon.dto.CouponDTO;
import com.coupon.dto.CouponSearchDTO;
import com.coupon.dto.PagedResponseDTO;
import com.coupon.entity.Campaign;
import com.coupon.entity.Coupon;
import com.coupon.entity.CouponBatch;
import com.coupon.entity.CouponStatus;
import com.coupon.repository.CouponRepository;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CouponService {

    private final CouponRepository couponRepository;

    @Transactional(readOnly = true)
    public PagedResponseDTO<CouponDTO> searchCoupons(CouponSearchDTO searchDTO) {
        int page = searchDTO.getPage() != null ? searchDTO.getPage() : 0;
        int size = searchDTO.getSize() != null ? searchDTO.getSize() : 20;
        size = Math.min(size, 100);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Specification<Coupon> spec = buildSpecification(searchDTO);
        
        Page<Coupon> couponPage = couponRepository.findAll(spec, pageable);
        
        List<CouponDTO> content = couponPage.getContent()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        return PagedResponseDTO.<CouponDTO>builder()
                .content(content)
                .page(couponPage.getNumber())
                .size(couponPage.getSize())
                .totalElements(couponPage.getTotalElements())
                .totalPages(couponPage.getTotalPages())
                .first(couponPage.isFirst())
                .last(couponPage.isLast())
                .build();
    }

    @Transactional(readOnly = true)
    public CouponDTO getCouponByCode(String code) {
        Coupon coupon = couponRepository.findByCodeWithBatchAndCampaign(code)
                .orElseThrow(() -> new RuntimeException("Coupon not found with code: " + code));
        return toDTO(coupon);
    }

    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void expireOldCoupons() {
        LocalDate today = LocalDate.now();
        int expired = couponRepository.expireCoupons(today);
        if (expired > 0) {
            log.info("Expired {} coupons that passed their expiry date", expired);
        }
    }

    private Specification<Coupon> buildSpecification(CouponSearchDTO searchDTO) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            Join<Coupon, CouponBatch> batchJoin = root.join("batch");

            if (searchDTO.getCode() != null && !searchDTO.getCode().isEmpty()) {
                predicates.add(cb.like(cb.upper(root.get("code")), 
                        "%" + searchDTO.getCode().toUpperCase() + "%"));
            }

            if (searchDTO.getPrefix() != null && !searchDTO.getPrefix().isEmpty()) {
                predicates.add(cb.like(cb.upper(root.get("code")), 
                        searchDTO.getPrefix().toUpperCase() + "%"));
            }

            if (searchDTO.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), searchDTO.getStatus()));
            }

            if (searchDTO.getBatchId() != null) {
                predicates.add(cb.equal(batchJoin.get("id"), searchDTO.getBatchId()));
            }

            if (searchDTO.getCampaignId() != null) {
                predicates.add(cb.equal(batchJoin.get("campaign").get("id"), searchDTO.getCampaignId()));
            }

            if (searchDTO.getCreatedFrom() != null) {
                LocalDateTime fromDateTime = searchDTO.getCreatedFrom().atStartOfDay();
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), fromDateTime));
            }

            if (searchDTO.getCreatedTo() != null) {
                LocalDateTime toDateTime = searchDTO.getCreatedTo().atTime(LocalTime.MAX);
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), toDateTime));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private CouponDTO toDTO(Coupon coupon) {
        CouponBatch batch = coupon.getBatch();
        Campaign campaign = batch.getCampaign();
        return CouponDTO.builder()
                .id(coupon.getId())
                .batchId(batch.getId())
                .code(coupon.getCode())
                .status(coupon.getStatus())
                .usageCount(coupon.getUsageCount())
                .maxUsages(batch.getMaxUsages())
                .startDate(campaign.getStartDate())
                .expiryDate(campaign.getExpiryDate())
                .campaignName(campaign.getName())
                .posCode(campaign.getPosCode())
                .atgCode(campaign.getAtgCode())
                .createdAt(coupon.getCreatedAt())
                .updatedAt(coupon.getUpdatedAt())
                .build();
    }
}
