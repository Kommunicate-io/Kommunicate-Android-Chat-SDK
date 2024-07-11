package io.kommunicate.models;

import java.util.List;

/**
 * Created by ashish on 20/04/18.
 */

public class KmArticleModel {

    private List<KmArticle> articles;
    private KmArticle article;
    private String code;
    private List<KmDashboardData> data;

    public List<KmArticle> getArticles() {
        return articles;
    }

    public void setArticles(List<KmArticle> articles) {
        this.articles = articles;
    }

    public KmArticle getArticle() {
        return article;
    }

    public void setArticle(KmArticle article) {
        this.article = article;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public List<KmDashboardData> getData() {
        return data;
    }

    public void setData(List<KmDashboardData> data) {
        this.data = data;
    }

    public class KmArticle {
        private String _id;
        private String article_id;
        private String title;
        private String slug;
        private String category_id;
        private String user_id;
        private String updated_at_relative;
        private String description;
        private String url;
        private KmAuthor author;

        public String get_id() {
            return _id;
        }

        public void set_id(String _id) {
            this._id = _id;
        }

        public String getArticle_id() {
            return article_id;
        }

        public void setArticle_id(String article_id) {
            this.article_id = article_id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getSlug() {
            return slug;
        }

        public void setSlug(String slug) {
            this.slug = slug;
        }

        public String getCategory_id() {
            return category_id;
        }

        public void setCategory_id(String category_id) {
            this.category_id = category_id;
        }

        public String getUser_id() {
            return user_id;
        }

        public void setUser_id(String user_id) {
            this.user_id = user_id;
        }

        public String getUpdated_at_relative() {
            return updated_at_relative;
        }

        public void setUpdated_at_relative(String updated_at_relative) {
            this.updated_at_relative = updated_at_relative;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public KmAuthor getAuthor() {
            return author;
        }

        public void setAuthor(KmAuthor author) {
            this.author = author;
        }

        @Override
        public String toString() {
            return "KmArticle{" +
                    "_id='" + _id + '\'' +
                    ", article_id='" + article_id + '\'' +
                    ", title='" + title + '\'' +
                    ", slug='" + slug + '\'' +
                    ", category_id='" + category_id + '\'' +
                    ", user_id='" + user_id + '\'' +
                    ", updated_at_relative='" + updated_at_relative + '\'' +
                    ", description='" + description + '\'' +
                    ", url='" + url + '\'' +
                    ", author=" + author +
                    '}';
        }
    }

    public class KmAuthor {
        private String name;
        private String profile_image;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getProfile_image() {
            return profile_image;
        }

        public void setProfile_image(String profile_image) {
            this.profile_image = profile_image;
        }

        @Override
        public String toString() {
            return "KmAuthor{" +
                    "name='" + name + '\'' +
                    ", profile_image='" + profile_image + '\'' +
                    '}';
        }
    }

    public class KmDashboardData {
        private String id;
        private String name;
        private String content;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }

    @Override
    public String toString() {
        return "KmArticleModel{" +
                "articles=" + articles +
                ", article=" + article +
                ", code='" + code + '\'' +
                ", data=" + data +
                '}';
    }
}
