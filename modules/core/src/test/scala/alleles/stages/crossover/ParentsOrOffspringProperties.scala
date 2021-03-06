package alleles.stages.crossover

import alleles.GenotypeImplicits
import alleles.genotype.Join
import alleles.genotype.syntax._
import alleles.stages.{CrossoverProperties, CrossoverStrategy}
import alleles.toolset.IterablePair
import org.scalacheck.Arbitrary._
import org.scalacheck.Gen._
import org.scalacheck.Prop._
import org.scalacheck._


object ParentsOrOffspringProperties extends CrossoverProperties("ParentsOrOffspring props") {
  type Ind = String

  val implGen: Gen[CrossoverStrategy[Ind]] =
    choose[Double](0.0, 1.0).map(CrossoverStrategy.parentsOrOffspring(_))

  val gPairGen: Gen[(Ind, Ind)] = arbTuple2[String, String].arbitrary

  implicit val join: Join[Ind] = GenotypeImplicits[String].join

  property("Either parents or offspring") = forAll(gPairGen, implGen) {
    case ((p1, p2), crossover) =>
      val parents = new IterablePair(p1, p2)
      val offspring = p1 >< p2
      val result = crossover.pair(p1, p2)
      result == parents || result == offspring
  }

  /**
    * Generator with parents chance 1.00 or more
    */
  val parentsBiasedGen: Gen[CrossoverStrategy[Ind]] =
    sized(n => choose(1.0, math.max(n, 1.0))).map(CrossoverStrategy.parentsOrOffspring(_))

  property("100% parents") = forAll(gPairGen, parentsBiasedGen) {
    case ((p1, p2), crossover) =>
      val result = crossover.pair(p1, p2)
      result ?= new IterablePair(p1, p2)
  }

  /**
    * Generator with parents chance 0.00 or less
    */
  val offspringBiasedGen: Gen[CrossoverStrategy[Ind]] =
    sized(n => choose(-n, 0.0)).map(CrossoverStrategy.parentsOrOffspring(_))

  property("0% parents") = forAll(gPairGen, offspringBiasedGen) {
    case ((p1, p2), crossover) =>
      val result = crossover.pair(p1, p2)
      val offspring = p1 >< p2
      result ?= offspring
  }
}
