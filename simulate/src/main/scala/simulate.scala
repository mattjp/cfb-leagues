package sim

import java.net.{URLEncoder => UrlEncoder}
import requests._
import upickle.default._

import types.{Game, League, Team}

object Simulate {

	val db: Db = Db("teams")

	/**
	 * TODO - take Team/teamId not teamName
	 */
	def getGames(teamName: String, year: Int): Seq[Game] = {

		val configJsonStr: String = scala.io.Source.fromFile("../resources/config.json").mkString // TODO -> this is all duplicate
		val secretsJsonStr: String = scala.io.Source.fromFile("../resources/secrets.json").mkString

		val configJsonMap: Map[String, ujson.Value] = ujson.read(configJsonStr).obj.toMap // TODO -> duplicate
		val secretsJsonMap: Map[String, ujson.Value] = ujson.read(secretsJsonStr).obj.toMap

		val baseUrl: String = configJsonMap("base_url").str // TODO -> duplicate from init and getWeeks
		val gamesEndpoint: String = configJsonMap("games_endpoint").str
		val apiKey: String = secretsJsonMap("api_key").str
		val headers: Map[String, String] = Map("Authorization" -> apiKey)
		
		val teamNameEncoded: String = UrlEncoder.encode(teamName, "UTF-8")
		val gamesUrl: String = s"http://$baseUrl/$gamesEndpoint?year=$year&team=$teamNameEncoded"

		val responses = ujson
			.read(requests.get(gamesUrl, headers = headers).text)
			.arr
			.toSeq

		val allTeams: Seq[Seq[Map[String, ujson.Value]]] = responses
			.flatMap { response =>
				response
					.obj
					.toMap
					.get("teams")
					.map { teams =>
						teams
							.arr
							.toSeq
							.map { team =>
								team
									.obj
									.toMap
									.filterKeys { key => 
										Set("homeAway", "school", "points").contains(key) 
									}
							}
					}
			}

		// reduce number of DB calls 
		val cache: Cache = Cache(db, year)
		val leagueIdCache: Map[String, Option[Int]] = cache.buildLeagueIdCache()

		// TODO -> everything is duplicated so it should probably be broken in functions
		allTeams.map { teams =>
			val homeTeam = teams
				.filter { t => t.get("homeAway") == Some(ujson.Str("home")) }
				.head

			val awayTeam = teams
				.filter { t => t.get("homeAway") == Some(ujson.Str("away")) }
				.head

			val homeTeamId: Option[String] = homeTeam.get("school").map(h => s"${year.toString}-${h.str}")
			val awayTeamId: Option[String] = awayTeam.get("school").map(a => s"${year.toString}-${a.str}")
			
			// This is clumsy but it will work?
			Game(
				homeTeamId = homeTeamId,
				awayTeamId = awayTeamId,
				homeTeamLeagueId = leagueIdCache.get(homeTeamId.getOrElse("")).flatten,
				awayTeamLeagueId = leagueIdCache.get(awayTeamId.getOrElse("")).flatten,
				homeTeamPoints = homeTeam.get("points").map(_.num.toInt),
				awayTeamPoints = awayTeam.get("points").map(_.num.toInt)
			)
		}
	}


	// Signature could also be teamId, gameId (that probably makes more sense, but I don't wanna set up the DB rn)
	def getPoints(teamId: String, game: Game): Int = {
		val hPoints = game.homeTeamPoints.getOrElse(0)
		val aPoints = game.awayTeamPoints.getOrElse(0)
		val pointMargin = (hPoints - aPoints).abs

		Some(teamId) match {

			// returning score for home team
			case game.homeTeamId => {
				// win @ home
				if (hPoints > aPoints) {
					scoreGame(
						leagueIdOpt    = game.homeTeamLeagueId,
						oppLeagueIdOpt = game.awayTeamLeagueId,
						pointMargin    = pointMargin,
						scoreFcs       = (p: Int) => if (p > 14) 0 else -1,
						scoreInferior  = (p: Int) => if (p > 21) 3 else  2,
						scoreEqual     = (p: Int) => if (p > 14) 4 else  3,
						scoreSuperior  = (p: Int) => if (p > 14) 5 else  4
					)
				// loss @ home
				} else {
					scoreGame(
						leagueIdOpt    = game.homeTeamLeagueId,
						oppLeagueIdOpt = game.awayTeamLeagueId,
						pointMargin    = pointMargin,
						scoreFcs       = (pm: Int) => if (pm > 14) -6 else -4,
						scoreInferior  = (pm: Int) => if (pm > 14) -3 else -2,
						scoreEqual     = (pm: Int) => 0,
						scoreSuperior  = (pm: Int) => if (pm > 14)  0 else  1
					)
				}
			}

			// returning score for away team
			case game.awayTeamId => {
				// win @ away
				if (aPoints > hPoints) {
					scoreGame(
						leagueIdOpt    = game.awayTeamLeagueId,
						oppLeagueIdOpt = game.homeTeamLeagueId,
						pointMargin    = pointMargin,
						scoreFcs       = (pm: Int) => if (pm > 14) 0 else -1,
						scoreInferior  = (pm: Int) => if (pm > 21) 3 else  2,
						scoreEqual     = (pm: Int) => if (pm > 14) 5 else  4,
						scoreSuperior  = (pm: Int) => if (pm > 14) 7 else  6
					)
				// loss @ away
				} else {
					scoreGame(
						leagueIdOpt    = game.awayTeamLeagueId,
						oppLeagueIdOpt = game.homeTeamLeagueId,
						pointMargin    = pointMargin,
						scoreFcs       = (p: Int) => if (p > 14) -4 else -2,
						scoreInferior  = (p: Int) => if (p > 14) -2 else -1,
						scoreEqual     = (p: Int) => if (p > 14)  0 else  1,
						scoreSuperior  = (p: Int) => if (p > 14)  1 else  2
					)
				}
			}

			// this should never happen unless it's a tie
			case _ => 0 
		}
	}

