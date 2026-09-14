package ch.pfvr.internapp;

import java.nio.file.Files;
import java.nio.file.Path;

/** Export the actual Java-generated JavaScript for DOM regression tests. */
public final class ExportAttendanceSkin {
    public static void main(String[] args) throws Exception {
        Path out=Path.of(args[0]);Files.createDirectories(out);
        for(String lang:new String[]{"de","gsw"})for(String theme:new String[]{"light","dark"}){
            boolean dark="dark".equals(theme);
            String script=InternalAttendanceSkin.javascript(dark?"#11171C":"#F6F8FA",dark?"#1A2228":"#FFFFFF",dark?"#232E36":"#EEF3F6",dark?"#ECF1F4":"#17222B",dark?"#A0B0BA":"#52616B",dark?"#344550":"#CFD8DE",dark?"#5BBED5":"#16798F",lang);
            Files.writeString(out.resolve(lang+"-"+theme+".js"),script);
        }
    }
}
