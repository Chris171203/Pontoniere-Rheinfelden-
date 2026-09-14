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
        SharedPreferences prefs=(SharedPreferences)field(a,"prefs");set(a,"tileLayoutStore",new TileLayoutStore(prefs));return prefs;
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
    private static Object call(MainActivity a,String method)throws Exception{Method m=MainActivity.class.getDeclaredMethod(method);m.setAccessible(true);return m.invoke(a);}
    private static View overview(MainActivity a)throws Exception{return (View)call(a,"club");}
    private static View action(View view,String prefix){
        if(view.isClickable()&&view.getContentDescription()!=null&&view.getContentDescription().toString().startsWith(prefix))return view;
        if(view instanceof ViewGroup group)for(int i=0;i<group.getChildCount();i++){View found=action(group.getChildAt(i),prefix);if(found!=null)return found;}
        return null;
    }
    private static int accordions(View view){
        int count=view.isClickable()&&view.getContentDescription()!=null&&(view.getContentDescription().toString().endsWith("Aufklappen")||view.getContentDescription().toString().endsWith("Ufklappe"))?1:0;
        if(view instanceof ViewGroup group)for(int i=0;i<group.getChildCount();i++)count+=accordions(group.getChildAt(i));
        return count;
    }
    private static void assertVisibleText(View view,String value){
        TextView found=find(view,value);assertNotNull(value,found);
        View child=found;while(child!=view){assertEquals(value,View.VISIBLE,child.getVisibility());child=(View)child.getParent();}
    }
    @Test public void narrowOverviewAndFlatArticlesShowContentWithoutExpandingInBothLanguagesAndThemes()throws Exception{
        for(String theme:new String[]{"light","dark"})for(String language:new String[]{"de","gsw"}){
            Field name=MainActivity.class.getDeclaredField("PREFS");name.setAccessible(true);
            org.robolectric.RuntimeEnvironment.getApplication().getSharedPreferences((String)name.get(null),0).edit()
                    .clear().putString("theme_mode",theme).putString("ui_language",language).commit();
            org.robolectric.RuntimeEnvironment.setFontScale(1.5f);
            try(var controller=Robolectric.buildActivity(MainActivity.class).setup()){
                MainActivity a=controller.get();SharedPreferences prefs=configure(a);assertEquals(theme.equals("dark"),field(a,"darkMode"));
                cache(prefs,ClubPageRepository.Page.ABOUT,ClubContentParserTest.ABOUT.replaceAll("<img[^>]*>",""));
                View view=overview(a);a.setContentView(view);layout(view);
                assertVisibleText(view,"18:30–20:00");assertVisibleText(view,"19:30");
                assertVisibleText(view,language.equals("gsw")?"Mäntig- und Mittwuchaabig":"Montag- und Mittwochabend");
                assertVisibleText(view,language.equals("gsw")?"Dunnschtig":"Donnerstag");
                assertEquals(0,accordions(view));assertNull(find(view,"38 Mitglieder"));
                checkTextBounds(view);capture(view,"overview-320-large-"+theme+"-"+language);
                action(view,language.equals("gsw")?"Träffpunkt uf de Charte":"Treffpunkt auf Karte").performClick();
                var map=Shadows.shadowOf(a).getNextStartedActivity();assertEquals("geo",map.getData().getScheme());
                assertTrue(android.net.Uri.decode(map.getDataString()).contains("Depot der Pontoniere Rheinfelden"));
                set(a,"clubDestination",ClubContentPresentation.Destination.SPORT);view=screen(a);a.setContentView(view);layout(view);
                assertVisibleText(view,"340 kg");assertVisibleText(view,"460 kg");assertVisibleText(view,"Stangen nicht berühren");assertVisibleText(view,"weiter oben anlanden");
                assertVisibleText(view,language.equals("gsw")?"Boot & Sport":"Boote & Sport");
                assertEquals(1,accordions(view));assertNull(find(view,"38 Mitglieder"));checkTextBounds(view);
                capture(view,"sport-320-large-"+theme+"-"+language);
                action(view,language.equals("gsw")?"Vollständige Quelltext":"Vollständiger Quelltext").performClick();
                assertVisibleText(view,"38 Mitglieder");
            }
        }
    }

    @SuppressWarnings({"unchecked","rawtypes"})
    @Test public void oneTapOpensNativeArticleAndBackRestoresOverviewAndNestedPosition()throws Exception{
        try(var controller=Robolectric.buildActivity(MainActivity.class).setup()){
            MainActivity a=controller.get();SharedPreferences prefs=configure(a);prefs.edit().putString("ui_language","de").commit();
            cache(prefs,ClubPageRepository.Page.ABOUT,ClubContentParserTest.ABOUT.replaceAll("<img[^>]*>",""));
            cache(prefs,ClubPageRepository.Page.YOUTH,"<p>Jeweils im Herbst lernen Kinder Knoten.</p>");
            a.setContentView((View)call(a,"buildShell"));
            Class type=Class.forName(MainActivity.class.getName()+"$Screen");Method navigate=MainActivity.class.getDeclaredMethod("navigate",type);navigate.setAccessible(true);navigate.invoke(a,Enum.valueOf(type,"CLUB"));
            ViewGroup container=(ViewGroup)field(a,"content");View view=container.getChildAt(0);layout(view);
            Shadows.shadowOf(android.os.Looper.getMainLooper()).idle();view.scrollTo(0,180);
            action(view,"Boote & Sport ·").performClick();
            assertEquals(ClubContentPresentation.Destination.SPORT,field(a,"clubDestination"));
            view=container.getChildAt(0);layout(view);Shadows.shadowOf(android.os.Looper.getMainLooper()).idle();view.scrollTo(0,220);
            Method open=MainActivity.class.getDeclaredMethod("openClubPage",ClubPageRepository.Page.class);open.setAccessible(true);open.invoke(a,ClubPageRepository.Page.YOUTH);
            call(a,"handleBack");assertEquals(ClubContentPresentation.Destination.SPORT,field(a,"clubDestination"));
            view=container.getChildAt(0);layout(view);Shadows.shadowOf(android.os.Looper.getMainLooper()).idle();assertEquals(220,view.getScrollY());
            call(a,"handleBack");assertEquals("CLUB",field(a,"current").toString());
            view=container.getChildAt(0);layout(view);Shadows.shadowOf(android.os.Looper.getMainLooper()).idle();assertEquals(180,view.getScrollY());
            assertVisibleText(view,"18:30–20:00");
        }
    }

    @Test public void failedRefreshAndChangedHeadingsStillExposeNativeSource()throws Exception{
        try(var controller=Robolectric.buildActivity(MainActivity.class).setup()){
            MainActivity a=controller.get();SharedPreferences prefs=configure(a);
            cache(prefs,ClubPageRepository.Page.ABOUT,"<h2>Neues Trainingsangebot</h2><p>Neuer Treffpunkt um 18:45 Uhr.</p>");
            set(a,"clubDestination",ClubContentPresentation.Destination.TRAINING);
            @SuppressWarnings("unchecked") Set<ClubPageRepository.Page> failed=(Set<ClubPageRepository.Page>)field(a,"clubFailed");failed.add(ClubPageRepository.Page.ABOUT);
            View view=screen(a);a.setContentView(view);layout(view);assertVisibleText(view,"18:45 Uhr");assertEquals(0,accordions(view));
        }
    }

    @Test @Config(qualifiers="w320dp-h800dp-xhdpi")
    public void articleSpacingIsConvertedToDevicePixelsOnlyOnce()throws Exception{
        try(var controller=Robolectric.buildActivity(MainActivity.class).setup()){
            MainActivity a=controller.get();SharedPreferences prefs=configure(a);
            cache(prefs,ClubPageRepository.Page.ABOUT,ClubContentParserTest.ABOUT.replaceAll("<img[^>]*>",""));
            set(a,"clubDestination",ClubContentPresentation.Destination.SPORT);
            View view=screen(a);a.setContentView(view);
            float density=a.getResources().getDisplayMetrics().density;assertEquals(2f,density,0.01f);
            int width=Math.round(320*density),height=Math.round(800*density);
            view.measure(View.MeasureSpec.makeMeasureSpec(width,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(height,View.MeasureSpec.EXACTLY));view.layout(0,0,width,height);
            TextView content=find(view,"340 kg");
            assertEquals(Math.round(8*density),content.getPaddingTop());assertEquals(Math.round(8*density),content.getPaddingBottom());
            View boat=(View)content.getParent();
            var margins=(ViewGroup.MarginLayoutParams)boat.getLayoutParams();
            assertEquals(Math.round(8*density),margins.topMargin);assertEquals(Math.round(4*density),margins.bottomMargin);
            checkTextBounds(view);
        }
    }

    @Test public void youthUsesBoardSourceContactAndHistoryKeepsPdfExternal()throws Exception{
        try(var controller=Robolectric.buildActivity(MainActivity.class).setup()){
            MainActivity a=controller.get();SharedPreferences prefs=configure(a);prefs.edit().putString("ui_language","de").commit();
            cache(prefs,ClubPageRepository.Page.YOUTH,"<p>Jeweils im Herbst lernen Kinder Knoten.</p>");
            cache(prefs,ClubPageRepository.Page.BOARD,"<div class='wp-block-column'><h3>Jungpontonier-Leiter</h3><p>Testperson <a href='mailto:test@example.org'>E-Mail</a></p></div>");
            set(a,"clubDestination",ClubContentPresentation.Destination.YOUTH);View view=screen(a);a.setContentView(view);layout(view);
            TextView contact=find(view,"Testperson");assertNotNull(contact);
            Spanned text=(Spanned)contact.getText();text.getSpans(0,text.length(),ClickableSpan.class)[0].onClick(contact);
            var email=Shadows.shadowOf(a).getNextStartedActivity();assertEquals(android.content.Intent.ACTION_SENDTO,email.getAction());assertEquals("mailto:test@example.org",email.getDataString());
            cache(prefs,ClubPageRepository.Page.HISTORY,"<p>Jubiläumsbuch 1896 – 1996.</p><a href='/wp-content/uploads/test-book.pdf'>Buch</a>");
            set(a,"clubDestination",ClubContentPresentation.Destination.HISTORY);view=screen(a);a.setContentView(view);layout(view);
            find(view,"Jubiläumsbuch (PDF)").performClick();var pdf=Shadows.shadowOf(a).getNextStartedActivity();
            assertEquals("https://www.pfvr.ch/wp-content/uploads/test-book.pdf",pdf.getDataString());
        }
    }
}
