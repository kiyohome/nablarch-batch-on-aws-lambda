package nablarch.integration.amazonaws.lambda;

import com.example.SampleBatch;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.HashMap;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class NablarchBatchOnS3EventLambdaTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void noRequestPath() {

        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("The NABLARCH_REQUEST_PATH environment variable is required.");

        NablarchBatchOnS3EventLambda sut = new NablarchBatchOnS3EventLambda();
        sut.handleRequest(null, null);
    }

    @Test
    public void success() {

        NablarchBatchOnS3EventLambda sut = new NablarchBatchOnS3EventLambda(new HashMap<String, String>() {{
            put("NABLARCH_DI_CONFIG", "classpath:batch-component-configuration.xml");
            put("NABLARCH_REQUEST_PATH", SampleBatch.class.getSimpleName());
        }});

        assertThat(sut.handleRequest(null, null), is("SUCCESS"));
    }
}