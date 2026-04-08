import org.junit.platform.suite.api.{ConfigurationParameter, IncludeEngines, SelectClasspathResource, Suite}
import io.cucumber.junit.platform.engine.Constants

@Suite
@IncludeEngines(Array("cucumber"))
@SelectClasspathResource("features")
@ConfigurationParameter(key = Constants.GLUE_PROPERTY_NAME, value = "services")
class RunCucumberTest {}
