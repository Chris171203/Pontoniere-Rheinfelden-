package ch.pfvr.internapp;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Safelist;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Source-based presentation only: unrecognised material remains in Content.html(). */
final class ClubContentParser {
    record Photo(String url,String caption,int width,int height) {}
    record Section(String id,String title,boolean appTitle,String html,Photo photo,List<Section> children,String location) {}
    record Layout(String intro,Photo hero,List<Section> sections,List<Section> milestones) {
        static final Layout EMPTY=new Layout("",null,List.of(),List.of());
    }
    private static final Safelist SAFE=Safelist.basic().addTags("h2","h3","h4").addProtocols("a","href","tel","mailto");
    private ClubContentParser() {}

    static Layout extract(ClubPageRepository.Page page,Element body) {
        return switch(page) {
            case ABOUT -> about(body);
            case YOUTH -> youth(body);
            case BOARD -> board(body);
            case HISTORY -> history(body);
            default -> Layout.EMPTY;
        };
    }
    static String clean(String html) {return Jsoup.clean(html,"https://www.pfvr.ch/",SAFE);}
    private static String paragraph(String text) {return new Element("p").text(text).outerHtml();}
    private static Section section(String id,String title,boolean app,String html,Photo photo) {
        return new Section(id,title,app,clean(html),photo,List.of(),"");
    }
    private static Section group(String id,String title,String html,List<Section> children) {
        return new Section(id,title,true,clean(html),null,List.copyOf(children),"");
    }
    private static List<Element> blocks(Element body) {return body.select("h2,h3,p,img");}
    private static int heading(List<Element> blocks,String text) {
        for(int i=0;i<blocks.size();i++)if(blocks.get(i).tagName().matches("h[23]")&&blocks.get(i).text().equalsIgnoreCase(text))return i;
        return -1;
    }
    private static String paragraphs(List<Element> blocks,int from,int to) {
        StringBuilder out=new StringBuilder();
        for(int i=Math.max(0,from);i<to;i++)if(blocks.get(i).tagName().equals("p")&&!blocks.get(i).text().isBlank())out.append(blocks.get(i).outerHtml());
        return clean(out.toString());
    }
    private static Photo firstPhoto(List<Element> blocks,int from,int to) {
        for(int i=Math.max(0,from);i<to;i++)if(blocks.get(i).tagName().equals("img")){
            Photo p=photo(blocks.get(i));if(p!=null)return p;
        }
        return null;
    }

    static boolean allowedPhoto(String value) {
        try {
            URI u=URI.create(value);String path=u.getPath();
            return "https".equals(u.getScheme())&&"www.pfvr.ch".equals(u.getHost())&&u.getUserInfo()==null&&u.getPort()==-1
                    &&path!=null&&path.startsWith("/wp-content/uploads/")&&!path.contains("..")
                    &&path.toLowerCase(Locale.ROOT).matches(".*\\.(jpg|jpeg|png|webp)");
        }catch(IllegalArgumentException e){return false;}
    }
    private static Photo photo(Element image) {
        String url=image.absUrl("src");
        // Choose a provided medium image; never invent a WordPress derivative URL.
        int chosen=Integer.MAX_VALUE;
        for(String candidate:image.attr("srcset").split(",")){
            String[] parts=candidate.trim().split("\\s+");
            if(parts.length!=2||!parts[1].matches("[0-9]+w"))continue;
            try{int width=Integer.parseInt(parts[1].replace("w",""));
                String target=URI.create("https://www.pfvr.ch/").resolve(parts[0]).toString();
                if(width>=640&&width<chosen&&allowedPhoto(target)){url=target;chosen=width;}
            }catch(IllegalArgumentException ignored){}
        }
        if(!allowedPhoto(url))return null;
        String caption=image.attr("alt");
        Element figure=image.closest("figure");
        if(figure!=null&&figure.selectFirst("figcaption")!=null)caption=figure.selectFirst("figcaption").text();
        int width=0,height=0;
        try{width=Integer.parseInt(image.attr("width"));height=Integer.parseInt(image.attr("height"));}catch(NumberFormatException ignored){}
        return new Photo(url,caption,width,height);
    }

