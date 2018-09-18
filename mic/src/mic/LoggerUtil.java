package mic;

import org.slf4j.Logger;

public class LoggerUtil {

    private static final boolean doTraceLog = true;

    public static void logEntry(Logger logger, String method)
    {
        if(doTraceLog) {
            logger.trace(method + " [enter ====>]");
        }
    }

    public static void logExit(Logger logger, String method)
    {
        if(doTraceLog) {
            logger.trace(method + " [exit <====]");
        }
    }
}
