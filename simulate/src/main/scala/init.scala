package sim

import java.net.{URLEncoder => UrlEncoder}
import requests._
import upickle.default._

import types.{League, Team}

object Init {

	/**
	 * Get all FBS teams for the given year.
	 * Sort all teams based on SP rating.
	 * Divide all teams into leagues of given size.
	 * Return a list of leagues.
	 */
	def initializeLeagues(year: Int, leagueSize: Int): Seq[League] = {
		val configJsonStr: String = scala.io.Source.fromFile("../resources/config.json").mkString
		val secretsJsonStr: String = scala.io.Source.fromFile("../resources/secrets.json").mkString
		val teamsJsonStr: String = scala.io.Source.fromFile("../resources/teams.json").mkString

		val configJsonMap: Map[String, ujson.Value] = ujson.read(configJsonStr).obj.toMap
		val secretsJsonMap: Map[String, ujson.Value] = ujson.read(secretsJsonStr).obj.toMap
		val teamsJsonMap: Map[String, ujson.Value] = ujson.read(teamsJsonStr).obj.toMap

		val baseUrl: String = configJsonMap("base_url").str
		val ratingsEndpoint: String = configJsonMap("ratings_endpoint").str
		val apiKey: String = secretsJsonMap("api_key").str
		val headers: Map[String, String] = Map("Authorization" -> apiKey)

		// val teamNames: Seq[String] = teamsJsonMap("teamNames")
		// 		.arr
		// 		.toSeq
		// 		.map(_.str)

		val teamNames: Seq[String] = Seq("Air Force", "Akron", "Alabama", "Arizona", "Arizona State")

		val teams: Seq[Team] = teamNames.map { teamName =>
			println(teamName)

			val teamNameEncoded: String = UrlEncoder.encode(teamName, "UTF-8")
			val ratingsUrl: String = s"http://$baseUrl/$ratingsEndpoint?year=$year&team=$teamNameEncoded"

			val responseJson: Map[String, ujson.Value] = ujson
					.read(requests.get(ratingsUrl, headers = headers).text) // response - ujson.Value
					.arr   // ujson.Arr
					.toSeq // Seq[ujson.Value]
					.head  // ujson.Value
					.obj   // ujson.Obj
					.toMap // Map[String, ujson.Value]
			
			val spRating: Double = responseJson.getOrElse("rating", ujson.Num(1.0)).num
			val conference: String = responseJson.getOrElse("conference", ujson.Str("FBS Independents")).str

			Team(
				name = teamName,
				conference = conference,
				initialSpRating = spRating,
				year = year
			)
		}

		val teamsSorted: Seq[Team] = teams
				.sortBy(_.initialSpRating)
				.reverse
				.zipWithIndex
				.map { case(t, i) => t.copy(rank = Some(i)) } // Add initial rankings

		val teamsDivided: Seq[Seq[Team]] = splitter(teamsSorted, leagueSize)

		teamsDivided.zipWithIndex.map { case(ts, i) => 
			League(
				id = i,
				name = s"League $i", // todo -> read in from names list?
				teams = ts.map { _.copy(leagueId = Some(i)) } // Set leagueId for each team in the league
			)
		}
	}


	/**
	 * Take an array and divide it into sub-arrays of defined length.
	 * Return an array containing the sub-arrays.
	 */ 
	def splitter[A](as: Seq[A], l: Int): Seq[Seq[A]] = 
		if (as.length < l * 2) Seq(as)
		else {
			val (h, t) = as.splitAt(l)
			Seq(h) ++ splitter(t, l)
		}

}
