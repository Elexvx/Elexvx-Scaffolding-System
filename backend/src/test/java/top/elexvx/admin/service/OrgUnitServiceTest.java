package top.elexvx.admin.service;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import top.elexvx.admin.dto.OrgUnitUpsertRequest;
import top.elexvx.admin.entity.OrgUnitEntity;
import top.elexvx.admin.mapper.OrgUnitLeaderMapper;
import top.elexvx.admin.mapper.OrgUnitMapper;
import top.elexvx.admin.mapper.UserOrgUnitMapper;
import top.elexvx.admin.mapper.UserMapper;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

public class OrgUnitServiceTest {

  @Test
  void updateCanClearParentIdToRoot() {
    OrgUnitMapper orgUnitMapper = mock(OrgUnitMapper.class);
    OrgUnitLeaderMapper leaderMapper = mock(OrgUnitLeaderMapper.class);
    UserMapper userMapper = mock(UserMapper.class);
    UserOrgUnitMapper userOrgUnitMapper = mock(UserOrgUnitMapper.class);

    OrgUnitService service = new OrgUnitService(orgUnitMapper, leaderMapper, userMapper, userOrgUnitMapper);

    OrgUnitEntity existing = new OrgUnitEntity();
    existing.setId(10L);
    existing.setParentId(1L);
    existing.setName("Old Team");
    existing.setType("DEPARTMENT");
    existing.setSortOrder(1);
    existing.setStatus(1);
    when(orgUnitMapper.selectById(10L)).thenReturn(existing);
    when(orgUnitMapper.selectAll()).thenReturn(List.of(existing));

    when(leaderMapper.selectLeaderIds(10L)).thenReturn(List.of());
    when(userMapper.selectByIds(any())).thenReturn(List.of());
    when(userOrgUnitMapper.selectAll()).thenReturn(List.of());

    OrgUnitUpsertRequest req = new OrgUnitUpsertRequest();
    req.setParentId(null);
    req.setName("New Team");
    req.setType("DEPARTMENT");
    req.setStatus(1);
    req.setLeaderIds(List.of());

    service.update(10L, req);

    ArgumentCaptor<OrgUnitEntity> captor = ArgumentCaptor.forClass(OrgUnitEntity.class);
    verify(orgUnitMapper).update(captor.capture());
    assertNull(captor.getValue().getParentId());
  }
}
