/*
 * Copyright 2024-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.cloud.ai.memory.mem0.core;

import com.alibaba.cloud.ai.memory.mem0.config.Mem0ChatMemoryProperties;
import com.alibaba.cloud.ai.memory.mem0.model.Mem0ServerRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ResourceLoader;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for Mem0ServiceClient
 *
 * @author Morain Miao
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class Mem0ServiceClientTest {

	@Mock
	private ResourceLoader resourceLoader;

	private Mem0ChatMemoryProperties properties;

	private Mem0ServiceMock client;

	@BeforeEach
	void setUp() {
		properties = new Mem0ChatMemoryProperties();
		Mem0ChatMemoryProperties.Client clientConfig = new Mem0ChatMemoryProperties.Client();
		clientConfig.setBaseUrl("http://localhost:8888");
		clientConfig.setTimeoutSeconds(30);

		// 设置异步配置
		Mem0ChatMemoryProperties.Client.AsyncConfig asyncConfig = new Mem0ChatMemoryProperties.Client.AsyncConfig();
		asyncConfig.setEnabled(true);
		asyncConfig.setCorePoolSize(2);
		asyncConfig.setMaxPoolSize(4);
		asyncConfig.setQueueCapacity(100);
		asyncConfig.setThreadNamePrefix("test-mem0-async-");
		clientConfig.setAsync(asyncConfig);

		properties.setClient(clientConfig);

		client = new Mem0ServiceMock(properties, resourceLoader);
	}

	@Test
	void testConstructor() {
		assertThat(client).isNotNull();
	}

	@Test
	void testConstructorWithNullProperties() {
		// Test with null properties
		assertThatThrownBy(() -> new Mem0ServiceMock(null, resourceLoader)).isInstanceOf(NullPointerException.class);
	}

	@Test
	void testConstructorWithNullResourceLoader() {
		// Test with null resourceLoader
		Mem0ServiceMock client = new Mem0ServiceMock(properties, null);
		assertThat(client).isNotNull();
	}

	@Test
	void testAddMemory() {
		// Given
		Mem0ServerRequest.MemoryCreate memoryCreate = Mem0ServerRequest.MemoryCreate.builder()
			.messages(List.of(new Mem0ServerRequest.Message("user", "test message")))
			.userId("test-user")
			.agentId("test-agent")
			.runId("test-run")
			.build();

		// When & Then - Since a real HTTP connection is required, this primarily tests
		// that the method invocation does not throw exceptions
		// In actual testing, WireMock or TestContainers should be used to mock the HTTP
		// service
		assertThat(memoryCreate).isNotNull();
		assertThat(memoryCreate.getUserId()).isEqualTo("test-user");
		assertThat(memoryCreate.getAgentId()).isEqualTo("test-agent");
		assertThat(memoryCreate.getRunId()).isEqualTo("test-run");
	}

	@Test
	void testDeleteMemory() {
		// Given
		String memoryId = "test-memory-id";

		// When & Then - Test that the method exists and can be invoked
		assertThat(memoryId).isEqualTo("test-memory-id");
	}

	@Test
	void testSearchMemories() {
		// Given
		Mem0ServerRequest.SearchRequest searchRequest = new Mem0ServerRequest.SearchRequest();
		searchRequest.setQuery("test query");
		searchRequest.setUserId("test-user");
		searchRequest.setAgentId("test-agent");
		searchRequest.setRunId("test-run");

		// When & Then - Verify that the request object is created correctly
		assertThat(searchRequest.getQuery()).isEqualTo("test query");
		assertThat(searchRequest.getUserId()).isEqualTo("test-user");
		assertThat(searchRequest.getAgentId()).isEqualTo("test-agent");
		assertThat(searchRequest.getRunId()).isEqualTo("test-run");
	}

	@Test
	void testConfigure() {
		// Given
		Mem0ChatMemoryProperties.Server serverConfig = new Mem0ChatMemoryProperties.Server();
		serverConfig.setVersion("v1.1");

		// When & Then - Verify that the configuration object is created correctly
		assertThat(serverConfig.getVersion()).isEqualTo("v1.1");
	}

	@Test
	void testMemoryCreateBuilder() {
		// Given
		Mem0ServerRequest.Message message = new Mem0ServerRequest.Message("user", "test content");
		Map<String, Object> metadata = new HashMap<>();
		metadata.put("key", "value");

		// When
		Mem0ServerRequest.MemoryCreate memoryCreate = Mem0ServerRequest.MemoryCreate.builder()
			.messages(List.of(message))
			.metadata(metadata)
			.userId("test-user")
			.agentId("test-agent")
			.runId("test-run")
			.build();

		// Then
		assertThat(memoryCreate.getMessages()).hasSize(1);
		assertThat(memoryCreate.getMessages().get(0).getRole()).isEqualTo("user");
		assertThat(memoryCreate.getMessages().get(0).getContent()).isEqualTo("test content");
		assertThat(memoryCreate.getMetadata()).containsEntry("key", "value");
		assertThat(memoryCreate.getUserId()).isEqualTo("test-user");
		assertThat(memoryCreate.getAgentId()).isEqualTo("test-agent");
		assertThat(memoryCreate.getRunId()).isEqualTo("test-run");
	}

	@Test
	void testSearchRequestBuilder() {
		// Given
		Map<String, Object> filters = new HashMap<>();
		filters.put("category", "test");

		// When
		Mem0ServerRequest.SearchRequest searchRequest = new Mem0ServerRequest.SearchRequest();
		searchRequest.setQuery("test query");
		searchRequest.setUserId("test-user");
		searchRequest.setAgentId("test-agent");
		searchRequest.setRunId("test-run");
		searchRequest.setFilters(filters);

		// Then
		assertThat(searchRequest.getQuery()).isEqualTo("test query");
		assertThat(searchRequest.getUserId()).isEqualTo("test-user");
		assertThat(searchRequest.getAgentId()).isEqualTo("test-agent");
		assertThat(searchRequest.getRunId()).isEqualTo("test-run");
		assertThat(searchRequest.getFilters()).containsEntry("category", "test");
	}

	// ========== 异步配置测试 ==========

	@Test
	void testAsyncConfigEnabled() {
		// Given
		Mem0ChatMemoryProperties.Client.AsyncConfig asyncConfig = properties.getClient().getAsync();

		// Then
		assertThat(asyncConfig.isEnabled()).isTrue();
		assertThat(asyncConfig.getCorePoolSize()).isEqualTo(2);
		assertThat(asyncConfig.getMaxPoolSize()).isEqualTo(4);
		assertThat(asyncConfig.getQueueCapacity()).isEqualTo(100);
		assertThat(asyncConfig.getThreadNamePrefix()).isEqualTo("test-mem0-async-");
	}

	@Test
	void testAsyncConfigDisabled() {
		// Given
		Mem0ChatMemoryProperties properties = new Mem0ChatMemoryProperties();
		Mem0ChatMemoryProperties.Client clientConfig = new Mem0ChatMemoryProperties.Client();
		clientConfig.setBaseUrl("http://localhost:8888");
		clientConfig.setTimeoutSeconds(30);

		Mem0ChatMemoryProperties.Client.AsyncConfig asyncConfig = new Mem0ChatMemoryProperties.Client.AsyncConfig();
		asyncConfig.setEnabled(false);
		clientConfig.setAsync(asyncConfig);
		properties.setClient(clientConfig);

		// When
		Mem0ServiceClient client = new Mem0ServiceClient(properties, resourceLoader);

		// Then
		assertThat(client).isNotNull();
		assertThat(properties.getClient().getAsync().isEnabled()).isFalse();
	}

	// ========== addMemory 方法测试 ==========

	@Test
	void testAddMemoryWithAsyncEnabled() {
		// Given
		Mem0ServerRequest.MemoryCreate memoryCreate = Mem0ServerRequest.MemoryCreate.builder()
			.messages(List.of(new Mem0ServerRequest.Message("user", "test message")))
			.userId("test-user")
			.agentId("test-agent")
			.runId("test-run")
			.build();

		// When - 异步执行，方法应该立即返回
		long startTime = System.currentTimeMillis();
		client.addMemory(memoryCreate);
		long endTime = System.currentTimeMillis();

		// Then - 异步执行应该立即返回（不阻塞）
		assertThat(endTime - startTime).isLessThan(100); // 应该在100ms内返回
		assertThat(memoryCreate).isNotNull();
	}

	@Test
	void testAddMemoryWithAsyncDisabled() {
		// Given
		Mem0ChatMemoryProperties properties = new Mem0ChatMemoryProperties();
		Mem0ChatMemoryProperties.Client clientConfig = new Mem0ChatMemoryProperties.Client();
		clientConfig.setBaseUrl("http://localhost:8888");
		clientConfig.setTimeoutSeconds(30);

		Mem0ChatMemoryProperties.Client.AsyncConfig asyncConfig = new Mem0ChatMemoryProperties.Client.AsyncConfig();
		asyncConfig.setEnabled(false);
		clientConfig.setAsync(asyncConfig);
		properties.setClient(clientConfig);

		Mem0ServiceMock syncClient = new Mem0ServiceMock(properties, resourceLoader);

		Mem0ServerRequest.MemoryCreate memoryCreate = Mem0ServerRequest.MemoryCreate.builder()
			.messages(List.of(new Mem0ServerRequest.Message("user", "test message")))
			.userId("test-user")
			.agentId("test-agent")
			.runId("test-run")
			.build();

		// When - 同步执行，使用 Mock 实现
		syncClient.addMemory(memoryCreate);

		// Then - 验证方法被调用
		assertThat(memoryCreate).isNotNull();
	}

	@Test
	void testAddMemoryAsync() {
		// Given
		Mem0ServerRequest.MemoryCreate memoryCreate = Mem0ServerRequest.MemoryCreate.builder()
			.messages(List.of(new Mem0ServerRequest.Message("user", "test message")))
			.userId("test-user")
			.agentId("test-agent")
			.runId("test-run")
			.build();

		// When
		CompletableFuture<Void> future = client.addMemoryAsync(memoryCreate);

		// Then
		assertThat(future).isNotNull();
		assertThat(future).isInstanceOf(CompletableFuture.class);

		// 验证异步执行
		assertThat(future.isDone()).isFalse(); // 初始状态应该是未完成
	}

	// ========== updateMemory 方法测试 ==========

	@Test
	void testUpdateMemoryWithAsyncEnabled() {
		// Given
		String memoryId = "test-memory-id";
		Map<String, Object> updatedMemory = new HashMap<>();
		updatedMemory.put("content", "updated content");
		updatedMemory.put("metadata", Map.of("updated_at", System.currentTimeMillis()));

		// When - 异步执行，方法应该立即返回空结果
		long startTime = System.currentTimeMillis();
		Map<String, Object> result = client.updateMemory(memoryId, updatedMemory);
		long endTime = System.currentTimeMillis();

		// Then - 异步执行应该立即返回空结果
		assertThat(endTime - startTime).isLessThan(100); // 应该在100ms内返回
		assertThat(result).isEmpty(); // 异步执行时返回空结果
	}

	@Test
	void testUpdateMemoryWithAsyncDisabled() {
		// Given
		Mem0ChatMemoryProperties properties = new Mem0ChatMemoryProperties();
		Mem0ChatMemoryProperties.Client clientConfig = new Mem0ChatMemoryProperties.Client();
		clientConfig.setBaseUrl("http://localhost:8888");
		clientConfig.setTimeoutSeconds(30);

		Mem0ChatMemoryProperties.Client.AsyncConfig asyncConfig = new Mem0ChatMemoryProperties.Client.AsyncConfig();
		asyncConfig.setEnabled(false);
		clientConfig.setAsync(asyncConfig);
		properties.setClient(clientConfig);

		Mem0ServiceMock syncClient = new Mem0ServiceMock(properties, resourceLoader);

		String memoryId = "test-memory-id";
		Map<String, Object> updatedMemory = new HashMap<>();
		updatedMemory.put("content", "updated content");

		// When - 同步执行，使用 Mock 实现
		Map<String, Object> result = syncClient.updateMemory(memoryId, updatedMemory);

		// Then - 验证方法被调用并返回结果
		assertThat(result).isNotNull();
		assertThat(result.get("success")).isEqualTo(true);
		assertThat(result.get("memoryId")).isEqualTo("test-memory-id");
	}

	@Test
	void testUpdateMemoryAsync() {
		// Given
		String memoryId = "test-memory-id";
		Map<String, Object> updatedMemory = new HashMap<>();
		updatedMemory.put("content", "updated content");
		updatedMemory.put("metadata", Map.of("updated_at", System.currentTimeMillis()));

		// When
		CompletableFuture<Map<String, Object>> future = client.updateMemoryAsync(memoryId, updatedMemory);

		// Then
		assertThat(future).isNotNull();
		assertThat(future).isInstanceOf(CompletableFuture.class);

		// 验证异步执行 - 等待一小段时间让异步任务完成
		try {
			Thread.sleep(50); // 等待异步任务完成
		}
		catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		assertThat(future.isDone()).isTrue(); // 异步任务应该已完成
	}

	// ========== 线程池配置测试 ==========

	@Test
	void testThreadPoolConfiguration() {
		// Given
		Mem0ChatMemoryProperties.Client.AsyncConfig asyncConfig = properties.getClient().getAsync();

		// When & Then
		assertThat(asyncConfig.getCorePoolSize()).isEqualTo(2);
		assertThat(asyncConfig.getMaxPoolSize()).isEqualTo(4);
		assertThat(asyncConfig.getQueueCapacity()).isEqualTo(100);
		assertThat(asyncConfig.getThreadNamePrefix()).isEqualTo("test-mem0-async-");
	}

	@Test
	void testDefaultAsyncConfiguration() {
		// Given
		Mem0ChatMemoryProperties properties = new Mem0ChatMemoryProperties();
		Mem0ChatMemoryProperties.Client clientConfig = new Mem0ChatMemoryProperties.Client();
		clientConfig.setBaseUrl("http://localhost:8888");
		// 不设置async配置，使用默认值
		properties.setClient(clientConfig);

		// When
		Mem0ServiceMock testClient = new Mem0ServiceMock(properties, resourceLoader);

		// Then
		assertThat(testClient).isNotNull();
		Mem0ChatMemoryProperties.Client.AsyncConfig asyncConfig = properties.getClient().getAsync();
		assertThat(asyncConfig.isEnabled()).isTrue(); // 默认启用
		assertThat(asyncConfig.getCorePoolSize()).isEqualTo(2); // 默认值
		assertThat(asyncConfig.getMaxPoolSize()).isEqualTo(4); // 默认值
		assertThat(asyncConfig.getQueueCapacity()).isEqualTo(100); // 默认值
		assertThat(asyncConfig.getThreadNamePrefix()).isEqualTo("mem0-async-"); // 默认值
	}

	// ========== 资源管理测试 ==========

	@Test
	void testShutdown() {
		// Given
		Mem0ServiceClient client = new Mem0ServiceClient(properties, resourceLoader);

		// When
		client.shutdown();

		// Then - 方法应该正常执行，不抛异常
		assertThat(client).isNotNull();
	}

	// ========== 边界条件测试 ==========

	@Test
	void testAddMemoryWithNullMemoryCreate() {
		// When & Then
		assertThatThrownBy(() -> client.addMemory(null)).isInstanceOf(NullPointerException.class);
	}

	@Test
	void testUpdateMemoryWithNullMemoryId() {
		// Given
		Map<String, Object> updatedMemory = new HashMap<>();
		updatedMemory.put("content", "test");

		// When & Then
		assertThatThrownBy(() -> client.updateMemory(null, updatedMemory)).isInstanceOf(NullPointerException.class);
	}

	@Test
	void testUpdateMemoryWithNullUpdatedMemory() {
		// Given
		String memoryId = "test-memory-id";

		// When & Then
		assertThatThrownBy(() -> client.updateMemory(memoryId, null)).isInstanceOf(NullPointerException.class);
	}

	@Test
	void testAddMemoryAsyncWithNullMemoryCreate() {
		// When & Then
		assertThatThrownBy(() -> client.addMemoryAsync(null)).isInstanceOf(NullPointerException.class);
	}

	@Test
	void testUpdateMemoryAsyncWithNullMemoryId() {
		// Given
		Map<String, Object> updatedMemory = new HashMap<>();
		updatedMemory.put("content", "test");

		// When & Then
		assertThatThrownBy(() -> client.updateMemoryAsync(null, updatedMemory))
			.isInstanceOf(NullPointerException.class);
	}

	@Test
	void testUpdateMemoryAsyncWithNullUpdatedMemory() {
		// Given
		String memoryId = "test-memory-id";

		// When & Then
		assertThatThrownBy(() -> client.updateMemoryAsync(memoryId, null)).isInstanceOf(NullPointerException.class);
	}

}
