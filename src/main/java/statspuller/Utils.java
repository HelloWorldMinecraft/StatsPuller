package statspuller;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.StringUtils;

import java.lang.reflect.Method;
import java.util.UUID;

public class Utils {
    public static UUID fromString(String string) {
        try {
            return UUID.fromString(string);
        } catch (IllegalArgumentException exception) {
            return UUID.fromString(string.replaceAll("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})",
                    "$1-$2-$3-$4-$5"));
        }
    }

    public static Object callMethod(Object object, String methodName) {
        try {
            return object.getClass().getMethod(methodName).invoke(object);
        } catch (Exception exception) {
            exception.printStackTrace();
            return exception;
        }
    }
}
