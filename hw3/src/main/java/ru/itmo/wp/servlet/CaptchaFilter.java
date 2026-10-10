package ru.itmo.wp.servlet;

import ru.itmo.wp.util.ImageUtils;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Base64;

public class CaptchaFilter extends HttpFilter {
    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpSession session = request.getSession();

        if (Boolean.TRUE.equals(session.getAttribute("captchaPassed"))) {
            chain.doFilter(request, response);
            return;
        }

        if ("POST".equals(request.getMethod()) && request.getParameter("captcha") != null) {
            String answer = (String) session.getAttribute("captchaAnswer");

            if (answer != null && answer.equals(request.getParameter("captcha"))) {
                session.setAttribute("captchaPassed", true);
                session.removeAttribute("captchaAnswer");

                String url = request.getRequestURI();
                if (request.getQueryString() != null) {
                    url += "?" + request.getQueryString();
                }

                response.sendRedirect(response.encodeRedirectURL(url));
                return;
            }

            session.setAttribute("captchaAnswer", newCaptcha());
            showCaptcha(session, response);
            return;
        }

        if ("GET".equals(request.getMethod())) {
            if (session.getAttribute("captchaAnswer") == null) {
                session.setAttribute("captchaAnswer", newCaptcha());
            }

            showCaptcha(session, response);
            return;
        }

        chain.doFilter(request, response);
    }

    private String newCaptcha() {
        return String.valueOf(100 + RANDOM.nextInt(900));
    }

    private void showCaptcha(HttpSession session, HttpServletResponse response) throws IOException {
        String answer = (String) session.getAttribute("captchaAnswer");
        String image = Base64.getEncoder().encodeToString(ImageUtils.toPng(answer));

        response.setContentType("text/html;charset=UTF-8");
        response.setHeader("Cache-Control", "no-store");

        response.getWriter().print(
                "<!DOCTYPE html><html><body>" +
                        "<p>Введите код с картинки:</p>" +
                        "<img alt='Капча' src='data:image/png;base64," + image + "'>" +
                        "<form method='post'>" +
                        "<input name='captcha' required autocomplete='off'>" +
                        "<button type='submit'>Отправить</button>" +
                        "</form></body></html>"
        );

        response.getWriter().flush();
    }
}