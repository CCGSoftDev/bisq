package bisq.desktop.components.paymentmethods;

import bisq.core.locale.Res;
import bisq.core.payment.AccountAgeWitnessService;
import bisq.core.payment.AsanPardakhtPayAccount;
import bisq.core.payment.PaymentAccount;
import bisq.core.payment.payload.AsanPardakhtPayAccountPayload;
import bisq.core.payment.payload.PaymentAccountPayload;
import bisq.core.util.BSFormatter;
import bisq.core.util.validation.InputValidator;
import bisq.desktop.util.validation.AsanPardakhtPayValidator;
import javafx.scene.layout.GridPane;

import static bisq.desktop.util.FormBuilder.addCompactTopLabelTextFieldWithCopyIcon;

public class AsanPardakhtPayForm extends GeneralAccountNumberForm {

    private final AsanPardakhtPayAccount asanPardakhtPayAccount;

    public AsanPardakhtPayForm(PaymentAccount paymentAccount, AccountAgeWitnessService accountAgeWitnessService, AsanPardakhtPayValidator asanPardakhtPayValidator, InputValidator inputValidator, GridPane gridPane, int gridRow, BSFormatter formatter) {
        super(paymentAccount, accountAgeWitnessService, inputValidator, gridPane, gridRow, formatter);
        this.asanPardakhtPayAccount = (AsanPardakhtPayAccount) paymentAccount;
    }
    public static int addFormForBuyer(GridPane gridPane, int gridRow, PaymentAccountPayload paymentAccountPayload) {
        addCompactTopLabelTextFieldWithCopyIcon(gridPane, ++gridRow, Res.get("payment.debitcard.no"), ((AsanPardakhtPayAccountPayload) paymentAccountPayload).getAccountNr());
        return gridRow;
    }

    @Override
    void setAccountNumber(String newValue) {
        asanPardakhtPayAccount.setAccountNr(newValue);
    }

    @Override
    String getAccountNr() {
        return asanPardakhtPayAccount.getAccountNr();
    }
}
