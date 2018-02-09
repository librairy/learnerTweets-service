package org.librairy.service.ner.service;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.librairy.service.learner.facade.model.Corpus;
import org.librairy.service.learner.facade.model.Hyperparameters;
import org.librairy.service.learner.facade.model.Language;
import org.librairy.service.learner.service.MyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = MyService.class)
@WebAppConfiguration
public class MyServiceTest {

    private static final Logger LOG = LoggerFactory.getLogger(MyServiceTest.class);

    @Autowired
    MyService service;

    @Test
    @Ignore
    public void train() throws IOException, InterruptedException {


        Corpus corpus = Corpus.newBuilder().setPath("src/test/resources/1k-tweets.csv").setLanguage(Language.ES).setSeparator(";;").setTextIndex(1).build();

        Hyperparameters parameters = Hyperparameters.newBuilder().setAlpha(0.1).setBeta(0.001).setTopics(5).setIterations(1000).setWordsPerTopic(20).build();

        String result = service.train(corpus, parameters, new HashMap<>());

        LOG.info("Result: " + result);
        Thread.sleep(20000);
        LOG.info("completed!");
//        Assert.assertEquals(2, annotations.size());
    }

    @Test
    @Ignore
    public void inference() throws IOException, InterruptedException {


        Corpus corpus = Corpus.newBuilder().setPath("src/test/resources/1k-tweets.csv").setLanguage(Language.ES).setSeparator(";;").setTextIndex(1).build();

        String result = service.inference(corpus, "src/test/resources/1k-tweets.csv-model-5-0_1-0_001-1518187084497");

        LOG.info("Result: " + result);
        Thread.sleep(20000);
        LOG.info("completed!");
//        Assert.assertEquals(2, annotations.size());
    }
}