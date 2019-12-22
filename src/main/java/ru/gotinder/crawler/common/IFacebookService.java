package ru.gotinder.crawler.common;

public interface IFacebookService {
    public default String getToken() {
        return "";
    }
}
