package sim

import requests._
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

		val rankingsUrl: String = configJsonMap("base_url").str + configJsonMap("rankings_endpoint").str
		val apiKey: String = "Bearer " + secretsJsonMap("api_key").str

		println(rankingsUrl)
		println(apiKey)


		val headers = Map("Authorization" -> apiKey)
		val params = Map("year" -> 2000)
		val response = requests.get(rankingsUrl, headers = headers, params = params)

		// println(jsonMap("base_url"))
		// println(jsonData.value)
		




		Seq.empty

	}

}

