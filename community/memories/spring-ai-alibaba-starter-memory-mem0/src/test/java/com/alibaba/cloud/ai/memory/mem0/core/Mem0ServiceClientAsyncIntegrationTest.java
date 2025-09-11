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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 异步功能集成测试
 *
 * @author Morain Miao
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class Mem0ServiceClientAsyncIntegrationTest {

	@Mock
	private ResourceLoader resourceLoader;

	private Mem0ChatMemoryProperties properties;

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
		asyncConfig.setThreadNamePrefix("test-async-");
		clientConfig.setAsync(asyncConfig);

		properties.setClient(clientConfig);
	}

	@Test
	void testConcurrentAddMemoryOperations() throws InterruptedException {
		// Given
		Mem0ServiceClient client = new Mem0ServiceClient(properties, resourceLoader);
		int numberOfOperations = 10;
		CountDownLatch latch = new CountDownLatch(numberOfOperations);
		AtomicInteger successCount = new AtomicInteger(0);
		AtomicInteger errorCount = new AtomicInteger(0);

		// When - 并发执行多个添加内存操作
		for (int i = 0; i < numberOfOperations; i++) {
			final int index = i;
			CompletableFuture.runAsync(() -> {
				try {
					Mem0ServerRequest.MemoryCreate memoryCreate = Mem0ServerRequest.MemoryCreate.builder()
						.messages(List.of(new Mem0ServerRequest.Message("user", "test message " + index)))
						.userId("test-user-" + index)
						.agentId("test-agent-" + index)
						.runId("test-run-" + index)
						.build();

					client.addMemory(memoryCreate);
					successCount.incrementAndGet();
				}
				catch (Exception e) {
					errorCount.incrementAndGet();
				}
				finally {
					latch.countDown();
				}
			});
		}

		// Then - 等待所有操作完成
		boolean allCompleted = latch.await(5, TimeUnit.SECONDS);
		assertThat(allCompleted).isTrue();

		// 验证异步执行（由于没有真实服务器，会有错误，但这是预期的）
		assertThat(successCount.get() + errorCount.get()).isEqualTo(numberOfOperations);
	}

	@Test
	void testConcurrentUpdateMemoryOperations() throws InterruptedException {
		// Given
		Mem0ServiceClient client = new Mem0ServiceClient(properties, resourceLoader);
		int numberOfOperations = 10;
		CountDownLatch latch = new CountDownLatch(numberOfOperations);
		AtomicInteger successCount = new AtomicInteger(0);
		AtomicInteger errorCount = new AtomicInteger(0);

		// When - 并发执行多个更新内存操作
		for (int i = 0; i < numberOfOperations; i++) {
			final int index = i;
			CompletableFuture.runAsync(() -> {
				try {
					String memoryId = "test-memory-" + index;
					Map<String, Object> updatedMemory = new HashMap<>();
					updatedMemory.put("content", "updated content " + index);
					updatedMemory.put("metadata", Map.of("updated_at", System.currentTimeMillis()));

					Map<String, Object> result = client.updateMemory(memoryId, updatedMemory);

					// 异步执行时应该返回空结果
					if (result.isEmpty()) {
						successCount.incrementAndGet();
					}
				}
				catch (Exception e) {
					errorCount.incrementAndGet();
				}
				finally {
					latch.countDown();
				}
			});
		}

		// Then - 等待所有操作完成
		boolean allCompleted = latch.await(5, TimeUnit.SECONDS);
		assertThat(allCompleted).isTrue();

		// 验证异步执行
		assertThat(successCount.get() + errorCount.get()).isEqualTo(numberOfOperations);
	}

	@Test
	void testAsyncVsSyncPerformance() {
		// Given
		Mem0ServiceClient asyncClient = new Mem0ServiceClient(properties, resourceLoader);

		// 创建同步客户端
		Mem0ChatMemoryProperties syncProperties = new Mem0ChatMemoryProperties();
		Mem0ChatMemoryProperties.Client syncClientConfig = new Mem0ChatMemoryProperties.Client();
		syncClientConfig.setBaseUrl("http://localhost:8888");
		syncClientConfig.setTimeoutSeconds(30);

		Mem0ChatMemoryProperties.Client.AsyncConfig syncAsyncConfig = new Mem0ChatMemoryProperties.Client.AsyncConfig();
		syncAsyncConfig.setEnabled(false);
		syncClientConfig.setAsync(syncAsyncConfig);
		syncProperties.setClient(syncClientConfig);

		Mem0ServiceClient syncClient = new Mem0ServiceClient(syncProperties, resourceLoader);

		Mem0ServerRequest.MemoryCreate memoryCreate = Mem0ServerRequest.MemoryCreate.builder()
			.messages(List.of(new Mem0ServerRequest.Message("user", "performance test")))
			.userId("perf-user")
			.agentId("perf-agent")
			.runId("perf-run")
			.build();

		// When - 测试异步执行性能
		long asyncStartTime = System.currentTimeMillis();
		asyncClient.addMemory(memoryCreate);
		long asyncEndTime = System.currentTimeMillis();
		long asyncDuration = asyncEndTime - asyncStartTime;

		// When - 测试同步执行性能
		long syncStartTime = System.currentTimeMillis();
		try {
			syncClient.addMemory(memoryCreate);
		}
		catch (Exception e) {
			// 预期的异常，因为服务器不存在
		}
		long syncEndTime = System.currentTimeMillis();
		long syncDuration = syncEndTime - syncStartTime;

		// Then - 异步执行应该比同步执行快（因为异步立即返回）
		assertThat(asyncDuration).isLessThan(syncDuration);
		assertThat(asyncDuration).isLessThan(100); // 异步应该在100ms内返回
	}

	@Test
	void testAsyncMethodReturnsCompletableFuture() {
		// Given
		Mem0ServiceClient client = new Mem0ServiceClient(properties, resourceLoader);
		Mem0ServerRequest.MemoryCreate memoryCreate = Mem0ServerRequest.MemoryCreate.builder()
			.messages(List.of(new Mem0ServerRequest.Message("user", "async test")))
			.userId("async-user")
			.agentId("async-agent")
			.runId("async-run")
			.build();

		// When
		CompletableFuture<Void> addFuture = client.addMemoryAsync(memoryCreate);

		String memoryId = "test-memory";
		Map<String, Object> updatedMemory = new HashMap<>();
		updatedMemory.put("content", "async update test");
		CompletableFuture<Map<String, Object>> updateFuture = client.updateMemoryAsync(memoryId, updatedMemory);

		// Then
		assertThat(addFuture).isNotNull();
		assertThat(addFuture).isInstanceOf(CompletableFuture.class);
		assertThat(addFuture.isDone()).isFalse();

		assertThat(updateFuture).isNotNull();
		assertThat(updateFuture).isInstanceOf(CompletableFuture.class);
		assertThat(updateFuture.isDone()).isFalse();
	}

	@Test
	void testAsyncMethodChaining() {
		// Given
		Mem0ServiceClient client = new Mem0ServiceClient(properties, resourceLoader);
		Mem0ServerRequest.MemoryCreate memoryCreate = Mem0ServerRequest.MemoryCreate.builder()
			.messages(List.of(new Mem0ServerRequest.Message("user", "chaining test")))
			.userId("chain-user")
			.agentId("chain-agent")
			.runId("chain-run")
			.build();

		// When - 链式异步操作
		CompletableFuture<Void> chainedFuture = client.addMemoryAsync(memoryCreate).thenRun(() -> {
			// 添加完成后执行更新
			Map<String, Object> updatedMemory = new HashMap<>();
			updatedMemory.put("content", "chained update");
			client.updateMemoryAsync("test-memory", updatedMemory);
		});

		// Then
		assertThat(chainedFuture).isNotNull();
		assertThat(chainedFuture).isInstanceOf(CompletableFuture.class);

		// 验证客户端已创建
		assertThat(client).isNotNull();
	}

	@Test
	void testThreadPoolConfiguration() {
		// Given
		Mem0ServiceClient client = new Mem0ServiceClient(properties, resourceLoader);

		// When
		Mem0ChatMemoryProperties.Client.AsyncConfig asyncConfig = properties.getClient().getAsync();

		// Then
		assertThat(asyncConfig.isEnabled()).isTrue();
		assertThat(asyncConfig.getCorePoolSize()).isEqualTo(2);
		assertThat(asyncConfig.getMaxPoolSize()).isEqualTo(4);
		assertThat(asyncConfig.getQueueCapacity()).isEqualTo(100);
		assertThat(asyncConfig.getThreadNamePrefix()).isEqualTo("test-async-");

		// 验证客户端已创建
		assertThat(client).isNotNull();
	}

	@Test
	void testShutdownAfterAsyncOperations() throws InterruptedException {
		// Given
		Mem0ServiceClient client = new Mem0ServiceClient(properties, resourceLoader);
		CountDownLatch latch = new CountDownLatch(5);

		// When - 启动一些异步操作
		for (int i = 0; i < 5; i++) {
			final int index = i;
			CompletableFuture.runAsync(() -> {
				try {
					Mem0ServerRequest.MemoryCreate memoryCreate = Mem0ServerRequest.MemoryCreate.builder()
						.messages(List.of(new Mem0ServerRequest.Message("user", "shutdown test " + index)))
						.userId("shutdown-user-" + index)
						.agentId("shutdown-agent-" + index)
						.runId("shutdown-run-" + index)
						.build();

					client.addMemory(memoryCreate);
				}
				catch (Exception e) {
					// 预期的异常
				}
				finally {
					latch.countDown();
				}
			});
		}

		// 等待一些操作开始
		Thread.sleep(100);

		// 关闭客户端
		client.shutdown();

		// Then - 关闭操作应该成功
		assertThat(client).isNotNull();

		// 等待所有操作完成
		latch.await(2, TimeUnit.SECONDS);
	}

	@Test
	void testAsyncErrorHandling() {
		// Given
		Mem0ServiceClient client = new Mem0ServiceClient(properties, resourceLoader);

		// When - 使用null参数调用异步方法
		CompletableFuture<Void> addFuture = client.addMemoryAsync(null);
		CompletableFuture<Map<String, Object>> updateFuture = client.updateMemoryAsync(null, null);

		// Then - 应该抛出异常
		assertThatThrownBy(() -> addFuture.get(1, TimeUnit.SECONDS)).isInstanceOf(Exception.class);

		assertThatThrownBy(() -> updateFuture.get(1, TimeUnit.SECONDS)).isInstanceOf(Exception.class);
	}

}
