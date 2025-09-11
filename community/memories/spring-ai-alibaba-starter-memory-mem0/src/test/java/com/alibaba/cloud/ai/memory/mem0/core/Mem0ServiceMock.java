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
import org.springframework.core.io.ResourceLoader;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 可测试的 Mem0ServiceClient，完全 Mock 实现，避免真实网络请求
 *
 * @author Morain Miao
 * @since 1.0.0
 */
public class Mem0ServiceMock {

	private final Mem0ChatMemoryProperties config;

	public Mem0ServiceMock(Mem0ChatMemoryProperties config, ResourceLoader resourceLoader) {
		if (config == null) {
			throw new NullPointerException("Config cannot be null");
		}
		this.config = config;
	}

	// Mock 实现，避免真实网络请求
	public void addMemory(Mem0ServerRequest.MemoryCreate memoryCreate) {
		if (memoryCreate == null) {
			throw new NullPointerException("MemoryCreate cannot be null");
		}

		if (config.getClient().getAsync().isEnabled()) {
			// 模拟异步执行
			CompletableFuture.runAsync(() -> {
				// 模拟处理
				try {
					Thread.sleep(10); // 模拟处理时间
				}
				catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			});
		}
		else {
			// 模拟同步执行
			try {
				Thread.sleep(10); // 模拟处理时间
			}
			catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
	}

	public Map<String, Object> updateMemory(String memoryId, Map<String, Object> updatedMemory) {
		if (memoryId == null) {
			throw new NullPointerException("MemoryId cannot be null");
		}
		if (updatedMemory == null) {
			throw new NullPointerException("UpdatedMemory cannot be null");
		}

		Map<String, Object> result = new HashMap<>();
		result.put("success", true);
		result.put("message", "Memory updated successfully");
		result.put("memoryId", memoryId);

		if (config.getClient().getAsync().isEnabled()) {
			// 模拟异步执行，立即返回空结果
			CompletableFuture.runAsync(() -> {
				// 模拟处理
				try {
					Thread.sleep(10); // 模拟处理时间
				}
				catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			});
			return new HashMap<>(); // 异步时返回空结果
		}
		else {
			// 模拟同步执行，返回结果
			try {
				Thread.sleep(10); // 模拟处理时间
			}
			catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
			return result;
		}
	}

	public CompletableFuture<Void> addMemoryAsync(Mem0ServerRequest.MemoryCreate memoryCreate) {
		if (memoryCreate == null) {
			throw new NullPointerException("MemoryCreate cannot be null");
		}

		return CompletableFuture.runAsync(() -> {
			// 模拟处理
			try {
				Thread.sleep(10); // 模拟处理时间
			}
			catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		});
	}

	public CompletableFuture<Map<String, Object>> updateMemoryAsync(String memoryId,
			Map<String, Object> updatedMemory) {
		if (memoryId == null) {
			throw new NullPointerException("MemoryId cannot be null");
		}
		if (updatedMemory == null) {
			throw new NullPointerException("UpdatedMemory cannot be null");
		}

		return CompletableFuture.supplyAsync(() -> {
			// 模拟处理
			try {
				Thread.sleep(10); // 模拟处理时间
			}
			catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}

			Map<String, Object> result = new HashMap<>();
			result.put("success", true);
			result.put("message", "Memory updated successfully");
			result.put("memoryId", memoryId);
			return result;
		});
	}

	public void shutdown() {
		// Mock 实现
	}

}
