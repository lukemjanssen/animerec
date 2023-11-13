import sys
import json
import requests
from bs4 import BeautifulSoup
from selenium import webdriver
from selenium.webdriver.firefox.options import Options
import argparse



def scrape_user_scores(url):
    user_score_list = []
    counter = 0
    session = requests.Session()

    while url and counter < 100:
        r = session.get(url)
        soup = BeautifulSoup(r.content, 'html.parser')

        leftside_div = soup.select_one('div.leftside')

        genres_and_themes = []
        for div in leftside_div.select('div.spaceit_pad'):
            dark_text = div.select_one('span.dark_text')
            if dark_text and (dark_text.text.strip() == 'Genres:' or dark_text.text.strip() == 'Themes:'):
                genres_and_themes.extend([a.text for a in div.select('a')])

        rows = soup.select('tr')
        for i, row in enumerate(rows):
            username = row.select_one('a.word-break')
            score = row.select_one('td.borderClass.ac')
            if username and score:
                score_text = score.text.strip()
                if not score_text.isnumeric():
                    score_text = '0'
                if score_text == '10':
                    user_score_list.append({'username': username.text, 'score': score_text})
                    counter += 1
                    if counter >= 100:
                        break

        next_page = soup.select_one('a:contains("Next Page")')
        url = next_page['href'] if next_page else None

    return {'user_scores': user_score_list, 'genres_and_themes': genres_and_themes}


def scrape_top_anime(url):
    options = Options()
    options.add_argument('--headless')
    anime_list = []

    with webdriver.Firefox(options=options) as driver:
        driver.get(url)
        driver.implicitly_wait(10)

        list_items = driver.find_elements('css selector', 'tr.list-table-data')
        for i, item in enumerate(list_items):
            if i >= 5:
                break
            anime_link = item.find_element('css selector', 'td.data.title.clearfix > a.link.sort')
            anime_url = anime_link.get_attribute('href')
            anime_title = anime_link.text.strip()
            anime_id = anime_url.split('/')[-2]
            anime_list.append({
                'title': anime_title,
                'url': anime_url,
                'mal_id': anime_id
            })

    return anime_list


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('url', help='URL of the leaderboard or user list')
    parser.add_argument('flag', help='Flag to indicate what to scrape: user_scores, genres_and_themes, or top_anime')
    args = parser.parse_args()

    if args.flag == 'user_scores' or args.flag == 'genres_and_themes':
        result = scrape_user_scores(args.url)
    elif args.flag == 'top_anime':
        result = scrape_top_anime(args.url)
    else:
        print('Invalid flag')
        sys.exit(1)

    print(json.dumps(result))
