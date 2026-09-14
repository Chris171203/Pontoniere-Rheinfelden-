package ch.pfvr.internapp;

import android.content.SharedPreferences;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.webkit.WebView;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;
import org.robolectric.RobolectricTestRunner;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Set;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk=28)
public class ClubAndHomeUiTest {
    private static Object call(Object target,String name) throws Exception {
        Method method=target.getClass().getDeclaredMethod(name);method.setAccessible(true);return method.invoke(target);
    }
    private static Object field(Object target,String name) throws Exception {
        Field field=target.getClass().getDeclaredField(name);field.setAccessible(true);return field.get(target);
    }
    private static void set(Object target,String name,Object value) throws Exception {
        Field field=target.getClass().getDeclaredField(name);field.setAccessible(true);field.set(target,value);
    }
    @SuppressWarnings("unchecked") private static void configure(MainActivity activity) throws Exception {
        SharedPreferences prefs=(SharedPreferences)field(activity,"prefs");
        set(activity,"tileLayoutStore",new TileLayoutStore(prefs));
        // Keep tests deterministic: no network request is allowed to start.
        ((Set<ClubPageRepository.Page>)field(activity,"clubLoading")).addAll(Set.of(ClubPageRepository.Page.values()));
    }
    private static String text(View view){
        String out=view instanceof TextView?((TextView)view).getText().toString():"";
        if(view instanceof ViewGroup)for(int i=0;i<((ViewGroup)view).getChildCount();i++)out+="\n"+text(((ViewGroup)view).getChildAt(i));
        return out;
    }
    private static int count(View view,Class<?> type){
        int n=type.isInstance(view)?1:0;
        if(view instanceof ViewGroup)for(int i=0;i<((ViewGroup)view).getChildCount();i++)n+=count(((ViewGroup)view).getChildAt(i),type);
        return n;
    }
    private static int refreshCount(View view){
        int n="Aktualisieren".contentEquals(view.getContentDescription()==null?"":view.getContentDescription())?1:0;
        if(view instanceof ViewGroup)for(int i=0;i<((ViewGroup)view).getChildCount();i++)n+=refreshCount(((ViewGroup)view).getChildAt(i));
        return n;
    }

    @Test public void clubUsesCachedSourceAndCentredLogoFooterWithoutWebView() throws Exception {
        try(var controller=Robolectric.buildActivity(MainActivity.class).setup()){
            MainActivity activity=controller.get();configure(activity);
            SharedPreferences prefs=(SharedPreferences)field(activity,"prefs");
            var page=ClubPageRepository.Page.ABOUT;
            prefs.edit().putString(page.cacheKey(),ClubPageRepositoryTest.response(page,"<p>Original Vereinsinhalt 2021</p>"))
                    .putLong(page.updatedKey(),System.currentTimeMillis()).commit();
            View club=(View)call(activity,"club");
            assertTrue(text(club).contains("Original Vereinsinhalt 2021"));
            assertEquals(0,count(club,WebView.class));
            assertEquals(3,count(club,ImageView.class)); // Vereinslogo plus two brand logos.
            ViewGroup body=(ViewGroup)((ViewGroup)club).getChildAt(0);
            LinearLayout footer=(LinearLayout)body.getChildAt(body.getChildCount()-1);
            assertEquals(android.view.Gravity.CENTER,footer.getGravity());
            assertEquals("Instagram",footer.getChildAt(0).getContentDescription());
            assertEquals("Facebook",footer.getChildAt(1).getContentDescription());
        }
    }

    @Test public void nativeDetailPreservesCachedTextAndContactLinksAfterFailure() throws Exception {
        try(var controller=Robolectric.buildActivity(MainActivity.class).setup()){
            MainActivity activity=controller.get();configure(activity);
            SharedPreferences prefs=(SharedPreferences)field(activity,"prefs");
            var page=ClubPageRepository.Page.BOARD;set(activity,"clubPage",page);
            prefs.edit().putString(page.cacheKey(),ClubPageRepositoryTest.response(page,"<h3>Präsident</h3><p>Test Person <a href='mailto:test@example.org'>E-Mail</a></p>"))
                    .putLong(page.updatedKey(),1L).commit();
            ((Set<?>)field(activity,"clubFailed")).clear();
            @SuppressWarnings("unchecked") Set<ClubPageRepository.Page> failed=(Set<ClubPageRepository.Page>)field(activity,"clubFailed");failed.add(page);
            View detail=(View)call(activity,"clubPageScreen");
            assertEquals(0,count(detail,WebView.class));
            assertTrue(text(detail).contains("Test Person"));
            assertTrue(text(detail).contains("Gespeicherter Stand"));
            assertTrue(text(detail).contains("1970"));
        }
    }

    @Test public void refreshFollowsFirstVisibleLiveTileIncludingCustomLayout() throws Exception {
        try(var controller=Robolectric.buildActivity(MainActivity.class).setup()){
            MainActivity activity=controller.get();configure(activity);
            SharedPreferences prefs=(SharedPreferences)field(activity,"prefs");
            for(String hidden:new String[]{"","home_weather","home_weather,home_weather_3day,home_river_summary"}){
                prefs.edit().putString("tile_hidden_home",hidden).commit();
                LinearLayout stack=new LinearLayout(activity);
                Method populate=MainActivity.class.getDeclaredMethod("populateHomeTileStack",LinearLayout.class);populate.setAccessible(true);populate.invoke(activity,stack);
                assertEquals(1,refreshCount(stack));
                View first=null;for(int i=0;i<stack.getChildCount();i++)if("home-live".equals(stack.getChildAt(i).getTag())){first=stack.getChildAt(i);break;}
                assertNotNull(first);assertEquals(1,refreshCount(first));
            }
        }
    }
}
