package org.librairy.service.learner.service.btm;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
public class Model {

    private Double alpha;
    private Double beta;
    private Integer numIterations;
    private Matrix wordTopicMatrix;
    private int numberOfTopics;
    private int[] numberOfBitermsPerTopic;
    private int numberOfWords;
    private String language;


    public Double getAlpha() {
        return alpha;
    }

    public void setAlpha(Double alpha) {
        this.alpha = alpha;
    }

    public Double getBeta() {
        return beta;
    }

    public void setBeta(Double beta) {
        this.beta = beta;
    }

    public Integer getNumIterations() {
        return numIterations;
    }

    public void setNumIterations(Integer numIterations) {
        this.numIterations = numIterations;
    }

    public Matrix getWordTopicMatrix() {
        return wordTopicMatrix;
    }

    public void setWordTopicMatrix(Matrix wordTopicMatrix) {
        this.wordTopicMatrix = wordTopicMatrix;
    }

    public int getNumberOfTopics() {
        return numberOfTopics;
    }

    public void setNumberOfTopics(int numberOfTopics) {
        this.numberOfTopics = numberOfTopics;
    }

    public int[] getNumberOfBitermsPerTopic() {
        return numberOfBitermsPerTopic;
    }

    public void setNumberOfBitermsPerTopic(int[] numberOfBitermsPerTopic) {
        this.numberOfBitermsPerTopic = numberOfBitermsPerTopic;
    }

    public int getNumberOfWords() {
        return numberOfWords;
    }

    public void setNumberOfWords(int numberOfWords) {
        this.numberOfWords = numberOfWords;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
}