	/**
	 * Return score for game based on league
	 */ 
	def scoreGame(
		leagueIdOpt: Option[Int],
		oppLeagueIdOpt: Option[Int],
		pointMargin: Int,
		scoreFcs: Int => Int,
		scoreInferior: Int => Int, 
		scoreEqual: Int => Int, 
		scoreSuperior: Int => Int
	) = {
		(leagueIdOpt, oppLeagueIdOpt) match {

			// both teams are FBS
			case (Some(leagueId), Some(oppLeagueId)) => {

				// opponent is in inferior league
				if (leagueId < oppLeagueId) scoreInferior(pointMargin)

				// opponent is in same league
				else if (leagueId == oppLeagueId) scoreEqual(pointMargin)

				// opponent is in superior league
				else scoreSuperior(pointMargin)
			}

			// opponent is FCS
			case (Some(leagueId), None) => scoreFcs(pointMargin)

			// this should never happen
			case _ => 0 
		}
	}


	/**
	 * Simulate one (1) season for all teams in all leagues
	 */
	def simulateSeason(leagues: Seq[League], year: Int): Seq[League] = {
		leagues.map { league => 
			println(s"=> Simulating league '${league.name}' for year $year")

			val updatedTeams: Seq[Team] = league.teams.map { team =>
				println(s"=> ${team.name}")

				// get all games for the given team for the given year
				val games: Seq[Game] = getGames(
					teamName = team.name, 
					year     = year
				)

				// get league points for all games
				val seasonPoints: Int = games.foldLeft(0) { (points, game) =>
					val p: Int = getPoints(
						teamId = team.teamId,
						game   = game
					)

					println(s"=> ${game.awayTeamId.getOrElse("None")} (${game.awayTeamPoints.getOrElse("")}) @ ${game.homeTeamId.getOrElse("None")} (${game.homeTeamPoints.getOrElse("")}) :: $p")
					points + p
				}

				println(s"=> Final points: $seasonPoints\n")
				team.copy(points = seasonPoints)
			}

			league.copy(teams = updatedTeams)
		}
	}

	/**
	 * Update the year for each team in each league.
	 */
	def updateYear(leagues: Seq[League], newYear: Int): Seq[League] = {
		leagues.map { league =>
			league.copy(teams = league.teams.map { _.copy(year = newYear) })
		}
	}

	/**
	 * For each league, relegate the bottom 3 teams to the league below. 
	 * Additionally, promote the top 3 teams to the league above.
	 * Return the new leagues.
	 */
	def promoteAndRelegate(leagues: Seq[League], n: Int): Seq[League] = {

		(0 until leagues.size).toSeq.foldLeft((Seq[League]())) { (ls, i) =>
			// get bottom 3 teams from superior league
			val relegatedTeams: Option[Seq[Team]] = leagues
				.lift(i-1)
				.map { _.teams.sortBy(_.points).reverse.takeRight(n) }

			// get top 3 teams from inferior league
			val promotedTeams: Option[Seq[Team]] = leagues
				.lift(i+1)
				.map { _.teams.sortBy(_.points).reverse.take(n) }

			val league: League = leagues(i)
			val teamsSorted: Seq[Team] = league.teams.sortBy(_.points).reverse // update sortBy to account for total points

			// replace top 3 teams with bottom 3 teams from L-1 (if it exists), write to L'
			val updatedTeamsSorted: Seq[Team] = 
				if (relegatedTeams.isDefined) relegatedTeams.get ++ teamsSorted.drop(n)
				else teamsSorted
			
			// replace bottom 3 teams with top 3 teams from L+1 (if it exists), write to L'
			val newTeams: Seq[Team] = 
				if (promotedTeams.isDefined) updatedTeamsSorted.dropRight(n) ++ promotedTeams.get
				else updatedTeamsSorted

			ls :+ league.copy(teams = newTeams)
		}

	}


	/**
	 * Sort teams in each league by the amount of points they earned last season.
	 * Update each teams rank as well as their leagueId. 
	 */
	def sortAndRankLeagues(leagues: Seq[League], leagueSize: Int): Seq[League] = {

		// sort teams in each league
		val sortedLeagues = leagues.map { league => 
			league.copy(teams = league.teams.sortBy(_.points).reverse)
		}

		// copy each team with updated rank and leagueId
		sortedLeagues
			.zipWithIndex
			.map { case (league, leagueId) => 
				league.copy(teams = league
					.teams
					.zipWithIndex
					.map { case (team, teamRank) => 
						team.copy(
							rank = Some(teamRank + (leagueId * leagueSize)),
							leagueId = Some(leagueId)
						)
					}
				)
			}
	}


	/**
	 * Set points equal to 0 for all teams in all leagues
	 */
	def resetPoints(leagues: Seq[League]): Seq[League] = {
		leagues.map { league =>
			league.copy(teams = league.teams.map { _.copy(points = 0) })
		}
	}

}
