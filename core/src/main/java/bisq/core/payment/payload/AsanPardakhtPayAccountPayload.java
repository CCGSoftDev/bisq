package bisq.core.payment.payload;

import bisq.core.locale.Res;
import com.google.protobuf.Message;
import io.bisq.generated.protobuffer.PB;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import javax.annotation.Nullable;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@ToString
@Setter
@Getter
@Slf4j
public class AsanPardakhtPayAccountPayload extends PaymentAccountPayload {
    private String accountNr = "";

    public AsanPardakhtPayAccountPayload(String paymentMethodId, String id) {
        super(paymentMethodId, id);
    }

    private AsanPardakhtPayAccountPayload(String paymentMethodId, String id, String accountNr, long maxTradePeriod,
                                          @Nullable Map<String, String> excludeFromJsonDataMapParam
    ) {
        super(paymentMethodId, id, maxTradePeriod, excludeFromJsonDataMapParam);
        this.accountNr = accountNr;
    }

    public static AsanPardakhtPayAccountPayload fromProto(PB.PaymentAccountPayload proto) {
        return new AsanPardakhtPayAccountPayload(proto.getPaymentMethodId(),
                proto.getId(),
                proto.getWeChatPayAccountPayload().getAccountNr(),
                proto.getMaxTradePeriod(),
                CollectionUtils.isEmpty(proto.getExcludeFromJsonDataMap()) ? null : new HashMap<>(proto.getExcludeFromJsonDataMap()));
    }

    @Override
    public Message toProtoMessage() {
        return getPaymentAccountPayloadBuilder()
                .setAsanPardakhtPayAccountPayload(PB.AsanPardakhtPayAccountPayload.newBuilder()
                        .setAccountNr(accountNr))
                .build();
    }

    @Override
    public String getPaymentDetails() {
        return Res.get(paymentMethodId) + " - " + Res.getWithCol("payment.account.no") + " " + accountNr;
    }

    @Override
    public String getPaymentDetailsForTradePopup() {
        return getPaymentDetails();
    }

    @Override
    public byte[] getAgeWitnessInputData() {
        return super.getAgeWitnessInputData(accountNr.getBytes(Charset.forName("UTF-8")));
    }

}
