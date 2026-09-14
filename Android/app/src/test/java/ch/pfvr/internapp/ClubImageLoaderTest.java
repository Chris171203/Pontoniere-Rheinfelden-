package ch.pfvr.internapp;

import android.graphics.Bitmap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.GraphicsMode;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk=28)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public class ClubImageLoaderTest {
    private static final String URL="https://www.pfvr.ch/wp-content/uploads/test.jpg";
    static byte[] samplePhoto(){
        Bitmap b=Bitmap.createBitmap(1500,800,Bitmap.Config.ARGB_8888);b.eraseColor(0xff197e9a);
        ByteArrayOutputStream out=new ByteArrayOutputStream();assertTrue(b.compress(Bitmap.CompressFormat.PNG,100,out));b.recycle();return out.toByteArray();
    }
    @Test public void storesReducedImageAndReusesItAfterRestartAndNetworkFailure()throws Exception {
        var dir=Files.createTempDirectory("club-image-test").toFile();AtomicInteger requests=new AtomicInteger();
        byte[] photo=samplePhoto();
        try(var images=new ClubImageLoader(dir,url->{requests.incrementAndGet();return photo;})){
            Bitmap first=images.load(URL);assertNotNull(first);assertTrue(first.getWidth()<=1024);
            assertNotNull(images.load(URL));assertEquals(1,requests.get());
        }
        assertNotNull(dir.listFiles());for(var file:dir.listFiles())assertTrue(file.setLastModified(1L));
        try(var offline=new ClubImageLoader(dir,url->{throw new Exception("offline");})){
            assertNotNull(offline.load(URL));offline.clear();assertNull(offline.read(URL));
        }
    }
    @Test public void badDownloadsDoNotReplaceValidCachedImageAndUnsupportedUrlsNeverFetch()throws Exception {
        var dir=Files.createTempDirectory("club-image-test").toFile();byte[] photo=samplePhoto();
        try(var images=new ClubImageLoader(dir,url->photo)){assertNotNull(images.load(URL));}
        for(var file:dir.listFiles())assertTrue(file.setLastModified(1L));
        AtomicInteger requests=new AtomicInteger();
        try(var images=new ClubImageLoader(dir,url->{requests.incrementAndGet();return "not an image".getBytes();})){
            assertNotNull(images.load(URL));assertEquals(1,requests.get());
            assertNull(images.load("https://example.org/track.png"));assertEquals(1,requests.get());images.clear();
        }
    }
    @Test public void clearingDuringDownloadCannotRepopulateCache()throws Exception {
        var dir=Files.createTempDirectory("club-image-test").toFile();byte[] photo=samplePhoto();
        CountDownLatch started=new CountDownLatch(1),release=new CountDownLatch(1);
        try(var images=new ClubImageLoader(dir,url->{started.countDown();assertTrue(release.await(5,TimeUnit.SECONDS));return photo;})){
            Thread download=new Thread(()->images.load(URL));download.start();
            assertTrue(started.await(5,TimeUnit.SECONDS));images.clear();release.countDown();download.join(5000);
            assertFalse(download.isAlive());assertNull(images.read(URL));
        }
    }
    @Test public void imagesLoadBothOnInitialAttachAndWhenAddedToOpenSection()throws Exception {
        byte[] photo=samplePhoto();var dir=Files.createTempDirectory("club-image-test").toFile();
        try(var controller=org.robolectric.Robolectric.buildActivity(android.app.Activity.class).setup();var images=new ClubImageLoader(dir,url->photo)){
            var activity=controller.get();var parent=new android.widget.LinearLayout(activity);activity.setContentView(parent);
            var initial=new android.widget.ImageView(activity);var initialStatus=new android.widget.TextView(activity);
            images.bind(initial,initialStatus,URL,"unavailable");parent.addView(initial);parent.addView(initialStatus);
            var opened=new android.widget.ImageView(activity);var openedStatus=new android.widget.TextView(activity);
            parent.addView(opened);parent.addView(openedStatus);assertTrue(opened.isAttachedToWindow());
            images.bind(opened,openedStatus,URL,"unavailable");
            var field=ClubImageLoader.class.getDeclaredField("worker");field.setAccessible(true);
            var executor=(java.util.concurrent.ExecutorService)field.get(images);executor.shutdown();assertTrue(executor.awaitTermination(5,TimeUnit.SECONDS));
            org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idle();
            assertNotNull(initial.getDrawable());assertNotNull(opened.getDrawable());
            assertEquals(android.view.View.GONE,openedStatus.getVisibility());
            images.clear();
        }
    }
}
