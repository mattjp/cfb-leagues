# CFB Leagues

1. Get list of teams
  
    a) Done via `team_collector.py`
  
    b) To run `team_collector.py`, add the file `resources/secrets.json`

      ```json
      {
        "api_key": "Bearer [your_api_key]"
      }
      ```

    c) This should be done through a secrets manager but this is attempt number 1


2. Create initial leagues

    a) Done using rankings from starting year via `init.scala`



+ Create initial leagues
  + 10 leagues with 12 teams
+ For each year (1999 end-of-season to 2019 end-of-season):
  + For each team:
    + For each game:
        + Update point total based on opponent and outcome
          +  `+3` for win in league above
          +  `+2` for win in same league
          +  `+1` for win in league below
          +  `+0` for win against FCS team
          +  `-0` for loss in league or above league 
          +  `-1` for loss in league below
          +  `-2` for loss against FCS team 
  + Sort teams in each league by points
  + Update leagues by promoting and relegating
