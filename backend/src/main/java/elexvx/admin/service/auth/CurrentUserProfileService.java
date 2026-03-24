package elexvx.admin.service.auth;

import elexvx.admin.dto.RoleSwitchRequest;
import elexvx.admin.dto.UserProfileUpdateRequest;
import elexvx.admin.entity.UserEntity;
import elexvx.admin.exception.BusinessException;
import elexvx.admin.exception.ErrorCodes;
import elexvx.admin.mapper.OrgUnitMapper;
import elexvx.admin.mapper.RoleMapper;
import elexvx.admin.mapper.UserMapper;
import elexvx.admin.security.AuthContext;
import elexvx.admin.security.AuthSession;
import elexvx.admin.service.AuthTokenService;
import elexvx.admin.service.AuthValidationService;
import elexvx.admin.service.ObjectStorageService;
import elexvx.admin.service.OperationLogService;
import elexvx.admin.service.PermissionFacade;
import elexvx.admin.util.SensitiveMaskUtil;
import elexvx.admin.vo.UserInfoResponse;
import elexvx.admin.vo.UserProfileResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CurrentUserProfileService {
  private static final Logger log = LoggerFactory.getLogger(CurrentUserProfileService.class);

  private final AuthContext authContext;
  private final UserMapper userMapper;
  private final OrgUnitMapper orgUnitMapper;
  private final PermissionFacade permissionFacade;
  private final RoleMapper roleMapper;
  private final AuthValidationService authValidationService;
  private final AuthTokenService authTokenService;
  private final ObjectStorageService storageService;
  private final OperationLogService operationLogService;

  public CurrentUserProfileService(
    AuthContext authContext,
    UserMapper userMapper,
    OrgUnitMapper orgUnitMapper,
    PermissionFacade permissionFacade,
    RoleMapper roleMapper,
    AuthValidationService authValidationService,
    AuthTokenService authTokenService,
    ObjectStorageService storageService,
    OperationLogService operationLogService
  ) {
    this.authContext = authContext;
    this.userMapper = userMapper;
    this.orgUnitMapper = orgUnitMapper;
    this.permissionFacade = permissionFacade;
    this.roleMapper = roleMapper;
    this.authValidationService = authValidationService;
    this.authTokenService = authTokenService;
    this.storageService = storageService;
    this.operationLogService = operationLogService;
  }

  public UserInfoResponse currentUserInfo() {
    long userId = authContext.requireUserId();
    UserEntity user = Optional.ofNullable(userMapper.selectById(userId)).orElseThrow(() -> badRequest("User not found"));
    ensureUserGuid(user);
    saveUser(user);
    List<String> roles = permissionFacade.getEffectiveRoles(userId);
    List<String> perms = permissionFacade.getEffectivePermissions(userId);
    UserInfoResponse resp = new UserInfoResponse(user.getName(), normalizeAvatar(user.getAvatar()), roles, perms);
    resp.setId(user.getId());
    resp.setGuid(user.getGuid());
    resp.setAssignedRoles(permissionFacade.getAssignedRoles(userId));
    resp.setRoleSimulated(permissionFacade.isAssumed(userId));
    resp.setOrgUnitNames(orgUnitMapper.selectNamesByUserId(userId));
    return resp;
  }

  public UserProfileResponse currentUserProfile() {
    long userId = authContext.requireUserId();
    UserEntity user = Optional.ofNullable(userMapper.selectById(userId)).orElseThrow(() -> badRequest("User not found"));
    return enrichProfile(toProfile(user), userId);
  }

  @Transactional
  public UserProfileResponse updateCurrentUserProfile(UserProfileUpdateRequest req) {
    long userId = authContext.requireUserId();
    UserEntity user = Optional.ofNullable(userMapper.selectById(userId)).orElseThrow(() -> badRequest("User not found"));
    String oldAvatar = user.getAvatar();
    boolean documentFieldsTouched = false;
    if (req.getName() != null) user.setName(req.getName());
    if (req.getMobile() != null && !SensitiveMaskUtil.isMasked(req.getMobile())) user.setMobile(req.getMobile());
    if (req.getEmail() != null && !SensitiveMaskUtil.isMasked(req.getEmail())) user.setEmail(req.getEmail());
    if (req.getIdCard() != null && !SensitiveMaskUtil.isMasked(req.getIdCard())) {
      user.setIdCard(req.getIdCard());
      documentFieldsTouched = true;
    }
    if (req.getIdType() != null) {
      user.setIdType(req.getIdType());
      documentFieldsTouched = true;
    }
    if (req.getIdValidFrom() != null) {
      user.setIdValidFrom(req.getIdValidFrom());
      documentFieldsTouched = true;
    }
    if (req.getIdValidTo() != null) {
      user.setIdValidTo(req.getIdValidTo());
      documentFieldsTouched = true;
    }
    if (req.getSeat() != null) user.setSeat(req.getSeat());
    if (req.getEntity() != null) user.setEntity(req.getEntity());
    if (req.getLeader() != null) user.setLeader(req.getLeader());
    if (req.getPosition() != null) user.setPosition(req.getPosition());
    if (req.getJoinDay() != null) user.setJoinDay(req.getJoinDay());
    if (req.getTeam() != null) user.setTeam(req.getTeam());
    if (req.getGender() != null) user.setGender(req.getGender());
    if (req.getNickname() != null) user.setNickname(req.getNickname());
    if (req.getProvinceId() != null) user.setProvinceId(req.getProvinceId());
    if (req.getProvince() != null) user.setProvince(req.getProvince());
    if (req.getCityId() != null) user.setCityId(req.getCityId());
    if (req.getCity() != null) user.setCity(req.getCity());
    if (req.getDistrictId() != null) user.setDistrictId(req.getDistrictId());
    if (req.getDistrict() != null) user.setDistrict(req.getDistrict());
    if (req.getTownId() != null) user.setTownId(req.getTownId());
    if (req.getTown() != null) user.setTown(req.getTown());
    if (req.getStreetId() != null) user.setStreetId(req.getStreetId());
    if (req.getStreet() != null) user.setStreet(req.getStreet());
    if (req.getZipCode() != null) user.setZipCode(req.getZipCode());
    if (req.getAddress() != null) user.setAddress(req.getAddress());
    if (req.getIntroduction() != null) user.setIntroduction(req.getIntroduction());
    if (req.getAvatar() != null) user.setAvatar(req.getAvatar());
    if (req.getTags() != null) user.setTags(req.getTags());
    if (documentFieldsTouched) {
      authValidationService.validateDocumentInfo(user);
    }
    ensureUserGuid(user);
    saveUser(user);
    updateSessionUserName(user.getName());
    if (req.getAvatar() != null && oldAvatar != null && !oldAvatar.equals(req.getAvatar())) {
      deleteUploadFile(oldAvatar);
    }
    operationLogService.log("UPDATE", "profile", "update profile");
    return enrichProfile(toProfile(user), userId);
  }

  @Transactional
  public UserInfoResponse switchRoles(RoleSwitchRequest req) {
    long userId = authContext.requireUserId();
    if (!permissionFacade.isAdminAccount(userId)) {
      throw badRequest("仅管理员可切换演示角色");
    }
    if (req.getRoles() == null || req.getRoles().isEmpty()) {
      permissionFacade.clearAssumedRoles(userId);
      return currentUserInfo();
    }
    permissionFacade.assumeRoles(userId, req.getRoles());
    return currentUserInfo();
  }

  public List<String> listAllRoleNames() {
    long userId = authContext.requireUserId();
    if (!permissionFacade.isAdminAccount(userId)) {
      throw badRequest("仅管理员可查看角色");
    }
    return roleMapper.selectAll().stream().map(r -> r.getName()).toList();
  }

  private UserProfileResponse enrichProfile(UserProfileResponse profile, long userId) {
    profile.setRoles(permissionFacade.getEffectiveRoles(userId));
    profile.setOrgUnitNames(orgUnitMapper.selectNamesByUserId(userId));
    return profile;
  }

  private UserProfileResponse toProfile(UserEntity user) {
    UserProfileResponse resp = new UserProfileResponse();
    resp.setId(user.getId());
    resp.setAccount(user.getAccount());
    resp.setName(user.getName());
    resp.setMobile(user.getMobile());
    resp.setEmail(user.getEmail());
    resp.setIdCard(user.getIdCard());
    resp.setIdType(user.getIdType());
    resp.setIdValidFrom(user.getIdValidFrom());
    resp.setIdValidTo(user.getIdValidTo());
    resp.setSeat(user.getSeat());
    resp.setEntity(user.getEntity());
    resp.setLeader(user.getLeader());
    resp.setPosition(user.getPosition());
    resp.setJoinDay(user.getJoinDay());
    resp.setTeam(user.getTeam());
    resp.setGender(user.getGender());
    resp.setNickname(user.getNickname());
    resp.setProvinceId(user.getProvinceId());
    resp.setProvince(user.getProvince());
    resp.setCityId(user.getCityId());
    resp.setCity(user.getCity());
    resp.setDistrictId(user.getDistrictId());
    resp.setDistrict(user.getDistrict());
    resp.setTownId(user.getTownId());
    resp.setTown(user.getTown());
    resp.setStreetId(user.getStreetId());
    resp.setStreet(user.getStreet());
    resp.setZipCode(user.getZipCode());
    resp.setAddress(user.getAddress());
    resp.setIntroduction(user.getIntroduction());
    resp.setAvatar(normalizeAvatar(user.getAvatar()));
    resp.setTags(user.getTags());
    authValidationService.fillProfileCompleteness(resp);
    return resp;
  }

  private UserEntity saveUser(UserEntity user) {
    if (user.getId() == null) {
      userMapper.insert(user);
    } else {
      userMapper.update(user);
    }
    return user;
  }

  private void ensureUserGuid(UserEntity user) {
    if (user.getGuid() == null || user.getGuid().isBlank()) {
      user.setGuid(UUID.randomUUID().toString());
    }
  }

  private void deleteUploadFile(String url) {
    try {
      storageService.deleteByUrl(url);
    } catch (Exception ex) {
      log.warn("删除历史头像失败，继续执行资料更新，url={}", url, ex);
    }
  }

  private String normalizeAvatar(String url) {
    if (url == null || url.isBlank()) return null;
    String original = url.trim();
    if (original.startsWith("http://") || original.startsWith("https://")) return original;
    String clean = original;
    if (clean.startsWith("/api/")) clean = clean.substring(4);
    String pathOnly = clean;
    int queryIndex = pathOnly.indexOf('?');
    if (queryIndex >= 0) pathOnly = pathOnly.substring(0, queryIndex);
    int fragmentIndex = pathOnly.indexOf('#');
    if (fragmentIndex >= 0) pathOnly = pathOnly.substring(0, fragmentIndex);
    if (!pathOnly.startsWith("/uploads/")) return original;
    String relative = pathOnly.substring("/uploads/".length());
    if (relative.isEmpty()) return null;
    try {
      Path root = Paths.get(System.getProperty("user.dir"), "uploads").toAbsolutePath().normalize();
      Path target = root.resolve(relative).normalize();
      if (!target.startsWith(root)) return null;
      if (!Files.exists(target)) return null;
      return original;
    } catch (Exception ex) {
      return original;
    }
  }

  private void updateSessionUserName(String userName) {
    String token = authContext.getToken();
    if (token == null) return;
    AuthSession session = authTokenService.getSession(token);
    if (session == null) return;
    session.getAttributes().put("userName", userName);
    authTokenService.updateSession(token, session);
  }

  private static BusinessException badRequest(String message) {
    return new BusinessException(ErrorCodes.BAD_REQUEST, message);
  }
}
