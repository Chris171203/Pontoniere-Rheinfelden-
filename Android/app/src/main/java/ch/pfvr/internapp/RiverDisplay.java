package ch.pfvr.internapp;

/** Station-specific presentation rules for hydrological values. */
final class RiverDisplay {
    private static final double BASEL_RHEINHALLE_GAUGE_ZERO_M = 240.0d;

    private RiverDisplay() {}

    static double levelValue(HydroStation station,double rawMetresAboveSea){
        if(!Double.isFinite(rawMetresAboveSea))return rawMetresAboveSea;
        if(station==HydroStation.BASEL_RHEINHALLE){
            return (rawMetresAboveSea-BASEL_RHEINHALLE_GAUGE_ZERO_M)*100.0d;
        }
        return rawMetresAboveSea;
    }

    static String levelUnit(HydroStation station){
        return station==HydroStation.BASEL_RHEINHALLE?"cm":"m ü.M.";
    }

    static int levelDecimals(HydroStation station){
        return station==HydroStation.BASEL_RHEINHALLE?0:2;
    }
}
