import sys
import json
from bs4 import BeautifulSoup
import requests

url = sys.argv[1]

user_score_list = []
counter = 0

# Loop through all the pages of the leaderboard
while url and counter < 100:
    r = requests.get(url)
    r.content

    soup = BeautifulSoup(r.content, 'html.parser')

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

    if counter >= 100:
        break

    # Find the "Next Page" link
    next_page = soup.find('a', text='Next Page')
    url = next_page['href'] if next_page else None

# Print the list in JSON format
print(json.dumps(user_score_list))