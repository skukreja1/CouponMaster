package com.coupon.repository;

import com.coupon.entity.ExportJob;
import com.coupon.entity.ExportStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExportJobRepository extends JpaRepository<ExportJob, Long> {

    List<ExportJob> findByBatchIdOrderByCreatedAtDesc(Long batchId);

    List<ExportJob> findByCampaignIdOrderByCreatedAtDesc(Long campaignId);

    List<ExportJob> findByStatusOrderByCreatedAtAsc(ExportStatus status);

    List<ExportJob> findAllByOrderByCreatedAtDesc();
}
