package com.github.novicezk.midjourney.service.impl;

import com.github.novicezk.midjourney.Constants;
import com.github.novicezk.midjourney.ReturnCode;
import com.github.novicezk.midjourney.enums.BlendDimensions;
import com.github.novicezk.midjourney.exception.AccountException;
import com.github.novicezk.midjourney.result.Message;
import com.github.novicezk.midjourney.result.SubmitResultVO;
import com.github.novicezk.midjourney.service.DiscordService;
import com.github.novicezk.midjourney.service.TaskService;
import com.github.novicezk.midjourney.service.TaskStoreService;
import com.github.novicezk.midjourney.support.Task;
import com.github.novicezk.midjourney.support.TaskQueueHelper;
import com.github.novicezk.midjourney.util.MimeTypeUtils;
import eu.maxschuster.dataurl.DataUrl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskStoreService taskStoreService;
    private final Map<String, DiscordService> discordServiceMap;
    private final TaskQueueHelper taskQueueHelper;

    @Override
    public SubmitResultVO submitImagine(Task task, List<DataUrl> dataUrls) {
        DiscordService discordService = getDiscordService(task.getAssociationKey());
        return this.taskQueueHelper.submitTask(task, () -> {
            List<String> imageUrls = new ArrayList<>();
            for (DataUrl dataUrl : dataUrls) {
                String taskFileName = getTaskFileName(task, dataUrl);
                Message<String> uploadResult = discordService.upload(taskFileName, dataUrl);
                if (uploadResult.getCode() != ReturnCode.SUCCESS) {
                    return Message.of(uploadResult.getCode(), uploadResult.getDescription());
                }
                String finalFileName = uploadResult.getResult();
                Message<String> sendImageResult = discordService.sendImageMessage("upload image: " + finalFileName, finalFileName);
                if (sendImageResult.getCode() != ReturnCode.SUCCESS) {
                    return Message.of(sendImageResult.getCode(), sendImageResult.getDescription());
                }
                imageUrls.add(sendImageResult.getResult());
            }
            if (!imageUrls.isEmpty()) {
                task.setPrompt(String.join(" ", imageUrls) + " " + task.getPrompt());
                task.setPromptEn(String.join(" ", imageUrls) + " " + task.getPromptEn());
                task.setDescription("/imagine " + task.getPrompt());
                this.taskStoreService.save(task);
            }
            return discordService.imagine(task.getPromptEn(), task.getPropertyGeneric(Constants.TASK_PROPERTY_NONCE));
        });
    }

    @Override
    public SubmitResultVO submitUpscale(Task task, String targetMessageId, String targetMessageHash, int index, int messageFlags) {
        DiscordService discordService = getDiscordService(task.getAssociationKey());
        return this.taskQueueHelper.submitTask(task, () -> discordService.upscale(targetMessageId, index, targetMessageHash, messageFlags, task.getPropertyGeneric(Constants.TASK_PROPERTY_NONCE)));
    }

    @Override
    public SubmitResultVO submitVariation(Task task, String targetMessageId, String targetMessageHash, int index, int messageFlags) {
        DiscordService discordService = getDiscordService(task.getAssociationKey());
        return this.taskQueueHelper.submitTask(task, () -> discordService.variation(targetMessageId, index, targetMessageHash, messageFlags, task.getPropertyGeneric(Constants.TASK_PROPERTY_NONCE)));
    }

    @Override
    public SubmitResultVO submitReroll(Task task, String targetMessageId, String targetMessageHash, int messageFlags) {
        DiscordService discordService = getDiscordService(task.getAssociationKey());
        return this.taskQueueHelper.submitTask(task, () -> discordService.reroll(targetMessageId, targetMessageHash, messageFlags, task.getPropertyGeneric(Constants.TASK_PROPERTY_NONCE)));
    }

    @Override
    public SubmitResultVO submitDescribe(Task task, DataUrl dataUrl) {
        DiscordService discordService = getDiscordService(task.getAssociationKey());
        return this.taskQueueHelper.submitTask(task, () -> {
            String taskFileName = getTaskFileName(task, dataUrl);
            Message<String> uploadResult = discordService.upload(taskFileName, dataUrl);
            if (uploadResult.getCode() != ReturnCode.SUCCESS) {
                return Message.of(uploadResult.getCode(), uploadResult.getDescription());
            }
            String finalFileName = uploadResult.getResult();
            return discordService.describe(finalFileName, task.getPropertyGeneric(Constants.TASK_PROPERTY_NONCE));
        });
    }

    @Override
    public SubmitResultVO submitBlend(Task task, List<DataUrl> dataUrls, BlendDimensions dimensions) {
        DiscordService discordService = getDiscordService(task.getAssociationKey());
        return this.taskQueueHelper.submitTask(task, () -> {
            List<String> finalFileNames = new ArrayList<>();
            for (DataUrl dataUrl : dataUrls) {
                String taskFileName = getTaskFileName(task, dataUrl);
                Message<String> uploadResult = discordService.upload(taskFileName, dataUrl);
                if (uploadResult.getCode() != ReturnCode.SUCCESS) {
                    return Message.of(uploadResult.getCode(), uploadResult.getDescription());
                }
                finalFileNames.add(uploadResult.getResult());
            }
            return discordService.blend(finalFileNames, dimensions, task.getPropertyGeneric(Constants.TASK_PROPERTY_NONCE));
        });
    }

    /**
     * 获取任务文件名称
     *
     * @param task    task
     * @param dataUrl dataUrl
     * @return String
     */
    private String getTaskFileName(Task task, DataUrl dataUrl) {
        return task.getId() + "." + MimeTypeUtils.guessFileSuffix(dataUrl.getMimeType());
    }

    /**
     * 根据任务轮询的key获取对应账号的服务
     * 由于绘图变化需关联对应账号的任务, 但有可能更换了账号, 所以这边抛异常拦截已更换账号的任务的绘图变化
     * 任务存到内存不会出现此情况
     * 任务持久化后, 并且替换/删除账号配置, 使用绘图变化就有可能出现此情况
     *
     * @param associationKey associationKey
     * @return DiscordService
     */
    private DiscordService getDiscordService(String associationKey) {
        return Optional.ofNullable(this.discordServiceMap.get(associationKey))
                .orElseThrow(() -> new AccountException(String.format("找不到%s对应账号, 请检查账号配置!", associationKey)));
    }
}
