package top.elexvx.admin.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class OrgUnitReorderRequest {
  @NotEmpty(message = "items 不能为空")
  @Valid
  private List<Item> items;

  public List<Item> getItems() {
    return items;
  }

  public void setItems(List<Item> items) {
    this.items = items;
  }

  public static class Item {
    @NotNull(message = "id 不能为空")
    private Long id;

    private Long parentId;

    @NotNull(message = "sortOrder 不能为空")
    private Integer sortOrder;

    public Long getId() {
      return id;
    }

    public void setId(Long id) {
      this.id = id;
    }

    public Long getParentId() {
      return parentId;
    }

    public void setParentId(Long parentId) {
      this.parentId = parentId;
    }

    public Integer getSortOrder() {
      return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
      this.sortOrder = sortOrder;
    }
  }
}
