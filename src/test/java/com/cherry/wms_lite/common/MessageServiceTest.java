package com.cherry.wms_lite.common;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MessageServiceTest {
    private static final String EN_CONTAINER_TYPE_NOT_FOUND = "Container type not found with id: 42";
    private static final String HU_CONTAINER_TYPE_NOT_FOUND = "Tároló típus nem található az azonosítóval: 42";
    private static final Locale HU_LOCALE = Locale.forLanguageTag("hu");

    private MessageService messageService;

    @BeforeEach
    void setUp() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:messages");
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setDefaultLocale(Locale.ENGLISH);
        messageService = new MessageService(messageSource);
    }

    @Test
    void getMessage_containerTypeNotFoundWithId_english() {
        LocaleContextHolder.setLocale(Locale.ENGLISH);

        String result = messageService.getMessage(ExceptionMessageKeys.CONTAINER_TYPE_NOT_FOUND_WITH_ID, 42L);

        assertEquals(EN_CONTAINER_TYPE_NOT_FOUND, result);
    }

    @Test
    void getMessage_containerTypeNotFoundWithId_hungarian() {
        LocaleContextHolder.setLocale(HU_LOCALE);

        String result = messageService.getMessage(ExceptionMessageKeys.CONTAINER_TYPE_NOT_FOUND_WITH_ID, 42L);

        assertEquals(HU_CONTAINER_TYPE_NOT_FOUND, result);
    }
}
