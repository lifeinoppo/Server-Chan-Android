package com.zhihaofans.server_chan_3rd_party

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.InputType
import com.zhihaofans.server_chan_3rd_party.util.ServerChanUtil
import com.zhihaofans.server_chan_3rd_party.util.SettingUtil
import com.zhihaofans.server_chan_3rd_party.view.AboutActivity
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick

class MainActivity : AppCompatActivity(), AnkoLogger {
    private val serverChan = ServerChanUtil()
    private val set = SettingUtil()
    private var sendByPost = false
    private var muitiLine = false
    var lastTitle = ""
    var lastDesp = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        set.setContext(this@MainActivity)
        val loading = indeterminateProgressDialog(title = "Loading...")
        init()
        loading.hide()
        b_about.onClick {
            startActivity<AboutActivity>()
        }
        b_setting.onClick {
            val countries = listOf(getString(R.string.text_singleLine), getString(R.string.text_multiLineText), getString(R.string.text_sendByGET), getString(R.string.text_sendByPOST), "SCKEY")
            selector(getString(R.string.text_setting), countries, { _, i ->
                when (i) {
                    0 -> {
                        et_text.setText("")
                        muitiLine = false
                        et_text.inputType = InputType.TYPE_CLASS_TEXT
                        et_text.singleLine = true
                        et_text.hint = getString(R.string.text_singleLine) + getString(R.string.text_text)
                        toast(R.string.text_singleLine)
                    }
                    1 -> {
                        et_text.setText("")
                        muitiLine = true
                        et_text.inputType = InputType.TYPE_TEXT_FLAG_MULTI_LINE
                        et_text.singleLine = false
                        et_text.hint = getString(R.string.text_multiLineText) + getString(R.string.text_text)
                        toast(R.string.text_multiLineText)
                    }
                    2 -> {
                        sendByPost = false
                        buttonSwitch()
                        set.setBoolean("sendByPost", sendByPost)
                        toast(R.string.text_sendByGET)
                    }
                    3 -> {
                        sendByPost = true
                        buttonSwitch()
                        set.setBoolean("sendByPost", sendByPost)
                        toast(R.string.text_sendByPOST)
                    }
                    4 -> {
                        setSCKEY()
                    }
                    else -> toast("error")
                }
            })
        }
        b_send.onClick {
            if (serverChan.getKey().isEmpty()) {
                alert(getString(R.string.error_noEnterKey), "Error") {
                    yesButton {
                        setSCKEY()
                    }
                }.show()
            } else {
                if (muitiLine && !sendByPost) {
                    alert(getString(R.string.error_noMultiByGet), "Error").show()
                } else {
                    set.setStr("lastTitle", et_title.text.toString())
                    set.setStr("lastDesp", et_text.text.toString())
                    loading.show()
                    var re: Int
                    doAsync {
                        re = if (sendByPost) {
                            serverChan.post(et_title.text.toString(), et_text.text.toString())
                        } else {
                            serverChan.get(et_title.text.toString(), et_text.text.toString())
                        }
                        uiThread {
                            debug(re)
                            loading.hide()
                            val errStr = serverChan.getError(re)
                            alert(errStr, getString(R.string.text_finish)) { yesButton { } }.show()
                        }
                    }
                }
            }
        }
    }

    private fun init() {
        serverChan.setKey(set.getStr("SCKEY", ""))
        sendByPost = set.getBoolean("sendByPost", false)
        muitiLine = set.getBoolean("muitiLine", false)
        lastTitle = set.getStr("lastTitle", "")
        lastDesp = set.getStr("lastDesp", "")
        if (muitiLine) {
            et_text.inputType = InputType.TYPE_TEXT_FLAG_MULTI_LINE
            et_text.singleLine = false
            et_text.hint = getString(R.string.text_multiLineText) + getString(R.string.text_text)
        } else {
            et_text.inputType = InputType.TYPE_CLASS_TEXT
            et_text.singleLine = true
            et_text.hint = getString(R.string.text_singleLine) + getString(R.string.text_text)
        }
        et_title.setText(lastTitle)
        et_text.setText(lastDesp)
        buttonSwitch()
        et_text.hint = getString(R.string.text_singleLine) + getString(R.string.text_text)
    }

    @SuppressLint("SetTextI18n")
    private fun buttonSwitch() {
        val t = if (sendByPost) {
            "POST"
        } else {
            "GET"
        }
        b_send.text = getString(R.string.text_send) + "($t)"
    }

    private fun setSCKEY() {
        alert {
            customView {
                verticalLayout {
                    val et = editText {
                        hint = "av123456"
                        title = getString(R.string.text_title)
                        inputType = InputType.TYPE_TEXT_FLAG_MULTI_LINE
                        setText(set.getStr("SCKEY", ""))
                    }
                    positiveButton(R.string.text_save) {
                        serverChan.setKey(et.text.toString())
                        if (set.setStr("SCKEY", et.text.toString())) {
                            toast("Ok")
                        } else {
                            toast("发生错误，SCKEY无法保存到本地，关闭APP后需重新输入")
                        }
                    }
                }
            }
        }.show()
    }
}
