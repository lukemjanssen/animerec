package com.animerec.animerec;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * The main class for the Anime Recommendation application.
 * This class is responsible for running the Python scripts, fetching data from
 * the Jikan API, and processing the data to recommend similar anime to the
 * input anime.
 * The recommended anime are printed to the console.
 */
@SpringBootApplication
public class AnimerecApplication {

    // Service for running Python scripts
    private final PythonService pythonService;

    // HTTP client for making API requests
    private final OkHttpClient client = new OkHttpClient();

    // Object mapper for parsing JSON responses
    private final ObjectMapper mapper = new ObjectMapper();

    private final Semaphore perSecondSemaphore = new Semaphore(3); // replace with per-second limit
    private final Semaphore perMinuteSemaphore = new Semaphore(60); // replace with per-minute limit

    /**
     * Constructor for AnimerecApplication.
     * 
     * @param pythonService Service for running Python scripts
     */
    @Autowired
    public AnimerecApplication(PythonService pythonService) {
        this.pythonService = pythonService;
    }

    /**
     * Main method for running the application.
     * 
     * @param args Command line arguments
     */
    public static void main(String[] args) throws IOException, InterruptedException{
        SpringApplication.run(AnimerecApplication.class, args);
        String url1 = "https://myanimelist.net/anime/10165/Nichijou/stats?m=all#members";
        AnimerecApplication animerecApplication = new AnimerecApplication(new PythonService());
        List<Anime> animeList1 = animerecApplication.getRecList(url1);      
    }

    /**
     * Method for running the Python script and generating anime recommendations.
     * 
     * @throws IOException          If there is an error reading the input or output
     *                              of the Python script
     * @throws InterruptedException If the thread is interrupted while waiting for
     *                              the Python script to finish
     */
    public List<Anime> getRecList(String url) throws IOException, InterruptedException {

        // Final return list.
        List<Anime> animeList = new ArrayList<>();

        // HARDCODED URL FOR TESTING PURPOSES
        // String debUrl = "https://myanimelist.net/anime/10165/Nichijou/stats?m=all#members";

        // Parse title from url:
        String inputTitle = url.substring(30, url.length() - 6);

        // Get user scores for the input anime
        CompletableFuture<List<UserScore>> userScoresFuture = CompletableFuture.supplyAsync(() -> {
            try {
                String output = pythonService.runPythonScript(url, "user_scores");
                return parseUserScores(output);
            } catch (IOException e) {
                System.err.println("Error parsing user scores: " + e.getMessage());
                return new ArrayList<>();
            }
        });

        // Get genres for the input anime
        CompletableFuture<List<String>> inputAnimeGenresFuture = CompletableFuture.supplyAsync(() -> {
            try {
                String output = pythonService.runPythonScript(url, "user_scores");
                return parseGenres(output);
            } catch (IOException e) {
                System.err.println("Error parsing input anime genres: " + e.getMessage());
                return new ArrayList<>();
            }
        });

        // Filter user scores to only include the highest scores
        CompletableFuture<List<UserScore>> highestScoresFuture = userScoresFuture
                .thenApplyAsync(this::filterHighestScores);

        // Get a list of all anime that match the input anime's genres
        CompletableFuture<List<Anime>> allMatchingAnimeListFuture = highestScoresFuture
                .thenComposeAsync(highestScores -> {
                    List<CompletableFuture<List<Anime>>> futures = highestScores.stream()
                            .map(userScore -> CompletableFuture.supplyAsync(() -> {
                                try {
                                    return getUserFavoritesContainingSimilarGenres(userScore.getUsername(),
                                            inputAnimeGenresFuture.get()).stream()
                                            .map(obj -> (Anime) obj)
                                            .collect(Collectors.toList());
                                } catch (Exception e) {
                                    System.err.println(
                                            "Error fetching user favorites for user: " + userScore.getUsername());
                                    return new ArrayList<Anime>();
                                }
                            }))
                            .collect(Collectors.toList());

                    return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                            .thenApplyAsync(v -> futures.stream()
                                    .map(CompletableFuture::join)
                                    .flatMap(List::stream)
                                    .collect(Collectors.toList()));
                });

        // Print the top 10 recommended anime
        allMatchingAnimeListFuture.thenAcceptAsync(allMatchingAnimeList -> {
            Map<Anime, Long> animeCounts = allMatchingAnimeList.stream()
                    .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

            List<Anime> sortedAnimeList = animeCounts.entrySet().stream()
                    .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());

            List<Anime> filteredAnimeList = sortedAnimeList.stream()
                    .filter(anime -> !anime.getTitle().equals(inputTitle))
                    .collect(Collectors.toList());

            for (int i = 0; i < 10; i++) {
                Anime anime = filteredAnimeList.get(i);
                System.out.println(anime.getTitle() + " (" + anime.getWeight() + ")");
                animeList.add(anime);
            }
        });

        // Wait for all futures to complete
        CompletableFuture.allOf(userScoresFuture, inputAnimeGenresFuture, highestScoresFuture,
                allMatchingAnimeListFuture).join();

