package ch.pfvr.internapp;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.text.Spanned;
import android.text.style.ClickableSpan;
import org.json.JSONArray;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.GraphicsMode;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.Map;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk=28,qualifiers="w320dp-h800dp-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public class ClubPresentationTest {
    private static Object field(Object target,String name)throws Exception{Field f=target.getClass().getDeclaredField(name);f.setAccessible(true);return f.get(target);}
    private static void set(Object target,String name,Object value)throws Exception{Field f=target.getClass().getDeclaredField(name);f.setAccessible(true);f.set(target,value);}
    private static View screen(MainActivity activity)throws Exception{Method m=MainActivity.class.getDeclaredMethod("clubPageScreen");m.setAccessible(true);return (View)m.invoke(activity);}
    @SuppressWarnings("unchecked") private static SharedPreferences configure(MainActivity a)throws Exception{
        ((Set<ClubPageRepository.Page>)field(a,"clubLoading")).addAll(Set.of(ClubPageRepository.Page.values()));
        return (SharedPreferences)field(a,"prefs");
    }
    private static void cache(SharedPreferences prefs,ClubPageRepository.Page page,String html)throws Exception{
        var rows=new JSONArray(ClubPageRepositoryTest.response(page,html));rows.getJSONObject(0).put("modified","2022-01-24T22:37:51");
        prefs.edit().putString(page.cacheKey(),rows.toString()).putLong(page.updatedKey(),System.currentTimeMillis()).commit();
    }
    private static TextView find(View view,String text){
        if(view instanceof TextView&&((TextView)view).getText().toString().contains(text))return (TextView)view;
        if(view instanceof ViewGroup)for(int i=0;i<((ViewGroup)view).getChildCount();i++){TextView found=find(((ViewGroup)view).getChildAt(i),text);if(found!=null)return found;}
        return null;
    }
    private static void layout(View view){view.measure(View.MeasureSpec.makeMeasureSpec(320,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(800,View.MeasureSpec.EXACTLY));view.layout(0,0,320,800);}
    private static void checkTextBounds(View view){
        if(view.getVisibility()!=View.VISIBLE)return;
        if(view instanceof TextView text&&text.getText().length()>0){
            assertNotNull(text.getText().toString(),text.getLayout());
            int last=text.getLayout().getLineCount()-1;
            assertEquals(text.getText().toString(),text.getText().length(),text.getLayout().getLineEnd(last));
            // getLineMax excludes trailing spaces; getLineWidth includes them beyond the wrap point.
            for(int i=0;i<=last;i++){assertEquals(0,text.getLayout().getEllipsisCount(i));assertTrue(text.getText().toString(),text.getLayout().getLineMax(i)<=text.getWidth()-text.getPaddingLeft()-text.getPaddingRight()+1);}
            assertTrue(text.getText().toString(),text.getLayout().getHeight()<=text.getHeight()-text.getPaddingTop()-text.getPaddingBottom()+1);
        }
        if(view instanceof ViewGroup group)for(int i=0;i<group.getChildCount();i++)checkTextBounds(group.getChildAt(i));
    }
    private static void capture(View view,String name)throws Exception{
        View body=((ViewGroup)view).getChildAt(0);
        Bitmap bitmap=Bitmap.createBitmap(320,body.getHeight(),Bitmap.Config.ARGB_8888);body.draw(new Canvas(bitmap));
        File dir=new File("build/reports/club-ui");assertTrue(dir.isDirectory()||dir.mkdirs());
        try(FileOutputStream output=new FileOutputStream(new File(dir,name+".png"))){assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG,100,output));}bitmap.recycle();
    }
    @Test public void narrowLargeTextLayoutsKeepSourceAndExpansionAcrossRebuilds()throws Exception{
        for(String theme:new String[]{"light","dark"})for(String language:new String[]{"de","gsw"}){
            // Theme is resolved in onCreate, just like a real preference-triggered restart.
            Field name=MainActivity.class.getDeclaredField("PREFS");name.setAccessible(true);
            org.robolectric.RuntimeEnvironment.getApplication().getSharedPreferences((String)name.get(null),0).edit()
                    .clear().putString("theme_mode",theme).putString("ui_language",language).commit();
            org.robolectric.RuntimeEnvironment.setFontScale(1.5f);
            try(var controller=Robolectric.buildActivity(MainActivity.class).setup()){
                MainActivity a=controller.get();SharedPreferences prefs=configure(a);
                assertEquals(theme.equals("dark"),field(a,"darkMode"));
                cache(prefs,ClubPageRepository.Page.ABOUT,ClubContentParserTest.ABOUT.replaceAll("<img[^>]*>",""));
                set(a,"clubPage",ClubPageRepository.Page.ABOUT);View view=screen(a);a.setContentView(view);layout(view);
                TextView heading=find(view,"Training  +");assertNotNull(heading);heading.performClick();
                String summer=language.equals("gsw")?"Summertraining":"Sommertraining";
                find(view,summer+"  +").performClick();layout(view);
                assertNotNull(find(view,"18:30"));assertNotNull(find(view,"Homepage"));
                assertNotNull(find(view,language.equals("gsw")?"Über de Verein":"Über den Verein"));
                assertNotNull(find(view,language.equals("gsw")?"Abgruefe":"Abgerufen"));
                capture(view,"training-320-large-"+theme+"-"+language);checkTextBounds(view);
                view=screen(a);a.setContentView(view);layout(view);
                assertNotNull(find(view,summer+"  −"));assertNotNull(find(view,"18:30"));
                find(view,language.equals("gsw")?"Träffpunkt uf de Charte":"Treffpunkt auf Karte").performClick();
                var intent=Shadows.shadowOf(a).getNextStartedActivity();assertEquals("geo",intent.getData().getScheme());
                assertTrue(android.net.Uri.decode(intent.getDataString()).contains("Depot der Pontoniere Rheinfelden"));
            }
        }
    }

    @Test public void youthUsesBoardSourceContactAndHistoryKeepsPdfExternal()throws Exception{
        try(var controller=Robolectric.buildActivity(MainActivity.class).setup()){
            MainActivity a=controller.get();SharedPreferences prefs=configure(a);prefs.edit().putString("ui_language","de").commit();
            cache(prefs,ClubPageRepository.Page.YOUTH,"<p>Jeweils im Herbst lernen Kinder Knoten.</p>");
            cache(prefs,ClubPageRepository.Page.BOARD,"<div class='wp-block-column'><h3>Jungpontonier-Leiter</h3><p>Testperson <a href='mailto:test@example.org'>E-Mail</a></p></div>");
            set(a,"clubPage",ClubPageRepository.Page.YOUTH);View view=screen(a);a.setContentView(view);layout(view);
            TextView contact=find(view,"Testperson");assertNotNull(contact);
            Spanned text=(Spanned)contact.getText();text.getSpans(0,text.length(),ClickableSpan.class)[0].onClick(contact);
            var email=Shadows.shadowOf(a).getNextStartedActivity();assertEquals(android.content.Intent.ACTION_SENDTO,email.getAction());assertEquals("mailto:test@example.org",email.getDataString());
            cache(prefs,ClubPageRepository.Page.HISTORY,"<p>Jubiläumsbuch 1896 – 1996.</p><a href='/wp-content/uploads/test-book.pdf'>Buch</a>");
            set(a,"clubPage",ClubPageRepository.Page.HISTORY);view=screen(a);a.setContentView(view);layout(view);
            find(view,"Jubiläumsbuch (PDF)").performClick();var pdf=Shadows.shadowOf(a).getNextStartedActivity();
            assertEquals("https://www.pfvr.ch/wp-content/uploads/test-book.pdf",pdf.getDataString());
        }
    }
}
