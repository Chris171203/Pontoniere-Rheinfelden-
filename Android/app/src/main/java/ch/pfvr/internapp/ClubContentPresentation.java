package ch.pfvr.internapp;

import org.jsoup.Jsoup;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/** App destinations are independent of the WordPress source page and its cache. */
final class ClubContentPresentation {
    enum Destination {
        TRAINING(ClubPageRepository.Page.ABOUT,"Trainingsinfos",List.of("training")),
        SPORT(ClubPageRepository.Page.ABOUT,"Boote & Sport",List.of("boats","sport")),
        LIFE(ClubPageRepository.Page.ABOUT,"Vereinsleben",List.of("life")),
        YOUTH(ClubPageRepository.Page.YOUTH,"Jungpontoniere",List.of()),
        BOARD(ClubPageRepository.Page.BOARD,"Vorstand",List.of()),
        HISTORY(ClubPageRepository.Page.HISTORY,"Geschichte",List.of()),
        CONTACT(ClubPageRepository.Page.CONTACT,"Kontakt",List.of());
        final ClubPageRepository.Page page;
        final String label;
        final List<String> sections;
        Destination(ClubPageRepository.Page page,String label,List<String> sections){this.page=page;this.label=label;this.sections=sections;}
        static Destination forPage(ClubPageRepository.Page page){
            return switch(page){case ABOUT->LIFE;case YOUTH->YOUTH;case BOARD->BOARD;case HISTORY->HISTORY;case CONTACT->CONTACT;};
        }
    }
    record Training(String label,String season,String days,String time,String location,String fallback) {}
    private ClubContentPresentation() {}

    static List<ClubContentParser.Section> sections(ClubPageRepository.Content content,Destination destination){
        return content.layout().sections().stream().filter(s->destination.sections.isEmpty()||destination.sections.contains(s.id())).collect(java.util.stream.Collectors.toList());
    }
    static String overviewIntro(ClubPageRepository.Content content){
        String intro=content.layout().intro();
        if(intro.isBlank())return firstSentence(content.preview());
        var paragraphs=Jsoup.parseBodyFragment(intro).select("p");
        String first=paragraphs.isEmpty()?"":firstSentence(paragraphs.first().text());
        // First founding sentence plus the sport introduction, without historical member totals.
        for(var p:paragraphs)if(p.text().startsWith("Pontonier ist"))return first+" "+firstSentence(p.text());
        return first;
    }
    private static String firstSentence(String value){
        var end=Pattern.compile("[.!?](?:\\s|$)").matcher(value);
        return end.find()?value.substring(0,end.start()+1):value;
    }
    static List<Training> training(ClubPageRepository.Content content){
        List<Training> result=new ArrayList<>();
        for(var group:sections(content,Destination.TRAINING)){
            List<ClubContentParser.Section> seasons=group.children().isEmpty()?List.of(group):group.children();
            for(var section:seasons){
                String text=Jsoup.parseBodyFragment(section.html()).text();
                String season=match("\\(([^()]+)\\)",text);
                String days=match("\\bam\\s+(.+?)(?:,|\\s+(?:von|um)\\s+)",text);
                var time=Pattern.compile("\\b([0-2]?\\d:[0-5]\\d)\\s*Uhr").matcher(text);
                List<String> times=new ArrayList<>();while(time.find()&&times.size()<2)times.add(time.group(1));
                String hours=String.join("–",times);
                boolean complete=!days.isBlank()&&!hours.isBlank()&&!section.location().isBlank();
                result.add(new Training(section.title(),season,days,hours,section.location(),complete?"":firstSentence(text)));
            }
        }
        return List.copyOf(result);
    }
    private static String match(String regex,String value){var m=Pattern.compile(regex).matcher(value);return m.find()?m.group(1):"";}
}
