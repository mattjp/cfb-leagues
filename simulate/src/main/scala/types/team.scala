package types

case class Team(
	name: String,
	conference: String,
	leagueId: Option[Int] = None,
	initialSpRating: Double,
	year: Int,
	rank: Option[Int] = None,
	wins: Int = 0,
	loses: Int = 0,
	ties: Int = 0,
	points: Int = 0
) {

	implicit val teamId: String = s"${this.year}-${this.name}"

	implicit val unpack = Seq(
		"name"            -> this.name,
		"conference"      -> this.conference,
		"leagueId"        -> this.leagueId.getOrElse(null),
		"initialSpRating" -> this.initialSpRating,
		"year"            -> this.year,
		"rank"            -> this.rank.getOrElse(null),
		"wins"            -> this.wins,
		"loses"           -> this.loses,
		"ties"            -> this.ties,
		"points"          -> this.points
	) 

}
