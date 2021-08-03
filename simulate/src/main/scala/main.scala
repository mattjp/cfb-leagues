package sim

import types.{Game, League, Team}


object Main extends App {

	val year = 2005 // update how this is set

	println(s"Running simulation for $year...")

	// 1. Run initialization of teams and leagues
	// there needs to be a way of doing this by hitting DB not API
	val leagues: Seq[League] = Init.initializeLeagues(2005, 10) // Default 2005, 10

	// 2. Write all teams to DB
	// this doesn't need to happen every time
	val db: Db = Db("teams")
	// leagues.foreach { league => db.writeTeams(league.teams) }


	// val result = db.getTeams(leagueId = Some(1))
	// println(result)

	// db.writeTeam(t)
	

	// for each year

	// get weeks in season

	val maxWeek: Int = Simulate.getWeeks(year)
	val weeks: Seq[Int] = (1 to maxWeek).toSeq
	
	println(weeks)

	leagues.foreach { league => 

		println(s"Simulating league '${league.name}' for year $year")

		// for each team

		league.teams.foreach { team => 

			// for each week

			// for (week <- 1 to maxWeek) {
			val updatedTeam = weeks.foldLeft((team)) { (t, week) => 

				// get game
				val game: Game = Simulate.getGame(
					teamName = t.name, 
					year     = year, 
					week     = week
				)
				println(game)

				// get points for game
				// this should return win/loss/draw as well
				val points: Int = Simulate.getPoints(
					teamId = t.teamId, 
					game   = game
				)
				println(points)

				// update team points
				val z = t.copy(points = t.points + points)
				println(z)
				z
			}


			println(updatedTeam)
			// after all weeks update db
		}

	}

}
