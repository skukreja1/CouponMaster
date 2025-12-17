# Software Requirements Specification
## Coupon Management System

**Version:** 1.0  
**Date:** December 2025

---

## 1. Introduction

### 1.1 Purpose
This document specifies the software requirements for the Coupon Management System, a web-based application designed to generate, manage, and track promotional coupon codes for retail operations.

### 1.2 Scope
The system enables administrators to create marketing campaigns, generate batches of unique coupon codes, and provides public APIs for Point of Sale (POS) and ATG systems to validate and redeem coupons.

### 1.3 Definitions and Acronyms

| Term | Definition |
|------|------------|
| POS | Point of Sale system |
| ATG | Oracle ATG Web Commerce platform |
| Coupon Code | Unique alphanumeric identifier for promotional discount |
| Campaign | Marketing promotion containing one or more coupon batches |
| Batch | Collection of coupon codes generated together |
| Redemption | The act of using a coupon for a discount |

---

## 2. System Overview

### 2.1 System Context
The Coupon Management System operates as a centralized coupon generation and validation service that integrates with:
- Administrative web interface for campaign management
- POS systems for in-store coupon redemption
- ATG e-commerce platform for online coupon redemption

### 2.2 System Architecture
- **Backend**: RESTful API built with Spring Boot
- **Frontend**: Single Page Application built with Angular
- **Database**: PostgreSQL (development) / Oracle (production)
- **Authentication**: Spring Security with Basic Authentication

---

## 3. Functional Requirements

### 3.1 User Authentication

| ID | Requirement | Priority |
|----|-------------|----------|
| FR-AUTH-001 | System shall authenticate administrators using username and password | High |
| FR-AUTH-002 | System shall use HTTP Basic Authentication for API access | High |
| FR-AUTH-003 | System shall maintain session state for authenticated users | High |
| FR-AUTH-004 | System shall provide logout functionality | Medium |

### 3.2 Campaign Management

| ID | Requirement | Priority |
|----|-------------|----------|
| FR-CAMP-001 | System shall allow creation of new campaigns | High |
| FR-CAMP-002 | System shall require campaign name (max 255 characters) | High |
| FR-CAMP-003 | System shall require POS code for each campaign | High |
| FR-CAMP-004 | System shall require ATG code for each campaign | High |
| FR-CAMP-005 | System shall require a 4-character user prefix (A-Z, 0-9) | High |
| FR-CAMP-006 | System shall require start date for each campaign | High |
| FR-CAMP-007 | System shall require expiry date for each campaign | High |
| FR-CAMP-008 | System shall require max usages per coupon (minimum 1) | High |
| FR-CAMP-009 | System shall allow optional description (max 2000 characters) | Low |
| FR-CAMP-010 | System shall allow editing of existing campaigns | High |
| FR-CAMP-011 | System shall allow deactivation of campaigns | High |
| FR-CAMP-012 | System shall allow reactivation of deactivated campaigns | Medium |
| FR-CAMP-013 | System shall display list of all campaigns with status | High |
| FR-CAMP-014 | System shall display batch count per campaign | Medium |
| FR-CAMP-015 | System shall display total coupon count per campaign | Medium |

### 3.3 Batch Management

| ID | Requirement | Priority |
|----|-------------|----------|
| FR-BATCH-001 | System shall allow creation of new batches for a campaign | High |
| FR-BATCH-002 | System shall require selection of parent campaign | High |
| FR-BATCH-003 | System shall require number of coupons to generate | High |
| FR-BATCH-004 | System shall support batch sizes up to 3 million coupons | High |
| FR-BATCH-005 | System shall inherit all settings from parent campaign | High |
| FR-BATCH-006 | System shall display list of all batches | High |
| FR-BATCH-007 | System shall display batches filtered by campaign | Medium |
| FR-BATCH-008 | System shall allow deactivation of batches | High |
| FR-BATCH-009 | System shall allow reactivation of deactivated batches | Medium |
| FR-BATCH-010 | System shall display generation timestamp for each batch | Medium |

### 3.4 Coupon Code Generation

| ID | Requirement | Priority |
|----|-------------|----------|
| FR-CODE-001 | System shall generate coupon codes in format: FF + 4 chars + 8 random | High |
| FR-CODE-002 | System shall use only uppercase letters (A-Z) and digits (0-9) | High |
| FR-CODE-003 | System shall guarantee global uniqueness across all coupons | High |
| FR-CODE-004 | System shall use database-level unique constraint | High |
| FR-CODE-005 | System shall implement retry logic for collision handling | High |
| FR-CODE-006 | System shall use batch inserts (max 5000 per batch) for performance | High |
| FR-CODE-007 | System shall initialize usage count to 0 for new coupons | High |
| FR-CODE-008 | System shall set initial status to ACTIVE for new coupons | High |

### 3.5 Coupon Search

