package com.inghub.credit.util;

import java.math.BigDecimal;
import java.util.List;

public class ConstantValues {

    public static final List<Integer> VALID_INSTALLMENT_NUMBERS = List.of(6, 9, 12, 24);
    public static final BigDecimal VALID_INTEREST_RATE_RANGE_MIN = new BigDecimal("0.1");
    public static final BigDecimal VALID_INTEREST_RATE_RANGE_MAX = new BigDecimal("0.5");
}
