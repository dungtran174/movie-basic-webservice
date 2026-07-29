package com.example.movieservice;

import com.example.movieservice.handler.MovieHandler;
import com.example.movieservice.repository.MovieRepository;
import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * Movie Web Service
 *
 * Webservice đơn giản trả lại nội dung phim đã crawl ở.
 * Kết quả trả về dưới dạng JSON đã được format đẹp.
 *
 * Sử dụng HttpServer có sẵn trong JDK (không cần thư viện HTTP bên ngoài).
 *
 * Endpoints:
 *   GET /movies         → Danh sách tất cả phim
 *   GET /movie?url=...  → Tìm phim theo source URL (từ toivote.com)
 *   GET /movie?id=...   → Tìm phim theo ID trong database
 */
public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    // Cấu hình qua biến môi trường hoặc dùng giá trị mặc định
    private static final int PORT = getEnvInt("SERVER_PORT", 8080);
    private static final String DB_PATH = getEnv("DB_PATH", "movies.db");

    public static void main(String[] args) {
        logger.info("=== KHỞI ĐỘNG MOVIE WEB SERVICE ===");
        logger.info("Database: {}", DB_PATH);
        logger.info("Port: {}", PORT);

        // 1. Khởi tạo repository đọc dữ liệu từ SQLite
        MovieRepository repository = new MovieRepository(DB_PATH);
        int totalMovies = repository.countMovies();
        if (totalMovies < 0) {
            logger.error("Không thể kết nối database. Hãy đảm bảo file {} tồn tại.", DB_PATH);
            logger.error("Copy file movies.db từ cp ../crawl-data/movies.db .");
            return;
        }
        logger.info("Database có {} phim.", totalMovies);

        // 2. Tạo HTTP Server
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

            // Đăng ký handler cho các endpoints
            MovieHandler handler = new MovieHandler(repository);
            server.createContext("/movies", handler);
            server.createContext("/movie", handler);

            // Root endpoint: hướng dẫn sử dụng
            server.createContext("/", exchange -> {
                String usage = "{\n" +
                        "    \"service\": \"Movie Web Service\",\n" +
                        "    \"endpoints\": {\n" +
                        "        \"GET /movies\": \"Lấy danh sách tất cả phim\",\n" +
                        "        \"GET /movie?id=1\": \"Lấy phim theo ID\",\n" +
                        "        \"GET /movie?url=<source_url>\": \"Lấy phim theo URL đã crawl\"\n" +
                        "    },\n" +
                        "    \"totalMoviesInDB\": " + repository.countMovies() + "\n" +
                        "}";
                byte[] bytes = usage.getBytes("UTF-8");
                exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
                exchange.sendResponseHeaders(200, bytes.length);
                exchange.getResponseBody().write(bytes);
                exchange.getResponseBody().close();
            });

            server.setExecutor(null); // Dùng default executor
            server.start();

            logger.info("=== SERVER ĐANG CHẠY TẠI http://localhost:{} ===", PORT);
            logger.info("Thử truy cập:");
            logger.info("  http://localhost:{}/movies        → Danh sách tất cả phim", PORT);
            logger.info("  http://localhost:{}/movie?id=1     → Phim có ID = 1", PORT);
            logger.info("Nhấn Ctrl+C để dừng server.");

        } catch (IOException e) {
            logger.error("Lỗi khi khởi động server: ", e);
        }
    }

    private static String getEnv(String key, String defaultValue) {
        String value = System.getenv(key);
        return (value != null && !value.isEmpty()) ? value : defaultValue;
    }

    private static int getEnvInt(String key, int defaultValue) {
        String value = System.getenv(key);
        if (value != null && !value.isEmpty()) {
            try { return Integer.parseInt(value); } catch (NumberFormatException ignored) {}
        }
        return defaultValue;
    }
}
