# MagnetPlay Backend

MagnetPlay Backend is a robust Spring Boot application designed to manage movies, users, and torrent data. It provides secure RESTful APIs for movie management, user authentication, and integration with external torrent sources.

## Technologies Used

- Java 17+
- Spring Boot
- Maven
- Spring Security (JWT authentication)
- JPA/Hibernate
- JUnit (Testing)

## Project Structure

```
src/
  main/
    java/
      org/sebas/magnetplay/
        MagnetPlayApplication.java
        config/           # Security and web configuration
        controller/       # REST controllers for movies, users, favorites
        dto/              # Data Transfer Objects
        exceptions/       # Custom exception classes and global handler
        init/             # Data initialization
        mapper/           # DTO <-> Model mappers
        model/            # Entity models (Movie, User, Role, etc.)
        repo/             # Spring Data repositories
        service/          # Business logic and integration services
    resources/
      application.properties  # Application configuration
  test/
    java/
      org/sebas/magnetplay/
        MagnetPlayApplicationTests.java
        controllerTest/   # Controller unit tests
        integrationTests/ # Integration tests (auth, etc.)
        service/          # Service layer tests
```

## Main Features

- **Movie Management:** CRUD operations for movies, including category-based organization.
- **User Management:** Registration, authentication, and user role management.
- **JWT Authentication:** Secure endpoints using JWT tokens.
- **Favorite Movies:** Users can manage their favorite movies.
- **Torrent Integration:** Fetches recent and trending movies from external torrent APIs, parses and deduplicates results, and stores them in the database.
- **Exception Handling:** Custom exceptions and global error handler for robust API responses.
- **Data Mapping:** DTOs and mappers for clean separation between API and persistence layers.
- **Testing:** Unit and integration tests for controllers and services.
- **Swagger/OpenAPI Documentation:** Interactive API docs available via Swagger UI (`/swagger-ui.html`).

## Getting Started

1. **Clone the repository**
2. **Configure database and properties** in `src/main/resources/application.properties`
3. **Build the project**
   ```
   mvn clean install
   ```
4. **Run the application**
   ```
   mvn spring-boot:run
   ```
## API Endpoints

Below are the most important endpoints, grouped by category for clarity:

### Movie Endpoints
| Method | Endpoint                | Description                       | Auth Required |
|--------|-------------------------|-----------------------------------|--------------|
| GET    | /api/movies             | List all movies                   | No           |
| GET    | /api/movies/{id}        | Get movie details by ID           | No           |
| POST   | /api/movies             | Create a new movie                | Yes (Admin)  |
| PUT    | /api/movies/{id}        | Update an existing movie          | Yes (Admin)  |
| DELETE | /api/movies/{id}        | Delete a movie by ID              | Yes (Admin)  |

### User Endpoints
| Method | Endpoint                | Description                       | Auth Required |
|--------|-------------------------|-----------------------------------|--------------|
| POST   | /api/users              | Register a new user               | No           |
| GET    | /api/users/{id}         | Get user details by ID            | Yes          |

### Authentication Endpoints
| Method | Endpoint                | Description                       | Auth Required |
|--------|-------------------------|-----------------------------------|--------------|
| POST   | /api/auth/login         | Authenticate and get JWT token    | No           |
| POST   | /api/auth/refresh       | Refresh JWT token                 | No           |

### Favorite Movies Endpoints
| Method | Endpoint                        | Description                       | Auth Required |
|--------|----------------------------------|-----------------------------------|--------------|
| GET    | /api/favorites                   | List user's favorite movies        | Yes          |
| POST   | /api/favorites/{movieId}         | Add movie to favorites             | Yes          |
| DELETE | /api/favorites/{movieId}         | Remove movie from favorites        | Yes          |

### Documentation Endpoint
| Method | Endpoint             | Description                       | Auth Required |
|--------|----------------------|-----------------------------------|--------------|
| GET    | /swagger-ui.html     | Swagger UI interactive API docs   | No           |

### Error Handling
All endpoints return structured error responses using `ErrorResponseDto` for validation errors, authentication failures, and resource not found exceptions.

## Testing

Run all tests with:
```
mvn test
```

## License

This project is licensed under the MIT License.
