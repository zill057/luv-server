package com.hiwangzi.luv.feature

import io.vertx.core.Vertx
import io.vertx.junit5.VertxExtension
import io.vertx.junit5.VertxTestContext
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.io.File
import javax.imageio.ImageIO

@ExtendWith(VertxExtension::class)
class ProfilePhotoCombinerTest {

  @Test
  fun testCombine2Images(vertx: Vertx, testContext: VertxTestContext) {
    val image1 = ImageIO.read(File("src/test/resources/profile-photos/0.jpg"))
    val image2 = ImageIO.read(File("src/test/resources/profile-photos/1.jpg"))
    val image = ProfilePhotoCombiner().combineImages(image1, image2)
    ImageIO.write(image, "JPG", File("build/tmp/combined-2-images.jpg"))
    testContext.completeNow()
  }

  @Test
  fun testCombine3Images(vertx: Vertx, testContext: VertxTestContext) {
    val image1 = ImageIO.read(File("src/test/resources/profile-photos/0.jpg"))
    val image2 = ImageIO.read(File("src/test/resources/profile-photos/1.jpg"))
    val image3 = ImageIO.read(File("src/test/resources/profile-photos/2.jpg"))
    val image = ProfilePhotoCombiner().combineImages(image1, image2, image3)
    ImageIO.write(image, "JPG", File("build/tmp/combined-3-images.jpg"))
    testContext.completeNow()
  }

  @Test
  fun testCombine4Images(vertx: Vertx, testContext: VertxTestContext) {
    val image1 = ImageIO.read(File("src/test/resources/profile-photos/0.jpg"))
    val image2 = ImageIO.read(File("src/test/resources/profile-photos/1.jpg"))
    val image3 = ImageIO.read(File("src/test/resources/profile-photos/2.jpg"))
    val image4 = ImageIO.read(File("src/test/resources/profile-photos/3.jpg"))
    val image = ProfilePhotoCombiner().combineImages(image1, image2, image3, image4)
    ImageIO.write(image, "JPG", File("build/tmp/combined-4-images.jpg"))
    testContext.completeNow()
  }
}
