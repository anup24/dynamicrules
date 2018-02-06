package com.dynamicrules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.drools.template.ObjectDataCompiler;
import org.kie.api.KieServices;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.StatelessKieSession;

import com.dynamicrules.data.Condition;
import com.dynamicrules.data.Rule;

public final class RuleBuilder {

	public static StatelessKieSession statelessKieSession;

	static public void main(String[] args) throws Exception {

		// this part will be replaced with rules retrieved from database tables RuleData ewiRule1 = new RuleData();
		List<RuleData> ewiRules = new ArrayList<RuleData>();
		List<Threshold> thresholdList = new ArrayList<Threshold>();
		RuleData ewiRule1 = new RuleData();
		ewiRule1.setRuleString("@CURRENT_EXPOSURE > $Exposure && @LIMIT > $Limit");
		ewiRules.add(ewiRule1);

		RuleData ewiRule2 = new RuleData();
		ewiRule2.setRuleString("@CURRENT_EXPOSURE > $Exposure && @LIMIT <= $Limit");
		ewiRules.add(ewiRule2);

		Threshold threshold1 = new Threshold();
		threshold1.setThresholdName("Exposure");
		threshold1.setThresholdValue("500000.90");
		thresholdList.add(threshold1);

		Threshold threshold2 = new Threshold();
		threshold2.setThresholdName("Limit");
		threshold2.setThresholdValue("35000.90");
		thresholdList.add(threshold2);

		List<Rule> listOfRules = new ArrayList<Rule>();

		if (ewiRules != null && ewiRules.size() > 0) {
			for (RuleData ewiRulee : ewiRules) {

				Rule rule = new Rule();
				List<String> rules = Arrays.asList(ewiRulee.getRuleString().split(" && "));

				for (String ruleString : rules) {

					List<String> rulesComponents = Arrays.asList(ruleString.split(" "));

					Condition condition = new Condition();

					for (String comp : rulesComponents) {
						if (StringUtils.startsWith(comp, "@")) {
							condition.setField(comp.substring(1));

						} else if (StringUtils.equals(comp, "==")) {
							condition.setOperator(Condition.Operator.EQUAL_TO);

						} else if (StringUtils.equals(comp, "!=")) {
							condition.setOperator(Condition.Operator.NOT_EQUAL_TO);

						} else if (StringUtils.equals(comp, ">")) {
							condition.setOperator(Condition.Operator.GREATER_THAN);

						} else if (StringUtils.equals(comp, "<")) {
							condition.setOperator(Condition.Operator.LESS_THAN);

						} else if (StringUtils.equals(comp, ">=")) {
							condition.setOperator(Condition.Operator.GREATER_THAN_OR_EQUAL_TO);

						} else if (StringUtils.equals(comp, "<=")) {
							condition.setOperator(Condition.Operator.LESS_THAN_OR_EQUAL_TO);

						} else if (StringUtils.startsWith(comp, "$")) {
							for (Threshold threshold : thresholdList) {
								if (StringUtils.equals(threshold.getThresholdName(), comp.substring(1)))
									condition.setValue(NumberUtils.createNumber(threshold.getThresholdValue()));
							}
						}
					}
					rule.getConditions().add(condition);
				}
				listOfRules.add(rule);
			}

			// fact - test data
			Map<String, Object> data = new HashMap<String, Object>();
			data.put("CURRENT_EXPOSURE", "909090909.89");
			data.put("LIMIT", "36000.90");
			Event event = new Event(data);

			// apply the rule template
			String drl = applyRuleTemplate(event, listOfRules);
			System.out.println(drl);

			// pass the fact data to the rule - this is testing the generated rule
			boolean flag = evaluate(drl, event);

			if (flag) {
				System.out.println("Rules executed !!");
			}
		}
	}

	public static StatelessKieSession getStatelessKieSession() {
		KieServices ks = KieServices.Factory.get();
		KieContainer kc = ks.getKieClasspathContainer();
		if (statelessKieSession == null) {
			statelessKieSession = kc.newStatelessKieSession("ksession-rules");
		}
		return statelessKieSession;
	}

	private static boolean evaluate(String drl, Event event) throws Exception {
		KieServices kieServices = KieServices.Factory.get();
		KieFileSystem kieFileSystem = kieServices.newKieFileSystem();
		kieFileSystem.write("src/main/resources/rule.drl", drl);
		kieServices.newKieBuilder(kieFileSystem).buildAll();

		KieContainer kieContainer = kieServices.newKieContainer(kieServices.getRepository().getDefaultReleaseId());
		StatelessKieSession statelessKieSession = kieContainer.getKieBase().newStatelessKieSession();

		// set global variables if any
		RuleDecision ruleDecision = new RuleDecision();
		statelessKieSession.getGlobals().set("alertDecision", ruleDecision);
		statelessKieSession.execute(event);
		return true;
	}

	static private String applyRuleTemplate(Event event, List<Rule> llist) throws Exception {
		List<Map<String, Object>> dataMap = new ArrayList<Map<String, Object>>();

		ObjectDataCompiler objectDataCompiler = new ObjectDataCompiler();

		for (Rule rule : llist) {
			Map<String, Object> data = new HashMap<String, Object>();
			data.put("rule", rule);
			data.put("eventType", event.getClass().getName());
			dataMap.add(data);
		}

		return objectDataCompiler.compile(dataMap, Thread.currentThread().getContextClassLoader().getResourceAsStream("rule-template.drl"));
	}
}