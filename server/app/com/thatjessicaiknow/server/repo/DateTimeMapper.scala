package com.thatjessicaiknow.server.repo

import java.sql.Timestamp
import java.time.LocalDateTime

import play.api.db.slick.HasDatabaseConfigProvider
import slick.jdbc.JdbcProfile

trait DateTimeMapper { self: HasDatabaseConfigProvider[JdbcProfile] =>
  
  import profile.api._
  
  implicit def dateTimeMapper = MappedColumnType.base[LocalDateTime,Timestamp](
    
    localDateTime => Timestamp.valueOf(localDateTime),
    timestamp     => timestamp.toLocalDateTime()
  )
}
