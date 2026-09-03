package ch.pfvr.internapp;

final class PaymentQrFileName {
    private PaymentQrFileName(){}

    static String forAmount(String amount){
        if(amount==null||amount.isBlank())return "PFVR_offenCHF.png";
        return "PFVR_"+amount+"CHF.png";
    }
}
