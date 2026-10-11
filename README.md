# ShopEase — Full Stack Java E-Commerce Project

A beginner-friendly Spring Boot + Thymeleaf + JDBC + MySQL student project based on the supplied Full Stack Java E-Commerce Project Guide.

## Features included
- Responsive home page and product cards
- Product details
- Search, category and min/max price filters
- Session-based cart: add, update quantity, remove, total calculation
- Demo checkout that records orders in MySQL (no real payment)
- Signup form with BCrypt password hashing (login/authentication is not implemented)
- Seed data for six sample products

## Requirements
- Java 17 or later (Gradle toolchain uses Java 17)
- MySQL 8+
- Internet for Gradle to download dependencies and for remote product images

## Run locally
1. Create the database:
   ```sql
   CREATE DATABASE ecommerce_db;
   ```
2. Configure credentials in `src/main/resources/application.properties`, or set environment variables:
   - `DB_URL=jdbc:mysql://localhost:3306/ecommerce_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC`
   - `DB_USERNAME=root`
   - `DB_PASSWORD=your_mysql_password`
3. Run from this folder:
   ```bash
   gradle bootRun
   ```
   Or open the folder in IntelliJ IDEA / Eclipse as an existing Gradle project and run `EcommerceApplication`.
4. Visit http://localhost:8080

Spring runs `schema.sql` and `data.sql` at startup. Ensure the MySQL database exists first.

## Important notes
- This is a college/student demo, not a production-ready commerce system.
- Checkout does not process payments and intentionally does not request card number, expiry or CVV.
- Signup stores a BCrypt password hash, but login/session authentication is intentionally not included in this starter.
- Before public deployment, add Spring Security, CSRF protection, validation, proper error handling, stock checks, migrations, and payment-provider integration.
