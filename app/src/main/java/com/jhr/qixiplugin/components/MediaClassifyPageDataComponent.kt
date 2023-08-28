package com.jhr.qixiplugin.components

import android.util.Log
import com.jhr.qixiplugin.util.JsoupUtil
import com.jhr.qixiplugin.util.ParseHtmlUtil
import com.su.mediabox.pluginapi.action.ClassifyAction
import com.su.mediabox.pluginapi.components.IMediaClassifyPageDataComponent
import com.su.mediabox.pluginapi.data.BaseData
import com.su.mediabox.pluginapi.data.ClassifyItemData
import com.su.mediabox.pluginapi.util.PluginPreferenceIns
import com.su.mediabox.pluginapi.util.TextUtil.urlDecode
import com.su.mediabox.pluginapi.util.WebUtil
import com.su.mediabox.pluginapi.util.WebUtilIns
import org.jsoup.Jsoup

class MediaClassifyPageDataComponent : IMediaClassifyPageDataComponent {
    var classify : String = "/vodshow/1-----------.html"

    override suspend fun getClassifyItemData(): List<ClassifyItemData> {
        val classifyItemDataList = mutableListOf<ClassifyItemData>()
        //示例：使用WebUtil解析动态生成的分类项
        val cookies = mapOf("cookie" to PluginPreferenceIns.get(JsoupUtil.cfClearanceKey, ""))
        if (!classify.startsWith("http")) classify = Const.host + classify
        Log.e("TAG","classify $classify")
        val document = Jsoup.parse(
            WebUtilIns.getRenderedHtmlCode(
                 classify, loadPolicy = object :
                    WebUtil.LoadPolicy by WebUtil.DefaultLoadPolicy {
                    override val headers = cookies
                    override val userAgentString = Const.ua
                    override val isClearEnv = false
                }
            )
        )
        document.select(".hl-filter-all").select("div[class='hl-filter-wrap hl-navswiper swiper-container-initialized swiper-container-horizontal swiper-container-free-mode']").forEach {
            classifyItemDataList.addAll(ParseHtmlUtil.parseClassifyEm(it))
        }
        document.select("div[class='hl-rb-title hl-site-tabs hl-site-tits hl-text-site']").select("a").forEach { a ->
            classifyItemDataList.add(ClassifyItemData().apply {
                action = ClassifyAction.obtain(
                    a.attr("href"), "筛选", a.text()
                )
            })
        }
        return classifyItemDataList
    }

    override suspend fun getClassifyData(
        classifyAction: ClassifyAction,
        page: Int
    ): List<BaseData> {
        val classifyList = mutableListOf<BaseData>()
        Log.e("TAG", "获取分类数据 ${classifyAction.url}")
        classifyAction.url?.urlDecode() ?.also {
            val str :String = when(it){
                // 全部 href
                "javascript:void(0)" -> {
                    classify
                }
                "/vodtype/1.html" -> {
                    // 修改分类项
                    classify = "/vodshow/1-----------.html"
                    classify
                }
                "/vodtype/2.html" -> {
                    classify = "/vodshow/2-----------.html"
                    classify
                }
                "/vodtype/3.html" -> {
                    classify = "/vodshow/3-----------.html"
                    classify
                }
                "/vodtype/4.html" -> {
                    classify = "/vodshow/4-----------.html"
                    classify
                }
                "/vodtype/25.html" -> {
                    classify = "/vodshow/25-----------.html"
                    classify
                }
                else -> { it }
            }

            // 指定要插入的字符 charToInsert
            val charToInsert = "$page"
            var indexToInsert = str.length - 8
            // 时间选项会插入到字符串末尾
            // https://7xi.tv/vodshow/1--------2---2021.html
            if (str[indexToInsert] != '-') {
                indexToInsert -= 4
            }
            // 使用 StringBuilder 创建一个可变的字符串，调用 insert() 方法将字符插入到指定位置，最后将结果转换回不可变字符串。
            var url = StringBuilder(str).insert(indexToInsert, charToInsert).toString()
            if (!url.startsWith(Const.host)){
                url = Const.host + url
            }
            Log.e("TAG", "获取分类数据 $url")
            JsoupUtil.getDocument(url).also { doc->
                classifyList.addAll(ParseHtmlUtil.parseClassifyEm(doc, url))
            }
        }
        return classifyList
    }
}