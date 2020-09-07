package com.cookie.android.util.arch.controller

import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.viewpager.widget.PagerAdapter

/**
 * 参考FragmentPagerAdapter源码改造，兼容ViewController的PagerAdapter
 * needAttach用于设置是否需要在创建ViewController时自动attach
 * Author: ZhangLingfei
 * Date : 2019/9/5 0005
 */
abstract class ViewControllerPagerAdapter(private val needAttach: Boolean = true) : PagerAdapter() {
    abstract fun getItem(position: Int, controllerId: String): ViewController

    override fun startUpdate(container: ViewGroup) {
        check(container.id != View.NO_ID) {
            ("ViewPager with adapter " + this
                    + " requires a v id")
        }
    }

    private var primaryController: ViewController? = null

    @CallSuper
    override fun setPrimaryItem(container: ViewGroup, position: Int, `object`: Any) {
        super.setPrimaryItem(container, position, `object`)
        if (`object` is ViewController && primaryController != `object`) {
            primaryController?.disappear(ViewController.APPEAR_LEVEL_PAGE)
            primaryController = `object`
            `object`.appear(ViewController.APPEAR_LEVEL_PAGE)
        }
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val controllerId = makeViewControllerName(container.id, position)
        val viewController = getItem(position, controllerId)
        viewController.maxAppearLevel = ViewController.APPEAR_LEVEL_PAGE
        if (needAttach)
            viewController.attach(container)
        return viewController
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        (`object` as ViewController).detach()
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return (`object` as ViewController).root === view
    }

    private fun makeViewControllerName(viewId: Int, id: Int): String {
        return "android:switcher:$viewId:$id"
    }
}
