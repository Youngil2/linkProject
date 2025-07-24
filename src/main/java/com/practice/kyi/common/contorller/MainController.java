package com.practice.kyi.common.contorller;

import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sun.management.OperatingSystemMXBean;

@Controller
public class MainController {
	
	private static final Logger logger = LoggerFactory.getLogger(MainController.class);
	
	 @GetMapping("/system/cpuData")
	 @ResponseBody
	 public Map<String, String> cpuData() throws Exception {
	     Map<String, String> data = new HashMap<>();
			
	     // 서버 PC 의 CPU 사용량 구하기
	     OperatingSystemMXBean mxBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
	     String checkData = String.format("%.2f", mxBean.getSystemCpuLoad() * 100);
	     // 서버 PC의 메모리 사용량 구하기
	     String memoryFree = String.format("%.2f", (double)mxBean.getFreePhysicalMemorySize()/1024/1024/1024);
	    
	     data.put("memoryFree", memoryFree);
	     data.put("checkData", checkData);
	     return data;
	 }
}
