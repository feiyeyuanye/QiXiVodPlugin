package com.jhr.qixiplugin.components

import android.graphics.Color
import android.graphics.Typeface
import android.util.Log
import android.view.Gravity
import com.jhr.qixiplugin.actions.TodoAction
import com.jhr.qixiplugin.components.Const.host
import com.jhr.qixiplugin.components.Const.layoutSpanCount
import com.jhr.qixiplugin.util.JsoupUtil
import com.su.mediabox.pluginapi.action.ClassifyAction
import com.su.mediabox.pluginapi.action.CustomPageAction
import com.su.mediabox.pluginapi.action.DetailAction
import com.su.mediabox.pluginapi.components.IHomePageDataComponent
import com.su.mediabox.pluginapi.data.*
import com.su.mediabox.pluginapi.util.UIUtil.dp

class HomePageDataComponent : IHomePageDataComponent {

    override suspend fun getData(page: Int): List<BaseData>? {
        if (page != 1)
            return null
        val url = host
        val doc = JsoupUtil.getDocument(url)
        val data = mutableListOf<BaseData>()
        //1.横幅
        doc.select(".conch-br-box").select("ul").apply {
            val bannerItems = mutableListOf<BannerData.BannerItemData>()
            select("li").forEach { bannerItem ->
                val nameEm = bannerItem.select(".hl-br-title").text()
                val ext = bannerItem.select(".hl-br-sub").text()
                val videoUrl = bannerItem.select("a").attr("href")
                val bannerImage = bannerItem.select("a").attr("data-original")
                if (bannerImage.isNotBlank()) {
//                    Log.e("TAG", "添加横幅项 封面：$bannerImage 链接：$videoUrl")
                    bannerItems.add(
                        BannerData.BannerItemData(Const.host+bannerImage,nameEm, ext).apply {
                            if (!videoUrl.isNullOrBlank())
                                action = DetailAction.obtain(videoUrl)
                        }
                    )
                }
            }
            if (bannerItems.isNotEmpty())
                data.add(BannerData(bannerItems, 6.dp).apply {
                    layoutConfig = BaseData.LayoutConfig(layoutSpanCount, 14.dp)
                    spanSize = layoutSpanCount
                })
        }

        //3.各类推荐
        val modules = doc.select("div[class='hl-row-box']")
        var hasUpdate = false
        val update = mutableListOf<BaseData>()
        for (em in modules){
            val moduleHeading = em.select(".hl-rb-head").first()
            val type = moduleHeading?.select(".hl-rb-title")
            val typeName = type?.text()
            if (typeName == "吃瓜中心") continue
            val typeUrl = moduleHeading?.select(".hl-rb-more")?.attr("href")
            if (!typeName.isNullOrBlank()) {
                typeName.contains("热播推荐").also {
                    if (!it && hasUpdate) {
                        //示例使用水平列表视图组件
                        data.add(HorizontalListData(update, 120.dp).apply {
                            spanSize = layoutSpanCount
                        })
                    }
                    hasUpdate = it
                }
                data.add(SimpleTextData(typeName).apply {
                    fontSize = 15F
                    fontStyle = Typeface.BOLD
                    fontColor = Color.BLACK
                    spanSize = layoutSpanCount / 2
                })
                if (!typeName.contains("热播推荐")) data.add(SimpleTextData("查看更多 >").apply {
                    fontSize = 12F
                    gravity = Gravity.RIGHT or Gravity.CENTER_VERTICAL
                    fontColor = Const.INVALID_GREY
                    spanSize = layoutSpanCount / 2
                }.apply {
                    if (typeName == "专题"){
                        action = CustomPageAction.obtain(SpecialPageDataComponent::class.java)
                        action?.extraData = typeUrl
                    }else {
                        action = ClassifyAction.obtain(typeUrl, typeName)
                    }
                })
            }
            val li = em.select(".row").select("ul").first()?.select("li") ?: return data
            for ((index,video) in li.withIndex()){
                video.apply {
                    val a = select("a").first()
                    val name = a?.attr("title")
                    val videoUrl = a?.attr("href")
                    val coverUrl = Const.host + a?.attr("data-original")
                    val episode = a?.select(".remarks")?.text()
//                   Log.e("TAG", "添加视频 ($name) ($videoUrl) ($coverUrl) ($episode)")
                    if (!name.isNullOrBlank() && !videoUrl.isNullOrBlank() && !coverUrl.isNullOrBlank()) {
                        (if (hasUpdate) update else data).add(
                            MediaInfo1Data(name, coverUrl, videoUrl, episode ?: "")
                                .apply {
                                    if (hasUpdate) {
                                        paddingRight = 8.dp
                                    }
                                    spanSize = layoutSpanCount / 3
                                    if (typeName == "专题"){
                                        action = CustomPageAction.obtain(SpecialDetailPageDataComponent::class.java)
                                        action?.extraData = videoUrl
                                    }else {
                                        action = DetailAction.obtain(videoUrl)
                                    }
                                })
                    }
                }
                if (index == 11) break
            }
        }
        return data
    }
}