    private static Layout about(Element body) {
        List<Element> b=blocks(body);int general=heading(b,"Allgemeine Informationen"),training=heading(b,"Training"),sport=heading(b,"Sport");
        if(general<0||training<=general||sport<=training)return Layout.EMPTY;
        String intro="",life="";List<Section> milestones=new ArrayList<>(),sections=new ArrayList<>();
        for(int i=general+1;i<training;i++){
            Element e=b.get(i);String text=e.text();
            if(e.tagName().equals("p")&&text.matches(".*\\b[12][0-9]{3} gegründet\\..*")){
                intro=clean(e.outerHtml());
                var year=java.util.regex.Pattern.compile("([12][0-9]{3}) gegründet").matcher(text);
                if(year.find())milestones.add(section("founding",year.group(1),false,e.outerHtml(),null));
            }
            if(e.tagName().equals("p")&&(text.startsWith("Das gesellschaftliche Leben")||text.startsWith("Unter dem Jahr")))life+=e.outerHtml();
            if(e.tagName().equals("img")){
                Photo p=photo(e);
                if(p!=null&&p.caption().contains("Einweihung")){
                    var date=java.util.regex.Pattern.compile("\\b[0-9]{2}\\.[0-9]{2}\\.[12][0-9]{3}\\b").matcher(p.caption());
                    if(date.find())milestones.add(section("club-photo",date.group(),false,paragraph(p.caption()),p));
                }
            }
        }
        Photo hero=firstPhoto(b,general+1,training);
        String trainingHtml=paragraphs(b,training+1,sport);
        int winter=trainingHtml.indexOf("Während der Wintersaison");
        if(winter>0&&trainingHtml.contains("In der Sommersaison")){
            String summer=clean(trainingHtml.substring(0,winter));
            String cold=clean("<p>"+trainingHtml.substring(winter));
            Section a=new Section("summer","Sommertraining",true,summer,firstPhoto(b,training+1,sport),List.of(),summer.contains("Depot der Pontoniere Rheinfelden")?"Depot der Pontoniere Rheinfelden":"");
            Section z=new Section("winter","Wintertraining",true,cold,null,List.of(),cold.contains("Schützenturnhalle")?"Schützenturnhalle Rheinfelden":"");
            sections.add(group("training","Training","",List.of(a,z)));
        }else if(!trainingHtml.isBlank())sections.add(section("training","Training",true,trainingHtml,firstPhoto(b,training+1,sport)));

        List<Section> boats=new ArrayList<>();
        for(String name:List.of("Weidling","Boot")){
            int h=heading(b,name);
            if(h<0)continue;
            for(int i=h+1;i<b.size()&&!b.get(i).tagName().matches("h[23]");i++){
                Element p=b.get(i);
                if(p.tagName().equals("p")&&!p.text().isBlank()){
                    // The first description belongs to this boat; following manoeuvres do not.
                    Photo image=firstPhoto(b,h+1,Math.min(b.size(),i+1+p.select("img").size()));
                    boats.add(section("boat-"+name.toLowerCase(Locale.ROOT),name,false,p.outerHtml(),image));break;
                }
            }
        }
        if(!boats.isEmpty())sections.add(group("boats","Unsere Boote","",boats));
        List<Section> techniques=new ArrayList<>();String sportIntro="";int introCount=0;
        for(int i=sport+1;i<b.size();i++){
            Element e=b.get(i);if(!e.tagName().equals("p")||e.text().isBlank())continue;
            String text=e.text();
            if(introCount<2&&(text.startsWith("Pontonier ist")||text.startsWith("Das Schiff wird"))){sportIntro+=e.outerHtml();introCount++;}
            int dash=text.indexOf("– ");
            if(dash>=0){
                String title=text.substring(dash+2).split("[(:–]",2)[0].trim();
                int end=i+1;while(end<b.size()&&!b.get(end).tagName().matches("p|h[23]"))end++;
                // Image-only paragraphs must also be considered, e.g. Spanntauwerfen.
                while(end<b.size()&&b.get(end).tagName().equals("p")&&b.get(end).text().isBlank())end++;
                while(end<b.size()&&b.get(end).tagName().equals("img"))end++;
                boolean multiple=text.indexOf("– ",dash+2)>=0;
                techniques.add(section("tech-"+title,multiple?"Weitere Disziplinen":title,multiple,e.outerHtml(),firstPhoto(b,i+1,end)));
            }else if(text.contains("Beim Sektionswettfahren")){
                int end=i+1;while(end<b.size()&&!b.get(end).tagName().matches("p|h[23]"))end++;
                techniques.add(section("section-racing","Sektionsfahren",true,e.outerHtml(),firstPhoto(b,i+1,end)));
            }
        }
        if(!sportIntro.isBlank()||!techniques.isEmpty())sections.add(group("sport","Pontoniersport erklärt",sportIntro,techniques));
        if(!life.isBlank())sections.add(section("life","Vereinsleben",true,life,hero));
        return new Layout(intro+clean(sportIntro),hero,List.copyOf(sections),List.copyOf(milestones));
    }

