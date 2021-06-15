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

		val configJsonMap: Map[String, ujson.Value] = ujson.read(configJsonStr).obj.toMap
		val secretsJsonMap: Map[String, ujson.Value] = ujson.read(secretsJsonStr).obj.toMap

		// val teams = 

		val rankingsUrl: String = Url(
			scheme = "http", 
			host = configJsonMap("base_url").str, 
			path = configJsonMap("ratings").str,
			query = QueryString.fromPairs("year" -> year, "team" -> "team")
		).toString
		
		val apiKey: String = secretsJsonMap("api_key").str
		val headers: Map[String, String] = Map("Authorization" -> apiKey)

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

