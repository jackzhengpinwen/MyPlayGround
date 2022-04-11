//package com.zpw.myplayground
//
//import android.content.Intent
//import android.os.Build
//import android.widget.TextView
//import androidx.test.core.app.ActivityScenario
//import com.zpw.myplayground.robolectric.EmailVerification
//import com.zpw.myplayground.robolectric.MassageStick
//import com.zpw.myplayground.robolectric.RobolectricAActivity
//import com.zpw.myplayground.robolectric.RobolectricActivity
//import org.junit.Assert.*
//import org.junit.Test
//import org.junit.runner.RunWith
//import org.robolectric.Robolectric
//import org.robolectric.RobolectricTestRunner
//import org.robolectric.Shadows.shadowOf
//import org.robolectric.annotation.Config
//
//@Config(sdk = intArrayOf(Build.VERSION_CODES.O_MR1))
//@RunWith(RobolectricTestRunner::class)
//class RobolectricTestClass {
//    @Test
//    fun functionEin() {
//        val emailVerification = EmailVerification()
//        assertEquals(emailVerification.isEmailAddress("ggg@taa.com"), true)
//        assertEquals(emailVerification.isEmailAddress("ggga"), false)
//        assertEquals(emailVerification.isEmailAddress("ggga#/aacc."), false)
//    }
//
//    @Test
//    fun functionEin2() {
//        val controller = Robolectric.buildActivity(RobolectricActivity::class.java).setup()
//        controller.get().findViewById<TextView>(R.id.text_jump).performClick()
//        val intent = Intent(controller.get(), RobolectricAActivity::class.java)
//        val actual = shadowOf(controller.get()).nextStartedActivity
//        assertEquals(intent.component, actual.component)
//    }
//
//    @Test
//    fun functionEin3() {
//        val controller = Robolectric.buildActivity(RobolectricActivity::class.java).setup()
//        controller.resume()
//        assertNotNull(controller.get().listener)
//        controller.pause()
//        assertNull(controller.get().listener)
//    }
//
//    @Test
//    fun functionEin4() {
//        val controller = Robolectric.buildActivity(RobolectricActivity::class.java).setup()
//        val massageStick = MassageStick()
//        assertEquals(massageStick.setStrength(controller.get(), MassageStick.STRONG), "strong")
//        assertEquals(massageStick.setStrength(controller.get(), MassageStick.MEDIUM), "medium")
//        assertEquals(massageStick.setStrength(controller.get(), MassageStick.WEAK), "weak")
////        assertEquals(massageStick.setStrength(controller.get(), 9527), "medium")
//    }
//
//    @Config(sdk = [Build.VERSION_CODES.O])
//    @Test
//    fun functionEin5() {
//        val controller = Robolectric.buildActivity(RobolectricActivity::class.java).setup()
//        assertEquals(controller.get().findViewById<TextView>(R.id.text_jump).text, "Android Device Version is beyond Marshmallow.")
//    }
//
//    @Config(sdk = [Build.VERSION_CODES.LOLLIPOP])
//    @Test
//    fun functionEin6() {
//        val controller = Robolectric.buildActivity(RobolectricActivity::class.java).setup()
//        assertEquals(controller.get().findViewById<TextView>(R.id.text_jump).text, "Android Device Version is under Marshmallow.")
//    }
//}