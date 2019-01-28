package com.victorviale.imacgenealogy

import org.specs2.specification.BeforeEach
import shapeless.{HNil, ::}

class StudentsDbSpec extends DbSpec with BeforeEach {
  import doobie.implicits._

  val insertValue = sql"INSERT INTO students(id, full_name, promotion_id) VALUES (1, 'Victor Viale', 2017)"
  val selectInserted = sql"SELECT id, full_name, promotion_id, genealogy :: text FROM students WHERE id = 1 LIMIT 1"

  "students" >> {
    "should be insertable without a genealogy" >> {
      val insert = insertValue.update.run.transact(xa).attempt.unsafeRunSync()
      insert must not be left
    }
  }

  "genealogy" >> {
    "should have a default value" >> {
      insertValue.update.run.transact(xa).attempt.unsafeRunSync()
      val id :: _ :: _ :: gen :: HNil = selectInserted.query[Int :: String :: Int :: String :: HNil].unique.transact(xa).unsafeRunSync()

      gen must_== id.toString
    }

    "should equal its id when not specified" >> {
      insertValue.update.run.transact(xa).attempt.unsafeRunSync()
      val id :: name :: pid :: gen :: HNil = selectInserted.query[Int :: String :: Int :: String :: HNil].unique.transact(xa).unsafeRunSync()

      name must_== "Victor Viale"
      pid must_== 2017
      gen must_== id.toString
    }
  }

  def before = {
    sql"TRUNCATE students;".update.run.transact(xa).unsafeRunSync()
  }
}
