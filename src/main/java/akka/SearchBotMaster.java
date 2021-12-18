package akka;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.PoisonPill;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class SearchBotMaster extends AbstractActor {
    ConcurrentLinkedQueue<SearchBotMessage> results = new ConcurrentLinkedQueue<>();
    AtomicInteger counter = new AtomicInteger(0);
    int length;

    private void processFiles(String pathToPackage) throws IOException {
        List<Path> paths = Files.walk(Paths.get(pathToPackage)).filter(Files::isRegularFile).collect(Collectors.toList());
        length = paths.size();
        for (Path path : paths) {
            ActorRef actorRef = getContext().actorOf(SearchBotMapper.props());
            actorRef.tell(path.toString(), getSelf());
            actorRef.tell(PoisonPill.getInstance(), ActorRef.noSender());
            while (true) {
                if (actorRef.isTerminated()) {
                    break;
                }
            }
            counter.incrementAndGet();
        }
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(String.class, this::processFiles).match(SearchBotMessage.class, reflection -> {
            results.add(reflection);
            if (counter.get() == length) {
                getSelf().tell(PoisonPill.getInstance(), getSelf());
            }
        }).build();
    }

    @Override
    public void postStop() {
        HashMap<String, HashSet<String>> result = new HashMap<>();
        for (SearchBotMessage reflection: results) {
            HashSet<String> value = result.getOrDefault(reflection.getParent(), new HashSet<>());
            value.add(reflection.getChild());
            result.put(reflection.getParent(), value);
        }
        result.keySet().forEach(k -> System.out.println(k + " базовый для " + result.get(k)));
     //   System.out.println(result);
        getContext().getSystem().terminate();
    }
}
