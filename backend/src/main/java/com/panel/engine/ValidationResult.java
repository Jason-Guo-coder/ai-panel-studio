package com.panel.engine;

// 发言调度硬规则校验结果
public record ValidationResult(boolean valid, String reason) {
    public static ValidationResult ok() {
        return new ValidationResult(true, null);
    }

    public static ValidationResult invalid(String reason) {
        return new ValidationResult(false, reason);
    }
}
