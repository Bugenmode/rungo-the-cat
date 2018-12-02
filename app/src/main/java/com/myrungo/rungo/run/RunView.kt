package com.myrungo.rungo.run

import com.arellomobile.mvp.MvpView
import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy
import com.arellomobile.mvp.viewstate.strategy.OneExecutionStateStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType
import com.myrungo.rungo.cat.CatView

@StateStrategyType(AddToEndSingleStrategy::class)
interface RunView : MvpView {
    fun showSkin(skin: CatView.Skins)
    fun showMap(show: Boolean)
    fun showTime(curTime: String, challengeTime: String)
    fun showSpeed(curSpeed: String, avgSpeed: String)
    fun showDistance(curDistance: String, challengeDistance: String)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun run(isRun: Boolean)
    @StateStrategyType(OneExecutionStateStrategy::class)
    fun showDialog()
}