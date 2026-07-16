package controller;

import dao.ReportDAO;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

@WebServlet(urlPatterns = {"/reports", "/api/reports"})
public class ReportServlet extends BaseServlet {
    private final ReportDAO reportDAO = new ReportDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if ("/reports".equals(request.getServletPath())) {
            try {
                request.getRequestDispatcher("/manage.jsp?initialView=reports").forward(request, response);
            } catch (Exception exception) {
                response.sendError(500);
            }
            return;
        }

        try {
            LocalDate[] range = resolveRange(request);
            writeJson(response, jsonMap(
                    "from", range[0].toString(),
                    "to", range[1].toString(),
                    "data", reportDAO.getReport(range[0], range[1])
            ));
        } catch (IllegalArgumentException exception) {
            writeJson(response, 400, jsonMap("message", "Khoảng thời gian không hợp lệ"));
        } catch (Exception exception) {
            getServletContext().log("Không thể tải báo cáo thống kê", exception);
            writeJson(response, 500, jsonMap("message", "Không thể tải báo cáo thống kê"));
        }
    }

    private LocalDate[] resolveRange(HttpServletRequest request) {
        LocalDate today = LocalDate.now();
        String range = request.getParameter("range");
        if (range == null) range = "month";
        LocalDate from;
        LocalDate to = today;
        switch (range) {
            case "today" -> from = today;
            case "7days" -> from = today.minusDays(6);
            case "quarter" -> from = LocalDate.of(today.getYear(), ((today.getMonthValue() - 1) / 3) * 3 + 1, 1);
            case "year" -> from = today.with(TemporalAdjusters.firstDayOfYear());
            case "custom" -> {
                from = LocalDate.parse(request.getParameter("from"));
                to = LocalDate.parse(request.getParameter("to"));
            }
            default -> from = today.with(TemporalAdjusters.firstDayOfMonth());
        }
        if (from.isAfter(to)) throw new IllegalArgumentException("from > to");
        return new LocalDate[]{from, to};
    }
}
