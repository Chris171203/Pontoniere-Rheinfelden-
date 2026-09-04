package ch.pfvr.internapp;

/** Station-specific presentation rules for hydrological values. */
final class RiverDisplay {
    private static final double BASEL_RHEINHALLE_GAUGE_ZERO_M = 240.0d;
    private static final double RHEINFELDEN_GAUGE_ZERO_M = 260.0d;

    private RiverDisplay() {}

    static double levelValue(HydroStation station,double rawMetresAboveSea){
        return rawMetresAboveSea;
    }

    static String levelUnit(HydroStation station){
        return "m ü.M.";
    }

    static int levelDecimals(HydroStation station){
        return 2;
    }


    static double graphLevelValue(HydroStation station,double rawMetresAboveSea,boolean centimetres){
        return centimetres?gaugeCentimetres(station,rawMetresAboveSea):levelValue(station,rawMetresAboveSea);
    }

    static String graphLevelUnit(boolean centimetres){
        return centimetres?"cm":"m ü.M.";
    }

    static int graphLevelDecimals(boolean centimetres){
        return centimetres?0:2;
    }

    static double gaugeCentimetres(HydroStation station,double rawMetresAboveSea){
        if(!Double.isFinite(rawMetresAboveSea))return rawMetresAboveSea;
        double zero=station==HydroStation.BASEL_RHEINHALLE
                ?BASEL_RHEINHALLE_GAUGE_ZERO_M
                :RHEINFELDEN_GAUGE_ZERO_M;
        return (rawMetresAboveSea-zero)*100.0d;
    }
}
