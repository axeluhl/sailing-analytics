package com.sap.sse.landscape.aws.orchestration;

import com.sap.sse.common.HttpRequestHeaderConstants;
import com.sap.sse.landscape.aws.ApplicationLoadBalancer;
import com.sap.sse.landscape.aws.TargetGroup;
import com.sap.sse.landscape.aws.common.shared.PlainRedirectDTO;

import software.amazon.awssdk.services.elasticloadbalancingv2.model.Action;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.ActionTypeEnum;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.Rule;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.RuleCondition;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.TargetGroupTuple;

public interface ProcedureCreatingLoadBalancerMapping<ShardingKey> {
    /**
     * The default number of rules required for a replica set, including a default redirect rule, but excluding
     * any additional sharding rules.
     */
    int NUMBER_OF_RULES_PER_REPLICA_SET = 5;
    
    default Rule[] createRules(ApplicationLoadBalancer<ShardingKey> alb, String hostName, TargetGroup<ShardingKey> masterTargetGroup,
            TargetGroup<ShardingKey> publicTargetGroup) {
        final Rule[] rules = new Rule[NUMBER_OF_RULES_PER_REPLICA_SET];
        int ruleCount = 0;
        rules[ruleCount++] = alb.getDefaultRedirectRule(hostName, new PlainRedirectDTO());
        rules[ruleCount++] = Rule.builder().conditions(
                RuleCondition.builder().field("http-header")
                        .httpHeaderConfig(hhcb -> hhcb.httpHeaderName(HttpRequestHeaderConstants.HEADER_KEY_FORWARD_TO)
                                .values(HttpRequestHeaderConstants.HEADER_FORWARD_TO_MASTER.getB()))
                        .build(),
                alb.createHostHeaderRuleCondition(hostName))
                .actions(createForwardToTargetGroupAction(masterTargetGroup)).build();
        rules[ruleCount++] = Rule.builder().conditions(
                RuleCondition.builder().field("http-header")
                        .httpHeaderConfig(hhcb -> hhcb.httpHeaderName(HttpRequestHeaderConstants.HEADER_KEY_FORWARD_TO)
                                .values(HttpRequestHeaderConstants.HEADER_FORWARD_TO_REPLICA.getB()))
                        .build(),
                alb.createHostHeaderRuleCondition(hostName))
                .actions(createForwardToTargetGroupAction(publicTargetGroup)).build();
        rules[ruleCount++] = Rule.builder()
                .conditions(
                        RuleCondition.builder().field("http-request-method")
                                .httpRequestMethodConfig(hrmcb -> hrmcb.values("GET")).build(),
                        alb.createHostHeaderRuleCondition(hostName))
                .actions(createForwardToTargetGroupAction(publicTargetGroup)).build();
        rules[ruleCount++] = Rule.builder()
                .conditions(alb.createHostHeaderRuleCondition(hostName))
                .actions(createForwardToTargetGroupAction(masterTargetGroup)).build();
        assert ruleCount == NUMBER_OF_RULES_PER_REPLICA_SET;
        return rules;
    }
    
    default Action createForwardToTargetGroupAction(TargetGroup<ShardingKey> targetGroup) {
        return Action.builder().type(ActionTypeEnum.FORWARD).forwardConfig(fc -> fc.targetGroups(
                TargetGroupTuple.builder().targetGroupArn(targetGroup.getTargetGroupArn()).build())) .build();
    }


}
