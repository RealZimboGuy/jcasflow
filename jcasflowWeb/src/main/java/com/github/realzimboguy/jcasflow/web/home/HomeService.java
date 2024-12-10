package com.github.realzimboguy.jcasflow.web.home;

import com.github.realzimboguy.jcasflow.engine.repo.dao.WorkflowInProgressDao;
import com.github.realzimboguy.jcasflow.engine.repo.dao.WorkflowNextExecutionDao;
import com.github.realzimboguy.jcasflow.engine.repo.entity.WorkflowInProgressGroupCountEntity;
import com.github.realzimboguy.jcasflow.engine.repo.entity.WorkflowNextExecutionGroupCountEntity;
import com.github.realzimboguy.jcasflow.web.home.model.WorkflowNextExecutionGroupCountModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class HomeService {

	Logger logger = LoggerFactory.getLogger(HomeService.class);

	private final WorkflowInProgressDao workflowInProgressDao;
	private final WorkflowNextExecutionDao workflowNextExecutionDao;

	public HomeService(WorkflowInProgressDao workflowInProgressDao, WorkflowNextExecutionDao workflowNextExecutionDao) {

		this.workflowInProgressDao = workflowInProgressDao;
		this.workflowNextExecutionDao = workflowNextExecutionDao;
	}

	public List<WorkflowInProgressGroupCountEntity> getWorkflowsInProgress() {

		List<WorkflowInProgressGroupCountEntity> workflowInProgressGroupCountEntities = new ArrayList<>();
		for (String group : workflowNextExecutionDao.getGroups()) {
			workflowInProgressGroupCountEntities.addAll(workflowInProgressDao.countInProgress(group));
		}
		return workflowInProgressGroupCountEntities;
	}

	public List<WorkflowNextExecutionGroupCountModel> getWorkflowsNextExecution() {

		List<WorkflowNextExecutionGroupCountModel> workflowNextExecutionGroupCountModels = new ArrayList<>();

		//calculate instant 5 min in the future
		Instant now = Instant.now();
		Instant inOneMin = now.plusSeconds(60);
		Instant inFiveMin = now.plusSeconds(300);
		Instant inThirtyMin = now.plusSeconds(1800);
		Instant inOneHour = now.plusSeconds(3600);
		Instant inOneDay = now.plusSeconds(86400);

		for (String group : workflowNextExecutionDao.getGroups()) {

			CompletableFuture<List<WorkflowNextExecutionGroupCountEntity>> futureInOneMin = CompletableFuture.supplyAsync(() ->
					workflowNextExecutionDao.countNextExecution(group,now, inOneMin)
			);
			CompletableFuture<List<WorkflowNextExecutionGroupCountEntity>> futureInFiveMin = CompletableFuture.supplyAsync(() ->
					workflowNextExecutionDao.countNextExecution(group,inOneMin, inFiveMin)
			);
			CompletableFuture<List<WorkflowNextExecutionGroupCountEntity>> futureInThirtyMin = CompletableFuture.supplyAsync(() ->
					workflowNextExecutionDao.countNextExecution(group,inFiveMin, inThirtyMin)
			);

			CompletableFuture<List<WorkflowNextExecutionGroupCountEntity>> futureInOneHour = CompletableFuture.supplyAsync(() ->
					workflowNextExecutionDao.countNextExecution(group,inThirtyMin, inOneHour)
			);

			CompletableFuture<List<WorkflowNextExecutionGroupCountEntity>> futureInOneDay = CompletableFuture.supplyAsync(() ->
					workflowNextExecutionDao.countNextExecution(group,inOneHour, inOneDay)
			);

			CompletableFuture<List<WorkflowNextExecutionGroupCountEntity>> futureGreaterOneDay = CompletableFuture.supplyAsync(() ->
					workflowNextExecutionDao.countNextExecution(group, inOneDay)
			);


			CompletableFuture.allOf(futureInOneMin,futureInFiveMin, futureInThirtyMin, futureInOneHour, futureInOneDay, futureGreaterOneDay).join();

			//add all the futures to the results
			for (WorkflowNextExecutionGroupCountEntity workflowNextExecutionGroupCountEntity : futureInOneMin.join()) {
				workflowNextExecutionGroupCountModels.add(new WorkflowNextExecutionGroupCountModel("In 1 min",
						workflowNextExecutionGroupCountEntity.getGroup(),
						workflowNextExecutionGroupCountEntity.getCount()));
			}
			for (WorkflowNextExecutionGroupCountEntity workflowNextExecutionGroupCountEntity : futureInFiveMin.join()) {
				workflowNextExecutionGroupCountModels.add(new WorkflowNextExecutionGroupCountModel("In 5 min",
						workflowNextExecutionGroupCountEntity.getGroup(),
						workflowNextExecutionGroupCountEntity.getCount()));
			}
			for (WorkflowNextExecutionGroupCountEntity workflowNextExecutionGroupCountEntity : futureInThirtyMin.join()) {
				workflowNextExecutionGroupCountModels.add(new WorkflowNextExecutionGroupCountModel("In 30 min",
						workflowNextExecutionGroupCountEntity.getGroup(),
						workflowNextExecutionGroupCountEntity.getCount()));
			}
			for (WorkflowNextExecutionGroupCountEntity workflowNextExecutionGroupCountEntity : futureInOneHour.join()) {
				workflowNextExecutionGroupCountModels.add(new WorkflowNextExecutionGroupCountModel("In 1 hour",
						workflowNextExecutionGroupCountEntity.getGroup(),
						workflowNextExecutionGroupCountEntity.getCount()));
			}
			for (WorkflowNextExecutionGroupCountEntity workflowNextExecutionGroupCountEntity : futureInOneDay.join()) {
				workflowNextExecutionGroupCountModels.add(new WorkflowNextExecutionGroupCountModel("In 1 day",
						workflowNextExecutionGroupCountEntity.getGroup(),
						workflowNextExecutionGroupCountEntity.getCount()));
			}
			for (WorkflowNextExecutionGroupCountEntity workflowNextExecutionGroupCountEntity : futureGreaterOneDay.join()) {
				workflowNextExecutionGroupCountModels.add(new WorkflowNextExecutionGroupCountModel("Greater than 1 day",
						workflowNextExecutionGroupCountEntity.getGroup(),
						workflowNextExecutionGroupCountEntity.getCount()));
			}
		}
		return workflowNextExecutionGroupCountModels;


	}
}
