package types

case class League(
	name: String,
	rank: Int,
	teams: Seq[Team]
)
