package com.victorviale.imacgenealogy.db

import com.victorviale.imacgenealogy.models.Tree

object LTree {
  final case class BranchDescriptor[A](nodes: Seq[A]) {
    def toTree: Tree[A] =
      nodes.foldRight(Tree.Empty(): Tree[A])((node, tree) => Tree.Branch(node, Vector(tree)))
  }

  def fromBranchDescriptors[A](bds: Seq[BranchDescriptor[A]]) = {

  }
}

object BranchDescriptor {
  import com.victorviale.imacgenealogy.db.LTree.BranchDescriptor

  def parse[A](str: String)(f: String => A): Option[BranchDescriptor[A]] = {
    val nodes = str.split(".").toSeq
    if (nodes.toSet.toSeq == nodes)
      Some(LTree.BranchDescriptor(nodes.map(f)))
    else
      None
  }

  def parse(str: String): Option[BranchDescriptor[String]] =
    parse[String](str)(identity)
}