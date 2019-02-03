package com.victorviale.imacgenealogy.lib

import cats.data.NonEmptyList
import cats.implicits._
import cats.{Applicative, Eval, Foldable, Functor, Monad, Traverse}
import com.victorviale.imacgenealogy.lib.RoseTree.Tree.Error.NoCommonValueError

object RoseTree {

  /** Forest is a stream of trees. */
  type Forest[A] = Stream[Tree[A]]

  /**
    * Simplified tree.
    * See: http://hackage.haskell.org/package/containers-0.6.0.1/docs/Data-Tree.html
    */
  sealed abstract class Tree[A] {
    import Tree._

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

    def flatMap[B](f: A => Tree[B]): Tree[B] = {
      val r: Tree[B] = f(value)
      Node(r.value, r.subForest #::: subForest.map(_.flatMap(f)))
    }

    /** Traverse tree */
    def traverse[F[_]: Applicative, B](f: A => F[B]): F[Tree[B]] =
      Applicative[F].map2(
        f(value),
        Traverse[Stream].sequence(subForest.map(_.traverse(f)))
      )(Node(_, _))

    def foldLeft[B](b: B)(f: (B, A) => B): B = flatten.foldLeft(b)(f)

    def foldRight[B](lb: Eval[B])(f: (A, Eval[B]) => Eval[B]): Eval[B] = Foldable[Stream].foldRight(flatten, lb)(f)

    /** Evaluates every leaf. Non-terminating on endless streams */
    def depthFirstSearch: Stream[Tree[A]] =
      this #:: subForest.flatMap(_.depthFirstSearch)

    /** Evaluates every branch layer. Non-terminating on endless streams
      * but could have a max depth to prevent stack overflow */
    def breadthFirstSearch: Stream[Tree[A]] = {
      def go(s: Stream[Tree[A]]): Stream[Tree[A]] = {
        if (s.isEmpty) s
        else s.head #:: go(s.tail #::: s.head.subForest)
      }

      go(Stream(this))
    }

    def append(nel: NonEmptyList[Tree[A]]): Tree[A] = Node(nel.head.value, nel.tail.toStream)
    def append(tree: Tree[A], trees: Tree[A]*): Tree[A] = append(NonEmptyList.of(tree, trees: _*))
    def append(tree: Tree[A]): Tree[A] = append(NonEmptyList.one(tree))

    def find(a: A): Option[Tree[A]] = breadthFirstSearch.find(_.value == a)

    def mergeOnValueLossy(other: Tree[A]): Tree[A] = find(other.value).map(append).getOrElse(this)

    def mergeOnValue(other: Tree[A]): Either[MergeError, Tree[A]] = {
      find(other.value).map(append).toRight(NoCommonValueError)
    }
  }

  /** Node. */
  case class Node[A](value: A, subForest: Forest[A]) extends Tree[A]

  object Tree {
    def leaf[A](a: A) = Node(a, Stream.empty)
    def branch[A](a: A, t: Tree[A]*) = Node(a, Stream(t: _*))

    def unfold[T, R](init: T)(f: T => Option[(R, T)]): Stream[R] = f(init) match {
      case None => Stream[R]()
      case Some((r,v)) => r #:: unfold(v)(f)
    }

    sealed abstract class Error(val message: String) extends Exception
    sealed abstract class MergeError(override val message: String) extends Error(message)
    object Error {
      final case object NoCommonValueError extends MergeError("No value could be found on `this` tree ")
    }
  }

  object implicits {
    implicit val treeTraverse: Traverse[Tree] = new Traverse[Tree] {
      override def traverse[G[_]: Applicative, A, B](fa: Tree[A])(f: A => G[B]): G[Tree[B]] =
        fa.traverse(f)

      override def foldLeft[A, B](fa: Tree[A], b: B)(f: (B, A) => B): B = fa.foldLeft(b)(f)

      override def foldRight[A, B](fa: Tree[A], lb: Eval[B])(f: (A, Eval[B]) => Eval[B]): Eval[B] = fa.foldRight(lb)(f)
    }

    implicit val treeFunctor: Functor[Tree] = new Functor[Tree] {
      override def map[A, B](fa: Tree[A])(f: A => B): Tree[B] = fa.map(f)
    }

    implicit val treeMonad: Monad[Tree] = new Monad[Tree] {
      override def pure[A](x: A): Tree[A] = Tree.leaf(x)

      override def flatMap[A, B](fa: Tree[A])(f: A => Tree[B]): Tree[B] = fa.flatMap(f)

      // welp, good run
      override def tailRecM[A, B](a: A)(f: A => Tree[Either[A, B]]): Tree[B] = ???
    }
  }
}
