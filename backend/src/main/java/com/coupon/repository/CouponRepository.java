package com.coupon.repository;

import com.coupon.entity.Coupon;
import com.coupon.entity.CouponStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.Optional;
import java.util.stream.Stream;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long>, JpaSpecificationExecutor<Coupon> {

    Optional<Coupon> findByCode(String code);

    boolean existsByCode(String code);

    Page<Coupon> findByBatchId(Long batchId, Pageable pageable);

    @Query("SELECT c FROM Coupon c JOIN FETCH c.batch b JOIN FETCH b.campaign WHERE c.code = :code")
    Optional<Coupon> findByCodeWithBatchAndCampaign(@Param("code") String code);

    @Modifying
    @Query("UPDATE Coupon c SET c.status = :status, c.updatedAt = CURRENT_TIMESTAMP WHERE c.batch.id = :batchId")
    int updateStatusByBatchId(@Param("batchId") Long batchId, @Param("status") CouponStatus status);

    @Modifying
    @Query("UPDATE Coupon c SET c.status = 'EXPIRED', c.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE c.batch.id IN (SELECT b.id FROM CouponBatch b WHERE b.campaign.expiryDate < :today) " +
           "AND c.status = 'ACTIVE'")
    int expireCoupons(@Param("today") LocalDate today);

    @Query("SELECT c FROM Coupon c WHERE c.batch.id = :batchId")
    Stream<Coupon> streamByBatchId(@Param("batchId") Long batchId);

    @Query("SELECT c FROM Coupon c")
    Stream<Coupon> streamAll();

    @Query("SELECT COUNT(c) FROM Coupon c WHERE c.batch.campaign.id = :campaignId")
    Long countByCampaignId(@Param("campaignId") Long campaignId);
}
