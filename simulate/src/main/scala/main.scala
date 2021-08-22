package sim

import types.{Game, League, Team}


object Main extends App {

	val startYear = 2006 // update how this is set
	val endYear = 2006 // update how this is set
	val leagueSize = 10
	val n = 3

	// 0. Get DB connection
	val db: Db = Db("teams")

	// 1. Run initialization of teams and leagues
	// val leagues: Seq[League] = Init.initializeLeaguesFromApi(year, leagueSize) // Default 2005, 10

	// 2. Write all teams to DB
	// db.writeLeagues(leagues)

	(startYear to endYear).toSeq.foreach { year =>
		println(s"Running simulation for $year...")

		val leagues: Seq[League] = Init.initializeLeaguesFromDb(startYear, leagueSize)
		// println(leagues)

		val leaguesSimulated: Seq[League] = Simulate.simulateSeason(leagues, year)	
		// val leaguesSimulated: Seq[League] = leagues // used for testing

		db.writeLeagues(leaguesSimulated)

		// TODO -> use andThen
		// update year
		val leaguesUpdatedYear: Seq[League] = Simulate.updateYear(leaguesSimulated, year + 1)

		// promote and relegate
		val leaguesPromotedRelegated: Seq[League] = Simulate.promoteAndRelegate(leaguesUpdatedYear, n)

		// reset ranks
		val leaguesSortedRanked: Seq[League] = Simulate.sortAndRankLeagues(leaguesPromotedRelegated, leagueSize)

		// reset points
		val leaguesReset: Seq[League] = Simulate.resetPoints(leaguesSortedRanked)

		// write updated leagues to DB for upcoming year
		db.writeLeagues(leaguesReset)
	}

}
