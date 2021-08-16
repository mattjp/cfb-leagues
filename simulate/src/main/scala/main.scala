package sim

import types.{Game, League, Team}


object Main extends App {

	val year = 2005 // update how this is set
	val leagueSize = 10
	val n = 3

	println(s"Running simulation for $year...")

	// 1. Run initialization of teams and leagues
	// there needs to be a way of doing this by hitting DB not API
	// val leagues: Seq[League] = Init.initializeLeaguesFromApi(year, leagueSize) // Default 2005, 10
	val leagues: Seq[League] = Init.initializeLeaguesFromDb(year, leagueSize)
	// println(leagues)

	// 2. Write all teams to DB
	// this doesn't need to happen every time
	val db: Db = Db("teams")
	// leagues.foreach { league => db.writeTeams(league.teams) }


	val updatedLeagues: Seq[League] = Simulate.promoteAndRelegate(leagues, n)

	for (league <- updatedLeagues) {
		println(s"League ${league.id}")
		for (team <- league.teams) {
			println(team)
		}
		println()
	}
	

	
	// simulate 1 season for all teams
	// TODO -> this probably belongs in simulate.scala
	// val maxWeek: Int = Simulate.getWeeks(year)
	// val weeks: Seq[Int] = (1 to maxWeek).toSeq
	
	// leagues.foreach { league => 

	// 	println(s"Simulating league '${league.name}' for year $year")

	// 	// for each team
	// 	league.teams.foreach { team => 

	// 		// for each week
	// 		val updatedTeam: Team = weeks.foldLeft((team)) { (t, week) => 

	// 			// get game
	// 			val gameOpt: Option[Game] = Simulate.getGame(
	// 				teamName = t.name, 
	// 				year     = year, 
	// 				week     = week
	// 			)

	// 			println(gameOpt)

	// 			gameOpt match {
	// 				case Some(game) => {

	// 					// get points for game
	// 					// TODO -> this should return win/loss/draw as well
	// 					val points: Int = Simulate.getPoints(
	// 						teamId = t.teamId, 
	// 						game   = game
	// 					)

	// 					println(points)

	// 					// update team points
	// 					t.copy(points = t.points + points)	
	// 				}

	// 				// no game that week
	// 				case None => t
	// 			}

				
	// 		}

	// 		println(updatedTeam)

	// 		// after all weeks update db
	// 		db.writeTeam(updatedTeam)

	// 	}

	// }

}
