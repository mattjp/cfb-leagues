package sim

import java.net.{URLEncoder => UrlEncoder}
import requests._
import upickle.default._

import types.{Game, League, Team}

object Simulate {

	val db: Db = Db("teams")

	def getWeeks(year: Int): Int = {

		val configJsonStr: String = scala.io.Source.fromFile("../resources/config.json").mkString // TODO -> this is all duplicate
		val secretsJsonStr: String = scala.io.Source.fromFile("../resources/secrets.json").mkString

		val configJsonMap: Map[String, ujson.Value] = ujson.read(configJsonStr).obj.toMap // TODO -> duplicate
		val secretsJsonMap: Map[String, ujson.Value] = ujson.read(secretsJsonStr).obj.toMap

		val baseUrl: String = configJsonMap("base_url").str // TODO -> duplicate
		val calendarEndpoint: String = configJsonMap("calendar_endpoint").str
		val apiKey: String = secretsJsonMap("api_key").str
		val headers: Map[String, String] = Map("Authorization" -> apiKey)

		val calendarUrl: String = s"http://$baseUrl/$calendarEndpoint?year=$year"

		ujson
			.read(requests.get(calendarUrl, headers = headers).text)
			.arr
			.toSeq
			.filter { _.obj.toMap.get("seasonType") == Some(ujson.Str("regular")) } // Postseason?
			.length
	}


	def getGame(teamName: String, year: Int, week: Int): Option[Game] = { // take Team/teamId not teamName

		val configJsonStr: String = scala.io.Source.fromFile("../resources/config.json").mkString // TODO -> this is all duplicate
		val secretsJsonStr: String = scala.io.Source.fromFile("../resources/secrets.json").mkString

		val configJsonMap: Map[String, ujson.Value] = ujson.read(configJsonStr).obj.toMap // TODO -> duplicate
		val secretsJsonMap: Map[String, ujson.Value] = ujson.read(secretsJsonStr).obj.toMap

		val baseUrl: String = configJsonMap("base_url").str // TODO -> duplicate
		val gamesEndpoint: String = configJsonMap("games_endpoint").str
		val apiKey: String = secretsJsonMap("api_key").str
		val headers: Map[String, String] = Map("Authorization" -> apiKey)
		
		val teamNameEncoded: String = UrlEncoder.encode(teamName, "UTF-8")
		val gamesUrl: String = s"http://$baseUrl/$gamesEndpoint?year=$year&week=$week&team=$teamNameEncoded"

		val responseSeq = ujson
			.read(requests.get(gamesUrl, headers = headers).text) // this can be empty if bye week
			.arr
			.toSeq // this can be empty if no game that week, probably needs to return Option[Game]

		if (responseSeq.isEmpty) return None
			
		val response = responseSeq
			.head
			.obj
			.toMap

		val teams: Option[Seq[Map[String, ujson.Value]]] = response
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

		// turn teams into home/away maps
		// TODO -> everything is duplicated so it should probably be broken in functions
		val homeTeam = teams
			.map { ts =>
				ts.filter { t =>
					t.get("homeAway") == Some(ujson.Str("home"))
				}.head
			}.get // TODO -> do this safely

		val awayTeam = teams
			.map { ts =>
				ts.filter { t =>
					t.get("homeAway") == Some(ujson.Str("away"))
				}.head 
			}.get // TODO -> do this safely

		val homeTeamId: Option[String] = homeTeam.get("school").map(h => s"${year.toString}-${h.str}")
		val awayTeamId: Option[String] = awayTeam.get("school").map(a => s"${year.toString}-${a.str}")

		val homeTeamDb: Option[Team] = db.getTeams(teamId = homeTeamId, limit = 1).headOption
		val awayTeamDb: Option[Team] = db.getTeams(teamId = awayTeamId, limit = 1).headOption
		
		// This is clumsy but it will work?
		Some(Game( // TODO -> Now that this func is returning Option, this is dumb
			homeTeamId = homeTeamId,
			awayTeamId = awayTeamId,
			homeTeamLeagueId = homeTeamDb.flatMap(_.leagueId),
			awayTeamLeagueId = awayTeamDb.flatMap(_.leagueId),
			homeTeamPoints = homeTeam.get("points").map(_.num.toInt),
			awayTeamPoints = awayTeam.get("points").map(_.num.toInt)
		))
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


	// def simulateSeason()

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
			val teamsSorted: Seq[Team] = league.teams.sortBy(_.points).reverse

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

}
