package types

case class Team(
	name: String,
	conference: String,
	initialSpRating: Double,
	year: Int,
	rank: Option[Int] = None,
	wins: Int = 0,
	loses: Int = 0,
	ties: Int = 0
)
