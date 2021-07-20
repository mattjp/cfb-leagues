package types

case class Game(
	// gameId: Int,
	homeTeamId: Option[String]    = None, // this is never None?
	awayTeamId: Option[String]    = None,
	homeTeamLeagueId: Option[Int] = None,
	awayTeamLeagueId: Option[Int] = None,
	homeTeamPoints: Option[Int]   = None,
	awayTeamPoints: Option[Int]   = None
) {

	implicit val gameId: String = s"${this.homeTeamId.getOrElse("NONE")}-${this.awayTeamId.getOrElse("NONE")}"

}
