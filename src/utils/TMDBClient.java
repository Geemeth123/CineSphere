package utils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import models.TMDBMovie;

/**
 * Handles all communication with the TMDB (The Movie Database) API.
 * Reads the API key from a .env file in the project root.
 */
public class TMDBClient {

    private static final String BASE_URL = "https://api.themoviedb.org/3";
    private static final String API_KEY;
    private static final Gson gson = new Gson();
    private static final HttpClient httpClient = HttpClient.newHttpClient();

    // Genre ID → Name lookup map (TMDB standard genre IDs)
    private static final Map<Integer, String> GENRE_MAP = new HashMap<>();

    static {
        // Load API key from .env
        API_KEY = loadApiKey();

        // Populate genre map
        GENRE_MAP.put(28, "Action");
        GENRE_MAP.put(12, "Adventure");
        GENRE_MAP.put(16, "Animation");
        GENRE_MAP.put(35, "Comedy");
        GENRE_MAP.put(80, "Crime");
        GENRE_MAP.put(99, "Documentary");
        GENRE_MAP.put(18, "Drama");
        GENRE_MAP.put(10751, "Family");
        GENRE_MAP.put(14, "Fantasy");
        GENRE_MAP.put(36, "History");
        GENRE_MAP.put(27, "Horror");
        GENRE_MAP.put(10402, "Music");
        GENRE_MAP.put(9648, "Mystery");
        GENRE_MAP.put(10749, "Romance");
        GENRE_MAP.put(878, "Sci-Fi");
        GENRE_MAP.put(10770, "TV Movie");
        GENRE_MAP.put(53, "Thriller");
        GENRE_MAP.put(10752, "War");
        GENRE_MAP.put(37, "Western");
    }

    private TMDBClient() {
    }

    /**
     * Reads the TMDB_API_KEY from the .env file in the project root.
     */
    private static String loadApiKey() {
        try (BufferedReader reader = new BufferedReader(new FileReader(".env"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("TMDB_API_KEY=")) {
                    return line.substring("TMDB_API_KEY=".length()).trim();
                }
            }
        } catch (IOException e) {
            System.err.println("Warning: Could not read .env file. TMDB features will not work.");
            System.err.println("Create a .env file in the project root with: TMDB_API_KEY=your_key_here");
        }
        return "";
    }

    /**
     * Searches TMDB for movies matching the given query string.
     *
     * @param query The search term.
     * @return A list of TMDBMovie results, or an empty list on error.
     */
    public static List<TMDBMovie> searchMovies(String query) {
        List<TMDBMovie> results = new ArrayList<>();

        if (API_KEY.isEmpty()) {
            System.err.println("TMDB API key is not configured. Cannot search.");
            return results;
        }

        try {
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
            String url = BASE_URL + "/search/movie?api_key=" + API_KEY
                    + "&query=" + encodedQuery
                    + "&language=en-US&page=1&include_adult=false";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
                JsonArray resultsArray = json.getAsJsonArray("results");

                for (int i = 0; i < resultsArray.size(); i++) {
                    TMDBMovie movie = gson.fromJson(resultsArray.get(i), TMDBMovie.class);
                    results.add(movie);
                }
            } else {
                System.err.println("TMDB API returned status: " + response.statusCode());
                System.err.println("Response: " + response.body());
            }
        } catch (Exception e) {
            System.err.println("Error searching TMDB: " + e.getMessage());
            e.printStackTrace();
        }

        return results;
    }

    /**
     * Converts an array of TMDB genre IDs to a comma-separated string of genre names.
     *
     * @param genreIds Array of TMDB genre IDs.
     * @return Comma-separated genre names (e.g., "Action, Sci-Fi").
     */
    public static String getGenreNames(int[] genreIds) {
        if (genreIds == null || genreIds.length == 0) {
            return "Unknown";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < genreIds.length; i++) {
            String name = GENRE_MAP.getOrDefault(genreIds[i], "Other");
            sb.append(name);
            if (i < genreIds.length - 1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

    /**
     * Checks if the TMDB API key is configured and available.
     */
    public static boolean isApiKeyConfigured() {
        return !API_KEY.isEmpty();
    }
}
