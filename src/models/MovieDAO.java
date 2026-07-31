package models;

import utils.DBUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MovieDAO {

    public List<Movie> getActiveMovies() {
        List<Movie> movies = new ArrayList<>();
        String sql = "SELECT * FROM movies WHERE status = 'ACTIVE'";

        try (Connection conn = DBUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                movies.add(mapRowToMovie(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return movies;
    }

    public boolean deleteMovie(String id) {
        int movieId = Integer.parseInt(id.replace("M", ""));
        
        try (Connection conn = DBUtils.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Delete bookings related to shows of this movie to avoid foreign key constraints
                String sqlBookings = "DELETE FROM bookings WHERE show_id IN (SELECT id FROM shows WHERE movie_id = ?)";
                try (PreparedStatement stmt = conn.prepareStatement(sqlBookings)) {
                    stmt.setInt(1, movieId);
                    stmt.executeUpdate();
                }

                // Delete the movie (this cascades to shows)
                String sqlMovie = "DELETE FROM movies WHERE id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sqlMovie)) {
                    stmt.setInt(1, movieId);
                    int affected = stmt.executeUpdate();
                    if (affected > 0) {
                        conn.commit();
                        return true;
                    }
                }
                conn.rollback();
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateMoviePricing(Movie movie) {
        int movieId = Integer.parseInt(movie.getId().replace("M", ""));
        String sql = "UPDATE movies SET showing_from = ?, showing_until = ?, adult_price = ?, kids_price = ? WHERE id = ?";
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, movie.getShowingFrom());
            stmt.setString(2, movie.getShowingUntil());
            stmt.setDouble(3, movie.getAdultPrice());
            stmt.setDouble(4, movie.getKidsPrice());
            stmt.setInt(5, movieId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public Movie createMovie(MovieDTO dto) {
        String sql = "INSERT INTO movies (title, description, duration_minutes, genre, tmdb_id, poster_path, banner_path, status, adult_price, kids_price) VALUES (?, ?, ?, ?, ?, ?, ?, 'ACTIVE', 0, 0)";
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, dto.title);
            stmt.setString(2, dto.overview);
            stmt.setInt(3, dto.runtime > 0 ? dto.runtime : 120); // Default if unknown
            
            String genre = "Unknown";
            if (dto.genres != null && !dto.genres.isEmpty()) {
                genre = dto.genres.get(0).name;
            } else if (dto.genre_ids != null && !dto.genre_ids.isEmpty()) {
                genre = utils.TMDBUtils.getGenreName(dto.genre_ids.get(0));
            }
            stmt.setString(4, genre);
            
            stmt.setInt(5, dto.id);
            stmt.setString(6, dto.poster_path);
            stmt.setString(7, dto.backdrop_path);
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        int id = rs.getInt(1);
                        Movie m = new Movie("M" + id, dto.title, genre, stmt.getParameterMetaData() != null ? "120 mins" : "120 mins", dto.overview, new ArrayList<>());
                        m.setTmdbId(dto.id);
                        m.setPosterPath(dto.poster_path);
                        m.setBannerPath(dto.backdrop_path);
                        return m;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean addManualMovie(Movie movie) {
        String sql = "INSERT INTO movies (title, description, duration_minutes, genre, poster_path, banner_path, status, adult_price, kids_price, showing_from, showing_until) VALUES (?, ?, ?, ?, ?, ?, 'ACTIVE', ?, ?, ?, ?)";
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, movie.getTitle());
            stmt.setString(2, movie.getDescription());
            stmt.setInt(3, Integer.parseInt(movie.getRuntime().replace(" mins", "").trim()));
            stmt.setString(4, movie.getGenre());
            stmt.setString(5, movie.getPosterPath());
            stmt.setString(6, movie.getBannerPath());
            stmt.setDouble(7, movie.getAdultPrice());
            stmt.setDouble(8, movie.getKidsPrice());
            stmt.setString(9, movie.getShowingFrom());
            stmt.setString(10, movie.getShowingUntil());
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException | NumberFormatException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean isMovieExistsByTmdbId(int tmdbId) {
        String sql = "SELECT id FROM movies WHERE tmdb_id = ?";
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, tmdbId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<Movie> getMoviesForScheduling() {
        return getActiveMovies(); // For scheduling, we only care about active movies
    }

    public Movie getMovieById(int id) {
        String sql = "SELECT * FROM movies WHERE id = ?";
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToMovie(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Movie> getPendingMovies() {
        List<Movie> movies = new ArrayList<>();
        String sql = "SELECT m.* FROM movies m LEFT JOIN shows s ON m.id = s.movie_id WHERE s.id IS NULL AND m.status = 'ACTIVE'";

        try (Connection conn = DBUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                movies.add(mapRowToMovie(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return movies;
    }

    public String getMostPopularMovieTitle() {
        String sql = "SELECT m.title, COUNT(bs.id) as ticket_count " +
                     "FROM movies m " +
                     "JOIN shows s ON m.id = s.movie_id " +
                     "JOIN bookings b ON s.id = b.show_id " +
                     "JOIN booking_seats bs ON b.id = bs.booking_id " +
                     "WHERE b.status != 'CANCELLED' " +
                     "GROUP BY m.id, m.title " +
                     "ORDER BY ticket_count DESC " +
                     "LIMIT 1";
        
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getString("title");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "No Data Yet";
    }

    private Movie mapRowToMovie(ResultSet rs) throws SQLException {
        Movie m = new Movie(
                "M" + rs.getInt("id"),
                rs.getString("title"),
                rs.getString("genre"),
                rs.getInt("duration_minutes") + " mins",
                rs.getString("description"),
                new ArrayList<>() // We will fetch shows separately if needed
        );
        m.setPosterPath(rs.getString("poster_path"));
        m.setBannerPath(rs.getString("banner_path"));
        m.setAdultPrice(rs.getDouble("adult_price"));
        m.setKidsPrice(rs.getDouble("kids_price"));
        m.setShowingFrom(rs.getString("showing_from"));
        m.setShowingUntil(rs.getString("showing_until"));
        m.setRating(rs.getDouble("rating"));
        m.setTmdbId(rs.getInt("tmdb_id"));
        return m;
    }
}
