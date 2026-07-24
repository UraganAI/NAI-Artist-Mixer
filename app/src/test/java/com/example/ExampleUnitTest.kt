package com.example

import com.example.model.FormattingOptions
import com.example.model.PictureItem
import com.example.util.NameFormatter
import org.junit.Assert.assertEquals
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun testRandomPictureNameFormatter() {
    val pictures = listOf(
      PictureItem(id = "1", fileName = "kittew.png", uriString = ""),
      PictureItem(id = "2", fileName = "magion02.png", uriString = ""),
      PictureItem(id = "3", fileName = "natsuishi nana.png", uriString = "")
    )
    val options = FormattingOptions(removeExtensions = true, customSeparator = ", ")
    val result = NameFormatter.formatPictureNames(pictures, options)
    assertEquals("kittew, magion02, natsuishi nana", result)
  }
}

