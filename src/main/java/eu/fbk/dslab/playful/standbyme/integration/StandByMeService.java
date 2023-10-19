package eu.fbk.dslab.playful.standbyme.integration;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.fbk.dslab.playful.standbyme.model.Educator;
import eu.fbk.dslab.playful.standbyme.model.ExternalActivity;
import eu.fbk.dslab.playful.standbyme.model.ExternalActivity.Type;
import eu.fbk.dslab.playful.standbyme.model.Group;
import eu.fbk.dslab.playful.standbyme.model.Learner;

@Service
public class StandByMeService implements ApplicationListener<ContextRefreshedEvent> {
	private static transient final Logger logger = LoggerFactory.getLogger(StandByMeService.class);
	
	static final String extModuleConf = "STADBYME";
	
	@Value("${sbm.endpoint}")
	private String sbmEndpoint;
	
	@Value("${sbm.cron}")
	private String sbmCron;
	
	@Value("${playful.domains}")
	private String playfulDomains;
	
	@Value("${playful.endpoint}")
	private String playfulEndpoint;
	
	@Value("${playful.token}")
	private String playfulToken;
	
    @Autowired
    RestTemplate restTemplate;
    
    @Autowired
    TaskScheduler taskScheduler;
    
    ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    
    public void getEducators(String domainId) {
    	String address = sbmEndpoint + "/educators";
    	try {
        	logger.info("getEducators:" + domainId);
    		ResponseEntity<String> res = restTemplate.exchange(address, HttpMethod.GET, 
    				new HttpEntity<Object>(null, createHeaders(false)), String.class);
    		if (!res.getStatusCode().is2xxSuccessful()) {
    			logger.error(String.format("getEducators:[%s] %s", res.getStatusCode(), res.getBody()));
    		}
    		JsonNode jsonNode = mapper.readTree(res.getBody());
    		Iterator<JsonNode> elements = jsonNode.elements();
    		List<Educator> result = new ArrayList<>();
    		while(elements.hasNext()) {
    			JsonNode node = elements.next();
				Educator ed = new Educator();
				ed.setDomainId(domainId);
				ed.setNickname(getField(node, "login"));
    			ed.setEmail(getField(node, "email"));
    			ed.setFirstname(getField(node, "displayname"));
    			result.add(ed);
    		}
    		logger.info("getEducators:" + result.size());
    		HttpEntity<List<Educator>> request = new HttpEntity<>(result, createHeaders(true));
    		String url = playfulEndpoint+ "/ext/int/educators?domainId=" + domainId;
    		res = restTemplate.postForEntity(url, request, String.class);
    		if (!res.getStatusCode().is2xxSuccessful()) {
    			logger.error(String.format("getEducators:[%s] %s", res.getStatusCode(), res.getBody()));
    		}    		
		} catch (Exception e) {
			logger.error(String.format("getEducators:%s", e.getMessage()));
		}
    }
    
    public void getLearners(String domainId) {
    	String address = sbmEndpoint + "/learners";
    	try {
        	logger.info("getLearners:" + domainId);
    		ResponseEntity<String> res = restTemplate.exchange(address, HttpMethod.GET, 
    				new HttpEntity<Object>(null, createHeaders(false)), String.class);
    		if (!res.getStatusCode().is2xxSuccessful()) {
    			logger.error(String.format("getLearners:[%s] %s", res.getStatusCode(), res.getBody()));
    		}
    		JsonNode jsonNode = mapper.readTree(res.getBody());
    		Iterator<JsonNode> elements = jsonNode.elements();
    		List<Learner> result = new ArrayList<>();
    		while(elements.hasNext()) {
    			JsonNode node = elements.next();
				Learner l = new Learner();
				l.setDomainId(domainId);
				l.setNickname(getField(node, "login"));
    			l.setEmail(getField(node, "email"));
    			l.setFirstname(getField(node, "displayname"));
    			result.add(l);
    		}
    		logger.info("getLearners:" + result.size());
    		HttpEntity<List<Learner>> request = new HttpEntity<>(result, createHeaders(true));
    		String url = playfulEndpoint+ "/ext/int/learners?domainId=" + domainId;
    		res = restTemplate.postForEntity(url, request, String.class);
    		if (!res.getStatusCode().is2xxSuccessful()) {
    			logger.error(String.format("getLearners:[%s] %s", res.getStatusCode(), res.getBody()));
    		}    		    		
		} catch (Exception e) {
			logger.error(String.format("getLearners:%s", e.getMessage()));
		}
    }
    
