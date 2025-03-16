package Altech.checkout.service;

import java.util.Map;

/**
 * 健康检查服务接口
 */
public interface HealthCheckService {
    
    /**
     * 执行基本健康检查
     * @return 健康检查结果
     */
    Map<String, Object> performBasicHealthCheck();
    
    /**
     * 执行详细健康检查
     * @return 健康检查结果
     */
    Map<String, Object> performDetailedHealthCheck();
    
    /**
     * 检查数据库连接
     * @return 是否连接成功
     */
    boolean isDatabaseConnected();
    
    /**
     * 检查特定表是否存在并可访问
     * @param tableName 表名
     * @return 结果信息
     */
    Map<String, Object> checkTableAccess(String tableName);
} 