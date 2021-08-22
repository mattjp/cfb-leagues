package sim

import types.Team

case class Cache(db: Db, year: Int) {

	private val teams: Seq[Team] = db.getTeams(year = Some(year))

	/**
	 * Map team ID to league ID
	 */
	def buildLeagueIdCache(teams: Seq[Team] = teams): Map[String, Option[Int]] = {
		teams
			.map { team => team.teamId -> team.leagueId }
			.toMap
	}

}
