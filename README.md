# Customer Management API

A production-ready Customer Management REST API built with Spring Boot, PostgreSQL, Spring Security (JWT-ready), and Docker.

## Features
- **Clean Architecture:** Divided into Controller, Service, Repository, DTO, Model, Exception, and Configuration layers.
- **REST Endpoints:** Complete CRUD operations for customers (`GET`, `POST`, `PUT`, `DELETE`).
- **Validation:** String length, email format, and optional phone format validation on requests.
- **Global Exception Handling:** Custom exception mapping to unified error JSON payloads.
- **OpenAPI / Swagger UI:** Integrated Swagger documentation reachable at `/swagger-ui.html`.
- **Stateless Security:** Pre-configured Spring Security stateless filter chain ready for JWT.
- **Health Monitoring:** Enabled Spring Boot Actuator health checks at `/actuator/health`.
- **DevOps Ready:** Multi-stage `Dockerfile`, `docker-compose.yml` stack, and `.env.example`.
- **Test Coverage:** Automated JUnit 5 & Mockito test suite for database, business, and controller layers.

## Tech Stack
- **Language:** Java 21
- **Framework:** Spring Boot 4.1.0 / Spring Security 6.x
- **Database:** PostgreSQL (Production/Dev), H2 (Testing)
- **Docs:** SpringDoc OpenAPI 3.0.3
- **Containerization:** Docker / Docker Compose

## Quick Start
1. Clone the repository.
2. Run `docker-compose up --build`.
3. Open Swagger UI at `http://localhost:8700/swagger-ui.html`.
