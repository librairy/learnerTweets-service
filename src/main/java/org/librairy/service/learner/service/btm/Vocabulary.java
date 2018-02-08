package org.librairy.service.learner.service.btm;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
public class Vocabulary {

    ConcurrentHashMap<String,Integer> wordDictionary   = new ConcurrentHashMap<>();
    ConcurrentHashMap<Integer,String> idDictionary     = new ConcurrentHashMap<>();

    public boolean exist(String word){
        return wordDictionary.containsKey(word);
    }

    public boolean exist(Integer id){
        return idDictionary.containsKey(id);
    }

    public Integer getId(String word){
        return wordDictionary.get(word);
    }

    public String getWord(Integer id){
        return idDictionary.get(id);
    }

    public Integer add(String word){
        Integer id = Integer.valueOf(getSize());
        this.wordDictionary.put(word,id);
        this.idDictionary.put(id,word);
        return id;
    }

    public Integer getSize(){
        return wordDictionary.size();
    }


    public ConcurrentHashMap<String, Integer> getWordDictionary() {
        return wordDictionary;
    }

    public void setWordDictionary(ConcurrentHashMap<String, Integer> wordDictionary) {
        this.wordDictionary = wordDictionary;
    }

    public ConcurrentHashMap<Integer, String> getIdDictionary() {
        return idDictionary;
    }

    public void setIdDictionary(ConcurrentHashMap<Integer, String> idDictionary) {
        this.idDictionary = idDictionary;
    }
}
