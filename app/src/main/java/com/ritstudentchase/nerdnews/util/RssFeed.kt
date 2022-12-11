package com.ritstudentchase.nerdnews.util

import com.ritstudentchase.nerdnews.models.ChocolateyItem
import com.ritstudentchase.nerdnews.models.MicrosoftItem
import com.ritstudentchase.nerdnews.viewmodels.RequestResult

interface RssFeed<out T_ITEM> {
    suspend fun loadLocal()

    fun merge(): RequestResult

    fun getItems(): MutableList<out T_ITEM?>
}