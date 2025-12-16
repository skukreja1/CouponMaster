package com.coupon.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "coupon_batch")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "coupon_batch_gen")
    @SequenceGenerator(name = "coupon_batch_gen", sequenceName = "coupon_batch_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id", nullable = false)
    private Campaign campaign;

    @Column(nullable = false, length = 6)
    private String prefix;

    @Column(name = "coupon_count", nullable = false)
    private Integer couponCount;

    @Column(name = "pos_code", length = 50)
    private String posCode;

    @Column(name = "atg_code", length = 50)
    private String atgCode;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    @Column(name = "max_usages", nullable = false)
    @Builder.Default
    private Integer maxUsages = 1;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
