package com.coupon.repository;

import com.coupon.entity.CouponBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CouponBatchRepository extends JpaRepository<CouponBatch, Long> {

    List<CouponBatch> findByCampaignIdOrderByCreatedAtDesc(Long campaignId);

    List<CouponBatch> findByActiveTrueOrderByCreatedAtDesc();

    @Query("SELECT cb FROM CouponBatch cb JOIN FETCH cb.campaign ORDER BY cb.createdAt DESC")
    List<CouponBatch> findAllWithCampaign();

    @Query("SELECT cb FROM CouponBatch cb JOIN FETCH cb.campaign WHERE cb.campaign.id = :campaignId ORDER BY cb.createdAt DESC")
    List<CouponBatch> findByCampaignIdWithCampaign(@Param("campaignId") Long campaignId);

    @Query("SELECT COUNT(c) FROM Coupon c WHERE c.batch.id = :batchId AND c.status = 'ACTIVE'")
    Long countActiveCouponsByBatchId(@Param("batchId") Long batchId);

    @Query("SELECT COUNT(c) FROM Coupon c WHERE c.batch.id = :batchId AND c.usageCount > 0")
    Long countUsedCouponsByBatchId(@Param("batchId") Long batchId);

    @Query("SELECT COUNT(c) FROM Coupon c WHERE c.batch.id = :batchId AND c.status = 'EXPIRED'")
    Long countExpiredCouponsByBatchId(@Param("batchId") Long batchId);
}
