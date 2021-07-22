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
  + 2005 is earliest year
  + For each team:
    + For each game:
        + Update point total based on opponent and outcome
          + Opponent is in same league
            + `+3` Win
              + `+1` Away
              + `+1` Point margin > 14
            + `+0` Loss
              + `+1` Away AND Point margin < 15
          + Opponent is in league above
            + `+4` Win
              + `+2` Away
              + `+2` Point margin > 14
            + `+0` Loss
              + `+1` Point margin < 15
              + `+1` Away and Point margin < 15
          + Opponent is in league below
            + `+2` for win
              + `+1` if away AND point margin > 14
              + `+1` if point margin > 21
            + `-1` for loss
              + `-1` if home
              + `-1` if point margin > 14
          + Opponent is FCS
            + `+0` for win
              + `-1` Point margin < 14
            + `-2` for loss
              + `-2` Home
              + `-2` Point margin > 14
    + Reasoning
      + Reward decisive victories without promoting blowouts
        + 15 point win margin is a reasonably comfortable margin of victory
        + Take away some "garbage time" in games if league points are on the line
      + Reward the away team, as winning on the road is difficult and a good indicator of how good a team is
      + Punish teams for playing worse competition, especially FCS teams
    + Old rules:        
      +  `+3` for win in league above
      +  `+2` for win in same league
      +  `+1` for win in league below
      +  `+0` for win against FCS team
      +  `-0` for loss in league or above league 
      +  `-1` for loss in league below
      +  `-2` for loss against FCS team 
  + Sort teams in each league by points
  + Update leagues by promoting and relegating
