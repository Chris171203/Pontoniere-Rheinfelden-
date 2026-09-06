package ch.pfvr.internapp;
import static org.junit.Assert.*;
import java.nio.charset.StandardCharsets;import java.nio.file.*;import org.junit.Test;
public class OpusSafetyRegressionTest {
 private static String source() throws Exception {String r="src/main/java/ch/pfvr/internapp/MainActivity.java";Path[] c={Paths.get(r),Paths.get("app",r),Paths.get("Android","app",r)};for(Path p:c)if(Files.isRegularFile(p))return Files.readString(p,StandardCharsets.UTF_8);throw new IllegalStateException();}
 @Test public void currentNavigationRequiresFreshData() throws Exception {String s=source();assertTrue(s.contains("fromCurrentBaselGaugeCm"));assertTrue(s.contains("dd.MM. HH:mm"));assertTrue(s.contains("riverLevelColor(HydroStation station"));}
 @Test public void webFlowsAreHardened() throws Exception {String s=source();assertTrue(s.contains("AppLinkPolicy.mayOpenExternally(uri.getScheme())"));assertFalse(s.contains("window.__pfvrBaseInternalUrl="));}
}
