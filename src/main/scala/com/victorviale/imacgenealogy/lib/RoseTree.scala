package com.victorviale.imacgenealogy.lib

import cats.implicits._
import cats.{Applicative, Eval, Foldable, Functor, Traverse}

object RoseTree {

  /** Forest is a stream of trees. */
  type Forest[A] = Stream[Tree[A]]

  /**
    * Simplified tree.
    * See: http://hackage.haskell.org/package/containers-0.5.10.2/docs/src/Data-Tree.html
    */
  sealed abstract class Tree[A] {
    /** Root node. */
    def value: A

    /** Sub-forest. */
    def subForest: Forest[A]

    /** -- | Lists of nodes at each level of the tree. */
    def levels: Stream[Stream[A]] = {
      val f = (s: Stream[Tree[A]]) => Foldable[Stream].foldMap(s)(_.subForest)
      Stream.iterate(Stream(this))(f).takeWhile(_.nonEmpty).map(_.map(_.value))
    }

    /** wip: fold entire stream. */
    private def squish(xs: Stream[A]): Stream[A] =
      value #:: subForest.foldRight(xs)(_.squish(_))

    /** -- | The elements of a tree in pre-order. */
    def flatten: Stream[A] = squish(Stream.empty)

    /** Apply f to the nodes of the tree. */
    def map[B](f: A => B): Tree[B] =
      Node(f(value), subForest.map(_.map(f)))

    /** Traverse tree */
    def traverse[F[_] : Applicative, B](f: A => F[B]): F[Tree[B]] =
      Applicative[F].map2(
        f(value),
        Traverse[Stream].sequence(subForest.map(_.traverse(f)))
      )(Node(_, _))

    def foldLeft[B](b: B)(f: (B, A) => B): B = flatten.foldLeft(b)(f)

    def foldRight[B](lb: Eval[B])(f: (A, Eval[B]) => Eval[B]): Eval[B] = Foldable[Stream].foldRight(flatten, lb)(f)
  }

  /** Node. */
  case class Node[A](value: A, subForest: Forest[A]) extends Tree[A]

  object implicits {
    implicit val treeTraverse = new Traverse[Tree] {
      override def traverse[G[_]: Applicative, A, B](fa: Tree[A])(f: A => G[B]): G[Tree[B]] =
        fa.traverse(f)

      override def foldLeft[A, B](fa: Tree[A], b: B)(f: (B, A) => B): B = fa.foldLeft(b)(f)

      override def foldRight[A, B](fa: Tree[A], lb: Eval[B])(f: (A, Eval[B]) => Eval[B]): Eval[B] = fa.foldRight(lb)(f)
    }

    implicit val treeFunctor: Functor[Tree] = new Functor[Tree] {
      override def map[A, B](fa: Tree[A])(f: A => B): Tree[B] = fa match {
        case Node(v, branches) => Node(f(v), branches.map(Functor[Tree].map(_)(f)))
      }
    }
  }
}
