package elexvx.admin.service;

import elexvx.admin.dao.AuthQueryDao;
import elexvx.admin.dto.UserCreateRequest;
import elexvx.admin.dto.UserGuidPair;
import elexvx.admin.dto.UserIdLongValue;
import elexvx.admin.dto.UserIdStringValue;
import elexvx.admin.dto.UserUpdateRequest;
import elexvx.admin.entity.UserEntity;
import elexvx.admin.exception.BusinessException;
import elexvx.admin.exception.ErrorCodes;
import elexvx.admin.mapper.OrgUnitMapper;
import elexvx.admin.mapper.RoleMapper;
import elexvx.admin.mapper.UserDepartmentMapper;
import elexvx.admin.mapper.UserMapper;
import elexvx.admin.mapper.UserOrgUnitMapper;
import elexvx.admin.security.AuthContext;
import elexvx.admin.util.SensitiveMaskUtil;
import elexvx.admin.vo.PageResult;
import elexvx.admin.vo.UserListItem;
import elexvx.admin.annotation.AiFunction;
import jakarta.annotation.PostConstruct;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

@Service
public class UserAdminService {
  private static final String ROOT_ADMIN_ACCOUNT = "admin";
  private static final int GUID_BACKFILL_BATCH_SIZE = 500;
  private static final String DOC_TYPE_RESIDENT_ID_CARD = "resident_id_card";
  private static final String DOC_TYPE_PASSPORT = "passport";
  private static final java.util.Set<String> DOC_TYPE_RESIDENT_ID_CARD_ALIASES =
    java.util.Set.of("resident_id_card", "id_card", "identity_card", "china_id_card", "居民身份证");
  private static final java.util.Set<String> DOC_TYPE_PASSPORT_ALIASES = java.util.Set.of("passport", "护照");
  private static final int[] RESIDENT_ID_CARD_WEIGHTS = { 7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2 };
  private static final char[] RESIDENT_ID_CARD_CHECKSUM_CODES = { '1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2' };
  private final UserMapper userMapper;
  private final RoleMapper roleMapper;
  private final AuthQueryDao authDao;
  private final OrgUnitMapper orgUnitMapper;
  private final UserOrgUnitMapper userOrgUnitMapper;
  private final UserDepartmentMapper userDepartmentMapper;
  private final OperationLogService operationLogService;
  private final PermissionFacade permissionFacade;
  private final PasswordPolicyService passwordPolicyService;
  private final AuthContext authContext;
  private final AuthTokenService authTokenService;
  private final boolean guidBackfillOnStartup;

  public UserAdminService(
    UserMapper userMapper,
    RoleMapper roleMapper,
    AuthQueryDao authDao,
    OrgUnitMapper orgUnitMapper,
    UserOrgUnitMapper userOrgUnitMapper,
    UserDepartmentMapper userDepartmentMapper,
    OperationLogService operationLogService,
    PermissionFacade permissionFacade,
    PasswordPolicyService passwordPolicyService,
    AuthContext authContext,
    AuthTokenService authTokenService,
    @Value("${elexvx.user.guid.backfill-on-startup:false}") boolean guidBackfillOnStartup
  ) {
    this.userMapper = userMapper;
    this.roleMapper = roleMapper;
    this.authDao = authDao;
    this.orgUnitMapper = orgUnitMapper;
    this.userOrgUnitMapper = userOrgUnitMapper;
    this.userDepartmentMapper = userDepartmentMapper;
    this.operationLogService = operationLogService;
    this.permissionFacade = permissionFacade;
    this.passwordPolicyService = passwordPolicyService;
    this.authContext = authContext;
    this.authTokenService = authTokenService;
    this.guidBackfillOnStartup = guidBackfillOnStartup;
  }

  private static BusinessException badRequest(String message) {
    return new BusinessException(ErrorCodes.BAD_REQUEST, message);
  }

  @PostConstruct
  public void backfillMissingGuidsOnStartup() {
    if (!guidBackfillOnStartup) return;
    backfillMissingGuids();
  }

