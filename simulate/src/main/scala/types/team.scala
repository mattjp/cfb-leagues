package types

case class Team(
	name: String,
	conference: String,
	initialSpRating: Double,
	rank: Int,
	wins: Int,
	loses: Int,
	ties: Int
)
