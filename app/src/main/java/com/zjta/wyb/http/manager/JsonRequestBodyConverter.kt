package com.zjta.wyb.http.manager

import com.google.gson.Gson
import com.google.gson.TypeAdapter
import com.zjta.wyb.kotlin.logi
import okhttp3.MediaType
import okhttp3.RequestBody
import retrofit2.Converter

/**
 * 自定义请求RequestBody
 */
class JsonRequestBodyConverter<T>(gson: Gson, private val adapter: TypeAdapter<T>) : Converter<T, RequestBody> {
    companion object {
        private val MEDIA_TYPE = MediaType.parse("application/json; charset=UTF-8")
    }

    override fun convert(value: T): RequestBody {
        logi("POST: " + value.toString(), showLine = false)
        val postBody = adapter.toJson(value)//对象转化成json
        return RequestBody.create(MEDIA_TYPE, postBody)
    }
}