| ID | Requirement | Priority |
|----|-------------|----------|
| FR-SEARCH-001 | System shall allow searching coupons by code | High |
| FR-SEARCH-002 | System shall allow filtering coupons by campaign | Medium |
| FR-SEARCH-003 | System shall allow filtering coupons by batch | Medium |
| FR-SEARCH-004 | System shall allow filtering coupons by status | Medium |
| FR-SEARCH-005 | System shall support pagination for search results | High |

### 3.6 Public Coupon Lookup API

| ID | Requirement | Priority |
|----|-------------|----------|
| FR-LOOKUP-001 | System shall provide public API for coupon lookup (no auth) | High |
| FR-LOOKUP-002 | System shall return coupon details including POS code | High |
| FR-LOOKUP-003 | System shall return coupon details including ATG code | High |
| FR-LOOKUP-004 | System shall return coupon status | High |
| FR-LOOKUP-005 | System shall return usage count and max usages | High |
| FR-LOOKUP-006 | System shall return campaign name | Medium |
| FR-LOOKUP-007 | System shall return start and expiry dates | High |
| FR-LOOKUP-008 | System shall return appropriate error for invalid codes | High |

### 3.7 Coupon Redemption API

| ID | Requirement | Priority |
|----|-------------|----------|
| FR-REDEEM-001 | System shall provide public API for redemption (no auth) | High |
| FR-REDEEM-002 | System shall validate coupon exists | High |
| FR-REDEEM-003 | System shall validate coupon is active | High |
| FR-REDEEM-004 | System shall validate campaign is active | High |
| FR-REDEEM-005 | System shall validate batch is active | High |
| FR-REDEEM-006 | System shall validate coupon is within valid date range | High |
| FR-REDEEM-007 | System shall validate usage count is below max usages | High |
| FR-REDEEM-008 | System shall increment usage count on successful redemption | High |
| FR-REDEEM-009 | System shall update status to MAX_USED when limit reached | High |
| FR-REDEEM-010 | System shall accept optional transaction number | Medium |
| FR-REDEEM-011 | System shall accept optional loyalty ID | Medium |
| FR-REDEEM-012 | System shall accept optional source (POS/ATG) | Medium |
| FR-REDEEM-013 | System shall record redemption timestamp | High |
| FR-REDEEM-014 | System shall return success/failure with message | High |
| FR-REDEEM-015 | System shall return remaining usages on success | Medium |

### 3.8 CSV Export

| ID | Requirement | Priority |
|----|-------------|----------|
| FR-EXPORT-001 | System shall export coupons by batch as CSV | High |
| FR-EXPORT-002 | System shall export all coupons as CSV | Medium |
| FR-EXPORT-003 | System shall use streaming for large exports | High |
| FR-EXPORT-004 | System shall include coupon code in export | High |
| FR-EXPORT-005 | System shall include status in export | High |
| FR-EXPORT-006 | System shall include usage count in export | Medium |

---

## 4. Non-Functional Requirements

### 4.1 Performance

| ID | Requirement | Priority |
|----|-------------|----------|
| NFR-PERF-001 | System shall generate 10,000 coupons in under 30 seconds | High |
| NFR-PERF-002 | System shall support up to 3 million coupons per batch | High |
| NFR-PERF-003 | Coupon lookup API shall respond in under 200ms | High |
| NFR-PERF-004 | Redemption API shall respond in under 500ms | High |
| NFR-PERF-005 | System shall handle concurrent redemption requests | High |

### 4.2 Scalability

| ID | Requirement | Priority |
|----|-------------|----------|
| NFR-SCALE-001 | System shall support multiple simultaneous admin users | Medium |
| NFR-SCALE-002 | System shall support high-volume redemption traffic | High |
| NFR-SCALE-003 | Database shall handle millions of coupon records | High |

### 4.3 Security

| ID | Requirement | Priority |
|----|-------------|----------|
| NFR-SEC-001 | Admin endpoints shall require authentication | High |
| NFR-SEC-002 | Passwords shall not be stored in plain text | High |
| NFR-SEC-003 | System shall prevent SQL injection attacks | High |
| NFR-SEC-004 | System shall implement CORS policy | Medium |
| NFR-SEC-005 | Public APIs shall not expose sensitive data | High |

### 4.4 Reliability

| ID | Requirement | Priority |
|----|-------------|----------|
| NFR-REL-001 | System shall maintain data integrity during failures | High |
| NFR-REL-002 | System shall use database transactions for redemption | High |
| NFR-REL-003 | System shall handle database connection failures gracefully | Medium |

### 4.5 Compatibility

| ID | Requirement | Priority |
|----|-------------|----------|
| NFR-COMP-001 | System shall support PostgreSQL database | High |
| NFR-COMP-002 | System shall support Oracle database | High |
| NFR-COMP-003 | Frontend shall work in modern web browsers | High |
| NFR-COMP-004 | APIs shall use JSON format for data exchange | High |

### 4.6 Maintainability

| ID | Requirement | Priority |
|----|-------------|----------|
| NFR-MAINT-001 | System shall use layered architecture (controller/service/repository) | High |
| NFR-MAINT-002 | System shall use DTOs for API data transfer | High |
| NFR-MAINT-003 | System shall provide meaningful error messages | Medium |
| NFR-MAINT-004 | System shall log important operations | Medium |

