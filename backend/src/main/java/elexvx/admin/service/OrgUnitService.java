package elexvx.admin.service;

import elexvx.admin.dto.OrgUnitReorderRequest;
import elexvx.admin.dto.OrgUnitLeaderPair;
import elexvx.admin.dto.OrgUnitUpsertRequest;
import elexvx.admin.entity.OrgUnitEntity;
import elexvx.admin.entity.UserEntity;
import elexvx.admin.entity.UserOrgUnitRelation;
import elexvx.admin.enums.OrgUnitType;
import elexvx.admin.exception.BusinessException;
import elexvx.admin.exception.ErrorCodes;
import elexvx.admin.mapper.OrgUnitLeaderMapper;
import elexvx.admin.mapper.OrgUnitMapper;
import elexvx.admin.mapper.UserOrgUnitMapper;
import elexvx.admin.mapper.UserMapper;
import elexvx.admin.vo.OrgUnitNode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrgUnitService {
  private final OrgUnitMapper orgUnitMapper;
  private final OrgUnitLeaderMapper leaderMapper;
  private final UserMapper userMapper;
  private final UserOrgUnitMapper userOrgUnitMapper;

  public OrgUnitService(
    OrgUnitMapper orgUnitMapper,
    OrgUnitLeaderMapper leaderMapper,
    UserMapper userMapper,
    UserOrgUnitMapper userOrgUnitMapper
  ) {
    this.orgUnitMapper = orgUnitMapper;
    this.leaderMapper = leaderMapper;
    this.userMapper = userMapper;
    this.userOrgUnitMapper = userOrgUnitMapper;
  }

  private static BusinessException badRequest(String message) {
    return new BusinessException(ErrorCodes.BAD_REQUEST, message);
  }

  public List<OrgUnitNode> tree() {
    List<OrgUnitEntity> entities = orgUnitMapper.selectAll();
    Map<Long, OrgUnitNode> nodeMap = new LinkedHashMap<>();
    for (OrgUnitEntity entity : entities) {
      nodeMap.put(entity.getId(), toNode(entity));
    }

    attachLeaderInfo(nodeMap);

    List<OrgUnitNode> roots = new ArrayList<>();
    for (OrgUnitNode node : nodeMap.values()) {
      if (node.getParentId() == null || node.getParentId() == 0) {
        roots.add(node);
      } else {
        OrgUnitNode parent = nodeMap.get(node.getParentId());
        if (parent != null) {
          parent.getChildren().add(node);
        } else {
          roots.add(node);
        }
      }
    }
    attachUserCount(roots);
    return roots;
  }

  public OrgUnitNode get(long id) {
    OrgUnitEntity entity = orgUnitMapper.selectById(id);
    if (entity == null) throw badRequest("机构不存在");
    OrgUnitNode node = toNode(entity);
    Map<Long, OrgUnitNode> nodeMap = Map.of(id, node);
    attachLeaderInfo(nodeMap);
    node.setUserCount(calcUserCountForOrgUnit(id));
    return node;
  }

  @Transactional
  public OrgUnitNode create(OrgUnitUpsertRequest req) {
    OrgUnitEntity entity = new OrgUnitEntity();
    apply(entity, req);
    orgUnitMapper.insert(entity);
    replaceLeaders(entity.getId(), req.getLeaderIds());
    return get(entity.getId());
  }

  @Transactional
  public OrgUnitNode update(long id, OrgUnitUpsertRequest req) {
    OrgUnitEntity entity = orgUnitMapper.selectById(id);
    if (entity == null) throw badRequest("机构不存在");
    apply(entity, req);
    orgUnitMapper.update(entity);
    replaceLeaders(id, req.getLeaderIds());
    return get(id);
  }

  @Transactional
  public boolean reorder(OrgUnitReorderRequest req) {
    List<OrgUnitReorderRequest.Item> items = req.getItems();
    Set<Long> ids = new HashSet<>();
    for (OrgUnitReorderRequest.Item it : items) {
      if (!ids.add(it.getId())) throw badRequest("items 存在重复 id");
      if (it.getParentId() != null && Objects.equals(it.getId(), it.getParentId())) {
        throw badRequest("parentId 不能指向自己");
      }
    }

    List<OrgUnitEntity> all = orgUnitMapper.selectAll();
    Map<Long, OrgUnitEntity> allMap = new HashMap<>();
    Map<Long, Long> parentMap = new HashMap<>();
    for (OrgUnitEntity e : all) {
      allMap.put(e.getId(), e);
      parentMap.put(e.getId(), e.getParentId());
    }

    for (OrgUnitReorderRequest.Item it : items) {
      if (!allMap.containsKey(it.getId())) throw badRequest("存在无效机构 id");
      Long pid = it.getParentId();
      if (pid != null && pid == 0) pid = null;
      if (pid != null && !allMap.containsKey(pid)) throw badRequest("parentId 无效: " + pid);
      parentMap.put(it.getId(), pid);
    }

    ensureNoCycles(parentMap);

    for (OrgUnitReorderRequest.Item it : items) {
      Long pid = it.getParentId();
      if (pid != null && pid == 0) pid = null;
      orgUnitMapper.updateParentAndSort(it.getId(), pid, it.getSortOrder());
    }
    return true;
  }

  @Transactional
  public boolean delete(long id) {
    if (orgUnitMapper.countChildren(id) > 0) {
      throw badRequest("请先删除子级机构");
    }
    leaderMapper.deleteByOrgUnitId(id);
    orgUnitMapper.deleteById(id);
    return true;
  }

  @Transactional
  public boolean addUsers(long orgUnitId, List<Long> userIds) {
    OrgUnitEntity entity = orgUnitMapper.selectById(orgUnitId);
    if (entity == null) throw badRequest("机构不存在");
    List<Long> cleaned = Stream.ofNullable(userIds)
      .flatMap(List::stream)
      .filter(Objects::nonNull)
      .distinct()
      .toList();
    if (cleaned.isEmpty()) return true;

    List<UserEntity> existingUsers = userMapper.selectByIds(cleaned);
    Set<Long> existingUserIds = existingUsers.stream().map(UserEntity::getId).filter(Objects::nonNull).collect(Collectors.toSet());
    List<Long> invalid = cleaned.stream().filter((id) -> !existingUserIds.contains(id)).toList();
    if (!invalid.isEmpty()) throw badRequest("存在无效用户: " + invalid);

    Set<Long> alreadyInOrg = new HashSet<>(userOrgUnitMapper.selectUserIdsByOrgUnitId(orgUnitId));
    List<Long> toInsert = cleaned.stream().filter((uid) -> !alreadyInOrg.contains(uid)).toList();
    if (toInsert.isEmpty()) return true;

    userOrgUnitMapper.insertOrgUnitUsers(orgUnitId, toInsert);
    return true;
  }

  private void apply(OrgUnitEntity entity, OrgUnitUpsertRequest req) {
    entity.setParentId(req.getParentId());
    entity.setName(req.getName());
    entity.setShortName(req.getShortName());
    OrgUnitType type = OrgUnitType.fromValue(req.getType());
    if (type == null) throw badRequest("机构类型不合法");
    entity.setType(type.name());
    Integer sortOrder = req.getSortOrder();
    if (sortOrder == null) {
      if (entity.getId() == null) {
        Integer max = orgUnitMapper.selectMaxSortOrder(entity.getParentId());
        sortOrder = (max == null ? 0 : max) + 1;
      } else {
        sortOrder = entity.getSortOrder();
      }
    }
    entity.setSortOrder(sortOrder == null ? 0 : sortOrder);
    entity.setStatus(req.getStatus() == null ? 1 : req.getStatus());
    entity.setPhone(req.getPhone());
    entity.setEmail(req.getEmail());
  }

  private void ensureNoCycles(Map<Long, Long> parentMap) {
    Set<Long> visiting = new HashSet<>();
    Set<Long> visited = new HashSet<>();
    for (Long id : parentMap.keySet()) {
      if (visited.contains(id)) continue;
      Long cur = id;
      visiting.clear();
      while (cur != null) {
        if (!parentMap.containsKey(cur)) break;
        if (!visiting.add(cur)) throw badRequest("检测到循环引用");
        cur = parentMap.get(cur);
      }
      visited.addAll(visiting);
    }
  }

  private OrgUnitNode toNode(OrgUnitEntity entity) {
    OrgUnitNode node = new OrgUnitNode();
    node.setId(entity.getId());
    node.setParentId(entity.getParentId());
    node.setName(entity.getName());
    node.setShortName(entity.getShortName());
    node.setType(entity.getType());
    OrgUnitType type = OrgUnitType.fromValue(entity.getType());
    node.setTypeLabel(type == null ? entity.getType() : type.getLabel());
    node.setSortOrder(entity.getSortOrder());
    node.setStatus(entity.getStatus());
    node.setPhone(entity.getPhone());
    node.setEmail(entity.getEmail());
    node.setCreatedAt(entity.getCreatedAt());
    node.setUpdatedAt(entity.getUpdatedAt());
    return node;
  }

  private void replaceLeaders(Long orgUnitId, List<Long> leaderIds) {
    leaderMapper.deleteByOrgUnitId(orgUnitId);
    if (leaderIds == null || leaderIds.isEmpty()) return;
    List<Long> cleaned = leaderIds.stream().filter(Objects::nonNull).distinct().toList();
    if (!cleaned.isEmpty()) {
      leaderMapper.insertLeaders(orgUnitId, cleaned);
    }
  }

  private void attachLeaderInfo(Map<Long, OrgUnitNode> nodes) {
    if (nodes.isEmpty()) return;
    List<Long> orgUnitIds = new ArrayList<>(nodes.keySet());
    Map<Long, List<Long>> leaderMap = new HashMap<>();
    Set<Long> userIds = new HashSet<>();
    List<OrgUnitLeaderPair> pairs = leaderMapper.selectLeaderPairsByOrgUnitIds(orgUnitIds);
    for (OrgUnitLeaderPair pair : pairs) {
      if (pair == null || pair.getOrgUnitId() == null || pair.getUserId() == null) continue;
      leaderMap.computeIfAbsent(pair.getOrgUnitId(), key -> new ArrayList<>()).add(pair.getUserId());
      userIds.add(pair.getUserId());
    }
    Map<Long, String> userNameMap = new HashMap<>();
    if (!userIds.isEmpty()) {
      List<UserEntity> users = userMapper.selectByIds(userIds);
      for (UserEntity user : users) {
        if (user.getId() != null) {
          userNameMap.put(user.getId(), user.getName());
        }
      }
    }
    for (Map.Entry<Long, OrgUnitNode> entry : nodes.entrySet()) {
      List<Long> leaders = leaderMap.get(entry.getKey());
      if (leaders == null) continue;
      OrgUnitNode node = entry.getValue();
      node.setLeaderIds(leaders);
      List<String> leaderNames = leaders.stream()
        .map(userNameMap::get)
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
      node.setLeaderNames(leaderNames);
    }
  }

  private void attachUserCount(List<OrgUnitNode> roots) {
    if (roots == null || roots.isEmpty()) return;
    List<UserOrgUnitRelation> relations = userOrgUnitMapper.selectAll();
    Map<Long, Set<Long>> direct = new HashMap<>();
    for (UserOrgUnitRelation rel : relations) {
      if (rel == null || rel.getOrgUnitId() == null || rel.getUserId() == null) continue;
      direct.computeIfAbsent(rel.getOrgUnitId(), (k) -> new HashSet<>()).add(rel.getUserId());
    }
    for (OrgUnitNode root : roots) {
      computeUserSet(root, direct);
    }
  }

  private Set<Long> computeUserSet(OrgUnitNode node, Map<Long, Set<Long>> direct) {
    Set<Long> set = new HashSet<>(direct.getOrDefault(node.getId(), Set.of()));
    if (node.getChildren() != null && !node.getChildren().isEmpty()) {
      for (OrgUnitNode child : node.getChildren()) {
        set.addAll(computeUserSet(child, direct));
      }
    }
    node.setUserCount(set.size());
    return set;
  }

  private int calcUserCountForOrgUnit(long orgUnitId) {
    List<OrgUnitEntity> entities = orgUnitMapper.selectAll();
    Map<Long, List<Long>> children = new HashMap<>();
    for (OrgUnitEntity e : entities) {
      Long pid = e.getParentId();
      if (pid == null || pid == 0) continue;
      children.computeIfAbsent(pid, (k) -> new ArrayList<>()).add(e.getId());
    }

    Set<Long> subtreeIds = new HashSet<>();
    ArrayList<Long> stack = new ArrayList<>();
    stack.add(orgUnitId);
    while (!stack.isEmpty()) {
      Long cur = stack.remove(stack.size() - 1);
      if (cur == null || !subtreeIds.add(cur)) continue;
      List<Long> cs = children.get(cur);
      if (cs != null) stack.addAll(cs);
    }

    if (subtreeIds.isEmpty()) return 0;
    List<UserOrgUnitRelation> relations = userOrgUnitMapper.selectAll();
    Set<Long> userIds = new HashSet<>();
    for (UserOrgUnitRelation rel : relations) {
      if (rel == null || rel.getUserId() == null || rel.getOrgUnitId() == null) continue;
      if (subtreeIds.contains(rel.getOrgUnitId())) {
        userIds.add(rel.getUserId());
      }
    }
    return userIds.size();
  }
}