  @Transactional
  public int backfillMissingGuids() {
    int updated = 0;
    int offset = 0;
    while (true) {
      List<UserEntity> rows = userMapper.selectMissingGuidUsers(offset, GUID_BACKFILL_BATCH_SIZE);
      if (rows == null || rows.isEmpty()) {
        break;
      }
      List<UserGuidPair> pairs = new ArrayList<>();
      for (UserEntity user : rows) {
        if (user == null || user.getId() == null) continue;
        UserGuidPair pair = new UserGuidPair();
        pair.setId(user.getId());
        pair.setGuid(java.util.UUID.randomUUID().toString());
        pairs.add(pair);
      }
      if (!pairs.isEmpty()) {
        updated += userMapper.updateGuidBatch(pairs);
      }
      if (rows.size() < GUID_BACKFILL_BATCH_SIZE) {
        break;
      }
      offset += GUID_BACKFILL_BATCH_SIZE;
    }
    return updated;
  }


  @AiFunction(
    name = "queryUser",
    description = "查询用户列表，支持按账号或姓名模糊搜索",
    requiredPermissions = { "system:SystemUser:query" }
  )
  public PageResult<UserListItem> page(
    String keyword,
    String mobile,
    Long orgUnitId,
    Long departmentId,
    Integer status,
    java.time.LocalDateTime startTime,
    java.time.LocalDateTime endTime,
    int page,
    int size
  ) {
    int safePage = Math.max(page, 0);
    int safeSize = Math.min(Math.max(size, 1), 200);
    int offset = safePage * safeSize;
    String kw = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
    String mobileKeyword = (mobile == null || mobile.isBlank()) ? null : mobile.trim();
    List<UserEntity> rows =
      userMapper.selectPage(kw, mobileKeyword, orgUnitId, departmentId, status, startTime, endTime, offset, safeSize);
    long total = userMapper.countByKeyword(kw, mobileKeyword, orgUnitId, departmentId, status, startTime, endTime);
    List<Long> userIds = rows.stream().map(UserEntity::getId).filter(Objects::nonNull).toList();
    UserRelatedData relatedData = loadUserRelatedData(userIds);

    List<UserListItem> list = new ArrayList<>();
    for (UserEntity u : rows) {
      UserListItem item = toListItem(u);
      Long userId = u.getId();
      item.setRoles(relatedData.roles().getOrDefault(userId, List.of()));
      item.setOrgUnitNames(relatedData.orgUnitNames().getOrDefault(userId, List.of()));
      item.setOrgUnitIds(relatedData.orgUnitIds().getOrDefault(userId, List.of()));
      item.setDepartmentNames(relatedData.departmentNames().getOrDefault(userId, List.of()));
      item.setDepartmentIds(relatedData.departmentIds().getOrDefault(userId, List.of()));
      list.add(item);
    }
    return new PageResult<>(list, total);
  }

  @AiFunction(
    name = "getUserDetail",
    description = "获取特定用户的详细信息",
    requiredPermissions = { "system:SystemUser:query" }
  )
  public UserListItem get(long id) {
    UserEntity u = Optional.ofNullable(userMapper.selectById(id)).orElseThrow(() -> badRequest("用户不存在"));
    UserRelatedData relatedData = loadUserRelatedData(List.of(id));
    UserListItem item = toListItem(u);
    item.setRoles(relatedData.roles().getOrDefault(id, List.of()));
    item.setOrgUnitNames(relatedData.orgUnitNames().getOrDefault(id, List.of()));
    item.setOrgUnitIds(relatedData.orgUnitIds().getOrDefault(id, List.of()));
    item.setDepartmentNames(relatedData.departmentNames().getOrDefault(id, List.of()));
    item.setDepartmentIds(relatedData.departmentIds().getOrDefault(id, List.of()));
    return item;
  }

