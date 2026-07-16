package util;

import org.mindrot.jbcrypt.BCrypt;

import java.util.regex.Pattern;

public final class PasswordUtil {
    private static final int LOG_ROUNDS = 12;
    private static final Pattern BCRYPT_PATTERN = Pattern.compile(
            "^\\$2[aby]\\$\\d{2}\\$[./A-Za-z0-9]{53}$"
    );

    private PasswordUtil() {
    }

    public static String hash(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            throw new IllegalArgumentException("Mật khẩu không được để trống");
        }
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(LOG_ROUNDS));
    }

    public static boolean verify(String plainPassword, String storedHash) {
        if (plainPassword == null || !isBCryptHash(storedHash)) {
            return false;
        }
        try {
            String compatibleHash = storedHash.startsWith("$2b$") || storedHash.startsWith("$2y$")
                    ? "$2a$" + storedHash.substring(4)
                    : storedHash;
            return BCrypt.checkpw(plainPassword, compatibleHash);
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    public static boolean isBCryptHash(String value) {
        return value != null && BCRYPT_PATTERN.matcher(value).matches();
    }
}
