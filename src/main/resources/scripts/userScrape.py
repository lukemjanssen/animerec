import sys
import json
from bs4 import BeautifulSoup
import requests
from selenium import webdriver
from selenium.webdriver.firefox.options import Options

url = sys.argv[1]  # Get the URL from the command line
flag = sys.argv[2]  # Get the flag from the command line

user_score_list = []
counter = 0

# Loop through all the pages of the leaderboard
while url and counter < 100:
    r = requests.get(url)
    r.content

    soup = BeautifulSoup(r.content, 'html.parser')

    # Find the 'leftside' div
    leftside_div = soup.find('div', class_='leftside')

    # Find the genres and themes
    if flag != 'user_list':
        genres_and_themes = []
        for div in leftside_div.find_all('div', class_='spaceit_pad'):
            dark_text = div.find('span', class_='dark_text')
            if dark_text and (dark_text.text.strip() == 'Genres:' or dark_text.text.strip() == 'Themes:'):
                genres_and_themes.extend([a.text for a in div.find_all('a')])
        if flag == 'genres_only':
            break

    # Scrape user scores only if the flag is not 'genres_only'
    if flag != 'genres_only' and flag != 'user_list':
        # Find all the rows in the table
        rows = soup.find_all('tr')

        # For each row, find the username and score and add them to a list
        for row in rows:
            username = row.find('a', class_='word-break')
            score = row.find('td', class_='borderClass ac')
            if username and score:  # Make sure both elements were found
                score_text = score.text.strip()
                # Check if score is numeric, if not, set it to '0'
                if not score_text.isdigit():
                    score_text = '0'
                if score_text == '10':
                    user_score_list.append({'username': username.text, 'score': score_text})
                    counter += 1
                    if counter >= 100:
                        break




    # Scrape top 5 anime from user's list if the flag is 'user_list'
    if flag == 'user_list':
        # Set up headless mode
        options = Options()
        options.headless = True

        # Create a new instance of the Firefox driver
        driver = webdriver.Firefox(options=options)

        # Go to the webpage
        driver.get(url)

        # Wait for the page to load and JavaScript to execute
        driver.implicitly_wait(10)

        anime_list = []
        list_items = driver.find_elements('css selector', 'tr.list-table-data')
        for i, item in enumerate(list_items):
            if i >= 5:  # Only get the top 5 anime
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
        print(json.dumps(anime_list))  # Print the list of anime as JSON
        driver.close()
        break

    # Don't forget to close the driver when you're done




    if counter >= 100:
        break

    # Find the "Next Page" link
    next_page = soup.find('a', text='Next Page')
    url = next_page['href'] if next_page else None

# Output the user scores and genres/themes based on the flag
if flag == 'genres_only':
    print(json.dumps({'genres_and_themes': genres_and_themes}))
else:
    print(json.dumps({'user_scores': user_score_list, 'genres_and_themes': genres_and_themes}))