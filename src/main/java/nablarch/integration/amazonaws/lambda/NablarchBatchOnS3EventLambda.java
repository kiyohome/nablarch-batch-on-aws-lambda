package nablarch.integration.amazonaws.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import nablarch.core.log.Logger;
import nablarch.core.log.LoggerManager;
import nablarch.fw.launcher.CommandLine;
import nablarch.fw.launcher.Main;

import java.util.HashMap;
import java.util.Map;

public class NablarchBatchOnS3EventLambda implements RequestHandler<S3Event, String> {

    private static final Logger LOGGER = LoggerManager.get(NablarchBatchOnS3EventLambda.class);

    private final Map<String, String> env;

    public NablarchBatchOnS3EventLambda() {
        this(new HashMap<>(System.getenv()));
    }

    /**
     * This constructor is used for testing purposes only.
     */
    NablarchBatchOnS3EventLambda(Map<String, String> env) {
        this.env = env;
    }

    @Override
    public String handleRequest(S3Event s3Event, Context context) {
        final int exitCode = Main.execute(createCommandLine(s3Event));
        return exitCode == 0 ? "SUCCESS" : "FAILURE";
    }

    protected CommandLine createCommandLine(S3Event s3Event) {

        final String diConfig = env.computeIfAbsent("NABLARCH_DI_CONFIG", key -> "classpath:batch-boot.xml");
        final String requestPath = env.computeIfAbsent("NABLARCH_REQUEST_PATH", key -> {
            throw new IllegalStateException("The NABLARCH_REQUEST_PATH environment variable is required.");
        });
        final String userId = env.computeIfAbsent("NABLARCH_USER_ID", key -> "batch_user");

        return new S3EventCommandLine(s3Event, "-diConfig", diConfig, "-requestPath", requestPath, "-userId", userId);
    }

    public static class S3EventCommandLine extends CommandLine {

        private final S3Event s3Event;

        public S3EventCommandLine(S3Event s3Event, String... commandline) {
            super(commandline);
            this.s3Event = s3Event;
        }

        public S3Event getS3Event() {
            return s3Event;
        }
    }
}