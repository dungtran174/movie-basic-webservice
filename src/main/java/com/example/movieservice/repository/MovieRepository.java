package com.example.movieservice.repository;

import com.example.movieservice.model.Movie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Lớp đọc dữ liệu phim từ SQLite Database (đã được crawl ở.
 * Chỉ thực hiện thao tác ĐỌC (SELECT), không ghi/sửa/xóa.
 */
public class MovieRepository {
    private static final Logger logger = LoggerFactory.getLogger(MovieRepository.class);
    private final String dbUrl;

    public MovieRepository(String dbPath) {
        this.dbUrl = "jdbc:sqlite:" + dbPath;
        logger.info("Kết nối database: {}", dbPath);
    }

    /**
     * Lấy tất cả phim từ database.
     */
    public List<Movie> findAll() {
        List<Movie> movies = new ArrayList<>();
        String sql = "SELECT id, title, release_year, country, genres, directors, actors, source_url FROM movies ORDER BY id";

        try (Connection conn = DriverManager.getConnection(dbUrl);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Movie movie = mapResultSetToMovie(rs);
                movies.add(movie);
            }
            logger.info("Đã tải {} phim từ database.", movies.size());

        } catch (SQLException e) {
            logger.error("Lỗi khi truy vấn tất cả phim: ", e);
        }
        return movies;
    }

    /**
     * Tìm phim theo source URL (URL đã crawl từ toivote.com).
     */
    public Movie findBySourceUrl(String sourceUrl) {
        String sql = "SELECT id, title, release_year, country, genres, directors, actors, source_url FROM movies WHERE source_url = ?";

        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, sourceUrl);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToMovie(rs);
            }

        } catch (SQLException e) {
            logger.error("Lỗi khi tìm phim theo URL: ", e);
        }
        return null;
    }

    /**
     * Tìm phim theo ID.
     */
    public Movie findById(int id) {
        String sql = "SELECT id, title, release_year, country, genres, directors, actors, source_url FROM movies WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToMovie(rs);
            }

        } catch (SQLException e) {
            logger.error("Lỗi khi tìm phim theo ID: ", e);
        }
        return null;
    }

    /**
     * Đếm tổng số phim trong database.
     */
    public int countMovies() {
        String sql = "SELECT COUNT(*) FROM movies";
        try (Connection conn = DriverManager.getConnection(dbUrl);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.getInt(1);
        } catch (SQLException e) {
            logger.error("Lỗi khi đếm phim: ", e);
            return -1;
        }
    }

    /**
     * Chuyển đổi một dòng kết quả SQL thành đối tượng Movie.
     * Dữ liệu genres/directors/actors trong DB lưu dạng CSV (vd: "Action, Comedy").
     * Hàm này sẽ tách chúng thành List để trả về JSON dạng mảng.
     */
    private Movie mapResultSetToMovie(ResultSet rs) throws SQLException {
        Movie movie = new Movie();
        movie.setId(rs.getInt("id"));
        movie.setTitle(rs.getString("title"));
        movie.setReleaseYear(rs.getString("release_year"));
        movie.setCountry(rs.getString("country"));
        movie.setGenres(Movie.csvToList(rs.getString("genres")));
        movie.setDirectors(Movie.csvToList(rs.getString("directors")));
        movie.setActors(Movie.csvToList(rs.getString("actors")));
        movie.setSourceUrl(rs.getString("source_url"));
        return movie;
    }
}
