package in.skdv.skdvinbackend;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;

import static in.skdv.skdvinbackend.config.Authorities.*;

public class MockJwtDecoder {

    private static final String SIMPLIFIED_CREATE_JUMPDAYS = simplifyPermission(CREATE_JUMPDAYS);
    private static final String SIMPLIFIED_READ_JUMPDAYS = simplifyPermission(READ_JUMPDAYS);
    private static final String SIMPLIFIED_READ_APPOINTMENTS = simplifyPermission(READ_APPOINTMENTS);
    private static final String SIMPLIFIED_UPDATE_APPOINTMENTS = simplifyPermission(UPDATE_APPOINTMENTS);

    public static Jwt decode(String permission) throws JwtException {
        return new Jwt(permission,
                Instant.now(),
                Instant.now().plus(1, ChronoUnit.MINUTES),
                Collections.singletonMap("foo", "bar"),
                Collections.singletonMap("permissions", convertPermission(permission)));
    }

    private static String convertPermission(String permission) {
        String convertedPermission = "";
        if (SIMPLIFIED_CREATE_JUMPDAYS.equals(permission)) {
            convertedPermission = CREATE_JUMPDAYS;
        }
        if (SIMPLIFIED_READ_JUMPDAYS.equals(permission)) {
            convertedPermission = READ_JUMPDAYS;
        }
        if (SIMPLIFIED_READ_APPOINTMENTS.equals(permission)) {
            convertedPermission = READ_APPOINTMENTS;
        }
        if (SIMPLIFIED_UPDATE_APPOINTMENTS.equals(permission)) {
            convertedPermission = UPDATE_APPOINTMENTS;
        }

        return convertedPermission.replace("SCOPE_", "");
    }

    public static String addHeader(String permission) {
        return "Bearer " + simplifyPermission(permission);
    }

    private static String simplifyPermission(String permission) {
        return permission.replace("SCOPE_", "").replace(":", "");
    }

}