---

## 5. Data Requirements

### 5.1 Campaign Entity

| Field | Type | Constraints |
|-------|------|-------------|
| id | Long | Primary Key, Auto-generated |
| name | String | Required, Max 255 chars |
| description | String | Optional, Max 2000 chars |
| posCode | String | Required, Max 50 chars |
| atgCode | String | Required, Max 50 chars |
| prefix | String | Required, Exactly 6 chars (FF + 4 user chars) |
| maxUsages | Integer | Required, Min 1 |
| startDate | Date | Required |
| expiryDate | Date | Required |
| active | Boolean | Default true |
| createdAt | Timestamp | Auto-generated |
| updatedAt | Timestamp | Auto-updated |

### 5.2 Batch Entity

| Field | Type | Constraints |
|-------|------|-------------|
| id | Long | Primary Key, Auto-generated |
| campaignId | Long | Foreign Key to Campaign |
| batchNumber | Integer | Auto-generated per campaign |
| couponCount | Integer | Required |
| active | Boolean | Default true |
| createdAt | Timestamp | Auto-generated |

### 5.3 Coupon Entity

| Field | Type | Constraints |
|-------|------|-------------|
| id | Long | Primary Key, Auto-generated |
| batchId | Long | Foreign Key to Batch |
| code | String | Required, Unique, 14 chars |
| status | Enum | ACTIVE, INACTIVE, EXPIRED, MAX_USED |
| usageCount | Integer | Default 0 |
| transactionNumber | String | Optional, Max 100 chars |
| loyaltyId | String | Optional, Max 100 chars |
| source | String | Optional, Max 20 chars |
| redeemedAt | Timestamp | Optional |
| createdAt | Timestamp | Auto-generated |

---

## 6. Interface Requirements

### 6.1 User Interface

| ID | Requirement | Priority |
|----|-------------|----------|
| UI-001 | System shall provide login page | High |
| UI-002 | System shall provide dashboard with navigation | High |
| UI-003 | System shall provide campaign list view | High |
| UI-004 | System shall provide campaign create/edit dialog | High |
| UI-005 | System shall provide batch list view | High |
| UI-006 | System shall provide batch create dialog | High |
| UI-007 | System shall provide coupon search interface | High |
| UI-008 | System shall use Material Design components | Medium |
| UI-009 | System shall display loading indicators | Medium |
| UI-010 | System shall display success/error notifications | High |

### 6.2 API Interface

| Endpoint | Method | Auth | Description |
|----------|--------|------|-------------|
| /api/auth/login | POST | No | User authentication |
| /api/campaigns | GET | Yes | List all campaigns |
| /api/campaigns | POST | Yes | Create campaign |
| /api/campaigns/{id} | PUT | Yes | Update campaign |
| /api/campaigns/{id} | DELETE | Yes | Deactivate campaign |
| /api/batches | GET | Yes | List all batches |
| /api/batches | POST | Yes | Create batch |
| /api/batches/{id} | DELETE | Yes | Deactivate batch |
| /api/coupons/search | GET | Yes | Search coupons |
| /api/public/coupon/{code} | GET | No | Lookup coupon |
| /api/public/redeem | POST | No | Redeem coupon |
| /api/export/batch/{id} | GET | Yes | Export batch CSV |
| /api/export/all | GET | Yes | Export all CSV |

---

## 7. Constraints

### 7.1 Technical Constraints
- Backend must be built with Spring Boot 3.x and Java 17+
- Frontend must be built with Angular 19
- Must support both PostgreSQL and Oracle databases
- Must use RESTful API design principles

### 7.2 Business Constraints
- Coupon codes must be globally unique across all campaigns
- Coupon code format is fixed: FF + 4 custom + 8 random characters
- Only uppercase letters (A-Z) and digits (0-9) allowed in codes
- POS Code and ATG Code are mandatory for all campaigns

---

## 8. Assumptions and Dependencies

### 8.1 Assumptions
- Administrators have access to modern web browsers
- POS and ATG systems can make HTTP REST API calls
- Database server is available and properly configured
- Network connectivity is available for all system components

### 8.2 Dependencies
- PostgreSQL or Oracle database server
- Java Runtime Environment 17+
- Node.js for frontend build
- Network infrastructure for API access

---

## 9. Acceptance Criteria

### 9.1 Campaign Management
- Admin can create a campaign with all required fields
- Admin can edit existing campaign details
- Admin can deactivate/reactivate campaigns
- Campaign list displays all campaigns with correct data

### 9.2 Batch Generation
- System generates specified number of unique coupons
- All coupons follow the correct format
- No duplicate codes exist in the database
- Batch creation completes within acceptable time

### 9.3 Coupon Redemption
- Valid coupon can be redeemed successfully
- Invalid/expired coupons are rejected with appropriate message
- Usage count is correctly incremented
- Transaction details are recorded

### 9.4 Integration
- POS system can lookup and redeem coupons via API
- ATG system can lookup and redeem coupons via API
- CSV exports contain accurate coupon data
