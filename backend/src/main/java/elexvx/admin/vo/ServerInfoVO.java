package elexvx.admin.vo;

import java.util.List;

public class ServerInfoVO {
  private Cpu cpu;
  private Mem mem;
  private Sys sys;
  private Jvm jvm;
  private List<SysFile> sysFiles;

  public static class Cpu {
    private int cpuNum;
    private String used;
    private String sys;
    private String free;

    public int getCpuNum() { return cpuNum; }
    public void setCpuNum(int cpuNum) { this.cpuNum = cpuNum; }
    public String getUsed() { return used; }
    public void setUsed(String used) { this.used = used; }
    public String getSys() { return sys; }
    public void setSys(String sys) { this.sys = sys; }
    public String getFree() { return free; }
    public void setFree(String free) { this.free = free; }
  }

  public static class Mem {
    private String total;
    private String used;
    private String free;
    private String usage;
    private String jvmTotal;
    private String jvmUsed;
    private String jvmFree;
    private String jvmUsage;

    public String getTotal() { return total; }
    public void setTotal(String total) { this.total = total; }
    public String getUsed() { return used; }
    public void setUsed(String used) { this.used = used; }
    public String getFree() { return free; }
    public void setFree(String free) { this.free = free; }
    public String getUsage() { return usage; }
    public void setUsage(String usage) { this.usage = usage; }
    public String getJvmTotal() { return jvmTotal; }
    public void setJvmTotal(String jvmTotal) { this.jvmTotal = jvmTotal; }
    public String getJvmUsed() { return jvmUsed; }
    public void setJvmUsed(String jvmUsed) { this.jvmUsed = jvmUsed; }
    public String getJvmFree() { return jvmFree; }
    public void setJvmFree(String jvmFree) { this.jvmFree = jvmFree; }
    public String getJvmUsage() { return jvmUsage; }
    public void setJvmUsage(String jvmUsage) { this.jvmUsage = jvmUsage; }
  }

  public static class Sys {
    private String computerName;
    private String computerIp;
    private String osName;
    private String osArch;

    public String getComputerName() { return computerName; }
    public void setComputerName(String computerName) { this.computerName = computerName; }
    public String getComputerIp() { return computerIp; }
    public void setComputerIp(String computerIp) { this.computerIp = computerIp; }
    public String getOsName() { return osName; }
    public void setOsName(String osName) { this.osName = osName; }
    public String getOsArch() { return osArch; }
    public void setOsArch(String osArch) { this.osArch = osArch; }
  }

  public static class Jvm {
    private String name;
    private String version;
    private String startTime;
    private String runTime;
    private String home;
    private String projectPath;
    private String inputArgs;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public String getRunTime() { return runTime; }
    public void setRunTime(String runTime) { this.runTime = runTime; }
    public String getHome() { return home; }
    public void setHome(String home) { this.home = home; }
    public String getProjectPath() { return projectPath; }
    public void setProjectPath(String projectPath) { this.projectPath = projectPath; }
    public String getInputArgs() { return inputArgs; }
    public void setInputArgs(String inputArgs) { this.inputArgs = inputArgs; }
  }

  public static class SysFile {
    private String dirName;
    private String sysTypeName;
    private String typeName;
    private String total;
    private String free;
    private String used;
    private String usage;

    public String getDirName() { return dirName; }
    public void setDirName(String dirName) { this.dirName = dirName; }
    public String getSysTypeName() { return sysTypeName; }
    public void setSysTypeName(String sysTypeName) { this.sysTypeName = sysTypeName; }
    public String getTypeName() { return typeName; }
    public void setTypeName(String typeName) { this.typeName = typeName; }
    public String getTotal() { return total; }
    public void setTotal(String total) { this.total = total; }
    public String getFree() { return free; }
    public void setFree(String free) { this.free = free; }
    public String getUsed() { return used; }
    public void setUsed(String used) { this.used = used; }
    public String getUsage() { return usage; }
    public void setUsage(String usage) { this.usage = usage; }
  }

  public Cpu getCpu() { return cpu; }
  public void setCpu(Cpu cpu) { this.cpu = cpu; }
  public Mem getMem() { return mem; }
  public void setMem(Mem mem) { this.mem = mem; }
  public Sys getSys() { return sys; }
  public void setSys(Sys sys) { this.sys = sys; }
  public Jvm getJvm() { return jvm; }
  public void setJvm(Jvm jvm) { this.jvm = jvm; }
  public List<SysFile> getSysFiles() { return sysFiles; }
  public void setSysFiles(List<SysFile> sysFiles) { this.sysFiles = sysFiles; }
}
