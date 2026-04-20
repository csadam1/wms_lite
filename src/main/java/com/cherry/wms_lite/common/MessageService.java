package com.cherry.wms_lite.common;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MessageService {

    private final MessageSource messageSource;

    public String getMessage(final String key, final Object... args) {
        return messageSource.getMessage(key, args, LocaleContextHolder.getLocale());
    }
}

