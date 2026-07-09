package models;

import java.sql.*;

public class Movie {

    private int id;
    private String title;
    private String description;
    private String posterPath;
    private double rating;
    private String releaseDate;
    private int durationMinutes;
    private String genre;
    private double adultPrice;
    private double kidsPrice;
    private int tmdbId;
    private String status;

    // Default constructor
    public Movie() {
        this.status = "ACTIVE";
        this.durationMinutes = 120;
    }

    // Full constructor
    public Movie(String title, String description, String posterPath, double rating,
                 String releaseDate, int durationMinutes, String genre,
                 double adultPrice, double kidsPrice, int tmdbId) {
        this.title = title;
        this.description = description;
        this.posterPath = posterPath;
        this.rating = rating;
        this.releaseDate = releaseDate;
        this.durationMinutes = durationMinutes;
        this.genre = genre;
        this.adultPrice = adultPrice;
        this.kidsPrice = kidsPrice;
        this.tmdbId = tmdbId;
        this.status = "ACTIVE";
    }

    // --- Getters ---
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getPosterPath() { return posterPath; }
    public double getRating() { return rating; }
    public String getReleaseDate() { return releaseDate; }
    public int getDurationMinutes() { return durationMinutes; }
    public String getGenre() { return genre; }
    public double getAdultPrice() { return adultPrice; }
    public double getKidsPrice() { return kidsPrice; }
    public int getTmdbId() { return tmdbId; }
    public String getStatus() { return status; }

    // --- Setters ---
    public void setId(int id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setPosterPath(String posterPath) { this.posterPath = posterPath; }
    public void setRating(double rating) { this.rating = rating; }
    public void setReleaseDate(String releaseDate) { this.releaseDate = releaseDate; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }
    public void setGenre(String genre) { this.genre = genre; }
    public void setAdultPrice(double adultPrice) { this.adultPrice = adultPrice; }
    public void setKidsPrice(double kidsPrice) { this.kidsPrice = kidsPrice; }
    public void setTmdbId(int tmdbId) { this.tmdbId = tmdbId; }
    public void setStatus(String status) { this.status = status; }

    // ==========================================
    // Database Operations
    // ==========================================

    /**
     * Returns the count of movies with ACTIVE status.
     */
    public static int getActiveCount() {
        String sql = "SELECT COUNT(*) FROM movies WHERE status = 'ACTIVE'";
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int count = rs.getInt(1);
                rs.close();
                stmt.close();
                return count;
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Inserts a new movie into the database.
     *
     * @param movie The Movie object to insert.
     * @return true if insertion was successful, false otherwise.
     */
    public static boolean insert(Movie movie) {
        String sql = "INSERT INTO movies (title, description, poster_path, rating, release_date, "
                   + "duration_minutes, genre, adult_price, kids_price, tmdb_id, status) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);

            stmt.setString(1, movie.getTitle());
            stmt.setString(2, movie.getDescription());
            stmt.setString(3, movie.getPosterPath());
            stmt.setDouble(4, movie.getRating());

            // Handle release_date — could be empty string
            if (movie.getReleaseDate() != null && !movie.getReleaseDate().isEmpty()) {
                stmt.setDate(5, Date.valueOf(movie.getReleaseDate()));
            } else {
                stmt.setNull(5, Types.DATE);
            }

            stmt.setInt(6, movie.getDurationMinutes());
            stmt.setString(7, movie.getGenre());
            stmt.setDouble(8, movie.getAdultPrice());
            stmt.setDouble(9, movie.getKidsPrice());

            // Handle tmdb_id — 0 means no TMDB association
            if (movie.getTmdbId() > 0) {
                stmt.setInt(10, movie.getTmdbId());
            } else {
                stmt.setNull(10, Types.INTEGER);
            }

            stmt.setString(11, movie.getStatus());

            int rows = stmt.executeUpdate();
            stmt.close();
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("Error inserting movie: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Checks if a movie with the given TMDB ID already exists in the database.
     *
     * @param tmdbId The TMDB movie ID to check.
     * @return true if a movie with this TMDB ID exists, false otherwise.
     */
    public static boolean existsByTmdbId(int tmdbId) {
        String sql = "SELECT COUNT(*) FROM movies WHERE tmdb_id = ?";
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, tmdbId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                boolean exists = rs.getInt(1) > 0;
                rs.close();
                stmt.close();
                return exists;
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
