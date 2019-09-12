package com.thatjessicaiknow.server.providers

import java.util.UUID

import javax.inject.Inject

trait UUIDProvider {

  def randomUUID: UUID

}


class UUIDProviderDefault @Inject() extends UUIDProvider {

  override def randomUUID: UUID = {

    UUID.randomUUID()
  }
}
