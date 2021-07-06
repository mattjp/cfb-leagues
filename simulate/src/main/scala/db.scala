package sim

import awscala._, dynamodbv2._
import types.{League, Team}

case class Db(tableName: String) {

	implicit val dynamoDB: DynamoDB = DynamoDB.at(Region.NorthernVirginia)
	implicit val table: Table = dynamoDB.table(tableName).get

	def getTeams(teamId: Option[String] = None, teamName: Option[String] = None, year: Option[Int] = None) = {


		val members = table.scan(Seq("teamId" -> cond.eq(teamId)))
		println(members)
	}

	def writeTeam(team: Team) = {
		table.putItem(team.teamId, team.year, team)
	}

	def deleteTeam(teamId: String) = {
		table.deleteItem(teamId)
	}

}
