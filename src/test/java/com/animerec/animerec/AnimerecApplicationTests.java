package com.animerec.animerec;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.List;

@SpringBootTest
public class AnimerecApplicationTests {

    @Autowired
    private AnimerecApplication animerecApplication;

    @Test
    public void testGetRecList() throws IOException, InterruptedException {
        String url1 = "https://myanimelist.net/anime/10165/Nichijou/stats?m=all#members";
        List<Anime> animeList1 = animerecApplication.getRecList(url1);
        // Add assertions to check the results
		printList(animeList1);
    }

	private void printList(List<Anime> animeList) {
		for (Anime anime : animeList) {
			System.out.println(anime.getTitle() + " " + anime.getWeight());
		}
	}

}