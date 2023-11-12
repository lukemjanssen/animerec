package com.animerec.animerec;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
// import okhttp3.OkHttpClient;
// import okhttp3.Request;
// import okhttp3.Response;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class is the entry point of the application.
 */
@SpringBootApplication
public class AnimerecApplication {

    //TODO: will implement after getting authorization for MAL API
    //private static final String API_KEY = "YOUR_MAL_API_KEY";

    private final PythonService pythonService;

    @Autowired
    public AnimerecApplication(PythonService pythonService) {
        this.pythonService = pythonService;
    }

    public static void main(String[] args) {
        SpringApplication.run(AnimerecApplication.class, args);
    }

    /**
     * Runs the python script to scrape the usernames and scores of users who have watched the input anime.
     * @throws IOException
     */
    @PostConstruct
    public void runPythonScript() throws IOException {
        // Hardcoded for now, will be passed in from the frontend later
        String url = "https://myanimelist.net/anime/49387/Vinland_Saga_Season_2/stats?m=all#members"; 
        String output = pythonService.runPythonScript(url);

        ObjectMapper mapper = new ObjectMapper();
        List<UserScore> userScores = mapper.readValue(output, new TypeReference<List<UserScore>>(){});

        List<UserScore> highestScores = userScores.stream()
            .filter(userScore -> userScore.getScore() == 10)
            .collect(Collectors.toList());

        highestScores.forEach(userScore -> System.out.println(userScore.getUsername()));
    }


    //TODO: will implement after getting authorization for MAL API
    // /**
    //  * Fetches the anime list of each user in the usernames list and returns a list of anime that match the genre of the input anime
    //  * @param inputAnimeId
    //  * @param usernames
    //  * @return
    //  * @throws IOException
    //  */
    // public List<Anime> getUserAnimeListMatchingGenre(String inputAnimeId, List<String> usernames) throws IOException {
    //     OkHttpClient client = new OkHttpClient();
    //     ObjectMapper mapper = new ObjectMapper();

    //     // Fetch the details of the input anime
    //     Request animeRequest = new Request.Builder()
    //             .url("https://api.myanimelist.net/v2/anime/" + inputAnimeId)
    //             .addHeader("Authorization", "Bearer " + API_KEY)
    //             .build();
    //     Response animeResponse = client.newCall(animeRequest).execute();
    //     Anime inputAnime = mapper.readValue(animeResponse.body().string(), Anime.class);

    //     List<Anime> matchingAnime = usernames.stream()
    //         .flatMap(username -> {
    //             try {
    //                 // Fetch the user's anime list
    //                 Request userRequest = new Request.Builder()
    //                     .url("https://api.myanimelist.net/v2/users/" + username + "/animelist")
    //                     .addHeader("Authorization", "Bearer " + API_KEY)
    //                     .build();
    //                 Response userResponse = client.newCall(userRequest).execute();
    //                 AnimeListResponse userAnimeList = mapper.readValue(userResponse.body().string(), AnimeListResponse.class);

    //                 // Filter the user's anime list to only include anime with the same genre as the input anime
    //                 return Arrays.stream(userAnimeList.data)
    //                     .filter(anime -> anime.node.genres.contains(inputAnime.node.genres.get(0)));
    //             } catch (IOException e) {
    //                 throw new RuntimeException(e);
    //             }
    //         })
    //         .collect(Collectors.toList());

    //     return matchingAnime;
    // }

    // static class AnimeListResponse {
    //     Anime[] data;
    // }

    // static class Anime {
    //     Node node;
    // }

    // static class Node {
    //     List<String> genres;
    // }
}