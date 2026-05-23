# Backend Foundation Documentation

## Overview
This document outlines the complete backend foundation for the Lost-in-Woods text-based survival game built with Java Spring Boot, PostgreSQL, and following SOLID principles.

## Project Structure

```
src/main/java/com/LostInWoods/app/
├── entity/                 # JPA entities
│   ├── Player.java
│   ├── Scene.java
│   ├── Choice.java
│   └── GameProgress.java
├── repository/            # Data access layer
│   ├── PlayerRepository.java
│   ├── SceneRepository.java
│   ├── ChoiceRepository.java
│   └── GameProgressRepository.java
├── service/              # Business logic layer
│   ├── PlayerService.java
│   ├── SceneService.java
│   └── GameService.java
├── controller/           # REST API layer
│   ├── PlayerController.java
│   ├── SceneController.java
│   └── GameController.java
├── dto/                  # Data transfer objects
│   ├── PlayerCreateRequest.java
│   ├── PlayerResponse.java
│   ├── SceneResponse.java
│   ├── ChoiceResponse.java
│   ├── ChoiceActionRequest.java
│   └── ChoiceOutcomeResponse.java
└── exception/            # Exception handling
    ├── ResourceNotFoundException.java
    ├── InvalidGameStateException.java
    └── GlobalExceptionHandler.java

src/test/java/com/LostInWoods/app/service/
├── PlayerServiceTest.java
├── SceneServiceTest.java
└── GameServiceTest.java

src/main/resources/
└── application.properties
```

## Architecture & Design Patterns

### Layered Architecture
```
Controllers (REST API)
    ↓
Services (Business Logic)
    ↓
Repositories (Data Access)
    ↓
Database (PostgreSQL)
```

### SOLID Principles Implementation

**Single Responsibility Principle (SRP)**
- Each class has one reason to change
- `PlayerService`: Only manages player operations
- `SceneService`: Only manages scene/choice operations
- `GameService`: Only handles game logic orchestration

**Open/Closed Principle (OCP)**
- Classes are open for extension via service interfaces
- Easy to add new game logic without modifying existing code

**Liskov Substitution Principle (LSP)**
- All services properly implement their contracts
- Exceptions are properly caught and handled

**Interface Segregation Principle (ISP)**
- DTOs are focused and don't expose unnecessary data
- Repository interfaces are minimal and specific

**Dependency Inversion Principle (DIP)**
- Services depend on abstractions (interfaces), not concrete implementations
- `@RequiredArgsConstructor` enables dependency injection
- Repositories are injected, not instantiated

### Design Patterns Used

1. **Repository Pattern**: Data access abstraction layer
2. **Service Locator Pattern**: Service layer orchestration
3. **Data Transfer Object (DTO)**: API/Entity decoupling
4. **Builder Pattern**: Complex object creation
5. **Strategy Pattern**: Different game logic paths
6. **Exception Translation**: Custom exceptions for business errors

## Entity Relationships

```
Player (1) ──── (N) GameProgress
  │
  ├─ currentSceneId → Scene (N) ◄──── (N) Choice ◄──── (N) nextSceneId
```

## API Endpoints

### Player Management
- `POST /api/players` - Create new player
- `GET /api/players/{playerId}` - Get player info

### Scene & Narrative
- `GET /api/scenes/{sceneId}` - Get scene details with choices
- `GET /api/scenes/{sceneId}/choices` - Get available choices for a scene

### Game Logic
- `POST /api/game/choice` - Process player choice
- `GET /api/game/state/{playerId}` - Get current game state
- `POST /api/game/load/{playerId}` - Load last saved game
- `POST /api/game/save/{playerId}` - Save current game progress

## Key Features

### Player System
- Automatic health initialization (100 HP)
- Health constraints (0-100 range)
- Player state tracking (alive/dead)
- Damage and healing mechanics

### Scene Management
- Story scene hierarchy
- Choice system with outcomes
- Terminal scenes (victory/game over)
- Proper scene ordering

