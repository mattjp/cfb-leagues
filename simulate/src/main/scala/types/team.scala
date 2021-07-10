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

}
