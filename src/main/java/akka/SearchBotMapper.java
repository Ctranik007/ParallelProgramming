package akka;

import akka.actor.AbstractActor;
import akka.actor.Props;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SearchBotMapper extends AbstractActor {

    private LinkedList<String> searchRegular(String str) {
        Pattern pattern = Pattern.compile("(class|interface)(\\s+)(\\w+)(<.*>)?(\\s+extends(\\s+\\w+(,\\s+\\w+)*))?((\\s+implements)(\\s+\\w+(,\\s+\\w+)*))*");
        Matcher matcher = pattern.matcher(str);
        LinkedList<String> list = new LinkedList<>();
        if (matcher.find()) {
            if (matcher.group(6) != null) {
                for (String s : matcher.group(6).split(",")) {
                    list.add(s + "-" + matcher.group(3));
                }
            }
            if (matcher.group(10) != null) {
                for (String s : matcher.group(10).split(",")) {
                    list.add(s + "-" + matcher.group(3));
                }
            }
        }
        return list;
    }

    public static Props props() {
        return Props.create(SearchBotMapper.class);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(String.class, message -> {
            try {

                Files.lines(Path.of(message)).map(s -> this.searchRegular(s)).flatMap(List::stream)
                        .forEach(x -> getSender().tell(new SearchBotMessage(x.split("-")[0], x.split("-")[1]), getSelf()));
                String h = "ffgg";
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).build();
    }
}
