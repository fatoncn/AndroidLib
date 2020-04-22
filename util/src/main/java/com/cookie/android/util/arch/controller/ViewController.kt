package com.cookie.android.util.arch.controller

import android.content.Context
import android.os.Bundle
import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.*
import androidx.lifecycle.Lifecycle.Event
import com.cookie.android.util.livedata.Live
import com.cookie.android.util.livedata.StoreLiveData
import com.cookie.android.util.livedata.observer.SafeObserver
import com.cookie.android.util.ContextUtil
import com.cookie.android.util.getViewModel
import com.cookie.android.util.observe
import com.cookie.android.util.runOnMainDelay

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
constructor(val activity: FragmentActivity, @LayoutRes val layoutResId: Int, controllerId: String = ""
            , protected val parentOwner: LifecycleOwner = activity)
    : LifecycleOwner, LifecycleObserver {

    constructor(controller: ViewController, @LayoutRes layoutResId: Int, controllerId: String = "")
            : this(controller.activity, layoutResId, controllerId, controller)

    constructor(fragment: Fragment, @LayoutRes layoutResId: Int, controllerId: String = "")
            : this(fragment.activity!!, layoutResId, controllerId, fragment)

    /**
     * 与Fragment的关联值，默认为类名，若该ViewController在Activity中存在多个相同的实例，则需要在
     * 构造器中传controllerId，controllerId根据具体业务定义唯一值以确保在Activity中的唯一性
     */
    protected val tag: String = if (controllerId.isEmpty()) javaClass.name else controllerId
    /**
     * 该Fragment用于绑定ViewModel、保存状态
     */
    private val storeFragment: DelegateFragment
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
    private val mLifecycleSyncObserver: LifecycleObserver = GenericLifecycleObserver { source, _ ->
        if (parentOwner.lifecycle.currentState == Lifecycle.State.DESTROYED) {
            parentOwner.lifecycle.removeObserver(this)
        }
        lifecycle.markState(source.lifecycle.currentState)
    }

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
    private val appearLevel = StoreLiveData(APPEAR_LEVEL_PARENT)

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
        val fm = when (parentOwner) {
            is ViewController -> parentOwner.getChildSupportFragmentManager()
            is Fragment -> parentOwner.childFragmentManager
            else -> activity.supportFragmentManager
        }
        /**
         * 先尝试取回绑定的Fragment，若为空则是第一次创建
         */
        var old = fm.findFragmentByTag(tag)
        if (old !is DelegateFragment) {
            old = DelegateFragment()
            old.arguments = Bundle()
            stateSaved = false
            fm.beginTransaction().add(old, tag).commitNowAllowingStateLoss()
        } else {
            stateSaved = true
        }
        storeFragment = old
        //让Fragment的保存状态回调能够响应到ViewController中
        storeFragment.viewController = this
        root = LayoutInflater.from(activity).inflate(layoutResId, FrameLayout(activity), false)
        appear(APPEAR_LEVEL_SHOW)
    }

    /**
     * ViewController自己的生命周期
     */
    private val lifecycle: LifecycleRegistry by lazy {
        val registry = LifecycleRegistry(this)
        registry.addObserver(GenericLifecycleObserver { _, event ->
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
        registry
    }

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
        if (parentOwner is ViewController)
            observe(parentOwner.hasAppear, Observer {
                if (it)
                    appear(APPEAR_LEVEL_PARENT)
                else
                    disappear(APPEAR_LEVEL_PARENT)
            })
        else
            appear(APPEAR_LEVEL_PARENT)
    }

    /**
     * 将ViewController附着在container中，只有附着在container中ViewController才会
     * 开始view的添加与绘制
     * 这个方法重复调用会被忽略
     */
    fun attach(container: ViewGroup): ViewController {
        if (this.container == null) {
            container.addView(root)
            this.container = container
            //同步ViewController生命周期到parentOwner当前状态
            lifecycle.markState(parentOwner.lifecycle.currentState)
            activity.lifecycle.addObserver(mLifecycleSyncObserver)
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
    fun getChildSupportFragmentManager(): FragmentManager {
        return storeFragment.childFragmentManager
    }

    /**
     * 异步attach，attach会延时执行
     */
    fun attachAsync(container: ViewGroup, ms: Int): ViewController {
        activity.runOnMainDelay(Runnable {
            attach(container)
        }, ms)
        return this
    }

    /**
     * 将ViewController从它的父view移出，随后会执行view层的销毁并清理自身资源
     */
    fun detach() {
        activity.lifecycle.removeObserver(mLifecycleSyncObserver)
        container?.removeView(root)
        container = null
        //同步ViewController生命周期至销毁状态
        //FIXME:这边需要验证DESTROYED状态与INITIALIZED状态的差异
        lifecycle.markState(Lifecycle.State.INITIALIZED)
//        lifecycle.markState(Lifecycle.State.DESTROYED)
        disappear(APPEAR_LEVEL_ATTACH)
    }

    override fun getLifecycle(): Lifecycle = lifecycle

    fun <T : ViewModel> getViewModel(clazz: Class<T>): T = storeFragment.getViewModel(clazz)

    fun <T : ViewModel> getActivityViewModel(clazz: Class<T>): T = activity.getViewModel(clazz)

    fun <T : ViewModel> getParentViewModel(clazz: Class<T>): T {
        return when (parentOwner) {
            is ViewController -> parentOwner.getViewModel(clazz)
            is Fragment -> parentOwner.getViewModel(clazz)
            else -> getActivityViewModel(clazz)
        }
    }

    fun getContext(): Context = ContextUtil.get()

    fun getString(@StringRes resId: Int): String = getContext().getString(resId)

    fun show(): ViewController {
        if (!isShow()) {
            appear(APPEAR_LEVEL_SHOW)
            root.visibility = View.VISIBLE
        }
        return this
    }

    fun hide(): ViewController {
        if (isShow()) {
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
        var args = storeFragment.arguments
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