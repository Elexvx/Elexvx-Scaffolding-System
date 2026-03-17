package com.tencent.tdesign.service;

import com.tencent.tdesign.entity.MessageEntity;
import com.tencent.tdesign.mapper.MessageMapper;
import com.tencent.tdesign.security.AccessControlService;
import com.tencent.tdesign.vo.Message;
import java.security.AccessControlException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MessageService {
  private static final String MESSAGE_BROADCAST_PERMISSION = "system:Message:broadcast";
  private static final String MESSAGE_SEND_PERMISSION = "system:Message:send";
  private static final int BROADCAST_BATCH_SIZE = 500;
  private final MessageMapper messageMapper;
  private final OperationLogService operationLogService;
  private final AccessControlService accessControlService;
  private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

  public MessageService(
    MessageMapper messageMapper,
    OperationLogService operationLogService,
    AccessControlService accessControlService
  ) {
    this.messageMapper = messageMapper;
    this.operationLogService = operationLogService;
    this.accessControlService = accessControlService;
  }

  @Transactional
  public int broadcast(String content, String type, String quality) {
    enforceBroadcastPermission();
    return broadcastInternal(content, type, quality);
  }

  @Transactional
  public int broadcastSystem(String content, String type, String quality) {
    return broadcastInternal(content, type, quality);
  }

  private int broadcastInternal(String content, String type, String quality) {
    LocalDateTime now = LocalDateTime.now();
    int offset = 0;
    int total = 0;
    while (true) {
      List<Long> userIds = messageMapper.selectUserIdsByPage(offset, BROADCAST_BATCH_SIZE);
      if (userIds == null || userIds.isEmpty()) {
        break;
      }
      List<MessageEntity> batch = new ArrayList<>(userIds.size());
      for (Long userId : userIds) {
        if (userId == null) continue;
        MessageEntity e = new MessageEntity();
        e.setId(UUID.randomUUID().toString());
        e.setToUserId(userId);
        e.setContent(content);
        e.setType(type);
        e.setStatus(true);
        e.setCollected(false);
        e.setQuality(quality);
        e.setCreatedAt(now);
        batch.add(e);
      }
      if (!batch.isEmpty()) {
        messageMapper.insertBatch(batch);
        total += batch.size();
      }
      offset += BROADCAST_BATCH_SIZE;
    }
    operationLogService.log("SEND", "消息发送", "广播消息发送成功，数量: " + total);
    return total;
  }

  @Transactional(readOnly = true)
  public List<Message> list(Long userId) {
    return messageMapper.listByUserId(userId).stream()
      .map(this::toVo)
      .collect(Collectors.toList());
  }

  @Transactional
  public Message send(Long currentUserId, Long toUserId, String content, String type, String quality) {
    enforceSendScope(currentUserId, toUserId);
    MessageEntity e = new MessageEntity();
    e.setId(UUID.randomUUID().toString());
    e.setToUserId(toUserId);
    e.setContent(content);
    e.setType(type);
    e.setStatus(true);
    e.setCollected(false);
    e.setQuality(quality);
    e.setCreatedAt(LocalDateTime.now());
    messageMapper.insert(e);
    operationLogService.log("SEND", "消息发送", "发送消息给用户: " + toUserId);
    return toVo(e);
  }

  @Transactional
  public boolean markRead(Long userId, String id, boolean read) {
    MessageEntity e = messageMapper.selectById(id);
    if (e == null) return false;
    ensureOwner(e, userId);
    e.setStatus(!read);
    messageMapper.update(e);
    return true;
  }

  @Transactional
  public int markAllRead(Long userId) {
    return messageMapper.markAllReadByUserId(userId);
  }

  @Transactional
  public boolean delete(Long userId, String id) {
    MessageEntity e = messageMapper.selectById(id);
    if (e == null) return false;
    ensureOwner(e, userId);
    messageMapper.deleteById(id);
    return true;
  }

  private void enforceBroadcastPermission() {
    if (accessControlService.hasRole("admin")) {
      return;
    }
    if (accessControlService.hasPermission(MESSAGE_BROADCAST_PERMISSION)) {
      return;
    }
    throw new AccessControlException("仅管理员或具备消息广播权限的用户可执行该操作");
  }

  private void enforceSendScope(Long currentUserId, Long toUserId) {
    if (accessControlService.hasRole("admin") || accessControlService.hasPermission(MESSAGE_SEND_PERMISSION)) {
      return;
    }
    if (currentUserId != null && Objects.equals(currentUserId, toUserId)) {
      return;
    }
    throw new AccessControlException("当前账号仅允许向自己发送消息");
  }

  private void ensureOwner(MessageEntity entity, Long currentUserId) {
    if (entity == null || currentUserId == null) {
      throw new AccessControlException("消息归属校验失败");
    }
    if (!Objects.equals(entity.getToUserId(), currentUserId)) {
      throw new AccessControlException("无权操作不属于当前用户的消息");
    }
  }

  private Message toVo(MessageEntity e) {
    return new Message(e.getId(), e.getContent(), e.getType(), e.isStatus(), e.isCollected(), fmt.format(e.getCreatedAt()), e.getQuality());
  }
}
