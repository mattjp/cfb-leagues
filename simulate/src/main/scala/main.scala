package sim

import types.{League, Team}


object Main extends App {

	println("Running simulation...")

	// 1. Run initialization of teams and leagues
	// val leagues: Seq[League] = Init.initializeLeagues(2000, 2) // Default 2000, 10

	// 2. Write all teams to DB
	val db: Db = Db("teams")
	// leagues.foreach { league => db.writeTeams(league.teams) }

	
	

	val t = Team(
		name = "University of Example",
		conference = "FBS Independents",
		leagueId = Some(8),
		initialSpRating = 5.67,
		year = 2000
	)

	val r = db.writeTeam(t)
	// println(r)

	// val result = db.getTeams(leagueId = Some(1))
	// println(result)

	// db.writeTeam(t)
	// Thread.sleep(5000)

	// val res = db.getTeams(teamId = Some("2000-University of Example"), year = Some(2000))
	// println(res)

}
