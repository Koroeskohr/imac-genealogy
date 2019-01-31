package com.victorviale.imacgenealogy.lib

import org.specs2._

class RoseTreeSpec extends mutable.Specification {
  import RoseTree._
  import RoseTree.Tree._

  val tree1: Tree[Int] = branch(1, branch(2, leaf(3), leaf(4)), branch(5, leaf(6), leaf(7)))

  "depth first search" >> {
    "should go through every leaf before going to next same-level branch" >> {
      tree1.depthFirstSearch.map(_.value).toList must_== List(1, 2, 3, 4, 5, 6, 7)
    }
  }

  "breadth first search" >> {
    "should go through every branch of a level before going deeper" >> {
      tree1.breadthFirstSearch.map(_.value).toList must_== List(1, 2, 5, 3, 4, 6, 7)
    }
  }

}
