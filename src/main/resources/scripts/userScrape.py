import sys
import json
import requests
from bs4 import BeautifulSoup
from selenium import webdriver
from selenium.webdriver.firefox.options import Options
import argparse
import threading


# Global Buffer for user scores
user_score_buffer = []


# Create a global counter and a lock
user_counter = 0
counter_lock = threading.Lock()


import traceback

def scrape_genres_and_themes(url):
    try:
        #print(f"Starting to scrape genres and themes from: {url}")
        session = requests.Session()
        r = session.get(url)
        soup = BeautifulSoup(r.content, 'html.parser')

        leftside_div = soup.select_one('div.leftside')

        if not leftside_div:
            print("No 'div.leftside' found on the page.")
            return []

        genres_and_themes = []
        for div in leftside_div.select('div.spaceit_pad'):
            dark_text = div.select_one('span.dark_text')
            if dark_text and (dark_text.text.strip() == 'Genres:' or dark_text.text.strip() == 'Themes:'):
                genres_and_themes.extend([a.text for a in div.select('a')])

        #print(f"Successfully scraped genres and themes: {genres_and_themes}")
        return genres_and_themes
    except Exception as e:
        print(f"An error occurred while scraping genres and themes from: {url}")
        print(traceback.format_exc())
        return []


def scrape_user_scores(url):
   global user_score_buffer
   global user_counter
   session = requests.Session()

   r = session.get(url)
   soup = BeautifulSoup(r.content, 'html.parser')

   rows = soup.select('tr')
   local_buffer = []
   for i, row in enumerate(rows):
       username = row.select_one('a.word-break')
       score = row.select_one('td.borderClass.ac')
       if username and score:
           score_text = score.text.strip()
           if not score_text.isnumeric():
               score_text = '0'
           if score_text == '10':
               local_buffer.append({'username': username.text, 'score': score_text})
               if len(local_buffer) >= 10:  # Update the global buffer and counter once per 10 users
                   with counter_lock:
                       if user_counter >= 100:
                           break
                       user_score_buffer.extend(local_buffer[:100-user_counter])
                       user_counter += len(local_buffer[:100-user_counter])
                   local_buffer = []
   # Update the global buffer and counter with remaining users at the end of the page
   with counter_lock:
       if user_counter < 100:
           user_score_buffer.extend(local_buffer[:100-user_counter])
           user_counter += len(local_buffer[:100-user_counter])


def manage_threads(base_url, num_threads):
   threads = []
   for i in range(num_threads):
       url = f"{base_url}&show={i*75}"
       t = threading.Thread(target=scrape_user_scores, args=(url,))
       t.start()
       threads.append(t)


   # Wait for all threads to finish
   for t in threads:
       t.join()




def scrape_top_anime(url):
    options = Options()
    options.add_argument('--headless')
    anime_list = []

    try:
        with webdriver.Firefox(options=options) as driver:
            driver.get(url)
            driver.implicitly_wait(10)

            list_items = driver.find_elements('css selector', 'tr.list-table-data')
            for i, item in enumerate(list_items):
                if i >= 15:
                    break
                anime_link = item.find_element('css selector', 'td.data.title.clearfix > a.link.sort')
                anime_url = anime_link.get_attribute('href')
                anime_title = anime_link.text.strip()
                anime_img_url = item.find_element('css selector', 'td.image > a > img').get_attribute('src')
                anime_id = anime_url.split('/')[-2]
                anime_list.append({
                    'title': anime_title,
                    'url': anime_url,
                    'mal_id': anime_id,
                    'anime_img_url': anime_img_url
                })

    except Exception as e:
        print(f"An error occurred: {e}")

    return anime_list


def manage_threads(base_url, num_threads):
   threads = []
   for i in range(num_threads):
       url = f"{base_url}&show={i*75}"
       t = threading.Thread(target=scrape_user_scores, args=(url,))
       t.start()
       threads.append(t)


   # Wait for all threads to finish
   for t in threads:
       t.join()


if __name__ == '__main__':
   parser = argparse.ArgumentParser()
   parser.add_argument('url', help='URL of the leaderboard or user list')
   parser.add_argument('flag', help='Flag to indicate what to scrape: user_scores, genres_and_themes, or top_anime')
   args = parser.parse_args()


   if args.flag == 'user_scores':
       manage_threads(args.url, 10)  # Create 10 threads
       result = {'user_scores': user_score_buffer}
   elif args.flag == 'genres_and_themes':
       result = {'genres_and_themes': scrape_genres_and_themes(args.url)}
   elif args.flag == 'top_anime':
       result = scrape_top_anime(args.url)
   else:
       print('Invalid flag')
       sys.exit(1)


   print(json.dumps(result))





