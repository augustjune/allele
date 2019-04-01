package alleles.environment.sequential

import alleles.Population
import alleles.environment.Ranking
import cats.Functor
// ToDo - add documentation
object SeqRanking extends {
  private val sequentialFunctor = new Functor[Population] {
    def map[A, B](fa: Population[A])(f: A => B): Population[B] = fa.map(f)
  }
} with Ranking(sequentialFunctor)
