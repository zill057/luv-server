package com.hiwangzi.luv.feature

import java.awt.Color
import java.awt.Image
import java.awt.image.BufferedImage

class ProfilePhotoCombiner {

  private val targetSize = 500

  fun combineImages(image1: BufferedImage, image2: BufferedImage): BufferedImage {
    val scaledSize = 240
    val paddingX = (targetSize - scaledSize * 2) / 3
    val paddingY = (targetSize - scaledSize) / 2

    val scaledImg1 = image1.getScaledInstance(scaledSize, scaledSize, Image.SCALE_SMOOTH)
    val scaledImg2 = image2.getScaledInstance(scaledSize, scaledSize, Image.SCALE_SMOOTH)

    val combined = BufferedImage(targetSize, targetSize, BufferedImage.TYPE_INT_RGB)
    val graphics = combined.createGraphics()
    graphics.color = Color.white
    graphics.fillRect(0, 0, targetSize, targetSize)
    graphics.drawImage(scaledImg1, paddingX, paddingY, null)
    graphics.drawImage(scaledImg2, paddingX + scaledSize + paddingX, paddingY, null)
    graphics.dispose()
    return combined
  }

  fun combineImages(image1: BufferedImage, image2: BufferedImage, image3: BufferedImage): BufferedImage {
    val scaledSize = 240
    val paddingX1stRow = (targetSize - scaledSize) / 2
    val paddingX2ndRow = (targetSize - scaledSize * 2) / 3
    val paddingY = (targetSize - scaledSize * 2) / 3

    val scaledImg1 = image1.getScaledInstance(scaledSize, scaledSize, Image.SCALE_SMOOTH)
    val scaledImg2 = image2.getScaledInstance(scaledSize, scaledSize, Image.SCALE_SMOOTH)
    val scaledImg3 = image3.getScaledInstance(scaledSize, scaledSize, Image.SCALE_SMOOTH)

    val combined = BufferedImage(targetSize, targetSize, BufferedImage.TYPE_INT_RGB)
    val graphics = combined.createGraphics()
    graphics.color = Color.white
    graphics.fillRect(0, 0, targetSize, targetSize)
    graphics.drawImage(scaledImg1, paddingX1stRow, paddingY, null)
    graphics.drawImage(scaledImg2, paddingX2ndRow, paddingY + scaledSize + paddingY, null)
    graphics.drawImage(
      scaledImg3,
      paddingX2ndRow + scaledSize + paddingX2ndRow,
      paddingY + scaledSize + paddingY,
      null
    )
    graphics.dispose()
    return combined
  }

  fun combineImages(
    image1: BufferedImage,
    image2: BufferedImage,
    image3: BufferedImage,
    image4: BufferedImage
  ): BufferedImage {
    val scaledSize = 240
    val paddingX = (targetSize - scaledSize * 2) / 3
    val paddingY = paddingX

    val scaledImg1 = image1.getScaledInstance(scaledSize, scaledSize, Image.SCALE_SMOOTH)
    val scaledImg2 = image2.getScaledInstance(scaledSize, scaledSize, Image.SCALE_SMOOTH)
    val scaledImg3 = image3.getScaledInstance(scaledSize, scaledSize, Image.SCALE_SMOOTH)
    val scaledImg4 = image4.getScaledInstance(scaledSize, scaledSize, Image.SCALE_SMOOTH)

    val combined = BufferedImage(targetSize, targetSize, BufferedImage.TYPE_INT_RGB)
    val graphics = combined.createGraphics()
    graphics.color = Color.white
    graphics.fillRect(0, 0, targetSize, targetSize)
    graphics.drawImage(scaledImg1, paddingX, paddingY, null)
    graphics.drawImage(scaledImg2, paddingX + scaledSize + paddingX, paddingY, null)
    graphics.drawImage(
      scaledImg3,
      paddingX,
      paddingY + scaledSize + paddingY,
      null
    )
    graphics.drawImage(
      scaledImg4,
      paddingX + scaledSize + paddingX,
      paddingY + scaledSize + paddingY,
      null
    )
    graphics.dispose()
    return combined
  }
}