    private static Layout youth(Element body) {
        List<Section> sections=new ArrayList<>();
        for(Element p:body.select("p")){
            String html=p.html();int start=html.indexOf("Jeweils im Herbst");
            if(start>=0)sections.add(section("youth-training","Fahren und Knoten",true,"<p>"+html.substring(start)+"</p>",firstPhoto(blocks(body),0,blocks(body).size())));
            if(p.text().startsWith("Jeweils im Sommer veranstaltet")){
                // Leave old age/exam prerequisites in the explicitly dated full source only.
                int age=html.indexOf("Wer schon "),after=html.indexOf("Weitere Informationen",age);
                if(age>=0&&after>age)html=html.substring(0,age)+html.substring(after);
                else if(age>=0)continue;
                sections.add(section("camp","JP-Lager",true,"<p>"+html+"</p>",null));
            }
        }
        // The knot photo gets its original caption without duplicating the gallery.
        for(Element image:body.select("img")){
            Photo p=photo(image);
            if(p!=null&&p.caption().equals("Schnüren"))sections.add(section("knots",p.caption(),false,"",p));
        }
        return new Layout("",null,List.copyOf(sections),List.of());
    }

    private static Layout board(Element body) {
        List<Section> sections=new ArrayList<>();
        for(Element column:body.select(".wp-block-column")){
            Element heading=column.selectFirst("h3");if(heading==null)continue;
            String html="";
            for(Element p:column.select("p"))if(!p.text().startsWith("Bild folgt"))html+=p.outerHtml();
            if(html.isBlank())continue;
            sections.add(section("board-"+heading.text(),heading.text(),false,html,firstPhoto(blocks(column),0,blocks(column).size())));
        }
        return new Layout("",null,List.copyOf(sections),List.of());
    }

    private static Layout history(Element body) {
        List<Section> milestones=new ArrayList<>();String intro="";
        for(Element p:body.select("p"))if(p.text().contains("Jubiläumsbuch")&&p.text().contains("1896")&&p.text().contains("1996")){
            intro=clean(p.outerHtml());milestones.add(section("book","1996",false,intro,null));break;
        }
        List<Section> links=new ArrayList<>();
        for(Element a:body.select("a[href]")){
            String href=a.absUrl("href");
            if(href.endsWith(".pdf")&&links.stream().noneMatch(s->s.id().equals(href)))
                links.add(section(href,"Jubiläumsbuch (PDF)",true,new Element("a").attr("href",href).text(a.text()).outerHtml(),null));
            else if(href.contains("/vorstandsarchiv-tabelle/"))links.add(section(href,"Vorstandsarchiv",true,new Element("a").attr("href",href).text(a.text()).outerHtml(),null));
        }
        return new Layout("",null,List.copyOf(links),List.copyOf(milestones));
    }
}
