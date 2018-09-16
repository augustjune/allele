package genetic.operators.selection

import genetic.operators.Selection
import genetic._
import genetic.genotype.Fitness


object Roulette {
  def apply[G: Fitness]: Roulette[G] = new Roulette()
}

/**
  * Fitness proportionate selection of selection size equal to previous population size,
  * built on relation of genotype fitness value to maximum fitness value in population.
  * Individuals with smaller fitness value have bigger chance to end up in next population.
  * Example:
  *   Population with corresponding fitnesses:
  *     x1 -> 6
  *     x2 -> 2
  *     x3 -> 10
  *     x4 -> 7
  *     x5 -> 9
  *   Max fitness = 10
  *   Priority boundaries = (10 - 6) + (10 - 2) + (10 - 10) + (10 - 7) + (10 - 9) = 16
  *   Individual chances:
  *     x1 -> (10 - 6) / 16 = 25%
  *     x2 -> (10 - 2) / 16 = 50%
  *     x3 -> (10 - 10) / 16 = 0%
  *     x4 -> (10 - 7) / 16 = 18.75%
  *     x5 -> (10 - 9) / 16 = 6.25%
  *
  * Note: chromosomes with the largest fitness value will never enter the next pop
  */
class Roulette[G: Fitness] extends Selection[G] {
  def apply(population: Population[G]): Population[G] = {
    val fitnesses = population.map(g => g -> Fitness(g))
    val largestFitness = fitnesses.map(_._2).max

    val sectors = fitnesses.map { case (g, f) => g -> (largestFitness - f)}

    for(_ <- population) yield RRandom.chooseByPriorities(sectors)
  }
}
