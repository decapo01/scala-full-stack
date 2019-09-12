package com.thatjessicaiknow.server.providers

import org.mindrot.jbcrypt.BCrypt

trait HashProvider {

  def hashPassword(password: String): String
  
  def passwordMatches(password: String,hash: String): Boolean
  
}


class HashProviderImpl extends HashProvider {
  
  override def hashPassword(password: String): String = {
  
    BCrypt.hashpw(password,BCrypt.gensalt())
  }
  
  override def passwordMatches(password: String, hash: String): Boolean = {
  
    BCrypt.checkpw(password,hash)
  }
}