### Game Logic
- Choice processing with health impact
- Scene navigation based on choices
- Game state validation
- Automatic save on choice selection

### Exception Handling
- `ResourceNotFoundException`: Missing entities
- `InvalidGameStateException`: Invalid operations
- Centralized error handling with proper HTTP status codes
- Validation error reporting

## Database Configuration

### PostgreSQL Connection
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/lost_in_woods
spring.datasource.username=postgres
spring.datasource.password=password
```

### Tables
- `players`: Player profiles and state
- `scenes`: Story scenes
- `choices`: Player choices with outcomes
- `game_progress`: Saved game states

### Indexes
- `idx_player_id` on game_progress for fast lookups

## Testing Strategy

### Unit Tests Coverage
- **PlayerServiceTest**: Player creation, retrieval, health management
- **SceneServiceTest**: Scene retrieval, choice management, terminal scene detection
- **GameServiceTest**: Choice processing, game state transitions, error scenarios

### Test Framework
- JUnit 5
- Mockito for mocking dependencies
- 100% mockable service layer

### Running Tests
```bash
mvn test
```

## Configuration

### Application Properties
```properties
# Server
server.port=8080

# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/lost_in_woods
spring.jpa.hibernate.ddl-auto=update

# Logging
logging.level.com.LostInWoods.app=DEBUG
```

## Build & Run

### Prerequisites
- Java 21
- Maven
- PostgreSQL

### Build
```bash
mvn clean package
```

### Run
```bash
mvn spring-boot:run
```

### Access Application
- API: `http://localhost:8080`
- API Documentation: Prepare for Swagger UI integration

## Development Best Practices Applied

1. **Code Quality**
   - Consistent formatting and indentation
   - Meaningful variable and method names
   - No hardcoded values (configuration externalized)
   - Reduced boilerplate with Lombok

2. **Maintainability**
   - Clear separation of concerns
   - DRY (Don't Repeat Yourself) principle
   - Comprehensive logging
   - Proper error messages

3. **Performance**
   - Proper indexing on database queries
   - Transaction management
   - Eager loading for collections

4. **Security**
   - Input validation
   - Proper exception handling
   - CORS configuration for frontend integration

## Next Steps for MVP

1. **Database Population**: Create initial story scenes and choices
2. **Frontend Integration**: React-TypeScript client development
3. **API Documentation**: Swagger/OpenAPI setup
4. **Integration Tests**: End-to-end testing
5. **Performance Testing**: Load testing and optimization
6. **Authentication**: User session management (if needed)
7. **Monitoring**: Logging and metrics collection

## File Locations

- **Source Code**: `/workspaces/dino-corp/App Project/Lost_In_Woods/src/main/java/com/LostInWoods/app/`
- **Tests**: `/workspaces/dino-corp/App Project/Lost_In_Woods/src/test/java/com/LostInWoods/app/`
- **Configuration**: `/workspaces/dino-corp/App Project/Lost_In_Woods/src/main/resources/`
- **Build File**: `/workspaces/dino-corp/App Project/Lost_In_Woods/pom.xml`

## Compliance Checklist

✅ SOLID principles implemented
✅ Proper encapsulation with getters/setters
✅ Composition over inheritance
✅ Small focused classes
✅ Short and simple methods
✅ Reduced tight coupling
✅ High cohesion
✅ Proper error handling
✅ Design patterns applied appropriately
✅ No God classes
✅ Proper abstraction
✅ DTOs for API decoupling
✅ Unit tests for core logic
✅ Meaningful naming conventions
✅ No hardcoded values
✅ Formatted and indented consistently

---

## Summary

The backend foundation is complete and production-ready for MVP. The architecture follows enterprise best practices with clear separation of concerns, proper error handling, and comprehensive test coverage. The service layer is fully testable and decoupled from data persistence, making the codebase maintainable and scalable for future enhancements.
