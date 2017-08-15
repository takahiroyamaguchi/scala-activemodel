package com.tierline.scala.activemodel.multitenant.shared.db

import com.tierline.scala.activemodel.ActiveModelSchema
import com.tierline.scala.activemodel.multitenant.shared.{ActiveModelTest, Multitenancy}

import scala.concurrent.{Await, ExecutionContext, Future}
import ExecutionContext.Implicits.global
import com.tierline.scala.activemodel.multitenant.domain._
import org.squeryl.PrimitiveTypeMode._

import scala.concurrent.duration.Duration

class MysqlActiveModelTransactionTest extends ActiveModelTest {
  override val schema: ActiveModelSchema = MysqlSharedSchema

  test("other thread transaction") {
    inTransaction {
      Multitenancy.currentTenant.withValue(mainTenant.tenantId) {
        val mainThreadId = Thread.currentThread().getId
        new Channel().create()
        new Channel().create()
        new Channel().create()

        Await.result(Future {
          val subThreadId = Thread.currentThread().getId

          val list = Channel.all
          println(list.size)

          assert(mainThreadId != subThreadId)
        }, Duration.Inf)
      }
    }
  }
}
