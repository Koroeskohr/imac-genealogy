package com.victorviale.imacgenealogy.models

import cats._
import cats.implicits._

sealed abstract class Tree[A] extends Product with Serializable {
  def flatten: Vector[A] = {
    def squish(tree: Tree[A], xs: Vector[A]): Vector[A] = 
      Vector(tree.rootLabel, Foldable[Vector].foldRight(tree.subForest, xs)(squish(_, _)))

    squish(this, Vector.empty)
  }

  def traverse[F[_]: Applicative, B](f: A => F[B]): F[Tree[B]] = this match {
    case Tree.Empty() =>
      Applicative[F].pure(Tree.Empty())
    case Tree.Branch(v, branches) =>
      Applicative[F].map2(
        f(v),
        Traverse[Vector].sequence(
          branches.map(_.traverse(f))
        )
      )(Tree.Branch(_, _))
  }

  def foldLeft[A, B](b: B)(f: (B, A) => B): B = this match {
    case Tree.Empty() => b
    case Tree.Branch(value, branches) =>
      f(b,
        branches.foldLeft[A](value)((b: A, branch: Tree[A]) => foldLeft(branch, b)(f))  )
  }
}

object Tree {
  final case class Branch[A](value: A, branches: Vector[Tree[A]]) extends Tree[A]

  object implicits {
    implicit val treeTraverse = new Traverse[Tree] {
      override def traverse[G[_]: Applicative, A, B](fa: Tree[A])(f: A => G[B]): G[Tree[B]] =
        fa.traverse(f)

      override def foldLeft[A, B](fa: Tree[A], b: B)(f: (B, A) => B): B = ???

      override def foldRight[A, B](fa: Tree[A], lb: Eval[B])(f: (A, Eval[B]) => Eval[B]): Eval[B] = ???
    }

    implicit val treeFunctor: Functor[Tree] = new Functor[Tree] {
      override def map[A, B](fa: Tree[A])(f: A => B): Tree[B] = fa match {
        case Empty() => Empty()
        case Branch(v, branches) => Tree.Branch(f(v), branches.map(Functor[Tree].map(_)(f)))
      }
    }
  }

}
