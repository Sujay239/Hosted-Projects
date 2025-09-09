# Secure Chat (backend)

Minimal README for the Secure Chat project (backend). It explains how to run the app locally and calls out that the database must be hosted on localhost (not a cloud DB) unless you explicitly change configuration.

## Project summary

This is the backend for a WebSocket-based chat application using Spring Boot, STOMP over SockJS, and JPA. It exposes REST endpoints for message history and provides real-time messaging over WebSocket endpoints.

Main features

- Public chat topic: /topic/public
- Private messages delivered via user destinations (e.g. /user/{username}/queue/messages)
- Room chat topic pattern: /topic/rooms/{roomId}
- Persistence for messages using JPA (messages table)

## Prerequisites

- Java 17+ (match project's configured Java version)
- Maven (or use your IDE to build/run)
- A relational database on your local machine (MySQL, PostgreSQL or another supported DB)
  - IMPORTANT: By default this project expects the database to be hosted on localhost (not in the cloud). See the caution below.
- Browser with SockJS + STOMP client (frontend static files included under src/main/resources/static)

## Important caution (READ)

The project is configured to use a database running on your local machine (localhost). Do not assume this is a cloud database. If you deploy to another environment or want to use a managed/cloud DB, update `src/main/resources/application.properties` (or the appropriate environment variables) to point to your remote database, update credentials, and ensure network connectivity and security rules are adjusted.

Typical local DB settings (example for MySQL, put in application.properties when testing locally):

spring.datasource.url=jdbc:mysql://localhost:3306/chatdb?useSSL=false&serverTimezone=UTC
spring.datasource.username=your_db_user
spring.datasource.password=your_db_password
spring.jpa.hibernate.ddl-auto=update

If you are using PostgreSQL or another DB, update the JDBC URL and driver accordingly.

## Build & run (local)

1. Configure the local database and create the schema/database (e.g. chatdb).
2. Update `application.properties` with DB credentials above.
3. From project root (backend) run:

   mvn clean package
   mvn spring-boot:run

or run the produced jar:

java -jar target/<artifact>.jar

4. Open the frontend at: http://localhost:8080/chat.html (static files served by Spring Boot)

## WebSocket / STOMP

- Endpoint: /ws (SockJS fallback)
- Application destination prefix: /app
- Broker prefixes: /topic, /queue
- User destination prefix: /user

Example client subscriptions (frontend already uses SockJS + STOMP):

- /topic/public
- /user/queue/messages
- /topic/rooms/{roomId}

## Useful REST endpoints

- GET /api/messages/conversation/{userA}/{userB} — fetch private conversation history between two usernames
- GET /api/messages/room/{roomId} — fetch messages for a room

## Notes and troubleshooting

- Ensure the JWT settings (if used) match frontend tokens and that WebSocket connections include Authorization header on CONNECT.
- If private messages are not received by the other user, confirm the receiver is connected and subscribed to their user queue (/user/queue/messages) and that the WebSocket principal is set correctly on connect.
- If JPA entities do not persist, verify DB connectivity, user/permissions, and that ddl-auto is configured appropriately.

If you need the README adjusted (add examples, env-specific instructions, or CI/deployment notes), tell me what to include.
