package ch.pfvr.internapp;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/** Public, read-only WordPress pages. No scripts, forms or personal Intern data. */
final class ClubPageRepository {
    enum Page {
        ABOUT("verein", "Über den Verein", "https://www.pfvr.ch/verein/"),
        YOUTH("jungpontoniere", "Jungpontoniere", "https://www.pfvr.ch/verein/jungpontoniere/"),
        BOARD("vorstand", "Vorstand", "https://www.pfvr.ch/verein/vorstand/"),
        HISTORY("geschichte", "Geschichte", "https://www.pfvr.ch/geschichte/"),
        CONTACT("kontakt", "Kontakt", "https://www.pfvr.ch/kontakt/");

        final String slug, label, url;
        Page(String slug, String label, String url) { this.slug=slug; this.label=label; this.url=url; }
        String cacheKey() { return "club_page_"+slug; }
        String updatedKey() { return cacheKey()+"_updated"; }
        String endpoint() { return "https://www.pfvr.ch/wp-json/wp/v2/pages?slug="+slug+"&_fields=slug,link,title,content,modified"; }
    }

    static final long CACHE_AGE_MS=24L*60L*60L*1000L;
    record Content(String title, String html, String preview, String modified, ClubContentParser.Layout layout) {}
    private ClubPageRepository() {}

    static String fetchRaw(Page page) throws Exception {
        HttpURLConnection connection=(HttpURLConnection)new URL(page.endpoint()).openConnection();
        try {
            connection.setConnectTimeout(7000);
            connection.setReadTimeout(12000);
            connection.setUseCaches(false);
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("User-Agent", "PFVR-Rheinfelden-App");
            if(connection.getResponseCode()/100!=2)throw new Exception("HTTP "+connection.getResponseCode());
            try(var input=connection.getInputStream();var output=new java.io.ByteArrayOutputStream()) {
                byte[] buffer=new byte[8192]; int count;
                while((count=input.read(buffer))!=-1) {
                    if(output.size()+count>1_000_000)throw new Exception("Page too large");
                    output.write(buffer,0,count);
                }
                return new String(output.toByteArray(),StandardCharsets.UTF_8);
            }
        } finally { connection.disconnect(); }
    }

    static Content parse(Page page, String raw) throws Exception {
        JSONArray rows=new JSONArray(raw);
        for(int i=0;i<rows.length();i++) {
            JSONObject row=rows.getJSONObject(i);
            if(!page.slug.equals(row.optString("slug"))||!page.url.equals(row.optString("link")))continue;
            String title=Jsoup.parse(row.getJSONObject("title").getString("rendered")).text();
            var document=Jsoup.parseBodyFragment(row.getJSONObject("content").getString("rendered"),page.url);
            document.select("script,style,form,iframe,object,embed,svg,nav,button,input,select,textarea").remove();
            // Rich text stays passive. Selected source photos are rendered separately as native images.
            Safelist allowed=Safelist.basic().addTags("h1","h2","h3","h4","h5","h6")
                    .addProtocols("a","href","tel","mailto");
            String html=Jsoup.clean(document.body().html(),page.url,allowed);
            String text=Jsoup.parseBodyFragment(html).text();
            if(title.isBlank()||text.isBlank())throw new Exception("Empty public page");
            var paragraphs=Jsoup.parseBodyFragment(html).select("p");
            ClubContentParser.Layout layout=ClubContentParser.extract(page,document.body());
            String preview=layout.intro().isBlank()?(paragraphs.isEmpty()?text:paragraphs.first().text()):Jsoup.parseBodyFragment(layout.intro()).text();
            if(preview.length()>360)preview=preview.substring(0,preview.lastIndexOf(' ',360)>0?preview.lastIndexOf(' ',360):360)+" …";
            String modified="";
            try{modified=java.time.LocalDateTime.parse(row.optString("modified")).toLocalDate().toString();}catch(Exception ignored){}
            return new Content(title,html,preview,modified,layout);
        }
        throw new Exception("Expected public page missing");
    }

    static Page pageForUrl(String value) {
        try {
            URI uri=URI.create(value);
            if(!"https".equals(uri.getScheme())||!"www.pfvr.ch".equals(uri.getHost())||uri.getUserInfo()!=null||uri.getPort()!=-1)return null;
            for(Page page:Page.values())if(URI.create(page.url).getPath().equals(uri.getPath()))return page;
        } catch(IllegalArgumentException ignored) {}
        return null;
    }
}
