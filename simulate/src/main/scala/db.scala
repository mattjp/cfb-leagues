package sim

import awscala._, dynamodbv2._
import types.{League, Team}

case class Db(tableName: String) {

	implicit val dynamoDB: DynamoDB = DynamoDB.at(Region.NorthernVirginia)
	implicit val table: Table = dynamoDB.table(tableName).get

	def getTeams(
		teamId: Option[String] = None, 
		teamName: Option[String] = None, 
		year: Option[Int] = None
	): Seq[Team] = {

		val filters = Map(
			"teamId" -> teamId,
			"name"   -> teamName,
			"year"   -> year
		)

		val filter = filters.collect { case (k, Some(v)) => k -> cond.eq(v) }.toSeq
		val items: Seq[Item] = table.scan(filter = filter)

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
				initialSpRating <- attributes.get("initialSpRating")
				year            <- attributes.get("year")
				rank            =  attributes.get("rank") // Option
				wins            <- attributes.get("wins")
				loses           <- attributes.get("loses")
				ties            <- attributes.get("ties")
			} yield {
				Team(
					name            = name.getS,
					conference      = conference.getS,
					initialSpRating = initialSpRating.getN.toDouble,
					year            = year.getN.toInt,
					rank            = rank.map(_.getN.toInt),
					wins            = wins.getN.toInt,
					loses           = loses.getN.toInt,
					ties            = ties.getN.toInt
				)
			}
		}

	}

	def writeTeam(team: Team) = {
		table.putItem(team.teamId, team.year, team)
	}

	def deleteTeam(teamId: String) = {
		table.deleteItem(teamId)
	}

}
