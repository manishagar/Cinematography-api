# Cinematography

Spring Boot microservices + React application for photography, video editing,
customization, customer portal, admin portal, payments and contact management.

## Technology
- Java 25
- Spring Boot 4.0.8
- Spring Cloud
- Maven multi-module
- Spring Security + JWT
- Eureka
- Spring Cloud Gateway
- SQL Server
- React + Vite

## Start order
1. SQL Server
2. Config Server :8888
3. Eureka :8761
4. Auth :8081
5. Other microservices
6. API Gateway :8080
7. React :5173

## Important






























Y` to the same strong, Base64-encoded signing key for both
the admin API and API gateway. Both services use it to sign and verify admin
tokens. Change the SQL Server password and JWT secret before any real
deployment.
The initial business services are intentionally scaffolds; media storage,
payment gateway verification, authorization filters, and full CRUD will be
implemented in the next phases.
