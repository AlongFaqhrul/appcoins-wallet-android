package com.asfoundation.wallet.ui.iab;

import io.reactivex.Observable;
import java.math.BigDecimal;
import java.util.List;

/**
 * Created by franciscocalado on 19/07/2018.
 */

public interface OnChainBuyView {
  Observable<OnChainBuyPresenter.BuyData> getBuyClick();

  Observable<Object> getCancelClick();

  Observable<Object> getOkErrorClick();

  void showLoading();

  void close();

  void finish(String hash);

  void showError();

  void setup();

  void showTransactionCompleted();

  void showBuy();

  void showWrongNetworkError();

  void showNoNetworkError();

  void showApproving();

  void showBuying();

  void showNonceError();

  void showNoTokenFundsError();

  void showNoEtherFundsError();

  void showNoFundsError();

  void showRaidenChannelValues(List<BigDecimal> values);

  Observable<Boolean> getCreateChannelClick();

  void showRaidenInfo();

  Observable<Object> getDontShowAgainClick();

  void showChannelAmount();

  void hideChannelAmount();

  void showChannelAsDefaultPayment();

  void showDefaultAsDefaultPayment();

  void showWallet(String wallet);

  void showNoChannelFundsError();
}
