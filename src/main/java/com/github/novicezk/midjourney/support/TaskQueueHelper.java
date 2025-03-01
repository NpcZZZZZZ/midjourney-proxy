package com.github.novicezk.midjourney.support;

import cn.hutool.core.text.CharSequenceUtil;
import com.github.novicezk.midjourney.ProxyProperties;
import com.github.novicezk.midjourney.ReturnCode;
import com.github.novicezk.midjourney.enums.TaskStatus;
import com.github.novicezk.midjourney.result.Message;
import com.github.novicezk.midjourney.result.SubmitResultVO;
import com.github.novicezk.midjourney.service.NotifyService;
import com.github.novicezk.midjourney.service.TaskStoreService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.function.Predicate;
import java.util.stream.Stream;

@Slf4j
@Component
public class TaskQueueHelper {
    @Resource
    private TaskStoreService taskStoreService;
    @Resource
    private NotifyService notifyService;

    private final ThreadPoolTaskExecutor taskExecutor;
    private final List<Task> runningTasks;
    private final Map<String, Future<?>> taskFutureMap;

    public TaskQueueHelper(ProxyProperties properties) {
        ProxyProperties.TaskQueueConfig queueConfig = properties.getQueue();
        ProxyProperties.DiscordConfig discord = properties.getDiscord();
        int size = discord.isUserWss() ? discord.getDiscordAccountConfigList().size() : discord.getBotTokenConfigList().size();
        int coreSize = queueConfig.getCoreSize() * size;
        this.runningTasks = new CopyOnWriteArrayList<>();
        this.taskFutureMap = new ConcurrentHashMap<>();
        this.taskExecutor = new ThreadPoolTaskExecutor();
        this.taskExecutor.setCorePoolSize(coreSize);
        this.taskExecutor.setMaxPoolSize(coreSize);
        this.taskExecutor.setQueueCapacity(queueConfig.getQueueSize() * size);
        this.taskExecutor.setThreadNamePrefix("TaskQueue-");
        this.taskExecutor.initialize();
    }

    public Set<String> getQueueTaskIds() {
        return this.taskFutureMap.keySet();
    }

    public Task getRunningTask(String id) {
        if (CharSequenceUtil.isBlank(id)) {
            return null;
        }
        return this.runningTasks.stream().filter(t -> id.equals(t.getId())).findFirst().orElse(null);
    }

    public Task getRunningTaskByNonce(String nonce) {
        if (CharSequenceUtil.isBlank(nonce)) {
            return null;
        }
        TaskCondition condition = new TaskCondition().setNonce(nonce);
        return findRunningTask(condition).findFirst().orElse(null);
    }

    public Stream<Task> findRunningTask(Predicate<Task> condition) {
        return this.runningTasks.stream().filter(condition);
    }


    public Future<?> getRunningFuture(String taskId) {
        return this.taskFutureMap.get(taskId);
    }

    public SubmitResultVO submitTask(Task task, Callable<Message<Void>> discordSubmit) {
        this.taskStoreService.save(task);
        int size;
        try {
            size = this.taskExecutor.getThreadPoolExecutor().getQueue().size();
            Future<?> future = this.taskExecutor.submit(() -> executeTask(task, discordSubmit));
            this.taskFutureMap.put(task.getId(), future);
        } catch (RejectedExecutionException e) {
            this.taskStoreService.delete(task.getId());
            return SubmitResultVO.fail(ReturnCode.QUEUE_REJECTED, "队列已满，请稍后尝试");
        } catch (Exception e) {
            log.error("submit task error", e);
            return SubmitResultVO.fail(ReturnCode.FAILURE, "提交失败，系统异常");
        }
        if (size == 0) {
            return SubmitResultVO.of(ReturnCode.SUCCESS, "提交成功", task.getId());
        } else {
            return SubmitResultVO.of(ReturnCode.IN_QUEUE, "排队中，前面还有" + size + "个任务", task.getId())
                    .setProperty("numberOfQueues", size);
        }
    }


    private void executeTask(Task task, Callable<Message<Void>> discordSubmit) {
        this.runningTasks.add(task);
        try {
            task.start();
            Message<Void> result = discordSubmit.call();
            if (result.getCode() != ReturnCode.SUCCESS) {
                task.fail(result.getDescription());
                saveAndNotify(task);
                return;
            }
            saveAndNotify(task);
            do {
                task.sleep();
                saveAndNotify(task);
            } while (task.getStatus() == TaskStatus.IN_PROGRESS);
            log.debug("task finished, id: {}, status: {}", task.getId(), task.getStatus());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.error("task execute error", e);
            task.fail("执行错误，系统异常");
            saveAndNotify(task);
        } finally {
            this.runningTasks.remove(task);
            this.taskFutureMap.remove(task.getId());
        }
    }

    public void saveAndNotify(Task task) {
        this.taskStoreService.save(task);
        this.notifyService.notifyTaskChange(task);
    }
}

