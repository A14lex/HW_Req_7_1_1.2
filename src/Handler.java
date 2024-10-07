import java.io.BufferedOutputStream;

public interface Handler {
    public void handle(Server.Request request, BufferedOutputStream responseStream);

}
