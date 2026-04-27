package com.cookie.linkpulse.config;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class SentinelConfig {

    @PostConstruct
    public void initSentinelRules() {
        List<FlowRule> rules = new ArrayList<>();

        // 短链跳转接口限流：每秒最多 5 次
        FlowRule redirectRule = new FlowRule();
        redirectRule.setResource("short-link-redirect");
        redirectRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        redirectRule.setCount(5);
        rules.add(redirectRule);

        // 后台创建短链接口限流：每秒最多 2 次
        FlowRule createRule = new FlowRule();
        createRule.setResource("admin-create-short-link");
        createRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        createRule.setCount(2);
        rules.add(createRule);

        FlowRuleManager.loadRules(rules);

        System.out.println("Sentinel flow rules loaded: short-link-redirect QPS=5, admin-create-short-link QPS=2");
    }
}