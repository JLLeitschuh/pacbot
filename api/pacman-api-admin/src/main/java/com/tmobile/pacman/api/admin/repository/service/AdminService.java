package com.tmobile.pacman.api.admin.repository.service;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.amazonaws.services.cloudwatchevents.model.DisableRuleRequest;
import com.amazonaws.services.cloudwatchevents.model.EnableRuleRequest;
import com.amazonaws.services.cloudwatchevents.model.ListRulesRequest;
import com.amazonaws.services.cloudwatchevents.model.RuleState;
import com.tmobile.pacman.api.admin.common.AdminConstants;
import com.tmobile.pacman.api.admin.config.PacmanConfiguration;
import com.tmobile.pacman.api.admin.repository.JobExecutionManagerRepository;
import com.tmobile.pacman.api.admin.repository.RuleRepository;
import com.tmobile.pacman.api.admin.repository.model.JobExecutionManager;
import com.tmobile.pacman.api.admin.repository.model.Rule;
import com.tmobile.pacman.api.admin.service.AmazonClientBuilderService;

@Service
public class AdminService {
	
	private static final Logger log = LoggerFactory.getLogger(AdminService.class);
	
	@Autowired
	private RuleRepository ruleRepository;
	
	@Autowired
	private JobExecutionManagerRepository jobRepository;
	
	@Autowired
	private AmazonClientBuilderService amazonClient;
	
	@Autowired
	private PacmanConfiguration config;
	
	public String shutDownAlloperations(String operation, String job) {
		if(operation.equals(AdminConstants.ENABLE)) {
			if(job.equals(AdminConstants.RULE)) {
				if(enableRules()) {
					return "All rules has been sucessfully enabled";
				}
			} else if(job.equals(AdminConstants.JOB)) {
				if(enableJobs()) {
					return "All jobs has been sucessfully enabled";
				}
			} else {
				if(enableRules() &&	enableJobs()) {
					return "All rules and jobs has been sucessfully enabled";
				}
			}
			return "Enabling operation failed";
		} else {
			if(job.equals(AdminConstants.RULE)) {
				if(disableRules()) {
					return "All rules has been sucessfully disabled";
				}
			} else if(job.equals(AdminConstants.JOB)) {
				if(disableJobs()) {
					return "All obs has been sucessfully disabled";
				}
			} else {
				if(disableRules() && disableJobs()) {
					return "All rules and jobs has been sucessfully disabled";
				}
			}
			return "Disabling operation failed";
		}
	}
	
	private boolean disableRules() {
		List<Rule> ruleIds = ruleRepository.findAll();
		List<String> rules = amazonClient.getAmazonCloudWatchEvents(config.getRule().getLambda().getRegion())
			.listRules(new ListRulesRequest()).getRules().parallelStream().map(rule->rule.getName()).collect(Collectors.toList());
		try {
			for(Rule rule : ruleIds) {
				if(rules.contains(rule.getRuleUUID())) {
					amazonClient.getAmazonCloudWatchEvents(config.getRule().getLambda().getRegion())
						.disableRule(new DisableRuleRequest().withName(rule.getRuleUUID()));
					rule.setStatus(RuleState.DISABLED.name());
					ruleRepository.save(rule);
				}
			}
			return true;
		} catch(Exception e) {
			log.error("Error in disable rules",e);
			return false;
		}
		
	}
	
	private boolean disableJobs() {
		List<JobExecutionManager> jobIds = jobRepository.findAll();
		List<String> rules = amazonClient.getAmazonCloudWatchEvents(config.getRule().getLambda().getRegion())
				.listRules(new ListRulesRequest()).getRules().parallelStream().map(rule->rule.getName()).collect(Collectors.toList());
		try {
			for(JobExecutionManager job : jobIds) {
				if(rules.contains(job.getJobUUID())) {
					amazonClient.getAmazonCloudWatchEvents(config.getRule().getLambda().getRegion())
						.disableRule(new DisableRuleRequest().withName(job.getJobUUID()));
					job.setStatus(RuleState.DISABLED.name());
					jobRepository.save(job);
				}
			}
			return true;
		} catch(Exception e) {
			log.error("Error in disable jobs",e);
			return false;
		}
	}
	
	private boolean enableRules() {
		List<Rule> ruleIds = ruleRepository.findAll();
		List<String> rules = amazonClient.getAmazonCloudWatchEvents(config.getRule().getLambda().getRegion())
				.listRules(new ListRulesRequest()).getRules().parallelStream().map(rule->rule.getName()).collect(Collectors.toList());
		try {
			for(Rule rule : ruleIds) {
				if(rules.contains(rule.getRuleUUID())) {
					amazonClient.getAmazonCloudWatchEvents(config.getRule().getLambda().getRegion())
							.enableRule(new EnableRuleRequest().withName(rule.getRuleUUID()));
					rule.setStatus(RuleState.ENABLED.name());
					ruleRepository.save(rule);
				}
			}
			return true;
		} catch(Exception e) {
			log.error("Error in enable rules",e);
			return false;
		}
	}
	
	private boolean enableJobs() {
		List<JobExecutionManager> jobIds = jobRepository.findAll();
		List<String> rules = amazonClient.getAmazonCloudWatchEvents(config.getRule().getLambda().getRegion())
				.listRules(new ListRulesRequest()).getRules().parallelStream().map(rule->rule.getName()).collect(Collectors.toList());
		try {
			for(JobExecutionManager job : jobIds) {
				if(rules.contains(job.getJobUUID())) {
					amazonClient.getAmazonCloudWatchEvents(config.getRule().getLambda().getRegion())
					.enableRule(new EnableRuleRequest().withName(job.getJobUUID()));
					job.setStatus(RuleState.DISABLED.name());
					jobRepository.save(job);
				}
			}
			return true;
		} catch(Exception e) {
			log.error("Error in enable jobs",e);
			return false;
		}
	}

}