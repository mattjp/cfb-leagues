package types

case class League(
	id: Int,
	name: String,
	teams: Seq[Team]
) {

	def printTeams(): Unit = {
		println(s"${this.name}")
		for (team <- this.teams) { println(team) }
	}

}
