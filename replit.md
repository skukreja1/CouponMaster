# Coupon Management System

## Overview
A production-grade Spring Boot + Angular coupon management system supporting batch generation of up to 3 million serialized coupons. Features campaign management, batch operations, coupon search/redemption API, CSV export, and Spring Security Basic Auth.

## Current Status
- Backend: Running on port 8080
- Frontend: Running on port 5000
- Database: PostgreSQL (Replit)

## Tech Stack
- **Backend**: Spring Boot 3.2, Java 17, Maven, JPA/Hibernate
- **Frontend**: Angular 19, Angular Material 19
- **Database**: PostgreSQL (development), Oracle (production schema provided)
- **Auth**: Spring Security Basic Auth

## Default Credentials
- Username: `admin`
- Password: `admin123`

## Coupon Code Format
Format: `FF` + 4 user-defined characters + 8 random characters (A-Z, 0-9)
Example: `FFABCD12X45Y78Z9`

## Architecture: Campaign-Level Settings

**All campaign settings are managed at the Campaign level:**
- Name & Description
- POS Code & ATG Code
- Start Date & Expiry Date
- User Prefix (4 characters, stored as FFxxxx)
- Max Usages per coupon

**Batches only specify:**
- Campaign selection (inherits all settings from campaign)
- Number of coupons to generate

This design ensures consistency across all coupons within a campaign and simplifies batch creation.

## Project Structure
```
/backend
  └── Spring Boot application
      ├── src/main/java/com/coupon/
      │   ├── entity/          # JPA entities (Campaign, CouponBatch, Coupon)
      │   ├── dto/             # Data transfer objects
      │   ├── repository/      # Spring Data repositories
      │   ├── service/         # Business logic
      │   ├── controller/      # REST controllers
      │   └── config/          # Security, datasource config
      └── src/main/resources/
          ├── schema-oracle.sql     # Oracle DDL
          └── schema-postgresql.sql # PostgreSQL DDL

/frontend
  └── Angular application
      └── src/app/
          ├── components/      # UI components (login, dashboard, campaigns, batches, coupons)
          ├── services/        # API services
          ├── guards/          # Route guards
          └── interceptors/    # HTTP interceptors
```

## API Endpoints

### Authentication
- `POST /api/auth/login` - Login (Basic Auth)

### Campaigns (Admin)
- `GET /api/campaigns` - List all campaigns
- `GET /api/campaigns/active` - List active campaigns
- `POST /api/campaigns` - Create campaign (includes prefix, maxUsages, dates, codes)
- `PUT /api/campaigns/{id}` - Update campaign
- `DELETE /api/campaigns/{id}` - Deactivate campaign
- `PUT /api/campaigns/{id}/reactivate` - Reactivate campaign

### Batches (Admin)
- `GET /api/batches` - List all batches (displays inherited campaign settings)
- `GET /api/batches/campaign/{campaignId}` - Batches by campaign
- `POST /api/batches` - Create/generate batch (only specifies campaignId and count)
- `DELETE /api/batches/{id}` - Deactivate batch
- `PUT /api/batches/{id}/reactivate` - Reactivate batch

### Coupons (Admin)
- `GET /api/coupons/search` - Search coupons with filters

### Coupon Lookup (Public - No Auth)
- `GET /api/public/coupon/{code}` - Get coupon details by code
- `POST /api/public/coupon/lookup` - Get coupon details (body: `{"code": "COUPONCODE"}`)

**Response:**
```json
{
  "couponCode": "FFTESTZU3H1SWD",
  "posCode": "C1234",
  "atgCode": "A1234",
  "status": "ACTIVE",
  "usageCount": 0,
  "maxUsages": 1,
  "campaignName": "$5 off $50",
  "startDate": "2025-12-16",
  "expiryDate": "2026-12-16"
}
```

### Redemption (Public - No Auth)
- `POST /api/public/redeem` - Validate and redeem coupon

**Request:**
```json
{
  "code": "FFTESTZU3H1SWD",
  "transactionNumber": "TXN123456",
  "loyaltyId": "LOYALTY789",
  "source": "POS"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Coupon redeemed successfully",
  "code": "FFTESTZU3H1SWD",
  "usageCount": 1,
  "maxUsages": 1,
  "remainingUsages": 0,
  "transactionNumber": "TXN123456",
  "loyaltyId": "LOYALTY789",
  "source": "POS",
  "redeemedAt": "2025-12-16T18:41:23.421306792"
}
```

### Export (Admin)
- `GET /api/export/batch/{id}` - Export batch as CSV
- `GET /api/export/all` - Export all coupons as CSV

## Key Design Decisions
1. **Campaign-level settings**: All coupon settings (prefix, maxUsages, dates, codes) are defined at campaign level
2. **Simplified batch creation**: Batches only require campaign selection and coupon count
3. **Memory-safe batch generation**: JDBC batch inserts limited to 5000 rows per batch
4. **Global uniqueness**: Coupon codes checked for global uniqueness during generation
5. **Streaming export**: CSV exports use streaming to handle large datasets
6. **Database URL handling**: Custom DataSourceConfig converts Replit's PostgreSQL URL format to JDBC format

## Running Locally
1. Backend: `cd backend && mvn spring-boot:run` (port 8080)
2. Frontend: `cd frontend && npm start` (port 5000)
