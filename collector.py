from urllib.request import urlopen

url = 'https://www.sports-reference.com/cfb/schools/'

page = urlopen(url)

# print(page)

html_bytes = page.read()

# print(html_bytes)

html = html_bytes.decode('utf-8', 'ignore')

print(html)


with open('teams.html', 'w+') as file:
	file.write(html)






