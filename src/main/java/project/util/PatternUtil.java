package project.util;

public class PatternUtil {
    public static final String USERNAME_PATTERN =
            "^[a-zA-Z0-9]([._-](?![._-])|[a-zA-Z0-9]){3,18}[a-zA-Z0-9]$";
    
    public static final String EMAIL_PATTERN = "^[\\w!#$%&’*+/=?`{|}~^-]+(?:\\.[\\w!#$%&’*+/=?`"
            + "{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$";
    
    public static final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-z])"
            + "(?=.*[A-Z])(?=.*[!@#&()–[{}]:;',?/*~$^+=<>]).{8,20}$";
    
    public static final String NAME_PATTERN = "^[A-Za-z\\-]{3,25}$";
}