  @Transactional
  public UserListItem create(UserCreateRequest req) {
    if (userMapper.countByAccount(req.getAccount()) > 0) {
      throw badRequest("账号已存在");
    }
    UserEntity u = new UserEntity();
    u.setAccount(req.getAccount());
    u.setGuid(java.util.UUID.randomUUID().toString());
    u.setName(req.getName());
    u.setNickname(req.getNickname());
    u.setGender(req.getGender());
    u.setMobile(req.getMobile());
    u.setEmail(req.getEmail());
    u.setIdType(req.getIdType());
    u.setIdCard(req.getIdCard());
    u.setIdValidFrom(req.getIdValidFrom());
    u.setIdValidTo(req.getIdValidTo());
    u.setSeat(req.getSeat());
    u.setEntity(req.getEntity());
    u.setLeader(req.getLeader());
    u.setPosition(req.getPosition());
    u.setJoinDay(req.getJoinDay());
    u.setTeam(req.getTeam());
    u.setProvinceId(req.getProvinceId());
    u.setProvince(req.getProvince());
    u.setCityId(req.getCityId());
    u.setCity(req.getCity());
    u.setDistrictId(req.getDistrictId());
    u.setDistrict(req.getDistrict());
    u.setZipCode(req.getZipCode());
    u.setAddress(req.getAddress());
    u.setStatus(req.getStatus() == null ? 1 : req.getStatus());

    validateDocumentInfo(u);

    String pwd = (req.getPassword() == null || req.getPassword().isBlank()) ? "123456" : req.getPassword();
    // 创建账号时也使用统一密码规范校验
    passwordPolicyService.validate(pwd);
    u.setPasswordHash(BCrypt.hashpw(pwd, BCrypt.gensalt()));
    u = saveUser(u);

    List<String> normalizedRoles = normalizeRoles(req.getRoles());
    List<String> roles = normalizedRoles.isEmpty() ? List.of("user") : normalizedRoles;
    ensureRolesExist(roles);
    authDao.replaceUserRoles(u.getId(), roles);
    validateOrgDepartmentSelection(req.getOrgUnitIds(), req.getDepartmentIds());
    replaceOrgUnits(u.getId(), req.getOrgUnitIds());
    replaceDepartments(u.getId(), req.getDepartmentIds());

    operationLogService.log("CREATE", "用户管理", "创建用户: " + u.getAccount());
    return get(u.getId());
  }

  @Transactional
  public UserListItem update(long id, UserUpdateRequest req) {
    UserEntity u = Optional.ofNullable(userMapper.selectById(id)).orElseThrow(() -> badRequest("用户不存在"));
    ensureManageableTarget(u, true);
    List<String> originalRoles = req.getRoles() != null ? authDao.findRoleNamesByUserId(id) : List.of();
    boolean documentFieldsTouched = false;
    if (req.getAccount() != null) {
      String nextAccount = req.getAccount().trim();
      if (nextAccount.isEmpty()) {
        throw badRequest("账号不能为空");
      }
      if (!nextAccount.matches("^[a-zA-Z0-9_@.-]+$")) {
        throw badRequest("账号包含非法字符");
      }
      if (!nextAccount.equals(u.getAccount())) {
        UserEntity existing = userMapper.selectByAccount(nextAccount);
        if (existing != null && !Objects.equals(existing.getId(), u.getId())) {
          throw badRequest("账号已存在");
        }
      }
      u.setAccount(nextAccount);
    }
    if (req.getName() != null) u.setName(req.getName());
    if (req.getNickname() != null) u.setNickname(req.getNickname());
    if (req.getGender() != null) u.setGender(req.getGender());
    if (req.getMobile() != null && !SensitiveMaskUtil.isMasked(req.getMobile())) u.setMobile(req.getMobile());
    if (req.getEmail() != null && !SensitiveMaskUtil.isMasked(req.getEmail())) u.setEmail(req.getEmail());
    if (req.getIdCard() != null && !SensitiveMaskUtil.isMasked(req.getIdCard())) {
      u.setIdCard(req.getIdCard());
      documentFieldsTouched = true;
    }
    if (req.getIdType() != null) {
      u.setIdType(req.getIdType());
      documentFieldsTouched = true;
    }
    if (req.getIdValidFrom() != null) {
      u.setIdValidFrom(req.getIdValidFrom());
      documentFieldsTouched = true;
    }
    if (req.getIdValidTo() != null) {
      u.setIdValidTo(req.getIdValidTo());
      documentFieldsTouched = true;
    }
    if (req.getSeat() != null) u.setSeat(req.getSeat());
    if (req.getEntity() != null) u.setEntity(req.getEntity());
    if (req.getLeader() != null) u.setLeader(req.getLeader());
    if (req.getPosition() != null) u.setPosition(req.getPosition());
    if (req.getJoinDay() != null) u.setJoinDay(req.getJoinDay());
    if (req.getTeam() != null) u.setTeam(req.getTeam());
    if (req.getProvinceId() != null) u.setProvinceId(req.getProvinceId());
    if (req.getProvince() != null) u.setProvince(req.getProvince());
    if (req.getCityId() != null) u.setCityId(req.getCityId());
    if (req.getCity() != null) u.setCity(req.getCity());
    if (req.getDistrictId() != null) u.setDistrictId(req.getDistrictId());
    if (req.getDistrict() != null) u.setDistrict(req.getDistrict());
    if (req.getZipCode() != null) u.setZipCode(req.getZipCode());
    if (req.getAddress() != null) u.setAddress(req.getAddress());
    if (req.getStatus() != null) u.setStatus(req.getStatus());
    if (documentFieldsTouched) {
      validateDocumentInfo(u);
    }
    ensureGuid(u);
    saveUser(u);

    if (req.getRoles() != null) {
      List<String> roles = normalizeRoles(req.getRoles());
      if (roles.isEmpty()) {
        throw badRequest("用户必须至少拥有一个角色");
      }
      ensureRolesExist(roles);
      authDao.replaceUserRoles(id, roles);
      if (hasRoleChanged(originalRoles, roles)) {
        long currentUserId = authContext.getUserIdOrDefault(-1L);
        if (currentUserId != id) {
          authTokenService.removeUserTokens(id);
        }
      }
    }
    if (req.getOrgUnitIds() != null || req.getDepartmentIds() != null) {
      List<Long> nextOrgUnitIds = req.getOrgUnitIds() != null ? req.getOrgUnitIds() : orgUnitMapper.selectIdsByUserId(id);
      List<Long> nextDepartmentIds =
        req.getDepartmentIds() != null ? req.getDepartmentIds() : userDepartmentMapper.selectDepartmentIdsByUserId(id);
      validateOrgDepartmentSelection(nextOrgUnitIds, nextDepartmentIds);
    }
    if (req.getOrgUnitIds() != null) {
      replaceOrgUnits(id, req.getOrgUnitIds());
    }
    if (req.getDepartmentIds() != null) {
      replaceDepartments(id, req.getDepartmentIds());
    }
    operationLogService.log("UPDATE", "用户管理", "更新用户: " + u.getAccount());
    return get(id);
  }

