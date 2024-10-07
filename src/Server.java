import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/*
2209 20:45 последнее изменение: сделал все сокеты общими для сервера, чтобы не передавать их в методы потоков
 */

public class Server {
    List<String> validPaths = List.of("/index.html", "/spring.svg", "/spring.png", "/resources.html", "/styles.css", "/app.js", "/links.html", "/forms.html", "/classic.html", "/events.html", "/events.js");
//ArrayList <String> validPaths = new ArrayList<>();

    ServerSocket serverSocket;
    Socket socket;
    static int countAccept = 0;

    public void initialize(int i) {
        try {
            serverSocket = new ServerSocket(i);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void start1() {

        while (true) {
            countAccept++;

            System.out.println(LocalDateTime.now().toString() + "start1: запускаю newAccept (ожидание нового подключения) " + countAccept);


            newAccept1();
        }
    }

    public String waitAccept() {
        try (
                final BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                final var out = new BufferedOutputStream(socket.getOutputStream());
        ) {
            final var requestLine = in.readLine();
            System.out.println(LocalDateTime.now().toString() + "requestLine = " + requestLine);
            if (requestLine == null) {
                out.flush();
                return null;
            }
            final var parts = requestLine.split(" ");

            if (parts.length != 3) {

                return null;
            }

            final var path = parts[1];
            if (!validPaths.contains(path)) {
                out.write((
                        "HTTP/1.1 404 Not Found\r\n" +
                                "Content-Length: 0\r\n" +
                                "Connection: close\r\n" +
                                "\r\n"
                ).getBytes());
                out.flush();
                return null;
            }

            final var filePath = Path.of(".", "public", path);
            final var mimeType = Files.probeContentType(filePath);


            if (path.equals("/classic.html")) {
                final var template = Files.readString(filePath);
                final var content = template.replace(
                        "{time}",
                        LocalDateTime.now().toString()
                ).getBytes();
                out.write((
                        "HTTP/1.1 200 OK\r\n" +
                                "Content-Type: " + mimeType + "\r\n" +
                                "Content-Length: " + content.length + "\r\n" +
                                "Connection: close\r\n" +
                                "\r\n"
                ).getBytes());
                out.write(content);
                out.flush();
                return null;
            }

            final var length = Files.size(filePath);
            out.write((
                    "HTTP/1.1 200 OK\r\n" +
                            "Content-Type: " + mimeType + "\r\n" +
                            "Content-Length: " + length + "\r\n" +
                            "Connection: close\r\n" +
                            "\r\n"
            ).getBytes());
            Files.copy(filePath, out);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String newAccept1() {
        ExecutorService threadPool = Executors.newFixedThreadPool(64);
        Callable<String> myCallable = new Callable<String>() {
            @Override
            public String call() throws Exception {
                return waitAccept();
            }
        };
        try {
            socket = serverSocket.accept();

            Future<String> task = threadPool.submit(myCallable);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
//    addHandler("GET", "/messages", new Handler()
    public void addHandler(String method, String messages, Handler handler){
        //нужно создать объект Request, чтоб было что передавать в метод handler
        Request request = new Request(method, messages);



    }

    public static class Request{
        private String requestMethod;
        private String requestMessage;

        public Request(String requestMethod, String requestMessage) {
            this.requestMethod = requestMethod;
            this.requestMessage = requestMessage;
        }
    }


}
