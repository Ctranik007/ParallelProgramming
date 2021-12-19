package akka;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.PoisonPill;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class SearchBotMaster extends AbstractActor {
    //потокобезопасные очереди и операции
    private final ConcurrentLinkedQueue<SearchBotMessage> results = new ConcurrentLinkedQueue<>();
    private final AtomicInteger counter = new AtomicInteger(0);
    private int length;
    private Instant start;

    private void giveTasks(String pathFiles) throws IOException {
        Files.find(Paths.get(pathFiles), 999, (p, bfa) -> bfa.isRegularFile()
                && p.getFileName().toString().matches(".*\\.java")).forEach(x -> {

            ActorRef actorRef = getContext().actorOf(SearchBotMapper.props());
            actorRef.tell(x.toString(), getSelf());
            actorRef.tell(PoisonPill.getInstance(), ActorRef.noSender());
            while (true) {
                if (actorRef.isTerminated()) {
                    break;
                }
            }
            counter.incrementAndGet();
        });
        length = counter.get();
    }

    @Override
    public Receive createReceive() {
        start = Instant.now();
        return receiveBuilder().match(String.class, pathFiles -> giveTasks(pathFiles)).match(SearchBotMessage.class, message -> {
            results.add(message);
            if (counter.get() == length) {
                getSelf().tell(PoisonPill.getInstance(), getSelf());
            }
        }).build();
    }

    @Override
    public void postStop() {
        HashMap<String, HashSet<String>> result = new HashMap<>();
        for (SearchBotMessage reflection : results) {
            HashSet<String> value = result.getOrDefault(reflection.getParent(), new HashSet<>());
            value.add(reflection.getChild());
            result.put(reflection.getParent(), value);
        }
        result.keySet().forEach(k -> System.out.println(k + " базовый для " + result.get(k)));
        Instant stop = Instant.now();
        Duration duration = Duration.between(start, stop);
        System.out.println(duration.toMillis());
        System.out.printf("%d H, %d M, %d S%n", duration.toHours(), duration.toMinutesPart(), duration.toSecondsPart());
        getContext().getSystem().terminate();
    }
}
