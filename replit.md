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
- `POST /api/campaigns` - Create campaign
- `PUT /api/campaigns/{id}` - Update campaign
- `DELETE /api/campaigns/{id}` - Deactivate campaign
- `PUT /api/campaigns/{id}/reactivate` - Reactivate campaign

### Batches (Admin)
- `GET /api/batches` - List all batches
- `GET /api/batches/campaign/{campaignId}` - Batches by campaign
- `POST /api/batches` - Create/generate batch (up to 3M coupons)
- `PUT /api/batches/{id}` - Update batch
- `DELETE /api/batches/{id}` - Deactivate batch
- `PUT /api/batches/{id}/reactivate` - Reactivate batch

### Coupons (Admin)
- `GET /api/coupons/search` - Search coupons with filters

### Redemption (Public - No Auth)
- `POST /api/public/redeem` - Validate and redeem coupon

### Export (Admin)
- `GET /api/export/batch/{id}` - Export batch as CSV
- `GET /api/export/all` - Export all coupons as CSV

## Key Design Decisions
1. **Memory-safe batch generation**: JDBC batch inserts limited to 5000 rows per batch for memory efficiency
2. **Global uniqueness**: Coupon codes checked for global uniqueness during generation
3. **Streaming export**: CSV exports use streaming to handle large datasets
4. **Database URL handling**: Custom DataSourceConfig converts Replit's PostgreSQL URL format to JDBC format

## Running Locally
1. Backend: `cd backend && mvn spring-boot:run` (port 8080)
2. Frontend: `cd frontend && npm start` (port 5000)
