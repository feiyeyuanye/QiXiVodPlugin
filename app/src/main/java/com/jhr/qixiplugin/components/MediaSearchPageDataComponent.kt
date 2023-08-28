package com.jhr.qixiplugin.components

import android.util.Log
import com.jhr.qixiplugin.components.Const.host
import com.jhr.qixiplugin.util.JsoupUtil
import com.jhr.qixiplugin.util.ParseHtmlUtil
import com.su.mediabox.pluginapi.components.IMediaSearchPageDataComponent
import com.su.mediabox.pluginapi.data.BaseData

class MediaSearchPageDataComponent : IMediaSearchPageDataComponent {

    override suspend fun getSearchData(keyWord: String, page: Int): List<BaseData> {
        val searchResultList = mutableListOf<BaseData>()
        // https://7xi.tv/vodsearch/page/2/wd/%E9%BE%99.html
        val url = "${host}/vodsearch/page/${page}/wd/${keyWord}.html"
        Log.e("TAG", url)

        val document = JsoupUtil.getDocument(url)
        searchResultList.addAll(ParseHtmlUtil.parseSearchEm(document, url))
        return searchResultList
    }

}