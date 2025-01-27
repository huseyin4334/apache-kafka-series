package com.example.transferservice.service;

import com.example.transferservice.model.TransferEntity;
import com.example.transferservice.repository.TransferRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.example.transferservice.error.TransferServiceException;
import com.example.transferservice.model.TransferRestModel;
import com.example.core.events.DepositRequestedEvent;
import com.example.core.events.WithdrawalRequestedEvent;

import javax.naming.ServiceUnavailableException;
import java.util.UUID;

@Service
public class TransferServiceImpl implements TransferService {
	private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

	private KafkaTemplate<String, Object> kafkaTemplate;
	private Environment environment;
	private RestTemplate restTemplate;
	private TransferRepository transferRepository;

	public TransferServiceImpl(KafkaTemplate<String, Object> kafkaTemplate, Environment environment,
			RestTemplate restTemplate, TransferRepository transferRepository) {
		this.kafkaTemplate = kafkaTemplate;
		this.environment = environment;
		this.restTemplate = restTemplate;
		this.transferRepository = transferRepository;
	}

	/*
	 * This method sends a withdrawal event to the withdrawal topic and a deposit
	 * This is a transactional method that sends a withdrawal event to the withdrawal topic and a deposit event to the deposit topic.
	 * It also calls a remote service that returns a response. If the response is not 200, it throws an exception.
	 * If the remote call is throwing an exception, kafka transaction manager will rollback the withdrawal event.
	 * Because the function is a single unit of work, it should be in 1 transaction.
	 * Then, if all function calls aren't successful, the transaction will be rolled back.
	 * ServiceUnavailableException only added for example purpose. Good practice to catch all exceptions.
	 */
	@Transactional(
			value = "kafkaTransactionManager",
			rollbackFor = {ServiceUnavailableException.class, TransferServiceException.class},
			noRollbackFor = {RuntimeException.class}
	)
	@Override
	public boolean transfer(TransferRestModel transferRestModel) {
		WithdrawalRequestedEvent withdrawalEvent = new WithdrawalRequestedEvent(transferRestModel.getSenderId(),
				transferRestModel.getRecepientId(), transferRestModel.getAmount());
		DepositRequestedEvent depositEvent = new DepositRequestedEvent(transferRestModel.getSenderId(),
				transferRestModel.getRecepientId(), transferRestModel.getAmount());

		try {
			kafkaTemplate.send(environment.getProperty("withdraw-money-topic", "withdraw-money-topic"),
					withdrawalEvent);
			LOGGER.info("Sent event to withdrawal topic.");

			// Business logic that causes and error
			callRemoteService();

			kafkaTemplate.send(environment.getProperty("deposit-money-topic", "deposit-money-topic"), depositEvent);
			LOGGER.info("Sent event to deposit topic");

		} catch (Exception ex) {
			LOGGER.error(ex.getMessage(), ex);
			throw new TransferServiceException(ex);
		}

		return true;
	}

	private ResponseEntity<String> callRemoteService() throws ServiceUnavailableException {
		String requestUrl = "http://localhost:8082/response/200";
		ResponseEntity<String> response = restTemplate.exchange(requestUrl, HttpMethod.GET, null, String.class);

		if (response.getStatusCode().value() == HttpStatus.SERVICE_UNAVAILABLE.value()) {
			throw new ServiceUnavailableException("Destination Microservice not availble");
		}

		if (response.getStatusCode().value() == HttpStatus.OK.value()) {
			LOGGER.info("Received response from mock service: " + response.getBody());
		}
		return response;
	}


	/*
	 	Scope of Transaction;
	 		- Transactional annotation is used to define the scope of a transaction.
	 		- If a method is annotated with @Transactional, it will be executed in a transaction.
	 		- If, get an exception, the transaction will be rolled back.
	 		- Kafka transaction manager open just 1 transaction for the annotated method.
	 		- If the method calls another method that is annotated with @Transactional, it won't be in the same transaction. (Because scope is changed)

	 	Local Transactions with kafkaTemplate;
	 		- When we enable transactions in Kafka, kafka template starts with internal kafka transaction manager.

	 		kafkaTemplate.executeInTransaction(transaction -> {
	 			kafkaTemplate.send("topic", "message");
	 			callRemoteService();
	 			kafkaTemplate.send("topic", "message");
	 			return true;
	 		});

	 		- When we get an exception from the remote service, the transaction will be rolled back.
	 		- BUT, if we get an exception after executeInTransaction method, the transaction won't be rolled back.
	 		- Because transaction is committed after the executeInTransaction method. (Scope is only for the method)

	 */

	/*
	 	Database transactions and kafka transactions;
	 	- Spring uses TransactionInterceptor to manage transactions.
	 	- TransactionInterceptor is an AOP interceptor that wraps the method with a transaction.
	 	- When we use @Transactional annotation, TransactionInterceptor wraps the method with a transaction.
	 	- TransactionManager is an interface that has 2 implementations: JpaTransactionManager and KafkaTransactionManager.
	 	- Bean names of them are "transactionManager" and "kafkaTransactionManager".

	 	@Transactional(value = "kafkaTransactionManager")
	 	void func() {
	 		repository.save(...);
	 		kafkaTemplate.send("topic", "message");
	 		callRemoteService();
	 		kafkaTemplate.send("topic", "message");
	 	}

	 	When we get an exception from the remote service, the kafka transaction will be rolled back.
	 	But db transaction won't be rolled back. Because, kafka transaction manager and db transaction manager are different.
	 	If we want to rollback db transaction, we should use @Transactional(value = "transactionManager") like this.

	 	----------------------------------------------

		@Transactional(value = "kafkaTransactionManager")
	 	void func() {
	 		callDbService();
	 		kafkaTemplate.send("topic", "message");
	 		callRemoteService();
	 		kafkaTemplate.send("topic", "message");
	 	}

	 	@Transactional(value = "transactionManager")
	 	void callDbService() {
	 		// Call remote service
	 	}

	 	If we get an exception from the remote service, the kafka transaction will be rolled back. But db transaction won't be rolled back.
	 	If we want to rollback db transaction, we should use @Transactional(value = "transactionManager") like this in the func() method.
	 	Also we should delete @Transactional annotation from callDbService() method.
	 */


	@Transactional(value = "transactionManager", rollbackFor = TransferServiceException.class)
	@Override
	public boolean transferV2(TransferRestModel transferRestModel) {
		WithdrawalRequestedEvent withdrawalEvent = new WithdrawalRequestedEvent(transferRestModel.getSenderId(),
				transferRestModel.getRecepientId(), transferRestModel.getAmount());
		DepositRequestedEvent depositEvent = new DepositRequestedEvent(transferRestModel.getSenderId(),
				transferRestModel.getRecepientId(), transferRestModel.getAmount());

		try {
			// save to db
			TransferEntity transferEntity = new TransferEntity();
			BeanUtils.copyProperties(transferRestModel, transferEntity);
			transferEntity.setTransferId(UUID.randomUUID().toString());
			transferRepository.save(transferEntity);

			kafkaTemplate.send(environment.getProperty("withdraw-money-topic", "withdraw-money-topic"),
					withdrawalEvent);
			LOGGER.info("Sent event to withdrawal topic.");

			// Business logic that causes and error
			callRemoteService();

			kafkaTemplate.send(environment.getProperty("deposit-money-topic", "deposit-money-topic"), depositEvent);
			LOGGER.info("Sent event to deposit topic");

		} catch (Exception ex) {
			LOGGER.error(ex.getMessage(), ex);
			throw new TransferServiceException(ex);
		}

		return true;
	}

}
