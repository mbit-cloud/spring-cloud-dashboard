/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.vanroy.cloud.dashboard;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import net.vanroy.cloud.dashboard.config.EnableCloudDashboard;
import net.vanroy.cloud.dashboard.model.Application;
import net.vanroy.cloud.dashboard.model.Instance;
import net.vanroy.cloud.dashboard.repository.ApplicationRepository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * 
 * Integration test to verify the correct functionality of the REST API.
 * 
 * @author Dennis Schulte
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = DashboardApplicationTest.DashboardApplication.class)
@WebAppConfiguration
@IntegrationTest({ "server.port=0" })
public class DashboardApplicationTest {

	@Value("${local.server.port}")
	private int port = 0;

	@Test
	public void testGetApplications() {
		@SuppressWarnings("rawtypes")
		ResponseEntity<List> entity = new TestRestTemplate().getForEntity("http://localhost:" + port + "/api/applications", List.class);
        assertNotNull(entity.getBody());
        assertEquals(2, entity.getBody().size());
        Map<String, Object> application = (Map<String, Object>) entity.getBody().get(0);
        assertEquals("MESSAGE", application.get("name"));
        assertEquals(3, ((List)application.get("instances")).size());
		assertEquals(HttpStatus.OK, entity.getStatusCode());
	}

	@Configuration
	@EnableAutoConfiguration
    @EnableCloudDashboard
	public static class DashboardApplication {

		public static void main(String[] args) {
			SpringApplication.run(DashboardApplicationTest.DashboardApplication.class, args);
		}

        @Bean
        public ApplicationRepository eurekaRepository() {
            return new ApplicationRepository() {

                @Override
                public Collection<Application> findAll() {
                    return ImmutableList.of(
                        new Application("MESSAGE",
                            ImmutableSet.of(
                                new Instance("http://localhost:8001", "INSTANCE 1", "ID1", "UP"),
                                new Instance("http://localhost:8002", "INSTANCE 2", "ID2", "DOWN"),
                                new Instance("http://localhost:8003", "INSTANCE 3", "ID3", "STARTING")
                            )
                        ),
                        new Application("FRONT",
                            ImmutableSet.of(
                                new Instance("http://localhost:8001", "INSTANCE 1", "ID1","OUT_OF_SERVICE"),
                                new Instance("http://localhost:8002", "INSTANCE 2", "ID2","DOWN"),
                                new Instance("http://localhost:8003", "INSTANCE 3", "ID3","UNKNOWN")
                            )
                        )
                    );
                }

                @Override
                public Collection<Application> findByName(String name) {
                    return null;
                }

                @Override
                public Instance findInstance(String id) {
                    return new Instance("http://localhost:8001", "INSTANCE 1", "ID1", "UP");
                }

                @Override
                public String getInstanceManagementUrl(String id) {
                    return "http://localhost:8001/manage";
                }
            };
        }
	}

}