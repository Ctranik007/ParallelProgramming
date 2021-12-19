package akka;

public class SearchBotMessage {
    private final String parent;
    private final String child;

    @Override
    public String toString() {
        return getParent() + " " + getChild();
    }

    public SearchBotMessage(String parent, String child) {
        this.parent = parent;
        this.child = child;
    }

    public String getChild() {
        return child;
    }

    public String getParent() {
        return parent;
    }
}
