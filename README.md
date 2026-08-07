# Electricity Bill Monitoring

A full-stack local project for monitoring electricity bills with:
- PHP 8 + Apache + MySQL backend
- Android client in Kotlin with Jetpack Compose
- REST APIs with JSON responses
- MySQL schema with seed data

## Project Structure

- backend/: PHP MVC-style backend for REST APIs
- android-app/: Android Studio project using Kotlin, MVVM, Retrofit, Room, Hilt, Compose
- database/: database export helpers
- sql/: schema and seed SQL files
- postman/: API collection
- documentation/: setup and architecture docs

## Local Setup

1. Install XAMPP with Apache and MySQL.
2. Create a MySQL database named `electricity_bill_monitoring`.
3. Import [sql/import.sql](sql/import.sql).
4. Copy [backend/.env.example](backend/.env.example) to [backend/.env](backend/.env) and adjust values.
5. Point Apache document root to [backend/public](backend/public) or place the backend under XAMPP htdocs.
6. Open [android-app](android-app) in Android Studio and sync Gradle.

## Notes

This starter is production-oriented and uses environment-based configuration for external services. Official board and gateway credentials should be supplied through environment variables.
