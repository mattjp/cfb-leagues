package sim

import types.{League, Team}


object Main extends App {

	println("Running simulation")

	val l = League("league 1", 1, Seq.empty)
	println(l)


	// 1. run initialization of leagues and return all leagues
	val leagues: Seq[League] = Init.initializeLeagues(2000)




}
