package com.asfoundation.wallet.ui.gamification

import android.animation.Animator
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.Animation.AnimationListener
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import com.asf.wallet.R
import com.asfoundation.wallet.analytics.gamification.GamificationAnalytics
import com.google.android.material.bottomsheet.BottomSheetBehavior
import dagger.android.support.DaggerFragment
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_gamification_my_level.*
import kotlinx.android.synthetic.main.fragment_rewards_level.*
import kotlinx.android.synthetic.main.level_component.view.*
import kotlinx.android.synthetic.main.rewards_progress_bar.*
import javax.inject.Inject

class MyLevelFragment : DaggerFragment(), MyLevelView {
  @Inject
  lateinit var gamificationInteractor: GamificationInteractor
  @Inject
  lateinit var levelResourcesMapper: LevelResourcesMapper
  @Inject
  lateinit var analytics: GamificationAnalytics


  private lateinit var presenter: MyLevelPresenter
  private lateinit var gamificationView: GamificationView
  private lateinit var howItWorksBottomSheet: BottomSheetBehavior<View>
  private var step = 100

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    presenter =
        MyLevelPresenter(this, gamificationView, gamificationInteractor, analytics, Schedulers.io(),
            AndroidSchedulers.mainThread())
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    if (context !is GamificationView) {
      throw IllegalArgumentException(
          HowItWorksFragment::class.java.simpleName + " needs to be attached to a " + GamificationView::class.java.simpleName)
    }
    gamificationView = context
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    childFragmentManager.beginTransaction()
        .replace(R.id.gamification_fragment_container, HowItWorksFragment())
        .commit()
    return inflater.inflate(R.layout.fragment_gamification_my_level, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    howItWorksBottomSheet =
        BottomSheetBehavior.from(gamification_fragment_container)
    presenter.present(savedInstanceState)
  }

  override fun onDestroyView() {
    presenter.stop()
    super.onDestroyView()
  }

  override fun setupLayout() {
    setLevelIcons(0)
    setLevelIcons(1)
    setLevelIcons(2)
    setLevelIcons(3)
    setLevelIcons(4)
  }

  override fun updateLevel(userStatus: UserRewardsStatus) {
    if (userStatus.bonus.size != 1) {
      step = 100 / (userStatus.bonus.size - 1)
    }
    gamification_loading.visibility = View.GONE

    setLevelResources(userStatus.level)

    animateProgress(userStatus.lastShownLevel, userStatus.level)

    if (userStatus.level > userStatus.lastShownLevel) {
      levelUpAnimation(userStatus.level)
    }

    for (value in userStatus.bonus) {
      val level = userStatus.bonus.indexOf(value)
      setLevelBonus(level, formatLevelInfo(value), level == userStatus.lastShownLevel)
    }
  }

  private fun animateProgress(fromLevel: Int, toLevel: Int) {
    if (fromLevel == toLevel) {
      if (toLevel == 0) {
        levelUp(0)
      }
      return
    }

    val nextLevel = fromLevel + 1
    val to = (nextLevel) * step
    val animation = ProgressAnimation(progress_bar,
        progress_bar.progress.toFloat(), to.toFloat())
    animation.duration = if (nextLevel == toLevel) 1000 else 600
    animation.setAnimationListener(object : AnimationListener {
      override fun onAnimationRepeat(animation: Animation?) {
      }

      override fun onAnimationEnd(animation: Animation?) {
        if (fromLevel <= nextLevel) {
          levelUp(nextLevel)
        } else {
          levelReUp(nextLevel)
        }
        animateProgress(fromLevel + 1, toLevel)
      }

      override fun onAnimationStart(animation: Animation?) {
        if (fromLevel <= nextLevel) {
          levelShow(fromLevel)
        } else {
          levelLock(fromLevel)
        }
      }

    })
    progress_bar.startAnimation(animation)
  }

  private fun levelUp(level: Int) {
    when (level) {
      0 -> animateLevelUp(level_1, level_1_text, true)
      1 -> animateLevelUp(level_2, level_2_text, true)
      2 -> animateLevelUp(level_3, level_3_text, true)
      3 -> animateLevelUp(level_4, level_4_text, true)
      4 -> animateLevelUp(level_5, level_5_text, true)

    }
  }

  private fun levelShow(level: Int) {
    when (level) {
      0 -> animateLevelShow(level_1, level_1_text)
      1 -> animateLevelShow(level_2, level_2_text)
      2 -> animateLevelShow(level_3, level_3_text)
      3 -> animateLevelShow(level_4, level_4_text)
      4 -> animateLevelShow(level_5, level_5_text)
    }
  }

