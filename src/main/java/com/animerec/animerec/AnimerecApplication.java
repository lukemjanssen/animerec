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

import com.google.common.util.concurrent.RateLimiter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AnimerecApplication {

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

    @PostConstruct
    public void runPythonScript() throws IOException, InterruptedException {
        String url = "https://myanimelist.net/anime/1639/Boku_no_Pico/stats?m=all#members";
        String inputTitle = "Boku no Pico";
        CompletableFuture<List<UserScore>> userScoresFuture = CompletableFuture.supplyAsync(() -> {
            try {
                String output = pythonService.runPythonScript(url, "user_scores");
                return parseUserScores(output);
            } catch (IOException e) {
                System.err.println("Error parsing user scores: " + e.getMessage());
                return new ArrayList<>();
            }
        });

        CompletableFuture<List<String>> inputAnimeGenresFuture = CompletableFuture.supplyAsync(() -> {
            try {
                String output = pythonService.runPythonScript(url, "user_scores");
                return parseGenres(output);
            } catch (IOException e) {
                System.err.println("Error parsing input anime genres: " + e.getMessage());
                return new ArrayList<>();
            }
        });

        CompletableFuture<List<UserScore>> highestScoresFuture = userScoresFuture
                .thenApplyAsync(this::filterHighestScores);

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
                System.out.println(filteredAnimeList.get(i).getTitle());
            }
        });
    }

    private List<UserScore> parseUserScores(String output) throws IOException {
        JsonNode jsonNode = mapper.readTree(output);
        if (jsonNode.has("user_scores")) {
            return mapper.convertValue(jsonNode.get("user_scores"), new TypeReference<List<UserScore>>() {
            });
        } else {
            return new ArrayList<>();
        }
    }

    private List<String> parseGenres(String output) throws IOException {
        JsonNode jsonNode = mapper.readTree(output);
        if (jsonNode.has("genres_and_themes")) {
            return mapper.convertValue(jsonNode.get("genres_and_themes"), new TypeReference<List<String>>() {
            });
        } else {
            return new ArrayList<>();
        }
    }

    private List<Anime> parseAnimeList(String output) {
        try {
            return mapper.readValue(output, new TypeReference<List<Anime>>() {
            });
        } catch (IOException e) {
            System.err.println("Failed to parse anime list: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    private List<UserScore> filterHighestScores(List<UserScore> userScores) {
        return userScores.stream()
                .filter(userScore -> userScore.getScore() == 10)
                .collect(Collectors.toList());
    }

    // Create a RateLimiter that allows 3 requests per second
    RateLimiter rateLimiterPerSecond = RateLimiter.create(3.0);
    // Create a RateLimiter that allows 60 requests per minute
    RateLimiter rateLimiterPerMinute = RateLimiter.create(60.0);

    public List<Anime> getUserFavoritesContainingSimilarGenres(String username, List<String> inputAnimeGenres)
            throws IOException {

        // Acquire a permit from both rate limiters
        rateLimiterPerSecond.acquire();
        rateLimiterPerMinute.acquire();
        
        Request request = new Request.Builder()
                .url("https://api.jikan.moe/v4/users/" + username + "/favorites")
                .build();
        Response response = client.newCall(request).execute();

        if (!response.isSuccessful()) {
            System.err.println("API call failed with response: " + response.body().string());
            return null;
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

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class UserFavoritesResponse {
        private List<Anime> anime;

        public List<Anime> getAnime() {
            return anime;
        }
    }
}