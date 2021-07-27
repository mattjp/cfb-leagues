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


	def getGame(teamName: String, year: Int, week: Int): Game = { // take Team/teamId not teamName

		val configJsonStr: String = scala.io.Source.fromFile("../resources/config.json").mkString // TODO -> this is all duplicate
		val secretsJsonStr: String = scala.io.Source.fromFile("../resources/secrets.json").mkString

		val configJsonMap: Map[String, ujson.Value] = ujson.read(configJsonStr).obj.toMap // TODO -> duplicate
		val secretsJsonMap: Map[String, ujson.Value] = ujson.read(secretsJsonStr).obj.toMap

		val baseUrl: String = configJsonMap("base_url").str // TODO -> duplicate
		val gamesEndpoint: String = configJsonMap("games_endpoint").str
		val apiKey: String = secretsJsonMap("api_key").str
		val headers: Map[String, String] = Map("Authorization" -> apiKey)
		
		val gamesUrl: String = s"http://$baseUrl/$gamesEndpoint?year=$year&week=$week&team=$teamName"

		val response = ujson
			.read(requests.get(gamesUrl, headers = headers).text)
			.arr
			.toSeq // this can be empty if no game that week
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
		Game(
			homeTeamId = homeTeamId,
			awayTeamId = awayTeamId,
			homeTeamLeagueId = homeTeamDb.flatMap(_.leagueId),
			awayTeamLeagueId = awayTeamDb.flatMap(_.leagueId),
			homeTeamPoints = homeTeam.get("points").map(_.num.toInt),
			awayTeamPoints = awayTeam.get("points").map(_.num.toInt)
		)
	}


	// Signature could also be teamId, gameId (that probably makes more sense, but I don't wanna set up the DB rn)
	def getPoints(teamId: String, game: Game): Int = {

		Some(teamId) match {
			case game.homeTeamId => 1
			case game.awayTeamId => 2
			case _               => 0 
		}


	}

}

