package org.librairy.service.ner.controllers;


import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.librairy.service.learner.controllers.AvroController;
import org.librairy.service.learner.facade.AvroClient;
import org.librairy.service.learner.facade.model.Corpus;
import org.librairy.service.learner.facade.model.Hyperparameters;
import org.librairy.service.learner.facade.model.Language;
import org.librairy.service.learner.service.MyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.io.IOException;
import java.util.HashMap;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {AvroController.class,MyService.class})
@WebAppConfiguration
public class AvroTest {

    private static final Logger LOG = LoggerFactory.getLogger(AvroTest.class);

    @Test
    @Ignore
    public void trainTest() throws InterruptedException, IOException {

        AvroClient client = new AvroClient();


        String host     = "localhost";
        Integer port    = 65112;

        client.open(host,port);

        Corpus corpus = Corpus.newBuilder().setPath("src/test/resources/input.csv").setLanguage(Language.ES).build();
        Hyperparameters hyperparameters = Hyperparameters.newBuilder().build();

        client.train(corpus, hyperparameters, new HashMap<>());

        client.close();

        Thread.sleep(5000);
    }

}