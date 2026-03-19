package elexvx.admin.dto;

import java.time.LocalDate;
import java.util.List;

public class UserUpdateRequest {
  private String account;
  private String name;
  private String nickname;
  private String gender;
  private List<String> roles;

  private String mobile;
  private String email;
  private String idType;
  private String idCard;
  private LocalDate idValidFrom;
  private LocalDate idValidTo;
  private String seat;
  private String entity;
  private String leader;
  private String position;
  private LocalDate joinDay;
  private String team;
  private Integer provinceId;
  private String province;
  private Integer cityId;
  private String city;
  private Integer districtId;
  private String district;
  private String zipCode;
  private String address;
  private Integer status;
  private List<Long> orgUnitIds;
  private List<Long> departmentIds;

  public String getAccount() {
    return account;
  }

  public void setAccount(String account) {
    this.account = account;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getNickname() {
    return nickname;
  }

  public void setNickname(String nickname) {
    this.nickname = nickname;
  }

  public String getGender() {
    return gender;
  }

  public void setGender(String gender) {
    this.gender = gender;
  }

  public List<String> getRoles() {
    return roles;
  }

  public void setRoles(List<String> roles) {
    this.roles = roles;
  }

  public String getMobile() {
    return mobile;
  }

  public void setMobile(String mobile) {
    this.mobile = mobile;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getIdType() {
    return idType;
  }

  public void setIdType(String idType) {
    this.idType = idType;
  }

  public String getIdCard() {
    return idCard;
  }

  public void setIdCard(String idCard) {
    this.idCard = idCard;
  }

  public LocalDate getIdValidFrom() {
    return idValidFrom;
  }

  public void setIdValidFrom(LocalDate idValidFrom) {
    this.idValidFrom = idValidFrom;
  }

  public LocalDate getIdValidTo() {
    return idValidTo;
  }

  public void setIdValidTo(LocalDate idValidTo) {
    this.idValidTo = idValidTo;
  }

  public String getSeat() {
    return seat;
  }

  public void setSeat(String seat) {
    this.seat = seat;
  }

  public String getEntity() {
    return entity;
  }

  public void setEntity(String entity) {
    this.entity = entity;
  }

  public String getLeader() {
    return leader;
  }

  public void setLeader(String leader) {
    this.leader = leader;
  }

  public String getPosition() {
    return position;
  }

  public void setPosition(String position) {
    this.position = position;
  }

  public LocalDate getJoinDay() {
    return joinDay;
  }

  public void setJoinDay(LocalDate joinDay) {
    this.joinDay = joinDay;
  }

  public String getTeam() {
    return team;
  }

  public void setTeam(String team) {
    this.team = team;
  }

  public Integer getProvinceId() {
    return provinceId;
  }

  public void setProvinceId(Integer provinceId) {
    this.provinceId = provinceId;
  }

  public String getProvince() {
    return province;
  }

  public void setProvince(String province) {
    this.province = province;
  }

  public Integer getCityId() {
    return cityId;
  }

  public void setCityId(Integer cityId) {
    this.cityId = cityId;
  }

  public String getCity() {
    return city;
  }

  public void setCity(String city) {
    this.city = city;
  }

  public Integer getDistrictId() {
    return districtId;
  }

  public void setDistrictId(Integer districtId) {
    this.districtId = districtId;
  }

  public String getDistrict() {
    return district;
  }

  public void setDistrict(String district) {
    this.district = district;
  }

  public String getZipCode() {
    return zipCode;
  }

  public void setZipCode(String zipCode) {
    this.zipCode = zipCode;
  }

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public Integer getStatus() {
    return status;
  }

  public void setStatus(Integer status) {
    this.status = status;
  }

  public List<Long> getOrgUnitIds() {
    return orgUnitIds;
  }

  public void setOrgUnitIds(List<Long> orgUnitIds) {
    this.orgUnitIds = orgUnitIds;
  }

  public List<Long> getDepartmentIds() {
    return departmentIds;
  }

  public void setDepartmentIds(List<Long> departmentIds) {
    this.departmentIds = departmentIds;
  }
}