  @Transactional
  public boolean delete(long id) {
    UserEntity u = userMapper.selectById(id);
    if (u == null) return true;
    ensureManageableTarget(u);
    authDao.replaceUserRoles(id, List.of());
    userOrgUnitMapper.deleteByUserId(id);
    userDepartmentMapper.deleteByUserId(id);
    authTokenService.removeUserTokens(id);
    userMapper.deleteById(id);
    operationLogService.log("DELETE", "用户管理", "删除用户: " + u.getAccount());
    return true;
  }

  @Transactional
  public boolean resetPassword(long id, String newPassword) {
    UserEntity u = Optional.ofNullable(userMapper.selectById(id)).orElseThrow(() -> badRequest("用户不存在"));
    ensureManageableTarget(u);
    String pwd = (newPassword == null || newPassword.isBlank()) ? "123456" : newPassword;
    // 重置密码也遵循统一的密码规范
    passwordPolicyService.validate(pwd);
    u.setPasswordHash(BCrypt.hashpw(pwd, BCrypt.gensalt()));
    saveUser(u);
    operationLogService.log("UPDATE", "用户管理", "重置密码: " + u.getAccount());
    return true;
  }

  private String normalizeIdCard(String idCard) {
    if (idCard == null) return "";
    return idCard.trim().toUpperCase();
  }

  private String normalizeDocumentType(String idType) {
    if (idType == null) return null;
    String normalized = idType.trim();
    if (normalized.isEmpty()) return null;
    String lower = normalized.toLowerCase(Locale.ROOT);
    if (DOC_TYPE_RESIDENT_ID_CARD_ALIASES.contains(lower) || DOC_TYPE_RESIDENT_ID_CARD_ALIASES.contains(normalized)) {
      return DOC_TYPE_RESIDENT_ID_CARD;
    }
    if (DOC_TYPE_PASSPORT_ALIASES.contains(lower) || DOC_TYPE_PASSPORT_ALIASES.contains(normalized)) {
      return DOC_TYPE_PASSPORT;
    }
    return lower;
  }

  private void validateDocumentInfo(UserEntity user) {
    String idType = normalizeDocumentType(user.getIdType());
    String idCard = normalizeIdCard(user.getIdCard());
    user.setIdType(idType);
    user.setIdCard(idCard.isBlank() ? null : idCard);

    if (idType == null && !idCard.isBlank()) {
      throw badRequest("证件号码已填写，请先选择证件类型");
    }

    if (!idCard.isBlank()) {
      switch (idType) {
        case DOC_TYPE_RESIDENT_ID_CARD -> validateResidentIdCard(idCard);
        case DOC_TYPE_PASSPORT -> validatePassport(idCard);
        default -> throw badRequest("不支持的证件类型: " + idType);
      }
    }

    LocalDate validFrom = user.getIdValidFrom();
    LocalDate validTo = user.getIdValidTo();
    if (validFrom != null && validTo != null && validTo.isBefore(validFrom)) {
      throw badRequest("证件有效期止不能早于证件有效期起");
    }
  }

