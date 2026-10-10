package ru.itmo.wp.servlet;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;

public class StaticServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String uri = request.getRequestURI();

        File tomcatDir = new File(getServletContext().getRealPath("/static")).getCanonicalFile();
        File sourceDir = new File(tomcatDir, "../../../src/main/webapp/static").getCanonicalFile();

        String[] paths = uri.split("\\+", -1);
        File[] files = new File[paths.length];

        for (int i = 0; i < paths.length; i++) {
            String path = paths[i];

            if (path.startsWith("/")) {
                path = path.substring(1);
            }

            File sourceFile = new File(sourceDir, path).getCanonicalFile();
            File tomcatFile = new File(tomcatDir, path).getCanonicalFile();

            if (!sourceFile.toPath().startsWith(sourceDir.toPath())
                    || !tomcatFile.toPath().startsWith(tomcatDir.toPath())) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            File file = sourceFile.isFile() ? sourceFile : tomcatFile;

            if (!file.isFile()) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            files[i] = file;
        }

        response.setContentType(getServletContext().getMimeType(files[0].getName()));

        try (OutputStream outputStream = response.getOutputStream()) {
            for (File file : files) {
                Files.copy(file.toPath(), outputStream);
            }
        }
    }
}
