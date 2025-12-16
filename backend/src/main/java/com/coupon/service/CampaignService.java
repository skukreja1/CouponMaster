package com.coupon.service;

import com.coupon.dto.CampaignDTO;
import com.coupon.entity.Campaign;
import com.coupon.repository.CampaignRepository;
import com.coupon.repository.CouponBatchRepository;
import com.coupon.repository.CouponRepository;
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
public class CampaignService {

    private final CampaignRepository campaignRepository;
    private final CouponBatchRepository batchRepository;
    private final CouponRepository couponRepository;

    private static final String PREFIX_START = "FF";

    @Transactional(readOnly = true)
    public List<CampaignDTO> getAllCampaigns() {
        return campaignRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CampaignDTO> getActiveCampaigns() {
        return campaignRepository.findAllActiveCampaigns()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CampaignDTO getCampaignById(Long id) {
        Campaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Campaign not found with id: " + id));
        return toDTO(campaign);
    }

    @Transactional
    public CampaignDTO createCampaign(CampaignDTO dto) {
        if (campaignRepository.existsByName(dto.getName())) {
            throw new RuntimeException("Campaign with name '" + dto.getName() + "' already exists");
        }

        validateDates(dto.getStartDate(), dto.getExpiryDate());

        String prefix = PREFIX_START + dto.getUserPrefix().toUpperCase();
        if (prefix.length() != 6) {
            throw new RuntimeException("Prefix must be exactly 6 characters (FF + 4 user characters)");
        }

        Campaign campaign = Campaign.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .posCode(dto.getPosCode())
                .atgCode(dto.getAtgCode())
                .prefix(prefix)
                .maxUsages(dto.getMaxUsages())
                .startDate(dto.getStartDate())
                .expiryDate(dto.getExpiryDate())
                .active(true)
                .build();

        Campaign saved = campaignRepository.save(campaign);
        log.info("Created campaign: {} with id: {} and prefix: {}", saved.getName(), saved.getId(), prefix);
        return toDTO(saved);
    }

    @Transactional
    public CampaignDTO updateCampaign(Long id, CampaignDTO dto) {
        Campaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Campaign not found with id: " + id));

        if (!campaign.getName().equals(dto.getName()) && campaignRepository.existsByName(dto.getName())) {
            throw new RuntimeException("Campaign with name '" + dto.getName() + "' already exists");
        }

        validateDates(dto.getStartDate(), dto.getExpiryDate());

        String prefix = PREFIX_START + dto.getUserPrefix().toUpperCase();
        if (prefix.length() != 6) {
            throw new RuntimeException("Prefix must be exactly 6 characters (FF + 4 user characters)");
        }

        campaign.setName(dto.getName());
        campaign.setDescription(dto.getDescription());
        campaign.setPosCode(dto.getPosCode());
        campaign.setAtgCode(dto.getAtgCode());
        campaign.setPrefix(prefix);
        campaign.setMaxUsages(dto.getMaxUsages());
        campaign.setStartDate(dto.getStartDate());
        campaign.setExpiryDate(dto.getExpiryDate());

        Campaign saved = campaignRepository.save(campaign);
        log.info("Updated campaign: {} with id: {}", saved.getName(), saved.getId());
        return toDTO(saved);
    }

    @Transactional
    public void deleteCampaign(Long id) {
        Campaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Campaign not found with id: " + id));
        campaign.setActive(false);
        campaignRepository.save(campaign);
        log.info("Soft deleted campaign with id: {}", id);
    }

    @Transactional
    public void reactivateCampaign(Long id) {
        Campaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Campaign not found with id: " + id));
        campaign.setActive(true);
        campaignRepository.save(campaign);
        log.info("Reactivated campaign with id: {}", id);
    }

    private void validateDates(LocalDate startDate, LocalDate expiryDate) {
        if (expiryDate.isBefore(startDate)) {
            throw new RuntimeException("Expiry date must be on or after start date");
        }
    }

    private CampaignDTO toDTO(Campaign campaign) {
        int batchCount = batchRepository.findByCampaignIdOrderByCreatedAtDesc(campaign.getId()).size();
        Long totalCoupons = couponRepository.countByCampaignId(campaign.getId());

        String prefix = campaign.getPrefix() != null ? campaign.getPrefix() : "FFTEST";
        String userPrefix = prefix.length() > 2 ? prefix.substring(2) : "";

        return CampaignDTO.builder()
                .id(campaign.getId())
                .name(campaign.getName())
                .description(campaign.getDescription())
                .posCode(campaign.getPosCode())
                .atgCode(campaign.getAtgCode())
                .prefix(campaign.getPrefix())
                .userPrefix(userPrefix)
                .maxUsages(campaign.getMaxUsages())
                .startDate(campaign.getStartDate())
                .expiryDate(campaign.getExpiryDate())
                .createdAt(campaign.getCreatedAt())
                .updatedAt(campaign.getUpdatedAt())
                .active(campaign.getActive())
                .batchCount(batchCount)
                .totalCoupons(totalCoupons)
                .build();
    }
}
