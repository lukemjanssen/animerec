package com.animerec.animerec;

import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

/**
 * AnimeController
 * 
 * This class is the controller for the Anime class.
 * 
 */
@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class AnimeController {

    private final AnimerecApplication animerecApplication;

    @Autowired
    public AnimeController(AnimerecApplication animerecApplication) {
        this.animerecApplication = animerecApplication;
    }

    @GetMapping("/getrecs")
    public void runApplication(@RequestParam String url) throws IOException, InterruptedException {
        animerecApplication.getRecList(url);
    }
}