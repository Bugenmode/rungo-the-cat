package com.myrungo.rungo.run

import android.os.Bundle
import com.arellomobile.mvp.MvpView
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.myrungo.rungo.FlowFragment
import com.myrungo.rungo.Scopes
import com.myrungo.rungo.Screens
import com.myrungo.rungo.challenge.ChallengeController
import com.myrungo.rungo.challenge.ChallengeItem
import com.myrungo.rungo.model.FlowRouter
import ru.terrakok.cicerone.Cicerone
import ru.terrakok.cicerone.NavigatorHolder
import ru.terrakok.cicerone.Router
import ru.terrakok.cicerone.commands.BackTo
import ru.terrakok.cicerone.commands.Replace
import toothpick.Toothpick
import toothpick.config.Module

class RunFlowFragment : FlowFragment(), MvpView {

    @InjectPresenter
    lateinit var presenter: RunFlowPresenter

    @ProvidePresenter
    fun providePresenter() = Toothpick
        .openScope(Scopes.PLAY)
        .getInstance(RunFlowPresenter::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        prepareScope(isFirstLaunch(savedInstanceState))
        super.onCreate(savedInstanceState)
        if (childFragmentManager.fragments.isEmpty()) {
            navigator.applyCommands(arrayOf(BackTo(null), Replace(Screens.Run)))
        }
    }

    private fun prepareScope(firstTime: Boolean) {
        val scope = Toothpick.openScopes(Scopes.APP, Scopes.PLAY)
        if (firstTime) {
            scope.installModules(
                object : Module() {
                    init {
                        val cicerone = Cicerone.create(FlowRouter(scope.getInstance(Router::class.java)))
                        bind(FlowRouter::class.java).toInstance(cicerone.router)
                        bind(NavigatorHolder::class.java).toInstance(cicerone.navigatorHolder)

                        bind(ChallengeItem::class.java)
                            .toInstance(arguments?.getParcelable(ARG_CHALLENGE) ?: ChallengeController.EMPTY)
                    }
                }
            )
        }
        Toothpick.inject(this, scope)
    }

    override fun onExit() {
        presenter.onExit()
    }

    companion object {
        private const val ARG_CHALLENGE = "rff_challenge"
        fun newInstance(challenge: ChallengeItem?) = RunFlowFragment()
            .apply {
                arguments = Bundle().apply { putParcelable(ARG_CHALLENGE, challenge) }
            }
    }
}