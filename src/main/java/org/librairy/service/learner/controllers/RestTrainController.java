package org.librairy.service.learner.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.avro.AvroRemoteException;
import org.librairy.service.learner.facade.model.LearnerService;
import org.librairy.service.learner.facade.rest.model.TrainRequest;
import org.librairy.service.learner.facade.rest.model.TrainResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@RestController
@RequestMapping("/train")
@Api(tags="/train", description="build a new model")
public class RestTrainController {

    private static final Logger LOG = LoggerFactory.getLogger(RestTrainController.class);

    @Autowired
    LearnerService service;

    @PostConstruct
    public void setup(){

    }

    @PreDestroy
    public void destroy(){

    }

    @ApiOperation(value = "confirmation", nickname = "postTrain", response=TrainResult.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success", response = TrainResult.class),
    })
    @RequestMapping(method = RequestMethod.POST, produces = "application/json")
    public TrainResult train(@RequestBody TrainRequest request)  {
        try {
            return new TrainResult(service.train(request.getCorpus(), request.getParameters(), request.getExtra()));
        } catch (AvroRemoteException e) {
            throw new RuntimeException(e);
        }
    }

}
