package types

import types.Team

case class League(
	name: String,
	rank: Int,
	teams: Seq[Team]
)
