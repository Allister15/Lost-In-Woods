# Backend Foundation Changelog

## Version 0.1.0 - Initial Backend Setup (2026-05-23)

### Added
- **Database Layer**
  - `Player` entity with health system and player profile data
  - `Scene` entity for story narrative management
  - `Choice` entity for game decisions and outcomes
  - `GameProgress` entity for save state management

- **Repository Layer**
  - `PlayerRepository` for player data access
  - `SceneRepository` for scene data access
  - `ChoiceRepository` for choice data access
  - `GameProgressRepository` for game progress management

- **Service Layer** (Business Logic)
  - `PlayerService`: Player creation, retrieval, and health management
  - `SceneService`: Scene and choice retrieval
  - `GameService`: Core game logic orchestration including:
    - Choice processing with health impact
    - Game state transitions
    - Progress saving and loading

- **Controller Layer** (REST API)
  - `PlayerController`: Player creation and retrieval endpoints
  - `SceneController`: Scene and choice retrieval endpoints
  - `GameController`: Game logic endpoints (choice processing, state management, save/load)

- **Data Transfer Objects (DTOs)**
  - `PlayerCreateRequest`: Player registration request
  - `PlayerResponse`: Player data response
  - `SceneResponse`: Scene data with choices
  - `ChoiceResponse`: Individual choice data
  - `ChoiceActionRequest`: Choice action request
  - `ChoiceOutcomeResponse`: Choice outcome response

- **Exception Handling**
  - `ResourceNotFoundException`: For missing resources
  - `InvalidGameStateException`: For invalid game operations
  - `GlobalExceptionHandler`: Centralized exception handling with proper HTTP status codes

- **Configuration**
  - PostgreSQL database configuration
  - JPA/Hibernate configuration
  - Spring Boot properties setup
  - CORS configuration for cross-origin requests

### Architecture Highlights
- **SOLID Principles Applied**
  - Single Responsibility: Each class has one responsibility
  - Dependency Injection: Services use @RequiredArgsConstructor
  - Interface segregation through focused DTOs
  - Abstraction via repository pattern

- **Design Patterns**
  - Repository Pattern: Data access abstraction
  - Service Locator: Service layer orchestration
  - Data Transfer Object (DTO): API decoupling
  - Dependency Injection: Loose coupling

- **Database Design**
  - Normalized schema with proper relationships
  - Indexed queries for performance
  - Automatic timestamp tracking (createdAt, updatedAt)
  - Health constraints (0-100 range)

### API Endpoints (MVP v1)
- `POST /api/players` - Create new player
- `GET /api/players/{playerId}` - Get player info
- `GET /api/scenes/{sceneId}` - Get scene details
- `GET /api/scenes/{sceneId}/choices` - Get available choices
- `POST /api/game/choice` - Make a choice
- `GET /api/game/state/{playerId}` - Get game state
- `POST /api/game/load/{playerId}` - Load saved game
- `POST /api/game/save/{playerId}` - Save game progress

### Testing
- Unit tests structure prepared
- Service layer fully testable with dependency injection
- Repository layer mockable through interfaces

### Technical Stack
- Java 21
- Spring Boot 4.0.6
- Spring Data JPA with Hibernate
- PostgreSQL
- Maven for build management
- Lombok for boilerplate reduction

### Next Steps
1. Populate database with initial story scenes and choices
2. Implement frontend integration
3. Add comprehensive unit tests
4. Add integration tests
5. Implement error recovery mechanisms
6. Add logging and monitoring
