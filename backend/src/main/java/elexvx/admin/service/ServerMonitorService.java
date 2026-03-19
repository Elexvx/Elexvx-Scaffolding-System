package elexvx.admin.service;

import com.sun.management.OperatingSystemMXBean;
import elexvx.admin.vo.ServerInfoVO;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import org.springframework.stereotype.Service;

@Service
public class ServerMonitorService {

  private static final DecimalFormat DF = new DecimalFormat("0.00");
  private static final long GB = 1024 * 1024 * 1024;
  private static final long MB = 1024 * 1024;

  public ServerInfoVO getServerInfo() {
    ServerInfoVO info = new ServerInfoVO();
    info.setCpu(getCpuInfo());
    info.setMem(getMemInfo());
    info.setSys(getSysInfo());
    info.setJvm(getJvmInfo());
    info.setSysFiles(getSysFiles());
    return info;
  }

  private ServerInfoVO.Cpu getCpuInfo() {
    ServerInfoVO.Cpu cpu = new ServerInfoVO.Cpu();
    OperatingSystemMXBean os = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
    
    int cpuNum = os.getAvailableProcessors();
    double systemCpuLoad = os.getCpuLoad();
    double processCpuLoad = os.getProcessCpuLoad();
    
    cpu.setCpuNum(cpuNum);
    cpu.setUsed(DF.format(processCpuLoad * 100) + "%");
    cpu.setSys(DF.format(systemCpuLoad * 100) + "%");
    cpu.setFree(DF.format((1 - systemCpuLoad) * 100) + "%");
    
    return cpu;
  }

  private ServerInfoVO.Mem getMemInfo() {
    ServerInfoVO.Mem mem = new ServerInfoVO.Mem();
    OperatingSystemMXBean os = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
    
    // 系统内存
    long totalPhysicalMemory = os.getTotalMemorySize();
    long freePhysicalMemory = os.getFreeMemorySize();
    long usedMemory = totalPhysicalMemory - freePhysicalMemory;
    
    mem.setTotal(formatBytes(totalPhysicalMemory));
    mem.setUsed(formatBytes(usedMemory));
    mem.setFree(formatBytes(freePhysicalMemory));
    mem.setUsage(DF.format((double) usedMemory / totalPhysicalMemory * 100) + "%");
    
    // JVM 内存
    long jvmTotal = Runtime.getRuntime().totalMemory();
    long jvmFree = Runtime.getRuntime().freeMemory();
    long jvmUsed = jvmTotal - jvmFree;
    long jvmMax = Runtime.getRuntime().maxMemory();
    
    mem.setJvmTotal(formatBytes(jvmMax));
    mem.setJvmUsed(formatBytes(jvmUsed));
    mem.setJvmFree(formatBytes(jvmMax - jvmUsed));
    mem.setJvmUsage(DF.format((double) jvmUsed / jvmMax * 100) + "%");
    
    return mem;
  }

  private ServerInfoVO.Sys getSysInfo() {
    ServerInfoVO.Sys sys = new ServerInfoVO.Sys();
    Properties props = System.getProperties();
    
    try {
      InetAddress addr = InetAddress.getLocalHost();
      sys.setComputerName(addr.getHostName());
      sys.setComputerIp(addr.getHostAddress());
    } catch (UnknownHostException e) {
      sys.setComputerName("unknown");
      sys.setComputerIp("unknown");
    }
    
    sys.setOsName(props.getProperty("os.name"));
    sys.setOsArch(props.getProperty("os.arch"));
    
    return sys;
  }

  private ServerInfoVO.Jvm getJvmInfo() {
    ServerInfoVO.Jvm jvm = new ServerInfoVO.Jvm();
    Properties props = System.getProperties();
    RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
    
    jvm.setName(runtime.getVmName());
    jvm.setVersion(props.getProperty("java.version"));
    jvm.setHome(props.getProperty("java.home"));
    
    long startTime = runtime.getStartTime();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    jvm.setStartTime(sdf.format(new Date(startTime)));
    
    long uptime = runtime.getUptime();
    jvm.setRunTime(formatUptime(uptime));
    
    jvm.setProjectPath(System.getProperty("user.dir"));
    jvm.setInputArgs(String.join(" ", runtime.getInputArguments()));
    
    return jvm;
  }

  private List<ServerInfoVO.SysFile> getSysFiles() {
    List<ServerInfoVO.SysFile> sysFiles = new ArrayList<>();
    File[] roots = File.listRoots();
    
    for (File file : roots) {
      long total = file.getTotalSpace();
      long free = file.getFreeSpace();
      long used = total - free;
      
      if (total == 0) continue;
      
      ServerInfoVO.SysFile sf = new ServerInfoVO.SysFile();
      sf.setDirName(file.getPath());
      sf.setSysTypeName(System.getProperty("os.name").contains("Windows") ? "NTFS" : "ext4");
      sf.setTypeName(file.getPath().equals("/") ? "/" : file.getPath());
      sf.setTotal(formatBytes(total));
      sf.setFree(formatBytes(free));
      sf.setUsed(formatBytes(used));
      sf.setUsage(DF.format((double) used / total * 100) + "%");
      
      sysFiles.add(sf);
    }
    
    return sysFiles;
  }

  private String formatBytes(long bytes) {
    if (bytes >= GB) {
      return DF.format((double) bytes / GB) + "G";
    } else if (bytes >= MB) {
      return DF.format((double) bytes / MB) + "M";
    } else {
      return bytes + "B";
    }
  }

  private String formatUptime(long uptime) {
    long days = uptime / (1000 * 60 * 60 * 24);
    long hours = (uptime % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60);
    long minutes = (uptime % (1000 * 60 * 60)) / (1000 * 60);
    
    StringBuilder sb = new StringBuilder();
    if (days > 0) sb.append(days).append("天");
    if (hours > 0) sb.append(hours).append("小时");
    sb.append(minutes).append("分钟");
    
    return sb.toString();
  }
}