    public void getGroups(String domainId) {
    	String address = sbmEndpoint + "/groups";
    	try {
        	logger.info("getGroups:" + domainId);
    		ResponseEntity<String> res = restTemplate.exchange(address, HttpMethod.GET, 
    				new HttpEntity<Object>(null, createHeaders(false)), String.class);
    		if (!res.getStatusCode().is2xxSuccessful()) {
    			logger.error(String.format("getGroups:[%s] %s", res.getStatusCode(), res.getBody()));
    		}
    		JsonNode jsonNode = mapper.readTree(res.getBody());
    		Iterator<JsonNode> elements = jsonNode.elements();
    		List<Group> result = new ArrayList<>();
    		while(elements.hasNext()) {
    			JsonNode node = elements.next();
				Group g = new Group();
				g.setDomainId(domainId);
				g.setExtId(getField(node, "id"));
    			g.setName(getField(node, "name"));
    			g.getLearners().clear();
    			JsonNode membersNode = mapper.readTree(node.get("members").asText());
    			Iterator<JsonNode> members = membersNode.elements();
    			while(members.hasNext()) {
    				JsonNode memberNode = members.next();
    				String learnerLogin = getField(memberNode, "login");
    				g.getLearners().add(learnerLogin);
    			}
    			result.add(g);
    		}
    		logger.info("getGroups:" + result.size());
    		HttpEntity<List<Group>> request = new HttpEntity<>(result, createHeaders(true));
    		String url = playfulEndpoint+ "/ext/int/groups?domainId=" + domainId;
    		res = restTemplate.postForEntity(url, request, String.class);
    		if (!res.getStatusCode().is2xxSuccessful()) {
    			logger.error(String.format("getGroups:[%s] %s", res.getStatusCode(), res.getBody()));
    		}    		    		    		
		} catch (Exception e) {
			logger.error(String.format("getGroups:%s", e.getMessage()));
		}    	
    }

    public void getActivities(String domainId) {
    	String address = sbmEndpoint + "/activities";
    	try {
        	logger.info("getActivities:" + domainId);
    		ResponseEntity<String> res = restTemplate.exchange(address, HttpMethod.GET, 
    				new HttpEntity<Object>(null, createHeaders(false)), String.class);
    		if (!res.getStatusCode().is2xxSuccessful()) {
    			logger.error(String.format("getActivities:[%s] %s", res.getStatusCode(), res.getBody()));
    		}
    		JsonNode jsonNode = mapper.readTree(res.getBody());
    		Iterator<JsonNode> elements = jsonNode.elements();
    		List<ExternalActivity> result = new ArrayList<>();
    		while(elements.hasNext()) {
    			JsonNode node = elements.next();
				ExternalActivity act = new ExternalActivity();
				act.setDomainId(domainId);
				act.setExtId(getField(node, "id"));
    			act.setTitle(getField(node, "title"));
    			act.setDesc(getField(node, "description"));
    			act.setLanguage(getField(node, "language"));
    			act.setExtUrl(getField(node, "url"));
    			act.setType(Type.individual);
    			//no group activities for now
    			/*String groupAct = getField(node, "group");
    			if(StringUtils.hasText(groupAct)) {
    				if(groupAct.equals("yes")) {
    					act.setType(Type.group);
    				}
    			}*/
        		result.add(act);
    		}
    		logger.info("getActivities:" + result.size());
    		HttpEntity<List<ExternalActivity>> request = new HttpEntity<>(result, createHeaders(true));
    		String url = playfulEndpoint+ "/ext/int/activities?domainId=" + domainId;
    		res = restTemplate.postForEntity(url, request, String.class);
    		if (!res.getStatusCode().is2xxSuccessful()) {
    			logger.error(String.format("getActivities:[%s] %s", res.getStatusCode(), res.getBody()));
    		}    		    		    		
		} catch (Exception e) {
			logger.error(String.format("getActivities:%s", e.getMessage()));
		}
    }
    
    public void importData(List<String> domains) {
    	domains.forEach(domainId -> {
    		getEducators(domainId);
    		getLearners(domainId);
    		getGroups(domainId);
    		getActivities(domainId);
    	});
    }

    String getField(JsonNode node, String filed) {
    	return node.get(filed).asText().replace("\"", "");
    }
    
	HttpHeaders createHeaders(boolean token) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		headers.setAcceptCharset(Arrays.asList(Charset.forName("UTF-8")));
		if(token) {
			headers.add("x-auth", playfulToken);
		}
		return headers;
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		ImportTask task = new ImportTask(Arrays.asList(playfulDomains.split(",")));
		taskScheduler.schedule(task, new CronTrigger(sbmCron));
	}	
    
	private class ImportTask implements Runnable {
		List<String> domains;
		
		public ImportTask(List<String> domains) {
			this.domains = domains;
		}
		
		@Override
		public void run() {
			importData(this.domains);
		}
		
	}
}
