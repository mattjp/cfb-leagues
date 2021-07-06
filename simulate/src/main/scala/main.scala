package sim

import types.{League, Team}


object Main extends App {

	println("Running simulation")

	// val l = League("league 1", 1, Seq.empty)
	// println(l)


	// 1. run initialization of leagues and return all leagues
	// val leagues: Seq[League] = Init.initializeLeagues(2000, 10)

	val db = Db("teams")
	// db.init()

	val t = Team(
		name = "University of Example",
		conference = "FBS Independents",
		initialSpRating = 5.67,
		year = 2000
	)

	// db.writeTeam(t)
	// Thread.sleep(5000)

	db.getTeams(teamId = "2000-University of Example")

	// db.deleteTeam(t)

	// Db().init()




}
