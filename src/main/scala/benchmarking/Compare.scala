package benchmarking

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import benchmarking.Measuring.Measured
import examples.qap.{Permutation, PermutationOps}
import genetic._
import genetic.engines.{CompositeDriver, EvolutionOptions, GeneticAlgorithm}
import genetic.genotype.Scheme
import genetic.genotype.syntax._
import genetic.operators.crossover.ParentsOrOffspring
import genetic.operators.mutation.RepetitiveMutation
import genetic.operators.selection.Tournament

import scala.concurrent.duration._
import scala.language.postfixOps

object Compare extends App {

  implicit val system = ActorSystem()
  implicit val mat = ActorMaterializer()

  val implicits = new PermutationOps("http://anjos.mgi.polymtl.ca/qaplib/data.d/had20.dat")

  import implicits._

  val initialPopSize = 50
  val initialPop = Scheme.make(initialPopSize)
  val operators = OperatorSet(
    Tournament(20),
    ParentsOrOffspring(0.25),
    RepetitiveMutation(0.8, 0.5))

  val options = EvolutionOptions(initialPop, operators)
  val iterations = 10

  val comparison = new LongRunningComparison[Permutation, Unit] {
    def candidates: List[(String, CompositeDriver)] = List(
      "Basic sync" -> GeneticAlgorithm,
      "Fully parallel" -> GeneticAlgorithm.par,
      "Parallel fitness" -> GeneticAlgorithm.parFitness
    )

    val evolutionPreferences: EvolutionPreferences[Permutation] = EvolutionPreferences(options, iterations)

    val resultMapper: (String, Measured[Population[Permutation]]) => Unit = {
      case (label, measured) => measured.run match {
        case (time, pop) =>
          println(f"$label%-30s : Time: $time with size: ${pop.size} and best candidate: ${pop.best.fitness}")
      }
    }

    override val restTime: FiniteDuration = 3 seconds
  }

  //  comparison.run
  val vec = Vector.fill(1000)(RRandom.nextInt)
  val m = new Measuring {}
   val mapF: Int => Int = x => {
    val start = System.currentTimeMillis()
    while(start + 5 > System.currentTimeMillis()) {}
    x
  }

  println(s"Mapping function: ${m.measure(mapF(0)).written}")
  println(s"Sequential approach ${m.measure(vec.map(mapF)).written}")
  println(s"Parallel approach ${m.measure(vec.par.map(mapF).seq).written}")
  system.terminate()
}
