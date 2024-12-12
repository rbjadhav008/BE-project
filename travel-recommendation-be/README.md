**Travel Recommendation System Backend**

This is the backend application for the Travel Recommendation System, built using a microservices architecture with Spring Boot and Java.

**Table of Contents**

- Description
- Prerequisites
- Installation
- Usage
- Microservices
- Technologies Used


**Description**

The Travel Recommendation System Backend is a collection of microservices that provide the core functionality for the Travel Recommendation System application. It handles user authentication, recommendation generation, and other essential features.

**Prerequisites**

Java 11 or later
Maven


**Installation**

Clone the repository: git clone https://githyd.epam.com/epm-petr/team-4-08.08/travel-recommendation-be.git
Navigate to the project directory: cd travel-recommendation-be
Build the project: mvn clean install

**Usage**

Start the microservices:

Option 1: Run each microservice individually using the mvn spring-boot:run command in the respective service directory.

- Admin Service: http://localhost:8081
- API Gateway: http://localhost:8084
- Recommendation Service: http://localhost:8083
- Service Registry: http://localhost:8761
- User Service: http://localhost:8082


Microservices

Admin Service: Handles administrative tasks and operations.
API Gateway: Acts as the entry point for all incoming requests, routing them to the appropriate microservices.
Recommendation Service: Responsible for generating travel recommendations based on user preferences and other factors.
Service Registry: Provides service discovery and registration for the microservices.
User Service: Handles user authentication, registration, and user-related operations.

**Technologies Used**

Java 11
Spring Boot
Spring Cloud
Netflix Eureka
Maven




