package bisq.core.payment;

import bisq.core.locale.FiatCurrency;
import bisq.core.payment.payload.AsanPardakhtPayAccountPayload;
import bisq.core.payment.payload.PaymentAccountPayload;
import bisq.core.payment.payload.PaymentMethod;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public class AsanPardakhtPayAccount extends PaymentAccount {
    public AsanPardakhtPayAccount() {
        super(PaymentMethod.ASANPARDAKHT_PAY);
        setSingleTradeCurrency(new FiatCurrency("IRR"));
    }

    @Override
    protected PaymentAccountPayload createPayload() {
        return new AsanPardakhtPayAccountPayload(paymentMethod.getId(), id);
    }
    public void setAccountNr(String accountNr) {
        ((AsanPardakhtPayAccountPayload) paymentAccountPayload).setAccountNr(accountNr);
    }
    public String getAccountNr() {
        return ((AsanPardakhtPayAccountPayload) paymentAccountPayload).getAccountNr();
    }
}
