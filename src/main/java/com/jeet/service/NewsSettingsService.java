package com.jeet.service;

import com.jeet.api.Logins;
import com.jeet.broadcasting.eventModel.NewsSettings;
import com.jeet.db.DAO;
import com.jeet.rest.responserecord.news.ErrorResponse;
import com.jeet.rest.responserecord.news.ScreenNewsResponse;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

@ApplicationScoped
public class NewsSettingsService {
    private static final Logger LOG = Logger.getLogger(NewsSettingsService.class);

    @Inject
    AccountService accounts;

    @Inject
    DAO dao;

    public record Result(int status, Object body) {
    }

    public Result get(String user) {
        try {
            Logins login = accounts.getUser(user);
            if (login.getId() == 0) {
                return error(404, "user_not_found");
            }
            NewsSettings settings = dao.findNewsSettings(login.getId());
            if (settings == null || "no".equals(settings.getNewsScreening())) {
                return success(false);
            }
            if ("yes".equals(settings.getNewsScreening())) {
                return success(true);
            }
            return error(500, "invalid_news_setting");
        } catch (RuntimeException e) {
            LOG.error("Unable to read screen-news settings", e);
            return error(500, "db_error");
        }
    }

    public Result put(String user, boolean enabled) {
        try {
            Logins login = accounts.getUser(user);
            if (login.getId() == 0) {
                return error(404, "user_not_found");
            }
            dao.upsertNewsScreening(login.getId(), enabled ? "yes" : "no");
            return success(enabled);
        } catch (RuntimeException e) {
            LOG.error("Unable to commit screen-news settings", e);
            return error(500, "db_error");
        }
    }

    public boolean isScreenNewsEnabled(int loginsId) {
        NewsSettings settings = dao.findNewsSettings(loginsId);
        return settings != null && "yes".equals(settings.getNewsScreening());
    }

    private Result success(boolean enabled) {
        return new Result(200, new ScreenNewsResponse(enabled));
    }

    private Result error(int status, String code) {
        return new Result(status, new ErrorResponse(code));
    }
}
