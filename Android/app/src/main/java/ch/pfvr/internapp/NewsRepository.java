package ch.pfvr.internapp;

import android.text.Html;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

final class NewsRepository {
    static final String PREF_CACHE="news_cache";
    static final String PREF_UPDATED="news_updated";
    static final String ENDPOINT="https://www.pfvr.ch/wp-json/wp/v2/posts?per_page=20&_fields=id,date,link,title,excerpt";
    private NewsRepository(){}

    static String fetchRaw() throws Exception{
        HttpURLConnection connection=(HttpURLConnection)new URL(ENDPOINT).openConnection();
        try{
            connection.setConnectTimeout(7000);connection.setReadTimeout(12000);connection.setUseCaches(true);
            connection.setRequestProperty("Accept","application/json");connection.setRequestProperty("User-Agent","PFVR-Rheinfelden-App/"+BuildConfig.VERSION_NAME);
            if(connection.getResponseCode()/100!=2)throw new Exception("HTTP "+connection.getResponseCode());
            try(BufferedReader reader=new BufferedReader(new InputStreamReader(connection.getInputStream(),StandardCharsets.UTF_8))){StringBuilder out=new StringBuilder();String line;while((line=reader.readLine())!=null)out.append(line);return out.toString();}
        }finally{connection.disconnect();}
    }

    static List<Article> parse(String raw) throws Exception{
        List<Article> result=new ArrayList<>();if(raw==null||raw.trim().isEmpty())return result;
        JSONArray rows=new JSONArray(raw);ZoneId zone=ZoneId.of("Europe/Zurich");
        for(int i=0;i<rows.length();i++){
            JSONObject row=rows.optJSONObject(i);if(row==null)continue;String link=row.optString("link","").trim();
            JSONObject titleObject=row.optJSONObject("title"),excerptObject=row.optJSONObject("excerpt");
            String title=plain(titleObject==null?"":titleObject.optString("rendered",""));String excerpt=plain(excerptObject==null?"":excerptObject.optString("rendered",""));
            if(title.isBlank()||link.isBlank())continue;long publishedAt=0L;
            try{LocalDateTime local=LocalDateTime.parse(row.optString("date",""),DateTimeFormatter.ISO_LOCAL_DATE_TIME);publishedAt=local.atZone(zone).toInstant().toEpochMilli();}catch(Exception ignored){}
            result.add(new Article(row.optLong("id",0L),publishedAt,title,excerpt,link));
        }
        return result;
    }

    private static String plain(String html){if(html==null||html.isBlank())return "";return Html.fromHtml(html,Html.FROM_HTML_MODE_LEGACY).toString().replace('\u00a0',' ').replaceAll("\\s+"," ").trim();}
    static final class Article{final long id,publishedAt;final String title,excerpt,link;Article(long id,long publishedAt,String title,String excerpt,String link){this.id=id;this.publishedAt=publishedAt;this.title=title;this.excerpt=excerpt;this.link=link;}}
}