        return animeList;
    }

    /**
     * Parses the user scores from the output of the Python script.
     * 
     * @param output Output of the Python script
     * @return List of UserScore objects
     * @throws IOException If there is an error parsing the JSON output
     */
    private List<UserScore> parseUserScores(String output) throws IOException {
        JsonNode jsonNode = mapper.readTree(output);
        if (jsonNode.has("user_scores")) {
            return mapper.convertValue(jsonNode.get("user_scores"), new TypeReference<List<UserScore>>() {
            });
        } else {
            return new ArrayList<>();
        }
    }

    /**
     * Parses the genres from the output of the Python script.
     * 
     * @param output Output of the Python script
     * @return List of genre strings
     * @throws IOException If there is an error parsing the JSON output
     */
    private List<String> parseGenres(String output) throws IOException {
        JsonNode jsonNode = mapper.readTree(output);
        if (jsonNode.has("genres_and_themes")) {
            return mapper.convertValue(jsonNode.get("genres_and_themes"), new TypeReference<List<String>>() {
            });
        } else {
            return new ArrayList<>();
        }
    }

    /**
     * Parses a list of anime from a JSON string.
     * 
     * @param output JSON string containing a list of anime
     * @return List of Anime objects
     */
    private List<Anime> parseAnimeList(String output) {
        try {
            return mapper.readValue(output, new TypeReference<List<Anime>>() {
            });
        } catch (IOException e) {
            System.err.println("Failed to parse anime list: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Filters the list of user scores to only include the highest scores.
     * 
     * @param userScores List of UserScore objects
     * @return List of UserScore objects with a score of 10
     */
    private List<UserScore> filterHighestScores(List<UserScore> userScores) {
        return userScores.stream()
                .filter(userScore -> userScore.getScore() == 10)
                .collect(Collectors.toList());
    }

    // // Create a RateLimiter that allows 3 requests per second
    // RateLimiter rateLimiterPerSecond = RateLimiter.create(3.0);
    // // Create a RateLimiter that allows 60 requests per minute
    // RateLimiter rateLimiterPerMinute = RateLimiter.create(60.0);

    /**
     * Gets a list of the user's favorite anime that contain similar genres to the
     * input anime.
     * 
     * @param username         Username of the user
     * @param inputAnimeGenres List of genres for the input anime
     * @return List of Anime objects
     * @throws IOException If there is an error making the API request or parsing
     *                     the response
     */
    public List<Anime> getUserFavoritesContainingSimilarGenres(String username, List<String> inputAnimeGenres)
            throws IOException {

        // Make the API call
        Response response = null;
        boolean acquired = false;
        try {
            // Acquire the per-second and per-minute permits before making the API call
            try {
                perSecondSemaphore.acquire();
                perMinuteSemaphore.acquire();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Preserve the interrupt status
                // Handle the exception
                System.err.println("Thread was interrupted while waiting for a permit.");
                return null;
            }

            Request request = new Request.Builder()
                    .url("https://api.jikan.moe/v4/users/" + username + "/favorites")
                    .build();
            acquired = true;
            response = client.newCall(request).execute();

            if (!response.isSuccessful()) {
                System.err.println("API call failed with response: " + response.body().string());
                return null;
            }
        } finally {
            if (acquired) {
                // Release the per-second permit immediately
                perSecondSemaphore.release();

                // Schedule the release of the per-minute permit after 60 seconds
                new Thread(() -> {
                    try {
                        TimeUnit.SECONDS.sleep(60);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        perMinuteSemaphore.release();
                    }
                }).start();
            }
        }

        UserFavoritesResponse userFavorites = mapper.readValue(response.body().string(), UserFavoritesResponse.class);

        List<Anime> favoriteAnime = userFavorites.getAnime();
        if (favoriteAnime == null || favoriteAnime.isEmpty()) {
            favoriteAnime = new ArrayList<>();
            String url = "https://myanimelist.net/animelist/" + username + "?status=2&order=4&order2=0";
            String output = pythonService.runPythonScript(url, "top_anime");
            List<Anime> top5Anime = mapper.convertValue(parseAnimeList(output), new TypeReference<List<Anime>>() {
            });
            favoriteAnime.addAll(top5Anime);
        }

        int maxWeight = 0;
        List<Anime> matchingFavorites = new ArrayList<>();
        Set<String> inputAnimeGenresSet = new HashSet<>(inputAnimeGenres);

        for (Anime anime : favoriteAnime) {
            String url = anime.getUrl();
            String outpString = pythonService.runPythonScript(url, "genres_and_themes");
            List<String> genres = parseGenres(outpString);
            if (genres != null) {
                for (String genre : genres) {
                    if (inputAnimeGenresSet.contains(genre)) {
                        anime.setWeight(anime.getWeight() + 1);
                    }
                }
                if (anime.getWeight() > maxWeight) {
                    matchingFavorites.clear();
                    maxWeight = anime.getWeight();
                }
                if (anime.getWeight() == maxWeight && anime.getWeight() > 0) {
                    matchingFavorites.add(anime);
                }
            }
        }

        return matchingFavorites;
    }

    /**
     * Response object for the Jikan API call to get user favorites.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class UserFavoritesResponse {
        private List<Anime> anime;

        public List<Anime> getAnime() {
            return anime;
        }
    }




}