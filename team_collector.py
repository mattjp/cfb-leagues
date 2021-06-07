import json
import requests

def collect_teams(year: int = 1999) -> None:
	"""
	Writes a newline delimited list of FBS teams to resources/teams.txt
	TODO: add wrapper for selecting different year
	"""

	with open('resources/config.json') as config_file, open('resources/secrets.json') as secrets_file:
		config_json = json.load(config_file)
		secrets_json = json.load(secrets_file)

		url = config_json['base_url'] + config_json['fbs_teams_endpoint']
		api_key = 'Bearer ' + secrets_json['api_key']
	

	headers = {'Authorization': api_key}
	params = {'year': year}
	response = requests.get(url, headers = headers, params = params).json()

	names = list(map(lambda r: r['school'], response))

	with open('resources/teams.txt', 'w') as teams_file:
		teams_file.write("\n".join(names))


if __name__ == '__main__':
	collect_teams()
