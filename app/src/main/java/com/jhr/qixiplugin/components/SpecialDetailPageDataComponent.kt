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
import com.su.mediabox.pluginapi.data.MediaInfo2Data
import com.su.mediabox.pluginapi.data.SimpleTextData
import com.su.mediabox.pluginapi.util.UIUtil.dp

/**
 * FileName: KuaKePageDataComponent
 * Founder: Jiang Houren
 * Create Date: 2023/7/4 10:44
 * Profile: 专题详情
 */
class SpecialDetailPageDataComponent : ICustomPageDataComponent {

    var hostUrl = Const.host

    override val pageName: String
        get() = "专题详情"

    override fun initPage(action: CustomPageAction) {
        super.initPage(action)
        hostUrl += action.extraData
    }

    override suspend fun getData(page: Int): List<BaseData>? {
        if (page!=1) return null
        val data = mutableListOf<BaseData>()
        val document = JsoupUtil.getDocument(hostUrl)

        val item2Name = document.select("title").text().substringBefore("_")
        val item2Cover = document.select("span[class='hl-item-thumb hl-lazy']").first()?.attr("data-original")
        data.add(
            MediaInfo2Data(
                item2Name, Const.host+item2Cover, "", "","")
                .apply {
                    layoutConfig = BaseData.LayoutConfig(layoutSpanCount)
                    spanSize = layoutSpanCount
                })

        document.select(".hl-list-wrap").first()?.also {
            it.select("ul").first()?.select("li")?.forEach {
                val itemName = it.select("a").attr("title")
                val itemCover = it.select("a").attr("data-original")
                val itemUrl = it.select("a").attr("href")
                val itemEpisode = it.select(".remarks").text()
                data.add(
                    MediaInfo1Data(itemName, Const.host+itemCover, Const.host+itemUrl, itemEpisode ?: "")
                        .apply {
                            spanSize = layoutSpanCount / 3
                            action = DetailAction.obtain(itemUrl)
                        })
            }
        }
        return data
    }
}