package models;

import com.google.gson.annotations.SerializedName;

/**
 * Maps a single movie result from the TMDB API JSON response.
 * Uses Gson @SerializedName to handle snake_case → camelCase mapping.
 */
public class TMDBMovie {

    private int id;
    private String title;
    private String overview;

    @SerializedName("poster_path")
    private String posterPath;

    @SerializedName("vote_average")
    private double voteAverage;

    @SerializedName("release_date")
    private String releaseDate;

    @SerializedName("genre_ids")
    private int[] genreIds;

    // --- Getters ---

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title != null ? title : "";
    }

    public String getOverview() {
        return overview != null ? overview : "";
    }

    public String getPosterPath() {
        return posterPath;
    }

    public double getVoteAverage() {
        return voteAverage;
    }

    public String getReleaseDate() {
        return releaseDate != null ? releaseDate : "";
    }

    public int[] getGenreIds() {
        return genreIds != null ? genreIds : new int[0];
    }

    /**
     * Builds the full TMDB poster image URL.
     * Returns null if no poster path is available.
     */
    public String getFullPosterUrl() {
        if (posterPath == null || posterPath.isEmpty()) {
            return null;
        }
        return "https://image.tmdb.org/t/p/w500" + posterPath;
    }

    /**
     * Returns a smaller thumbnail URL for table display.
     */
    public String getThumbnailUrl() {
        if (posterPath == null || posterPath.isEmpty()) {
            return null;
        }
        return "https://image.tmdb.org/t/p/w92" + posterPath;
    }
}
