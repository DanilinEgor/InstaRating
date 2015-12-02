package ru.egor_d.instarating.model;

public class InstagramResponse<T> {
    public InstagramMeta meta;
    public InstagramPagination pagination;
    public T data;
}
