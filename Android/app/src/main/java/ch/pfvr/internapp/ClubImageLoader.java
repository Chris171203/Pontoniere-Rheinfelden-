package ch.pfvr.internapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Small, persistent public-photo cache; no Activity or private page data retained. */
final class ClubImageLoader implements AutoCloseable {
    private static final long DISK_LIMIT=16L*1024*1024;
    private final File directory;
    private final ExecutorService worker=Executors.newFixedThreadPool(2);
    private final Handler main=new Handler(Looper.getMainLooper());
    private volatile boolean closed;
    private volatile int generation;
    interface Fetcher {byte[] fetch(String url) throws Exception;}
    private final Fetcher fetcher;

    ClubImageLoader(Context context) {this(new File(context.getFilesDir(),"club-images"),ClubImageLoader::download);}
    ClubImageLoader(File directory,Fetcher fetcher) {this.directory=directory;this.fetcher=fetcher;}

    void bind(ImageView target,TextView status,String url,String unavailable) {
        WeakReference<ImageView> image=new WeakReference<>(target);
        WeakReference<TextView> label=new WeakReference<>(status);
        target.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener(){
            @Override public void onViewDetachedFromWindow(View view) {}
            @Override public void onViewAttachedToWindow(View view) {
                view.removeOnAttachStateChangeListener(this);
                if(closed)return;
                int requested=generation;
                worker.execute(()->{
                    Bitmap cached=read(url);
                    if(cached!=null)show(image,label,cached,unavailable,requested);
                    Bitmap loaded=load(url,requested);
                    if(loaded!=null||cached==null)show(image,label,loaded,unavailable,requested);
                });
            }
        });
    }
    private void show(WeakReference<ImageView> image,WeakReference<TextView> label,Bitmap bitmap,String unavailable,int requested) {
        main.post(()->{
            ImageView view=image.get();TextView status=label.get();
            if(closed||requested!=generation||view==null||status==null)return;
            if(bitmap!=null){view.setImageBitmap(bitmap);status.setVisibility(View.GONE);}
            else {view.setVisibility(View.GONE);status.setText(unavailable);}
        });
    }
    synchronized Bitmap read(String url) {
        if(!ClubContentParser.allowedPhoto(url))return null;
        return decodeFile(file(url));
    }
    Bitmap load(String url) {return load(url,generation);}
    private Bitmap load(String url,int requested) {
        if(!ClubContentParser.allowedPhoto(url)||closed)return null;
        File file=file(url);Bitmap cached=read(url);
        if(cached!=null&&System.currentTimeMillis()-file.lastModified()<ClubPageRepository.CACHE_AGE_MS)return cached;
        try {
            byte[] bytes=fetcher.fetch(url);
            if(bytes.length>5_000_000)throw new Exception("Image too large");
            BitmapFactory.Options options=new BitmapFactory.Options();options.inJustDecodeBounds=true;
            BitmapFactory.decodeByteArray(bytes,0,bytes.length,options);
            if(options.outWidth<=0||options.outHeight<=0||options.outWidth>16000||options.outHeight>16000)throw new Exception("Invalid image");
            options.inSampleSize=1;
            while(Math.max(options.outWidth,options.outHeight)/options.inSampleSize>1024)options.inSampleSize*=2;
            options.inJustDecodeBounds=false;
            Bitmap bitmap=BitmapFactory.decodeByteArray(bytes,0,bytes.length,options);
            if(bitmap==null)throw new Exception("Invalid image");
            synchronized(this){
                if(closed||requested!=generation)return cached;
                if(!directory.isDirectory()&&!directory.mkdirs())throw new Exception("Cache unavailable");
                File temp=File.createTempFile("photo-",".tmp",directory);
                try{
                    try(FileOutputStream output=new FileOutputStream(temp)){
                        if(!bitmap.compress(Bitmap.CompressFormat.JPEG,88,output))throw new Exception("Cannot save image");
                    }
                    if(!temp.renameTo(file))throw new Exception("Cannot replace image");
                }finally{if(temp.exists())temp.delete();}
                trim();
            }
            return bitmap;
        }catch(Exception ignored){return cached;}
    }
    private static Bitmap decodeFile(File file) {return file.isFile()?BitmapFactory.decodeFile(file.getAbsolutePath()):null;}
    private File file(String url) {
        try{
            byte[] digest=MessageDigest.getInstance("SHA-256").digest(url.getBytes(StandardCharsets.UTF_8));
            StringBuilder key=new StringBuilder();for(byte b:digest)key.append(String.format(java.util.Locale.ROOT,"%02x",b&255));
            return new File(directory,key+".jpg");
        }catch(Exception e){throw new IllegalStateException(e);}
    }
    private void trim() {
        File[] files=directory.listFiles();if(files==null)return;
        Arrays.sort(files,Comparator.comparingLong(File::lastModified));
        long size=0;for(File f:files)size+=f.length();
        int count=files.length;
        for(File f:files){if(size<=DISK_LIMIT&&count<=64)break;long length=f.length();if(f.delete()){size-=length;count--;}}
    }
    synchronized void clear() {
        generation++;
        File[] files=directory.listFiles();if(files!=null)for(File file:files)file.delete();
    }
    @Override public synchronized void close() {closed=true;worker.shutdownNow();main.removeCallbacksAndMessages(null);}

    private static byte[] download(String url) throws Exception {
        // Only the public upload path. Redirects are validated before every request.
        for(int redirect=0;redirect<4;redirect++){
            if(!ClubContentParser.allowedPhoto(url))throw new Exception("Invalid image URL");
            HttpURLConnection c=(HttpURLConnection)new URL(url).openConnection();
            try{
                c.setInstanceFollowRedirects(false);c.setConnectTimeout(7000);c.setReadTimeout(12000);
                c.setRequestProperty("User-Agent","PFVR-Rheinfelden-App");
                int code=c.getResponseCode();
                if(code>=300&&code<400){url=new URL(new URL(url),c.getHeaderField("Location")).toString();continue;}
                if(code!=200||c.getContentLengthLong()>5_000_000)throw new Exception("Image unavailable");
                try(var input=c.getInputStream();var output=new ByteArrayOutputStream()){
                    byte[] buffer=new byte[8192];int count;
                    while((count=input.read(buffer))!=-1){if(output.size()+count>5_000_000)throw new Exception("Image too large");output.write(buffer,0,count);}
                    return output.toByteArray();
                }
            }finally{c.disconnect();}
        }
        throw new Exception("Too many redirects");
    }
}
