# Movie Web Service

A simple RESTful web service that returns movie data as formatted JSON.

## Prerequisites

- **Java 11+**
- **Maven 3.6+**
- **`movies.db`** file (contains crawled movie data)

## Quick Start

```bash
# 1. Copy database from crawler project
cp ../crawl-data/movies.db .

# 2. Build
mvn clean package

# 3. Run
java -jar target/movie-service-1.0-SNAPSHOT-jar-with-dependencies.jar

# 4. Open browser
# http://localhost:8080/movies
```

## API Endpoints

| Method | Endpoint | Description | Example |
|--------|----------|-------------|---------|
| GET | `/` | Service info & usage | `http://localhost:8080/` |
| GET | `/movies` | All movies (JSON array) | `http://localhost:8080/movies` |
| GET | `/movie?id=N` | Single movie by ID | `http://localhost:8080/movie?id=1` |
| GET | `/movie?url=URL` | Single movie by source URL | `http://localhost:8080/movie?url=https://toivote.com/movie/...` |

## JSON Response Example

```json
{
    "id": 1,
    "title": "Phép Thuật",
    "releaseYear": "2007",
    "country": "Hoa Kỳ",
    "genres": [
        "Nhạc kịch",
        "Viễn tưởng",
        "Hài hước"
    ],
    "directors": [
        "Kevin Lima"
    ],
    "actors": [
        "Amy Adams",
        "Patrick Dempsey"
    ],
    "sourceUrl": "https://toivote.com/movie/..."
}
```

## Debug with Conditional Breakpoint

See `docs/HUONG_DAN.md` for detailed instructions on setting up conditional breakpoints to pause on actors whose names start with "A".

## Project Structure

```
basic-webservice/
 pom.xml
 .env.example
 movies.db                          (copied from crawler project)
 docs/
    HUONG_DAN.md
 src/main/java/com/example/movieservice/
     Main.java                      # Entry point + HTTP server setup
     handler/
        MovieHandler.java          # Request handling + JSON formatting
     model/
        Movie.java                 # Data model
     repository/
         MovieRepository.java       # SQLite read operations
```
