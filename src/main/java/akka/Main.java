package akka;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

public class Main {
    public static void main(String[] args) {
        ActorSystem actorSystem = ActorSystem.create("test");
        Props props1 = Props.create(SearchBotMaster.class);
        actorSystem.actorOf(props1).tell("input", ActorRef.noSender());
    }
}
