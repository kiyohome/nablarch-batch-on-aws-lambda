package nablarch.integration.amazonaws.lambda;

import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.event.S3EventNotification;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import nablarch.core.util.FilePathSetting;
import nablarch.fw.ExecutionContext;
import nablarch.fw.Handler;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class S3FileHandler implements Handler<Object, Object> {

    private final Map<String, String> env;
    private final AmazonS3 s3Client;

    public S3FileHandler() {
        this(new HashMap<>(System.getenv()), AmazonS3ClientBuilder.defaultClient());
    }

    public S3FileHandler(Map<String, String> env, AmazonS3 s3Client) {
        this.env = env;
        this.s3Client = s3Client;
    }

    @Override
    public Object handle(Object data, ExecutionContext context) {

        final NablarchBatchOnS3EventLambda.S3EventCommandLine commandLine
                = (NablarchBatchOnS3EventLambda.S3EventCommandLine) data;

        getS3(commandLine, context);

        final Object result = context.handleNext(data);

        putS3(commandLine, context);

        return result;
    }

    protected void getS3(NablarchBatchOnS3EventLambda.S3EventCommandLine commandLine, ExecutionContext context) {

        final S3Event s3Event = commandLine.getS3Event();
        final S3EventNotification.S3Entity s3Entity = s3Event.getRecords().get(0).getS3();
        final String bucketName = s3Entity.getBucket().getName();
        final String objectKey = s3Entity.getObject().getUrlDecodedKey();
        final GetObjectRequest getObjectRequest = new GetObjectRequest(bucketName, objectKey);

        final String basePathName = env.computeIfAbsent("NABLARCH_INPUT_BASE_PATH_NAME",  key -> "input");
        final String fileName = env.computeIfAbsent("NABLARCH_INPUT_FILE_NAME",  key -> "input-data");
        final File file = FilePathSetting.getInstance().getFile(basePathName, fileName);

        s3Client.getObject(getObjectRequest, file);

        context.setRequestScopedVar(OBJECT_KEY_VARIABLE_NAME, objectKey);
    }

    private static final String OBJECT_KEY_VARIABLE_NAME = "s3InputObjectKey";

    protected void putS3(NablarchBatchOnS3EventLambda.S3EventCommandLine commandLine, ExecutionContext context) {

        final String bucketName = env.computeIfAbsent("NABLARCH_S3_PUT_BUCKET_NAME", key -> {
            throw new IllegalStateException("The NABLARCH_S3_PUT_BUCKET_NAME environment variable is required.");
        });
        final String objectKey = env.computeIfAbsent("NABLARCH_S3_PUT_OBJECT_KEY",
                key -> "out-" + context.getRequestScopedVar(OBJECT_KEY_VARIABLE_NAME));

        final String basePathName = env.computeIfAbsent("NABLARCH_OUTPUT_BASE_PATH_NAME",  key -> "output");
        final String fileName = env.computeIfAbsent("NABLARCH_OUTPUT_FILE_NAME",  key -> "output-data");
        final File file = FilePathSetting.getInstance().getFile(basePathName, fileName);

        final PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, objectKey, file);

        s3Client.putObject(putObjectRequest);
    }
}
