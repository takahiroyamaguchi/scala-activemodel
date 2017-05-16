package com.tierline.scala.activemodel

import com.tierline.scala.activemodel.domain._


class ActiveModelTest extends TestSuite {

  def insert(size: Int) {
    for (i <- 0 until size) new Cart("Note" + i, 1000 * i).save()
  }

  test("Save") {
    val isSaved = new Cart("Note", 1000).save()

    assert(isSaved, "didn't saved")
  }

  test("Delete") {
    val savedEntity = new Cart("Note", 1000).create()
    val id = savedEntity.id
    savedEntity.delete()

    Cart.findById(id) match {
      case Some(g) => assert(false)
      case None => assert(true)
    }
  }

  test("Update") {
    val savedEntity = new Cart("Note", 1000).create()
    val id = savedEntity.id
    savedEntity.name = "new name"
    savedEntity.size = 2000
    savedEntity.update()

    Cart.findById(id) match {
      case Some(g) =>
        assert(g.name == "new name")
        assert(g.size == 2000)
      case None => assert(false)
    }
  }

  test("Update partial field") {
    import org.squeryl.PrimitiveTypeMode._

    val savedEntity = new Cart("Note", 1000).create()
    val id = savedEntity.id
    savedEntity.updatePartial(c => c.name := "new name")

    Cart.findById(id) match {
      case Some(g) =>
        assert(g.name == "new name")
      case None => assert(false)
    }

  }

}