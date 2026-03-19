package elexvx.admin.service;

import elexvx.admin.entity.AreaEntity;
import elexvx.admin.exception.BusinessException;
import elexvx.admin.exception.ErrorCodes;
import elexvx.admin.mapper.AreaMapper;
import elexvx.admin.vo.AreaNodeResponse;
import elexvx.admin.vo.AreaNodeRow;
import elexvx.admin.vo.AreaPathNode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AreaService {
  private static final Logger log = LoggerFactory.getLogger(AreaService.class);
  private static final String AREA_DATA_NOT_INITIALIZED_MESSAGE = "请先导入 database/tdesign_init.sql 或对应数据库的初始化脚本。";

  private static BusinessException badRequest(String message) {
    return new BusinessException(ErrorCodes.BAD_REQUEST, message);
  }
  private final AreaMapper areaMapper;

  public AreaService(AreaMapper areaMapper) {
    this.areaMapper = areaMapper;
  }

  public List<AreaNodeResponse> listChildren(Integer parentId) {
    Integer safeParentId = parentId == null ? 0 : parentId;
    List<AreaNodeRow> rows;
    try {
      rows = areaMapper.selectChildren(safeParentId);
    } catch (Exception e) {
      log.error("Load area children failed, parentId={}", safeParentId, e);
      throw badRequest(AREA_DATA_NOT_INITIALIZED_MESSAGE);
    }
    List<AreaNodeResponse> result = new ArrayList<>();
    for (AreaNodeRow row : rows) {
      AreaNodeResponse node = new AreaNodeResponse();
      node.setId(row.getId());
      node.setName(row.getName());
      node.setLevel(row.getLevel());
      node.setZipCode(row.getZipCode());
      node.setHasChildren(row.getChildrenCount() != null && row.getChildrenCount() > 0);
      result.add(node);
    }
    return result;
  }

  public List<AreaPathNode> getPath(Integer areaId) {
    if (areaId == null || areaId <= 0) return Collections.emptyList();
    List<AreaPathNode> path = new ArrayList<>();
    Integer cursor = areaId;
    int guard = 0;
    while (cursor != null && cursor > 0 && guard < 10) {
      AreaEntity entity;
      try {
        entity = areaMapper.selectById(cursor);
      } catch (Exception e) {
        log.error("Load area path failed, areaId={}", areaId, e);
        throw badRequest(AREA_DATA_NOT_INITIALIZED_MESSAGE);
      }
      if (entity == null) break;
      AreaPathNode node = new AreaPathNode();
      node.setId(entity.getId());
      node.setName(entity.getName());
      node.setLevel(entity.getLevel());
      node.setZipCode(entity.getZipCode());
      path.add(node);
      cursor = entity.getParentId();
      guard += 1;
    }
    Collections.reverse(path);
    return path;
  }

  public List<AreaPathNode> resolvePath(String province, String city, String district) {
    List<AreaPathNode> path = new ArrayList<>();
    Integer parentId = 0;
    try {
      parentId = appendByName(path, parentId, province);
      parentId = appendByName(path, parentId, city);
      parentId = appendByName(path, parentId, district);
    } catch (Exception e) {
      log.error("Resolve area path failed.", e);
      throw badRequest(AREA_DATA_NOT_INITIALIZED_MESSAGE);
    }
    return path;
  }

  private Integer appendByName(List<AreaPathNode> path, Integer parentId, String name) {
    String target = name == null ? "" : name.trim();
    if (target.isEmpty()) return parentId;
    List<AreaEntity> hits = areaMapper.selectByParentAndName(parentId, target);
    if (hits == null || hits.isEmpty()) return parentId;
    AreaEntity entity = hits.get(0);
    AreaPathNode node = new AreaPathNode();
    node.setId(entity.getId());
    node.setName(entity.getName());
    node.setLevel(entity.getLevel());
    node.setZipCode(entity.getZipCode());
    path.add(node);
    return entity.getId();
  }
}