  private fun levelLock(level: Int) {
    when (level) {
      0 -> animateLevelToLock(level_1, level_1_text)
      1 -> animateLevelToLock(level_2, level_2_text)
      2 -> animateLevelToLock(level_3, level_3_text)
      3 -> animateLevelToLock(level_4, level_4_text)
      4 -> animateLevelToLock(level_5, level_5_text)
    }
  }

  private fun levelReUp(level: Int) {
    when (level) {
      0 -> animateLevelUp(level_1, level_1_text, false)
      1 -> animateLevelUp(level_2, level_2_text, false)
      2 -> animateLevelUp(level_3, level_3_text, false)
      3 -> animateLevelUp(level_4, level_4_text, false)
      4 -> animateLevelUp(level_5, level_5_text, false)
    }
  }

  override fun setStaringLevel(userStatus: UserRewardsStatus) {
    progress_bar.progress = userStatus.lastShownLevel * (100 / (userStatus.bonus.size - 1))
    levelUpAnimation(userStatus.level)
    for (i in 0..userStatus.lastShownLevel) {
      showPreviousLevelIcons(i, i < userStatus.lastShownLevel)
    }
  }

  private fun showPreviousLevelIcons(level: Int, shouldHideLabel: Boolean) {
    when (level) {
      0 -> {
        noAnimationLevelUpdate(level, shouldHideLabel, level_1, level_1_text)
      }
      1 -> {
        noAnimationLevelUpdate(level, shouldHideLabel, level_2, level_2_text)
      }
      2 -> {
        noAnimationLevelUpdate(level, shouldHideLabel, level_3, level_3_text)
      }
      3 -> {
        noAnimationLevelUpdate(level, shouldHideLabel, level_4, level_4_text)
      }
      4 -> {
        noAnimationLevelUpdate(level, shouldHideLabel, level_5, level_5_text)
      }
    }
  }

  private fun noAnimationLevelUpdate(level: Int, shouldHideLabel: Boolean, iconView: View,
                                     labelView: TextView) {
    iconView.level_inactive_icon.setImageResource(levelResourcesMapper.mapIcons(level))
    iconView.level_inactive_icon.visibility = View.VISIBLE
    if (shouldHideLabel) {
      labelView.visibility = View.INVISIBLE
    } else {
      labelView.isEnabled = true
    }
  }

  private fun animateLevelUp(levelIcon: View, levelText: TextView, newLevel: Boolean) {
    val activeIcon = levelIcon.findViewById(R.id.level_active_icon) as ImageView
    val listener = object : AnimationListener {
      override fun onAnimationRepeat(animation: Animation?) {
      }

      override fun onAnimationEnd(animation: Animation?) {
        activeIcon.visibility = View.VISIBLE
        levelText.isEnabled = true
        levelText.visibility = View.VISIBLE
      }

      override fun onAnimationStart(animation: Animation?) {
      }
    }
    if (newLevel) startBounceAnimation(activeIcon, listener) else startRebounceAnimation(activeIcon,
        listener)
  }

  override fun animateBackgroundFade() {
    howItWorksBottomSheet.setBottomSheetCallback(object :
        BottomSheetBehavior.BottomSheetCallback() {
      override fun onStateChanged(bottomSheet: View, newState: Int) {
      }

      override fun onSlide(bottomSheet: View, slideOffset: Float) {
        background_fade_animation?.progress = slideOffset
      }
    })
  }

  private fun animateLevelToLock(levelIcon: View, levelText: TextView) {
    val icon = levelIcon.findViewById(R.id.level_active_icon) as ImageView
    startShrinkAnimation(icon)
    icon.visibility = View.INVISIBLE
    levelText.isEnabled = false
    levelText.visibility = View.VISIBLE
  }

  private fun animateLevelShow(levelIcon: View, levelText: TextView) {
    val activeIcon = levelIcon.findViewById(R.id.level_active_icon) as ImageView
    startGrowAnimation(activeIcon)
    levelText.isEnabled = false
    levelText.visibility = View.INVISIBLE
  }

  private fun startBounceAnimation(view: View, listener: AnimationListener) {
    val animation = AnimationUtils.loadAnimation(activity, R.anim.bounce_animation)
    animation.setAnimationListener(listener)
    animation.fillAfter = true
    view.startAnimation(animation)
  }

  private fun startRebounceAnimation(view: View, listener: AnimationListener) {
    val animation = AnimationUtils.loadAnimation(activity, R.anim.rebounce_animation)
    animation.setAnimationListener(listener)
    animation.fillAfter = true
    view.startAnimation(animation)
  }

  private fun startShrinkAnimation(view: View) {
    val animation = AnimationUtils.loadAnimation(activity, R.anim.shrink_animation)
    animation.fillAfter = true
    view.startAnimation(animation)
  }

