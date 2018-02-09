package org.librairy.service.learner.service;

import org.apache.avro.AvroRemoteException;
import org.librairy.service.learner.facade.model.Corpus;
import org.librairy.service.learner.facade.model.Hyperparameters;
import org.librairy.service.learner.facade.model.LearnerService;
import org.librairy.service.learner.service.btm.Document;
import org.librairy.service.learner.service.btm.MyBTM;
import org.librairy.service.learner.tools.CorpusReader;
import org.librairy.service.nlp.facade.model.Form;
import org.librairy.service.nlp.facade.model.PoS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class MyService implements LearnerService {

    private static final Logger LOG = LoggerFactory.getLogger(MyService.class);

    @Value("#{environment['RESOURCE_FOLDER']?:'${resource.folder}'}")
    String resourceFolder;

    String model              ;

    private ExecutorService executors;

    @PostConstruct
    public void setup() throws IOException {

        this.executors = Executors.newCachedThreadPool();

        LOG.info("Service initialized");
    }


    @Override
    public String train(Corpus corpus, Hyperparameters hyperparameters, Map<String, String> extra) throws AvroRemoteException {
        LOG.debug("ready to train a new model from corpus: " + corpus.getPath());

        executors.submit(() -> {
            Instant startModel  = Instant.now();
            File corpusFile = getFile(corpus.getPath());
            MyBTM btmAlgorithm  = new MyBTM(corpusFile.getAbsolutePath(), corpus.getLanguage().name().toLowerCase(), corpus.getSeparator(),corpus.getTextIndex());
            btmAlgorithm.train(hyperparameters.getTopics(), hyperparameters.getIterations(), hyperparameters.getAlpha(), hyperparameters.getBeta(),hyperparameters.getWordsPerTopic());
            Instant endModel    = Instant.now();
            LOG.info("Model created in: " + ChronoUnit.MINUTES.between(startModel,endModel) + "min " + (ChronoUnit.SECONDS.between(startModel,endModel)%60) + "secs");
        });

        String modelDescription = "Training a new BTM model from ["+corpus+"] with ["+hyperparameters+"] and [" + extra + "]";
        return modelDescription;
    }

    @Override
    public String inference(Corpus corpus, String s) throws AvroRemoteException {
        LOG.debug("ready to inference topic distributions from a given model: '" + model +"'  for corpus: " + corpus.getPath());

        executors.submit(() -> {


            File corpusFile = getFile(corpus.getPath());
            File model      = getFile(s);

            try {
                Instant startModel  = Instant.now();
                MyBTM btm = MyBTM.load(model.getAbsolutePath());
                btm.inference(corpusFile, corpus.getSeparator(), corpus.getTextIndex());
                Instant endModel    = Instant.now();
                LOG.info("Inference completed in: " + ChronoUnit.MINUTES.between(startModel,endModel) + "min " + (ChronoUnit.SECONDS.between(startModel,endModel)%60) + "secs");
            } catch (IOException e) {
                LOG.error("Error loading model: " + model, e);
            }
        });

        String modelDescription = "Infering topic distributions for ["+corpus+"] from model '"+model+"'";
        return modelDescription;
    }

    private File getFile(String path){
        File corpusFile = new File(path);
        if (!corpusFile.exists()){
            corpusFile = new File("/mnt/"+path);
        }
        if (!corpusFile.exists()) {
            LOG.warn("Corpus file not found: " + path);
            throw new RuntimeException("Missing corpus file");
        }
        return corpusFile;
    }

}
