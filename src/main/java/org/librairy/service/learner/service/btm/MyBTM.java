package org.librairy.service.learner.service.btm;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 *
 *     Created from code of ffftzh
 */

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.avro.AvroRemoteException;
import org.apache.commons.lang.StringUtils;
import org.librairy.service.learner.tools.CorpusReader;
import org.librairy.service.learner.tools.OutputWriter;
import org.librairy.service.nlp.facade.AvroClient;
import org.librairy.service.nlp.facade.model.Form;
import org.librairy.service.nlp.facade.model.PoS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class MyBTM {

    private static final Logger LOG = LoggerFactory.getLogger(MyBTM.class);
    private String language;

    private String data_path;
    private String outputPath;

    private double alpha;
    private double beta;
    private int iter_num;
    private int numberOfTopics;

    private Vocabulary vocabulary = new Vocabulary();

    private int numberOfWords = 0;

    private Corpus corpus = null;
    ArrayList<HashMap<BiTerm, Integer>> bitermsPerDocument = new ArrayList<>();
    int[] bitermsPerDocumentCounter;


    ArrayList<BiTerm> bitermCorpus = new ArrayList<>();


    private int[] topicPerBiterm;
    private Matrix wordTopicMatrix;
    private int[] numberOfBitermsPerTopic;

    private HashMap<BiTerm, Double> bitermSum = new HashMap<>();

    public MyBTM(String data_path, String language, String separator, Integer textIndex) {
        this.data_path = data_path;
        this.language = language;
        this.load_data(language, separator, textIndex);
    }

    public void load_data(String language, String separator, Integer textIndex) {
        try {
            AvroClient client = new AvroClient();

            String host     = "nlp-"+language+"-service";
            Integer port    = 65111;

            client.open(host,port);


            CorpusReader reader = new CorpusReader(this.data_path);
//            String line;
            this.corpus = new Corpus();

            AtomicInteger counter = new AtomicInteger(0);
            reader.getReader().lines().parallel().forEach( line -> {
                String text = line;
                if (textIndex > 0){
                    StringTokenizer tokenizer = new StringTokenizer(line,separator);
                    Integer index = 0;
                    while(tokenizer.hasMoreTokens() && index < textIndex){
                        text = tokenizer.nextToken();
                    }
                }
                int current = counter.incrementAndGet();
                if (current % 100 == 0) LOG.info(current + " tweets processed");
                // NLP on texts
                String processedText = null;
                try {
                    processedText = client.process(text, Arrays.asList(new PoS[]{PoS.NOUN, PoS.VERB, PoS.ADVERB, PoS.ADJECTIVE}), Form.LEMMA);
                } catch (AvroRemoteException e) {
                    e.printStackTrace();
                }
                Document document = new Document(processedText,vocabulary);
                this.corpus.add(document);
            });

//            while ((line = reader.readLine()) != null) {
//
//                String text = line;
//                if (textIndex > 0){
//                    StringTokenizer tokenizer = new StringTokenizer(line,separator);
//                    Integer index = 0;
//                    while(tokenizer.hasMoreTokens() && index < textIndex){
//                        text = tokenizer.nextToken();
//                    }
//                }
//
//                // NLP on texts
//                String processedText = client.process(text, Arrays.asList(new PoS[]{PoS.NOUN, PoS.VERB, PoS.ADJECTIVE}), Form.LEMMA);
//                Document document = new Document(processedText,vocabulary);
//                this.corpus.add(document);
//            }
            reader.close();
            client.close();

            this.bitermsPerDocumentCounter = new int[this.corpus.getSize()];

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private MyBTM() {

    }

    public static MyBTM load(String modelPath) throws IOException {

        Path filePath= Paths.get(modelPath, "model.json");

        LOG.info(filePath.toString());

        ObjectMapper jsonMapper = new ObjectMapper();
        Model model = jsonMapper.readValue(filePath.toFile(), Model.class);


        MyBTM btm = new MyBTM();

        btm.alpha                   = model.getAlpha();
        btm.beta                    = model.getBeta();
        btm.iter_num                = model.getNumIterations();
        btm.numberOfTopics          = model.getNumberOfTopics();
        btm.wordTopicMatrix         = model.getWordTopicMatrix();
        btm.numberOfBitermsPerTopic = model.getNumberOfBitermsPerTopic();
        btm.numberOfWords           = model.getNumberOfWords();
        btm.language                = model.getLanguage();


        Path vocabularyPath = Paths.get(modelPath, "vocabulary.txt");

        btm.vocabulary = new Vocabulary();

        try (Stream<String> stream = Files.lines(vocabularyPath)) {

            stream.forEach(line -> btm.vocabulary.add(StringUtils.substringAfterLast(line, " ")));

        } catch (IOException e) {
            e.printStackTrace();
        }

        return btm;
    }


    private void print_parameter() {
        System.out.println("path:" + this.data_path
                + "\talpha:" + this.alpha
                + "\tbeta:" + this.beta
                + "\titer_num:" + this.iter_num
                + "\tnumberOfTopics:" + this.numberOfTopics
        );
    }



    private void init_model() {
        createBiterms();

        this.numberOfWords = this.vocabulary.getSize();

        this.topicPerBiterm     = new int[this.bitermCorpus.size()];
        this.wordTopicMatrix    = new Matrix(this.numberOfWords,this.numberOfTopics);
        this.numberOfBitermsPerTopic = new int[this.numberOfTopics];

        for(int bitermIndex = 0; bitermIndex<this.bitermCorpus.size(); bitermIndex++){
            int topicId = (int) (Math.random() * this.numberOfTopics);
            BiTerm biterm = this.bitermCorpus.get(bitermIndex);
            int term1 = biterm.getWord1();
            int term2 = biterm.getWord2();
            this.wordTopicMatrix.increment(term1, topicId);
            this.wordTopicMatrix.increment(term2, topicId);
            this.numberOfBitermsPerTopic[topicId] += 1;
            this.topicPerBiterm[bitermIndex] = topicId;
        }
    }

    private void createBiterms(){
        int docIndex = 0;
        for(Document doc:this.corpus.getDocuments()){
            HashMap<BiTerm, Integer> docBiterms = new HashMap<>();
            for(int word1:doc.getWords()){
                for(int word2:doc.getWords()){
                    if(word1<word2){
                        BiTerm biterm = new BiTerm(word1,word2);
                        if(!docBiterms.containsKey(biterm)){
                            docBiterms.put(biterm,0);
                        }
                        docBiterms.put(biterm,docBiterms.get(biterm)+1);
                        this.bitermCorpus.add(biterm);
                        this.bitermsPerDocumentCounter[docIndex] += 1;
                    }
                }
            }
            docIndex++;
            this.bitermsPerDocument.add(docBiterms);
        }
    }

    private void build_model() {

        for (int iter = 0; iter < this.iter_num; iter++) {
            long startTime = System.currentTimeMillis();

            for(int bitermIndex = 0; bitermIndex<this.topicPerBiterm.length; bitermIndex++) {
                int oldTopicId = this.topicPerBiterm[bitermIndex];
                BiTerm biterm = this.bitermCorpus.get(bitermIndex);
                int word1 = biterm.getWord1();
                int word2 = biterm.getWord2();
                this.wordTopicMatrix.decrement(word1,oldTopicId);
                this.wordTopicMatrix.decrement(word2,oldTopicId);
                this.numberOfBitermsPerTopic[oldTopicId] -= 1;

                int newTopicId = -1;

                double[] p = new double[this.numberOfTopics];
                for (int k = 0; k < this.numberOfTopics; k++) {
                    p[k] = (this.numberOfBitermsPerTopic[k] + alpha)
                            * (this.wordTopicMatrix.get(word1,k) + beta)
                            * (this.wordTopicMatrix.get(word2,k) + beta)
                            / Math.pow(this.numberOfBitermsPerTopic[k]*2 + this.numberOfWords * beta, 2);
                }

                for (int k = 1; k < this.numberOfTopics; k++) {
                    p[k] += p[k - 1];
                }

                double u = Math.random() * p[this.numberOfTopics - 1];
                for (int k = 0; k < this.numberOfTopics; k++) {
                    if (u < p[k]) {
                        newTopicId = k;
                        break;
                    }

                }
                this.wordTopicMatrix.increment(word1,newTopicId);
                this.wordTopicMatrix.increment(word2,newTopicId);

                this.numberOfBitermsPerTopic[newTopicId] += 1;

                this.topicPerBiterm[bitermIndex] = newTopicId;
            }

            LOG.info("finished iter :" + iter + "\tcost time:" + ((double) System.currentTimeMillis() - startTime) / 1000);
        }

    }

    private void saveTopicWords(int topWordNum) throws IOException {
        LOG.info("Saving topics ..");
        List<Topic> topics = new ArrayList<>();

        OutputWriter writer = new OutputWriter(this.outputPath + "topics.json");

        for (int topic_id = 0; topic_id < this.numberOfTopics; topic_id++) {
            HashMap<Integer, Double> oneLine = new HashMap<>();
            for (int word_id = 0; word_id < this.numberOfWords; word_id++) {
                double wordScore = ((double) this.wordTopicMatrix.get(word_id,topic_id)) / this.numberOfBitermsPerTopic[topic_id] / 2;
                oneLine.put(word_id, wordScore);
            }

            List<Map.Entry<Integer, Double>> maplist = new ArrayList<>(oneLine.entrySet());

            Collections.sort(maplist, (o1, o2) -> o2.getValue().compareTo(o1.getValue()));

//            writer.write("Topic:" + topic_id);
            Topic topic = new Topic();
            topic.setId(String.valueOf(topic_id));
            List<Word> words = new ArrayList<>();
            int count = 0;
            for (Map.Entry<Integer, Double> o1 : maplist) {
                Word word = new Word();
                word.setValue(this.vocabulary.getWord(o1.getKey()));
                word.setScore(o1.getValue());
                words.add(word);
//                writer.write("\t" + this.vocabulary.getWord(o1.getKey()) + "\t:" + o1.getValue());
                count++;
                if (count > topWordNum) {
                    break;
                }
            }
            topic.setWords(words);
            topics.add(topic);
//            writer.write("");

        }


        ObjectMapper jsonMapper = new ObjectMapper();
        jsonMapper.enable(SerializationFeature.INDENT_OUTPUT);

        writer.write(jsonMapper.writeValueAsString(topics));

        writer.close();
        LOG.info("topics saved");
    }

    private void saveVocabulary() throws IOException {
        LOG.info("Saving vocabulary ..");
        OutputWriter writer = new OutputWriter(this.outputPath + "vocabulary.txt");
        for (Integer key = 0; key < this.vocabulary.getSize(); key++) {
            writer.write(key + " " + this.vocabulary.getWord(key));
        }
        writer.close();
        LOG.info("vocabulary saved");
    }


    private double getSum(BiTerm biterm){
        if(!bitermSum.containsKey(biterm)) {
            double sum = 0;
            int word1 = biterm.getWord1();
            int word2 = biterm.getWord2();
            for (int topic_id = 0; topic_id < this.numberOfTopics; topic_id++) {
                sum += (this.numberOfBitermsPerTopic[topic_id] + alpha)
                        * (this.wordTopicMatrix.get(word1,topic_id) + beta)
                        * (this.wordTopicMatrix.get(word2,topic_id) + beta)
                        / Math.pow(this.numberOfBitermsPerTopic[topic_id] * 2 + this.numberOfWords * beta, 2);
            }
            bitermSum.put(biterm,sum);
        }
        return bitermSum.get(biterm);
    }

    private void saveTheta() throws IOException {
        LOG.info("Saving theta ..");
        OutputWriter writer = new OutputWriter(this.outputPath + "theta.txt");
        int docIndex = 0;
        for (HashMap<BiTerm,Integer> line : this.bitermsPerDocument) {
            if (line.isEmpty()) continue;
            double[] oneTheta = inference(line);
            writer.write(Arrays.toString(oneTheta));
            docIndex++;
        }
        writer.close();
        LOG.info("theta saved");
    }


    public void inference(File corpus, String separator, Integer textIndex) throws IOException {

        this.data_path = corpus.getAbsolutePath();
        this.load_data(language, separator, textIndex);

        File dataFile = new File(data_path);
        this.outputPath = (dataFile).getParentFile().getAbsolutePath() + "/"+dataFile.getName()+"-inference-"+ numberOfTopics+"-" + String.valueOf(alpha).replace(".","_")+ "-" + String.valueOf(beta).replace(".","_") + "-" + System.currentTimeMillis()+ "/";
        (new File(this.outputPath)).mkdirs();

        createBiterms();
        saveTheta();
    }

    public double[] inference(HashMap<BiTerm,Integer> bitermsInDocument){
        Integer numBiterms = bitermsInDocument.entrySet().stream().map(entry -> entry.getValue()).reduce((a, b) -> a + b).get();
        double[] oneTheta = new double[this.numberOfTopics];
        for(int topic_id = 0; topic_id<this.numberOfTopics; topic_id++) {
            double oneSum=0;
            for (BiTerm biterm : bitermsInDocument.keySet()) {
                int word1 = biterm.getWord1();
                int word2 = biterm.getWord2();
                Integer bitermFrequencyInDocument = bitermsInDocument.get(biterm);
                oneSum+=(((double)bitermFrequencyInDocument)/numBiterms)
                        *((
                        (this.numberOfBitermsPerTopic[topic_id] + alpha)
                                * (this.wordTopicMatrix.get(word1,topic_id) + beta)
                                * (this.wordTopicMatrix.get(word2,topic_id) + beta)
                                / Math.pow(this.numberOfBitermsPerTopic[topic_id]*2 + this.numberOfWords * beta, 2)
                )/(getSum(biterm)));

            }
            oneTheta[topic_id] = oneSum;
        }
        return oneTheta;
    }

    private void savePhi() throws IOException {
        LOG.info("Saving phi ..");
        OutputWriter writer = new OutputWriter(this.outputPath + "phi.txt");
        int topic_index = 0;
        for (int topic_id = 0; topic_id < this.numberOfTopics; topic_id++) {
            for (int word_id = 0; word_id < numberOfWords; word_id++) {
                writer.write(((this.wordTopicMatrix.get(word_id,topic_id) + beta) / (this.numberOfBitermsPerTopic[topic_id] * 2 + numberOfWords * beta))+" ",false);
            }

            writer.write("");
        }

        writer.close();
        LOG.info("phi saved");
    }




    private void save_result(Integer wordsPerTopic) {
        try {
            this.saveTopicWords(wordsPerTopic);
            this.saveTheta();
            this.saveVocabulary();
            this.savePhi();
            this.save_model();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void save_model() throws IOException {
        LOG.info("Saving model ..");
        OutputWriter writer = new OutputWriter(this.outputPath + "model.json");
        ObjectMapper jsonMapper = new ObjectMapper();

        // Save Model
        Model model = new Model();
        model.setAlpha(alpha);
        model.setBeta(beta);
        model.setNumberOfBitermsPerTopic(numberOfBitermsPerTopic);
        model.setNumberOfTopics(numberOfTopics);
        model.setNumberOfWords(numberOfWords);
        model.setNumIterations(iter_num);
        model.setWordTopicMatrix(wordTopicMatrix);
        model.setLanguage(language);


        writer.write(jsonMapper.writeValueAsString(model));

        writer.close();
        LOG.info("model saved");
    }


    public String train(int numberOfTopics, int iter_num, double alpha, double beta, Integer wordsPerTopic) {

        this.alpha = alpha;
        this.beta = beta;
        this.iter_num = iter_num;
        this.numberOfTopics = numberOfTopics;


        File dataFile = new File(data_path);

        this.outputPath = (dataFile).getParentFile().getAbsolutePath() + "/"+dataFile.getName()+"-model-"+ numberOfTopics+"-" + String.valueOf(alpha).replace(".","_")+ "-" + String.valueOf(beta).replace(".","_") + "-" + System.currentTimeMillis()+ "/";
        (new File(this.outputPath)).mkdirs();
        print_parameter();

        this.init_model();
        this.build_model();
        this.save_result(wordsPerTopic);

        return this.outputPath;
    }


    public double[] inference(String text) throws IOException {
        if (vocabulary == null) throw new RuntimeException("Load the model first!");
        Document document = new Document(text,vocabulary);
        return inference(document.getBiterms());
    }


    public static void main(String[] args){
        Integer numTopics   = 3;
        Double alpha        = 0.1;
        Double beta         = 0.001;
        Integer iterations  = 1000;

        try {
            MyBTM btm = MyBTM.load("src/test/resources/output");
            double[] topicDistribution = btm.inference("apple wednesday reported exceptional quarter nearly doubling income");
            LOG.info("Topics: " + topicDistribution);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}