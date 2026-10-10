package ru.itmo.wp.servlet;

import com.google.gson.Gson;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MessageServlet extends HttpServlet {
    private final Gson gson = new Gson();
    private final List<Message> messages = new ArrayList<>();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");

        String path = request.getPathInfo();

        if ("/auth".equals(path)) {
            auth(request, response);
        } else if ("/findAll".equals(path)) {
            findAll(response);
        } else if ("/add".equals(path)) {
            add(request, response);
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            writeJson(response, "Not found");
        }
    }

    private void auth(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        String user = request.getParameter("user");

        if (user != null) {
            session.setAttribute("user", user);
        }

        String currentUser = (String) session.getAttribute("user");
        writeJson(response, currentUser == null ? "" : currentUser);
    }

    private void findAll(HttpServletResponse response) throws IOException {
        List<Message> result;

        synchronized (messages) {
            result = new ArrayList<>(messages);
        }

        writeJson(response, result);
    }

    private void add(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String user = (String) request.getSession().getAttribute("user");
        String text = request.getParameter("text");

        if (user == null || user.isEmpty() || text == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeJson(response, false);
            return;
        }

        synchronized (messages) {
            messages.add(new Message(user, text));
        }

        writeJson(response, true);
    }

    private void writeJson(HttpServletResponse response, Object object) throws IOException {
        response.getWriter().print(gson.toJson(object));
        response.getWriter().flush();
    }

    private static class Message {
        private final String user;
        private final String text;

        public Message(String user, String text) {
            this.user = user;
            this.text = text;
        }
    }
}