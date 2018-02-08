package org.librairy.service.learner.service.btm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
public class Document {

    List<String> stop_words = Arrays.asList(new String[]{
            "RT","rt","https","http"
    });

    List<Integer> words = new ArrayList<>();

    public Document(String text, Vocabulary vocabulary){
        String[] tokens = text.split("\\s+");
        for (String token: tokens){
            if (!isValid(token)) continue;
            if (!vocabulary.exist(token)) {
                vocabulary.add(token);
            }
            words.add(vocabulary.getId(token));
        }
    }

    private boolean isValid(String token){
        if (!token.matches("[A-Za-z0-9]+")) return false;
        if (stop_words.contains(token)) return false;
        if (token.length() < 2) return false;
        return true;
    }

    public void add(Integer word){
        words.add(word);
    }

    public Integer get(Integer index){
        return words.get(index);
    }

    public Integer getSize(){
        return words.size();
    }

    public List<Integer> getWords(){
        return words;
    }

    public HashMap<BiTerm, Integer> getBiterms(){
        HashMap<BiTerm, Integer> docBiterms = new HashMap<>();
        for(int word1:getWords()){
            for(int word2:getWords()){
                if(word1<word2){
                    BiTerm biterm = new BiTerm(word1,word2);
                    if(!docBiterms.containsKey(biterm)){
                        docBiterms.put(biterm,0);
                    }
                    docBiterms.put(biterm,docBiterms.get(biterm)+1);
                }
            }
        }
        return docBiterms;
    }
}
