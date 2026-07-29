package com.example.movieservice.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Lớp biểu diễn dữ liệu của một bộ phim.
 * Tương thích với schema SQLite từ.
 */
public class Movie {
    private int id;
    private String title;
    private String releaseYear;
    private String country;
    private List<String> genres;
    private List<String> directors;
    private List<String> actors;
    private String sourceUrl;

    public Movie() {}

    // --- Getters & Setters ---

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getReleaseYear() { return releaseYear; }
    public void setReleaseYear(String releaseYear) { this.releaseYear = releaseYear; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public List<String> getGenres() { return genres; }
    public void setGenres(List<String> genres) { this.genres = genres; }

    public List<String> getDirectors() { return directors; }
    public void setDirectors(List<String> directors) { this.directors = directors; }

    public List<String> getActors() { return actors; }
    public void setActors(List<String> actors) { this.actors = actors; }

    public String getSourceUrl() { return sourceUrl; }
    public void setSourceUrl(String sourceUrl) { this.sourceUrl = sourceUrl; }

    /**
     * Chuyển chuỗi phân cách bằng dấu phẩy thành List.
     * Dùng khi đọc dữ liệu từ SQLite (lưu dạng "Action, Comedy, Drama").
     */
    public static List<String> csvToList(String csv) {
        if (csv == null || csv.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.asList(csv.split("\\s*,\\s*"));
    }

    @Override
    public String toString() {
        return "Movie{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", releaseYear='" + releaseYear + '\'' +
                ", country='" + country + '\'' +
                ", genres=" + genres +
                ", directors=" + directors +
                ", actors=" + actors +
                ", sourceUrl='" + sourceUrl + '\'' +
                '}';
    }
}
