package com.coupon.service;

import com.coupon.entity.CouponBatch;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class CouponGeneratorService {

    private final JdbcTemplate jdbcTemplate;

    private static final int BATCH_SIZE = 5000;
    private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int SERIAL_LENGTH = 8;
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int MAX_RETRY_ATTEMPTS = 10;

    @Transactional
    public int generateCoupons(CouponBatch batch, int count) {
        log.info("Starting memory-safe coupon generation for batch {} with {} coupons", batch.getId(), count);
        
        String prefix = batch.getPrefix();
        int totalGenerated = 0;
        int batchNumber = 0;
        
        String insertSql = "INSERT INTO coupon (id, batch_id, code, status, usage_count, created_at, updated_at) " +
                     "VALUES (nextval('coupon_seq'), ?, ?, 'ACTIVE', 0, ?, ?) ON CONFLICT (code) DO NOTHING";
        
        while (totalGenerated < count) {
            int remaining = count - totalGenerated;
            int targetBatchSize = Math.min(BATCH_SIZE, remaining);
            batchNumber++;
            
            List<String> candidateCodes = generateUniqueCandidates(prefix, targetBatchSize * 2);
            List<String> availableCodes = filterExistingCodes(candidateCodes);
            
            if (availableCodes.size() < targetBatchSize) {
                int needed = targetBatchSize - availableCodes.size();
                int retryAttempts = 0;
                while (availableCodes.size() < targetBatchSize && retryAttempts < MAX_RETRY_ATTEMPTS) {
                    List<String> moreCandidates = generateUniqueCandidates(prefix, needed * 3);
                    List<String> moreAvailable = filterExistingCodes(moreCandidates);
                    for (String code : moreAvailable) {
                        if (!availableCodes.contains(code) && availableCodes.size() < targetBatchSize) {
                            availableCodes.add(code);
                        }
                    }
                    retryAttempts++;
                    needed = targetBatchSize - availableCodes.size();
                }
            }
            
            int currentBatchSize = Math.min(targetBatchSize, availableCodes.size());
            if (currentBatchSize == 0) {
                log.warn("Could not generate any unique codes in batch {}. Stopping.", batchNumber);
                break;
            }
            
            List<String> codesToInsert = availableCodes.subList(0, currentBatchSize);
            
            final Long batchId = batch.getId();
            final Timestamp now = Timestamp.valueOf(LocalDateTime.now());
            
            try {
                int[] results = jdbcTemplate.execute(insertSql, (PreparedStatement ps) -> {
                    for (String code : codesToInsert) {
                        ps.setLong(1, batchId);
                        ps.setString(2, code);
                        ps.setTimestamp(3, now);
                        ps.setTimestamp(4, now);
                        ps.addBatch();
                    }
                    return ps.executeBatch();
                });
                
                int actualInserted = 0;
                if (results != null) {
                    for (int result : results) {
                        if (result > 0 || result == -2) {
                            actualInserted++;
                        }
                    }
                }
                
                totalGenerated += actualInserted;
                
                if (batchNumber % 10 == 0) {
                    log.info("Progress: Generated {} / {} coupons ({}%)", 
                            totalGenerated, count, (totalGenerated * 100) / count);
                }
                
            } catch (Exception e) {
                log.error("Error during batch insert at batch {}: {}", batchNumber, e.getMessage());
                throw new RuntimeException("Failed to generate coupons: " + e.getMessage(), e);
            }
        }
        
        log.info("Completed coupon generation for batch {}. Total generated: {}", batch.getId(), totalGenerated);
        return totalGenerated;
    }
    
    private List<String> generateUniqueCandidates(String prefix, int count) {
        Set<String> candidates = new HashSet<>();
        int attempts = 0;
        int maxAttempts = count * 5;
        
        while (candidates.size() < count && attempts < maxAttempts) {
            String code = prefix + generateRandomSerial();
            candidates.add(code);
            attempts++;
        }
        
        return new ArrayList<>(candidates);
    }
    
    private List<String> filterExistingCodes(List<String> candidates) {
        if (candidates.isEmpty()) {
            return new ArrayList<>();
        }
        
        StringBuilder placeholders = new StringBuilder();
        for (int i = 0; i < candidates.size(); i++) {
            if (i > 0) placeholders.append(",");
            placeholders.append("?");
        }
        
        String checkSql = "SELECT code FROM coupon WHERE code IN (" + placeholders + ")";
        
        Set<String> existingCodes = new HashSet<>(
            jdbcTemplate.queryForList(checkSql, String.class, candidates.toArray())
        );
        
        List<String> available = new ArrayList<>();
        for (String code : candidates) {
            if (!existingCodes.contains(code)) {
                available.add(code);
            }
        }
        
        return available;
    }

    private String generateRandomSerial() {
        StringBuilder sb = new StringBuilder(SERIAL_LENGTH);
        for (int i = 0; i < SERIAL_LENGTH; i++) {
            sb.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
        }
        return sb.toString();
    }
}
