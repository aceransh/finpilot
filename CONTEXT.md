# FinPilot - Project Specification & Rules

## 1. Project Goal
Build "FinPilot," a personal finance dashboard that aggregates bank data via Plaid and auto-categorizes transactions using a complex rules engine.

## 2. Tech Stack
- **Root:** Monorepo
- **Backend:** Java 21, Spring Boot 3.x, Maven.
- **Database:** PostgreSQL (Docker), Flyway for migrations (Strict requirement).
- **Frontend:** React (Vite), TypeScript, Material UI (MUI) v5, Redux Toolkit.

## 3. Database Schema (PostgreSQL)
*Strictly enforce these types in V1__init_schema.sql*

- **users:** id (UUID), email (Unique), password_hash, created_at.
- **plaid_items:** id (UUID), user_id (FK), access_token, item_id, status.
- **accounts:** id (UUID), plaid_item_id (FK), plaid_account_id (Unique), name, type, balance.
- **categories:** id (BigSerial), user_id (FK, Nullable), name, color_hex.
- **categorization_rules:**
    - id (BigSerial), user_id (FK), keyword (Text), category_id (FK), priority (Int).
    - match_type (ENUM): CONTAINS, EXACT, STARTS_WITH, REGEX.
- **transactions:** id (UUID), account_id (FK), plaid_transaction_id (Unique), amount, date, description, category_id.

## 4. Coding Standards (The "Vibe")
- **Backend:** Use Lombok (@Data, @Builder). Use Service layer for logic.
- **Validation:** If match_type is REGEX, validate the pattern before saving.
- **Frontend:** Clean functional components. Use MUI Grid for layout.