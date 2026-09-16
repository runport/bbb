package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.ui.common.PersianUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("کارگاه پوشاک", appName)
  }

  @Test
  fun `verify Persian currency formatting`() {
    val formatted = PersianUtils.formatPrice(1250000.0)
    assertTrue(formatted.contains("تومان"))
    assertTrue(formatted.contains("۱،۲۵۰،۰۰۰"))
  }

  @Test
  fun `verify Persian number conversion`() {
    val formatted = PersianUtils.formatNumber(12345)
    assertEquals("۱۲،۳۴۵", formatted)
  }
}
