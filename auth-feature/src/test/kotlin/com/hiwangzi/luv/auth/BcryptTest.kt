package com.hiwangzi.luv.auth

import at.favre.lib.crypto.bcrypt.BCrypt
import io.vertx.core.Vertx
import io.vertx.junit5.VertxExtension
import io.vertx.junit5.VertxTestContext
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.slf4j.LoggerFactory

@ExtendWith(VertxExtension::class)
class BcryptTest {

  private val logger = LoggerFactory.getLogger(BCrypt::class.java)

  @Test
  fun testHashAndVerify(vertx: Vertx, testContext: VertxTestContext) {
    val password = "hello, world"
    val saltRounds = 10

    val time1 = System.currentTimeMillis()
    val hash = BCrypt.withDefaults()
      .hash(saltRounds, password.toByteArray())
    val hashString = String(hash)
    val time2 = System.currentTimeMillis()
    logger.info("Hash time: ${(time2 - time1)} ms")

    val time3 = System.currentTimeMillis()
    val result = BCrypt.verifyer(BCrypt.Version.VERSION_2A)
      .verify(password.toByteArray(), hashString.toByteArray())
    val time4 = System.currentTimeMillis()
    logger.info("Verify time: ${(time4 - time3)} ms")

    Assertions.assertTrue(result.verified)
    testContext.completeNow()
  }
}
