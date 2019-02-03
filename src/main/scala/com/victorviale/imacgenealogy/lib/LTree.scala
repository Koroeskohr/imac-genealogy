package com.victorviale.imacgenealogy
package lib

import cats.Foldable
import cats.data.NonEmptyList

object LTree {
  sealed abstract case class BranchDescriptor[A](nodes: NonEmptyList[A]) {
    def toTree: RoseTree.Tree[A] = nodes match {
      case NonEmptyList(h, Nil) => RoseTree.Node(h, Stream.empty)
      case l @ NonEmptyList(h, _) =>
        Foldable[NonEmptyList].foldLeft(l, RoseTree.Node(h, Stream.empty))((tree, node) => RoseTree.Node(node, Stream(tree)))
    }
  }

  def fromBranchDescriptors[A](bds: Seq[BranchDescriptor[A]]) = {
    bds
  }

  sealed abstract class Error extends Exception {
    def message: String
  }

  final case object DuplicateNodeInTree extends Error { val message = "Node was found twice in the tree" }
  final case object EmptyBranchDescription extends Error { val message = "Branch description was empty, resulting from an illegal state. Must be investigated." }
  final case object UnknownError extends Error { val message = "Unknown error" }
}

object BranchDescriptor {
  import LTree.BranchDescriptor

  def parse[A](str: String)(f: String => A): Either[LTree.Error, BranchDescriptor[A]] = {
    val nodes = str.split(".").toList
    nodes match {
      case Nil =>                                    Left(LTree.EmptyBranchDescription)
      case list @ h :: t if list.distinct == list => Right(new BranchDescriptor(NonEmptyList.of(h, t: _*).map(f)) {})
      case list if list.toSet.toList != nodes =>     Left(LTree.DuplicateNodeInTree)
      case _ =>                                      Left(LTree.UnknownError)
    }
  }

  def parse(str: String): Either[LTree.Error, BranchDescriptor[String]] =
    parse[String](str)(identity)
}