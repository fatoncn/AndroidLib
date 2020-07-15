package com.cookie.android.util.arch.controller

import android.content.Context
import android.os.Bundle
import android.util.SparseBooleanArray
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.*
import androidx.lifecycle.Lifecycle.Event
import com.cookie.android.util.*
import com.cookie.android.util.arch.view.ViewElement
import com.cookie.android.util.livedata.Store
import com.cookie.android.util.livedata.observer.SafeObserver

/**
 * 替代Fragment的控制器方案，将Fragment的View层抽离出来，依赖于Activity
 * 1.controllerId值用于绑定Fragment的唯一标识，根据业务情况自行设计（类似
 * Fragment的tag），在ViewPager中若采用默认值会造成ViewModel共用的问题
 * 2.该方法的生命周期在attach后与Activity（或父controller）严格同步，不存在attach后视图依然
 * 没有创建的问题，可以同步对ViewController进行view操作
 * 3.layoutResId的根节点不能为merge
 *
 * Author: ZhangLingfei
 * Date : 2019/7/30 0030
 */
@Suppress("LeakingThis")
abstract class ViewController @JvmOverloads
constructor(protected val parent: ViewElement, root: View, controllerId: String = "") : LifecycleOwner, LifecycleObserver, ViewElement {

    constructor(parent: ViewElement, @LayoutRes layoutResId: Int, controllerId: String) : this(parent, layoutResId.inflate(parent.getRootActivity()), controllerId)

    /**
     * 与Fragment的关联值，默认为类名，若该ViewController在Activity中存在多个相同的实例，则需要在
     * 构造器中传controllerId，controllerId根据具体业务定义唯一值以确保在Activity中的唯一性
     */
    protected val tag: String = if (controllerId.isEmpty()) javaClass.name else controllerId
    /**
     * 该Fragment用于绑定ViewModel、保存状态
     */
    private val delegateFragment: DelegateFragment
    /**
     * ViewController根布局
     */
    val root: View
    /**
     * ViewController的容器
     */
    private var container: ViewGroup? = null
    /**
     * 状态是否保存
     */
    private val stateSaved: Boolean

    /**
     * 用于保持attach后与parentOwner（默认为Activity）生命周期同步
     */
    private val mLifecycleSyncObserver: LifecycleObserver = LifecycleEventObserver { source, _ ->
        if (parent.lifecycle.currentState == Lifecycle.State.DESTROYED) {
            parent.lifecycle.removeObserver(this)
        }
        lifecycle.currentState = source.lifecycle.currentState
    }

    val activity = parent.getRootActivity()

    private var maxAppearLevel = 3

    companion object {
        const val APPEAR_LEVEL_PARENT = 0
        const val APPEAR_LEVEL_ATTACH = 1
        const val APPEAR_LEVEL_START = 2
        const val APPEAR_LEVEL_SHOW = 3
        const val APPEAR_LEVEL_PAGE = 4
    }

    /**
     * 界面中的显示层级
     */
    private val appearLevel = Store(APPEAR_LEVEL_PARENT)

    /**
     * 各层级的显示情况
     */
    private val appearValue = SparseBooleanArray()

    private val hasAppear = MutableLiveData<Boolean>()

    private val isShow = MutableLiveData<Boolean>()

    internal fun getAppearLevel() = appearLevel.value

    init {
        appearLevel.observeForever(object : SafeObserver<Int>(true) {
            override fun onSafeChanged(t: Int) {
                val newValue = appearLevel.value >= maxAppearLevel
                if (hasAppear() != newValue) {
                    hasAppear.postValue(newValue)
                }
            }
        })
        appearLevel.observeForever(object : SafeObserver<Int>(true) {
            override fun onSafeChanged(t: Int) {
                val newValue = appearLevel.value >= APPEAR_LEVEL_SHOW
                if (isShow() != newValue)
                    isShow.postValue(newValue)
            }
        })
    }

    /**
     * 判断Controller是否显示
     */
    fun isShow() = appearLevel.value >= APPEAR_LEVEL_SHOW

    fun isShowLive(): LiveData<Boolean> = isShow

    fun hasAppear(): Boolean = hasAppear.value ?: false

    fun getAppearLive(): LiveData<Boolean> = hasAppear

    init {
        val parentFM = parent.getChildFragmentManager()
        /**
         * 先尝试取回绑定的Fragment，若为空则是第一次创建
         */
        var old = parentFM.findFragmentByTag(tag)
        if (old !is DelegateFragment) {
            old = DelegateFragment()
            old.arguments = Bundle()
            stateSaved = false
            parentFM.beginTransaction().add(old, tag).commitNowAllowingStateLoss()
        } else {
            stateSaved = true
        }
        delegateFragment = old
        //让Fragment的保存状态回调能够响应到ViewController中
        delegateFragment.viewController = this
        this.root = root
        appear(APPEAR_LEVEL_SHOW)
    }

    /**
     * ViewController自己的生命周期
     */
    private var lifecycle = resetLifecycle()

    internal fun disappear(level: Int) {
        appearValue.put(level, false)
        for (i in 0..maxAppearLevel)
            if (!appearValue[i]) {
                if (i - 1 < appearLevel.value)
                    appearLevel.postValue(i - 1)
                break
            } else if (i == maxAppearLevel) {
                if (i < appearLevel.value)
                    appearLevel.postValue(i)
            }
    }

    internal fun appear(level: Int) {
        if (level > maxAppearLevel)
            maxAppearLevel = level
        appearValue.put(level, true)
        for (i in 0..maxAppearLevel)
            if (!appearValue[i]) {
                if (i - 1 > appearLevel.value)
                    appearLevel.postValue(i - 1)
                break
            } else if (i == maxAppearLevel) {
                if (i > appearLevel.value)
                    appearLevel.postValue(i)
            }
    }

    init {
        if (parent is ViewController)
            observe(parent.hasAppear) {
                if (it)
                    appear(APPEAR_LEVEL_PARENT)
                else
                    disappear(APPEAR_LEVEL_PARENT)
            }
        else
            appear(APPEAR_LEVEL_PARENT)
    }

    /**
     * 将ViewController附着在container中，只有附着在container中ViewController才会
     * 开始view的添加与绘制
     * 这个方法重复调用会被忽略
     */
    fun attach(container: ViewGroup? = null): ViewController {
        if (this.container == null) {
            if (container != null) {
                container.addView(root)
                this.container = container
            } else if (root.parent is ViewGroup) {
                this.container = root.parent as ViewGroup
            }
            if (lifecycle.currentState == Lifecycle.State.DESTROYED)
                lifecycle = resetLifecycle()
            //同步ViewController生命周期到parentOwner当前状态
            lifecycle.currentState = parent.lifecycle.currentState
            parent.lifecycle.addObserver(mLifecycleSyncObserver)
            appear(APPEAR_LEVEL_ATTACH)
        }
        return this
    }

    fun isAttach(): Boolean {
        return appearLevel.value >= APPEAR_LEVEL_ATTACH
    }

    /**
     * 异步attach，attach会延时执行
     */
    fun attachAsync(container: ViewGroup): ViewController {
        return attachAsync(container, 0)
    }

    /**
     * 用户嵌套controller的情况
     */

    override fun getChildFragmentManager(): FragmentManager {
        return delegateFragment.childFragmentManager
    }

    override fun getRootActivity(): FragmentActivity {
        return parent.getRootActivity()
    }

    /**
     * 异步attach，attach会延时执行
     */
    fun attachAsync(container: ViewGroup, ms: Int): ViewController {
        parent.safeRunOnMainDelay(Runnable {
            attach(container)
        }, ms)
        return this
    }

    /**
     * 将ViewController从它的父view移出，随后会执行view层的销毁并清理自身资源
     */
    fun detach() {
        container?.removeView(root)
        container = null
        //同步ViewController生命周期至destroyed
        parent.lifecycle.removeObserver(mLifecycleSyncObserver)
        lifecycle.currentState = Lifecycle.State.DESTROYED
        disappear(APPEAR_LEVEL_ATTACH)
    }

    private fun resetLifecycle(): LifecycleRegistry {
        val registry = LifecycleRegistry(this)
        registry.addObserver(LifecycleEventObserver { owner, event ->
            when (event) {
                Event.ON_CREATE -> {
                    onCreate()
                    if (!stateSaved)
                        onInitCreate()
                    onActivityCreated()
                    onViewCreated()
                }
                Event.ON_DESTROY -> {
                    onDestroyView()
                    onDestroy()
                }
                Event.ON_START -> {
                    appear(APPEAR_LEVEL_START)
                    onStart()
                }
                Event.ON_RESUME -> onResume()
                Event.ON_PAUSE -> onPause()
                Event.ON_STOP -> {
                    disappear(APPEAR_LEVEL_START)
                    onStop()
                }
                else -> {
                }
            }
        })
        return registry
    }

    override fun getLifecycle(): Lifecycle = lifecycle

    fun <T : ViewModel> getViewModel(clazz: Class<T>): T = delegateFragment.getViewModel(clazz)

    fun <T : ViewModel> getActivityViewModel(clazz: Class<T>): T = activity.getViewModel(clazz)

    fun <T : ViewModel> getParentViewModel(clazz: Class<T>): T {
        return when (parent) {
            is ViewController -> parent.getViewModel(clazz)
            is Fragment -> (parent as Fragment).getViewModel(clazz)
            else -> getActivityViewModel(clazz)
        }
    }

    fun getContext(): Context = ContextUtil.get()

    fun getString(@StringRes resId: Int): String = getContext().getString(resId)

    fun show(): ViewController {
        if (!appearValue[APPEAR_LEVEL_SHOW]) {
            appear(APPEAR_LEVEL_SHOW)
            root.visibility = View.VISIBLE
        }
        return this
    }

    fun hide(): ViewController {
        if (appearValue[APPEAR_LEVEL_SHOW]) {
            disappear(APPEAR_LEVEL_SHOW)
            root.visibility = View.INVISIBLE
        }
        return this
    }

    protected open fun onCreate() {
    }

    /**
     * 用于只需在第一次页面创建执行的初始化行为（如入参初始化、初始化请求）
     */
    protected open fun onInitCreate() {

    }

    protected open fun onActivityCreated() {
    }

    protected open fun onViewCreated() {

    }

    protected open fun onDestroy() {

    }

    protected open fun onDestroyView() {

    }

    protected open fun onStart() {

    }

    protected open fun onStop() {

    }

    protected open fun onViewStateRestored(savedInstanceState: Bundle?) {

    }

    protected open fun onSaveInstanceState(outState: Bundle) {

    }

    protected open fun onResume() {

    }

    protected open fun onPause() {

    }

    fun getArguments(): Bundle {
        var args = delegateFragment.arguments
        if (args == null) {
            args = Bundle()
        }
        return args
    }

    class DelegateFragment : Fragment() {

        var viewController: ViewController? = null

        override fun onSaveInstanceState(outState: Bundle) {
            super.onSaveInstanceState(outState)
            viewController?.onSaveInstanceState(outState)
        }

        override fun onViewStateRestored(savedInstanceState: Bundle?) {
            super.onViewStateRestored(savedInstanceState)
            viewController?.onViewStateRestored(savedInstanceState)
        }
    }

}