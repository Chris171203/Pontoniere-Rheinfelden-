package ch.pfvr.internapp;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Swiss-QR amounts are exact decimal CHF values with at most two fractional digits. */
final class PaymentAmount {
    private PaymentAmount() {}

    static String normalize(String raw){
        if(raw==null||raw.trim().isEmpty())return "";
        String value=raw.trim();
        if(!value.matches("[0-9]+(?:[.,][0-9]{1,2})?"))return null;
        try{
            BigDecimal amount=new BigDecimal(value.replace(',','.'));
            if(amount.compareTo(BigDecimal.ZERO)==0)return "";
            if(amount.compareTo(new BigDecimal("100000"))>0)return null;
            return amount.setScale(2,RoundingMode.UNNECESSARY).toPlainString();
        }catch(ArithmeticException|NumberFormatException error){return null;}
    }
}
