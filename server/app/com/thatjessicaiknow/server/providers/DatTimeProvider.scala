package com.thatjessicaiknow.server.providers

import java.time.LocalDateTime

trait DateTimeProvider {

  def now : LocalDateTime

}

class DateTimeProviderImpl extends DateTimeProvider {


  def now : LocalDateTime = LocalDateTime.now()

}
