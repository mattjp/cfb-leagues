package types

case class League(
	id: Int,
	name: String,
	teams: Seq[Team]
)
