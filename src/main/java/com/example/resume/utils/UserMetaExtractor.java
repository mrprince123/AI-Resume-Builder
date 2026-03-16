package com.example.resume.utils;

import com.example.resume.entity.UserMeta;
import eu.bitwalker.useragentutils.UserAgent;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

@Component
public class UserMetaExtractor {

    public UserMeta extract(HttpServletRequest request){
        String ip = extractIp(request);
        String userAgentString = request.getHeader("User-Agent");
        UserAgent userAgent = UserAgent.parseUserAgentString(userAgentString);

        return UserMeta.builder()
                .lastLoginIp(ip)
                .device(userAgent.getOperatingSystem().getDeviceType().getName())
                .browser(userAgent.getBrowser().getName())
                .os(userAgent.getOperatingSystem().getName())
                .timezone(request.getHeader("X-Timezone"))
                .build();
    }

    private String extractIp(HttpServletRequest request){
        String forwarded = request.getHeader("X-Forwarded-For");
        if(forwarded != null && !forwarded.isEmpty()){
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
