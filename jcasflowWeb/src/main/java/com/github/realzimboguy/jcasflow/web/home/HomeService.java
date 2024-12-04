package com.github.realzimboguy.jcasflow.web.home;

import com.github.realzimboguy.jcasflow.engine.repo.dao.WorkflowInProgressDao;
import com.github.realzimboguy.jcasflow.engine.repo.dao.WorkflowNextExecutionDao;
import com.github.realzimboguy.jcasflow.engine.repo.entity.WorkflowInProgressGroupCountEntity;
import com.github.realzimboguy.jcasflow.engine.repo.entity.WorkflowNextExecutionGroupCountEntity;
import com.github.realzimboguy.jcasflow.web.home.model.WorkflowNextExecutionGroupCountModel;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class HomeService {

	private final WorkflowInProgressDao workflowInProgressDao;
	private final WorkflowNextExecutionDao workflowNextExecutionDao;

	public HomeService(WorkflowInProgressDao workflowInProgressDao, WorkflowNextExecutionDao workflowNextExecutionDao) {

		this.workflowInProgressDao = workflowInProgressDao;
		this.workflowNextExecutionDao = workflowNextExecutionDao;
	}

	public List<WorkflowInProgressGroupCountEntity> getWorkflowsInProgress() {

		return workflowInProgressDao.countInProgress();
	}

	public List<WorkflowNextExecutionGroupCountModel> getWorkflowsNextExecution() {

		List<WorkflowNextExecutionGroupCountModel> workflowNextExecutionGroupCountModels = new ArrayList<>();

		//calculate instant 5 min in the future
		Instant now = Instant.now();
		Instant inFiveMin = now.plusSeconds(300);
		Instant inThirtyMin = now.plusSeconds(1800);
		Instant inOneHour = now.plusSeconds(3600);
		Instant inOneDay = now.plusSeconds(86400);

		CompletableFuture<List<WorkflowNextExecutionGroupCountEntity>> futureInFiveMin = CompletableFuture.supplyAsync(() ->
			workflowNextExecutionDao.countNextExecution(now, inFiveMin)
		);
		CompletableFuture<List<WorkflowNextExecutionGroupCountEntity>> futureInThirtyMin = CompletableFuture.supplyAsync(() ->
			workflowNextExecutionDao.countNextExecution(inFiveMin, inThirtyMin)
		);

		CompletableFuture<List<WorkflowNextExecutionGroupCountEntity>> futureInOneHour = CompletableFuture.supplyAsync(() ->
			workflowNextExecutionDao.countNextExecution(inThirtyMin, inOneHour)
		);

		CompletableFuture<List<WorkflowNextExecutionGroupCountEntity>> futureInOneDay = CompletableFuture.supplyAsync(() ->
				workflowNextExecutionDao.countNextExecution(inOneHour, inOneDay)
		);

		CompletableFuture<List<WorkflowNextExecutionGroupCountEntity>> futureGreaterOneDay = CompletableFuture.supplyAsync(() ->
				workflowNextExecutionDao.countNextExecution(inOneHour, inOneDay)
		);


		CompletableFuture.allOf(futureInFiveMin, futureInThirtyMin, futureInOneHour, futureInOneDay, futureGreaterOneDay).join();

		//add all the futures to the results
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
		return workflowNextExecutionGroupCountModels;


	}
}
