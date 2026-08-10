package com.booknook.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "booknook")
public class BooknookProperties {

    private Cors cors = new Cors();
    private Firebase firebase = new Firebase();
    private Hardcover hardcover = new Hardcover();
    private GoogleBooks googleBooks = new GoogleBooks();
    private Push push = new Push();
    private SeriesCache seriesCache = new SeriesCache();

    public Cors getCors() {
        return cors;
    }

    public void setCors(Cors cors) {
        this.cors = cors;
    }

    public Firebase getFirebase() {
        return firebase;
    }

    public void setFirebase(Firebase firebase) {
        this.firebase = firebase;
    }

    public Hardcover getHardcover() {
        return hardcover;
    }

    public void setHardcover(Hardcover hardcover) {
        this.hardcover = hardcover;
    }

    public GoogleBooks getGoogleBooks() {
        return googleBooks;
    }

    public void setGoogleBooks(GoogleBooks googleBooks) {
        this.googleBooks = googleBooks;
    }

    public Push getPush() {
        return push;
    }

    public void setPush(Push push) {
        this.push = push;
    }

    public SeriesCache getSeriesCache() {
        return seriesCache;
    }

    public void setSeriesCache(SeriesCache seriesCache) {
        this.seriesCache = seriesCache;
    }

    public static class Cors {
        private String allowedOrigin;

        public String getAllowedOrigin() {
            return allowedOrigin;
        }

        public void setAllowedOrigin(String allowedOrigin) {
            this.allowedOrigin = allowedOrigin;
        }
    }

    public static class Firebase {
        private String projectId;
        private String credentialsPath;

        public String getProjectId() {
            return projectId;
        }

        public void setProjectId(String projectId) {
            this.projectId = projectId;
        }

        public String getCredentialsPath() {
            return credentialsPath;
        }

        public void setCredentialsPath(String credentialsPath) {
            this.credentialsPath = credentialsPath;
        }
    }

    public static class Hardcover {
        private String apiKey;
        private String graphqlUrl;

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

        public String getGraphqlUrl() {
            return graphqlUrl;
        }

        public void setGraphqlUrl(String graphqlUrl) {
            this.graphqlUrl = graphqlUrl;
        }
    }

    public static class GoogleBooks {
        // Optional: unauthenticated requests share a small, easily-exhausted anonymous quota.
        // Without a key, ISBN lookup and title search still work but may 429 under light use.
        private String apiKey;

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }
    }

    public static class Push {
        private String vapidPublicKey;
        private String vapidPrivateKey;
        private String vapidSubject;

        public String getVapidPublicKey() {
            return vapidPublicKey;
        }

        public void setVapidPublicKey(String vapidPublicKey) {
            this.vapidPublicKey = vapidPublicKey;
        }

        public String getVapidPrivateKey() {
            return vapidPrivateKey;
        }

        public void setVapidPrivateKey(String vapidPrivateKey) {
            this.vapidPrivateKey = vapidPrivateKey;
        }

        public String getVapidSubject() {
            return vapidSubject;
        }

        public void setVapidSubject(String vapidSubject) {
            this.vapidSubject = vapidSubject;
        }
    }

    public static class SeriesCache {
        private int ttlHours = 24;

        public int getTtlHours() {
            return ttlHours;
        }

        public void setTtlHours(int ttlHours) {
            this.ttlHours = ttlHours;
        }
    }
}
