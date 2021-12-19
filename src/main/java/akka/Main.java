package akka;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

public class Main {
    public static void main(String[] args) {

        ActorSystem actorSystem = ActorSystem.create("SearchBot");
        Props propsMaster = Props.create(SearchBotMaster.class);
        actorSystem.actorOf(propsMaster).tell("G:\\учёба университет\\7 семестр\\параллельное\\spring-framework-main", ActorRef.noSender());
    }
}
