package com.jhr.qixiplugin.util

import android.util.Log
import com.jhr.qixiplugin.components.Const.host
import com.jhr.qixiplugin.components.Const.layoutSpanCount
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import com.su.mediabox.pluginapi.action.ClassifyAction
import com.su.mediabox.pluginapi.action.DetailAction
import com.su.mediabox.pluginapi.data.*
import com.su.mediabox.pluginapi.util.UIUtil.dp
import java.net.URL

object ParseHtmlUtil {

    fun getCoverUrl(cover: String, imageReferer: String): String {
        return when {
            cover.startsWith("//") -> {
                try {
                    "${URL(imageReferer).protocol}:$cover"
                } catch (e: Exception) {
                    e.printStackTrace()
                    cover
                }
            }
            cover.startsWith("/") -> {
                //url不全的情况
                host + cover
            }
            else -> cover
        }
    }

    /**
     * 解析搜索的元素
     * @param element ul的父元素
     */
    fun parseSearchEm(
        element: Element,
        imageReferer: String
    ): List<BaseData> {
        val videoInfoItemDataList = mutableListOf<BaseData>()

        val lpic = element.select(".hl-list-wrap").select("ul").first()?:return videoInfoItemDataList
        val results: Elements = lpic.select("li")
        for (i in results.indices) {
            var cover = results[i].select(".hl-item-thumb").attr("data-original")
            if (imageReferer.isNotBlank())
                cover = getCoverUrl(cover, imageReferer)
            val title = results[i].select(".hl-item-thumb").attr("title")
            val url = results[i].select(".hl-item-thumb").attr("href")
            val episode = results[i].select(".remarks").text() + " [七喜影院]"
            val tags = mutableListOf<TagData>()
            val tag = results[i].select("p[class='hl-item-sub hl-lc-1']")
            tags.add(TagData(tag.select("span").text()))
            tags.add(TagData(tag.select("em").text()))
            tag.first()?.ownText()?.split("·")?.forEach {
                if (it.isNotBlank()) tags.add(TagData(it))
            }
            val describe = results[i].select("p[class='hl-item-sub hl-text-muted hl-lc-2']").text()
            val item = MediaInfo2Data(
                title, cover, host + url, episode, describe, tags
            ).apply {
                    action = DetailAction.obtain(url)
                }
            videoInfoItemDataList.add(item)
        }
        return videoInfoItemDataList
    }
    /**
     * 解析分类下的元素
     * @param element ul的父元素
     */
    fun parseClassifyEm(
        element: Element,
        imageReferer: String
    ): List<BaseData> {
        val videoInfoItemDataList = mutableListOf<BaseData>()
        val results: Elements = element.select("ul[class='hl-vod-list clearfix']").select("li")
        for (i in results.indices) {
            val title = results[i].select(".hl-item-thumb").attr("title")
            var cover = results[i].select(".hl-item-thumb").attr("data-original")
            if (imageReferer.isNotBlank())
                cover = getCoverUrl(cover, imageReferer)
            val url = results[i].select(".hl-item-thumb").attr("href")
            val episode = results[i].select(".remarks").text()
            val item = MediaInfo1Data(title, cover, host + url, episode ?: "")
                .apply {
                    layoutConfig = BaseData.LayoutConfig(layoutSpanCount, 14.dp)
                    spanSize = layoutSpanCount / 3
                    action = DetailAction.obtain(url)
                }
            videoInfoItemDataList.add(item)
        }
        return videoInfoItemDataList
    }
    /**
     * 解析分类元素
     */
    fun parseClassifyEm(element: Element): List<ClassifyItemData> {
        val classifyItemDataList = mutableListOf<ClassifyItemData>()
        val classifyCategory = element.select("span").text()
        val li = element.select("ul").select("li")
        for (em in li){
            val a = em.select("a")
            classifyItemDataList.add(ClassifyItemData().apply {
                    action = ClassifyAction.obtain(
                        a.attr("href").apply {
                            Log.d("分类链接", this)
                        },
                        classifyCategory,
                        a.text()
                    )
                })
            }
        return classifyItemDataList
    }
}