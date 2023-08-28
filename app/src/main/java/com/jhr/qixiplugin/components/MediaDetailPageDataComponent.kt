package com.jhr.qixiplugin.components

import android.graphics.Color
import android.graphics.Typeface
import android.util.Log
import android.view.Gravity
import com.jhr.qixiplugin.util.JsoupUtil
import com.su.mediabox.pluginapi.components.IMediaDetailPageDataComponent
import com.su.mediabox.pluginapi.action.ClassifyAction
import com.su.mediabox.pluginapi.action.CustomPageAction
import com.su.mediabox.pluginapi.action.DetailAction
import com.su.mediabox.pluginapi.action.PlayAction
import com.su.mediabox.pluginapi.data.*
import com.su.mediabox.pluginapi.util.TextUtil.urlEncode
import com.su.mediabox.pluginapi.util.UIUtil.dp
import org.jsoup.nodes.Element
import org.jsoup.select.Elements

class MediaDetailPageDataComponent : IMediaDetailPageDataComponent {

    override suspend fun getMediaDetailData(partUrl: String): Triple<String, String, List<BaseData>> {
        var cover = ""
        var title = ""
        var desc = ""
        // 导演
        var director = ""
        // 主演
        var protagonist = ""
        // 更新时间
        var time = ""
        var upState = ""
        // 时长
        var duration = ""
        // 上映
        var show = ""
        val url = Const.host + partUrl
        val tags = mutableListOf<TagData>()
        val details = mutableListOf<BaseData>()
        val document = JsoupUtil.getDocument(url)

        // ------------- 番剧头部信息
        cover = Const.host+document.select(".hl-dc-pic").select("span").first()?.attr("data-original")?:""
        title = document.select(".hl-dc-content").select("h2").first()?.text()?:""
        // 更新状况
        val upStateItems = document.select(".hl-dc-content").select("ul").first()?.select("li")?.forEach {
            val t = it.text()
            when{
                t.contains("年份：") -> {
                    tags.add(TagData(it.ownText()))
                }
                t.contains("地区：") -> {
                    tags.add(TagData(it.ownText()))
                }
                t.contains("语言：") -> {
                    tags.add(TagData(it.ownText()))
                }
                t.contains("类型：") -> {
                    tags.add(TagData(it.select("a").text()).apply {
                        action = ClassifyAction.obtain(it.select("a").attr("href"), "", it.select("a").text())
                    })
                }

                t.contains("导演：") -> director = t
                t.contains("主演：") -> protagonist = t
                t.contains("状态：") -> upState = t
                t.contains("更新：") -> time = t
                t.contains("时长：") -> duration = t
                t.contains("上映：") -> show = t
                t.contains("简介：") -> desc = t
            }
        }

        // ---------------------------------- 播放列表+header

        val playEpisodeList : Elements = document.select("#playlist").select("div[class='hl-tabs-box hl-fadeIn']")
        val module = document.select("#playlist").select("div[class='hl-plays-from hl-tabs swiper-wrapper clearfix']").first()?.also {
            val playNameList = it.select("a")
//            Log.e("TAG","playNameList ${playNameList.size}")
//            Log.e("TAG","playEpisodeList ${playEpisodeList.size}")
            for (index in 0..playNameList.size) {
                val playName = playNameList.getOrNull(index)
                val playEpisode = playEpisodeList.getOrNull(index)
                if (playName != null && playEpisode != null) {
                    val episodes = parseEpisodes(playEpisode)
                    if (episodes.isNullOrEmpty())
                        continue
                    details.add(
                        SimpleTextData(
                            playName.text() + "(${episodes.size}集)"
                        ).apply {
                            fontSize = 16F
                            fontColor = Color.WHITE
                        }
                    )
                    details.add(EpisodeListData(episodes))
                }
            }
        }

        // ----------------------------------  系列动漫推荐
        val series = parseSeries(document.select("ul[class='hl-vod-list clearfix']"))
        if (series.isNotEmpty()) {
            details.add(
                SimpleTextData("猜你喜欢").apply {
                    fontSize = 16F
                    fontColor = Color.WHITE
                }
            )
            details.addAll(series)
        }
        return Triple(cover, title, mutableListOf<BaseData>().apply {
            add(Cover1Data(cover).apply {
                layoutConfig =
                    BaseData.LayoutConfig(
                        itemSpacing = 12.dp,
                        listLeftEdge = 12.dp,
                        listRightEdge = 12.dp
                    )
            })
            add(
                SimpleTextData(title).apply {
                    fontColor = Color.WHITE
                    fontSize = 20F
                    gravity = Gravity.CENTER
                    fontStyle = 1
                }
            )
            add(TagFlowData(tags))
            add(LongTextData(desc).apply {
                    fontColor = Color.WHITE
                }
            )
            add(SimpleTextData("·$director").apply {
                fontSize = 14F
                fontColor = Color.WHITE
                fontStyle = Typeface.BOLD
            })
            add(SimpleTextData("·$protagonist").apply {
                fontSize = 14F
                fontColor = Color.WHITE
                fontStyle = Typeface.BOLD
            })
            add(SimpleTextData("·$show").apply {
                fontSize = 14F
                fontColor = Color.WHITE
                fontStyle = Typeface.BOLD
            })
            add(SimpleTextData("·$duration").apply {
                fontSize = 14F
                fontColor = Color.WHITE
                fontStyle = Typeface.BOLD
            })
            add(SimpleTextData("·$time").apply {
                fontSize = 14F
                fontColor = Color.WHITE
                fontStyle = Typeface.BOLD
            })
            add(SimpleTextData("·$upState").apply {
                fontSize = 14F
                fontColor = Color.WHITE
                fontStyle = Typeface.BOLD
            })
            add(LongTextData(douBanSearch(title)).apply {
                fontSize = 14F
                fontColor = Color.WHITE
                fontStyle = Typeface.BOLD
            })
            addAll(details)
        })
    }

    private fun parseEpisodes(element: Element): List<EpisodeData> {
        val episodeList = mutableListOf<EpisodeData>()
        val elements: Elements = element.select("ul").select("li")
        for (k in elements.indices) {
            val a  = elements[k].select("a")
            val episodeUrl = a.attr("href")
            episodeList.add(
                EpisodeData(a.text(), episodeUrl).apply {
                    action = PlayAction.obtain(episodeUrl)
                }
            )
        }
        return episodeList
    }

    private fun parseSeries(element: Elements): List<MediaInfo1Data> {
        val videoInfoItemDataList = mutableListOf<MediaInfo1Data>()
        val results = element.select("li")
        for (i in results.indices) {
            val cover = results[i].select("a").attr("data-original")
            val title = results[i].select("a").attr("title")
            val url = results[i].select("a").attr("href")
            val item = MediaInfo1Data(
                title, Const.host + cover, Const.host + url,
                nameColor = Color.WHITE, coverHeight = 120.dp
            ).apply {
                action = DetailAction.obtain(url)
            }
            videoInfoItemDataList.add(item)
            if (i == 15) break
        }
        return videoInfoItemDataList
    }
    private fun douBanSearch(name: String) =
        "·豆瓣评分：https://m.douban.com/search/?query=${name.urlEncode()}"
}