  private void validateResidentIdCard(String idCard) {
    if (!idCard.matches("^[1-9]\\d{16}[0-9X]$")) {
      throw badRequest("居民身份证号码格式不正确");
    }
    String birth = idCard.substring(6, 14);
    try {
      LocalDate.parse(birth, java.time.format.DateTimeFormatter.BASIC_ISO_DATE);
    } catch (Exception ex) {
      throw badRequest("居民身份证号码中的出生日期不合法");
    }
    if (!isValidResidentIdCardChecksum(idCard)) {
      throw badRequest("居民身份证号码校验位不正确");
    }
  }

  private boolean isValidResidentIdCardChecksum(String idCard) {
    int sum = 0;
    for (int i = 0; i < 17; i++) {
      char ch = idCard.charAt(i);
      if (ch < '0' || ch > '9') return false;
      sum += (ch - '0') * RESIDENT_ID_CARD_WEIGHTS[i];
    }
    char expected = RESIDENT_ID_CARD_CHECKSUM_CODES[sum % 11];
    return expected == idCard.charAt(17);
  }

  private void validatePassport(String passportNo) {
    if (!passportNo.matches("^[A-Z0-9]{5,17}$")) {
      throw badRequest("护照号码格式不正确");
    }
  }

  private List<String> normalizeRoles(List<String> roles) {
    if (roles == null) return List.of();
    List<String> normalized = new ArrayList<>();
    for (String role : roles) {
      if (role == null) continue;
      String value = role.trim();
      if (!value.isEmpty()) normalized.add(value);
    }
    return normalized;
  }

  private boolean hasRoleChanged(List<String> before, List<String> after) {
    if (before == null && after == null) return false;
    if (before == null) return !after.isEmpty();
    if (after == null) return !before.isEmpty();
    return !new java.util.HashSet<>(before).equals(new java.util.HashSet<>(after));
  }

  private void ensureRolesExist(List<String> roles) {
    for (String role : roles) {
      if (role == null || role.isBlank()) continue;
      if (roleMapper.countByName(role) == 0) {
        throw badRequest("角色不存在: " + role);
      }
    }
  }

  static void validateManageableTarget(
    long currentUserId,
    long targetUserId,
    String targetAccount,
    boolean targetIsAdminRole,
    boolean currentIsAdminRole,
    boolean allowRootAdmin
  ) {
    if (!allowRootAdmin && targetAccount != null && targetAccount.equalsIgnoreCase(ROOT_ADMIN_ACCOUNT)) {
      throw badRequest("禁止操作系统管理员账号");
    }
    if (currentUserId == targetUserId) return;
    if (targetIsAdminRole && !currentIsAdminRole) {
      throw badRequest("禁止操作管理员账号");
    }
  }

  private void ensureManageableTarget(UserEntity targetUser, boolean allowRootAdmin) {
    long currentUserId = authContext.requireUserId();
    long targetUserId = Objects.requireNonNull(targetUser.getId());
    boolean targetIsAdmin = permissionFacade.isAdminAccount(targetUserId);
    boolean currentIsAdmin = permissionFacade.isAdminAccount(currentUserId);
    validateManageableTarget(currentUserId, targetUserId, targetUser.getAccount(), targetIsAdmin, currentIsAdmin, allowRootAdmin);
  }

  private void ensureManageableTarget(UserEntity targetUser) {
    ensureManageableTarget(targetUser, false);
  }

  private UserListItem toListItem(UserEntity u) {
    UserListItem item = new UserListItem();
    item.setId(u.getId());
    item.setGuid(u.getGuid());
    item.setAccount(u.getAccount());
    item.setName(u.getName());
    item.setNickname(u.getNickname());
    item.setGender(u.getGender());
    item.setMobile(u.getMobile());
    item.setEmail(u.getEmail());
    item.setIdType(u.getIdType());
    item.setIdCard(u.getIdCard());
    item.setIdValidFrom(u.getIdValidFrom());
    item.setIdValidTo(u.getIdValidTo());
    item.setSeat(u.getSeat());
    item.setEntity(u.getEntity());
    item.setLeader(u.getLeader());
    item.setPosition(u.getPosition());
    item.setJoinDay(u.getJoinDay());
    item.setTeam(u.getTeam());
    item.setProvinceId(u.getProvinceId());
    item.setProvince(u.getProvince());
    item.setCityId(u.getCityId());
    item.setCity(u.getCity());
    item.setDistrictId(u.getDistrictId());
    item.setDistrict(u.getDistrict());
    item.setZipCode(u.getZipCode());
    item.setAddress(u.getAddress());
    item.setStatus(u.getStatus());
    item.setCreatedAt(u.getCreatedAt());
    return item;
  }

