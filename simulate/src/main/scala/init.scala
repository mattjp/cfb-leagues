package sim

import requests._
import io.lemonlabs.uri.{Url, QueryString}
// import scalaj.http._
import upickle.default._

import types.League

object Init {

	def initializeLeagues(year: Int): Seq[League] = {

		// for each team in teams.txt
		//   get their opening season rank for the year
		// subdivide all teams into leagues of 12 teams each

		val configJsonStr: String = scala.io.Source.fromFile("../resources/config.json").mkString
		val secretsJsonStr: String = scala.io.Source.fromFile("../resources/secrets.json").mkString
		val teamsJsonStr: String = scala.io.Source.fromFile("../resources/teams.json").mkString

		val configJsonMap: Map[String, ujson.Value] = ujson.read(configJsonStr).obj.toMap
		val secretsJsonMap: Map[String, ujson.Value] = ujson.read(secretsJsonStr).obj.toMap
		val teamsJsonMap: Map[String, ujson.Value] = ujson.read(teamsJsonStr).obj.toMap

		val teamNames: Seq[String] = teamsJsonMap("teamNames")
				.arr
				.toSeq
				.map(_.str)

		val baseUrl: String = configJsonMap("base_url").str
		val ratingsEndpoint: String = configJsonMap("ratings_endpoint").str
		val apiKey: String = secretsJsonMap("api_key").str
		val headers: Map[String, String] = Map("Authorization" -> apiKey)

		// Create list of Team objs
		// Sort list based on rank
		// for (team <- teams) {

			val teamName: String = teamNames.head

			// this is dumb and ugly but all the libraries i've tried don't take multiple param types
			val teamNameFormatted: String = teamNames.head.replace(" ", "%20")
			// println(teamName)

			val ratingsUrl: String = s"http://$baseUrl/$ratingsEndpoint?year=$year&team=$teamNameFormatted"
			// val ratingsUrl = "http://api.collegefootballdata.com/ratings/sp?year=2000&team=Air%20Force"
			// println(ratingsUrl)

			
			val responseJson: Map[String, ujson.Value] = ujson
					.read(requests.get(ratingsUrl, headers = headers).text)
					.arr
					.toSeq
					.head // ujson.Value
					.obj
					.toMap
			
			println(responseJson)

			// val 

			val spRating: Double = responseJson("ratingz").num // this errors if key not found
			// val conference: String = responseJson.getOrElse("conference", "FBS Independents")
			// println(rating)

			println(spRating)
			// println(conference)

			// val team = Team(
			// 	name = teamName,
			// 	conference = conference,
			// 	initialSpRating = spRating
			// )

			// println(team)

			// val ratingsUrl: String = Url(
			// 	scheme = "http",
			// 	host = configJsonMap("base_url").str,
			// 	path = configJsonMap("ratings_endpoint").str,

			// )


		// }

		// val rankingsUrl: String = Url(
		// 	scheme = "http", 
		// 	host = configJsonMap("base_url").str, 
		// 	path = configJsonMap("ratings").str,
		// 	query = QueryString.fromPairs("year" -> year, "team" -> "team") // this doesn't work with different types - classic
		// ).toString
		
		

		// val params = Map("year" -> 2000) // why do you not accept non-string params???
		// val responseJson = ujson.read(requests.get(rankingsUrl, headers = headers).text)
		// println(responseJson)
		// val response = Http(rankingsUrl)
				// .header("Authorization", apiKey)
				// .param("year", 2000)

		// println(jsonMap("base_url"))
		// println(jsonData.value)
		




		Seq.empty

	}

}

