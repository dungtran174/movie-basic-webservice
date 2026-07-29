package com.example.movieservice.handler;

import com.example.movieservice.model.Movie;
import com.example.movieservice.repository.MovieRepository;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handler xử lý các HTTP request liên quan đến Movie.
 *
 * Endpoints:
 *   GET /movies         → Trả về danh sách tất cả phim (JSON array)
 *   GET /movie?url=...  → Trả về 1 phim theo source URL (JSON object)
 *   GET /movie?id=...   → Trả về 1 phim theo ID (JSON object)
 */
public class MovieHandler implements HttpHandler {
    private static final Logger logger = LoggerFactory.getLogger(MovieHandler.class);
    private final MovieRepository repository;

    public MovieHandler(MovieRepository repository) {
        this.repository = repository;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        String query = exchange.getRequestURI().getQuery();

        logger.info("Request: {} {} {}", method, path, query != null ? "?" + query : "");

        // Chỉ cho phép phương thức GET
        if (!"GET".equalsIgnoreCase(method)) {
            sendResponse(exchange, 405, errorJson("Method Not Allowed. Only GET is supported."));
            return;
        }

        try {
            if ("/movies".equals(path)) {
                handleGetAllMovies(exchange);
            } else if ("/movie".equals(path)) {
                handleGetSingleMovie(exchange, query);
            } else {
                sendResponse(exchange, 404, errorJson("Not Found. Use /movies or /movie?url=... or /movie?id=..."));
            }
        } catch (Exception e) {
            logger.error("Lỗi xử lý request: ", e);
            sendResponse(exchange, 500, errorJson("Internal Server Error: " + e.getMessage()));
        }
    }

    /**
     * GET /movies → Trả về tất cả phim dưới dạng JSON Array.
     * Mỗi phim được duyệt qua, tạo cơ hội để đặt breakpoint điều kiện
     * trên tên diễn viên (yêu cầu debug của đề bài).
     */
    private void handleGetAllMovies(HttpExchange exchange) throws IOException {
        List<Movie> movies = repository.findAll();
        JSONArray jsonArray = new JSONArray();

        for (Movie movie : movies) {
            // Duyệt qua từng diễn viên của mỗi phim
            // → Đây là vị trí lý tưởng để đặt Conditional Breakpoint
            //    với điều kiện: actorName.startsWith("A")
            List<String> actors = movie.getActors();
            if (actors != null) {
                for (String actorName : actors) {
                    // Đặt Conditional Breakpoint ở dòng dưới đây trong IntelliJ:
                    //   Condition: actorName.startsWith("A")
                    logger.debug("Processing actor: {} (Movie: {})", actorName, movie.getTitle());
                }
            }

            jsonArray.put(movieToJson(movie));
        }

        JSONObject result = new JSONObject();
        result.put("totalMovies", movies.size());
        result.put("movies", jsonArray);

        sendResponse(exchange, 200, result.toString(4)); // indent 4 spaces cho đẹp
    }

    /**
     * GET /movie?url=... hoặc GET /movie?id=...
     * Trả về 1 phim dưới dạng JSON Object.
     */
    private void handleGetSingleMovie(HttpExchange exchange, String query) throws IOException {
        if (query == null || query.isEmpty()) {
            sendResponse(exchange, 400, errorJson("Missing parameter. Use ?url=<source_url> or ?id=<movie_id>"));
            return;
        }

        Map<String, String> params = parseQueryParams(query);
        Movie movie = null;

        if (params.containsKey("url")) {
            String url = URLDecoder.decode(params.get("url"), StandardCharsets.UTF_8.name());
            movie = repository.findBySourceUrl(url);
        } else if (params.containsKey("id")) {
            try {
                int id = Integer.parseInt(params.get("id"));
                movie = repository.findById(id);
            } catch (NumberFormatException e) {
                sendResponse(exchange, 400, errorJson("Invalid id. Must be an integer."));
                return;
            }
        } else {
            sendResponse(exchange, 400, errorJson("Unknown parameter. Use ?url=<source_url> or ?id=<movie_id>"));
            return;
        }

        if (movie == null) {
            sendResponse(exchange, 404, errorJson("Movie not found."));
            return;
        }

        // Duyệt qua diễn viên của phim tìm được (cho mục đích debug)
        List<String> actors = movie.getActors();
        if (actors != null) {
            for (String actorName : actors) {
                // Conditional Breakpoint: actorName.startsWith("A")
                logger.debug("Actor in result: {}", actorName);
            }
        }

        sendResponse(exchange, 200, movieToJson(movie).toString(4));
    }

    /**
     * Chuyển đổi Movie object thành JSONObject.
     * Genres, Directors, Actors được trả về dạng JSON Array (không phải chuỗi CSV).
     */
    private JSONObject movieToJson(Movie movie) {
        JSONObject json = new JSONObject();
        json.put("id", movie.getId());
        json.put("title", movie.getTitle());
        json.put("releaseYear", movie.getReleaseYear() != null ? movie.getReleaseYear() : JSONObject.NULL);
        json.put("country", movie.getCountry() != null ? movie.getCountry() : JSONObject.NULL);
        json.put("genres", new JSONArray(movie.getGenres() != null ? movie.getGenres() : List.of()));
        json.put("directors", new JSONArray(movie.getDirectors() != null ? movie.getDirectors() : List.of()));
        json.put("actors", new JSONArray(movie.getActors() != null ? movie.getActors() : List.of()));
        json.put("sourceUrl", movie.getSourceUrl() != null ? movie.getSourceUrl() : JSONObject.NULL);
        return json;
    }

    /**
     * Parse query string "key1=value1&key2=value2" thành Map.
     */
    private Map<String, String> parseQueryParams(String query) {
        Map<String, String> params = new HashMap<>();
        for (String param : query.split("&")) {
            String[] pair = param.split("=", 2);
            if (pair.length == 2) {
                params.put(pair[0], pair[1]);
            }
        }
        return params;
    }

    /**
     * Tạo JSON lỗi.
     */
    private String errorJson(String message) {
        JSONObject error = new JSONObject();
        error.put("error", message);
        return error.toString(4);
    }

    /**
     * Gửi HTTP response với status code và body.
     */
    private void sendResponse(HttpExchange exchange, int statusCode, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}