  private UserRelatedData loadUserRelatedData(List<Long> userIds) {
    if (userIds == null || userIds.isEmpty()) {
      return new UserRelatedData(java.util.Map.of(), java.util.Map.of(), java.util.Map.of(), java.util.Map.of(), java.util.Map.of());
    }
    java.util.Map<Long, List<String>> roles = groupStringValues(authDao.findRoleNamesByUserIds(userIds));
    java.util.Map<Long, List<String>> orgUnitNames = groupStringValues(orgUnitMapper.selectNamesByUserIds(userIds));
    java.util.Map<Long, List<Long>> orgUnitIds = groupLongValues(orgUnitMapper.selectIdsByUserIds(userIds));
    java.util.Map<Long, List<String>> departmentNames = groupStringValues(userDepartmentMapper.selectDepartmentNamesByUserIds(userIds));
    java.util.Map<Long, List<Long>> departmentIds = groupLongValues(userDepartmentMapper.selectDepartmentIdsByUserIds(userIds));
    return new UserRelatedData(roles, orgUnitNames, orgUnitIds, departmentNames, departmentIds);
  }

  private java.util.Map<Long, List<String>> groupStringValues(List<UserIdStringValue> rows) {
    java.util.Map<Long, List<String>> result = new java.util.LinkedHashMap<>();
    if (rows == null) return result;
    for (UserIdStringValue row : rows) {
      if (row == null || row.getUserId() == null || row.getValue() == null) continue;
      result.computeIfAbsent(row.getUserId(), key -> new ArrayList<>()).add(row.getValue());
    }
    return result;
  }

  private java.util.Map<Long, List<Long>> groupLongValues(List<UserIdLongValue> rows) {
    java.util.Map<Long, List<Long>> result = new java.util.LinkedHashMap<>();
    if (rows == null) return result;
    for (UserIdLongValue row : rows) {
      if (row == null || row.getUserId() == null || row.getValue() == null) continue;
      result.computeIfAbsent(row.getUserId(), key -> new ArrayList<>()).add(row.getValue());
    }
    return result;
  }

  private void validateOrgDepartmentSelection(List<Long> orgUnitIds, List<Long> departmentIds) {
    boolean hasOrg = orgUnitIds != null && orgUnitIds.stream().anyMatch(Objects::nonNull);
    boolean hasDepartment = departmentIds != null && departmentIds.stream().anyMatch(Objects::nonNull);
    if (hasOrg && hasDepartment) {
      throw badRequest("已选择机构时不能选择部门");
    }
  }

  private void replaceOrgUnits(Long userId, List<Long> orgUnitIds) {
    userOrgUnitMapper.deleteByUserId(userId);
    if (orgUnitIds == null || orgUnitIds.isEmpty()) return;
    List<Long> cleaned = orgUnitIds.stream().filter(Objects::nonNull).distinct().toList();
    if (!cleaned.isEmpty()) {
      userOrgUnitMapper.insertUserOrgUnits(userId, cleaned);
    }
  }

  private void replaceDepartments(Long userId, List<Long> departmentIds) {
    userDepartmentMapper.deleteByUserId(userId);
    if (departmentIds == null || departmentIds.isEmpty()) return;
    List<Long> cleaned = departmentIds.stream().filter(Objects::nonNull).distinct().toList();
    if (!cleaned.isEmpty()) {
      userDepartmentMapper.insertUserDepartments(userId, cleaned);
    }
  }

  private UserEntity saveUser(UserEntity user) {
    if (user.getId() == null) {
      userMapper.insert(user);
    } else {
      userMapper.update(user);
    }
    return user;
  }

  private record UserRelatedData(
    java.util.Map<Long, List<String>> roles,
    java.util.Map<Long, List<String>> orgUnitNames,
    java.util.Map<Long, List<Long>> orgUnitIds,
    java.util.Map<Long, List<String>> departmentNames,
    java.util.Map<Long, List<Long>> departmentIds
  ) {}

  private boolean ensureGuid(UserEntity user) {
    if (user.getGuid() != null && !user.getGuid().isBlank()) return false;
    user.setGuid(java.util.UUID.randomUUID().toString());
    return true;
  }
}
