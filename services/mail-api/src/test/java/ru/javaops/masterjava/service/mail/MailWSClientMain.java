package ru.javaops.masterjava.service.mail;

import com.google.common.collect.ImmutableSet;

public class MailWSClientMain {
    public static void main(String[] args) {
        MailWSClient.sendToGroup(
                ImmutableSet.of(new Addressee("To <leha.isakov@yandex.ru>")),
                ImmutableSet.of(new Addressee("Copy <leha.isakov@yandex.ru>")), "Subject", "Body");
    }
}