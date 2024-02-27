package mpesa.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum B2CCommandID {
    SALARY_PAYMENT("SalaryPayment"), BUSINESS_PAYMENT("BusinessPayment"), PROMOTION_PAYMENT("PromotionPayment");
    private final String value;
}
