# API Gateway

The **API Gateway** serves as the single entry point for all client requests to the Vendo platform.

It is responsible for routing requests to the appropriate backend microservices, load balancing, and handling cross-cutting concerns such as distributed tracing and centralized configuration.

---

# Tech Stack

* Java 17
* Spring Boot
* Docker
* Eureka
* Zipkin
* Lombok
* Maven
* Gateway Server Webflux
---

# Architecture

The service follows **Hexagonal Architecture (Ports and Adapters)** and **CRQS** pattern.

The core business logic is isolated from external systems such as databases, REST APIs, or messaging systems.

## Layers

**adapter**

External integrations and routing rules.
* Contains programmatic route definitions that map incoming request paths to specific downstream microservices.

**infrastructure**

Framework-specific configurations and bean definitions.
* Configurations for Service Discovery and external infrastructure.

---

# Project Structure

```
src
 └── main
     └── java
         └── com.vendo.api_gateway
             ├── adapter
             └── infrastructure
```

---

## Prerequisites

Before running this service, you need to start required infrastructure services.

## Dependencies

This service dynamically routes traffic and fetches configuration, so it heavily depends on:

- **Config Server** – provides externalized routing and application configuration
- **Service Registry (Eureka)** – used to discover routing targets dynamically
- **Zipkin** – for distributed tracing

---

## 1. Clone and run Config Server

```
git clone https://github.com/vendo-marketplace/config-server
cd config-server
mvn spring-boot:run
```


---

## 2. Clone and run Service Registry

```
git clone https://github.com/vendo-marketplace/registry-service
cd registry-service
mvn spring-boot:run
```


# Running the Service

---

## 3. Run application

Or build and run:

```
mvn clean package
java -jar target/api-gateway.jar
```

---

# Environment Variables

| Variable          | Description       | Default   |
|-------------------|-------------------|-----------|
| CONFIG_SERVER_URL | Config server url | 8010      |

---

# Code Style

The project follows standard **Java code conventions**.

Key principles:

* Clean Architecture
* SOLID principles
* Immutable DTOs
* Constructor injection
* Clear separation between layers

---

# Contributing

1. Create feature branch
2. Write tests
3. Ensure tests pass
4. Create pull request