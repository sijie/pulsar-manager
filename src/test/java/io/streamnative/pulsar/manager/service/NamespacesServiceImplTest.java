/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.streamnative.pulsar.manager.service;

import com.google.common.collect.Maps;
import io.streamnative.pulsar.manager.PulsarManagerApplication;
import io.streamnative.pulsar.manager.profiles.SqliteDBTestProfile;
import io.streamnative.pulsar.manager.utils.HttpUtil;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Map;

@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(SpringRunner.class)
@PowerMockIgnore( {"javax.management.*", "javax.net.ssl.*"})
@PrepareForTest(HttpUtil.class)
@SpringBootTest(
    classes = {
        PulsarManagerApplication.class,
        SqliteDBTestProfile.class
    }
)
@ActiveProfiles("test")
public class NamespacesServiceImplTest {

    @Autowired
    private NamespacesService namespacesService;

    @Autowired
    private BrokerStatsService brokerStatsService;

    @Test
    public void namespaceServiceImplTest() {
        PowerMockito.mockStatic(HttpUtil.class);
        Map<String, String> header = Maps.newHashMap();
        header.put("Content-Type", "application/json");
        PowerMockito.when(HttpUtil.doGet("http://localhost:8080/admin/v2/namespaces/public", header))
                .thenReturn("[\"public/default\"]");

        PowerMockito.when(HttpUtil.doGet("http://localhost:8080/admin/v2/persistent/public/default", header))
                .thenReturn("[\"persistent://public/default/test789\"]");
        PowerMockito.when(HttpUtil.doGet(
                "http://localhost:8080/admin/v2/persistent/public/default/partitioned", header))
                .thenReturn("[]");
        Map<String, Object> result = namespacesService.getNamespaceList(1, 1, "public", "http://localhost:8080");
        Assert.assertEquals(result.get("total"), 1);
        Assert.assertFalse((Boolean) result.get("isPage"));
        Assert.assertEquals(result.get("data").toString(), "[{topics=1, namespace=default}]");
    }

    @Test
    public void getNamespaceStatsTest() {
        PowerMockito.mockStatic(HttpUtil.class);
        Map<String, String> header = Maps.newHashMap();
        header.put("Content-Type", "application/json");
        PowerMockito.when(HttpUtil.doGet("http://localhost:8080/admin/v2/clusters", header))
                .thenReturn("[\"standalone\"]");

        PowerMockito.when(HttpUtil.doGet("http://localhost:8080/admin/v2/brokers/standalone", header))
                .thenReturn("[\"localhost:8080\"]");
        PowerMockito.when(HttpUtil.doGet("http://localhost:8080/admin/v2/broker-stats/topics", header))
                .thenReturn(BrokerStatsServiceImplTest.testData);
        PowerMockito.when(HttpUtil.doGet("http://localhost:8080/admin/v2/clusters/standalone/failureDomains", header))
                .thenReturn("{}");
        PowerMockito.when(HttpUtil.doGet("http://localhost:8080/admin/v2/clusters/standalone", header))
                .thenReturn("{\n" +
                        "\"serviceUrl\" : \"http://tengdeMBP:8080\",\n" +
                        "\"brokerServiceUrl\" : \"pulsar://tengdeMBP:6650\"\n" +
                        "}");
        String environment = "staging";
        String cluster = "standalone";
        String serviceUrl = "http://localhost:8080";
        brokerStatsService.collectStatsToDB(
                System.currentTimeMillis() / 1000,
                environment,
                cluster,
                serviceUrl
        );
        Map<String, Object> namespaceStats = namespacesService.getNamespaceStats(
                environment, "public", "functions");
        Assert.assertEquals(namespaceStats.get("outMsg"), 0.0);
        Assert.assertEquals(namespaceStats.get("inMsg"), 0.0);
        Assert.assertEquals(namespaceStats.get("msgThroughputIn"), 0.0);
        Assert.assertEquals(namespaceStats.get("msgThroughputOut"), 0.0);
    }
}