  private fun startGrowAnimation(view: View) {
    val animation = AnimationUtils.loadAnimation(activity, R.anim.grow_animation)
    animation.fillAfter = true
    view.startAnimation(animation)
  }

  private fun setLevelResources(level: Int) {
    levelIdleAnimation(level)
    level_title.text = getString(R.string.gamification_level_header,
        getString(levelResourcesMapper.mapTitle(level)))
    level_title.visibility = View.VISIBLE
    level_description.text = getString(levelResourcesMapper.mapSubtitle(level))
    level_description.visibility = View.VISIBLE
    current_level.text =
        getString(R.string.gamification_level_on_graphic, (level + 1).toString())
  }

  private fun setLevelBonus(level: Int, text: String, isCurrentLevel: Boolean) {
    val bonusLabel = if (isCurrentLevel) {
      R.string.gamification_level_bonus
    } else {
      R.string.gamification_how_table_b2
    }
    when (level) {
      0 -> level_1_text.text = getString(bonusLabel, text)
      1 -> level_2_text.text = getString(bonusLabel, text)
      2 -> level_3_text.text = getString(bonusLabel, text)
      3 -> level_4_text.text = getString(bonusLabel, text)
      4 -> level_5_text.text = getString(bonusLabel, text)
    }
  }

  private fun setLevelIcons(level: Int) {
    when (level) {
      0 -> level_1.level_active_icon.setImageResource(levelResourcesMapper.mapIcons(level))
      1 -> level_2.level_active_icon.setImageResource(levelResourcesMapper.mapIcons(level))
      2 -> level_3.level_active_icon.setImageResource(levelResourcesMapper.mapIcons(level))
      3 -> level_4.level_active_icon.setImageResource(levelResourcesMapper.mapIcons(level))
      4 -> level_5.level_active_icon.setImageResource(levelResourcesMapper.mapIcons(level))
    }
  }

  private fun setLevelIdleAnimation(level: Int) {
    when (level) {
      0 -> gamification_current_level_animation.setMinAndMaxFrame(30, 150)
      1 -> gamification_current_level_animation.setMinAndMaxFrame(210, 330)
      2 -> gamification_current_level_animation.setMinAndMaxFrame(390, 510)
      3 -> gamification_current_level_animation.setMinAndMaxFrame(570, 690)
      4 -> gamification_current_level_animation.setMinAndMaxFrame(750, 870)
    }
  }

  private fun setLevelTransitionAnimation(toLevel: Int) {
    when (toLevel) {
      0 -> gamification_current_level_animation.setMinAndMaxFrame(0, 30)
      1 -> gamification_current_level_animation.setMinAndMaxFrame(30, 210)
      2 -> gamification_current_level_animation.setMinAndMaxFrame(210, 390)
      3 -> gamification_current_level_animation.setMinAndMaxFrame(390, 570)
      4 -> gamification_current_level_animation.setMinAndMaxFrame(570, 750)
    }
  }

  private fun levelUpAnimation(level: Int) {
    setLevelTransitionAnimation(level)
    gamification_current_level_animation.visibility = View.VISIBLE
    gamification_current_level_animation.playAnimation()
    gamification_current_level_animation.addAnimatorListener(object : Animator.AnimatorListener {
      override fun onAnimationRepeat(animation: Animator?) {
        setLevelIdleAnimation(level)
      }

      override fun onAnimationEnd(animation: Animator?) {
      }

      override fun onAnimationCancel(animation: Animator?) {
      }

      override fun onAnimationStart(animation: Animator?) {
      }
    })
  }

  private fun levelIdleAnimation(level: Int) {
    setLevelIdleAnimation(level)
    gamification_current_level_animation.visibility = View.VISIBLE
    gamification_current_level_animation.playAnimation()
  }

  private fun expandBottomSheet() {
    howItWorksBottomSheet.state = BottomSheetBehavior.STATE_EXPANDED
  }

  private fun collapseBottomSheet() {
    howItWorksBottomSheet.state = BottomSheetBehavior.STATE_COLLAPSED
  }

  override fun changeBottomSheetState() {
    if (howItWorksBottomSheet.state == BottomSheetBehavior.STATE_COLLAPSED) {
      expandBottomSheet()
    } else {
      collapseBottomSheet()
    }
  }

  private fun formatLevelInfo(value: Double): String {
    val splitValue = value.toString()
        .split(".")
    return if (splitValue[1] != "0") {
      value.toString()
    } else {
      removeDecimalPlaces(value)
    }
  }

  private fun removeDecimalPlaces(value: Double): String {
    val splitValue = value.toString()
        .split(".")
    return splitValue[0]
  }
}