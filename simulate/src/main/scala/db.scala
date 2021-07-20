package sim

import awscala._, dynamodbv2._
import types.{League, Team}

case class Db(tableName: String) {

	implicit val dynamoDB: DynamoDB = DynamoDB.at(Region.NorthernVirginia)
	implicit val table: Table = dynamoDB.table(tableName).get

	def getTeams(
		teamId: Option[String] = None,
		leagueId: Option[Int] = None,
		teamName: Option[String] = None, 
		year: Option[Int] = None,
		limit: Int = 50
	): Seq[Team] = {

		val filters = Map(
			"teamId"   -> teamId,
			"leagueId" -> leagueId,
			"name"     -> teamName,
			"year"     -> year
		)

		val filter = filters.collect { case (k, Some(v)) => k -> cond.eq(v) }.toSeq
		val items: Seq[Item] = table.scan(filter = filter, limit = limit)

		// Flatten out missing Items
		items.flatMap { item =>
			val attributes = item
				.attributes
				.map { attribute => attribute.name -> attribute.value }
				.toMap
			

			// If any attribute is missing, skip that Item
			for {
				name            <- attributes.get("name")
				conference      <- attributes.get("conference")
				leagueId        =  attributes.get("leagueId") // Option
				initialSpRating <- attributes.get("initialSpRating")
				year            <- attributes.get("year")
				rank            =  attributes.get("rank") // Option
				wins            <- attributes.get("wins")
				loses           <- attributes.get("loses")
				ties            <- attributes.get("ties")
				points          <- attributes.get("points")
			} yield {
				Team(
					name            = name.getS,
					conference      = conference.getS,
					leagueId        = leagueId.map(_.getN.toInt),
					initialSpRating = initialSpRating.getN.toDouble,
					year            = year.getN.toInt,
					rank            = rank.map(_.getN.toInt),
					wins            = wins.getN.toInt,
					loses           = loses.getN.toInt,
					ties            = ties.getN.toInt,
					points          = points.getN.toInt
				)
			}
		}

	}


	def writeTeam(team: Team) = {
		table.put(team.teamId, team.year, team.unpack: _*) // This does not write nulls
	}


	def writeTeams(teams: Seq[Team]) = {
		teams.foreach { team => writeTeam(team) }
	}


	def deleteTeam(teamId: String) = {
		table.deleteItem(teamId)
	}

}
