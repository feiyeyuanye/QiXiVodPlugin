package com.jhr.qixiplugin.components

import android.graphics.Color
import android.graphics.Typeface
import android.util.Log
import com.jhr.qixiplugin.components.Const.layoutSpanCount
import com.jhr.qixiplugin.util.JsoupUtil
import com.su.mediabox.pluginapi.action.CustomPageAction
import com.su.mediabox.pluginapi.action.DetailAction
import com.su.mediabox.pluginapi.action.WebBrowserAction
import com.su.mediabox.pluginapi.components.ICustomPageDataComponent
import com.su.mediabox.pluginapi.data.BaseData
import com.su.mediabox.pluginapi.data.MediaInfo1Data
import com.su.mediabox.pluginapi.data.SimpleTextData
import com.su.mediabox.pluginapi.util.UIUtil.dp

/**
 * FileName: KuaKePageDataComponent
 * Founder: Jiang Houren
 * Create Date: 2023/7/4 10:44
 * Profile: 专题
 */
class SpecialPageDataComponent : ICustomPageDataComponent {

    var hostUrl = Const.host

    override val pageName: String
        get() = "专题"

    override fun initPage(action: CustomPageAction) {
        super.initPage(action)
        hostUrl += action.extraData
    }

    override suspend fun getData(page: Int): List<BaseData>? {
        val charToInsert = "-$page"
        val indexToInsert = hostUrl.length - 5
        val url = StringBuilder(hostUrl).insert(indexToInsert, charToInsert).toString()
//        Log.e("TAG", hostUrl)  // https://www.7xi.tv/topic.html
//        Log.e("TAG", url)  // https://www.7xi.tv/topic-1.html
        val data = mutableListOf<BaseData>()
        val document = JsoupUtil.getDocument(url)

        val content = document.select(".hl-list-wrap")
        content.select("ul").first()?.select("li")?.forEach {
            val itemName = it.select("a").attr("title")
            val itemCover = it.select("a").attr("data-original")
            val itemUrl = it.select("a").attr("href")
            val itemEpisode = it.select(".remarks").text()
            data.add(
                MediaInfo1Data(itemName, Const.host+itemCover, Const.host+itemUrl, itemEpisode ?: "")
                    .apply {
                        spanSize = layoutSpanCount / 2
                        action = CustomPageAction.obtain(SpecialDetailPageDataComponent::class.java)
                        action?.extraData = itemUrl
                    })
        }
        if (data.size>0) data[0].layoutConfig = BaseData.LayoutConfig(layoutSpanCount)
        return data
    }
}