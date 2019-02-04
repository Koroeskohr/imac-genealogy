package com.victorviale.imacgenealogy

import org.specs2.control.Debug

class StudentsDbSpec extends DbSpec with Debug {
  import doobie.implicits._

  val insertValue = sql"INSERT INTO students(id, full_name, promotion_id) VALUES (1, 'Victor Viale', 2017)"
  val selectInserted = sql"SELECT id, full_name, promotion_id, genealogy :: text FROM students WHERE id = 1 LIMIT 1"

  "students" >> {
    "should be insertable without a genealogy" >> {
      val insert = transactionally(insertValue.update.run).attempt.unsafeRunSync()
      insert must not be left
    }
  }

  "genealogy" >> {
    "should have a default value" >> {

      val trans = for {
        _ <- insertValue.update.run
        (id, _, _, gen) <- selectInserted.query[(Int, String, Int, String)].unique
      } yield (id, gen)

      val (id, gen) = transactionally[(Int, String)](trans).unsafeRunSync()

      gen must_== id.toString
    }

    "should equal its id when not specified" >> {
      val trans = for {
        _ <- insertValue.update.run
        (id, name, pid, gen) <- selectInserted.query[(Int, String, Int, String)].unique
      } yield (id, name, pid, gen)

      val (id, name, pid, gen) = transactionally[(Int, String, Int, String)](trans).unsafeRunSync()

      name must_== "Victor Viale"
      pid must_== 2017
      gen must_== id.toString
    }

    "should throw if the same node is present more than once" >> {
      val insertValueWithGenealogy = sql"INSERT INTO students(id, full_name, promotion_id, genealogy) VALUES (1, 'Victor Viale', 2017, '1.2.1')"
      val run = transactionally(insertValueWithGenealogy.update.run).attempt.unsafeRunSync()
      run must beLeft
      run.left.get.getMessage must contain("has duplicates")
    }

    "should throw if the root node of a genealogy is present in another genealogy tree" >> {
      val insertValue = sql"INSERT INTO students(id, full_name, promotion_id, genealogy) VALUES (1, 'Victor Viale', 2017, '4.5.6.1')"
      // ************************************************************************************************************** ⇙-----⇗
      val insertValue2 = sql"INSERT INTO students(id, full_name, promotion_id, genealogy) VALUES (2, 'Foo Bar', 2017, '6.1.2')"

      val trans = for {
        _ <- insertValue.update.run
        _ <- insertValue2.update.run
      } yield ()

      val run = transactionally(trans).attempt.unsafeRunSync()

      run must beLeft
      run.left.get.getMessage must contain("has a root node which exists as a sub node in another tree")
    }

    "should throw if any of the ids don't exist" >> {
      val insertValue = sql"INSERT INTO students(id, full_name, promotion_id, genealogy) VALUES (1, 'Victor Viale', 2017, '4.5.6.1')"
      val trans = insertValue.update.run

      val run = transactionally(trans).attempt.unsafeRunSync()

      run must beLeft
    }
  }

}
