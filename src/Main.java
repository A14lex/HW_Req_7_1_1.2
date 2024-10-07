import java.io.BufferedOutputStream;
import java.io.IOException;

import java.util.logging.LogRecord;

public class Main {

    public static void main(String[] args) throws IOException {
        Server server = new Server();
        server.addHandler("GET", "/messages", new Handler() {
            //есть еще какой-то встроенный Handler что ли...
            

            @Override
            public void handle(Server.Request request, BufferedOutputStream responseStream) {


            }
        });
        server.initialize(9998);

        server.start1();

    }
}
