package com.victorviale.imacgenealogy

class StudentsDbSpec extends DbSpec {
  import doobie.implicits._

  val insertValue = sql"INSERT INTO students(id, full_name, promotion_id) VALUES (1, 'Victor Viale', 2017)"
  val selectInserted = sql"SELECT id, full_name, promotion_id, genealogy :: text FROM students WHERE id = 1 LIMIT 1"

  "students" >> {
    "should be insertable without a genealogy" >> {
      val insert = inTransaction(insertValue.update.run).attempt.unsafeRunSync()
      insert must not be left
    }
  }

  "genealogy" >> {
    "should have a default value" >> {

      val trans = for {
        _ <- insertValue.update.run
        (id, _, _, gen) <- selectInserted.query[(Int, String, Int, String)].unique
      } yield (id, gen)

      val (id, gen) = inTransaction[(Int, String)](trans).unsafeRunSync()

      gen must_== id.toString
    }

    "should equal its id when not specified" >> {
      val trans = for {
        _ <- insertValue.update.run
        (id, name, pid, gen) <- selectInserted.query[(Int, String, Int, String)].unique
      } yield (id, name, pid, gen)

      val (id, name, pid, gen) = inTransaction[(Int, String, Int, String)](trans).unsafeRunSync()

      name must_== "Victor Viale"
      pid must_== 2017
      gen must_== id.toString
    }

    "should throw if the same node is present more than once" >> {
      val insertValueWithGenalogy = sql"INSERT INTO students(id, full_name, promotion_id, genealogy) VALUES (1, 'Victor Viale', 2017, '1.2.1')"
      val run = inTransaction(insertValueWithGenalogy.update.run).attempt.unsafeRunSync()
      run.pp must be left
    }
  }

}
