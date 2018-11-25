package genetic.engines

import genetic.{OperatorSet, Population}
import genetic.genotype._

trait EvolutionFlow {
  def nextGeneration[G: Join : Modification](ratedPop: Population[Rated[G]],
                                             operators: OperatorSet): Population[G]
}
