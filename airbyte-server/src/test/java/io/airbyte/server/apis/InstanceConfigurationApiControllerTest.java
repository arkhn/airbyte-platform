/*
 * Copyright (c) 2023 Airbyte, Inc., all rights reserved.
 */

package io.airbyte.server.apis;

import static org.mockito.Mockito.when;

import io.airbyte.api.model.generated.InstanceConfigurationResponse;
import io.airbyte.commons.server.handlers.instance_configuration.InstanceConfigurationHandler;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.env.Environment;
import io.micronaut.core.util.StringUtils;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpStatus;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

@MicronautTest
@Requires(property = "mockito.test.enabled",
          defaultValue = StringUtils.TRUE,
          value = StringUtils.TRUE)
@Requires(env = {Environment.TEST})
@SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
class InstanceConfigurationApiControllerTest extends BaseControllerTest {

  @Mock
  InstanceConfigurationHandler instanceConfigurationHandler = Mockito.mock(InstanceConfigurationHandler.class);

  @MockBean(InstanceConfigurationHandler.class)
  @Replaces(InstanceConfigurationHandler.class)
  InstanceConfigurationHandler mmInstanceConfigurationHandler() {
    return instanceConfigurationHandler;
  }

  static String PATH = "/api/v1/instance_configuration";

  @Test
  void testGetInstanceConfiguration() {
    when(instanceConfigurationHandler.getInstanceConfiguration())
        .thenReturn(new InstanceConfigurationResponse());

    testEndpointStatus(HttpRequest.GET(PATH), HttpStatus.OK);
  }

}
