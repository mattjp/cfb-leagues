package sim

import types.{League, Team}


object Main extends App {

	println("Running simulation...")

	// 1. Run initialization of teams and leagues
	// val leagues: Seq[League] = Init.initializeLeagues(2005, 2) // Default 2005, 10

	// 2. Write all teams to DB
	val db: Db = Db("teams")
	// leagues.foreach { league => db.writeTeams(league.teams) }

	
	

	// val t = Team(
	// 	name = "University of Example",
	// 	conference = "FBS Independents",
	// 	leagueId = Some(8),
	// 	initialSpRating = 5.67,
	// 	year = 2005
	// )

	// val r = db.writeTeam(t)
	// println(r)

	// val result = db.getTeams(leagueId = Some(1))
	// println(result)

	// db.writeTeam(t)
	
	// Simulate.getWeeks(2005)

	val g = Simulate.getGame("Alabama", 2005, 1)
	println(g)

}
