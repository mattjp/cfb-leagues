package sim

import types.{Game, League, Team}


object Main extends App {

	val startYear = 2005 // update how this is set
	val endYear = 2020 // update how this is set
	val leagueSize = 10
	val n = 3

	println(s"Running simulation for $startYear...")

	// 1. Run initialization of teams and leagues
	// there needs to be a way of doing this by hitting DB not API
	// val leagues: Seq[League] = Init.initializeLeaguesFromApi(year, leagueSize) // Default 2005, 10
	val leagues: Seq[League] = Init.initializeLeaguesFromDb(startYear, leagueSize)
	// println(leagues)

	// 2. Write all teams to DB
	// this doesn't need to happen every time
	val db: Db = Db("teams")
	// leagues.foreach { league => db.writeTeams(league.teams) }

	// val simulatedLeagues: Seq[League] = Simulate.simulateSeason(leagues, year)	
	// simulatedLeagues.foreach { league => db.writeTeams(league.teams) }
	val simulatedLeagues: Seq[League] = leagues

	// promote and relegate
	val updatedLeagues: Seq[League] = Simulate.promoteAndRelegate(simulatedLeagues, n)

	// write updated leagues to DB for upcoming year

	for (league <- updatedLeagues) {
		println(s"League ${league.id}")
		for (team <- league.teams) {
			println(team)
		}
		println()
	}

}
