package com.animerec.animerec;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.Comparator;
import java.util.HashSet;

import com.google.common.util.concurrent.RateLimiter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


/**
 * This class is the entry point of the application.
 */
@SpringBootApplication
public class AnimerecApplication {

    // TODO: will implement after getting authorization for MAL API
    // private static final String API_KEY = "MAL_API_KEY";

    private final PythonService pythonService;
    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    public AnimerecApplication(PythonService pythonService) {
        this.pythonService = pythonService;
    }

    public static void main(String[] args) {
        SpringApplication.run(AnimerecApplication.class, args);
    }

    /**
     * Runs the python script to scrape the usernames and scores of users who have
     * watched the input anime.
     * 
     * @throws IOException
     */
    @PostConstruct
    public void runPythonScript() throws IOException, InterruptedException {
        // Hardcoded for now, will be passed in from the frontend later
        String url = "https://myanimelist.net/anime/49387/Vinland_Saga_Season_2/stats?m=all#members";
        String output = pythonService.runPythonScript(url, "both");

        // Parse the output into a list of UserScore objects
        List<UserScore> userScores = parseUserScores(output);

        // Grab the genres and themes of the input anime
        List<String> inputAnimeGenres = parseGenres(output);

        List<UserScore> highestScores = filterHighestScores(userScores);

        List<Anime> allMatchingAnimeList = Collections.synchronizedList(new ArrayList<>());

        // Create a RateLimiter that allows 1 requests per second
        RateLimiter rateLimiter = RateLimiter.create(1);

        for (UserScore userScore : highestScores) {
            // Acquire a permit before making a request
            rateLimiter.acquire();

            try {
                List<Anime> matchingAnimeList = getUserFavoritesContainingSimilarGenres(userScore.getUsername(),
                        inputAnimeGenres);

                // If the user has no favorites, skip to the next user
                if (matchingAnimeList != null) {
                    allMatchingAnimeList.addAll(matchingAnimeList);
                }

            } catch (IOException e) {
                // Handle exception
                System.err.println("Error fetching user favorites for user: " + userScore.getUsername());
            }

        }

        // Count the number of times each anime appears in the list
        Map<Anime, Long> animeCounts = allMatchingAnimeList.stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        // Sort the list by count
        List<Anime> sortedAnimeList = animeCounts.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        // Print the top 10 matches
        for (int i = 0; i < 10; i++) {
            System.out.println(sortedAnimeList.get(i));
        }
    }

    /**
     * Parses the output of the python script into a list of UserScore objects
     * 
     * @param output
     * @return
     * @throws IOException
     */
    private List<UserScore> parseUserScores(String output) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(output);
        if (jsonNode.has("user_scores")) {
            return mapper.convertValue(jsonNode.get("user_scores"), new TypeReference<List<UserScore>>() {
            });
        } else {
            return new ArrayList<>();
        }
    }

    /**
     * Parse Genres and Themes from the output of the webscrape
     */
    private List<String> parseGenres(String output) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(output);
        if (jsonNode.has("genres_and_themes")) {
            return mapper.convertValue(jsonNode.get("genres_and_themes"), new TypeReference<List<String>>() {
            });
        } else {
            return new ArrayList<>();
        }
    }

    /** 
     * Parse the anime list from the output of the webscrape
     */
    private List<Anime> parseAnimeList(String output) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            // Parse the output as a list of Anime objects
            return mapper.readValue(output, new TypeReference<List<Anime>>() {});
        } catch (IOException e) {
            System.err.println("Failed to parse anime list: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Filters the list of UserScore objects to only include the users with the
     * highest score
     * 
     * @param userScores
     * @return
     */
    private List<UserScore> filterHighestScores(List<UserScore> userScores) {
        return userScores.stream()
                .filter(userScore -> userScore.getScore() == 10)
                .collect(Collectors.toList());
    }

    /**
     * Fetches the user's favorites and returns a list of anime that contain the
     * input anime
     * 
     * @param username
     * @param inputAnimeId
     * @return
     * @throws IOException
     */
    public List<Anime> getUserFavoritesContainingSimilarGenres(String username, List<String> inputAnimeGenres)
            throws IOException {
        // Fetch the user's favorites
        Request request = new Request.Builder()
                .url("https://api.jikan.moe/v4/users/" + username + "/favorites")
                .build();
        Response response = client.newCall(request).execute();

        // If the API call failed, print the response and return null
        if (!response.isSuccessful()) {
            System.err.println("API call failed with response: " + response.body().string());
            return null;
        }

        UserFavoritesResponse userFavorites = mapper.readValue(response.body().string(), UserFavoritesResponse.class);

        // If the user has no favorite anime, instead grab the users top 5 anime using the webscraper
        List<Anime> favoriteAnime = userFavorites.getAnime();
        if (favoriteAnime == null || favoriteAnime.isEmpty()) {
            favoriteAnime = new ArrayList<>();
            String url = "https://myanimelist.net/animelist/" + username + "?status=2&order=4&order2=0";
            String output = pythonService.runPythonScript(url, "user_list");
            List<Anime> top5Anime = mapper.convertValue(parseAnimeList(output), new TypeReference<List<Anime>>() {
            });
            favoriteAnime.addAll(top5Anime);
        }


        int maxWeight = 0; // The weight of the anime with the highest weight 
        List<Anime> matchingFavorites = new ArrayList<>(); // The list of anime with the highest weight
        Set<String> inputAnimeGenresSet = new HashSet<>(inputAnimeGenres);

        // Create a list of anime that match the input anime
        for (Anime anime : favoriteAnime) {
            // Fetch the anime url
            String url = anime.getUrl();
            String outpString = pythonService.runPythonScript(url, "genres_only");
            List<String> genres = parseGenres(outpString);
            if (genres != null) {
                for (String genre : genres) {
                    if (inputAnimeGenresSet.contains(genre)) {
                        anime.setWeight(anime.getWeight() + 1); // increment weight
                    }
                }
                // If the anime's weight is greater than the current maximum, clear the list and update the maximum
                if (anime.getWeight() > maxWeight) {
                    matchingFavorites.clear();
                    maxWeight = anime.getWeight();
                }
                // If the anime's weight is equal to the maximum, add it to the list
                if (anime.getWeight() == maxWeight && anime.getWeight() > 0) {
                    matchingFavorites.add(anime);
                }
            }
        }

        return matchingFavorites;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class UserFavoritesResponse {
        private List<Anime> anime;

        // getters and setters...
        public List<Anime> getAnime() {
            return anime;
        }
    }


}