package com.coupon.repository;

import com.coupon.entity.Campaign;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CampaignRepository extends JpaRepository<Campaign, Long> {

    List<Campaign> findByActiveTrue();

    List<Campaign> findAllByOrderByCreatedAtDesc();

    Optional<Campaign> findByName(String name);

    boolean existsByName(String name);

    @Query("SELECT c FROM Campaign c WHERE c.active = true ORDER BY c.createdAt DESC")
    List<Campaign> findAllActiveCampaigns();
}
