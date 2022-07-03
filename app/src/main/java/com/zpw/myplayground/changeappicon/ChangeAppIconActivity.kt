package com.zpw.myplayground.changeappicon

import android.content.ComponentName
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity

class ChangeAppIconActivity: AppCompatActivity() {
    var isChange = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    fun doClick(view: View) {
        var name = "com.zpw.myplayground"
        val pm = packageManager
        if (isChange){//第二次进来isChange会被赋值为true，所以需要点击两次才会恢复成原来的icon,真正需求的话需要缓存起来，而不是成员变量
            isChange = false
            //关注 COMPONENT_ENABLED_STATE_DISABLED和COMPONENT_ENABLED_STATE_ENABLED
            pm.setComponentEnabledSetting(ComponentName(this,"com.zpw.myplayground.changeappicon.ChangeAppIconActivity"), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP)
            pm.setComponentEnabledSetting(ComponentName(this,name), PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP)
        } else {
            pm.setComponentEnabledSetting(ComponentName(this,"com.zpw.myplayground.changeappicon.ChangeAppIconActivity"), PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP)
            pm.setComponentEnabledSetting(ComponentName(this,name), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP)
        }
    }
}