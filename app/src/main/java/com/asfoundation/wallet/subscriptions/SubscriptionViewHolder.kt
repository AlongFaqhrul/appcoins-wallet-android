package com.asfoundation.wallet.subscriptions

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.asf.wallet.R
import com.squareup.picasso.Picasso
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.subscription_item.view.*
import java.math.RoundingMode

class SubscriptionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

  fun bind(item: SubscriptionItem, clickCallback: PublishSubject<String>?) {
    itemView.apply {
      app_name.text = item.appName
      recurrence_value.text = String.format("%s / %s",
          item.symbol + item.amount.setScale(FIAT_SCALE, RoundingMode.FLOOR), item.recurrence)
      more_button.setOnClickListener { clickCallback?.onNext(item.packageName) }
      item_parent.setOnClickListener { clickCallback?.onNext(item.packageName) }
    }

    Picasso.with(itemView.context)
        .load(item.iconUrl)
        .error(R.drawable.ic_transaction_peer)//TODO
        .placeholder(R.drawable.ic_transaction_peer)//TODO
        .into(itemView.app_icon)
  }

  companion object {
    private const val FIAT_SCALE = 2
  }

}