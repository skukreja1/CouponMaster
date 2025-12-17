# Coupon Management System

A production-grade Spring Boot + Angular coupon management system supporting batch generation of up to 3 million serialized coupons with global uniqueness guarantee.

## Features

- **Campaign Management**: Create and manage marketing campaigns with configurable settings
- **Batch Generation**: Generate up to 3 million unique coupon codes per batch
- **Unique Coupon Codes**: Format `FF` + 4 custom characters + 8 random characters (A-Z, 0-9)
- **Public Coupon Lookup API**: No authentication required for coupon validation
- **Redemption Tracking**: Track transaction details, loyalty IDs, and source systems
- **CSV Export**: Export coupons by batch or all coupons
- **Spring Security**: Basic authentication for admin operations

## Tech Stack

| Component | Technology |
|-----------|------------|
| Backend | Spring Boot 3.2, Java 17, Maven |
| Frontend | Angular 19, Angular Material 19 |
| Database | PostgreSQL (development), Oracle (production-ready) |
| Security | Spring Security Basic Auth |

## Getting Started

### Prerequisites

- Java 17+
- Node.js 18+
- PostgreSQL database

### Installation

1. **Clone the repository**

2. **Start the Backend**
   ```bash
   cd backend
   mvn spring-boot:run
   ```
   Backend runs on port 8080

3. **Start the Frontend**
   ```bash
   cd frontend
   npm install
   npm start
   ```
   Frontend runs on port 5000

### Default Credentials

- **Username**: `admin`
- **Password**: `admin123`

## Coupon Code Format

All coupon codes follow this format:
```
FF + [4 user-defined chars] + [8 random chars]
```

Example: `FFABCD12X45Y78Z9`

- `FF` - Fixed prefix
- `ABCD` - User-defined prefix (set per campaign)
- `12X45Y78Z9` - Random alphanumeric characters (A-Z, 0-9)

## Architecture

### Campaign-Level Settings

All coupon settings are managed at the Campaign level:
- Campaign Name & Description
- POS Code (required)
- ATG Code (required)
- Start Date & Expiry Date
- User Prefix (4 characters)
- Max Usages per coupon

### Batch Generation

Batches only specify:
- Campaign selection (inherits all settings)
- Number of coupons to generate

This design ensures consistency across all coupons within a campaign.

## API Reference

### Authentication
All admin endpoints require Basic Auth credentials.

### Public Endpoints (No Auth Required)

#### Coupon Lookup
```http
GET /api/public/coupon/{code}
POST /api/public/coupon/lookup
```

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

#### Coupon Redemption
```http
POST /api/public/redeem
```

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

### Admin Endpoints (Auth Required)

#### Campaigns
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/campaigns` | List all campaigns |
| GET | `/api/campaigns/active` | List active campaigns |
| POST | `/api/campaigns` | Create campaign |
| PUT | `/api/campaigns/{id}` | Update campaign |
| DELETE | `/api/campaigns/{id}` | Deactivate campaign |
| PUT | `/api/campaigns/{id}/reactivate` | Reactivate campaign |

#### Batches
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/batches` | List all batches |
| GET | `/api/batches/campaign/{campaignId}` | Batches by campaign |
| POST | `/api/batches` | Create/generate batch |
| DELETE | `/api/batches/{id}` | Deactivate batch |
| PUT | `/api/batches/{id}/reactivate` | Reactivate batch |

#### Coupons
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/coupons/search` | Search coupons with filters |

#### Export
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/export/batch/{id}` | Export batch as CSV |
| GET | `/api/export/all` | Export all coupons as CSV |

## Project Structure

```
/backend
  └── Spring Boot application
      ├── src/main/java/com/coupon/
      │   ├── entity/          # JPA entities
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
          ├── components/      # UI components
          ├── services/        # API services
          ├── guards/          # Route guards
          └── interceptors/    # HTTP interceptors
```

## Database Support

The system includes DDL scripts for both:
- **PostgreSQL** - Used in development
- **Oracle** - Production-ready schema provided

## Security Considerations

- Basic authentication for admin endpoints
- Public endpoints for POS/ATG integration (no auth required)
- CORS enabled for cross-origin requests
- Password hashing recommended for production deployment

## Performance

- JDBC batch inserts limited to 5000 rows per batch for memory efficiency
- Streaming CSV export for large datasets
- Database-level unique constraints with retry logic for code generation
