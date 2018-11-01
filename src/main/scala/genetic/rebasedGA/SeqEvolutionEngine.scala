package genetic.rebasedGA

import akka.NotUsed
import akka.stream.scaladsl.Source
import genetic._
import genetic.genotype._

import scala.collection.parallel.immutable.ParVector

trait EvolutionEngine {
  def evolve[G: Fitness : Join : Modification](initial: Population[G], operators: OperatorSet): Source[Population[G], NotUsed] =
    Source.repeat(()).scan(initial) {
      case (prev, _) => evolutionStep(evalFitnesses(prev), operators)
    }

  def evalFitnesses[G: Fitness](population: Population[G]): Population[(G, Double)]

  def evolutionStep[G: Join : Modification](scoredPop: Population[(G, Double)],
                                            operators: OperatorSet): Population[G]

  def withBest = new BestTrackingEvolutionEngine(this)
}

object SeqEvolutionEngine extends EvolutionEngine {
  def par: ParallelEvolutionEngine.type = ParallelEvolutionEngine

  def evalFitnesses[G: Fitness](population: Population[G]): Population[(G, Double)] =
    population.map(g => g -> Fitness(g))

  def evolutionStep[G: Join : Modification](scoredPop: Population[(G, Double)],
                                            operators: OperatorSet): Population[G] =
    operators.mutation.generation(
      operators.crossover.generation(
        operators.selection.generation(scoredPop)
      )
    )
}

object ParallelEvolutionEngine extends EvolutionEngine {
  def evalFitnesses[G: Fitness](population: Population[G]): Population[(G, Double)] =
    population.par.map(g => g -> Fitness(g)).seq

  def evolutionStep[G: Join : Modification](scoredPop: Population[(G, Double)],
                                            operators: OperatorSet): Population[G] = operators match {
    case OperatorSet(selection, crossover, mutation) =>
      ParVector.fill(scoredPop.size / 2)(())
        .map(_ => selection.single(scoredPop))
        .flatMap(crossover.single(_))
        .map(mutation.single(_))
        .seq
  }
}

class BestTrackingEvolutionEngine(inner: EvolutionEngine) {
  def evolve[G: Fitness : Join : Modification](initial: Population[G],
                                               operators: OperatorSet): Source[PopulationWithBest[G], NotUsed] =
    Source.repeat(()).scan((initial, (initial.head, Double.MaxValue))) {
      case ((prev, prevBest), _) =>
        val withFitnesses = inner.evalFitnesses(prev)
        (inner.evolutionStep(withFitnesses, operators), (prevBest +: withFitnesses).minBy(_._2))
    }
}


