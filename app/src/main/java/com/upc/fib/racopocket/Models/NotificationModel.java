package com.upc.fib.racopocket.Models;

import android.support.v4.util.Pair;

import java.util.List;

public class NotificationModel {

    String idNotification;
    String title;
    String pubDate;
    List<Pair<String, String>> attachmentsList; // idAttachment, attachmentTitle
    String description;

    /**
     * NotificationModel constructor.
     * @param idNotification Notification identifier.
     * @param title Notification title.
     * @param pubDate Notification publication date.
     * @param attachmentsList Notification attachments list.
     * @param description Notification description.
     */
    public NotificationModel(String idNotification, String title, String pubDate,
                             List<Pair<String, String>> attachmentsList, String description) {
        this.idNotification = idNotification;
        this.title = title;
        this.pubDate = pubDate;
        this.attachmentsList = attachmentsList;
        this.description = description;
    }

    /**
     * Gets the notification identifier.
     * @return String object representing the notification id.
     */
    public String getIdNotification() {
        return this.idNotification;
    }

    /**
     * Gets the notification title.
     * @return String object representing the notification title.
     */
    public String getTitle() {
        return this.title;
    }

    /**
     * Gets the notification publication date.
     * @return String object representing the notification pubDate.
     */
    public String getPubDate() {
        return this.pubDate;
    }

    /**
     * Gets the notification attachment list.
     * @return String object representing the notification attachment list.
     */
    public List<Pair<String, String>> getAttachmentsList() {
        return this.attachmentsList;
    }

    /**
     * Gets the notification description.
     * @return String object representing the notification description.
     */
    public String getDescription() {
        return this.description;
    }

}
