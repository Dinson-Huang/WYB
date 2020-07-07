package com.zjta.wyb.http.manager

import android.content.Context
import com.google.gson.Gson
import com.zjta.wyb.BuildConfig
import com.zjta.wyb.base.GlobalApplication
import com.zjta.wyb.utils.AESUtils
import com.zjta.wyb.utils.StringUtils
import okhttp3.Cookie
import okhttp3.HttpUrl
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.ArrayList

/**
 *  持久化缓存cookie
 */
class InDiskCookieStore {
    companion object {
        private const val COOKIE_PREFS_FILE_NAME = "Cookies_Prefs"
    }

    private val cookiePrefs = GlobalApplication.getContext()
        .getSharedPreferences(COOKIE_PREFS_FILE_NAME, Context.MODE_PRIVATE)
    private val cookies = HashMap<String, ConcurrentHashMap<String, Cookie>>()

    init {
        //将持久化的cookies缓存到内存中 即map cookies
        cookiePrefs.all.filter { it.key.contains("@") }.forEach {
            val split = it.key.split("@")
            if (!cookies.containsKey(split[1])) cookies[split[1]] = ConcurrentHashMap()

            val sp = cookiePrefs.getString(it.key, "")
            if (sp != null && StringUtils.isNotEmpty(sp)) {
                val cookie = decodeCookie(sp)
                if (cookie.name() != null)
                    cookies[split[1]]!![cookie.name()] = cookie
            }
        }
    }

    /**
     * 添加cookie
     * （本地加内存都存储）
     */
    fun addCookie(url: HttpUrl, cookie: Cookie) {
        val name = getCookieToken(cookie)
        //将cookies缓存到内存中 如果缓存过期 就重置此cookie
        if (cookie.persistent()) {
            if (!cookies.containsKey(url.host())) {
                cookies[url.host()] = ConcurrentHashMap()
            }
            cookies[url.host()]!![cookie.name()] = cookie
            //将cookies持久化到本地
            cookiePrefs.edit().putString(name, AESUtils.encrypt(BuildConfig.APPLICATION_ID, encodeCookie(cookie))).apply()
        } else {
            if (cookies.containsKey(url.host())) {
                cookies[url.host()]!!.remove(cookie.name())
            }
        }
    }


    /**
     * 获取某个域名下的Cookie
     */
    fun getCookies(url: HttpUrl): List<Cookie> {
        val ret = ArrayList<Cookie>()
        if (cookies.containsKey(url.host()))
            ret.addAll(cookies[url.host()]!!.values)
        return ret
    }

    /**
     * 获取某个域名下的Cookie
     */
    fun getCookies(domain: String): List<Cookie> {
        val ret = ArrayList<Cookie>()
        if (cookies.containsKey(domain))
            ret.addAll(cookies[domain]!!.values)
        return ret
    }


    /**
     * 获取所有的cookie
     */
    fun getAllCookies(): List<Cookie> {
        val ret = ArrayList<Cookie>()
        for (key in cookies.keys)
            ret.addAll(cookies[key]!!.values)
        return ret
    }

    /**
     * 删除某个域名下的Cookie
     */
    fun removeCookies(domain: String) {
        //本地删除
        cookiePrefs.all.keys.filter { it.contains("@") && it.split("@")[1] == domain }
            .forEach { cookiePrefs.edit().remove(it).apply() }
        //内存删除
        if (cookies.containsKey(domain)) cookies[domain]?.clear()
    }

    /**
     * 删除所有的Cookie
     */
    fun removeAllCookies() {
        cookies.clear()
        cookiePrefs.edit().clear().apply()
    }

    /**
     * 格式化cookie名称
     */
    private fun getCookieToken(cookie: Cookie): String = cookie.name() + "@" + cookie.domain()

    /**
     * cookies 序列化成 string
     *
     */
    private fun encodeCookie(cookie: Cookie) = Gson().toJson(cookie)

    /**
     * 将字符串反序列化成cookies
     */
    private fun decodeCookie(cookieString: String): Cookie =
        Gson().fromJson(AESUtils.decrypt(BuildConfig.APPLICATION_ID, cookieString), Cookie::class.java)


}