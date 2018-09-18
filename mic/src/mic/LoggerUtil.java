package mic;

import org.slf4j.Logger;

public class LoggerUtil {



    public static void logEntry(Logger logger, String method)
    {
        logger.trace(method+" [enter ====>]");
    }

    public static void logExit(Logger logger, String method)
    {

        logger.trace(method+" [exit <====]");
    }
}
