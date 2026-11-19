import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.io.*;
import java.util.*;

public class EventManagement {

    static class Event {
        int id;
        String name, description, date;

        Event(int id, String name, String description, String date) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.date = date;
        }
    }

    static class Student {
        String name, email, department, event;

        Student(String name, String email, String department, String event) {
            this.name = name;
            this.email = email;
            this.department = department;
            this.event = event;
        }
    }

    static List<Event> events = new ArrayList<>();
    static List<Student> students = new ArrayList<>();
    static int nextId = 4;

    static String escape(String s) {
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    static Map<String, String> readForm(HttpExchange ex)
            throws IOException {

        String body = new String(
            ex.getRequestBody().readAllBytes(),
            StandardCharsets.UTF_8
        );

        Map<String, String> data = new HashMap<>();

        for (String pair : body.split("&")) {
            String[] parts = pair.split("=", 2);

            if (parts.length == 2) {
                data.put(
                    URLDecoder.decode(parts[0], StandardCharsets.UTF_8),
                    URLDecoder.decode(parts[1], StandardCharsets.UTF_8)
                );
            }
        }

        return data;
    }

    static String pageStart(String title) {
        return """
        <!DOCTYPE html>
        <html>
        <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>Eventify</title>
        <style>
            body {
                margin: 0;
                font-family: Arial;
                background: #f5f3fa;
                color: #302052;
            }
            header {
                background: #302052;
                color: white;
                padding: 22px 7%;
                display: flex;
                justify-content: space-between;
                flex-wrap: wrap;
            }
            header a {
                color: white;
                text-decoration: none;
                margin: 10px;
            }
            .hero {
                background: #e9e2f7;
                text-align: center;
                padding: 55px 15px;
            }
            .hero h1 { font-size: 38px; }
            .container {
                padding: 35px 7%;
                text-align: center;
            }
            .cards {
                display: flex;
                justify-content: center;
                flex-wrap: wrap;
                gap: 22px;
            }
            .card {
                background: white;
                width: 260px;
                padding: 22px;
                border-radius: 12px;
                box-shadow: 0 3px 12px #ddd;
            }
            input, select {
                width: 90%;
                padding: 12px;
                margin: 8px;
                border: 1px solid #ccc;
                border-radius: 6px;
                box-sizing: border-box;
            }
            button, .btn {
                background: #7049a6;
                color: white;
                border: none;
                padding: 12px 20px;
                border-radius: 6px;
                cursor: pointer;
                text-decoration: none;
                display: inline-block;
                margin: 5px;
            }
            button:hover, .btn:hover {
                background: #302052;
            }
            .form-box {
                background: white;
                max-width: 450px;
                padding: 25px;
                margin: auto;
                border-radius: 12px;
                box-shadow: 0 3px 12px #ddd;
            }
            table {
                width: 100%;
                background: white;
                border-collapse: collapse;
                margin-top: 20px;
            }
            th, td {
                border: 1px solid #ddd;
                padding: 12px;
                text-align: left;
            }
            th { background: #302052; color: white; }
            footer {
                background: #302052;
                color: white;
                text-align: center;
                padding: 20px;
                margin-top: 35px;
            }
        </style>
        </head>
        <body>
        <header>
            <h2>EVENTIFY</h2>
            <nav>
                <a href="/">Home</a>
                <a href="/#events">Events</a>
                <a href="/#register">Register</a>
                <a href="/admin">Admin Dashboard</a>
            </nav>
        </header>
        """;
    }

    static String homePage() {
        StringBuilder html = new StringBuilder(pageStart("Eventify"));

        html.append("""
        <section class="hero">
            <h1>Welcome to Eventify!</h1>
            <p>Discover, Participate and Enjoy College Events.</p>
            <a class="btn" href="#events">Explore Events</a>
        </section>

        <section class="container" id="events">
            <h2>Upcoming College Events</h2>
            <div class="cards">
        """);

        synchronized (events) {
            for (Event e : events) {
                html.append("<div class='card'>");
                html.append("<h3>").append(escape(e.name)).append("</h3>");
                html.append("<p>").append(escape(e.description)).append("</p>");
                html.append("<p>Date: ").append(escape(e.date)).append("</p>");
                html.append("<a class='btn' href='#register'>Register Now</a>");
                html.append("</div>");
            }
        }

        html.append("""
            </div>
        </section>

        <section class="container" id="register">
            <h2>Student Registration</h2>
            <div class="form-box">
                <form action="/register" method="POST">
                    <input name="name" placeholder="Full Name" required>
                    <input name="email" type="email" placeholder="Email Address" required>

                    <select name="department" required>
                        <option value="">Select Department</option>
                        <option>CSE</option>
                        <option>IT</option>
                        <option>ENTC</option>
                        <option>Mechanical</option>
                    </select>

                    <select name="event" required>
                        <option value="">Select Event</option>
        """);

        synchronized (events) {
            for (Event e : events) {
                html.append("<option value='")
                    .append(escape(e.name)).append("'>")
                    .append(escape(e.name)).append("</option>");
            }
        }

        html.append("""
                    </select>
                    <button type="submit">Submit Registration</button>
                </form>
            </div>
        </section>

        <footer>
            <p>College Event Management System</p>
            <p>Contact: events@college.com</p>
        </footer>
        </body>
        </html>
        """);

        return html.toString();
    }

    static String adminPage() {
        StringBuilder html = new StringBuilder(pageStart("Admin Dashboard"));

        html.append("""
        <section class="container">
            <h1>Admin Dashboard</h1>
            <div class="cards">
                <div class="card">
                    <h2>
        """);

        synchronized (events) {
            html.append(events.size());
        }

        html.append("""
                    </h2>
                    <p>Total Events</p>
                </div>
                <div class="card">
                    <h2>
        """);

        synchronized (students) {
            html.append(students.size());
        }

        html.append("""
                    </h2>
                    <p>Total Registrations</p>
                </div>
            </div>

            <h2>Add New Event</h2>
            <div class="form-box">
                <form action="/add-event" method="POST">
                    <input name="name" placeholder="Event Name" required>
                    <input name="description" placeholder="Event Description" required>
                    <input name="date" placeholder="Event Date" required>
                    <button type="submit">Add Event</button>
                </form>
            </div>

            <h2>Manage Events</h2>
            <table>
                <tr><th>Event</th><th>Date</th><th>Action</th></tr>
        """);

        synchronized (events) {
            for (Event e : events) {
                html.append("<tr><td>").append(escape(e.name))
                    .append("</td><td>").append(escape(e.date))
                    .append("</td><td><form action='/delete-event' method='POST'>")
                    .append("<input type='hidden' name='id' value='")
                    .append(e.id).append("'>")
                    .append("<button type='submit'>Delete</button>")
                    .append("</form></td></tr>");
            }
        }

        html.append("""
            </table>

            <h2>Registered Students</h2>
            <table>
                <tr>
                    <th>Name</th>
                    <th>Email</th>
                    <th>Department</th>
                    <th>Event</th>
                </tr>
        """);

        synchronized (students) {
            for (Student s : students) {
                html.append("<tr><td>").append(escape(s.name))
                    .append("</td><td>").append(escape(s.email))
                    .append("</td><td>").append(escape(s.department))
                    .append("</td><td>").append(escape(s.event))
                    .append("</td></tr>");
            }
        }

        html.append("""
            </table>
        </section>
        </body>
        </html>
        """);

        return html.toString();
    }

    static void send(HttpExchange ex, String content)
            throws IOException {

        byte[] data = content.getBytes(StandardCharsets.UTF_8);

        ex.getResponseHeaders().set(
            "Content-Type", "text/html; charset=UTF-8"
        );

        ex.sendResponseHeaders(200, data.length);

        try (OutputStream os = ex.getResponseBody()) {
            os.write(data);
        }
    }

    static void redirect(HttpExchange ex) throws IOException {
        ex.getResponseHeaders().set("Location", "/admin");
        ex.sendResponseHeaders(303, -1);
        ex.close();
    }

    public static void main(String[] args) throws Exception {

        events.add(new Event(1, "Tech Fest 2026",
            "Explore technology and innovation.", "10 December 2026"));

        events.add(new Event(2, "Music Festival",
            "Enjoy music, dance and entertainment.", "15 December 2026"));

        events.add(new Event(3, "Sports Day",
            "Show your talent and sportsmanship.", "20 December 2026"));

        HttpServer server = HttpServer.create(
            new InetSocketAddress(8080), 0
        );

        server.createContext("/", ex -> {
            String path = ex.getRequestURI().getPath();
            String method = ex.getRequestMethod();

            if (path.equals("/") && method.equals("GET")) {
                send(ex, homePage());
            }

            else if (path.equals("/admin") && method.equals("GET")) {
                send(ex, adminPage());
            }

            else if (path.equals("/register") && method.equals("POST")) {
                Map<String, String> data = readForm(ex);

                synchronized (students) {
                    students.add(new Student(
                        data.getOrDefault("name", ""),
                        data.getOrDefault("email", ""),
                        data.getOrDefault("department", ""),
                        data.getOrDefault("event", "")
                    ));
                }

                redirect(ex);
            }

            else if (path.equals("/add-event") && method.equals("POST")) {
                Map<String, String> data = readForm(ex);

                synchronized (events) {
                    events.add(new Event(
                        nextId++,
                        data.getOrDefault("name", ""),
                        data.getOrDefault("description", ""),
                        data.getOrDefault("date", "")
                    ));
                }

                redirect(ex);
            }

            else if (path.equals("/delete-event") && method.equals("POST")) {
                Map<String, String> data = readForm(ex);

                try {
                    int id = Integer.parseInt(data.get("id"));

                    synchronized (events) {
                        events.removeIf(e -> e.id == id);
                    }
                } catch (Exception ignored) {
                }

                redirect(ex);
            }

            else {
                send(ex, pageStart("Not Found") +
                    "<h2>Page Not Found</h2></body></html>");
            }
        });

        server.start();

        System.out.println("Eventify running at http://localhost:8080");
        System.out.println("Admin Dashboard: http://localhost:8080/admin");
    }
}