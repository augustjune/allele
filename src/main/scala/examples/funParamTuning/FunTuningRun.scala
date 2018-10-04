package examples.funParamTuning

import genetic.engines.sync.ParallelGA
import genetic.genotype.{Fitness, Join, Modification, Scheme}
import genetic.genotype.syntax._
import genetic.operators.crossover.ParentsOrOffspring
import genetic.operators.mutation.RepetitiveMutation
import genetic.operators.selection.Tournament
import genetic.{OperatorSet, Population, PopulationExtension, RRandom}
import concurrent.duration._

object FunTuningRun extends App {

  object Genotype {
    implicit val join: Join[Fun] = Join.commutative {
      case (Fun(args1), Fun(args2)) => Fun(args1.zipAll(args2, 0.0, 0.0).map { case (a, b) => (a + b) / 2 })
    }

    implicit val scheme: Scheme[Fun] = Scheme.fromOne(Fun(Vector.fill(4)(0)))

    def round(x: Double, dec: Int): Double = {
      val prec = math.pow(10, dec)
      (x * prec).toInt / prec
    }

    implicit val modification: Modification[Fun] = {
      case Fun(args) =>
        val idx = RRandom.nextInt(args.size)
        Fun(args.updated(idx, if (RRandom.nextBoolean) args(idx) + round(RRandom.nextGaussian(), 2) else round(args(idx), 2)))
    }

    def calcFitness(values: Map[Double, Double]): Fitness[Fun] = (fun: Fun) => {
      values.foldLeft(0.0) { case (sum, (x, y)) => sum + math.abs(fun(x) - y) }
    }
  }

  import Genotype._

  val secretFun = Fun(Vector(5, -2, 1, 4))

  def createValues(fun: Fun, n: Int): Map[Double, Double] = (for (_ <- 1 to n) yield {
    val x = RRandom.nextDouble * 10
    x -> fun(x)
  }).toMap

  val inputValues = createValues(secretFun, 1000)
  implicit val fitness = calcFitness(inputValues)
  val operators = OperatorSet(Tournament(10), ParentsOrOffspring(0.25), RepetitiveMutation(0.7, 0.4))

  val population: Population[Fun] = ParallelGA.createAndEvolve(100, operators, 10 seconds)
  val best: Fun = population.best

  println(s"Best cost f: " + best.fitness)
  println(best)
  inputValues.foreach { case (x, y) => println(s"f($x) = ${best(x)}| expected: $y | false: ${math.abs(y - best(x)) / y * 100}%") }
}