package genetic.engines.parallel

import genetic.engines.{Evolution, Rated}
import genetic.genotype.{Join, Modification}
import genetic.{OperatorSet, Population}

import scala.collection.parallel.immutable.ParVector

object ParallelEvolution$ extends Evolution {
  def nextGeneration[G: Join : Modification](ratedPop: Population[Rated[G]],
                                             operators: OperatorSet): Population[G] = operators match {
    case OperatorSet(selection, crossover, mutation) =>
      ParVector.fill(ratedPop.size / 2)(())
        .map(_ => selection.single(ratedPop))
        .flatMap(crossover.single(_))
        .map(mutation.single(_))
        .seq
  }
}