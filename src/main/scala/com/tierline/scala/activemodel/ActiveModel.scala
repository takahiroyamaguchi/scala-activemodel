package com.tierline.scala.activemodel

import org.squeryl._
import org.squeryl.PrimitiveTypeMode._
import org.squeryl.dsl.ast.{LogicalBoolean, UpdateAssignment}
import grizzled.slf4j.Logging
import com.tierline.scala.activemodel.util.Companion
import org.squeryl.annotations.Transient

trait ActiveModel extends ActiveModelBase[Long] {

  override val idWhereClause: ActiveModelBase[Long] => LogicalBoolean = { a => a.id === id }

  @Transient
  protected lazy val repository = companion.asInstanceOf[Repository[_]]

}

trait StringKeyActiveModel extends ActiveModelBase[String] {

  override val idWhereClause: ActiveModelBase[String] => LogicalBoolean = { a => a.id === id }

  @Transient
  protected lazy val repository = companion.asInstanceOf[StringKeyRepository[_]]

}

trait ActiveModelCRUD {
  def save(): Boolean

  def create(): this.type

  def delete(): Boolean

  def update(): this.type

  def updatePartial(func: this.type => UpdateAssignment): this.type
}

trait ActiveModelBase[K] extends KeyedEntity[K] with Logging with ActiveModelCRUD {

  protected val repository: RepositoryBase[K, _]

  @Transient
  protected lazy val companion = Companion.of[this.type].getOrElse(throw new IllegalAccessException(s"Can't get companion object"))
  @Transient
  protected lazy val schema = repository.schema
  @Transient
  protected lazy val schemaName = repository.schemaName
  @Transient
  protected lazy val tableName = repository.tableName
  @Transient
  lazy val table: Table[ActiveModelBase[K]] = repository.table.asInstanceOf[Table[ActiveModelBase[K]]]

  protected val idWhereClause: ActiveModelBase[K] => LogicalBoolean

  override def save(): Boolean = inTransaction {
    table.insert(this) != null
  }

  override def create(): this.type = inTransaction {
    table.insert(this)
    this
  }

  override def delete(): Boolean = inTransaction {
    table.delete(this.id)
  }

  override def update(): this.type = inTransaction {
    table.update(this)
    this
  }


  override def updatePartial(func: this.type => UpdateAssignment): this.type = {
    executeReturnThis(s"Update Partial") { e =>
      table.update(e => where(idWhereClause(e)) set func(e.asInstanceOf[this.type]))
    }
  }
  
  protected def executeReturnThis(debugMessage: String)(query: (this.type) => Unit): this.type = {
    val isSuccess = execute(debugMessage)(e => query(this))
    if (isSuccess) this else throw new ActiveModelException(s"Failure $debugMessage $tableName at $schemaName")
  }

  protected def execute(debugMessage: String)(query: (this.type) => Unit): Boolean = inTransaction {
    var isSuccess = false
    try {
      query(this)
      isSuccess = true
      debug(s"$debugMessage $tableName[ID:$id] by $schemaName")
    } catch {
      case e: Throwable => {
        info("[Exception] at " + debugMessage)
        isSuccess = false
        if (e != null && e.getMessage != null) debug(e.getMessage)
      }
    }
    isSuccess
  }


}
